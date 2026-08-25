"""
Sinh SQL UPDATE cho 20 cafe đã seed — thêm description + avatar_url + cover_url.
Match theo cafe_pages.name (unique trong practice).

Không đụng đến payments/page_members/regions — an toàn với FK.

Chạy: python generate_update_cafe_media.py
Output: ./output/2026-07-16_update_cafe_media_cantho.sql
"""

from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Dict

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

from cantho_cafes import CANTHO_CAFES

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_update_cafe_media_cantho.sql"
IMAGE_URLS_PATH = Path(__file__).parent / "cafe_image_urls.json"


def _sql_escape(text: str) -> str:
    return text.replace("'", "''")


def _load_image_urls() -> Dict[str, Dict[str, str]]:
    if not IMAGE_URLS_PATH.is_file():
        raise SystemExit(f"Không tìm thấy {IMAGE_URLS_PATH.name}")
    urls = json.loads(IMAGE_URLS_PATH.read_text(encoding="utf-8"))
    missing = [
        i for i in range(1, 21)
        if str(i) not in urls
        or "avatar" not in urls[str(i)]
        or "cover" not in urls[str(i)]
    ]
    if missing:
        raise SystemExit(f"Thiếu URL cho cafe: {missing}")
    return urls


def main() -> None:
    urls = _load_image_urls()

    lines = [
        "-- UPDATE 20 cafe đã seed: thêm description + avatar_url + cover_url.",
        "-- Match theo cafe_pages.name (unique trong batch này).",
        "-- Preflight check: 20 quán phải tồn tại — nếu thiếu, abort.",
        "-- Idempotent: chạy lại được, giá trị cuối cùng thắng.",
        "",
        "BEGIN;",
        "",
        "DO $$",
        "DECLARE",
        "    v_existing_count integer;",
        "BEGIN",
        "    -- Preflight: 20 cafe name phải có sẵn trong DB",
        "    SELECT count(*) INTO v_existing_count",
        "    FROM cafe_pages",
        "    WHERE name IN (",
    ]
    name_literals = [f"        '{_sql_escape(row[0])}'" for row in CANTHO_CAFES]
    lines.append(",\n".join(name_literals))
    lines.extend([
        "    );",
        "    IF v_existing_count <> 20 THEN",
        "        RAISE EXCEPTION 'Chỉ tìm được %/20 cafe theo tên — abort. Có thể chưa apply seed cafe.', v_existing_count;",
        "    END IF;",
        "END $$;",
        "",
    ])

    # 20 UPDATE statements
    for index, cafe in enumerate(CANTHO_CAFES, start=1):
        name = cafe[0]
        description = cafe[5]
        entry = urls[str(index)]
        avatar_url = entry["avatar"]
        cover_url = entry["cover"]

        lines.extend([
            f"-- Cafe #{index:02d}: {name}",
            "UPDATE cafe_pages SET",
            f"    description = '{_sql_escape(description)}',",
            f"    avatar_url  = '{_sql_escape(avatar_url)}',",
            f"    cover_url   = '{_sql_escape(cover_url)}',",
            "    updated_at  = now()",
            f"WHERE name = '{_sql_escape(name)}';",
            "",
        ])

    lines.append("COMMIT;")

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  UPDATE statements: {len(CANTHO_CAFES)}")
    print(f"  Image URLs: {len(urls)}")


if __name__ == "__main__":
    main()
