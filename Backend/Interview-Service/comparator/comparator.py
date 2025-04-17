from sentence_transformers import SentenceTransformer, util

class AnswerComparator:
    def __init__(self, model_name: str = 'paraphrase-multilingual-MiniLM-L12-v2'):
        """
        Инициализация компаратора ответов с загрузкой модели.

        Args:
            model_name (str): Название модели для sentence-transformers.
        """
        self.model = SentenceTransformer(model_name)

    def check_answer_vector(self, user_answer: str, correct_answer: str) -> float:
        """
        Сравнивает пользовательский ответ с эталонным, используя векторные представления.

        Args:
            user_answer (str): Ответ пользователя.
            correct_answer (str): Эталонный ответ.

        Returns:
            float: Оценка сходства в диапазоне [0, 1].
        """
        # Получаем векторные представления (эмбеддинги)
        user_embedding = self.model.encode(user_answer, convert_to_tensor=True)
        correct_embedding = self.model.encode(correct_answer, convert_to_tensor=True)

        # Вычисляем косинусное сходство
        similarity = util.cos_sim(user_embedding, correct_embedding).item()

        # Нормализуем результат в диапазон [0, 1]
        return max(0.0, min(1.0, similarity))


# Пример использования
if __name__ == "__main__":
    input("Жду ввод")
    user_answer = "Солнце излучает яркий свет."
    correct_answer = "Солнце светит очень ярко."
    comparator = AnswerComparator('paraphrase-multilingual-MiniLM-L12-v2')
    score = comparator.check_answer_vector(user_answer, correct_answer)
    print(f"Оценка: {score * 100:.1f}%")  # Вывод: Оценка: ~80-90% (зависит от модели)