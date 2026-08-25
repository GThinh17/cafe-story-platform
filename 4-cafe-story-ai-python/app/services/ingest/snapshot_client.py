import logging
from typing import Any, Iterator

from app.config.rules import get_rag_config
from app.utils.java_client import get_internal


logger = logging.getLogger("cafestory-ai.snapshot-client")

SNAPSHOT_PATH = "/api/internal/rag/snapshot"
FORMULA_SNAPSHOT_PATH = "/api/internal/rag/snapshots/formula"


def fetch_formula_snapshot() -> dict[str, Any]:
    """GET single-object snapshot cho active ReviewerFormula (Path B).

    Trả về dict từ formulaData() bên Java, hoặc {'empty': True} nếu chưa có
    active formula. Java wrap trong FormatResponse → get_internal đã unwrap.
    """
    result = get_internal(FORMULA_SNAPSHOT_PATH)
    return result if isinstance(result, dict) else {}


def iter_snapshot_pages(source_type: str, since: str | None) -> Iterator[dict[str, Any]]:
    """Iterate cursor qua Java snapshot endpoint (plan §11.1).

    Yield từng page: {items, tombstones, nextSince, nextSourceId, hasMore}.
    """
    limit = get_rag_config()["ingest"]["snapshot_page_limit"]
    cursor = since
    cursor_id: str | None = None
    while True:
        params: dict[str, Any] = {"sourceType": source_type, "limit": limit}
        if cursor:
            params["since"] = cursor
        if cursor_id:
            params["cursorId"] = cursor_id
        page = get_internal(SNAPSHOT_PATH, params)
        yield page
        if not page.get("hasMore"):
            break
        next_since = page.get("nextSince")
        next_cursor_id = page.get("nextSourceId")
        if not next_since or not next_cursor_id:
            logger.warning("snapshot cursor missing at %s/%s for %s, stopping", cursor, cursor_id, source_type)
            break
        if next_since == cursor and next_cursor_id == cursor_id:
            logger.warning("snapshot cursor stalled at %s/%s for %s, stopping", cursor, cursor_id, source_type)
            break
        cursor = next_since
        cursor_id = next_cursor_id
