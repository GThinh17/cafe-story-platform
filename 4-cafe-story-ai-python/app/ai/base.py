import logging
import time
from abc import ABC, abstractmethod

from PIL import Image


logger = logging.getLogger("cafestory-ai.timing")


class AIModel(ABC):
    @abstractmethod
    def _generate_text_impl(self, prompt: str) -> str: ...

    @abstractmethod
    def _generate_with_images_impl(self, prompt: str, images: list[Image.Image]) -> str: ...

    def generate_text(self, prompt: str) -> str:
        start = time.perf_counter()
        result = self._generate_text_impl(prompt)
        elapsed = time.perf_counter() - start
        logger.info("generate_text completed model=%s duration=%.2fs", getattr(self, "model_name", "?"), elapsed)
        return result

    def generate_with_images(self, prompt: str, images: list[Image.Image]) -> str:
        start = time.perf_counter()
        result = self._generate_with_images_impl(prompt, images)
        elapsed = time.perf_counter() - start
        logger.info("generate_with_images completed model=%s images=%d duration=%.2fs", getattr(self, "model_name", "?"), len(images), elapsed)
        return result
