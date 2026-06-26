import logging
from dataclasses import dataclass

from PIL import Image

from app.prompts.model_selector import get_response, render_prompt
from app.utils.json_extractor import extract_json


logger = logging.getLogger("cafestory-ai.image-cafe-detector")


@dataclass(frozen=True)
class ImageCafeResult:
    is_cafe: bool
    confidence: int
    reason: str
    error: str | None = None


def detect_cafe_images(images: list[Image.Image]) -> ImageCafeResult:
    if not images:
        return ImageCafeResult(is_cafe=False, confidence=0, reason="Không có ảnh được cung cấp.")

    prompt = render_prompt("image_cafe_detection.j2")
    try:
        raw = get_response(prompt, images)
        logger.info("raw cafe detection output: %s", raw)
        payload = extract_json(raw)
        confidence = max(0, min(100, int(payload.get("confidence", 0))))
        return ImageCafeResult(
            is_cafe=bool(payload.get("is_cafe", False)),
            confidence=confidence,
            reason=str(payload.get("reason") or "Không có lý do."),
        )
    except Exception as exc:
        logger.exception("image cafe detection failed")
        return ImageCafeResult(
            is_cafe=False,
            confidence=0,
            reason="Unable to classify images.",
            error=str(exc),
        )
