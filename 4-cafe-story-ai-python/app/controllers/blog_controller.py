import logging

from fastapi import APIRouter

from app.schemas import BlogEvaluateRequest, BlogEvaluateResponse
from app.services.blog_evaluator import safe_evaluate_blog


logger = logging.getLogger("cafestory-ai.blog-controller")

router = APIRouter(prefix="/api/ai", tags=["blogs"])


@router.post("/blogs/evaluate", response_model=BlogEvaluateResponse)
def evaluate_blog(payload: BlogEvaluateRequest) -> BlogEvaluateResponse:
    result = safe_evaluate_blog(payload)
    logger.info(
        "blog evaluation completed blogId=%s imageCount=%s status=%s",
        payload.blogId,
        len(payload.imageUrls or []),
        result.status,
    )
    return result
