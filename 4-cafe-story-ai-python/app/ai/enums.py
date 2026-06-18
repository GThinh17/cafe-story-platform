from enum import Enum


class ModelProvider(str, Enum):
    GEMINI = "gemini" #gemini-2.0-flash
    OPENROUTER = "openrouter" #nex-agi/nex-n2-pro:free
