"""
Sinh SQL seed 50 blog cá nhân của 20 reviewer Đà Nẵng (10 rev × 2 blog + 10 rev × 3 blog).

Follow rule BE:
- author_user_id = reviewer user_id
- page_id = NULL (blog cá nhân)
- region_id = user.region_id (BE require không null)
- content NOT NULL — caption tiếng Việt góc nhìn reviewer, inject reviewer full name
- imageUrls (blog_images): 2 ảnh Cloudinary/blog
- status = PUBLISHED

Reviewer pool: 20 reviewer Đà Nẵng — dùng lại logic seed_20_reviewers_danang
(province_code='48', ORDER BY md5 LIMIT 20).

created_at rải interleaved 60 ngày qua — feed đa dạng theo thời gian.

Chạy: python generate_seed_reviewer_blogs_danang.py
Output: output/2026-07-16_seed_50_reviewer_blogs_danang.sql
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

from reviewer_blog_captions import caption_template_for
from danang_wards import PROVINCE_CODE  # '48'

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_50_reviewer_blogs_danang.sql"
URLS_PATH = Path(__file__).parent / "danang_reviewer_blog_image_urls.json"

TOTAL_REVIEWERS = 20
COUNT_2_BLOG = 10
COUNT_3_BLOG = 10
TOTAL_BLOGS = COUNT_2_BLOG * 2 + COUNT_3_BLOG * 3  # 50

SPREAD_DAYS = 60
MINUTES_PER_SLOT = (SPREAD_DAYS * 24 * 60) // TOTAL_BLOGS  # 1728


def _sql_escape(text: str) -> str:
    return text.replace("'", "''")


def _blogs_for(rev_index: int) -> int:
    return 2 if rev_index <= COUNT_2_BLOG else 3


def _load_urls() -> Dict[str, Dict[str, List[str]]]:
    if not URLS_PATH.is_file():
        raise SystemExit(f"Không tìm thấy {URLS_PATH.name} — chạy upload_danang_reviewer_blog_images.py trước.")
    urls = json.loads(URLS_PATH.read_text(encoding="utf-8"))
    missing = []
    for rev_idx in range(1, TOTAL_REVIEWERS + 1):
        rev_key = str(rev_idx)
        if rev_key not in urls:
            missing.append(f"rev#{rev_idx}")
            continue
        n_blogs = _blogs_for(rev_idx)
        for blog_idx in range(1, n_blogs + 1):
            blog_key = str(blog_idx)
            if blog_key not in urls[rev_key]:
                missing.append(f"rev#{rev_idx} blog#{blog_idx}")
                continue
            imgs = urls[rev_key][blog_key]
            if len(imgs) != 2 or not all(imgs):
                missing.append(f"rev#{rev_idx} blog#{blog_idx} (imgs={len(imgs)})")
    if missing:
        raise SystemExit(f"Thiếu URLs cho: {missing[:5]}{'...' if len(missing) > 5 else ''}")
    return urls


def _emit_reviewer_block(rev_index: int, urls: Dict[str, List[str]], slot_base_ref: List[int]) -> str:
    n_blogs = _blogs_for(rev_index)
    lines: List[str] = [
        f"    -- === Reviewer Đà Nẵng #{rev_index:02d} ({n_blogs} blog) ===",
        f"    v_user_id := v_reviewer_ids[{rev_index}];",
        f"    SELECT u.region_id INTO v_region_id",
        f"    FROM users u WHERE u.user_id = v_user_id;",
        f"    IF v_region_id IS NULL THEN",
        f"        RAISE EXCEPTION 'Reviewer % has NULL region_id — blogs need region.', v_user_id;",
        f"    END IF;",
        "",
    ]

    for blog_idx in range(1, n_blogs + 1):
        blog_slot = slot_base_ref[0]
        offset_min = blog_slot * MINUTES_PER_SLOT
        img1, img2 = urls[str(blog_idx)]

        caption_template = caption_template_for(
            blog_idx, f"dn_rev{rev_index}_blog{blog_idx}"
        )
        left, right = caption_template.split("{name}")

        lines.append(
            f"    -- Blog #{blog_idx} (slot {blog_slot}, {offset_min} min ago)"
        )
        if blog_idx == 1:
            lines.append(
                f"    SELECT user_full_name INTO v_user_full_name FROM users WHERE user_id = v_user_id;"
            )
        content_expr = f"'{_sql_escape(left)}' || v_user_full_name || '{_sql_escape(right)}'"

        lines.extend([
            f"    INSERT INTO blogs (id, author_user_id, page_id, region_id, content, status,",
            f"                       is_pinned, allow_comment, like_count, share_count, comment_count,",
            f"                       created_at)",
            f"    VALUES (gen_random_uuid(), v_user_id, NULL, v_region_id,",
            f"            {content_expr},",
            f"            'PUBLISHED', false, true, 0, 0, 0,",
            f"            now() - interval '{offset_min} minutes')",
            f"    RETURNING id INTO v_blog_id;",
            f"    INSERT INTO blog_images (blog_id, image_url) VALUES",
            f"        (v_blog_id, '{_sql_escape(img1)}'),",
            f"        (v_blog_id, '{_sql_escape(img2)}');",
            "",
        ])
        slot_base_ref[0] += 1

    return "\n".join(lines)


def _emit_sql(urls: Dict[str, Dict[str, List[str]]]) -> str:
    header = f"""-- Seed {TOTAL_BLOGS} blog cá nhân cho {TOTAL_REVIEWERS} reviewer Đà Nẵng.
