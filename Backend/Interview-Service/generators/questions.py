# questions.py
# -*- coding: utf-8 -*-
import torch
import logging
import time
from transformers import T5Tokenizer, T5ForConditionalGeneration
from func_timeout import func_timeout, FunctionTimedOut

logger = logging.getLogger(__name__)

def timeout(seconds):
    def decorator(func):
        def wrapper(*args, **kwargs):
            try:
                return func_timeout(seconds, func, args=args, kwargs=kwargs)
            except FunctionTimedOut:
                raise TimeoutError(f"Function {func.__name__} timed out after {seconds} seconds")
        return wrapper
    return decorator

class QuestionGenerator:
    def __init__(self, model_path):
        """Инициализация генератора вопросов с загрузкой модели"""
        self.model, self.tokenizer, self.device = self._load_model(model_path)

    def _load_model(self, model_path):
        """Загрузка сохраненной модели и токенизатора"""
        try:
            tokenizer = T5Tokenizer.from_pretrained(model_path)
            model = T5ForConditionalGeneration.from_pretrained(model_path)
            device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
            model.to(device)
            model.eval()
            logger.info(f"Модель загружена на устройство: {device}")
            return model, tokenizer, device
        except Exception as e:
            raise Exception(f"Ошибка при загрузке модели: {e}")

    @timeout(30)  # 30-second timeout for question generation
    def generate_questions(self, context, num_questions=3, max_length=64, max_input_tokens=256):
        """Генерация вопросов для заданного контекста"""
        logger.info("Starting question generation")
        if not context or not isinstance(context, str):
            logger.error("Контекст должен быть непустой строкой")
            raise ValueError("Контекст должен быть непустой строкой")

        if not isinstance(num_questions, int) or num_questions <= 0:
            logger.warning(f"Некорректное количество вопросов: {num_questions}, установлено 1")
            num_questions = 1

        # Truncate context to avoid exceeding token limit
        logger.info(f"Original context length: {len(context)} characters")
        start_time = time.time()
        encoded = self.tokenizer(context, truncation=True, max_length=max_input_tokens, return_tensors="pt")
        truncated_context = self.tokenizer.decode(encoded.input_ids[0], skip_special_tokens=True)
        logger.info(f"Truncated context length: {len(truncated_context)} characters, tokenization took {time.time() - start_time:.2f} seconds")

        questions = []
        input_text = f"сгенерировать вопрос: {truncated_context}"
        logger.info("Tokenizing input text")
        input_ids = self.tokenizer(input_text, return_tensors="pt").input_ids.to(self.device)
        logger.info(f"Input IDs shape: {input_ids.shape}")

        try:
            generation_start = time.time()
            with torch.no_grad():
                for i in range(num_questions):
                    logger.info(f"Generating question {i+1}/{num_questions}")
                    question_start = time.time()
                    output = self.model.generate(
                        input_ids,
                        max_length=max_length,
                        do_sample=True,
                        top_k=50,
                        top_p=0.95,
                        temperature=0.7
                    )
                    question = self.tokenizer.decode(output[0], skip_special_tokens=True)
                    questions.append(question)
                    logger.info(f"Question {i+1} generated in {time.time() - question_start:.2f} seconds")
            elapsed = time.time() - generation_start
            logger.info(f"Generated {len(questions)} questions in {elapsed:.2f} seconds: {questions}")
            return questions
        except Exception as e:
            logger.error(f"Error generating questions: {str(e)}")
            raise

if __name__ == "__main__":
    # Путь к сохраненной модели
    model_path = "../ruT5-it-question-generator-sberquad"

    try:
        # Инициализация генератора вопросов
        generator = QuestionGenerator(model_path)
    except Exception as e:
        logger.error(f"Не удалось инициализировать генератор: {e}")

    while True:
        # Запрос контекста у пользователя
        logger.info("\nВведите контекст (или 'выход' для завершения):")
        context = input().strip()

        if context.lower() == "выход":
            logger.info("Программа завершена.")
            break

        # Запрос количества вопросов
        try:
            logger.info("Сколько вопросов сгенерировать? (по умолчанию 1):")
            num_questions_input = input().strip()
            num_questions = int(num_questions_input) if num_questions_input else 1
        except ValueError:
            logger.error("Некорректный ввод. Используется значение по умолчанию (1).")
            num_questions = 1

        # Генерация вопросов
        try:
            questions = generator.generate_questions(context, num_questions)
            logger.info(f"\nКонтекст: {len(context)}")
            logger.info("Сгенерированные вопросы:")
            for i, question in enumerate(questions, 1):
                logger.info(f"{i}. {question}")
        except Exception as e:
            logger.error(f"Ошибка: {e}")