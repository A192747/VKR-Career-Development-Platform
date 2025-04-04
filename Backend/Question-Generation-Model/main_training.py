# -*- coding: utf-8 -*-
import torch
from transformers import T5Tokenizer, T5ForConditionalGeneration
from torch.utils.data import Dataset, DataLoader
from datasets import load_dataset
import numpy as np
from tqdm import tqdm
import random

# 1. Кастомный класс для датасета
class QuestionGenerationDataset(Dataset):
    def __init__(self, contexts, questions, tokenizer, max_length=512):
        self.contexts = contexts
        self.questions = questions
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.contexts)

    def __getitem__(self, idx):
        context = str(self.contexts[idx])
        question = str(self.questions[idx])

        source_encoding = self.tokenizer(
            f"сгенерировать вопрос: {context}",
            max_length=self.max_length,
            padding="max_length",
            truncation=True,
            return_tensors="pt"
        )

        target_encoding = self.tokenizer(
            question,
            max_length=self.max_length,
            padding="max_length",
            truncation=True,
            return_tensors="pt"
        )

        return {
            "input_ids": source_encoding["input_ids"].squeeze(),
            "attention_mask": source_encoding["attention_mask"].squeeze(),
            "labels": target_encoding["input_ids"].squeeze()
        }

# 2. Функция для вычисления метрик
def compute_metrics(pred_ids, label_ids, tokenizer):
    pred_str = tokenizer.batch_decode(pred_ids, skip_special_tokens=True)
    label_str = tokenizer.batch_decode(label_ids, skip_special_tokens=True)
    accuracy = np.mean([p == l for p, l in zip(pred_str, label_str)])
    return {"accuracy": accuracy}

# 3. Функция для фильтрации датасета с сохранением разнообразия
def filter_dataset_by_keywords(dataset, keywords, non_related_ratio=0.3):
    # Собираем все записи, сохраняя разнообразие вопросов для одинаковых контекстов
    it_data = [d for d in dataset if any(kw.lower() in d["context"].lower() for kw in keywords)]
    non_it_data = [d for d in dataset if not any(kw.lower() in d["context"].lower() for kw in keywords)]

    if len(it_data) == 0:
        raise ValueError("Не найдено записей, связанных с IT. Проверьте ключевые слова.")

    # Вычисляем количество не-IT записей (30% от итогового объема)
    it_count = len(it_data)
    target_non_it_count = int(it_count * non_related_ratio / (1 - non_related_ratio))
    target_non_it_count = min(target_non_it_count, len(non_it_data))

    # Случайно выбираем не-IT записи
    selected_non_it_data = random.sample(non_it_data, target_non_it_count) if target_non_it_count > 0 else []

    # Объединяем IT и не-IT данные
    filtered_data = it_data + selected_non_it_data
    random.shuffle(filtered_data)  # Перемешиваем для равномерного распределения

    contexts = [d["context"] for d in filtered_data]
    questions = [d["question"] for d in filtered_data]

    return contexts, questions

# 4. Функция для генерации заданного количества вопросов
def generate_questions(model, tokenizer, context, num_questions=1, max_length=64):
    model.eval()
    questions = []
    input_text = f"сгенерировать вопрос: {context}"
    input_ids = tokenizer(input_text, return_tensors="pt").input_ids.to(model.device)

    with torch.no_grad():
        for _ in range(num_questions):
            output = model.generate(
                input_ids,
                max_length=max_length,
                do_sample=True,  # Случайный выбор из вероятностного распределения
                top_k=50,  # Ограничение на 50 самых вероятных токенов
                top_p=0.95,  # Фильтрация по кумулятивной вероятности
                temperature=0.7  # Контроль "креативности"
            )
            question = tokenizer.decode(output[0], skip_special_tokens=True)
            questions.append(question)
    return questions