--   {COUNT_2_BLOG} reviewer × 2 blog + {COUNT_3_BLOG} reviewer × 3 blog = {TOTAL_BLOGS} blog.
--   100 blog_images (2 ảnh/blog) Pexels themes barista / quán nước / thức uống tại quán,
--   distinct photo_id trong batch (dedup khi download).
-- Follow rule BE:
--   * author_user_id = reviewer user
--   * page_id = NULL (blog cá nhân → hiện ở user profile)
--   * region_id = user.region_id (BE require)
--   * content dùng tên thật reviewer (query user_full_name runtime)
-- Reviewer pool: cùng logic seed_20_reviewers_danang — province_code='{PROVINCE_CODE}',
-- ORDER BY md5(user_id) LIMIT {TOTAL_REVIEWERS}.
-- created_at rải interleaved {SPREAD_DAYS} ngày qua ({MINUTES_PER_SLOT} min/slot).

BEGIN;

DO $$
DECLARE
    v_reviewer_ids     uuid[];
    v_user_id          uuid;
    v_region_id        uuid;
    v_blog_id          uuid;
    v_user_full_name   text;
BEGIN
    -- Pick {TOTAL_REVIEWERS} reviewer user_ids Đà Nẵng — cùng thứ tự với seed reviewer step
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_reviewer_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '{PROVINCE_CODE}'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        LIMIT {TOTAL_REVIEWERS}
    ) sub;
    IF v_reviewer_ids IS NULL OR array_length(v_reviewer_ids, 1) < {TOTAL_REVIEWERS} THEN
        RAISE EXCEPTION 'Chỉ pick được %/{TOTAL_REVIEWERS} reviewer Đà Nẵng, cần đủ {TOTAL_REVIEWERS}.',
                        coalesce(array_length(v_reviewer_ids, 1), 0);
    END IF;

"""

    slot_ref = [0]
    body_parts = [
        _emit_reviewer_block(rev_idx, urls[str(rev_idx)], slot_ref)
        for rev_idx in range(1, TOTAL_REVIEWERS + 1)
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
    print(f"  Total blogs: {TOTAL_BLOGS} ({COUNT_2_BLOG}×2 + {COUNT_3_BLOG}×3)")
    print(f"  Total blog_images: {TOTAL_BLOGS * 2}")
    print(f"  Time spread: {SPREAD_DAYS} days, {MINUTES_PER_SLOT} min/slot")


if __name__ == "__main__":
    main()
