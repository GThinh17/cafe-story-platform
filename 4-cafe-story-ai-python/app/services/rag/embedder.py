import logging
import time
from functools import lru_cache

from app.config.rules import get_rag_config
from app.config.settings import RAG_DAILY_BUDGET_USD
from app.infra.openai_embedding import OpenAIEmbeddingModel
from app.services.rag.chunker import estimate_tokens
from app.services.rag.vector_store import get_vector_store


logger = logging.getLogger("cafestory-ai.embedder")

# Giá gpt-4o-mini + text-embedding-3-small (USD / 1M tokens) — budget guard §15.5.
EMBEDDING_PRICE_PER_1M = 0.02
GENERATOR_INPUT_PRICE_PER_1M = 0.15
GENERATOR_OUTPUT_PRICE_PER_1M = 0.60

# Cache spend trong process để không SUM lại rag_usage_log 2-3 lần mỗi request.
# Ngân sách tính theo ngày nên trễ ~vài giây là chấp nhận được.
_SPEND_CACHE_TTL_SECONDS = 30.0
_spend_cache: tuple[float, float] | None = None  # (monotonic_at, spend_usd)


class BudgetExceededError(RuntimeError):
    pass


@lru_cache(maxsize=1)
def _get_model() -> OpenAIEmbeddingModel:
    return OpenAIEmbeddingModel()


def _spend_for(operation: str, input_tokens: int, output_tokens: int) -> float:
    """USD cho 1 operation, dùng đúng đơn giá từng loại (embed rẻ hơn generator ~7.5x)."""
    if operation == "embed":
        return input_tokens * EMBEDDING_PRICE_PER_1M / 1_000_000
    # router + generate (gpt-4o-mini) và mọi operation lạ → giá generator (an toàn).
    return (
        input_tokens * GENERATOR_INPUT_PRICE_PER_1M / 1_000_000
        + output_tokens * GENERATOR_OUTPUT_PRICE_PER_1M / 1_000_000
    )


def _compute_spend_today_usd() -> float:
    by_op = get_vector_store().usage_today_by_operation()
    return sum(_spend_for(op, inp, out) for op, (inp, out) in by_op.items())


def estimated_spend_today_usd(use_cache: bool = True) -> float:
    global _spend_cache
    if use_cache and _spend_cache is not None:
        cached_at, cached_spend = _spend_cache
        if time.monotonic() - cached_at < _SPEND_CACHE_TTL_SECONDS:
            return cached_spend
    spend = _compute_spend_today_usd()
    _spend_cache = (time.monotonic(), spend)
    return spend


def check_budget() -> None:
    spend = estimated_spend_today_usd()
    if spend >= RAG_DAILY_BUDGET_USD:
        raise BudgetExceededError(
            f"Daily RAG budget exceeded: ${spend:.4f} >= ${RAG_DAILY_BUDGET_USD}"
        )


def embed_texts(texts: list[str]) -> list[list[float]]:
    """Embed batch với budget guard + usage logging."""
    if not texts:
        return []
    check_budget()

    config = get_rag_config()["ingest"]
    batch_size = config["embed_batch_size"]
    model = _get_model()

    embeddings: list[list[float]] = []
    for start in range(0, len(texts), batch_size):
        batch = texts[start:start + batch_size]
        embeddings.extend(model.embed_batch(batch))
        total_tokens = sum(estimate_tokens(t) for t in batch)
        get_vector_store().log_usage("embed", total_tokens)

    return embeddings


def embed_query(text: str) -> list[float]:
    check_budget()
    embedding = _get_model().embed_one(text)
    get_vector_store().log_usage("embed", estimate_tokens(text))
    return embedding
