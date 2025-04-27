from generators.questions import QuestionGenerator
from fastapi import FastAPI, HTTPException
from api.models import QuestionRequest, ContextRequest, AnswerResponse, ContextResponse, CompareRequest
from comparator.comparator import AnswerComparator
from generators.rag.core import get_app
import logging
import os

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI app
app = FastAPI(title="RAG API with txtai")

@app.post("/answer", response_model=AnswerResponse)
async def answer_question(request: QuestionRequest):
    try:
        app_instance = get_app()
        answer = app_instance.get_answer(request.question)
        return AnswerResponse(answer=answer)
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
        return result
    except Exception as e:
        logger.error(f"Error retrieving context: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


_question_generator = None
_comparator = None


def init():
    global _question_generator
    model_path = "./ruT5-it-question-generator-sberquad"
    if os.path.isdir(model_path):
        print(f"Directory {model_path} exists")
    else:
        print(f"Directory {model_path} does not exist")

    logger.info("QuestionGenerator initialising")
    _question_generator = QuestionGenerator(model_path)
    logger.info("QuestionGenerator initialised")

    logger.info("RAG initialising")
    get_app()
    logger.info("RAG initialised")

    global _comparator
    _comparator = AnswerComparator()




if __name__ == "__main__":
    init()
    import uvicorn
    os.environ["TOKENIZERS_PARALLELISM"] = "false"
    uvicorn.run(app, host="0.0.0.0", port=8000)