from typing import Any

import frontmatter

from app.config.rules import get_rag_config
from app.services.rag.chunker import estimate_tokens
from app.services.rag.vector_store import RagChunk


def _split_by_heading(lines: list[str], level: int) -> tuple[str, list[tuple[str, list[str]]]]:
    """Line-based split theo heading level, không cắt trong fenced code block (R1)."""
    marker = "#" * level + " "
    preamble: list[str] = []
    sections: list[tuple[str, list[str]]] = []
    current_title: str | None = None
    current_lines: list[str] = []
    in_code_block = False

    for line in lines:
        if line.lstrip().startswith("```"):
            in_code_block = not in_code_block
        if not in_code_block and line.startswith(marker):
            if current_title is not None:
                sections.append((current_title, current_lines))
            current_title = line[len(marker):].strip()
            current_lines = []
        elif current_title is None:
            preamble.append(line)
        else:
            current_lines.append(line)

    if current_title is not None:
        sections.append((current_title, current_lines))
    return "\n".join(preamble).strip(), sections


def parse_doc_file(relative_path: str, raw_content: str) -> tuple[dict[str, Any], list[RagChunk]]:
    """Parse frontmatter + chunk theo H2 (S13). H3 gộp vào H2 cha nếu ≤ threshold, tách nếu vượt.

    source_id = relative_path cho MỌI chunk của file → upsert_with_hash_check
    tự làm section-level diff (§13.3). Hash trên body (không breadcrumb) → đổi
    title/frontmatter không re-embed (R2).
    """
    post = frontmatter.loads(raw_content)
    meta = dict(post.metadata)
    threshold = get_rag_config()["chunking"]["doc_section_split_threshold_tokens"]

    lines = post.content.splitlines()
    _, h1_sections = _split_by_heading(lines, 1)
    doc_title = h1_sections[0][0] if h1_sections else meta.get("title", relative_path)
    body_lines = h1_sections[0][1] if h1_sections else lines

    h2_preamble, h2_sections = _split_by_heading(body_lines, 2)

    platform = str(meta.get("platform", "both"))
    category = str(meta.get("category", ""))
    breadcrumb_root = " › ".join(
        p for p in [platform.capitalize() if platform != "both" else "CafeStory", category, doc_title] if p
    )

    base_metadata = {
        "platform": platform,
        "category": category,
        "doc_slug": meta.get("slug", ""),
        "doc_title": doc_title,
        "tags": meta.get("tags", []),
        "version": meta.get("version", 1),
        "filepath": relative_path,
    }

    chunks: list[RagChunk] = []

    def add_chunk(section_title: str, body: str) -> None:
        body = body.strip()
        if not body:
            return
        breadcrumb = f"[{breadcrumb_root} › {section_title}]" if section_title else f"[{breadcrumb_root}]"
        chunks.append(
            RagChunk(
                source_type="doc",
                source_id=relative_path,
                chunk_index=len(chunks),
                body=body,
                embed_text=f"{breadcrumb}\n\n{body}",
                metadata={**base_metadata, "section_title": section_title},
            )
        )

    if h2_preamble:
        add_chunk("", h2_preamble)

    for h2_title, h2_lines in h2_sections:
        h2_text = "\n".join(h2_lines).strip()
        if estimate_tokens(h2_text) <= threshold:
            add_chunk(h2_title, h2_text)
            continue
        # Section quá dài → tách theo H3, giữ preamble H2 làm chunk riêng.
        h3_preamble, h3_sections = _split_by_heading(h2_lines, 3)
        if not h3_sections:
            add_chunk(h2_title, h2_text)
            continue
        if h3_preamble:
            add_chunk(h2_title, h3_preamble)
        for h3_title, h3_lines in h3_sections:
            add_chunk(f"{h2_title} › {h3_title}", "\n".join(h3_lines).strip())

    return meta, chunks
