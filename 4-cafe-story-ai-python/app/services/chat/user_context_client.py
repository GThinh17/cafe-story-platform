import logging
from typing import Any

from app.utils.java_client import JavaClientError, post_internal


logger = logging.getLogger("cafestory-ai.user-context-client")

BLOG_MODERATION_PATH = "/api/internal/user-context/blog-moderation"


def fetch_my_blog_moderation(user_jwt: str, limit: int = 5) -> list[dict[str, Any]]:
    """Route E (plan §3 Rule 6): forward JWT của end-user tới Java.

    Java verify JWT → chỉ trả moderation results của user đó.
    KHÔNG cache kết quả này (user-scoped).
    """
    try:
        return post_internal(BLOG_MODERATION_PATH, {"userJwt": user_jwt, "limit": limit}) or []
    except JavaClientError:
        logger.exception("user blog moderation fetch failed")
        return []