# 5. Основной код обучения
def train_model(model_name, non_related_ratio=0.3, dataset_name="kuznetsoffandrey/sberquad"):
    # Загрузка модели и токенизатора
    tokenizer = T5Tokenizer.from_pretrained(model_name)
    model = T5ForConditionalGeneration.from_pretrained(model_name)

    # Настройка устройства
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)
    print(f"Using device: {device}")

    # Загрузка датасета со всеми сплитами
    dataset = load_dataset(dataset_name)

    # Ключевые слова для IT-тематики
    it_keywords = [
        "программирование", "технологии", "алгоритмы", "информатика",
        "компьютер", "софт", "программа", "код", "разработка",
        "база данных", "интернет", "сеть", "система", "искусственный интеллект"
    ]

    # Фильтрация каждого сплита с сохранением разнообразия
    print(f"Фильтрация датасета по IT-ключевым словам с добавлением {non_related_ratio * 100:.0f}% не-IT записей...")
    train_contexts, train_questions = filter_dataset_by_keywords(dataset["train"], it_keywords, non_related_ratio=non_related_ratio)
    val_contexts, val_questions = filter_dataset_by_keywords(dataset["validation"], it_keywords, non_related_ratio=non_related_ratio)
    test_contexts, test_questions = filter_dataset_by_keywords(dataset["test"], it_keywords, non_related_ratio=non_related_ratio)

    print(f"Train: найдено {len(train_contexts)} записей (IT + {non_related_ratio * 100:.0f}% не-IT)")
    print(f"Validation: найдено {len(val_contexts)} записей (IT + {non_related_ratio * 100:.0f}% не-IT)")
    print(f"Test: найдено {len(test_contexts)} записей (IT + {non_related_ratio * 100:.0f}% не-IT)")

    if len(train_contexts) == 0 or len(val_contexts) == 0 or len(test_contexts) == 0:
        raise ValueError("Один из сплитов пуст после фильтрации. Проверьте ключевые слова.")

    # Создание датасетов
    train_dataset = QuestionGenerationDataset(train_contexts, train_questions, tokenizer)
    val_dataset = QuestionGenerationDataset(val_contexts, val_questions, tokenizer)
    test_dataset = QuestionGenerationDataset(test_contexts, test_questions, tokenizer)

    # Создание загрузчиков данных
    batch_size = 4
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size)
    test_loader = DataLoader(test_dataset, batch_size=batch_size)

    # Настройка оптимизатора
    optimizer = torch.optim.AdamW(model.parameters(), lr=5e-5)
    num_epochs = 3

    # Цикл обучения
    for epoch in range(num_epochs):
        model.train()
        total_train_loss = 0

        for batch in tqdm(train_loader, desc=f"Epoch {epoch + 1} - Training"):
            optimizer.zero_grad()

            input_ids = batch["input_ids"].to(device)
            attention_mask = batch["attention_mask"].to(device)
            labels = batch["labels"].to(device)

            outputs = model(input_ids=input_ids, attention_mask=attention_mask, labels=labels)
            loss = outputs.loss
            total_train_loss += loss.item()

            loss.backward()
            optimizer.step()

            torch.cuda.empty_cache()

        avg_train_loss = total_train_loss / len(train_loader)
        print(f"Epoch {epoch + 1} - Average training loss: {avg_train_loss:.4f}")

        # Оценка на валидационной выборке
        model.eval()
        total_val_loss = 0
        total_val_accuracy = 0

        with torch.no_grad():
            for batch in tqdm(val_loader, desc=f"Epoch {epoch + 1} - Validation"):
                input_ids = batch["input_ids"].to(device)
                attention_mask = batch["attention_mask"].to(device)
                labels = batch["labels"].to(device)

                outputs = model(input_ids=input_ids, attention_mask=attention_mask, labels=labels)
                total_val_loss += outputs.loss.item()

                preds = model.generate(input_ids=input_ids, attention_mask=attention_mask, max_length=64)
                metrics = compute_metrics(preds, labels, tokenizer)
                total_val_accuracy += metrics["accuracy"]

        avg_val_loss = total_val_loss / len(val_loader)
        avg_val_accuracy = total_val_accuracy / len(val_loader)
        print(f"Validation Loss: {avg_val_loss:.4f}")
        print(f"Validation Accuracy: {avg_val_accuracy:.4f}")

    # Финальная оценка на тестовой выборке
    model.eval()
    total_test_loss = 0
    total_test_accuracy = 0

    with torch.no_grad():
        for batch in tqdm(test_loader, desc="Final Testing"):
            input_ids = batch["input_ids"].to(device)
            attention_mask = batch["attention_mask"].to(device)
            labels = batch["labels"].to(device)

            outputs = model(input_ids=input_ids, attention_mask=attention_mask, labels=labels)
            total_test_loss += outputs.loss.item()

            preds = model.generate(input_ids=input_ids, attention_mask=attention_mask, max_length=64)
            metrics = compute_metrics(preds, labels, tokenizer)
            total_test_accuracy += metrics["accuracy"]

    avg_test_loss = total_test_loss / len(test_loader)
    avg_test_accuracy = total_test_accuracy / len(test_loader)
    print(f"Test Loss: {avg_test_loss:.4f}")
    print(f"Test Accuracy: {avg_test_accuracy:.4f}")

    # Сохранение модели
    output_dir = "./ruT5-it-question-generator-sberquad"
    model.save_pretrained(output_dir)
    tokenizer.save_pretrained(output_dir)
    print(f"Model saved to {output_dir}")

    return model, tokenizer

# 6. Запуск обучения и тестирование
if __name__ == "__main__":
    model_name = "cointegrated/rut5-small"
    non_related_ratio = 0.3  # 30% не-IT записей
    dataset_name = "kuznetsoffandrey/sberquad"
    model, tokenizer = train_model(model_name, non_related_ratio, dataset_name)

    # Пример генерации вопросов
    test_context = "Программирование — это процесс создания компьютерных программ с использованием языков программирования."
    num_questions = 3
    generated_questions = generate_questions(model, tokenizer, test_context, num_questions)

    print(f"Контекст: {test_context}")
    print(f"Сгенерированные вопросы:")
    for i, q in enumerate(generated_questions, 1):
        print(f"{i}. {q}")