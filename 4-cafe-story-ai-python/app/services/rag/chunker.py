import re
from typing import Any

from app.config.rules import get_rag_config
from app.services.rag.sanitizer import sanitize_text
from app.services.rag.vector_store import RagChunk


# Token estimate cho tiếng Việt: ~4 chars/token (đủ chính xác cho size cap).
def estimate_tokens(text: str) -> int:
    return max(1, len(text) // 4)


def _split_paragraphs(text: str) -> list[str]:
    paragraphs = [p.strip() for p in re.split(r"\n\s*\n", text)]
    return [p for p in paragraphs if p]


def _split_sentences(paragraph: str) -> list[str]:
    sentences = re.split(r"(?<=[.!?…])\s+", paragraph)
    return [s.strip() for s in sentences if s.strip()]


def _split_oversized_paragraph(paragraph: str, max_tokens: int) -> list[str]:
    """R1: không cắt giữa câu — gom câu cho tới sát max_tokens."""
    parts: list[str] = []
    current: list[str] = []
    current_tokens = 0
    for sentence in _split_sentences(paragraph):
        sentence_tokens = estimate_tokens(sentence)
        if current and current_tokens + sentence_tokens > max_tokens:
            parts.append(" ".join(current))
            current, current_tokens = [], 0
        current.append(sentence)
        current_tokens += sentence_tokens
    if current:
        parts.append(" ".join(current))
    return parts


def chunk_cafe_page(source_id: str, data: dict[str, Any]) -> list[RagChunk]:
    """Document-based (S12): 1 quán = 1 chunk. Tags từ AI moderation nếu blog thuộc quán có."""
    address_parts = [data.get("address"), data.get("area"), data.get("city"), data.get("province")]
    address = ", ".join(p for p in address_parts if p)
    lines = [
        f"Quán: {data.get('name') or ''}",
        f"Địa chỉ: {address}",
    ]
    if data.get("description"):
        lines.append(f"Mô tả: {sanitize_text(data['description'])}")

    # Lớp 3 fix (plan §16): thêm dòng "Từ khóa tìm kiếm" giúp BM25 và vector
    # dễ match với các biến thể user hay dùng ("quán cafe", "cà phê") và địa danh
    # (nhiều name gốc chỉ có "icafe" — BM25 không match "cafe" trực tiếp).
    search_keywords = ["quán cafe", "cà phê"]
    for value in (data.get("province"), data.get("city"), data.get("area"), data.get("ward")):
        if value and value not in search_keywords:
            search_keywords.append(value)
    lines.append(f"Từ khóa tìm kiếm: {', '.join(search_keywords)}")

    body = "\n".join(lines)

    metadata = {
        "name": data.get("name"),
        "province": data.get("province"),
        "city": data.get("city"),
        "region_id": data.get("regionId"),
        "follower_count": data.get("followerCount"),
        "like_count": data.get("likeCount"),
        "avatar_url": data.get("avatarUrl"),
        "cover_url": data.get("coverUrl"),
    }
    return [
        RagChunk(
            source_type="cafe_page",
            source_id=source_id,
            chunk_index=0,
            body=body,
            embed_text=body,
            metadata=metadata,
        )
    ]


def chunk_blog(source_id: str, data: dict[str, Any]) -> list[RagChunk]:
    """Paragraph-based (S10) + stable boundaries (§11.4).

    - Hash tính trên body paragraph (không gồm header) → đổi pageName không re-embed.
    - Không overlap giữa chunks (R3).
    """
    config = get_rag_config()["chunking"]
    max_tokens = config["paragraph_split_threshold_tokens"]

    content = sanitize_text(data.get("content") or "", mask_mentions=True)
    page_name = data.get("pageName")
    tags = data.get("tags") or []

    header_parts = []
    if page_name:
        header_parts.append(f"Quán: {page_name}")
    if tags:
        header_parts.append(f"Đặc điểm từ hình ảnh: {', '.join(tags)}")
    header = " | ".join(header_parts)

    bodies: list[str] = []
    for paragraph in _split_paragraphs(content):
        if estimate_tokens(paragraph) > max_tokens:
            bodies.extend(_split_oversized_paragraph(paragraph, max_tokens))
        else:
            bodies.append(paragraph)

    metadata = {
        "page_id": data.get("pageId"),
        "page_name": page_name,
        "region_id": data.get("regionId"),
        "author_user_name": data.get("authorUserName"),
        "tags": tags,
        "like_count": data.get("likeCount"),
        "image_urls": data.get("imageUrls") or [],
    }
    return [
        RagChunk(
            source_type="blog",
            source_id=source_id,
            chunk_index=index,
            body=body,
            embed_text=f"Bài viết về {page_name or 'quán cafe'}\n{header}\n\n{body}" if header else body,
            metadata=metadata,
        )
        for index, body in enumerate(bodies)
    ]


def chunk_reviewer(source_id: str, data: dict[str, Any]) -> list[RagChunk]:
    """Document-based: 1 reviewer = 1 chunk. Chỉ field public (không email/phone/stripe).

    Nội dung gồm: tên + username + khu vực + badge hiện tại + tổng số huy hiệu
    theo loại + số follower/like. Giúp match câu hỏi kiểu "reviewer nào ở Cần Thơ"
    (region) hoặc "reviewer nào nổi bật" (badge + follower).
    """
    full_name = data.get("userFullName") or data.get("userName") or ""
    user_name = data.get("userName") or ""

    lines: list[str] = []
    lines.append(f"Reviewer: {full_name}")
    if user_name:
        lines.append(f"Tên tài khoản: @{user_name}")

    region_parts = [data.get("area"), data.get("city"), data.get("province")]
    region_text = ", ".join(p for p in region_parts if p)
    if region_text:
        lines.append(f"Khu vực: {region_text}")

    latest_badge = data.get("latestBadge")
    latest_month = data.get("latestBadgeMonth")
    latest_score = data.get("latestBadgeScore")
    if latest_badge:
        badge_line = f"Huy hiệu gần nhất: {latest_badge}"
        if latest_month:
            badge_line += f" (tháng {latest_month})"
        if latest_score is not None:
            badge_line += f", điểm {latest_score}"
        lines.append(badge_line)

    badge_counts = data.get("badgeCounts") or {}
    if badge_counts:
        summary = ", ".join(f"{name} x{count} tháng" for name, count in badge_counts.items())
        lines.append(f"Lịch sử huy hiệu: {summary}")
    total_months = data.get("badgeTotalMonths")
    if total_months:
        lines.append(f"Tổng số tháng đạt huy hiệu: {total_months}")

    follower_count = data.get("followerCount") or 0
    like_count = data.get("likeCount") or 0
    if follower_count or like_count:
        lines.append(f"Số người theo dõi: {follower_count} | Lượt thích: {like_count}")

    # Lớp 3 (giống cafe_page): keyword line cho BM25 dễ match biến thể user hay dùng.
    search_keywords = ["reviewer", "người đánh giá", "người review"]
    for value in (data.get("province"), data.get("city"), data.get("area")):
        if value and value not in search_keywords:
            search_keywords.append(value)
    if latest_badge:
        search_keywords.append(f"huy hiệu {latest_badge}")
    lines.append(f"Từ khóa tìm kiếm: {', '.join(search_keywords)}")

    body = "\n".join(lines)
    metadata = {
        "user_name": user_name,
        "user_full_name": full_name,
        "user_avatar": data.get("userAvatar"),
        "province": data.get("province"),
        "city": data.get("city"),
        "area": data.get("area"),
        "region_id": data.get("regionId"),
        "latest_badge": latest_badge,
        "follower_count": follower_count,
        "like_count": like_count,
    }
    return [
        RagChunk(
            source_type="reviewer",
            source_id=source_id,
            chunk_index=0,
            body=body,
            embed_text=body,
            metadata=metadata,
        )
    ]


def full_content_for_parent(chunks: list[RagChunk]) -> str:
    return "\n\n".join(chunk.body for chunk in chunks)
