from enum import Enum


class ModelProvider(str, Enum):
    GEMINI = "gemini"
    OPENROUTER = "openrouter"
    OLLAMA = "ollama"
