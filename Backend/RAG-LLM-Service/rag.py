b"""
Runs a RAG application backed by a txtai Embeddings database.
"""

import os
import platform
import re

from glob import glob
from io import BytesIO
from uuid import UUID

import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from PIL import Image
from tqdm import tqdm

import matplotlib.pyplot as plt
import networkx as nx

from txtai import Embeddings, LLM, RAG
from txtai.pipeline import Textractor



app = FastAPI()


class QuestionRequest(BaseModel):
    question: str

class EvaluateRequest(BaseModel):
    reference_answer: str
    user_answer: str

class TopicRequest(BaseModel):
    topics: list[str]
    num_questions: int

"""
Этот класс содержит методы для проверки, является ли идентификатор (UID) действительным автоидентификатором (UUID или числовой идентификатор).
"""
class AutoId:
    """
    Helper methods to detect txtai auto ids
    """

    @staticmethod
    def valid(uid):
        """
        Проверяет, является ли UID действительным AUTO ID (UUID или числовой идентификатор).

        Args:
            UID: идентификатор ввода

        Возвращает:
            Правда, если это аутоид, неверно иначе
        """

        # Check if this is a UUID
        try:
            return UUID(str(uid))
        except ValueError:
            pass

        # Return True if this is numeric, False otherwise
        return isinstance(uid, int) or uid.isdigit()


"""
Этот класс инициализирует необходимые компоненты приложения, включая языковую модель и эмбеддинги.
"""

