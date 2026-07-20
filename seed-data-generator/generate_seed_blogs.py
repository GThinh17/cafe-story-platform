"""
Sinh SQL seed 100 blog (20 cafe × 5 blog) + 200 blog_images.

Follow rule BE:
- author_user_id = owner của cafe (query runtime từ cafe_pages)
- page_id = cafe.id (blog thuộc cafe page → hiện ở cafe page)
- region_id = cafe.region_id (BE require không null)
- content NOT NULL — caption tiếng Việt
- imageUrls (via blog_images table): 2 ảnh Cloudinary
- status = PUBLISHED

created_at rải trong 60 ngày với thứ tự interleaved:
  blog_slot = (blog_index - 1) * 20 + (cafe_index - 1)   # 0..99
  offset_minutes = blog_slot * 864                        # 14.4h step
  created_at = now() - interval '{offset_minutes} minutes'

→ Blog #1 của mỗi cafe rải đều 20 slot đầu (mỗi cafe cách 14.4h)
→ Blog #2 của mỗi cafe rải slot 20-39
→ ...
→ Cả feed đa dạng chronological, không cluster theo cafe.

Chạy: python generate_seed_blogs.py
Output: output/2026-07-16_seed_100_blogs_cantho.sql
"""

from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Dict, List

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

from cantho_cafes import CANTHO_CAFES
from vietnamese_blog_captions import caption_for

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_100_blogs_cantho.sql"
BLOG_URLS_PATH = Path(__file__).parent / "blog_image_urls.json"

BLOGS_PER_CAFE = 5
SPREAD_DAYS = 60
MINUTES_PER_SLOT = (SPREAD_DAYS * 24 * 60) // (BLOGS_PER_CAFE * len(CANTHO_CAFES))  # 864


def _sql_escape(text: str) -> str:
    return text.replace("'", "''")


def _load_blog_urls() -> Dict[str, Dict[str, List[str]]]:
    if not BLOG_URLS_PATH.is_file():
        raise SystemExit(f"Không tìm thấy {BLOG_URLS_PATH.name} — chạy upload_blog_images.py trước.")
    urls = json.loads(BLOG_URLS_PATH.read_text(encoding="utf-8"))
    missing = []
    for cafe_idx in range(1, 21):
        cafe_key = str(cafe_idx)
        if cafe_key not in urls:
            missing.append(f"cafe#{cafe_idx}")
            continue
        for blog_idx in range(1, 6):
            blog_key = str(blog_idx)
            if blog_key not in urls[cafe_key]:
                missing.append(f"cafe#{cafe_idx} blog#{blog_idx}")
                continue
            imgs = urls[cafe_key][blog_key]
            if len(imgs) != 2 or not all(imgs):
                missing.append(f"cafe#{cafe_idx} blog#{blog_idx} (imgs={len(imgs)})")
    if missing:
        raise SystemExit(f"Thiếu URLs cho: {missing[:5]}{'...' if len(missing) > 5 else ''}")
    return urls


def _emit_cafe_blogs(cafe_index: int, cafe_name: str, blog_urls: Dict[str, List[str]]) -> str:
    """Sinh SQL block cho 5 blog của 1 cafe."""
    lines: List[str] = [
        f"    -- === Cafe #{cafe_index:02d}: {cafe_name} ===",
        f"    SELECT cp.id, cp.owner_user_id, cp.region_id",
        f"    INTO v_cafe_id, v_owner_id, v_region_id",
        f"    FROM cafe_pages cp WHERE cp.name = '{_sql_escape(cafe_name)}';",
        f"    IF v_cafe_id IS NULL THEN",
        f"        RAISE EXCEPTION 'Cafe not found: {_sql_escape(cafe_name)}';",
        f"    END IF;",
        f"    IF v_owner_id IS NULL THEN",
        f"        RAISE EXCEPTION 'Cafe % has no owner_user_id', v_cafe_id;",
        f"    END IF;",
        f"    IF v_region_id IS NULL THEN",
        f"        RAISE EXCEPTION 'Cafe % has no region_id', v_cafe_id;",
        f"    END IF;",
        "",
    ]

    for blog_idx in range(1, BLOGS_PER_CAFE + 1):
        blog_slot = (blog_idx - 1) * len(CANTHO_CAFES) + (cafe_index - 1)
        offset_minutes = blog_slot * MINUTES_PER_SLOT
        caption = caption_for(blog_idx, cafe_name)
        img1, img2 = blog_urls[str(blog_idx)]

        lines.extend([
            f"    -- Blog #{blog_idx} (slot {blog_slot}, {offset_minutes} min ago)",
            f"    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,",
            f"                       is_pinned, allow_comment, like_count, share_count, comment_count,",
            f"                       created_at)",
            f"    VALUES (gen_random_uuid(), v_owner_id, v_cafe_id, v_region_id,",
            f"            '{_sql_escape(caption)}',",
            f"            'PUBLISHED', false, true, 0, 0, 0,",
            f"            now() - interval '{offset_minutes} minutes')",
            f"    RETURNING id INTO v_blog_id;",
            f"    INSERT INTO blog_images (blog_id, image_url) VALUES",
            f"        (v_blog_id, '{_sql_escape(img1)}'),",
            f"        (v_blog_id, '{_sql_escape(img2)}');",
            "",
        ])

    return "\n".join(lines)


def _emit_sql(blog_urls: Dict[str, Dict[str, List[str]]]) -> str:
    header = f"""-- Seed 100 blog cho 20 cafe Cần Thơ (5 blog/cafe, 2 ảnh/blog = 200 blog_images).
-- Follow rule BE:
--   * author_user_id = cafe.owner_user_id (query runtime)
--   * page_id = cafe.id (blog thuộc cafe page)
--   * region_id = cafe.region_id (BE require không null)
--   * status = PUBLISHED, defaults cho pin/comment/counts
-- Caption tiếng Việt về cà phê, theme rotate theo blog_index (1-5).
-- created_at rải interleaved trong {SPREAD_DAYS} ngày gần đây
-- ({MINUTES_PER_SLOT} phút/slot × 100 blog).
-- Ảnh Cloudinary từ blog_image_urls.json (đã upload trước đó).

BEGIN;

DO $$
DECLARE
    v_cafe_id   uuid;
    v_owner_id  uuid;
    v_region_id uuid;
    v_blog_id   uuid;
BEGIN
"""

    body_parts: List[str] = []
    for cafe_index, cafe_row in enumerate(CANTHO_CAFES, start=1):
        cafe_name = cafe_row[0]
        cafe_urls = blog_urls[str(cafe_index)]
        body_parts.append(_emit_cafe_blogs(cafe_index, cafe_name, cafe_urls))

    footer = """END $$;

COMMIT;
"""
    return header + "\n".join(body_parts) + "\n" + footer


def main() -> None:
    blog_urls = _load_blog_urls()
    sql = _emit_sql(blog_urls)
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(sql, encoding="utf-8")

    total_blogs = len(CANTHO_CAFES) * BLOGS_PER_CAFE
    total_images = total_blogs * 2
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  Total blogs: {total_blogs} ({len(CANTHO_CAFES)} cafe × {BLOGS_PER_CAFE} blog)")
    print(f"  Total blog_images: {total_images}")
    print(f"  Time spread: {SPREAD_DAYS} days, {MINUTES_PER_SLOT} min/slot")
    print(f"  Sample caption: {caption_for(1, CANTHO_CAFES[0][0])[:80]}...")


if __name__ == "__main__":
    main()
