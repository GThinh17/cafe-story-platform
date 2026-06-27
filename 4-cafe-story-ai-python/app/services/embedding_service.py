import logging
from dataclasses import dataclass

import requests

from app.config.rules import get_ollama_config
from app.config.settings import OLLAMA_BASE_URL


logger = logging.getLogger("cafestory-ai.embedding")


@dataclass(frozen=True)
class EmbeddingResult:
    vector: list[float]
    error: str | None = None


def _base_url() -> str:
    return (OLLAMA_BASE_URL or get_ollama_config()["base_url"]).rstrip("/")


def _embedding_model() -> str:
    return get_ollama_config()["embedding_model"]


def embed_text(text: str) -> EmbeddingResult:
    try:
        payload = {"model": _embedding_model(), "prompt": text}
        response = requests.post(f"{_base_url()}/api/embeddings", json=payload, timeout=30)
        response.raise_for_status()
        return EmbeddingResult(vector=response.json()["embedding"])
    except Exception as exc:
        logger.exception("embedding failed")
        return EmbeddingResult(vector=[], error=str(exc))


def embed_batch(texts: list[str]) -> list[EmbeddingResult]:
    return [embed_text(t) for t in texts]
