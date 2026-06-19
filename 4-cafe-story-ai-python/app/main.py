import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.config.rules import get_rules
from app.schemas import BlogEvaluateRequest, BlogEvaluateResponse
from app.services.blog_evaluator import safe_evaluate_blog


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("cafestory-ai")


def preload_ai_services() -> None:
    get_rules()
    logger.info("AI backend startup complete")


@asynccontextmanager
async def lifespan(app: FastAPI):
    preload_ai_services()
    yield


app = FastAPI(
    title="CafeStory AI Moderation Backend",
    version="2.0.0",
    lifespan=lifespan,
    default_response_class=JSONResponse,
)


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.exception("unhandled request failed path=%s", request.url.path)
    return JSONResponse(
        status_code=500,
        content={
            "status": "error",
            "message": "Internal AI backend error.",
        },
    )


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/ai/blogs/evaluate", response_model=BlogEvaluateResponse)
def evaluate_blog(payload: BlogEvaluateRequest) -> BlogEvaluateResponse:
    result = safe_evaluate_blog(payload)
    logger.info(
        "blog evaluation completed blogId=%s imageCount=%s status=%s",
        payload.blogId,
        len(payload.imageUrls or []),
        result.status,
    )
    return result
