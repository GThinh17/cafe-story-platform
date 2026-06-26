import base64
import io

import requests
from PIL import Image

from app.config.rules import get_ollama_config
from app.config.settings import OLLAMA_BASE_URL
from app.infra.base import AIModel


class OllamaModel(AIModel):
    def __init__(self, model_name: str) -> None:
        self.model_name = model_name
        config_url = get_ollama_config()["base_url"]
        self._base_url = (OLLAMA_BASE_URL or config_url).rstrip("/")

    def _image_to_base64(self, image: Image.Image) -> str:
        buf = io.BytesIO()
        image.convert("RGB").save(buf, format="JPEG")
        return base64.b64encode(buf.getvalue()).decode("utf-8")

    def _extract_content(self, response: requests.Response) -> str:
        response.raise_for_status()
        body = response.json()
        if "message" not in body:
            raise RuntimeError(f"Ollama error: {body}")
        return body["message"]["content"] or ""

    def _generate_text_impl(self, prompt: str) -> str:
        payload = {
            "model": self.model_name,
            "messages": [{"role": "user", "content": prompt}],
            "stream": False,
            "options": {"temperature": 0.1, "num_predict": 512},
        }
        response = requests.post(f"{self._base_url}/api/chat", json=payload, timeout=180)
        return self._extract_content(response)

    def _generate_with_images_impl(self, prompt: str, images: list[Image.Image]) -> str:
        payload = {
            "model": self.model_name,
            "messages": [
                {
                    "role": "user",
                    "content": prompt,
                    "images": [self._image_to_base64(img) for img in images],
                }
            ],
            "stream": False,
            "options": {"temperature": 0.1, "num_predict": 512},
        }
        response = requests.post(f"{self._base_url}/api/chat", json=payload, timeout=300)
        return self._extract_content(response)
