import os

from dotenv import load_dotenv
from google import genai
from google.genai import types
from PIL import Image

from app.ai.base import AIModel

load_dotenv()


class GeminiModel(AIModel):
    def __init__(self, model_name: str) -> None:
        self.model_name = model_name
        api_key = os.getenv("GOOGLE_API_KEY")
        if not api_key:
            raise RuntimeError("GOOGLE_API_KEY is required for Gemini.")
        self._client = genai.Client(api_key=api_key)

    def _generate_text_impl(self, prompt: str) -> str:
        response = self._client.models.generate_content(
            model=self.model_name,
            contents=prompt,
            config=types.GenerateContentConfig(response_mime_type="application/json"),
        )
        return response.text or ""

    def _generate_with_images_impl(self, prompt: str, images: list[Image.Image]) -> str:
        response = self._client.models.generate_content(
            model=self.model_name,
            contents=[prompt, *images],
        )
        return response.text or ""
