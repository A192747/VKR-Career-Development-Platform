from pydantic import BaseModel, validator
from typing import List, Optional
from fastapi import Query
from sqlalchemy import Column, Integer, String, Text, ForeignKey
from sqlalchemy.ext.declarative import declarative_base

# SQLAlchemy Base
Base = declarative_base()

# SQLAlchemy Models
class Topic(Base):
    __tablename__ = "topics"
    id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)

class Material(Base):
    __tablename__ = "materials"
    id = Column(Integer, primary_key=True)
    content = Column(Text, nullable=True)
    url = Column(String, nullable=True)

class Question(Base):
    __tablename__ = "questions"
    id = Column(Integer, primary_key=True)
    topic_id = Column(Integer, ForeignKey("topics.id"), nullable=False)
    question_text = Column(Text, nullable=False)
    answer_text = Column(Text, nullable=False)
    material_id = Column(Integer, ForeignKey("materials.id"), nullable=False)

# Pydantic Models
class TopicCreateRequest(BaseModel):
    name: str

class QuestionRequest(BaseModel):
    question: str

class ContextRequest(BaseModel):
    topic: str

class AnswerResponse(BaseModel):
    answer: str
    context: str

class ContextResponse(BaseModel):
    context: str

class CompareRequest(BaseModel):
    userInput: str
    answer: str

class ContentRequest(BaseModel):
    content: Optional[str] = None
    url: Optional[str] = None
    topic_ids: Optional[List[int]] = Query(default=[1], description="List of topic IDs for the content")

    @validator("url", pre=True, always=True)
    def check_content_or_url(cls, url, values):
        content = values.get("content")
        if content and url:
            raise ValueError("Provide either content or url, not both")
        if not content and not url:
            raise ValueError("Either content or url must be provided")
        return url

class TestRequest(BaseModel):
    topic_id: int
    num_questions: int

class TestResponse(BaseModel):
    questions: List[dict]

class AnswerCheckRequest(BaseModel):
    question_id: int
    user_answer: str

class AnswerCheckResponse(BaseModel):
    similarity: float
    material: Optional[dict] = None

class TopicResponse(BaseModel):
    id: int
    name: str

class TopicsListResponse(BaseModel):
    topics: List[TopicResponse]
    total_items: int
    current_page: int
    page_size: int
    total_pages: int

class QuestionsByTopicsRequest(BaseModel):
    topics: List[int]
    num_questions_per_topic: int = 3  # По умолчанию 3 вопроса