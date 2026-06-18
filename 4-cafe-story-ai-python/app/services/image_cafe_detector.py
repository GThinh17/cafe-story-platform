import logging
from dataclasses import dataclass

from PIL import Image

from app.ai.enums import ModelProvider
from app.ai.factory import get_model
from app.config.rules import get_rules
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

    ai = get_rules()["ai_model"]
    rules = get_rules()["image_cafe_detector"]
    model = get_model(ModelProvider(ai["provider"]), ai["model_name"])
    try:
        raw = model.generate_with_images(rules["prompt"], images)
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
