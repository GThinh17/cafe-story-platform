import json
from functools import lru_cache
from pathlib import Path
from typing import Any


RULES_PATH = Path(__file__).with_name("moderation_rules.json")


@lru_cache(maxsize=1)
def get_rules() -> dict[str, Any]:
    with RULES_PATH.open("r", encoding="utf-8") as file:
        return json.load(file)
