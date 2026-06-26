import os

from dotenv import load_dotenv

load_dotenv()

GOOGLE_API_KEY: str = os.getenv("GOOGLE_API_KEY", "")
OPENROUTER_API_KEY: str = os.getenv("OPENROUTER_API_KEY", "")
OLLAMA_BASE_URL: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
