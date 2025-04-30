from generators.questions import QuestionGenerator
from fastapi import FastAPI, HTTPException, Depends, Query
from api.models import (
    QuestionRequest, ContextRequest, AnswerResponse, ContextResponse,
    CompareRequest, ContentRequest, TestRequest, TestResponse,
    AnswerCheckRequest, AnswerCheckResponse, Topic, Material, Question,
    Base, TopicCreateRequest, TopicResponse, TopicsListResponse
)
from comparator.comparator import AnswerComparator
from generators.rag.core import get_app
from mykafka.service import KafkaService
from sqlalchemy import create_engine, func
from sqlalchemy.orm import Session, sessionmaker
from kafka.admin import KafkaAdminClient, NewTopic
import logging
import os
import asyncio
import uuid
import math

# Force UTF-8 encoding on Windows to avoid cp1251 issues
os.environ["PYTHONUTF8"] = "1"

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="RAG API with txtai")
DATABASE_URL = "postgresql+psycopg2://llm_user:llm_password@llm_postgres:5432/llm_db"
engine = create_engine(DATABASE_URL)
Base.metadata.create_all(engine)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

_question_generator = None
_comparator = None
_kafka_service = KafkaService()

def init():
    global _question_generator, _comparator
    model_path = "./ruT5-it-question-generator-sberquad"
    if os.path.isdir(model_path):
        logger.info(f"Directory {model_path} exists")
    else:
        logger.error(f"Directory {model_path} does not exist")
        raise Exception(f"Model path {model_path} not found")

    logger.info("QuestionGenerator initialising")
    _question_generator = QuestionGenerator(model_path)
    logger.info("QuestionGenerator initialised")

    logger.info("RAG initialising")
    get_app()
    logger.info("RAG initialised")

    logger.info("AnswerComparator initialising")
    _comparator = AnswerComparator()
    logger.info("AnswerComparator initialised")

    # Create Kafka topic if it doesn't exist
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
            "topic_ids": request.topic_ids or [1]  # Default to [1] if topic_ids is empty
        }
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

@app.post("/topics", response_model=TopicResponse)
async def create_topic(request: TopicCreateRequest, db: Session = Depends(get_db)):
    try:
        # Check if topic name is empty or whitespace
        if not request.name or request.name.isspace():
            raise HTTPException(status_code=400, detail="Topic name cannot be empty")

        # Check for duplicate topic name
        existing_topic = db.query(Topic).filter(Topic.name == request.name).first()
        if existing_topic:
            raise HTTPException(status_code=400, detail="Topic with this name already exists")

        # Create new topic
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
        # Calculate offset
        offset = (page - 1) * page_size

        # Query topics with pagination
        topics_query = db.query(Topic).offset(offset).limit(page_size)
        topics = topics_query.all()

        # Get total count for pagination metadata
        total_items = db.query(func.count(Topic.id)).scalar()
        total_pages = math.ceil(total_items / page_size)

        # Prepare response
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
        # Check Kafka consumer status
        consumer_active = False
        try:
            # Attempt to consume a single message to verify consumer
            for _ in _kafka_service.consume_content():
                consumer_active = True
                break
        except Exception as e:
            logger.error(f"Kafka consumer health check failed: {e}")
            consumer_active = False

        return {
            "status": "healthy" if consumer_active else "unhealthy",
            "kafka_consumer_active": consumer_active
        }
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))

async def process_kafka_messages():
    app_instance = get_app()
    db = SessionLocal()
    logger.info("Starting Kafka consumer loop")
    while True:
        try:
            for message in _kafka_service.consume_content():
                logger.info(f"Processing Kafka message: {message}")
                try:
                    content = message.get("content")
                    url = message.get("url")
                    topic_ids = message.get("topic_ids", [1])  # Default to [1] if topic_ids is missing

                    # Validate topic_ids
                    for topic_id in topic_ids:
                        if not db.query(Topic).filter(Topic.id == topic_id).first():
                            raise HTTPException(status_code=400, detail=f"Topic ID {topic_id} does not exist")

                    # Store material
                    material = Material(content=content, url=url)
                    db.add(material)
                    db.flush()

                    # Index in embeddings
                    if content:
                        start = app_instance.embeddings.count()
                        sections = [{"text": content, "source_url": url}]
                        app_instance.embeddings.upsert(sections)
                        # Create LLM-generated topics
                        app_instance.infertopics(app_instance.embeddings, start)
                        # Save embeddings, if necessary
                        app_instance.persist(app_instance.embeddings)
                    elif url:
                        app_instance.addurl(url)

                    # Generate questions
                    context = content or url
                    questions = _question_generator.generate_questions(context, num_questions=3)

                    # Generate answers and store for each topic
                    for topic_id in topic_ids:
                        for question_text in questions:
                            answer_data = app_instance.get_answer(question_text)
                            question = Question(
                                topic_id=topic_id,
                                question_text=question_text,
                                answer_text=answer_data["answer"],
                                material_id=material.id
                            )
                            db.add(question)

                    db.commit()
                    logger.info(f"Processed content: {message['id']} for topics: {topic_ids}")
                except Exception as e:
                    db.rollback()
                    logger.error(f"Error processing Kafka message: {str(e)}")
        except Exception as e:
            logger.error(f"Kafka consumer loop error: {str(e)}")
            await asyncio.sleep(5)
        finally:
            db.close()

if __name__ == "__main__":
    try:
        init()
        import uvicorn
        from sqlalchemy.sql import func

        os.environ["TOKENIZERS_PARALLELISM"] = "false"

        # Start Kafka consumer in background
        loop = asyncio.get_event_loop()
        logger.info("Starting process_kafka_messages task")
        loop.create_task(process_kafka_messages())
        loop.run_until_complete(loop.create_task(asyncio.sleep(1)))  # Ensure task starts

        uvicorn.run(app, host="0.0.0.0", port=8000)
    except Exception as e:
        logger.error(f"Failed to start application: {e}")
        raise