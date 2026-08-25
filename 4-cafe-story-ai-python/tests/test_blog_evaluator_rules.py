import unittest
from unittest.mock import patch

from PIL import Image

from app.schemas import BlogEvaluateRequest
from app.services.blog_evaluator import evaluate_blog
from app.services.image_cafe_detector import ImageCafeResult
from app.services.image_tagger import ImageTagResult
from app.services.text_moderator import CaptionModerationResult


_PASS_TAGS = ["study cafe", "brunch cafe", "garden cafe"]
_OK_CAPTION = CaptionModerationResult(is_coffee_related=True, is_violation=False, score=0, reason="ok")


class BlogEvaluatorDecisionRulesTest(unittest.TestCase):
    def test_toxic_caption_is_denied(self):
        result = self.evaluate(caption_score=70, caption_related=True, cafe_confidence=90, is_cafe=True)

        self.assertEqual(result.status, "deny")

    def test_unrelated_caption_is_denied(self):
        result = self.evaluate(caption_score=0, caption_related=False, cafe_confidence=90, is_cafe=True)

        self.assertEqual(result.status, "deny")

    def test_low_cafe_confidence_is_denied(self):
        # AI says not a cafe → deny
        result = self.evaluate(caption_score=0, caption_related=True, cafe_confidence=30, is_cafe=False)

        self.assertEqual(result.status, "deny")
        self.assertEqual(result.imageScore, 30)

    def test_mid_cafe_confidence_goes_to_admin(self):
        # AI says is_cafe=True but confidence below threshold → send Admin
        result = self.evaluate(caption_score=0, caption_related=True, cafe_confidence=40, is_cafe=True)

        self.assertEqual(result.status, "send Admin")

    def test_caption_and_image_pass_is_approved(self):
        result = self.evaluate(caption_score=0, caption_related=True, cafe_confidence=75, is_cafe=True)

        self.assertEqual(result.status, "approve")

    def test_image_error_goes_to_admin_json_contract(self):
        payload = BlogEvaluateRequest(
            blogId="test-image-error",
            caption="Quán cà phê yên tĩnh",
            imageUrls=["https://example.com/broken.jpg"],
        )

        with patch(
            "app.services.blog_evaluator.moderate_caption",
            return_value=_OK_CAPTION,
        ), patch(
            "app.services.blog_evaluator.load_image_from_url",
            side_effect=RuntimeError("download failed"),
        ):
            result = evaluate_blog(payload)

        self.assertEqual(result.status, "send Admin")
        self.assertEqual(len(result.tags), 3)

    def evaluate(self, caption_score: int, caption_related: bool, cafe_confidence: int, is_cafe: bool):
        payload = BlogEvaluateRequest(
            blogId="test-blog",
            caption="Quán cà phê yên tĩnh",
            imageUrls=["https://example.com/cafe.jpg"],
        )
        image = Image.new("RGB", (8, 8), color="white")
        cafe_result = ImageCafeResult(
            is_cafe=is_cafe,
            confidence=cafe_confidence,
            reason="test",
        )
        caption_result = CaptionModerationResult(
            is_coffee_related=caption_related,
            is_violation=False,
            score=caption_score,
            reason="ok",
        )
        tag_result = ImageTagResult(tags=_PASS_TAGS, raw_text="")

        with patch(
            "app.services.blog_evaluator.moderate_caption",
            return_value=caption_result,
        ), patch(
            "app.services.blog_evaluator.load_image_from_url",
            return_value=image,
        ), patch(
            "app.services.blog_evaluator.detect_cafe_images",
            return_value=cafe_result,
        ), patch(
            "app.services.blog_evaluator.classify_image_tags",
            return_value=tag_result,
        ):
            return evaluate_blog(payload)


if __name__ == "__main__":
    unittest.main()
