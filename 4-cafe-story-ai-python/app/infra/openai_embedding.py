import logging
import time

from openai import OpenAI

from app.config.rules import get_rag_config
from app.config.settings import OPENAI_API_KEY


logger = logging.getLogger("cafestory-ai.openai-embedding")

MAX_RETRIES = 3
RETRY_BASE_DELAY_SECONDS = 1.0


class OpenAIEmbeddingModel:

    def __init__(self) -> None:
        if not OPENAI_API_KEY:
            raise RuntimeError("OPENAI_API_KEY is required for embeddings.")
        config = get_rag_config()["models"]
        self.model_name: str = config["embedding"]
        self.dimensions: int = config["embedding_dimensions"]
        self._client = OpenAI(api_key=OPENAI_API_KEY, timeout=30.0)

    def embed_batch(self, texts: list[str]) -> list[list[float]]:
        if not texts:
            return []
        last_error: Exception | None = None
        for attempt in range(MAX_RETRIES):
            try:
                response = self._client.embeddings.create(model=self.model_name, input=texts)
                return [item.embedding for item in response.data]
            except Exception as exc:
                last_error = exc
                delay = RETRY_BASE_DELAY_SECONDS * (2 ** attempt)
                logger.warning("embedding attempt %s failed, retrying in %.1fs: %s", attempt + 1, delay, exc)
                time.sleep(delay)
        raise RuntimeError(f"Embedding failed after {MAX_RETRIES} retries") from last_error

    def embed_one(self, text: str) -> list[float]:
        return self.embed_batch([text])[0]
