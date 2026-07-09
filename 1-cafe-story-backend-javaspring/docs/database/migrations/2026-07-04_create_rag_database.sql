-- RAG vector database cho CafeStory chatbot.
-- Chạy MANUAL với superuser (không qua Flyway — DB riêng, tách khỏi cafestory_main).
-- Plan: documents/rag-implementation (§6, §13.1, §15.1, §15.5)

-- 1. Database + user riêng. Python service CHỈ có credential user này,
--    không thể truy cập cafestory_main (bảo mật Rule 1).
CREATE DATABASE cafestory_rag;

-- Đổi password trước khi chạy.
CREATE USER cafestory_rag_user WITH PASSWORD 'changeme-strong-random';

\c cafestory_rag

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- unaccent() mặc định là STABLE, không dùng được trong generated column/index expression.
-- Dùng trực tiếp trong INSERT (to_tsvector('simple', unaccent(content))) — xem vector_store.py.

-- 2. Bảng chunks chính. UNIQUE theo content_hash (không phải chunk_index)
--    để incremental reindex dedup được (plan §11.4).
CREATE TABLE rag_documents (
  id            BIGSERIAL PRIMARY KEY,
  source_type   TEXT NOT NULL,           -- 'cafe_page' | 'blog' | 'reviewer' | 'doc'
  source_id     TEXT NOT NULL,           -- UUID gốc hoặc 'filepath:section_id' cho docs
  chunk_index   INT NOT NULL DEFAULT 0,  -- thứ tự hiển thị, không phải business key
  content_hash  CHAR(16) NOT NULL,       -- sha256(body)[:16], KHÔNG gồm breadcrumb/header
  content       TEXT NOT NULL,           -- embed_text (breadcrumb + body)
  metadata      JSONB NOT NULL DEFAULT '{}',
  embedding     vector(1536),            -- text-embedding-3-small
  content_tsv   tsvector,                -- BM25: to_tsvector('simple', unaccent(content))
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (source_type, source_id, content_hash)
);

CREATE INDEX rag_documents_hnsw ON rag_documents
  USING hnsw (embedding vector_cosine_ops);
CREATE INDEX rag_documents_tsv ON rag_documents
  USING gin (content_tsv);
CREATE INDEX rag_documents_meta ON rag_documents
  USING gin (metadata);
CREATE INDEX rag_documents_trgm ON rag_documents
  USING gin (content gin_trgm_ops);
CREATE INDEX rag_documents_source ON rag_documents (source_type, source_id);

-- 3. Parent documents cho Small-to-Big (plan §15.1) —
--    retrieval trả chunk nhỏ, generate lấy full content local.
CREATE TABLE rag_parent_documents (
  source_type  TEXT NOT NULL,
  source_id    TEXT NOT NULL,
  full_content TEXT NOT NULL,
  metadata     JSONB NOT NULL DEFAULT '{}',
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (source_type, source_id)
);

-- 4. Cursor state cho incremental ingest (plan §11.1, §13.1).
CREATE TABLE rag_ingest_state (
  source_type     TEXT PRIMARY KEY,
  last_updated_at TIMESTAMPTZ,
  last_id         TEXT,
  last_commit_sha CHAR(40),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Manifest cho docs markdown (plan §13.1).
CREATE TABLE rag_docs_manifest (
  filepath        TEXT PRIMARY KEY,
  file_hash       CHAR(16) NOT NULL,
  last_commit_sha CHAR(40),
  section_count   INT NOT NULL DEFAULT 0,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Answer cache — FAQ lặp lại không tốn OpenAI call (plan §15.5).
CREATE TABLE rag_query_cache (
  query_hash  CHAR(16) PRIMARY KEY,
  answer      TEXT NOT NULL,
  sources     JSONB NOT NULL DEFAULT '[]',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Usage log — budget guard $0.15/ngày (plan §15.5).
CREATE TABLE rag_usage_log (
  id             BIGSERIAL PRIMARY KEY,
  operation      TEXT NOT NULL,          -- 'embed' | 'router' | 'generate'
  input_tokens   INT NOT NULL DEFAULT 0,
  output_tokens  INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX rag_usage_log_created ON rag_usage_log (created_at);

-- 8. GRANT tối thiểu — chỉ các bảng rag_*, không gì khác.
GRANT CONNECT ON DATABASE cafestory_rag TO cafestory_rag_user;
GRANT USAGE ON SCHEMA public TO cafestory_rag_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON
  rag_documents, rag_parent_documents, rag_ingest_state,
  rag_docs_manifest, rag_query_cache, rag_usage_log
TO cafestory_rag_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cafestory_rag_user;
