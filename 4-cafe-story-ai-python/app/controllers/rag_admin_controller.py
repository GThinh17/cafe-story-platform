import hashlib
import hmac
import logging
import time

from fastapi import APIRouter, Header, HTTPException, Request

from app.config.settings import RAG_INTERNAL_SECRET
from app.services.ingest.db_ingest import ingest_all_db_sources
from app.services.ingest.docs_ingest import ingest_docs_incremental


logger = logging.getLogger("cafestory-ai.rag-admin-controller")

router = APIRouter(prefix="/api/internal/rag", tags=["rag-admin"])

TIMESTAMP_WINDOW_SECONDS = 300


async def _verify_hmac(
    request: Request,
    method: str,
    path: str,
    timestamp: str | None,
    signature: str | None,
    body_hash: str | None,
) -> None:
    """Same scheme as Java RagHmacAuthFilter: method/path/query/timestamp/bodyHash."""
    if not RAG_INTERNAL_SECRET:
        raise HTTPException(status_code=401, detail="RAG internal secret is not configured")
    if not timestamp or not signature or not body_hash:
        raise HTTPException(status_code=401, detail="Missing RAG signature headers")
    try:
        if abs(time.time() - int(timestamp)) > TIMESTAMP_WINDOW_SECONDS:
            raise HTTPException(status_code=401, detail="RAG timestamp outside allowed window")
    except ValueError:
        raise HTTPException(status_code=401, detail="Invalid RAG timestamp")

    actual_body_hash = hashlib.sha256(await request.body()).hexdigest()
    if not hmac.compare_digest(actual_body_hash, body_hash):
        raise HTTPException(status_code=401, detail="Invalid RAG body hash")

    payload = f"{method}\n{path}\n\n{timestamp}\n{actual_body_hash}"
    expected = hmac.new(
        RAG_INTERNAL_SECRET.encode("utf-8"), payload.encode("utf-8"), hashlib.sha256
    ).hexdigest()
    if not hmac.compare_digest(expected, signature):
        raise HTTPException(status_code=401, detail="Invalid RAG signature")


@router.post("/reindex-docs")
async def reindex_docs(
    request: Request,
    x_rag_timestamp: str | None = Header(default=None),
    x_rag_signature: str | None = Header(default=None),
    x_rag_body_sha256: str | None = Header(default=None),
) -> dict:
    await _verify_hmac(
        request, "POST", "/api/internal/rag/reindex-docs",
        x_rag_timestamp, x_rag_signature, x_rag_body_sha256)
    return ingest_docs_incremental()


@router.post("/reindex-db")
async def reindex_db(
    request: Request,
    x_rag_timestamp: str | None = Header(default=None),
    x_rag_signature: str | None = Header(default=None),
    x_rag_body_sha256: str | None = Header(default=None),
) -> dict:
    await _verify_hmac(
        request, "POST", "/api/internal/rag/reindex-db",
        x_rag_timestamp, x_rag_signature, x_rag_body_sha256)
    return ingest_all_db_sources()
