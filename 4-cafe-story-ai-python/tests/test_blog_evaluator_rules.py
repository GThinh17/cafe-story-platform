import unittest
from unittest.mock import patch

from PIL import Image

from app.schemas import BlogEvaluateRequest
from app.services.blog_evaluator import evaluate_blog
from app.services.clip_cafe_detector import ClipImageResult
from app.services.gemini_text_moderator import CaptionModerationResult


CAFE_LABEL = "coffee shop space"
NOT_CAFE_LABEL = "not a coffee shop (bar, office workspace with computers, restaurant, classroom, gym), unrelated image"


class BlogEvaluatorDecisionRulesTest(unittest.TestCase):
    def test_toxic_caption_is_denied(self):
        result = self.evaluate(caption_score=70, caption_related=True, image_cafe_probability=90)

        self.assertEqual(result.status, "deny")

    def test_unrelated_caption_is_denied(self):
        result = self.evaluate(caption_score=0, caption_related=False, image_cafe_probability=90)

        self.assertEqual(result.status, "deny")

    def test_low_image_score_is_denied(self):
        result = self.evaluate(caption_score=0, caption_related=True, image_cafe_probability=30)

        self.assertEqual(result.status, "deny")
        self.assertEqual(result.imageScore, 30)

    def test_mid_image_score_goes_to_admin(self):
        result = self.evaluate(caption_score=0, caption_related=True, image_cafe_probability=40)

        self.assertEqual(result.status, "send Admin")

    def test_caption_and_image_pass_is_approved(self):
        result = self.evaluate(caption_score=0, caption_related=True, image_cafe_probability=75)

        self.assertEqual(result.status, "approve")

    def test_image_error_goes_to_admin_json_contract(self):
        payload = BlogEvaluateRequest(
            blogId="test-image-error",
            caption="Quán cà phê yên tĩnh",
            imageUrls=["https://example.com/broken.jpg"],
        )

        with patch(
            "app.services.blog_evaluator.moderate_caption",
            return_value=CaptionModerationResult(True, False, 0, "ok"),
        ), patch(
            "app.services.blog_evaluator.load_image_from_url",
            side_effect=RuntimeError("download failed"),
        ):
            result = evaluate_blog(payload)

        self.assertEqual(result.status, "send Admin")
        self.assertEqual(len(result.tags), 3)

    def evaluate(self, caption_score: int, caption_related: bool, image_cafe_probability: int):
        payload = BlogEvaluateRequest(
            blogId="test-blog",
            caption="Quán cà phê yên tĩnh",
            imageUrls=["https://example.com/cafe.jpg"],
        )
        image = Image.new("RGB", (8, 8), color="white")
        clip_result = ClipImageResult(
            url="https://example.com/cafe.jpg",
            cafe_related=image_cafe_probability > 50,
            label_predict=CAFE_LABEL if image_cafe_probability > 50 else NOT_CAFE_LABEL,
            confidence_score=float(max(image_cafe_probability, 100 - image_cafe_probability)),
            all_scores={
                CAFE_LABEL: image_cafe_probability / 100,
                NOT_CAFE_LABEL: (100 - image_cafe_probability) / 100,
            },
        )

        with patch(
            "app.services.blog_evaluator.moderate_caption",
            return_value=CaptionModerationResult(caption_related, False, caption_score, "ok"),
        ), patch(
            "app.services.blog_evaluator.load_image_from_url",
            return_value=image,
        ), patch(
            "app.services.blog_evaluator.classify_image",
            return_value=clip_result,
        ), patch(
            "app.services.blog_evaluator.classify_image_tags",
            return_value=None,
        ), patch(
            "app.services.blog_evaluator.aggregate_tags",
            return_value=(["study cafe", "brunch cafe", "garden cafe"], False),
        ):
            return evaluate_blog(payload)


if __name__ == "__main__":
    unittest.main()
