"""
Sinh SQL seed 40 blog cá nhân cho 40 user Cần Thơ CHỈ có role USER
(không phải admin, cafe owner, reviewer) — mỗi user 1 blog, mỗi blog 1 ảnh Pexels
thuộc chủ đề quán nước / thức uống / cà phê / quầy bar.

Follow rule BE:
- author_user_id = user_id
- page_id = NULL (blog cá nhân → user profile)
- region_id = user.region_id (BE require không null)
- content NOT NULL — caption tiếng Việt tone khách hàng thường
- imageUrls (blog_images): 1 ảnh Cloudinary/blog
- status = PUBLISHED

User pool: 40 user Cần Thơ (province_code='92') non-admin, KHÔNG có role
CAFE_PAGE và KHÔNG có role REVIEWER — deterministic theo md5(user_id).

created_at rải interleaved 60 ngày qua (2160 min/slot) — feed đa dạng theo thời gian.

Chạy: python generate_seed_user_blogs.py
Output: output/2026-07-16_seed_40_user_blogs_cantho.sql
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

# Import THEMES + caption picker
from download_pexels_user_blog_images import THEMES
from user_blog_captions import caption_for_slot

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_40_user_blogs_cantho.sql"
URLS_PATH = Path(__file__).parent / "user_blog_image_urls.json"

TOTAL_USERS = 40
SPREAD_DAYS = 60
MINUTES_PER_SLOT = (SPREAD_DAYS * 24 * 60) // TOTAL_USERS  # 2160


def _sql_escape(text: str) -> str:
    return text.replace("'", "''")


def _load_urls() -> Dict[str, str]:
    if not URLS_PATH.is_file():
        raise SystemExit(
            f"Không tìm thấy {URLS_PATH.name} — chạy upload_user_blog_images.py trước."
        )
    urls = json.loads(URLS_PATH.read_text(encoding="utf-8"))
    missing = []
    for user_idx in range(1, TOTAL_USERS + 1):
        key = str(user_idx)
        if not urls.get(key):
            missing.append(f"user#{user_idx}")
    if missing:
        raise SystemExit(
            f"Thiếu URLs cho: {missing[:5]}{'...' if len(missing) > 5 else ''}"
        )
    return urls


def _emit_user_block(user_idx: int, image_url: str) -> str:
    slot_idx = user_idx - 1
    offset_min = slot_idx * MINUTES_PER_SLOT
    caption = caption_for_slot(slot_idx, THEMES, seed=f"user{user_idx}")
    theme = THEMES[slot_idx % len(THEMES)]

    lines: List[str] = [
        f"    -- === User #{user_idx:03d} (slot {slot_idx}, theme '{theme}') ===",
        f"    v_user_id := v_user_ids[{user_idx}];",
        f"    SELECT u.region_id INTO v_region_id",
        f"    FROM users u WHERE u.user_id = v_user_id;",
        f"    IF v_region_id IS NULL THEN",
        f"        RAISE EXCEPTION 'User % has NULL region_id — blogs need region.', v_user_id;",
        f"    END IF;",
        f"    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,",
        f"                       is_pinned, allow_comment, like_count, share_count, comment_count,",
        f"                       created_at)",
        f"    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,",
        f"            '{_sql_escape(caption)}',",
        f"            'PUBLISHED', false, true, 0, 0, 0,",
        f"            now() - interval '{offset_min} minutes')",
        f"    RETURNING id INTO v_blog_id;",
        f"    INSERT INTO blog_images (blog_id, image_url) VALUES",
        f"        (v_blog_id, '{_sql_escape(image_url)}');",
        "",
    ]
    return "\n".join(lines)


def _emit_sql(urls: Dict[str, str]) -> str:
    header = f"""-- Seed {TOTAL_USERS} blog cá nhân cho {TOTAL_USERS} user Cần Thơ CHỈ có role USER.
--   Loại: admin, cafe owner (CAFE_PAGE), reviewer (REVIEWER).
--   Mỗi user 1 blog cá nhân (page_id NULL), 1 ảnh Cloudinary/blog.
--   Ảnh Pexels chủ đề quán nước / thức uống / cà phê / quầy bar (folder cafestory/user_blogs).
-- Follow rule BE:
--   * author_user_id = user
--   * page_id = NULL (blog cá nhân → hiện ở user profile, KHÔNG hiện ở cafe page)
--   * region_id = user.region_id (BE require)
--   * content tone khách hàng thường (không chèn tên vì đã hiển thị author)
-- User pool: {TOTAL_USERS} user Cần Thơ (province_code='92') non-admin, không có role
-- CAFE_PAGE và không có role REVIEWER — deterministic ORDER BY md5(user_id) LIMIT {TOTAL_USERS}.
-- created_at rải interleaved {SPREAD_DAYS} ngày qua ({MINUTES_PER_SLOT} min/slot).

BEGIN;

DO $$
DECLARE
    v_user_ids   uuid[];
    v_user_id    uuid;
    v_region_id  uuid;
    v_blog_id    uuid;
BEGIN
    -- Pick {TOTAL_USERS} user Cần Thơ chỉ có role USER (skip admin/CAFE_PAGE/REVIEWER),
    -- deterministic theo md5(user_id)
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_user_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '92'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
          AND NOT EXISTS (
              SELECT 1
              FROM user_roles ur
              JOIN roles ro ON ro.id = ur.role_id
              WHERE ur.user_id = u.user_id
                AND ro.name IN ('ADMIN', 'CAFE_PAGE', 'REVIEWER')
          )
        ORDER BY md5(u.user_id::text)
        LIMIT {TOTAL_USERS}
    ) sub;
    IF v_user_ids IS NULL OR array_length(v_user_ids, 1) < {TOTAL_USERS} THEN
        RAISE EXCEPTION 'Chỉ pick được %/{TOTAL_USERS} user chỉ role USER, cần đủ {TOTAL_USERS}.',
                        coalesce(array_length(v_user_ids, 1), 0);
    END IF;

"""

    body_parts = [
        _emit_user_block(user_idx, urls[str(user_idx)])
        for user_idx in range(1, TOTAL_USERS + 1)
    ]

    footer = """END $$;

COMMIT;
"""
    return header + "\n".join(body_parts) + "\n" + footer


def main() -> None:
    urls = _load_urls()
    sql = _emit_sql(urls)
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(sql, encoding="utf-8")
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  Total blogs: {TOTAL_USERS}")
    print(f"  Total blog_images: {TOTAL_USERS}")
    print(f"  Time spread: {SPREAD_DAYS} days, {MINUTES_PER_SLOT} min/slot")


if __name__ == "__main__":
    main()
