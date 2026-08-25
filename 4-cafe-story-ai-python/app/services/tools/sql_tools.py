import logging
from typing import Any

from app.utils.java_client import JavaClientError, get_internal


logger = logging.getLogger("cafestory-ai.sql-tools")

TRENDING_CAFES_PATH = "/api/internal/rag/trending-cafes"
TOP_REVIEWERS_PATH = "/api/internal/rag/top-reviewers"


def fetch_trending_cafes(limit: int = 5, province: str | None = None) -> list[dict[str, Any]]:
    """Route A: dữ liệu realtime từ Java, KHÔNG qua vector DB (plan §1 nhóm A)."""
    params: dict[str, Any] = {"limit": limit}
    if province:
        params["province"] = province
    try:
        return get_internal(TRENDING_CAFES_PATH, params) or []
    except JavaClientError:
        logger.exception("trending cafes tool failed")
        return []


def fetch_top_reviewers(limit: int = 5) -> list[dict[str, Any]]:
    try:
        return get_internal(TOP_REVIEWERS_PATH, {"limit": limit}) or []
    except JavaClientError:
        logger.exception("top reviewers tool failed")
        return []
