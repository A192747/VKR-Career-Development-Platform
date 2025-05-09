from uuid import UUID
import logging
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

class AutoId:
    @staticmethod
    def valid(uid):
        try:
            return UUID(str(uid))
        except ValueError:
            return isinstance(uid, int) or uid.isdigit()

class GraphContext:
    def __init__(self, embeddings, context):
        self.embeddings = embeddings
        self.context = context

    def __call__(self, question):
        query, concepts, context = self.parse(question)
        if self.embeddings.graph and (query or concepts):
            path = self.path(query, concepts)
            graph = self.embeddings.graph.search(path, graph=True)
            if graph.count():
                context = [
                    {
                        "id": graph.attribute(node, "id"),
                        "text": graph.attribute(node, "text"),
                    }
                    for node in list(graph.scan())
                ]
                if context:
                    default = (
                        "Write a title and text summarizing the context.\n"
                        f"Include the following concepts: {concepts} if they're mentioned in the context."
                    )
                    question = query if query else default
        return question, context

    def parse(self, question):
        prefix = "gq: "
        query, concepts, context = None, None, None
        if "->" in question or question.strip().lower().startswith(prefix):
            concepts = [x.strip() for x in question.strip().lower().split("->")]
            if prefix in concepts[-1]:
                query, concepts = concepts[-1], concepts[:-1]
                query = [x.strip() for x in query.split(prefix, 1)]
                if query[0]:
                    concepts.append(query[0])
                if len(query) > 1:
                    query = query[1]
        return query, concepts, context


    def path(self, question, concepts):
        ids = []
        if concepts:
            for concept in concepts:
                uid = self.embeddings.search(concept, 1)[0]["id"]
                ids.append(f'({{id: "{uid}"}})')
        else:
            for x in self.embeddings.search(question, 3):
                ids.append(f"({{id: \"{x['id']}\"}})")
        ids = "-[*1..4]->".join(ids)
        query = f"MATCH P={ids} RETURN P LIMIT {self.context}"
        logger.debug(query)
        return query

    def deduplicate(self, graph, threshold):
        labels, topics, deletes = {}, {}, []
        for node in graph.scan():
            uid, topic = graph.attribute(node, "id"), graph.attribute(node, "topic")
            label = topic if AutoId.valid(uid) and topic else uid

            topicnames = list(topics.keys())
            pid, pscore = (
                self.embeddings.similarity(label, topicnames)[0]
                if topicnames
                else (0, 0.0)
            )
            primary = topics[topicnames[pid]] if pscore >= threshold else None

            if not primary:
                labels[node], topics[label] = label, node
            else:
                logger.debug(f"DUPLICATE NODE: {label} - {topicnames[pid]}")
                edges = graph.edges(node)
                if edges:
                    for target, attributes in graph.edges(node).items():
                        if primary != target:
                            graph.addedge(primary, target, **attributes)
                deletes.append(node)

        graph.delete(deletes)
        return graph, labels