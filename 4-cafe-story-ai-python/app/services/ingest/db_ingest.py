import logging
from typing import Any, Callable

from app.config.rules import get_rag_config
from app.services.ingest.snapshot_client import fetch_formula_snapshot, iter_snapshot_pages
from app.services.rag import chunker
from app.services.rag.embedder import embed_texts
from app.services.rag.vector_store import RagChunk, get_vector_store


logger = logging.getLogger("cafestory-ai.db-ingest")

_CHUNKERS: dict[str, Callable[[str, dict[str, Any]], list[RagChunk]]] = {
    "cafe_page": chunker.chunk_cafe_page,
    "blog": chunker.chunk_blog,
    "reviewer": chunker.chunk_reviewer,
}


def ingest_source_type(source_type: str) -> dict[str, int]:
    """Delta ingest 1 source type từ Java snapshot (plan §5.1, §11).

    Idempotent: content-hash dedup trong upsert_with_hash_check khiến item
    trùng cursor được xử lý lại với chi phí 0.
    """
    store = get_vector_store()
    chunk_fn = _CHUNKERS[source_type]

    state = store.get_ingest_state(source_type)
    since = None
    if state and state["last_updated_at"]:
        since = state["last_updated_at"].isoformat()

    totals = {"items": 0, "embedded": 0, "deleted": 0, "kept": 0, "tombstoned": 0}
    last_seen: str | None = None

    for page in iter_snapshot_pages(source_type, since):
        for item in page.get("items", []):
            source_id = item["sourceId"]
            chunks = chunk_fn(source_id, item.get("data") or {})
            counters = store.upsert_with_hash_check(source_type, source_id, chunks, embed_texts)
            store.upsert_parent_document(
                source_type,
                source_id,
                chunker.full_content_for_parent(chunks),
                chunks[0].metadata if chunks else {},
            )
            totals["items"] += 1
            totals["embedded"] += counters["embedded"]
            totals["deleted"] += counters["deleted"]
            totals["kept"] += counters["kept"]
            last_seen = item.get("updatedAt") or last_seen

        for tombstone_id in page.get("tombstones", []):
            deleted = store.delete_by_source(source_type, tombstone_id)
            if deleted:
                totals["tombstoned"] += 1

        if page.get("nextSince"):
            last_seen = page["nextSince"]

    if last_seen:
        store.set_ingest_state(source_type, last_updated_at=last_seen)

    logger.info("db ingest %s: %s", source_type, totals)
    return totals


def ingest_formula() -> dict[str, int]:
    """Ingest active ReviewerFormula thành 1 chunk source_type='doc' (Path B).

    Idempotent: hash-check trong upsert bỏ qua nếu formula không đổi. Chạy sau
    các source_type khác trong ingest_all_db_sources — admin update formula gọi
    reindex-db endpoint, hàm này bảo đảm chunk _system/reviewer-formula sync.
    """
    data = fetch_formula_snapshot()
    chunks = chunker.chunk_formula(data)
    if not chunks:
        return {"skipped": 1}

    store = get_vector_store()
    counters = store.upsert_with_hash_check(
        "doc", chunker.FORMULA_SOURCE_ID, chunks, embed_texts,
    )
    store.upsert_parent_document(
        "doc",
        chunker.FORMULA_SOURCE_ID,
        chunker.full_content_for_parent(chunks),
        chunks[0].metadata,
    )
    return counters


def ingest_all_db_sources() -> dict[str, dict[str, int]]:
    results: dict[str, dict[str, int]] = {}
    for source_type in get_rag_config()["ingest"]["source_types"]:
        try:
            results[source_type] = ingest_source_type(source_type)
        except Exception:
            logger.exception("ingest failed for source_type=%s", source_type)
            results[source_type] = {"error": 1}
    try:
        results["formula"] = ingest_formula()
    except Exception:
        logger.exception("formula ingest failed")
        results["formula"] = {"error": 1}
    return results
