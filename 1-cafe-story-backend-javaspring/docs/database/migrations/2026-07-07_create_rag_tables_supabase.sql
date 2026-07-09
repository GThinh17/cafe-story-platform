-- RAG tables cho CafeStory chatbot — BẢN SUPABASE.
-- Khác bản 2026-07-04 (self-host): Supabase không cho CREATE DATABASE,
-- nên các bảng rag_* nằm trong database `postgres` hiện tại (prefix riêng).
-- Lưu ý bảo mật: trên môi trường self-host production, dùng bản 2026-07-04
-- với DB + user riêng (plan §6). Trên Supabase, cân nhắc tạo role hạn chế sau.

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS rag_documents (
  id            BIGSERIAL PRIMARY KEY,
  source_type   TEXT NOT NULL,
  source_id     TEXT NOT NULL,
  chunk_index   INT NOT NULL DEFAULT 0,
  content_hash  CHAR(16) NOT NULL,
  content       TEXT NOT NULL,
  metadata      JSONB NOT NULL DEFAULT '{}',
  embedding     vector(1536),
  content_tsv   tsvector,
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (source_type, source_id, content_hash)
);

CREATE INDEX IF NOT EXISTS rag_documents_hnsw ON rag_documents
  USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS rag_documents_tsv ON rag_documents
  USING gin (content_tsv);
CREATE INDEX IF NOT EXISTS rag_documents_meta ON rag_documents
  USING gin (metadata);
CREATE INDEX IF NOT EXISTS rag_documents_trgm ON rag_documents
  USING gin (content gin_trgm_ops);
CREATE INDEX IF NOT EXISTS rag_documents_source ON rag_documents (source_type, source_id);

CREATE TABLE IF NOT EXISTS rag_parent_documents (
  source_type  TEXT NOT NULL,
  source_id    TEXT NOT NULL,
  full_content TEXT NOT NULL,
  metadata     JSONB NOT NULL DEFAULT '{}',
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (source_type, source_id)
);

CREATE TABLE IF NOT EXISTS rag_ingest_state (
  source_type     TEXT PRIMARY KEY,
  last_updated_at TIMESTAMPTZ,
  last_id         TEXT,
  last_commit_sha CHAR(40),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rag_docs_manifest (
  filepath        TEXT PRIMARY KEY,
  file_hash       CHAR(16) NOT NULL,
  last_commit_sha CHAR(40),
  section_count   INT NOT NULL DEFAULT 0,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rag_query_cache (
  query_hash  CHAR(16) PRIMARY KEY,
  answer      TEXT NOT NULL,
  sources     JSONB NOT NULL DEFAULT '[]',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rag_usage_log (
  id             BIGSERIAL PRIMARY KEY,
  operation      TEXT NOT NULL,
  input_tokens   INT NOT NULL DEFAULT 0,
  output_tokens  INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS rag_usage_log_created ON rag_usage_log (created_at);

SELECT 'rag tables created' AS status;
