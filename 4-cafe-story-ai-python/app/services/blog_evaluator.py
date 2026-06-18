import logging
from concurrent.futures import ThreadPoolExecutor

from PIL import Image

from app.config.rules import get_rules
from app.schemas import BlogEvaluateRequest, BlogEvaluateResponse, Status
from app.services.image_cafe_detector import ImageCafeResult, detect_cafe_images
from app.services.image_tagger import ImageTagResult, classify_image_tags, get_fallback_tags
from app.services.text_moderator import CaptionModerationResult, moderate_caption
from app.utils.image_loader import load_image_from_url, resize_for_ai


logger = logging.getLogger("cafestory-ai.evaluator")


def _decide_status(
    caption: CaptionModerationResult,
    cafe_detection: ImageCafeResult,
    total_images: int,
    image_errors: list[str],
) -> Status:
    rules = get_rules()["decision"]

    if caption.error:
        return "send Admin"
    if caption.score >= rules["caption_deny_score"]:
        return "deny"
    if caption.is_violation:
        return "deny"
    if not caption.is_coffee_related:
        return "deny"

    if total_images == 0:
        return "send Admin"
    if image_errors:
        return "send Admin"
    if cafe_detection.error:
        return "send Admin"
    if not cafe_detection.is_cafe:
        return "deny"
    if cafe_detection.confidence < rules["image_approve_score"]:
        return "send Admin"

    return "approve"


def evaluate_blog(payload: BlogEvaluateRequest) -> BlogEvaluateResponse:
    image_urls = payload.imageUrls or []
    loaded_images: list[tuple[str, Image.Image]] = []
    image_errors: list[str] = []

    for url in image_urls:
        try:
            image = resize_for_ai(load_image_from_url(url))
            loaded_images.append((url, image))
        except Exception as exc:
            image_errors.append(f"{url}: {exc}")

    images = [img for _, img in loaded_images]

    with ThreadPoolExecutor(max_workers=2) as executor:
        text_future = executor.submit(moderate_caption, payload.caption)
        image_future = executor.submit(detect_cafe_images, images)

    caption: CaptionModerationResult = text_future.result()
    cafe_detection: ImageCafeResult = image_future.result()

    status = _decide_status(caption, cafe_detection, len(image_urls), image_errors)

    tags: list[str]
    if status == "approve":
        max_images = get_rules()["image_classifier"]["max_images"]
        tag_result: ImageTagResult = classify_image_tags(images[:max_images])
        tags = tag_result.tags if not tag_result.error and len(tag_result.tags) == 3 else get_fallback_tags()
    else:
        tags = get_fallback_tags()

    for _, image in loaded_images:
        image.close()

    return BlogEvaluateResponse(
        blogId=payload.blogId,
        captionScore=caption.score,
        captionReason=caption.reason,
        imageScore=cafe_detection.confidence,
        imageReason=cafe_detection.reason,
        tags=tags,
        status=status,
    )


def safe_evaluate_blog(payload: BlogEvaluateRequest) -> BlogEvaluateResponse:
    try:
        return evaluate_blog(payload)
    except Exception:
        logger.exception("blog evaluation failed blogId=%s", payload.blogId)
        try:
            fallback_tags = get_fallback_tags()
        except Exception:
            fallback_tags = ["study cafe", "brunch cafe", "photo cafe"]
        return BlogEvaluateResponse(
            blogId=payload.blogId,
            captionScore=0,
            captionReason="Unable to evaluate caption.",
            imageScore=0,
            imageReason="Unable to evaluate images.",
            tags=fallback_tags,
            status="send Admin",
        )
