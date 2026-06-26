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
    logger.info("AI backend startup complete")
    yield


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
