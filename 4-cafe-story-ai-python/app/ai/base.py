from abc import ABC, abstractmethod

from PIL import Image


class AIModel(ABC):
    @abstractmethod
    def generate_text(self, prompt: str) -> str: ...

    @abstractmethod
    def generate_with_images(self, prompt: str, images: list[Image.Image]) -> str: ...
