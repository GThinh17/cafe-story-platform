import logging
from dataclasses import dataclass
from typing import Any

from app.prompts.model_selector import get_response, render_prompt
from app.utils.json_extractor import extract_json


logger = logging.getLogger("cafestory-ai.text-moderator")


_REASON_MESSAGES: dict[str, str] = {
    "COFFEE_RELATED": "Caption liên quan đến trải nghiệm quán cà phê.",
    "NOT_COFFEE": "Caption không liên quan đến quán cà phê.",
    "PROFANITY": "Caption chứa ngôn từ thô tục hoặc xúc phạm.",
    "IRRELEVANT": "Caption không phù hợp với nội dung nền tảng.",
    "SPAM": "Caption có dấu hiệu spam hoặc quảng cáo ngoài chủ đề.",
}
_DEFAULT_REASON = "Không có lý do từ hệ thống kiểm duyệt."


@dataclass(frozen=True)
class CaptionModerationResult:
    is_coffee_related: bool
    is_violation: bool
    score: int
    reason: str
    error: str | None = None


def _coerce_result(payload: dict[str, Any]) -> CaptionModerationResult:
    score = max(0, min(100, int(payload.get("score", 0))))
    code = str(payload.get("reason_code") or "").strip().upper()
    reason = _REASON_MESSAGES.get(code) or str(payload.get("reason") or _DEFAULT_REASON)
    return CaptionModerationResult(
        is_coffee_related=bool(payload.get("is_coffee_related", False)),
        is_violation=bool(payload.get("is_violation", False)),
        score=score,
        reason=reason,
    )


def moderate_caption(text: str) -> CaptionModerationResult:
    prompt = render_prompt("text_moderation.j2", text=text or "")
    try:
        raw = get_response(prompt)
        logger.info("raw model output: %s", raw)
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
