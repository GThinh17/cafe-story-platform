import hashlib
import hmac
import json
import logging
import time
from typing import Any
from urllib.parse import urlencode

import requests

from app.config.settings import JAVA_BACKEND_BASE_URL, RAG_INTERNAL_SECRET


logger = logging.getLogger("cafestory-ai.java-client")

SIGNATURE_HEADER = "X-RAG-Signature"
TIMESTAMP_HEADER = "X-RAG-Timestamp"
BODY_HASH_HEADER = "X-RAG-Body-SHA256"
DEFAULT_TIMEOUT_SECONDS = 30


class JavaClientError(RuntimeError):
    pass


def _body_hash(body: str = "") -> str:
    return hashlib.sha256(body.encode("utf-8")).hexdigest()


def _canonical_json(json_body: dict[str, Any]) -> str:
    return json.dumps(json_body, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def _sign(method: str, path: str, query: str, timestamp: str, body_hash: str | None = None) -> str:
    payload = f"{method}\n{path}\n{query}\n{timestamp}\n{body_hash or _body_hash()}"
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
    body_hash = _body_hash()
    headers = {
        TIMESTAMP_HEADER: timestamp,
        BODY_HASH_HEADER: body_hash,
        SIGNATURE_HEADER: _sign("GET", path, query, timestamp, body_hash),
    }
    url = f"{JAVA_BACKEND_BASE_URL}{path}"
    if query:
        url = f"{url}?{query}"

    response = requests.get(url, headers=headers, timeout=DEFAULT_TIMEOUT_SECONDS)
    return _unwrap(response, path)


def post_internal(path: str, json_body: dict[str, Any]) -> Any:
    """Call a Java /api/internal/** POST endpoint with HMAC headers.

    Body được canonicalize, hash SHA-256 và đưa vào HMAC payload để request
    không thể bị replay với nội dung JSON khác trong timestamp window.
    """
    if not RAG_INTERNAL_SECRET:
        raise JavaClientError("RAG_INTERNAL_SECRET is not configured")

    timestamp = str(int(time.time()))
    body = _canonical_json(json_body)
    body_hash = _body_hash(body)
    headers = {
        TIMESTAMP_HEADER: timestamp,
        BODY_HASH_HEADER: body_hash,
        SIGNATURE_HEADER: _sign("POST", path, "", timestamp, body_hash),
        "Content-Type": "application/json",
    }
    response = requests.post(
        f"{JAVA_BACKEND_BASE_URL}{path}",
        data=body.encode("utf-8"),
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
