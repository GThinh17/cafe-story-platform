from typing import Any

from app.config.rules import get_rules


def get_ai_model_config() -> dict[str, Any]:
    return get_rules()["ai_model"]
