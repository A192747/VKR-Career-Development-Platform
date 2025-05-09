from generators.questions import QuestionGenerator
from fastapi import FastAPI, HTTPException, Depends, Query, BackgroundTasks
from api.models import (
    QuestionRequest, ContextRequest, AnswerResponse, ContextResponse,
    CompareRequest, ContentRequest, TestRequest, TestResponse,
    AnswerCheckRequest, AnswerCheckResponse, Topic, Material, Question,
    Base, TopicCreateRequest, TopicResponse, TopicsListResponse, QuestionsByTopicsRequest
)
from comparator.comparator import AnswerComparator
from generators.rag.core import get_app
from mykafka.service import KafkaService
from sqlalchemy import create_engine, func
from sqlalchemy.orm import Session, sessionmaker
from kafka.admin import KafkaAdminClient, NewTopic
import logging
import os
from typing import List, Optional
import asyncio
import uuid
import math
import uvicorn
from contextlib import asynccontextmanager
from functools import partial
import requests
from bs4 import BeautifulSoup

# Force UTF-8 encoding on Windows to avoid cp1251 issues
os.environ["PYTHONUTF8"] = "1"

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="RAG API with txtai")
DATABASE_URL = "postgresql+psycopg2://llm_user:llm_password@llm_postgres:5432/llm_db"
logger.info("Creating database engine")
try:
    engine = create_engine(DATABASE_URL)
    logger.info("Creating database tables")
    Base.metadata.create_all(engine)
except Exception as e:
    logger.error(f"Failed to initialize database: {e}")
    raise
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


_question_generator = None
_comparator = None
_kafka_service = None
_consumer_task = None


def init_components():
    global _question_generator, _comparator
    logger.info("Starting component initialization")

    # Check model path
    model_path = "./ruT5-it-question-generator-sberquad"
    logger.info(f"Checking model path: {model_path}")
    if os.path.isdir(model_path):
        logger.info(f"Directory {model_path} exists")
    else:
        logger.error(f"Directory {model_path} does not exist")
        raise Exception(f"Model path {model_path} not found")

    # Initialize QuestionGenerator with timeout
    logger.info("QuestionGenerator initialising")
    try:
        async def load_model():
            loop = asyncio.get_event_loop()
            return await loop.run_in_executor(None, lambda: QuestionGenerator(model_path))

        _question_generator = asyncio.run(asyncio.wait_for(load_model(), timeout=60))
        logger.info("QuestionGenerator initialised")
    except Exception as e:
        logger.error(f"Failed to initialize QuestionGenerator: {e}")
        raise

    # Initialize RAG
    logger.info("RAG initialising")
    try:
        get_app()
        logger.info("RAG initialised")
    except Exception as e:
        logger.error(f"Failed to initialize RAG: {e}")
        raise

    # Initialize AnswerComparator
    logger.info("AnswerComparator initialising")
    try:
        _comparator = AnswerComparator()
        logger.info("AnswerComparator initialised")
    except Exception as e:
        logger.error(f"Failed to initialize AnswerComparator: {e}")
        raise


async def init_kafka():
    global _kafka_service
    logger.info("KafkaService initialising")
    try:
        _kafka_service = KafkaService()
        logger.info("KafkaService initialised")
    except Exception as e:
        logger.error(f"Failed to initialize KafkaService: {e}")
        raise

    # Create Kafka topic
    logger.info("Checking Kafka topic 'content_processing'")
    try:
        admin_client = KafkaAdminClient(bootstrap_servers='kafka1:29092')
        topic_list = admin_client.list_topics()
        if 'content_processing' not in topic_list:
            logger.info("Creating Kafka topic 'content_processing'")
            topic = NewTopic(
                name='content_processing',
                num_partitions=1,
                replication_factor=1
            )
            admin_client.create_topics(new_topics=[topic], validate_only=False)
            logger.info("Kafka topic 'content_processing' created")
        else:
            logger.info("Kafka topic 'content_processing' already exists")
        admin_client.close()
    except Exception as e:
        logger.error(f"Error creating Kafka topic: {e}")
        raise


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting application lifespan")
    global _consumer_task
    try:
        await init_kafka()
        logger.info("Scheduling Kafka consumer task")
        loop = asyncio.get_event_loop()
        _consumer_task = loop.run_in_executor(None, partial(process_kafka_messages_sync, kafka_service=_kafka_service))
        yield
    except Exception as e:
        logger.error(f"Lifespan startup error: {e}")
        raise
    finally:
        logger.info("Shutting down application")
        if _consumer_task:
            _consumer_task.cancel()
            logger.info("Consumer task cancelled")


app.router.lifespan_context = lifespan


@app.post("/answer", response_model=AnswerResponse)
async def answer_question(request: QuestionRequest):
    try:
        app_instance = get_app()
        answer = app_instance.get_answer(request.question)
        return AnswerResponse(**answer)
    except Exception as e:
        logger.error(f"Error processing question: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/context", response_model=ContextResponse)
