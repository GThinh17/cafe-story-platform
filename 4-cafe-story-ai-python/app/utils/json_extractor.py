import json
import re
from typing import Any


def extract_json(raw: str) -> dict[str, Any]:
    """Extract the first JSON object from a string.

    Handles: bare JSON, ```json ... ```, or JSON embedded in surrounding text.
    """
    raw = raw.strip()

    # Try bare JSON first (most common for well-behaved models)
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        pass

    # Extract from ```json ... ``` or ``` ... ``` block
    block_match = re.search(r"```(?:json)?\s*([\s\S]*?)```", raw)
    if block_match:
        try:
            return json.loads(block_match.group(1).strip())
        except json.JSONDecodeError:
            pass

    # Last resort: find first {...} in the text
    brace_match = re.search(r"\{[\s\S]*\}", raw)
    if brace_match:
        try:
            return json.loads(brace_match.group(0))
        except json.JSONDecodeError:
            pass

    return {}
