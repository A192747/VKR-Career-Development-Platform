from pydantic import BaseModel
from typing import List, Dict, Any

class QuestionRequest(BaseModel):
    question: str

class ContextRequest(BaseModel):
    topic: str

class AnswerResponse(BaseModel):
    answer: str

class ContextResponse(BaseModel):
    context: List[Dict[str, Any]]

class CompareRequest(BaseModel):
    userInput: str
    answer: str