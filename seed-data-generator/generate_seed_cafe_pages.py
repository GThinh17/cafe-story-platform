"""
Sinh SQL seed 20 cafe page thật ở Cần Thơ, mô phỏng full BE flow:
  user → payment (STRIPE_CARD, PAID) → cafe page kích hoạt (page_active=true,
  page_expires_at=+duration, status=ACTIVE) → OWNER membership → user_roles.CAFE_PAGE

Data từ cantho_cafes.py. Owner được lookup runtime trong SQL DO block:
  ORDER BY md5(user_id::text) LIMIT 20 → pseudo-random deterministic từ user_id đã seed.

Chạy: python generate_seed_cafe_pages.py
Output: ./output/2026-07-16_seed_20_cafe_pages_cantho.sql
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

from cantho_cafes import (
    CANTHO_CAFES,
    CITY_CODE,
    CITY_NAME,
    PROVINCE_CODE,
    PROVINCE_NAME,
)

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_20_cafe_pages_cantho.sql"
IMAGE_URLS_PATH = Path(__file__).parent / "cafe_image_urls.json"

# Package key → (extra_fees.name, duration months, SQL interval literal)
PACKAGE_MAP = {
    "6mo": ("Cafe Page 6 tháng", 6, "6 months"),
    "3mo": ("Cafe Page 3 tháng", 3, "3 months"),
}


def _sql_escape(text: str) -> str:
    return text.replace("'", "''")


def _emit_cafe_block(index: int, cafe: tuple, image_urls: Dict[str, Dict[str, str]]) -> str:
    """Sinh SQL block cho 1 cafe (index 1-based). Tất cả biến trong DO block."""
    name, street, ward_code, ward_name, package_key, description = cafe
    package_name, _months, interval_literal = PACKAGE_MAP[package_key]
    price_var = "v_price_6mo" if package_key == "6mo" else "v_price_3mo"
    fee_var = "v_fee_6mo" if package_key == "6mo" else "v_fee_3mo"

    entry = image_urls.get(str(index), {})
    avatar_url = entry.get("avatar", "")
    cover_url = entry.get("cover", "")
    if not avatar_url or not cover_url:
        raise SystemExit(
            f"Cafe #{index} thiếu avatar hoặc cover URL trong {IMAGE_URLS_PATH.name}"
        )

    # Address = street + ward + city
    address_full = f"{street}, {ward_name}, {CITY_NAME}"
    txn_id = f"seed_stripe_txn_{index:03d}"
    order_id = f"seed_order_{index:03d}"

    return f"""    -- Cafe #{index:02d}: {name} ({package_key})
    v_owner_id := v_owner_ids[{index}];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '{PROVINCE_CODE}', '{CITY_CODE}', '{ward_code}',
            '{_sql_escape(PROVINCE_NAME)}', '{_sql_escape(CITY_NAME)}', '{_sql_escape(ward_name)}',
            '{_sql_escape(street)}', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            '{_sql_escape(name)}',
            '{_sql_escape(address_full)}',
            '{_sql_escape(description)}',
            '{_sql_escape(avatar_url)}',
            '{_sql_escape(cover_url)}',
            'ACTIVE', 0, 0, 2, true, now() + interval '{interval_literal}',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, {fee_var}, v_cafe_id,
            'STRIPE_CARD', {price_var}::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            '{txn_id}', '{order_id}',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;
"""


def _load_image_urls() -> Dict[str, Dict[str, str]]:
    if not IMAGE_URLS_PATH.is_file():
        raise SystemExit(
            f"Không tìm thấy {IMAGE_URLS_PATH.name}. Chạy upload_cafe_images.py trước."
        )
    urls = json.loads(IMAGE_URLS_PATH.read_text(encoding="utf-8"))
    missing = [
        i for i in range(1, 21)
        if str(i) not in urls
        or "avatar" not in urls[str(i)]
        or "cover" not in urls[str(i)]
    ]
    if missing:
        raise SystemExit(f"Thiếu avatar/cover URL cho cafe: {missing}")
    return urls


def _emit_sql(image_urls: Dict[str, Dict[str, str]]) -> str:
    # Preflight: check trùng tên (list literals for the IF EXISTS clause)
    name_literals = ",\n            ".join(
        f"'{_sql_escape(cafe[0])}'" for cafe in CANTHO_CAFES
    )

    header = f"""-- Seed 20 real Cần Thơ cafe pages, mô phỏng full BE flow:
