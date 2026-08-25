from dataclasses import dataclass
import logging
from typing import Any

from PIL import Image

from app.config.rules import get_rules


logger = logging.getLogger("cafestory-ai.clip")
_model: Any | None = None
_processor: Any | None = None


@dataclass(frozen=True)
class ClipImageResult:
    url: str
    cafe_related: bool
    label_predict: str
    confidence_score: float
    all_scores: dict[str, float]


def get_model_and_processor() -> tuple[Any, Any]:
    global _model, _processor
    from transformers import CLIPModel, CLIPProcessor

    rules = get_rules()["clip"]
    if _model is None or _processor is None:
        logger.info("preloading CLIP model name=%s", rules["model_name"])
        _model = CLIPModel.from_pretrained(rules["model_name"])
        _processor = CLIPProcessor.from_pretrained(rules["model_name"])
        _model.eval()
        logger.info("CLIP model ready")
    return _model, _processor


def preload_clip_model() -> None:
    get_model_and_processor()


def is_cafe_related(label: str) -> bool:
    not_cafe_label = get_rules()["clip"]["not_cafe_label"].lower()
    normalized_label = label.strip().lower()
    return normalized_label != not_cafe_label and not normalized_label.startswith(
        ("not a cafe", "not a coffee shop")
    )


def classify_image(url: str, image: Image.Image) -> ClipImageResult:
    import torch

    rules = get_rules()["clip"]
    labels = rules["labels"]
    model, processor = get_model_and_processor()

    inputs = processor(text=labels, images=image, return_tensors="pt", padding=True)
    with torch.no_grad():
        outputs = model(**inputs)
        probs = outputs.logits_per_image.softmax(dim=1).cpu().numpy()[0]

    scores = {label: float(score) for label, score in zip(labels, probs)}
    best_match = max(scores, key=scores.get)
    confidence = round(scores[best_match] * 100, 2)

    return ClipImageResult(
        url=url,
        cafe_related=is_cafe_related(best_match),
        label_predict=best_match,
        confidence_score=confidence,
        all_scores=scores,
    )
