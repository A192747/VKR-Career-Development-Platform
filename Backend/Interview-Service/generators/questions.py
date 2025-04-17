# questions.py
# -*- coding: utf-8 -*-
import torch
from transformers import T5Tokenizer, T5ForConditionalGeneration


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
            print(f"Модель загружена на устройство: {device}")
            return model, tokenizer, device
        except Exception as e:
            raise Exception(f"Ошибка при загрузке модели: {e}")

    def generate_questions(self, context, num_questions=3, max_length=64):
        """Генерация вопросов для заданного контекста"""
        if not context or not isinstance(context, str):
            raise ValueError("Контекст должен быть непустой строкой")

        if not isinstance(num_questions, int) or num_questions <= 0:
            num_questions = 3

        questions = []
        input_text = f"сгенерировать вопрос: {context}"
        input_ids = self.tokenizer(input_text, return_tensors="pt").input_ids.to(self.device)

        try:
            with torch.no_grad():
                for _ in range(num_questions):
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
            return questions
        except Exception as e:
            raise Exception(f"Ошибка при генерации вопросов: {e}")



if __name__ == "__main__":
    # Путь к сохраненной модели
    model_path = "../ruT5-it-question-generator-sberquad"

    try:
        # Инициализация генератора вопросов
        generator = QuestionGenerator(model_path)
    except Exception as e:
        print(f"Не удалось инициализировать генератор: {e}")

    while True:
        # Запрос контекста у пользователя
        print("\nВведите контекст (или 'выход' для завершения):")
        context = input().strip()

        if context.lower() == "выход":
            print("Программа завершена.")
            break

        # Запрос количества вопросов
        try:
            print("Сколько вопросов сгенерировать? (по умолчанию 3):")
            num_questions_input = input().strip()
            num_questions = int(num_questions_input) if num_questions_input else 3
        except ValueError:
            print("Некорректный ввод. Используется значение по умолчанию (3).")
            num_questions = 3

        # Генерация вопросов
        try:
            questions = generator.generate_questions(context, num_questions)
            print(f"\nКонтекст: {context}")
            print("Сгенерированные вопросы:")
            for i, question in enumerate(questions, 1):
                print(f"{i}. {question}")
        except Exception as e:
            print(f"Ошибка: {e}")