async def get_topic_context(request: ContextRequest):
    try:
        app_instance = get_app()
        context = app_instance.get_context(request.topic)
        return ContextResponse(context=context)
    except Exception as e:
        logger.error(f"Error retrieving context: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/compare")
async def compare(request: CompareRequest):
    try:
        global _comparator
        result = _comparator.check_answer_vector(request.userInput, request.answer)
        return {"similarity": result}
    except Exception as e:
        logger.error(f"Error comparing answers: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/process_content")
async def process_content(request: ContentRequest):
    try:
        content_data = {
            "id": str(uuid.uuid4()),
            "content": request.content,
            "url": request.url,
            "topic_ids": request.topic_ids or [1]
        }
        global _kafka_service
        if _kafka_service is None:
            raise HTTPException(status_code=503, detail="Kafka service not initialized")
        _kafka_service.send_content(content_data)
        return {"status": "Content queued for processing"}
    except Exception as e:
        logger.error(f"Error queuing content: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/create_test", response_model=TestResponse)
async def create_test(request: TestRequest, db: Session = Depends(get_db)):
    try:
        questions = db.query(Question).filter(Question.topic_id == request.topic_id).order_by(func.random()).limit(
            request.num_questions).all()
        return TestResponse(questions=[
            {"id": q.id, "question_text": q.question_text, "topic_id": q.topic_id}
            for q in questions
        ])
    except Exception as e:
        logger.error(f"Error creating test: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/check_answer", response_model=AnswerCheckResponse)
async def check_answer(request: AnswerCheckRequest, db: Session = Depends(get_db)):
    try:
        question = db.query(Question).filter(request.question_id == Question.id).first()
        if not question:
            raise HTTPException(status_code=404, detail="Question not found")
        similarity = _comparator.check_answer_vector(request.user_answer, question.answer_text)
        response = AnswerCheckResponse(similarity=similarity)
        if similarity < 0.6:
            material = db.query(Material).filter(Material.id == question.material_id).first()
            response.material = {
                "content": material.content,
                "url": material.url
            }
        return response
    except Exception as e:
        logger.error(f"Error checking answer: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/compare_answer", response_model=AnswerCheckResponse)
async def compare_answer(request: AnswerCheckRequest, db: Session = Depends(get_db)):
    try:
        # Находим вопрос по ID
        question = db.query(Question).filter(Question.id == request.question_id).first()
        if not question:
            raise HTTPException(status_code=404, detail="Question not found")

        # Сравниваем пользовательский ответ с эталонным
        similarity = _comparator.check_answer_vector(request.user_answer, question.answer_text)

        # Формируем ответ
        response = AnswerCheckResponse(similarity=similarity)

        # Если сходство меньше 0.6, добавляем информацию о материале
        if similarity < 0.6:
            material = db.query(Material).filter(Material.id == question.material_id).first()
            if material:
                response.material = {
                    "content": material.content,
                    "url": material.url
                }

        return response
    except Exception as e:
        logger.error(f"Error comparing answer: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/topics", response_model=TopicResponse)
async def create_topic(request: TopicCreateRequest, db: Session = Depends(get_db)):
    try:
        if not request.name or request.name.isspace():
            raise HTTPException(status_code=400, detail="Topic name cannot be empty")
        existing_topic = db.query(Topic).filter(Topic.name == request.name).first()
        if existing_topic:
            raise HTTPException(status_code=400, detail="Topic with this name already exists")
        new_topic = Topic(name=request.name)
        db.add(new_topic)
        db.commit()
        db.refresh(new_topic)
        return TopicResponse(id=new_topic.id, name=new_topic.name)
    except Exception as e:
        db.rollback()
        logger.error(f"Error creating topic: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/topics", response_model=TopicsListResponse)
async def get_topics(
        page: int = Query(1, ge=1, description="Page number (1-based)"),
        page_size: int = Query(10, ge=1, le=100, description="Number of topics per page"),
        db: Session = Depends(get_db)
):
    try:
        offset = (page - 1) * page_size
        topics_query = db.query(Topic).offset(offset).limit(page_size)
        topics = topics_query.all()
        total_items = db.query(func.count(Topic.id)).scalar()
        total_pages = math.ceil(total_items / page_size)
        return TopicsListResponse(
            topics=[TopicResponse(id=t.id, name=t.name) for t in topics],
            total_items=total_items,
            current_page=page,
            page_size=page_size,
            total_pages=total_pages
        )
    except Exception as e:
        logger.error(f"Error retrieving topics: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/health")