--   user → payment PAID → cafe page active.
-- Data từ web search 2026-07-16 (Phúc Long, Highlands, Nhà Phạm, Đậu Ơi, ...).
-- Owner = 20 random users Cần Thơ (ORDER BY md5(user_id::text) — stable pseudo-random).
-- Payment method: STRIPE_CARD, payment_status: PAID.
-- Package: 10 cafe × 'Cafe Page 6 tháng' + 10 cafe × 'Cafe Page 3 tháng'.
-- Kết quả: cafe_pages.status = ACTIVE, page_active = true, page_expires_at = +duration.

BEGIN;

DO $$
DECLARE
    v_owner_ids     uuid[];
    v_owner_id      uuid;
    v_region_id     uuid;
    v_cafe_id       uuid;
    v_payment_id    uuid;
    v_fee_6mo       uuid;
    v_fee_3mo       uuid;
    v_price_6mo     bigint;
    v_price_3mo     bigint;
    v_role_cafepage integer;
BEGIN
    -- 1) Resolve extra_fee packages
    SELECT extra_fee_id, price INTO v_fee_6mo, v_price_6mo
    FROM extra_fees WHERE name = 'Cafe Page 6 tháng';
    SELECT extra_fee_id, price INTO v_fee_3mo, v_price_3mo
    FROM extra_fees WHERE name = 'Cafe Page 3 tháng';
    IF v_fee_6mo IS NULL OR v_fee_3mo IS NULL THEN
        RAISE EXCEPTION 'Extra fee packages missing — chạy 2026-07-16_seed_extra_fee_packages.sql trước.';
    END IF;

    -- 2) Ensure CAFE_PAGE role exists (matches PaymentServiceImpl.assignRole)
    INSERT INTO roles (name) VALUES ('CAFE_PAGE') ON CONFLICT (name) DO NOTHING;
    SELECT id INTO v_role_cafepage FROM roles WHERE name = 'CAFE_PAGE';

    -- 3) Pick 20 owner user_ids: Cần Thơ users, excluding admin, deterministic order
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_owner_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '{PROVINCE_CODE}'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        LIMIT 20
    ) sub;
    IF v_owner_ids IS NULL OR array_length(v_owner_ids, 1) < 20 THEN
        RAISE EXCEPTION 'Chỉ tìm được % owner Cần Thơ, cần 20.',
                        coalesce(array_length(v_owner_ids, 1), 0);
    END IF;

    -- 4) Abort if any cafe name already exists (idempotent guard)
    IF EXISTS (
        SELECT 1 FROM cafe_pages WHERE name IN (
            {name_literals}
        )
    ) THEN
        RAISE EXCEPTION 'Một trong 20 cafe name đã tồn tại — abort để tránh duplicate.';
    END IF;

"""

    body_parts: List[str] = [
        _emit_cafe_block(index, cafe, image_urls)
        for index, cafe in enumerate(CANTHO_CAFES, start=1)
    ]

    footer = """END $$;

COMMIT;
"""
    return header + "\n".join(body_parts) + "\n" + footer


def main() -> None:
    image_urls = _load_image_urls()
    sql = _emit_sql(image_urls)
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(sql, encoding="utf-8")

    count_6mo = sum(1 for c in CANTHO_CAFES if c[4] == "6mo")
    count_3mo = sum(1 for c in CANTHO_CAFES if c[4] == "3mo")
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  Cafes: {len(CANTHO_CAFES)} ({count_6mo} × 6mo + {count_3mo} × 3mo)")
    print(f"  Unique names: {len({c[0] for c in CANTHO_CAFES})}")
    print(f"  Wards used: {sorted({c[3] for c in CANTHO_CAFES})}")
    print(f"  Image URLs loaded: {len(image_urls)}")
    print(f"  Sample: {CANTHO_CAFES[0][0]} @ {CANTHO_CAFES[0][1]}")


if __name__ == "__main__":
    main()
