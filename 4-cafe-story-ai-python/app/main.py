import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.config.rules import get_rules
from app.router import router


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("cafestory-ai")


@asynccontextmanager
async def lifespan(app: FastAPI):
    get_rules()
    scheduler_started = False
    try:
        from app.services.ingest.ingest_scheduler import start_ingest_scheduler

        start_ingest_scheduler()
        scheduler_started = True
    except Exception:
        logger.exception("ingest scheduler failed to start — RAG ingest disabled")
    logger.info("AI backend startup complete (ingest scheduler=%s)", scheduler_started)
    yield
    if scheduler_started:
        from app.services.ingest.ingest_scheduler import shutdown_ingest_scheduler

        shutdown_ingest_scheduler()


app = FastAPI(
    title="CafeStory AI Moderation Backend",
    version="2.0.0",
    lifespan=lifespan,
    default_response_class=JSONResponse,
)


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.exception("unhandled request failed path=%s", request.url.path)
    return JSONResponse(
        status_code=500,
        content={"status": "error", "message": "Internal AI backend error."},
    )


app.include_router(router)
