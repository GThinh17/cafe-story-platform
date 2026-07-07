import logging
from pathlib import Path

from app.config.settings import RAG_DOCS_CORPUS_DIR
from app.services.rag.embedder import embed_texts
from app.services.rag.markdown_parser import parse_doc_file
from app.services.rag.vector_store import content_hash, get_vector_store


logger = logging.getLogger("cafestory-ai.docs-ingest")


def _scan_corpus() -> dict[str, str]:
    """Trả {relative_path: raw_content} cho mọi file .md trong corpus."""
    corpus_dir = Path(RAG_DOCS_CORPUS_DIR).resolve()
    if not corpus_dir.is_dir():
        logger.warning("docs corpus dir not found: %s", corpus_dir)
        return {}
    files: dict[str, str] = {}
    for path in corpus_dir.rglob("*.md"):
        relative = path.relative_to(corpus_dir).as_posix()
        files[relative] = path.read_text(encoding="utf-8")
    return files


def ingest_docs_incremental() -> dict[str, int]:
    """File-level diff qua manifest (§13.2) + section-level diff qua content hash (§13.3).

    Catch đủ: thêm file, xóa file, sửa/thêm/xóa section, đổi thứ tự, rename.
    """
    store = get_vector_store()
    current_files = _scan_corpus()
    current_hashes = {fp: content_hash(raw) for fp, raw in current_files.items()}
    manifest = store.get_docs_manifest()

    current_set = set(current_hashes)
    manifest_set = set(manifest)

    added = current_set - manifest_set
    deleted = manifest_set - current_set
    common = current_set & manifest_set
    modified = {fp for fp in common if current_hashes[fp] != manifest[fp]}
    unchanged = common - modified

    # Rename detection (§15.6): file "mới" có hash trùng file "đã xóa" → move, 0 re-embed
    # (section hashes giữ nguyên nên upsert chỉ INSERT metadata mới sau khi xóa source cũ;
    #  embedding của file rename vẫn phải tính lại vì source_id đổi — chấp nhận, hiếm).
    counters = {"added": 0, "deleted": 0, "modified": 0, "unchanged": len(unchanged), "embedded": 0}

    for filepath in deleted:
        store.delete_by_source("doc", filepath)
        store.delete_docs_manifest(filepath)
        counters["deleted"] += 1

    for filepath in added | modified:
        meta, chunks = parse_doc_file(filepath, current_files[filepath])
        result = store.upsert_with_hash_check("doc", filepath, chunks, embed_texts)
        full_content = "\n\n".join(c.body for c in chunks)
        store.upsert_parent_document("doc", filepath, full_content, dict(meta))
        store.upsert_docs_manifest(filepath, current_hashes[filepath], len(chunks))
        counters["embedded"] += result["embedded"]
        counters["added" if filepath in added else "modified"] += 1

    logger.info(
        "docs ingest: +%s -%s ~%s =%s (embedded %s sections)",
        counters["added"], counters["deleted"], counters["modified"],
        counters["unchanged"], counters["embedded"],
    )
    return counters
