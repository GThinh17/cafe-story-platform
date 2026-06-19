import logging
import re
from dataclasses import dataclass

from PIL import Image

from app.ai.enums import ModelProvider
from app.ai.factory import get_model
from app.config.rules import get_rules


logger = logging.getLogger("cafestory-ai.image-tagger")

_MIN_CONFIDENCE = 60


@dataclass(frozen=True)
class ImageTagResult:
    tags: list[str]
    raw_text: str
    error: str | None = None


def _build_prompt() -> str:
    rules = get_rules()["image_classifier"]
    tag_lines = "\n".join(f"- {tag}: {desc}" for tag, desc in rules["tags"].items())
    strict_rules = "\n".join(f"{i + 1}. {rule}" for i, rule in enumerate(rules["prompt_rules"]))
    return f"""Bạn là hệ thống phân loại ảnh quán cafe.

Phân tích ảnh được cung cấp và chọn 1-3 tag phù hợp từ danh sách dưới đây.
Chỉ chọn tag khi confidence >= {_MIN_CONFIDENCE}. Thà chọn ít tag đúng hơn nhiều tag sai.

QUY TẮC NGHIÊM NGẶT:
{strict_rules}

DANH SÁCH TAG:
{tag_lines}

OUTPUT (chỉ trả các tag có confidence >= {_MIN_CONFIDENCE}, format chính xác như sau):
Tags:
- [tag1] (confidence: XX)
- [tag2] (confidence: XX)
- [tag3] (confidence: XX)"""


def _parse_tags(text: str) -> list[str]:
    allowed_tags = list(get_rules()["image_classifier"]["tags"].keys())
    tag_lookup = {t.lower(): t for t in allowed_tags}
    results: list[str] = []

    # Primary: parse lines with confidence scores "- tag name (confidence: 85)"
    conf_pattern = re.compile(r"-\s*(.+?)\s*\(confidence:\s*(\d+)\)", re.IGNORECASE)
    for match in conf_pattern.finditer(text):
        tag_candidate = match.group(1).strip().lower()
        confidence = int(match.group(2))
        if tag_candidate in tag_lookup and confidence >= _MIN_CONFIDENCE:
            canonical = tag_lookup[tag_candidate]
            if canonical not in results:
                results.append(canonical)

    # Fallback: no confidence format detected, do substring match
    if not results:
        lowered = text.lower()
        for tag in allowed_tags:
            if tag.lower() in lowered:
                results.append(tag)

    return results[:3]


def classify_image_tags(images: list[Image.Image]) -> ImageTagResult:
    if not images:
        return ImageTagResult(tags=[], raw_text="", error="No images provided.")
    ai = get_rules()["ai_model"]
    model = get_model(ModelProvider(ai["provider"]), ai["model_name"])
    try:
        raw = model.generate_with_images(_build_prompt(), images)
        return ImageTagResult(tags=_parse_tags(raw), raw_text=raw)
    except Exception as exc:
        logger.exception("image tagging failed")
        return ImageTagResult(tags=[], raw_text="", error=str(exc))
