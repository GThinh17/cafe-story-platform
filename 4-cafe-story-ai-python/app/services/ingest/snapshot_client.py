import logging
from typing import Any, Iterator

from app.config.rules import get_rag_config
from app.utils.java_client import get_internal


logger = logging.getLogger("cafestory-ai.snapshot-client")

SNAPSHOT_PATH = "/api/internal/rag/snapshot"


def iter_snapshot_pages(source_type: str, since: str | None) -> Iterator[dict[str, Any]]:
    """Iterate cursor qua Java snapshot endpoint (plan §11.1).

    Yield từng page: {items, tombstones, nextSince, hasMore}.
    """
    limit = get_rag_config()["ingest"]["snapshot_page_limit"]
    cursor = since
    while True:
        params: dict[str, Any] = {"sourceType": source_type, "limit": limit}
        if cursor:
            params["since"] = cursor
        page = get_internal(SNAPSHOT_PATH, params)
        yield page
        if not page.get("hasMore"):
            break
        next_since = page.get("nextSince")
        if not next_since or next_since == cursor:
            logger.warning("snapshot cursor stalled at %s for %s, stopping", cursor, source_type)
            break
        cursor = next_since
