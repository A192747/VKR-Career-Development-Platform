import os
import logging
from glob import glob
import re
from txtai import Embeddings, LLM, RAG
from txtai.pipeline import Textractor
from generators.rag.utils import GraphContext, AutoId
from typing import List, Dict, Any

logger = logging.getLogger(__name__)


class Application:
    def __init__(self):
        self.textractor = None
        default_api_base = "http://host.docker.internal:11434"
        default_llm_model = "ollama/llama3.1:8b"
        ollama_api_base = os.environ.get("OLLAMA_API_BASE", default_api_base)
        llm_model = os.environ.get("LLM", default_llm_model)

        self.llm = LLM(
            path=llm_model,
            api_base=ollama_api_base,
            method="litellm"
        )

        self.embeddings = self.load()
        self.context = int(os.environ.get("CONTEXT", 10))

        template = """
        Answer the following question using only the context below. Only include information
        specifically discussed.

        question: {question}
        context: {context} """

        self.rag = RAG(
            self.embeddings,
            self.llm,
            system="You are a friendly assistant. You answer questions from users.",
            template=template,
            context=self.context,
        )

        self.process_urls_from_file()

    def addurl(self, url):
        # Store number in index before indexing
        start = self.embeddings.count()

        # Add URL content to embeddings index, including URL as metadata
        sections = self.extract([url])
        for section in sections:
            section["source_url"] = url
        self.embeddings.upsert(sections)

        # Create LLM-generated topics
        self.infertopics(self.embeddings, start)

        # Save embeddings, if necessary
        self.persist(self.embeddings)

    def process_urls_from_file(self, filename='url.lst'):
        try:
            load_from_urls_list = os.environ.get("LOAD_CONTEXT_FROM_URLS_FILE", "True") == "True"

            if load_from_urls_list:
                with open(filename, 'r', encoding='utf-8') as file:
                    urls = file.readlines()  # Read all lines from the file

                for url in urls:
                    url = url.strip()  # Remove extra spaces and newlines
                    if url:  # Check that the line is not empty
                        logger.info(f"Adding {url} to index")
                        self.addurl(url)  # Apply addurl to each link

        except FileNotFoundError:
            logger.error(f"File '{filename}' not found.")
        except Exception as e:
            logger.error(f"An error occurred: {e}")

    def load(self):
        embeddings = None
        data = os.environ.get("DATA")
        database = os.environ.get("EMBEDDINGS", "neuml/txtai-wikipedia-slim")

        if database:
            logger.info(f"Loading index: {database}")
            embeddings = Embeddings()
            if embeddings.exists(database):
                embeddings.load(database)
            elif not os.path.isabs(database) and embeddings.exists(
                    cloud={"provider": "huggingface-hub", "container": database}
            ):
                embeddings.load(provider="huggingface-hub", container=database)
            else:
                logger.info(f"No index found: {database}")
                embeddings = None

        embeddings = embeddings if embeddings else self.create()

        if data:
            logger.info(f"Indexing data: {data}")
            embeddings.upsert(self.stream(data))
            self.infertopics(embeddings, 0)
            self.persist(embeddings)

        return embeddings

    def create(self):
        return Embeddings(
            autoid="uuid5",
            path="intfloat/e5-large",
            instructions={"query": "query: ", "data": "passage: "},
            content=True,
            graph={"approximate": False, "minscore": 0.7},
        )

    def stream(self, data):
        for file_path in glob(f"{data}/**/*", recursive=True):
            is_url = file_path.startswith(("http://", "https://"))
            for section in self.extract([file_path]):
                section["source_url"] = file_path if is_url else ""
                yield section

    def extract(self, inputs):
        if not self.textractor:
            self.textractor = Textractor(
                paragraphs=True,
                backend=os.environ.get("TEXTBACKEND", "available"),
            )
        sections = self.textractor(inputs)
        for section in sections:
            if isinstance(section, dict):
                section["source_url"] = section.get("source_url", "")
            else:
                section = {
                    "text": section,
                    "source_url": ""
                }
            yield section

    def infertopics(self, embeddings, start):
        if embeddings.graph:
            batch = []
            for uid in embeddings.graph.scan():
                rid = embeddings.graph.attribute(uid, "id")
                topic = embeddings.graph.attribute(uid, "topic")
                if AutoId.valid(rid) and not topic:
                    text = embeddings.graph.attribute(uid, "text") or rid
                    batch.append((uid, text))
                    if len(batch) == 32:
                        self.topics(embeddings, batch)
                        batch = []
            if batch:
                self.topics(embeddings, batch)

    def persist(self, embeddings):
        persist = os.environ.get("PERSIST")
        if persist:
            logger.info(f"Saving index: {persist}")
            embeddings.save(persist)

    def topics(self, embeddings, batch):
        prompt = """
                Create a simple, concise topic for the following text. Only return the topic name.

                Text:
                {text}"""

        prompts = []
        for uid, text in batch:
            text = text if re.search(r"\w+", text) else uid
            prompts.append([{"role": "user", "content": prompt.format(text=text)}])

        topicsbatch = os.environ.get("TOPICSBATCH")
        kwargs = {"batch_size": int(topicsbatch)} if topicsbatch else {}

        for x, topic in enumerate(self.llm(prompts, maxlength=int(os.environ.get("MAXLENGTH", 2048)), **kwargs)):
            uid = batch[x][0]
            embeddings.graph.addattribute(uid, "topic", topic)
            topics = embeddings.graph.topics
            if topics:
                if topic not in topics:
                    topics[topic] = []
                topics[topic].append(uid)

    def get_answer(self, question: str) -> Dict[str, Any]:
        """
        Processes a question and returns an answer along with source information, including URLs.

        Args:
            question (str): The question to process.

        Returns:
            Dict[str, Any]: Dictionary containing the answer and a list of sources with URLs.
        """
        logger.info(f"Processing question: {question}")
        graph = GraphContext(self.embeddings, self.context)
        question, context = graph(question)

        logger.info("Forming context")
        sources = []
        if context:
            logger.info(f"Graph context retrieved: {len(context)} items")
            sources = [
                {
                    "id": x["id"],
                    "text": x["text"],
                    "source_url": x.get("source_url", "")
                }
                for x in context
            ]
        else:
            logger.info("Using vector search for context")
            sources = [
                {
                    "id": x["id"],
                    "text": x["text"],
                    "source_url": x.get("source_url", "")
                }
                for x in self.embeddings.search(question, self.context)
            ]

        # Extract text for RAG context
        context_text = [x["text"] for x in sources]

        logger.info("Generating response")
        response = self.rag(
            question,
            context_text,
            maxlength=int(os.environ.get("MAXLENGTH", 4096)),
            stream=False,
        )

        logger.info("Processing response")
        answer = response['answer'] if isinstance(response, dict) and 'answer' in response else str(response)

        return {
            "answer": answer,
            "sources": [
                {
                    "id": source["id"],
                    "text": source["text"],
                    "url": source["source_url"]
                }
                for source in sources
            ]
        }

    def get_context(self, topic: str) -> List[Dict[str, Any]]:
        logger.info(f"Retrieving context for topic: {topic}")
        graph = GraphContext(self.embeddings, self.context)
        _, context = graph(topic)

        if context:
            logger.info(f"Graph context retrieved: {len(context)} items")
            return [
                {
                    "id": x["id"],
                    "text": x["text"],
                    "source_url": x.get("source_url", "")
                }
                for x in context
            ]
        else:
            logger.info("Using vector search for context")
            return [
                {
                    "id": x["id"],
                    "text": x["text"],
                    "source_url": x.get("source_url", "")
                }
                for x in self.embeddings.search(topic, self.context)
            ]


_app_instance = None


def get_app():
    global _app_instance
    if _app_instance is None:
        logger.info("Initializing application")
        _app_instance = Application()
    return _app_instance