import json
import logging
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from functools import lru_cache
from typing import Any, Iterator

from openai import OpenAI

from app.config.rules import get_rag_config
from app.config.settings import OPENAI_API_KEY
from app.prompts.model_selector import render_prompt
from app.services.chat.user_context_client import fetch_my_blog_moderation
from app.services.rag.chunker import estimate_tokens
from app.services.rag.embedder import BudgetExceededError, check_budget, embed_query
from app.services.rag.router import RouteDecision, route_query
from app.services.rag.sanitizer import sanitize_text
from app.services.rag.vector_store import RetrievedChunk, content_hash, get_vector_store
from app.services.tools.sql_tools import fetch_top_reviewers, fetch_trending_cafes


logger = logging.getLogger("cafestory-ai.chat-generator")

BUSY_MESSAGE = "Hệ thống đang bận, vui lòng thử lại sau ít phút nhé."
NO_CONTEXT_MESSAGE = "Mình chưa có thông tin về điều này. Bạn thử hỏi theo cách khác xem sao nhé."
LOGIN_HINT_MESSAGE = (
    "Để xem lý do cụ thể cho bài viết của bạn, hãy đăng nhập rồi hỏi lại, "
    "hoặc vào mục \"Bài viết của tôi\" để xem trạng thái từng bài."
)


@dataclass
class ChatAnswer:
    answer: str
    sources: list[dict[str, Any]]
    cached: bool = False
    route: str | None = None


@dataclass
class _Prepared:
    """Kết quả bước retrieval — dùng chung cho cả sync và streaming."""
    early_answer: ChatAnswer | None = None
    decision: RouteDecision | None = None
    chunks: list[RetrievedChunk] | None = None
    prompt: str | None = None
    cacheable: bool = False
    query_hash: str = ""


@lru_cache(maxsize=1)
def _get_client() -> OpenAI:
    if not OPENAI_API_KEY:
        raise RuntimeError("OPENAI_API_KEY is required for chat generation.")
    return OpenAI(api_key=OPENAI_API_KEY, timeout=30.0)


def _tool_chunk(source_type: str, source_id: str, content: str, metadata: dict[str, Any]) -> RetrievedChunk:
    return RetrievedChunk(
        source_type=source_type,
        source_id=source_id,
        chunk_index=0,
        content=content,
        metadata=metadata,
        score=1.0,
    )


def _trending_chunks(province: str | None) -> list[RetrievedChunk]:
    cafes = fetch_trending_cafes(limit=5, province=province)
    return [
        _tool_chunk(
            "cafe_page",
            cafe.get("id", ""),
            "Quán đang được quan tâm: {name}\nĐịa chỉ: {address}\nKhu vực: {city}, {province}\n"
            "Lượt theo dõi: {followers} | Lượt thích: {likes}".format(
                name=cafe.get("name"),
                address=cafe.get("address"),
                city=cafe.get("city") or "",
                province=cafe.get("province") or "",
                followers=cafe.get("followerCount") or 0,
                likes=cafe.get("likeCount") or 0,
            ),
            {"name": cafe.get("name"), "image_urls": [u for u in [cafe.get("avatarUrl")] if u]},
        )
        for cafe in cafes
    ]


def _top_reviewer_chunks() -> list[RetrievedChunk]:
    reviewers = fetch_top_reviewers(limit=5)
    return [
        _tool_chunk(
            "reviewer",
            reviewer.get("reviewerId", ""),
            "Reviewer nổi bật: {full_name} (@{user_name})\nLượt theo dõi: {followers}".format(
                full_name=reviewer.get("userFullName") or reviewer.get("userName"),
                user_name=reviewer.get("userName"),
                followers=reviewer.get("followerCount") or 0,
            ),
            {"name": reviewer.get("userFullName") or reviewer.get("userName")},
        )
        for reviewer in reviewers
    ]


