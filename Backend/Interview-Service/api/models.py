from pydantic import BaseModel
from typing import List, Dict, Any

class QuestionRequest(BaseModel):
    question: str

class ContextRequest(BaseModel):
    topic: str

class Source(BaseModel):
    id: str
    text: str
    url: str

class AnswerResponse(BaseModel):
    answer: str
    sources: List[Source] # or a custom Source model

class ContextResponse(BaseModel):
    context: List[Dict[str, Any]]

class CompareRequest(BaseModel):
    userInput: str
    answer: str