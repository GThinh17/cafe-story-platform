import logging
from dataclasses import dataclass
from typing import Any

from app.ai.enums import ModelProvider
from app.ai.factory import get_model
from app.config.rules import get_rules
from app.utils.json_extractor import extract_json


logger = logging.getLogger("cafestory-ai.text-moderator")


@dataclass(frozen=True)
class CaptionModerationResult:
    is_coffee_related: bool
    is_violation: bool
    score: int
    reason: str
    error: str | None = None


def _coerce_result(payload: dict[str, Any]) -> CaptionModerationResult:
    score = max(0, min(100, int(payload.get("score", 0))))
    return CaptionModerationResult(
        is_coffee_related=bool(payload.get("is_coffee_related", False)),
        is_violation=bool(payload.get("is_violation", False)),
        score=score,
        reason=str(payload.get("reason") or "Không có lý do từ hệ thống kiểm duyệt."),
    )


def moderate_caption(text: str) -> CaptionModerationResult:
    ai = get_rules()["ai_model"]
    rules = get_rules()["text_moderator"]
    model = get_model(ModelProvider(ai["provider"]), ai["model_name"])
    prompt = rules["prompt_template"].format(text=text or "")
    try:
        raw = model.generate_text(prompt)
        return _coerce_result(extract_json(raw))
    except Exception as exc:
        logger.exception("text moderation failed")
        return CaptionModerationResult(
            is_coffee_related=False,
            is_violation=False,
            score=0,
            reason="Unable to evaluate caption.",
            error=str(exc),
        )
