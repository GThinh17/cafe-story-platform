import base64
import io
import os

import requests
from dotenv import load_dotenv
from PIL import Image

from app.ai.base import AIModel

load_dotenv()


_API_URL = "https://openrouter.ai/api/v1/chat/completions"


class OpenRouterModel(AIModel):
    def __init__(self, model_name: str) -> None:
        self.model_name = model_name
        api_key = os.getenv("OPENROUTER_API_KEY")
        if not api_key:
            raise RuntimeError("OPENROUTER_API_KEY is required for OpenRouter.")
        self._headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        }

    def _image_to_base64(self, image: Image.Image) -> str:
        buf = io.BytesIO()
        image.convert("RGB").save(buf, format="JPEG")
        return base64.b64encode(buf.getvalue()).decode("utf-8")

    def _extract_content(self, response: requests.Response) -> str:
        response.raise_for_status()
        body = response.json()
        if "choices" not in body:
            error = body.get("error", body)
            raise RuntimeError(f"OpenRouter error: {error}")
        return body["choices"][0]["message"]["content"] or ""

    def _generate_text_impl(self, prompt: str) -> str:
        payload = {
            "model": self.model_name,
            "messages": [{"role": "user", "content": prompt}],
        }
        response = requests.post(_API_URL, headers=self._headers, json=payload, timeout=60)
        return self._extract_content(response)

    def _generate_with_images_impl(self, prompt: str, images: list[Image.Image]) -> str:
        content: list[dict] = [{"type": "text", "text": prompt}]
        for image in images:
            b64 = self._image_to_base64(image)
            content.append({
                "type": "image_url",
                "image_url": {"url": f"data:image/jpeg;base64,{b64}"},
            })
        payload = {
            "model": self.model_name,
            "messages": [{"role": "user", "content": content}],
        }
        response = requests.post(_API_URL, headers=self._headers, json=payload, timeout=120)
        return self._extract_content(response)
