import logging

from apscheduler.schedulers.background import BackgroundScheduler

from app.config.settings import RAG_INGEST_CRON_MINUTES
from app.services.ingest.db_ingest import ingest_all_db_sources
from app.services.ingest.docs_ingest import ingest_docs_incremental
from app.services.rag.vector_store import get_vector_store


logger = logging.getLogger("cafestory-ai.ingest-scheduler")

_scheduler: BackgroundScheduler | None = None


def _run_db_ingest() -> None:
    try:
        ingest_all_db_sources()
    except Exception:
        logger.exception("scheduled db ingest failed")


def _run_docs_ingest() -> None:
    try:
        ingest_docs_incremental()
    except Exception:
        logger.exception("scheduled docs ingest failed")


def _run_purge() -> None:
    try:
        get_vector_store().purge_old_records()
    except Exception:
        logger.exception("scheduled purge failed")


def start_ingest_scheduler() -> BackgroundScheduler:
    global _scheduler
    if _scheduler is not None:
        return _scheduler

    scheduler = BackgroundScheduler(timezone="UTC")
    scheduler.add_job(
        _run_db_ingest,
        "interval",
        minutes=RAG_INGEST_CRON_MINUTES,
        id="rag-db-ingest",
        coalesce=True,
        max_instances=1,
    )
    scheduler.add_job(
        _run_docs_ingest,
        "interval",
        minutes=max(RAG_INGEST_CRON_MINUTES * 2, 30),
        id="rag-docs-ingest",
        coalesce=True,
        max_instances=1,
    )
    # C9: dọn rag_usage_log / rag_query_cache mỗi ngày để không phình bảng.
    scheduler.add_job(
        _run_purge,
        "interval",
        minutes=1440,
        id="rag-purge",
        coalesce=True,
        max_instances=1,
    )
    scheduler.start()
    _scheduler = scheduler
    logger.info("ingest scheduler started (db=%sm, docs=%sm)",
                RAG_INGEST_CRON_MINUTES, max(RAG_INGEST_CRON_MINUTES * 2, 30))
    return scheduler


def shutdown_ingest_scheduler() -> None:
    global _scheduler
    if _scheduler is not None:
        _scheduler.shutdown(wait=False)
        _scheduler = None