def _personal_moderation_chunks(user_jwt: str) -> list[RetrievedChunk]:
    """Route E cá nhân (plan §7 Phase 4): dữ liệu user-scoped, KHÔNG cache."""
    items = fetch_my_blog_moderation(user_jwt)
    chunks = []
    for item in items:
        lines = [
            f"Bài viết của bạn (trạng thái: {item.get('blogStatus')}, "
            f"kết quả kiểm duyệt: {item.get('decision')}):"
        ]
        if item.get("captionSnippet"):
            lines.append(f"- Trích nội dung: \"{item['captionSnippet']}\"")
        if item.get("captionReason"):
            lines.append(f"- Đánh giá nội dung: {item['captionReason']}")
        if item.get("imageReason"):
            lines.append(f"- Đánh giá hình ảnh: {item['imageReason']}")
        if item.get("resolvedAction"):
            lines.append(f"- Admin đã xử lý: {item['resolvedAction']}")
        chunks.append(
            _tool_chunk(
                "user_moderation",
                item.get("blogId", ""),
                "\n".join(lines),
                {"name": "Bài viết của bạn"},
            )
        )
    return chunks


def _retrieve_for_route(
    decision: RouteDecision,
    query_embedding: list[float] | None = None,
    user_jwt: str | None = None,
) -> list[RetrievedChunk]:
    """Adaptive dispatch (plan §1): mỗi route 1 chiến lược retrieval."""
    if decision.route == "A":
        query_lower = decision.rewritten_query.lower()
        if "reviewer" in query_lower:
            return _top_reviewer_chunks()
        return _trending_chunks(decision.province)

    if decision.route == "E" and user_jwt:
        personal = _personal_moderation_chunks(user_jwt)
        if personal:
            return personal
        # Không có kết quả kiểm duyệt nào → fall through sang docs chính sách.

    store = get_vector_store()
    if query_embedding is None:
        query_embedding = embed_query(decision.rewritten_query)

    if decision.route in ("C", "D", "E"):
        return store.hybrid_search(
            query_embedding=query_embedding,
            query_text=decision.rewritten_query,
            source_types=["doc"],
        )

    # Route B (default): tìm quán/bài viết, ưu tiên filter province trước similarity.
    metadata_filter = {"province": decision.province} if decision.province else None
    chunks = store.hybrid_search(
        query_embedding=query_embedding,
        query_text=decision.rewritten_query,
        source_types=["cafe_page", "blog", "reviewer"],
        metadata_filter=metadata_filter,
    )
    if not chunks and metadata_filter:
        # Data cũ có thể thiếu region metadata → retry không filter (recall > precision).
        chunks = store.hybrid_search(
            query_embedding=query_embedding,
            query_text=decision.rewritten_query,
            source_types=["cafe_page", "blog", "reviewer"],
        )
    return chunks


def _chunk_sources(chunks: list[RetrievedChunk]) -> list[dict[str, Any]]:
    sources = []
    seen: set[tuple[str, str]] = set()
    for chunk in chunks:
        key = (chunk.source_type, chunk.source_id)
        if key in seen:
            continue
        seen.add(key)
        sources.append(
            {
                "source_type": chunk.source_type,
                "source_id": chunk.source_id,
                "title": chunk.metadata.get("name")
                or chunk.metadata.get("doc_title")
                or chunk.metadata.get("page_name")
                or (
                    f"Bài viết của @{chunk.metadata.get('author_user_name')}"
                    if chunk.metadata.get("author_user_name")
                    else None
                )
                or chunk.source_id,
                "image_urls": chunk.metadata.get("image_urls") or [],
            }
        )
    return sources


