import hashlib
import hmac
import logging
import time
from typing import Any
from urllib.parse import urlencode

import requests

from app.config.settings import JAVA_BACKEND_BASE_URL, RAG_INTERNAL_SECRET


logger = logging.getLogger("cafestory-ai.java-client")

SIGNATURE_HEADER = "X-RAG-Signature"
TIMESTAMP_HEADER = "X-RAG-Timestamp"
DEFAULT_TIMEOUT_SECONDS = 30


class JavaClientError(RuntimeError):
    pass


def _sign(method: str, path: str, query: str, timestamp: str) -> str:
    payload = f"{method}\n{path}\n{query}\n{timestamp}"
    digest = hmac.new(
        RAG_INTERNAL_SECRET.encode("utf-8"),
        payload.encode("utf-8"),
        hashlib.sha256,
    )
    return digest.hexdigest()


def get_internal(path: str, params: dict[str, Any] | None = None) -> Any:
    """Call a Java /api/internal/** GET endpoint with HMAC headers.

    Returns the unwrapped `data` field from the Java FormatResponse envelope.
    """
    if not RAG_INTERNAL_SECRET:
        raise JavaClientError("RAG_INTERNAL_SECRET is not configured")

    query = urlencode(params or {})
    timestamp = str(int(time.time()))
    headers = {
        TIMESTAMP_HEADER: timestamp,
        SIGNATURE_HEADER: _sign("GET", path, query, timestamp),
    }
    url = f"{JAVA_BACKEND_BASE_URL}{path}"
    if query:
        url = f"{url}?{query}"

    response = requests.get(url, headers=headers, timeout=DEFAULT_TIMEOUT_SECONDS)
    return _unwrap(response, path)


def post_internal(path: str, json_body: dict[str, Any]) -> Any:
    """Call a Java /api/internal/** POST endpoint with HMAC headers.

    Body không nằm trong signature (khớp RagHmacAuthFilter — chỉ sign
    method/path/query/timestamp); secret + timestamp window vẫn chặn replay.
    """
    if not RAG_INTERNAL_SECRET:
        raise JavaClientError("RAG_INTERNAL_SECRET is not configured")

    timestamp = str(int(time.time()))
    headers = {
        TIMESTAMP_HEADER: timestamp,
        SIGNATURE_HEADER: _sign("POST", path, "", timestamp),
    }
    response = requests.post(
        f"{JAVA_BACKEND_BASE_URL}{path}",
        json=json_body,
        headers=headers,
        timeout=DEFAULT_TIMEOUT_SECONDS,
    )
    return _unwrap(response, path)


def _unwrap(response: requests.Response, path: str) -> Any:
    if response.status_code != 200:
        raise JavaClientError(
            f"Java internal call failed: {response.status_code} {path} {response.text[:200]}"
        )
    body = response.json()
    if isinstance(body, dict) and "data" in body:
        return body["data"]
    return body
