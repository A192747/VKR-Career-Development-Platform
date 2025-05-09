import torch
from transformers import T5ForConditionalGeneration, T5Tokenizer


def load_model_and_tokenizer(model_path):
    """
    Загрузка модели и токенизатора из указанной папки.

    Args:
    - model_path (str): Путь к папке с сохранённой моделью.

    Returns:
    - model (T5ForConditionalGeneration): Загруженная модель.
    - tokenizer (T5Tokenizer): Загруженный токенизатор.
    """
    # Загрузка токенизатора
    tokenizer = T5Tokenizer.from_pretrained(model_path)

    # Загрузка модели
    model = T5ForConditionalGeneration.from_pretrained(model_path)

    # Переключение модели в режим оценки (инференс)
    model.eval()

    return model, tokenizer


def generate_question(model, tokenizer, context, max_length=64):
    """
    Генерация вопроса на основе контекста с использованием загруженной модели.

    Args:
    - model (T5ForConditionalGeneration): Модель для генерации вопросов.
    - tokenizer (T5Tokenizer): Токенизатор для преобразования текста в токены.
    - context (str): Контекст, на основе которого генерируется вопрос.
    - max_length (int, optional): Максимальная длина генерируемого вопроса. Defaults to 64.

    Returns:
    - question (str): Сгенерированный вопрос.
    """
    # Префикс для генерации вопросов (определён в задаче)
    input_text = f"генерировать вопрос: {context}"

    # Токенизация входного текста
    input_ids = tokenizer(input_text, return_tensors="pt").input_ids

    # Генерация вопроса без градиентов (поскольку мы в режиме оценки)
    with torch.no_grad():
        output = model.generate(input_ids, max_length=max_length)

    # Декодирование токенов обратно в текст, пропуская специальные токены
    question = tokenizer.decode(output[0], skip_special_tokens=True)

    return question


if __name__ == "__main__":
    # Путь к папке с сохранённой моделью
    model_path = "./ruT5-question-generator"

    # Загрузка модели и токенизатора
    model, tokenizer = load_model_and_tokenizer(model_path)

    # Тестовый контекст
    test_context = "LinkedList: Двусвязный список, который обеспечивает эффективное добавление/удаление элементов. Может использоваться как список и очередь.\
                    HashSet: Использует хеш-таблицу для хранения уникальных элементов. Не гарантирует порядок элементов.\
                    LinkedHashSet: Расширяет `HashSet`, сохраняя порядок вставки элементов.\
                    TreeSet: Хранит элементы в отсортированном и восходящем порядке. Использует красно-черное дерево.\
                    HashMap: Хеш-таблица для хранения пар ключ/значение. Не поддерживает упорядоченность ключей или значений."

    # Генерация вопроса
    generated_question = generate_question(model, tokenizer, test_context)

    # Вывод результата
    print(f"Сгенерированный вопрос: {generated_question}")