def _prepare(
    query: str,
    platform: str | None,
    history: list[dict[str, Any]] | None,
    user_jwt: str | None,
) -> _Prepared:
    """Router + retrieval chung cho sync và streaming.

    §15.4: router và embed chạy song song (ThreadPoolExecutor — pattern
    blog_evaluator). Nếu router rewrite khác query gốc thì re-embed (rẻ).
    """
    query = (query or "").strip()
    if not query:
        return _Prepared(early_answer=ChatAnswer(answer=NO_CONTEXT_MESSAGE, sources=[]))

    store = get_vector_store()

    # Cache: chỉ câu hỏi độc lập, không user-scoped.
    cacheable = not history and not user_jwt
    query_hash = content_hash(f"{platform or ''}:{query.lower()}")
    if cacheable:
        cached = store.get_cached_answer(query_hash)
        if cached:
            return _Prepared(
                early_answer=ChatAnswer(
                    answer=cached["answer"], sources=cached["sources"], cached=True)
            )

    try:
        check_budget()
        with ThreadPoolExecutor(max_workers=2) as pool:
            route_future = pool.submit(route_query, query, history)
            embed_future = pool.submit(embed_query, query)
            decision = route_future.result()
            try:
                original_embedding = embed_future.result()
            except Exception:
                original_embedding = None

        query_embedding = original_embedding
        if decision.rewritten_query != query or query_embedding is None:
            query_embedding = None  # _retrieve_for_route sẽ embed lại theo rewritten

        chunks = _retrieve_for_route(decision, query_embedding, user_jwt)
    except BudgetExceededError:
        logger.warning("budget exceeded, refusing query")
        return _Prepared(early_answer=ChatAnswer(answer=BUSY_MESSAGE, sources=[]))

    if not chunks:
        no_context = NO_CONTEXT_MESSAGE
        if decision.route == "E" and not user_jwt:
            no_context = LOGIN_HINT_MESSAGE
        return _Prepared(
            early_answer=ChatAnswer(answer=no_context, sources=[], route=decision.route))

    prompt = render_prompt("chat_answer.j2", chunks=chunks, query=decision.rewritten_query)
    return _Prepared(
        decision=decision,
        chunks=chunks,
        prompt=prompt,
        cacheable=cacheable,
        query_hash=query_hash,
    )


def _finalize(prepared: _Prepared, raw_answer: str, usage_in: int, usage_out: int) -> ChatAnswer:
    store = get_vector_store()
    store.log_usage("generate", usage_in, usage_out)

    # PII redaction ở output (plan §3 Rule 5) — lớp cuối chống hallucinated PII.
    answer = sanitize_text(raw_answer)
    sources = _chunk_sources(prepared.chunks or [])

    if prepared.cacheable:
        store.set_cached_answer(prepared.query_hash, answer, sources)
    return ChatAnswer(
        answer=answer,
        sources=sources,
        route=prepared.decision.route if prepared.decision else None,
    )


def answer_query(
    query: str,
    platform: str | None = None,
    history: list[dict[str, Any]] | None = None,
    user_jwt: str | None = None,
) -> ChatAnswer:
    prepared = _prepare(query, platform, history, user_jwt)
    if prepared.early_answer:
        return prepared.early_answer

    config = get_rag_config()["models"]
    response = _get_client().chat.completions.create(
        model=config["generator"],
        messages=[{"role": "user", "content": prepared.prompt}],
        max_tokens=600,
        temperature=0.3,
    )
    raw_answer = response.choices[0].message.content or NO_CONTEXT_MESSAGE
    usage = response.usage
    return _finalize(
        prepared,
        raw_answer,
        usage.prompt_tokens if usage else estimate_tokens(prepared.prompt or ""),
        usage.completion_tokens if usage else estimate_tokens(raw_answer),
    )


def answer_query_stream(
    query: str,
    platform: str | None = None,
    history: list[dict[str, Any]] | None = None,
    user_jwt: str | None = None,
) -> Iterator[str]:
    """SSE stream (plan §7 Phase 4). Yield từng event `data: {...}\n\n`.

    Event: {"delta": "..."} cho từng token, cuối cùng {"done": true, "sources": [...]}.
    Lưu ý: PII redaction trên stream áp dụng ở bản final (delta đã redact theo chunk).
    """
    prepared = _prepare(query, platform, history, user_jwt)
    if prepared.early_answer:
        answer = prepared.early_answer
        yield _sse({"delta": answer.answer})
        yield _sse({"done": True, "sources": answer.sources, "cached": answer.cached})
        return

    config = get_rag_config()["models"]
    stream = _get_client().chat.completions.create(
        model=config["generator"],
        messages=[{"role": "user", "content": prepared.prompt}],
        max_tokens=600,
        temperature=0.3,
        stream=True,
    )

    collected: list[str] = []
    for event in stream:
        delta = event.choices[0].delta.content if event.choices else None
        if delta:
            collected.append(delta)
            yield _sse({"delta": sanitize_text(delta)})

    raw_answer = "".join(collected) or NO_CONTEXT_MESSAGE
    result = _finalize(
        prepared,
        raw_answer,
        estimate_tokens(prepared.prompt or ""),
        estimate_tokens(raw_answer),
    )
    yield _sse({"done": True, "sources": result.sources, "route": result.route})


def _sse(payload: dict[str, Any]) -> str:
    return f"data: {json.dumps(payload, ensure_ascii=False)}\n\n"
