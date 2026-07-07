import hashlib
import json
import logging
from dataclasses import dataclass, field
from typing import Any

import psycopg
from pgvector.psycopg import register_vector
from psycopg_pool import ConnectionPool

from app.config.rules import get_rag_config
from app.config.settings import RAG_PGVECTOR_DSN


logger = logging.getLogger("cafestory-ai.vector-store")


def content_hash(body: str) -> str:
    return hashlib.sha256(body.encode("utf-8")).hexdigest()[:16]


@dataclass
class RagChunk:
    source_type: str
    source_id: str
    chunk_index: int
    body: str                       # raw body, dùng để hash — KHÔNG chứa breadcrumb/header
    embed_text: str                 # breadcrumb/header + body, dùng để embed
    metadata: dict[str, Any] = field(default_factory=dict)

    @property
    def body_hash(self) -> str:
        return content_hash(self.body)


@dataclass
class RetrievedChunk:
    source_type: str
    source_id: str
    chunk_index: int
    content: str
    metadata: dict[str, Any]
    score: float


class VectorStore:

    def __init__(self, dsn: str | None = None) -> None:
        self._pool = ConnectionPool(
            dsn or RAG_PGVECTOR_DSN,
            min_size=1,
            max_size=5,
            configure=self._configure_connection,
            open=True,
        )

    @staticmethod
    def _configure_connection(conn: psycopg.Connection) -> None:
        register_vector(conn)

    def existing_hashes(self, source_type: str, source_id: str) -> set[str]:
        with self._pool.connection() as conn:
            rows = conn.execute(
                """
                SELECT content_hash FROM rag_documents
                WHERE source_type = %s AND source_id = %s
                """,
                (source_type, source_id),
            ).fetchall()
        return {row[0] for row in rows}

    def upsert_with_hash_check(
        self,
        source_type: str,
        source_id: str,
        chunks: list[RagChunk],
        embed_fn,
    ) -> dict[str, int]:
        """Incremental reindex: chỉ embed chunks có body_hash mới (plan §11.4).

        embed_fn: callable(list[str]) -> list[list[float]]
        Returns counters {embedded, deleted, kept}.
        """
        new_hashes = {c.body_hash for c in chunks}
        old_hashes = self.existing_hashes(source_type, source_id)

        to_delete = old_hashes - new_hashes
        to_embed = [c for c in chunks if c.body_hash not in old_hashes]
        to_keep = [c for c in chunks if c.body_hash in old_hashes]

        embeddings = embed_fn([c.embed_text for c in to_embed]) if to_embed else []

        with self._pool.connection() as conn:
            with conn.transaction():
                if to_delete:
                    conn.execute(
                        """
                        DELETE FROM rag_documents
                        WHERE source_type = %s AND source_id = %s
                          AND content_hash = ANY(%s)
                        """,
                        (source_type, source_id, list(to_delete)),
                    )
                for chunk, embedding in zip(to_embed, embeddings):
                    conn.execute(
                        """
                        INSERT INTO rag_documents
                            (source_type, source_id, chunk_index, content_hash,
                             content, metadata, embedding, content_tsv, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s,
                                to_tsvector('simple', unaccent(%s)), NOW())
                        ON CONFLICT (source_type, source_id, content_hash)
                        DO UPDATE SET
                            chunk_index = EXCLUDED.chunk_index,
                            metadata = EXCLUDED.metadata,
                            updated_at = NOW()
                        """,
                        (
                            chunk.source_type,
                            chunk.source_id,
                            chunk.chunk_index,
                            chunk.body_hash,
                            chunk.embed_text,
                            json.dumps(chunk.metadata, ensure_ascii=False, default=str),
                            embedding,
                            chunk.embed_text,
                        ),
                    )
                for chunk in to_keep:
                    conn.execute(
                        """
                        UPDATE rag_documents
                        SET chunk_index = %s, metadata = %s, updated_at = NOW()
                        WHERE source_type = %s AND source_id = %s AND content_hash = %s
                        """,
                        (
                            chunk.chunk_index,
                            json.dumps(chunk.metadata, ensure_ascii=False, default=str),
                            source_type,
                            source_id,
                            chunk.body_hash,
                        ),
                    )

        counters = {"embedded": len(to_embed), "deleted": len(to_delete), "kept": len(to_keep)}
        logger.info("upsert %s:%s -> %s", source_type, source_id, counters)
        return counters

    def upsert_parent_document(
        self, source_type: str, source_id: str, full_content: str, metadata: dict[str, Any]
    ) -> None:
        with self._pool.connection() as conn:
            conn.execute(
                """
                INSERT INTO rag_parent_documents (source_type, source_id, full_content, metadata, updated_at)
                VALUES (%s, %s, %s, %s, NOW())
                ON CONFLICT (source_type, source_id)
                DO UPDATE SET full_content = EXCLUDED.full_content,
                              metadata = EXCLUDED.metadata,
                              updated_at = NOW()
                """,
                (source_type, source_id, full_content, json.dumps(metadata, ensure_ascii=False, default=str)),
            )

    def get_parent_document(self, source_type: str, source_id: str) -> str | None:
        with self._pool.connection() as conn:
            row = conn.execute(
                """
                SELECT full_content FROM rag_parent_documents
                WHERE source_type = %s AND source_id = %s
                """,
                (source_type, source_id),
            ).fetchone()
        return row[0] if row else None

    def delete_by_source(self, source_type: str, source_id: str) -> int:
        with self._pool.connection() as conn:
            with conn.transaction():
                result = conn.execute(
                    "DELETE FROM rag_documents WHERE source_type = %s AND source_id = %s",
                    (source_type, source_id),
                )
                conn.execute(
                    "DELETE FROM rag_parent_documents WHERE source_type = %s AND source_id = %s",
                    (source_type, source_id),
                )
        return result.rowcount

    def hybrid_search(
        self,
        query_embedding: list[float],
        query_text: str,
        source_types: list[str] | None = None,
        metadata_filter: dict[str, str] | None = None,
        top_k: int | None = None,
    ) -> list[RetrievedChunk]:
        """RRF fusion giữa vector cosine và BM25 (tsvector unaccented)."""
        config = get_rag_config()["retrieval"]
        top_k = top_k or config["top_k"]
        candidate_k = config["candidate_k"]
        rrf_k = config["rrf_k"]

        filters = ["TRUE"]
        params: list[Any] = []
        if source_types:
            filters.append("source_type = ANY(%s)")
            params.append(source_types)
        if metadata_filter:
            for key, value in metadata_filter.items():
                filters.append("metadata->>%s ILIKE %s")
                params.extend([key, f"%{value}%"])
        where_clause = " AND ".join(filters)

        sql = f"""
            WITH vector_hits AS (
                SELECT id, ROW_NUMBER() OVER (ORDER BY embedding <=> %s::vector) AS rank
                FROM rag_documents
                WHERE {where_clause}
                ORDER BY embedding <=> %s::vector
                LIMIT %s
            ),
            text_hits AS (
                SELECT id, ROW_NUMBER() OVER (
                    ORDER BY ts_rank(content_tsv, plainto_tsquery('simple', unaccent(%s))) DESC
                ) AS rank
                FROM rag_documents
                WHERE {where_clause}
                  AND content_tsv @@ plainto_tsquery('simple', unaccent(%s))
                LIMIT %s
            ),
            fused AS (
                SELECT COALESCE(v.id, t.id) AS id,
                       COALESCE(1.0 / (%s + v.rank), 0) + COALESCE(1.0 / (%s + t.rank), 0) AS rrf_score
                FROM vector_hits v
                FULL OUTER JOIN text_hits t ON v.id = t.id
            )
            SELECT d.source_type, d.source_id, d.chunk_index, d.content, d.metadata, f.rrf_score
            FROM fused f
            JOIN rag_documents d ON d.id = f.id
            ORDER BY f.rrf_score DESC
            LIMIT %s
        """
        query_params = (
            [query_embedding]
            + params
            + [query_embedding, candidate_k]
            + [query_text]
            + params
            + [query_text, candidate_k, rrf_k, rrf_k, top_k]
        )

        with self._pool.connection() as conn:
            conn.execute("SET hnsw.ef_search = 100")
            rows = conn.execute(sql, query_params).fetchall()

        return [
            RetrievedChunk(
                source_type=row[0],
                source_id=row[1],
                chunk_index=row[2],
                content=row[3],
                metadata=row[4] if isinstance(row[4], dict) else json.loads(row[4]),
                score=float(row[5]),
            )
            for row in rows
        ]


    # ---- Ingest state (plan §11.1, §13.1) ----

    def get_ingest_state(self, source_type: str) -> dict[str, Any] | None:
        with self._pool.connection() as conn:
            row = conn.execute(
                """
                SELECT last_updated_at, last_id, last_commit_sha
                FROM rag_ingest_state WHERE source_type = %s
                """,
                (source_type,),
            ).fetchone()
        if row is None:
            return None
        return {"last_updated_at": row[0], "last_id": row[1], "last_commit_sha": row[2]}

    def set_ingest_state(
        self,
        source_type: str,
        last_updated_at: Any = None,
        last_id: str | None = None,
        last_commit_sha: str | None = None,
    ) -> None:
        with self._pool.connection() as conn:
            conn.execute(
                """
                INSERT INTO rag_ingest_state (source_type, last_updated_at, last_id, last_commit_sha, updated_at)
                VALUES (%s, %s, %s, %s, NOW())
                ON CONFLICT (source_type)
                DO UPDATE SET
                    last_updated_at = COALESCE(EXCLUDED.last_updated_at, rag_ingest_state.last_updated_at),
                    last_id = COALESCE(EXCLUDED.last_id, rag_ingest_state.last_id),
                    last_commit_sha = COALESCE(EXCLUDED.last_commit_sha, rag_ingest_state.last_commit_sha),
                    updated_at = NOW()
                """,
                (source_type, last_updated_at, last_id, last_commit_sha),
            )

    # ---- Docs manifest (plan §13.1) ----

    def get_docs_manifest(self) -> dict[str, str]:
        with self._pool.connection() as conn:
            rows = conn.execute("SELECT filepath, file_hash FROM rag_docs_manifest").fetchall()
        return {row[0]: row[1] for row in rows}

    def upsert_docs_manifest(self, filepath: str, file_hash: str, section_count: int) -> None:
        with self._pool.connection() as conn:
            conn.execute(
                """
                INSERT INTO rag_docs_manifest (filepath, file_hash, section_count, updated_at)
                VALUES (%s, %s, %s, NOW())
                ON CONFLICT (filepath)
                DO UPDATE SET file_hash = EXCLUDED.file_hash,
                              section_count = EXCLUDED.section_count,
                              updated_at = NOW()
                """,
                (filepath, file_hash, section_count),
            )

    def delete_docs_manifest(self, filepath: str) -> None:
        with self._pool.connection() as conn:
            conn.execute("DELETE FROM rag_docs_manifest WHERE filepath = %s", (filepath,))

    # ---- Query cache (plan §15.5) ----

    def get_cached_answer(self, query_hash: str, ttl_minutes: int = 60) -> dict[str, Any] | None:
        with self._pool.connection() as conn:
            row = conn.execute(
                """
                SELECT answer, sources FROM rag_query_cache
                WHERE query_hash = %s
                  AND created_at >= NOW() - make_interval(mins => %s)
                """,
                (query_hash, ttl_minutes),
            ).fetchone()
        if row is None:
            return None
        sources = row[1] if isinstance(row[1], list) else json.loads(row[1])
        return {"answer": row[0], "sources": sources}

    def set_cached_answer(self, query_hash: str, answer: str, sources: list[dict[str, Any]]) -> None:
        with self._pool.connection() as conn:
            conn.execute(
                """
                INSERT INTO rag_query_cache (query_hash, answer, sources, created_at)
                VALUES (%s, %s, %s, NOW())
                ON CONFLICT (query_hash)
                DO UPDATE SET answer = EXCLUDED.answer,
                              sources = EXCLUDED.sources,
                              created_at = NOW()
                """,
                (query_hash, answer, json.dumps(sources, ensure_ascii=False, default=str)),
            )

    # ---- Usage log / budget guard (plan §15.5) ----

    def log_usage(self, operation: str, input_tokens: int, output_tokens: int = 0) -> None:
        with self._pool.connection() as conn:
            conn.execute(
                """
                INSERT INTO rag_usage_log (operation, input_tokens, output_tokens)
                VALUES (%s, %s, %s)
                """,
                (operation, input_tokens, output_tokens),
            )

    def usage_today_tokens(self) -> tuple[int, int]:
        with self._pool.connection() as conn:
            row = conn.execute(
                """
                SELECT COALESCE(SUM(input_tokens), 0), COALESCE(SUM(output_tokens), 0)
                FROM rag_usage_log
                WHERE created_at >= date_trunc('day', NOW())
                """
            ).fetchone()
        return int(row[0]), int(row[1])


_store: VectorStore | None = None


def get_vector_store() -> VectorStore:
    global _store
    if _store is None:
        _store = VectorStore()
    return _store
