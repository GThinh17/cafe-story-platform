import os

from dotenv import load_dotenv

load_dotenv()

GOOGLE_API_KEY: str = os.getenv("GOOGLE_API_KEY", "")
OPENROUTER_API_KEY: str = os.getenv("OPENROUTER_API_KEY", "")
OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
OLLAMA_BASE_URL: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")

RAG_PGVECTOR_DSN: str = os.getenv(
    "RAG_PGVECTOR_DSN",
    "postgresql://cafestory_rag_user:changeme@localhost:5432/cafestory_rag",
)
JAVA_BACKEND_BASE_URL: str = os.getenv("JAVA_BACKEND_BASE_URL", "http://localhost:8080")
RAG_INTERNAL_SECRET: str = os.getenv("RAG_INTERNAL_SECRET", "")
RAG_INGEST_CRON_MINUTES: int = int(os.getenv("RAG_INGEST_CRON_MINUTES", "15"))
RAG_DOCS_CORPUS_DIR: str = os.getenv(
    "RAG_DOCS_CORPUS_DIR",
    os.path.join(os.path.dirname(__file__), "..", "..", "..", "documents", "rag-corpus"),
)
RAG_DAILY_BUDGET_USD: float = float(os.getenv("RAG_DAILY_BUDGET_USD", "0.15"))
