import logging

from fastapi import APIRouter, Header
from fastapi.responses import StreamingResponse

from app.schemas import ChatAskRequest, ChatAskResponse
from app.services.chat.generator import answer_query, answer_query_stream


logger = logging.getLogger("cafestory-ai.chat-controller")

router = APIRouter(prefix="/api/ai", tags=["chat"])

BEARER_PREFIX = "Bearer "


def _extract_user_jwt(authorization: str | None) -> str | None:
    if not authorization or not authorization.startswith(BEARER_PREFIX):
        return None
    token = authorization[len(BEARER_PREFIX):].strip()
    return token or None


@router.post("/chat/ask", response_model=ChatAskResponse)
def ask(
    payload: ChatAskRequest,
    authorization: str | None = Header(default=None),
) -> ChatAskResponse:
    history = [message.model_dump() for message in payload.history]
    user_jwt = _extract_user_jwt(authorization)
    result = answer_query(payload.query, payload.platform, history, user_jwt)
    logger.info(
        "chat answered queryLen=%s route=%s sources=%s cached=%s authed=%s",
        len(payload.query),
        result.route,
        len(result.sources),
        result.cached,
        bool(user_jwt),
    )
    return ChatAskResponse(answer=result.answer, sources=result.sources, cached=result.cached)


@router.post("/chat/ask/stream")
def ask_stream(
    payload: ChatAskRequest,
    authorization: str | None = Header(default=None),
) -> StreamingResponse:
    history = [message.model_dump() for message in payload.history]
    user_jwt = _extract_user_jwt(authorization)
    return StreamingResponse(
        answer_query_stream(payload.query, payload.platform, history, user_jwt),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )
