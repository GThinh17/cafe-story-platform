import os
import re
import logging
from collections import Counter
from dataclasses import dataclass

from dotenv import load_dotenv
from google import genai
from PIL import Image

from app.config.rules import get_rules


load_dotenv()

logger = logging.getLogger("cafestory-ai.gemini-image")
_client: genai.Client | None = None


@dataclass(frozen=True)
class ImageTagResult:
    tags: list[str]
    raw_text: str
    error: str | None = None


def get_client() -> genai.Client:
    global _client
    api_key = os.getenv("GOOGLE_API_KEY")
    if not api_key:
        raise RuntimeError("GOOGLE_API_KEY is required for Gemini image classification.")
    if _client is None:
        _client = genai.Client(api_key=api_key)
    return _client


def preload_image_classifier() -> None:
    rules = get_rules()["gemini_image"]
    get_client()
    logger.info("Gemini image client ready model=%s", rules["model_name"])


def _build_prompt() -> str:
    rules = get_rules()["gemini_image"]
    tag_lines = "\n".join(
        f"- {tag}: {description}" for tag, description in rules["tags"].items()
    )
    strict_rules = "\n".join(f"{index + 1}. {rule}" for index, rule in enumerate(rules["prompt_rules"]))
    return f"""
You are a strict image classifier.

Your task is to classify a cafe image into EXACTLY 3 tags.

STRICT RULES:
{strict_rules}

TAG DEFINITIONS:
{tag_lines}

INSTRUCTIONS:
- Select EXACTLY 3 tags.
- Each tag MUST be supported by clear visual evidence.
- If a tag is not strongly visible, DO NOT select it.
- Prefer concrete objects (cake display, instruments, animals) over abstract vibes.
- Avoid overusing "photo cafe".

OUTPUT FORMAT:
Tags:
- [tag1]
- [tag2]
- [tag3]
""".strip()


def _parse_tags(text: str) -> list[str]:
    allowed_tags = list(get_rules()["gemini_image"]["tags"].keys())
    lowered = text.lower()
    found: list[str] = []
    for tag in allowed_tags:
        if re.search(rf"\b{re.escape(tag.lower())}\b", lowered):
            found.append(tag)
    return found[:3]


def classify_image_tags(image: Image.Image) -> ImageTagResult:
    rules = get_rules()["gemini_image"]
    try:
        response = get_client().models.generate_content(
            model=rules["model_name"],
            contents=[_build_prompt(), image],
        )
        raw_text = response.text or ""
        return ImageTagResult(tags=_parse_tags(raw_text), raw_text=raw_text)
    except Exception as exc:
        return ImageTagResult(tags=[], raw_text="", error=str(exc))


def aggregate_tags(results: list[ImageTagResult]) -> tuple[list[str], bool]:
    rules = get_rules()["gemini_image"]
    counts: Counter[str] = Counter()
    uncertain = False

    for result in results:
        if result.error or len(result.tags) < 3:
            uncertain = True
        counts.update(result.tags)

    tags = [tag for tag, _ in counts.most_common(3)]
    if len(tags) < 3:
        uncertain = True
        for fallback in rules["fallback_tags"]:
            if fallback not in tags:
                tags.append(fallback)
            if len(tags) == 3:
                break

    return tags[:3], uncertain
