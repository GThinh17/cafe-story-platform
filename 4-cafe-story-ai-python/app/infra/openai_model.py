import base64
import io

from openai import OpenAI
from PIL import Image

from app.config.settings import OPENAI_API_KEY
from app.infra.base import AIModel


class OpenAIModel(AIModel):
    def __init__(self, model_name: str) -> None:
        self.model_name = model_name
        if not OPENAI_API_KEY:
            raise RuntimeError("OPENAI_API_KEY is required for OpenAI.")
        self._client = OpenAI(api_key=OPENAI_API_KEY, timeout=15.0)

    def _image_to_base64(self, image: Image.Image) -> str:
        buf = io.BytesIO()
        image.convert("RGB").save(buf, format="JPEG")
        return base64.b64encode(buf.getvalue()).decode("utf-8")

    def _generate_text_impl(self, prompt: str) -> str:
        response = self._client.chat.completions.create(
            model=self.model_name,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=300,
            temperature=0,
            response_format={"type": "json_object"},
        )
        return response.choices[0].message.content or ""

    def _generate_with_images_impl(self, prompt: str, images: list[Image.Image]) -> str:
        content: list[dict] = [{"type": "text", "text": prompt}]
        for image in images:
            b64 = self._image_to_base64(image)
            content.append({"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{b64}"}})
        response = self._client.chat.completions.create(
            model=self.model_name,
            messages=[{"role": "user", "content": content}],
            max_tokens=400,
            temperature=0,
        )
        return response.choices[0].message.content or ""
