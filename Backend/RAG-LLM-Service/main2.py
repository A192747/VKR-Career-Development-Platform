"""
FastAPI application for RAG with txtai, exposing endpoints to answer questions and retrieve context.
"""

import os
from uuid import UUID
from typing import List, Dict, Any
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from txtai import Embeddings, LLM, RAG
from txtai.pipeline import Textractor
import logging
from glob import glob
import re

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI app
app = FastAPI(title="RAG API with txtai")

# Pydantic models for request/response
class QuestionRequest(BaseModel):
    question: str

class ContextRequest(BaseModel):
    topic: str

class AnswerResponse(BaseModel):
    answer: str

class ContextResponse(BaseModel):
    context: List[Dict[str, Any]]

# Helper class for auto IDs
class AutoId:
    @staticmethod
    def valid(uid):
        try:
            return UUID(str(uid))
        except ValueError:
            return isinstance(uid, int) or uid.isdigit()

# GraphContext class (simplified, without plotting)
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
        """
        Deduplicates input graph. This method merges nodes with topics having a similarity of more
        than the input threshold. This method also builds a dictionary of labels for each node.

        Args:
            graph: input graph
            threshold: topic merge threshold

        Returns:
            graph, labels
        """

        labels, topics, deletes = {}, {}, []
        for node in graph.scan():
            uid, topic = graph.attribute(node, "id"), graph.attribute(node, "topic")
            label = topic if AutoId.valid(uid) and topic else uid

            # Find similar topics
            topicnames = list(topics.keys())
            pid, pscore = (
                self.embeddings.similarity(label, topicnames)[0]
                if topicnames
                else (0, 0.0)
            )
            primary = topics[topicnames[pid]] if pscore >= threshold else None

            if not primary:
                # Set primary node
                labels[node], topics[label] = label, node
            else:
                # Copy edges to primary node
                logger.debug(f"DUPLICATE NODE: {label} - {topicnames[pid]}")
                edges = graph.edges(node)
                if edges:
                    for target, attributes in graph.edges(node).items():
                        if primary != target:
                            graph.addedge(primary, target, **attributes)

                # Add duplicate node to delete list
                deletes.append(node)

        # Delete duplicate nodes
        graph.delete(deletes)

        return graph, labels


# Main Application class
class Application:
    def __init__(self):
        self.textractor = None

        # LLM settings
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

        # Create RAG pipeline
        self.rag = RAG(
            self.embeddings,
            self.llm,
            system="You are a friendly assistant. You answer questions from users.",
            template=template,
            context=self.context,

        )

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
        for sections in self.extract(glob(f"{data}/**/*", recursive=True)):
            yield from sections

    def extract(self, inputs):
        if not self.textractor:
            self.textractor = Textractor(
                paragraphs=True,
                backend=os.environ.get("TEXTBACKEND", "available"),
            )
        return self.textractor(inputs)

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

    def get_answer(self, question: str) -> str:
        logger.info(f"Processing question: {question}")
        graph = GraphContext(self.embeddings, self.context)
        question, context = graph(question)

        logger.info("Forming context")
        if context:
            logger.info(f"Graph context retrieved: {len(context)} items {context}")

            context = [x["text"] for x in context]
        else:
            logger.info("Using vector search for context")
            context = [x["text"] for x in self.embeddings.search(question, self.context)]

        logger.info("Almost there")
        response = self.rag(
            question,
            context,
            maxlength=int(os.environ.get("MAXLENGTH", 4096)),
            stream=False,
        )
        logger.info("Its OK?")
        # Ensure response is a string
        if isinstance(response, dict) and 'answer' in response:
            return response['answer']
        return str(response)  # Fallback to string conversion if needed

    def get_context(self, topic: str) -> List[Dict[str, Any]]:
        logger.info(f"Retrieving context for topic: {topic}")
        graph = GraphContext(self.embeddings, self.context)
        _, context = graph(topic)

        if context:
            logger.info(f"Graph context retrieved: {len(context)} items")
            return context
        else:
            logger.info("Using vector search for context")
            return [
                {"id": x["id"], "text": x["text"]}
                for x in self.embeddings.search(topic, self.context)
            ]

# Global application instance
_app_instance = None

def get_app():
    global _app_instance
    if _app_instance is None:
        logger.info("Initializing application")
        _app_instance = Application()
    return _app_instance

# API Endpoints
@app.post("/answer", response_model=AnswerResponse)
async def answer_question(request: QuestionRequest):
    try:
        app = get_app()
        answer = app.get_answer(request.question)
        return AnswerResponse(answer=answer)
    except Exception as e:
        logger.error(f"Error processing question: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/context", response_model=ContextResponse)
async def get_topic_context(request: ContextRequest):
    try:
        app = get_app()
        context = app.get_context(request.topic)
        return ContextResponse(context=context)
    except Exception as e:
        logger.error(f"Error retrieving context: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    os.environ["TOKENIZERS_PARALLELISM"] = "false"
    get_app()
    uvicorn.run(app, host="0.0.0.0", port=8000)