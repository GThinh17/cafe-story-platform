import logging
import re
from dataclasses import dataclass

from PIL import Image

from app.config.rules import get_rules
from app.prompts.model_selector import get_response, render_prompt


logger = logging.getLogger("cafestory-ai.image-tagger")

_MIN_CONFIDENCE = 60


@dataclass(frozen=True)
class ImageTagResult:
    tags: list[str]
    raw_text: str
    error: str | None = None


def _parse_tags(text: str) -> list[str]:
    allowed_tags = list(get_rules()["image_classifier"]["tags"].keys())
    tag_lookup = {t.lower(): t for t in allowed_tags}
    results: list[str] = []

    conf_pattern = re.compile(r"-\s*(.+?)\s*\(confidence:\s*(\d+)\)", re.IGNORECASE)
    for match in conf_pattern.finditer(text):
        tag_candidate = match.group(1).strip().lower()
        confidence = int(match.group(2))
        if tag_candidate in tag_lookup and confidence >= _MIN_CONFIDENCE:
            canonical = tag_lookup[tag_candidate]
            if canonical not in results:
                results.append(canonical)

    if not results:
        lowered = text.lower()
        for tag in allowed_tags:
            if tag.lower() in lowered:
                results.append(tag)

    return results[:3]


def classify_image_tags(images: list[Image.Image]) -> ImageTagResult:
    if not images:
        return ImageTagResult(tags=[], raw_text="", error="No images provided.")

    rules = get_rules()["image_classifier"]
    prompt = render_prompt(
        "image_classification.j2",
        tags=rules["tags"],
        prompt_rules=rules["prompt_rules"],
        min_confidence=_MIN_CONFIDENCE,
    )
    try:
        raw = get_response(prompt, images)
        logger.info("raw tag output: %s", raw)
        return ImageTagResult(tags=_parse_tags(raw), raw_text=raw)
    except Exception as exc:
        logger.exception("image tagging failed")
        return ImageTagResult(tags=[], raw_text="", error=str(exc))
