# -*- coding: utf-8 -*-
import torch
from transformers import T5Tokenizer, T5ForConditionalGeneration


def load_model(model_path):
    """Загрузка сохраненной модели и токенизатора"""
    tokenizer = T5Tokenizer.from_pretrained(model_path)
    model = T5ForConditionalGeneration.from_pretrained(model_path)
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)
    model.eval()
    print(f"Модель загружена на устройство: {device}")
    return model, tokenizer, device


def generate_questions(model, tokenizer, context, device, num_questions=3, max_length=64):
    """Генерация вопросов для заданного контекста"""
    questions = []
    input_text = f"сгенерировать вопрос: {context}"
    input_ids = tokenizer(input_text, return_tensors="pt").input_ids.to(device)

    with torch.no_grad():
        for _ in range(num_questions):
            output = model.generate(
                input_ids,
                max_length=max_length,
                do_sample=True,
                top_k=50,
                top_p=0.95,
                temperature=0.7
            )
            question = tokenizer.decode(output[0], skip_special_tokens=True)
            questions.append(question)
    return questions


def main():
    # Путь к сохраненной модели
    model_path = "./ruT5-it-question-generator-sberquad"

    # Загрузка модели
    try:
        model, tokenizer, device = load_model(model_path)
    except Exception as e:
        print(f"Ошибка при загрузке модели: {e}")
        return

    while True:
        # Запрос контекста у пользователя
        print("\nВведите контекст (или 'выход' для завершения):")
        context = input().strip()

        if context.lower() == "выход":
            print("Программа завершена.")
            break

        if not context:
            print("Контекст не может быть пустым. Попробуйте снова.")
            continue

        # Запрос количества вопросов
        try:
            print("Сколько вопросов сгенерировать? (по умолчанию 3):")
            num_questions_input = input().strip()
            num_questions = int(num_questions_input) if num_questions_input else 3
            if num_questions <= 0:
                print("Количество вопросов должно быть больше 0. Используется значение по умолчанию (3).")
                num_questions = 3
        except ValueError:
            print("Некорректный ввод. Используется значение по умолчанию (3).")
            num_questions = 3

        # Генерация вопросов
        try:
            generated_questions = generate_questions(model, tokenizer, context, device, num_questions)
            print(f"\nКонтекст: {context}")
            print("Сгенерированные вопросы:")
            for i, question in enumerate(generated_questions, 1):
                print(f"{i}. {question}")
        except Exception as e:
            print(f"Ошибка при генерации вопросов: {e}")


if __name__ == "__main__":
    main()