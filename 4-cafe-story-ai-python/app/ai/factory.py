from functools import lru_cache

from app.ai.base import AIModel
from app.ai.enums import ModelProvider


@lru_cache(maxsize=8)
def get_model(provider: ModelProvider, model_name: str) -> AIModel:
    if provider == ModelProvider.GEMINI:
        from app.ai.gemini import GeminiModel
        return GeminiModel(model_name)
    if provider == ModelProvider.OPENROUTER:
        from app.ai.openrouter import OpenRouterModel
        return OpenRouterModel(model_name)
    raise ValueError(f"Unknown provider: {provider}")