class Application:

    def __init__(self):

        # Устанавливаем значения по умолчанию
        default_api_base = "http://host.docker.internal:11434"
        default_llm_model = "ollama/llama3.1:8b"

        # Получаем значения переменных окружения или используем значения по умолчанию
        ollama_api_base = os.environ.get("OLLAMA_API_BASE", default_api_base)
        llm_model = os.environ.get("LLM", default_llm_model)

        self.llm = LLM(
            path=llm_model,
            api_base=ollama_api_base,
            method="litellm"
        )


        # Load embeddings
        self.embeddings = self.load()

        # Context size
        self.context = int(os.environ.get("CONTEXT", 10))

        # Define prompt template
        template = """
                    Answer the following question using only the context below. Only include information
                    specifically discussed.
                    
                    question: {question}
                    context: {context} 
                    """

        # Create RAG pipeline
        self.rag = RAG(
            self.embeddings,
            self.llm,
            system="You are a friendly assistant. You answer questions from users.",
            template=template,
            context=self.context,
        )

        template_questions = """
                    Задай вопрос только используя текст далее. Используй только информацию относящуюся к данной теме.
                    
                    question: {question}
                    context: {context} 
                    """

        self.rag_questions = RAG(
            self.embeddings,
            self.llm,
            system="You are a friendly assistant. You give questions to users.",
            template=template_questions,
            context=self.context,
        )

        # Textractor instance (lazy loaded)
        self.textractor = None

        self.process_urls_from_file()

    def load(self):
        """
        Creates or loads an Embeddings instance.

        Returns:
            Embeddings
        """

        embeddings = None

        # Raw data path
        data = os.environ.get("DATA")

        # Embeddings database path
        database = os.environ.get("EMBEDDINGS", "neuml/txtai-wikipedia-slim")

        # Check for existing index
        if database:
            embeddings = Embeddings()
            if embeddings.exists(database):
                embeddings.load(database)
            elif not os.path.isabs(database) and embeddings.exists(
                cloud={"provider": "huggingface-hub", "container": database}
            ):
                embeddings.load(provider="huggingface-hub", container=database)
            else:
                embeddings = None

        # Default embeddings index if not found
        embeddings = embeddings if embeddings else self.create()

        # Add content from data directory, if provided
        if data:
            embeddings.upsert(self.stream(data))

            # Create LLM-generated topics
            self.infertopics(embeddings, 0)

            # Save embeddings, if necessary
            self.persist(embeddings)

        return embeddings


    def run_query(self, question):
        context = self.embeddings.search(question, limit=self.context)
        if context:
            context = [x["text"] for x in context]

        response = self.rag(
                            question,
                            context,
                            maxlength=int(os.environ.get("MAXLENGTH", 4096)),
                            stream=False,)
        return response


    def generate_questions_by_topics(self, topics, num_questions):
        questions = []

        for topic in topics:
            context = self.embeddings.search(topic, limit=self.context)
            if context:
                context = [x["text"] for x in context]
            for _ in range(num_questions):
                questions.append(self.rag_questions(f"Задай вопрос по теме {topic} на руссом языке.",
                                          f"Не задавай вопросы из этого списка: {questions}",
                                          context=context,
                                          maxlength=int(os.environ.get("MAXLENGTH", 4096)),
                                          stream=False
                                          )
                                 )
        return questions

    def evaluate(self, reference_answer, user_answer):
        prompt = f"""
                Вы опытный оценщик. Ваша задача - сравнить ответ пользователя с эталонным ответом
                и определить, насколько ответ пользователя похож на эталонный ответ.
            
                Эталонный ответ: {reference_answer}
            
                Ответ пользователя: {user_answer}
            
                Определите ключевые компоненты или точки, которые присутствуют в эталонном ответе, но отсутствуют в ответе пользователя.
                Предоставьте только краткое резюме того, что в ответе правильно и чего не хватает. 
                **Не повторяйте вопросы и не включайте дополнительные объяснения.**
                """

        return app_instance.llm(prompt)

    @app.post("/answer")
    async def ask_question(request: QuestionRequest):
        try:
            response = app_instance.run_query(request.question)
            return response
        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))

    @app.post("/questions")
    async def generate_questions(request: TopicRequest):
        try:
            questions = app_instance.generate_questions_by_topics(request.topics, request.num_questions)
            return questions
        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))

    @app.post("/evaluate")
    async def evaluate_answers(request: EvaluateRequest):
        try:
            result = app_instance.evaluate(request.reference_answer, request.user_answer)
            return result
        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))



    def addurl(self, url):

        # Store number in index before indexing
        start = self.embeddings.count()

        # Add file to embeddings index
        self.embeddings.upsert(self.extract(url))

        # Create LLM-generated topics
        self.infertopics(self.embeddings, start)

        # Save embeddings, if necessary
        self.persist(self.embeddings)

    def create(self):
        """
        Создает новый пустой индекс Embeddings.

        Возврат:
            Вложения
        """

        # Create empty embeddings database
        return Embeddings(
            autoid="uuid5",
            path="intfloat/e5-large",
            instructions={"query": "query: ", "data": "passage: "},
            content=True,
            graph={"approximate": False, "minscore": 0.7},
        )

    def stream(self, data):
        """
        Запускает конвейер textractor и передает извлеченный контент из каталога данных.

        Аргументы:
            данные: каталог входных данных
        """

        # Stream sections from content
        for sections in self.extract(glob(f"{data}/**/*", recursive=True)):
            yield from sections

    def extract(self, inputs):
        """
        Извлекайте разделы из входных данных с помощью конвейера Textractor.

        Аргументы:
            входы: входной контент

        Возврат:
            извлеченный контент
        """

        # Initialize textractor
        if not self.textractor:
            self.textractor = Textractor(
                paragraphs=True,
                backend=os.environ.get("TEXTBACKEND", "available"),
            )

        # Extract text
        return self.textractor(inputs)

    def infertopics(self, embeddings, start):
        """
        Обходит граф, связанный с экземпляром внедрения, и добавляет
        Темы, созданные LLM для каждой записи.

        Аргументы:
            вложения: база данных вложений
            начало: количество записей перед индексацией
        """

        if embeddings.graph:
            batch = []
            for uid in tqdm(
                embeddings.graph.scan(),
                desc="Inferring topics",
                total=embeddings.graph.count() - start,
            ):
                # Infer topic if id is an autoid and topic is empty
                rid = embeddings.graph.attribute(uid, "id")
                topic = embeddings.graph.attribute(uid, "topic")
                if AutoId.valid(rid) and not topic:
                    text = embeddings.graph.attribute(uid, "text")
                    text = text if text else rid

                    batch.append((uid, text))
                    if len(batch) == 32:
                        self.topics(embeddings, batch)
                        batch = []

            if batch:
                self.topics(embeddings, batch)

    def persist(self, embeddings):
        """
        Сохраняет индекс внедрения, если установлен параметр PERSIST.

        Аргументы:
            вложения: вложения для сохранения
        """

        persist = os.environ.get("PERSIST")
        if persist:
            embeddings.save(persist)

    def topics(self, embeddings, batch):
        """
        Генерирует партию тем с LLM. Темы установлены непосредственно на встраивании
        пример.

        Args:
            Встроенные: база данных Entricdings
            партия: партия (id, текст) элементов
        """

        prompt = """
                Create a simple, concise topic for the following text. Only return the topic name.
                
                Text:
                {text}"""

        # Build batch of prompts
        prompts = []
        for uid, text in batch:
            text = text if re.search(r"\w+", text) else uid
            prompts.append([{"role": "user", "content": prompt.format(text=text)}])

        # Check if batch processing is enabled
        topicsbatch = os.environ.get("TOPICSBATCH")
        kwargs = {"batch_size": int(topicsbatch)} if topicsbatch else {}

        # Run prompt batch and set topics
        for x, topic in enumerate(
            self.llm(
                prompts, maxlength=int(os.environ.get("MAXLENGTH", 2048)), **kwargs
            )
        ):
            # Set topic attribute
            uid = batch[x][0]
            embeddings.graph.addattribute(uid, "topic", topic)

            # Add topic to topics
            topics = embeddings.graph.topics
            if topics:
                if topic not in topics:
                    topics[topic] = []

                topics[topic].append(uid)

    def process_urls_from_file(self, filename='url.lst'):
        try:
            with open(filename, 'r', encoding='utf-8') as file:
                urls = file.readlines()  # Считываем все строки из файла

            for url in urls:
                url = url.strip()  # Убираем лишние пробелы и символы новой строки
                if url:  # Проверяем, что строка не пустая
                    print()
                    print()
                    print(f"Adding {url} to index")
                    print()
                    self.addurl(url)  # Применяем метод addrul к каждой ссылке

        except FileNotFoundError:
            print(f"Файл '{filename}' не найден.")
        except Exception as e:
            print(f"Произошла ошибка: {e}")


if __name__ == "__main__":
    app_instance = Application()
    print("RAG SERVER STARTING")
    uvicorn.run(app, host="0.0.0.0", port=8000)
    print("SOME TEXT")
