from functools import lru_cache
from pathlib import Path
from typing import Any

import jinja2
from PIL import Image

from app.infra.base import AIModel
from app.prompts.enums import ModelProvider
from app.prompts.model_config import get_ai_model_config


_TEMPLATES_DIR = Path(__file__).parent / "templates"

_jinja_env = jinja2.Environment(
    loader=jinja2.FileSystemLoader(str(_TEMPLATES_DIR)),
    trim_blocks=True,
    lstrip_blocks=True,
)


@lru_cache(maxsize=8)
def _get_model(provider: ModelProvider, model_name: str) -> AIModel:
    if provider == ModelProvider.GEMINI:
        from app.infra.gemini import GeminiModel
        return GeminiModel(model_name)
    if provider == ModelProvider.OPENROUTER:
        from app.infra.openrouter import OpenRouterModel
        return OpenRouterModel(model_name)
    if provider == ModelProvider.OLLAMA:
        from app.infra.ollama import OllamaModel
        return OllamaModel(model_name)
    if provider == ModelProvider.OPENAI:
        from app.infra.openai_model import OpenAIModel
        return OpenAIModel(model_name)
    raise ValueError(f"Unknown provider: {provider}")


def render_prompt(template_name: str, **kwargs: Any) -> str:
    return _jinja_env.get_template(template_name).render(**kwargs)


def get_response(prompt: str, images: list[Image.Image] | None = None) -> str:
    cfg = get_ai_model_config()
    provider = ModelProvider(cfg["provider"])
    if images:
        model_name = cfg.get("vision_model") or cfg.get("model_name")
    else:
        model_name = cfg.get("text_model") or cfg.get("model_name")
    model = _get_model(provider, model_name)
    if images:
        return model.generate_with_images(prompt, images)
    return model.generate_text(prompt)
