import logging

from PIL import Image

from app.config.rules import get_rules
from app.schemas import BlogEvaluateRequest, BlogEvaluateResponse, Status
from app.services.clip_cafe_detector import ClipImageResult, classify_image
from app.services.gemini_image_classifier import aggregate_tags, classify_image_tags
from app.services.gemini_text_moderator import CaptionModerationResult, moderate_caption
from app.utils.image_loader import load_image_from_url


logger = logging.getLogger("cafestory-ai.evaluator")


def _clamp_score(value: float) -> int:
    return int(round(max(0, min(100, value))))


def _score_clip_image(result: ClipImageResult) -> float:
    cafe_probability = sum(
        score
        for label, score in result.all_scores.items()
        if label.strip().lower() != get_rules()["clip"]["not_cafe_label"].lower()
        and not label.strip().lower().startswith(("not a cafe", "not a coffee shop"))
    )
    return cafe_probability * 100


def _summarize_images(
    total_images: int,
    clip_results: list[ClipImageResult],
    errors: list[str],
    image_score: int,
    status: Status,
) -> str:
    if total_images == 0:
        return "No images were provided."

    cafe_count = sum(1 for result in clip_results if result.cafe_related)
    labels = [result.label_predict for result in clip_results[:3]]
    reason = f"{cafe_count}/{total_images} images are cafe-related. imageScore={image_score}."
    if labels:
        reason += f" Main evidence: {'; '.join(labels)}."
    if errors:
        reason += f" Image errors: {'; '.join(errors)}."
    if status == "deny":
        reason += " Denied because cafe-related image probability is too low."
    elif status == "send Admin":
        reason += " Sent to admin because image evidence is weak or uncertain."
    return reason


def _decide_status(
    caption: CaptionModerationResult,
    image_score: int,
    total_images: int,
    clip_results: list[ClipImageResult],
    image_errors: list[str],
    tag_uncertain: bool,
) -> Status:
    rules = get_rules()["decision"]

    if caption.error:
        return "send Admin"
    if caption.score >= rules["caption_deny_score"]:
        return "deny"
    if caption.is_violation and caption.score >= rules["caption_deny_score"]:
        return "deny"
    if not caption.is_coffee_related:
        return "deny"

    if image_errors:
        return "send Admin"
    if total_images == 0:
        return "send Admin"
    if image_score <= rules["image_deny_score"]:
        return "deny"
    if rules["image_review_min_score"] < image_score < rules["image_review_max_score"]:
        return "send Admin"
    if image_score < rules["image_approve_score"]:
        return "send Admin"
    if tag_uncertain:
        return "send Admin"

    if not caption.is_violation and caption.is_coffee_related and image_score >= rules["image_approve_score"]:
        return "approve"

    return "send Admin"


def evaluate_blog(payload: BlogEvaluateRequest) -> BlogEvaluateResponse:
    caption = moderate_caption(payload.caption)
    image_urls = payload.imageUrls or []
    clip_results: list[ClipImageResult] = []
    loaded_images: list[tuple[str, Image.Image]] = []
    image_errors: list[str] = []

    for url in image_urls:
        try:
            image = load_image_from_url(url)
            loaded_images.append((url, image))
            clip_results.append(classify_image(url, image))
        except Exception as exc:
            image_errors.append(f"{url}: {exc}")

    if clip_results:
        # Strict moderation: one clearly unrelated image is enough to lower the post image score.
        image_score = _clamp_score(min(_score_clip_image(result) for result in clip_results))
    else:
        image_score = 0

    cafe_images = [
        image
        for url, image in loaded_images
        if any(result.url == url and result.cafe_related for result in clip_results)
    ]
    max_tag_images = get_rules()["gemini_image"]["max_images"]
    tag_results = [classify_image_tags(image) for image in cafe_images[:max_tag_images]]
    tags, tag_uncertain = aggregate_tags(tag_results)

    status = _decide_status(
        caption=caption,
        image_score=image_score,
        total_images=len(image_urls),
        clip_results=clip_results,
        image_errors=image_errors,
        tag_uncertain=tag_uncertain,
    )
    image_reason = _summarize_images(len(image_urls), clip_results, image_errors, image_score, status)

    for _, image in loaded_images:
        image.close()

    return BlogEvaluateResponse(
        blogId=payload.blogId,
        captionScore=caption.score,
        captionReason=caption.reason,
        imageScore=image_score,
        imageReason=image_reason,
        tags=tags,
        status=status,
    )


def safe_evaluate_blog(payload: BlogEvaluateRequest) -> BlogEvaluateResponse:
    try:
        return evaluate_blog(payload)
    except Exception:
        logger.exception("blog evaluation failed blogId=%s", payload.blogId)
        return BlogEvaluateResponse(
            blogId=payload.blogId,
            captionScore=0,
            captionReason="Unable to evaluate caption.",
            imageScore=0,
            imageReason="Unable to evaluate images.",
            tags=get_rules()["gemini_image"]["fallback_tags"][:3],
            status="send Admin",
        )