async def health_check():
    try:
        kafka_active = _kafka_service is not None
        return {
            "status": "healthy" if kafka_active else "unhealthy",
            "kafka_service_active": kafka_active
        }
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/get_questions", response_model=List[dict])
async def get_questions(request: QuestionsByTopicsRequest, db: Session = Depends(get_db)):
    try:
        result = []
        for topic_id in request.topics:
            # Проверяем, существует ли тема
            topic = db.query(Topic).filter(Topic.id == topic_id).first()
            if not topic:
                logger.warning(f"Topic ID {topic_id} not found")
                continue

            # Получаем случайные вопросы для темы
            questions = (
                db.query(Question)
                .filter(Question.topic_id == topic_id)
                .order_by(func.random())
                .limit(request.num_questions_per_topic)
                .all()
            )

            # Формируем ответ
            for question in questions:
                result.append({
                    "id": question.id,
                    "question_text": question.question_text,
                    "topic_id": question.topic_id
                })

        if not result:
            raise HTTPException(status_code=404, detail="No questions found for the specified topics")

        return result
    except Exception as e:
        logger.error(f"Error retrieving questions: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


def fetch_webpage_content(url):
    logger.info(f"Fetching content from URL: {url}")
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')
        # Extract main content (e.g., paragraphs)
        paragraphs = soup.find_all('p')
        content = ' '.join(p.get_text().strip() for p in paragraphs if p.get_text().strip())
        logger.info(f"Successfully fetched {len(content)} characters from {url}")
        return content
    except Exception as e:
        logger.error(f"Error fetching content from {url}: {str(e)}")
        return None


def process_kafka_messages_sync(kafka_service):
    logger.info("Starting Kafka consumer loop in thread")
    app_instance = get_app()
    while True:
        db = SessionLocal()
        try:
            logger.info("Polling for Kafka messages")
            for message in kafka_service.consume_content():
                logger.info(f"Processing Kafka message: {message}")
                try:
                    content = message.get("content")
                    url = message.get("url")
                    topic_ids = message.get("topic_ids", [1])
                    for topic_id in topic_ids:
                        if not db.query(Topic).filter(Topic.id == topic_id).first():
                            logger.error(f"Topic ID {topic_id} does not exist")
                            continue
                    # Fetch content if URL is provided and content is empty
                    processed_content = content
                    if not content and url:
                        logger.info(f"Fetching content for URL: {url}")
                        processed_content = fetch_webpage_content(url)
                    # Store content in materials
                    material = Material(content=processed_content, url=url)
                    db.add(material)
                    db.flush()
                    if processed_content and len(processed_content) > 0:
                        logger.info(
                            f"Processing content ({len(processed_content)} characters) for embeddings from {url or 'provided content'}")
                        start = app_instance.embeddings.count()
                        sections = [{"text": processed_content, "source_url": url}]
                        app_instance.embeddings.upsert(sections)
                        app_instance.infertopics(app_instance.embeddings, start)
                        app_instance.persist(app_instance.embeddings)
                    elif url:
                        logger.info(f"Adding {url} to RAG index")
                        processed_content = app_instance.addurl(url)
                    if not processed_content:
                        logger.warning(f"No content available for question generation from {url or 'message'}")
                        db.commit()  # Commit material even if no questions are generated
                        continue
                    # Generate questions using processed content
                    logger.info(f"Generating questions for content from {url or 'provided content'}")
                    questions = []
                    try:
                        questions = _question_generator.generate_questions(processed_content, num_questions=1)
                        logger.info(f"Generated {len(questions)} questions: {questions}")
                    except Exception as e:
                        logger.error(f"Error generating questions for {url or 'provided content'}: {str(e)}")
                        db.commit()  # Commit material even if question generation fails
                        continue
                    for topic_id in topic_ids:
                        for question_text in questions:
                            try:
                                answer_data = app_instance.get_answer(question_text, processed_content)
                                question = Question(
                                    topic_id=topic_id,
                                    question_text=question_text,
                                    answer_text=answer_data["answer"],
                                    material_id=material.id
                                )
                                db.add(question)
                            except Exception as e:
                                logger.error(f"Error creating question for topic {topic_id}: {str(e)}")
                                continue
                    db.commit()
                    logger.info(f"Processed content: {message['id']} for topics: {topic_ids}")
                except Exception as e:
                    db.rollback()
                    logger.error(f"Error processing Kafka message: {str(e)}")
                    continue
        except Exception as e:
            logger.error(f"Kafka consumer loop error: {str(e)}")
            import time
            time.sleep(5)
        finally:
            db.close()


async def process_kafka_messages():
    logger.info("Starting async Kafka consumer wrapper")
    loop = asyncio.get_event_loop()
    try:
        await loop.run_in_executor(None, partial(process_kafka_messages_sync, kafka_service=_kafka_service))
    except Exception as e:
        logger.error(f"Async consumer wrapper error: {str(e)}")
        await asyncio.sleep(5)
        await process_kafka_messages()


async def start_consumer():
    logger.info("Starting Kafka consumer task")
    try:
        await process_kafka_messages()
    except Exception as e:
        logger.error(f"Kafka consumer task failed: {e}")
        await asyncio.sleep(5)
        await start_consumer()


if __name__ == "__main__":
    logger.info("Starting application")
    try:
        init_components()
        logger.info("Component initialization completed")
        os.environ["TOKENIZERS_PARALLELISM"] = "false"
        logger.info("Starting Uvicorn server")
        uvicorn.run(app, host="0.0.0.0", port=8000)
    except Exception as e:
        logger.error(f"Failed to start application: {e}")
        raise