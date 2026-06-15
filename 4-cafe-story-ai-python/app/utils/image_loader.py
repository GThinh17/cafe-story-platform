from io import BytesIO
from urllib.parse import urlparse

import requests
from PIL import Image


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
