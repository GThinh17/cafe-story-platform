import json
import logging
import os
from dataclasses import dataclass
from typing import Any

from dotenv import load_dotenv
from google import genai
from google.genai import types

from app.config.rules import get_rules


load_dotenv()

logger = logging.getLogger("cafestory-ai.gemini-text")
_client: genai.Client | None = None


@dataclass(frozen=True)
class CaptionModerationResult:
    is_coffee_related: bool
    is_violation: bool
    score: int
    reason: str
    error: str | None = None


def get_client() -> genai.Client:
    global _client
    api_key = os.getenv("GOOGLE_API_KEY")
    if not api_key:
        raise RuntimeError("GOOGLE_API_KEY is required for Gemini text moderation.")
    if _client is None:
        _client = genai.Client(api_key=api_key)
    return _client


def preload_text_moderator() -> None:
    rules = get_rules()["gemini_text"]
    get_client()
    logger.info("Gemini text client ready model=%s", rules["model_name"])


def _coerce_result(payload: dict[str, Any]) -> CaptionModerationResult:
    score = int(payload.get("score", 0))
    score = max(0, min(100, score))
    return CaptionModerationResult(
        is_coffee_related=bool(payload.get("is_coffee_related", False)),
        is_violation=bool(payload.get("is_violation", False)),
        score=score,
        reason=str(payload.get("reason") or "Không có lý do từ hệ thống kiểm duyệt."),
    )


def moderate_caption(text: str) -> CaptionModerationResult:
    rules = get_rules()["gemini_text"]
    prompt = rules["prompt_template"].format(text=text or "")

    try:
        response = get_client().models.generate_content(
            model=rules["model_name"],
            contents=prompt,
            config=types.GenerateContentConfig(response_mime_type="application/json"),
        )
        return _coerce_result(json.loads(response.text or "{}"))
    except Exception as exc:
        return CaptionModerationResult(
            is_coffee_related=False,
            is_violation=False,
            score=0,
            reason="Unable to evaluate caption.",
            error=str(exc),
        )
