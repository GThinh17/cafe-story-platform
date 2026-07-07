import logging
from dataclasses import dataclass
from functools import lru_cache
from typing import Any

from app.config.rules import get_rag_config
from app.infra.openai_model import OpenAIModel
from app.prompts.model_selector import render_prompt
from app.services.rag.chunker import estimate_tokens
from app.services.rag.vector_store import get_vector_store
from app.utils.json_extractor import extract_json


logger = logging.getLogger("cafestory-ai.rag-router")

VALID_ROUTES = {"A", "B", "C", "D", "E"}
DEFAULT_ROUTE = "B"


@dataclass
class RouteDecision:
    route: str
    rewritten_query: str
    province: str | None = None


@lru_cache(maxsize=1)
def _get_router_model() -> OpenAIModel:
    return OpenAIModel(get_rag_config()["models"]["router"])


def route_query(query: str, history: list[dict[str, Any]] | None = None) -> RouteDecision:
    """Gộp rewrite + classify + extract filter trong 1 call (plan §15.4).

    Skip LLM khi không có history VÀ câu ngắn gọn rõ ràng? Không — classify vẫn cần LLM.
    Fallback về route B (hybrid search) nếu LLM lỗi/JSON hỏng.
    """
    prompt = render_prompt("router.j2", query=query, history=history or [])
    try:
        raw = _get_router_model().generate_text(prompt)
        parsed = extract_json(raw)
    except Exception:
        logger.exception("router LLM failed, falling back to route %s", DEFAULT_ROUTE)
        return RouteDecision(route=DEFAULT_ROUTE, rewritten_query=query)

    get_vector_store().log_usage("router", estimate_tokens(prompt), estimate_tokens(str(parsed)))

    route = str(parsed.get("route", "")).strip().upper()
    if route not in VALID_ROUTES:
        route = DEFAULT_ROUTE

    rewritten = str(parsed.get("rewritten_query") or "").strip() or query
    province = parsed.get("province")
    if isinstance(province, str):
        province = province.strip() or None
        if province and province.lower() in ("null", "none"):
            province = None
    else:
        province = None

    decision = RouteDecision(route=route, rewritten_query=rewritten, province=province)
    logger.info("routed query -> %s (province=%s)", decision.route, decision.province)
    return decision
