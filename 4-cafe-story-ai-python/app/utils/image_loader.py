from io import BytesIO
from urllib.parse import urlparse

import requests
from PIL import Image


_AI_MAX_SIDE = 800


class ImageLoadError(Exception):
    pass


def load_image_from_url(url: str, timeout_seconds: float = 10.0) -> Image.Image:
    parsed = urlparse(url)
    if parsed.scheme not in {"http", "https"}:
        raise ImageLoadError("Only http and https image URLs are supported.")

    try:
        response = requests.get(url, timeout=timeout_seconds)
        response.raise_for_status()
        image = Image.open(BytesIO(response.content)).convert("RGB")
        image.load()
        return image
    except Exception as exc:
        raise ImageLoadError(f"Unable to load image: {exc}") from exc


def resize_for_ai(image: Image.Image, max_side: int = _AI_MAX_SIDE) -> Image.Image:
    w, h = image.size
    if w <= max_side and h <= max_side:
        return image
    ratio = min(max_side / w, max_side / h)
    new_size = (int(w * ratio), int(h * ratio))
    return image.resize(new_size, Image.LANCZOS)
