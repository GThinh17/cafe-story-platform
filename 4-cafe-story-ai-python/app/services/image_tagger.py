import logging
import re
from dataclasses import dataclass

from PIL import Image

from app.ai.enums import ModelProvider
from app.ai.factory import get_model
from app.config.rules import get_rules


logger = logging.getLogger("cafestory-ai.image-tagger")


@dataclass(frozen=True)
class ImageTagResult:
    tags: list[str]
    raw_text: str
    error: str | None = None


def _build_prompt() -> str:
    rules = get_rules()["image_classifier"]
    tag_lines = "\n".join(f"- {tag}: {desc}" for tag, desc in rules["tags"].items())
    strict_rules = "\n".join(f"{i + 1}. {rule}" for i, rule in enumerate(rules["prompt_rules"]))
    return f"""You are a strict image classifier.

Your task is to classify cafe image(s) into EXACTLY 3 tags.

STRICT RULES:
{strict_rules}

TAG DEFINITIONS:
{tag_lines}

INSTRUCTIONS:
- Select EXACTLY 3 tags.
- Each tag MUST be supported by clear visual evidence.
- If a tag is not strongly visible, DO NOT select it.
- Prefer concrete objects over abstract vibes.
- Avoid overusing "photo cafe".

OUTPUT FORMAT:
Tags:
- [tag1]
- [tag2]
- [tag3]"""


def _parse_tags(text: str) -> list[str]:
    allowed_tags = list(get_rules()["image_classifier"]["tags"].keys())
    lowered = text.lower()
    found: list[str] = []
    for tag in allowed_tags:
        if re.search(rf"\b{re.escape(tag.lower())}\b", lowered):
            found.append(tag)
    return found[:3]


def classify_image_tags(images: list[Image.Image]) -> ImageTagResult:
    if not images:
        return ImageTagResult(tags=[], raw_text="", error="No images provided.")
    ai = get_rules()["ai_model"]
    rules = get_rules()["image_classifier"]
    model = get_model(ModelProvider(ai["provider"]), ai["model_name"])
    try:
        raw = model.generate_with_images(_build_prompt(), images)
        return ImageTagResult(tags=_parse_tags(raw), raw_text=raw)
    except Exception as exc:
        logger.exception("image tagging failed")
        return ImageTagResult(tags=[], raw_text="", error=str(exc))


def get_fallback_tags() -> list[str]:
    return get_rules()["image_classifier"]["fallback_tags"][:3]
