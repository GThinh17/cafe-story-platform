"""
Sinh SQL seed 20 cafe page THẬT ở Đà Nẵng, mô phỏng full BE flow:
  user → payment (STRIPE_CARD, PAID) → cafe page kích hoạt (page_active=true,
  page_expires_at=+duration, status=ACTIVE) → OWNER membership → user_roles.CAFE_PAGE

Data cafe từ danang_cafes.py (đã cross-check web).
Owner được lookup runtime trong SQL DO block, pool user Đà Nẵng CHỈ những người
chưa có row trong bảng reviewers (loại 20 reviewer đã seed).

Ảnh (avatar/cover) tạm để NULL — sẽ update sau ở bước riêng.

Chạy: python generate_seed_cafe_pages_danang.py
Output: output/2026-07-16_seed_20_cafe_pages_danang.sql
"""

from __future__ import annotations

import sys
from pathlib import Path
from typing import List

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

from danang_cafes import (
    DANANG_CAFES,
    CITY_CODE,
    CITY_NAME,
    PROVINCE_CODE,
    PROVINCE_NAME,
)

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_20_cafe_pages_danang.sql"

PACKAGE_MAP = {
    "6mo": ("Cafe Page 6 tháng", 6, "6 months"),
    "3mo": ("Cafe Page 3 tháng", 3, "3 months"),
}


def _sql_escape(text: str) -> str:
    return text.replace("'", "''")


def _emit_cafe_block(index: int, cafe: tuple) -> str:
    """Sinh SQL block cho 1 cafe (index 1-based). avatar/cover = NULL để update sau."""
    name, street, ward_code, ward_name, package_key, description = cafe
    package_name, _months, interval_literal = PACKAGE_MAP[package_key]
    price_var = "v_price_6mo" if package_key == "6mo" else "v_price_3mo"
    fee_var = "v_fee_6mo" if package_key == "6mo" else "v_fee_3mo"

    address_full = f"{street}, {ward_name}, {CITY_NAME}"
    txn_id = f"seed_stripe_dn_cafe_txn_{index:03d}"
    order_id = f"seed_dn_cafe_order_{index:03d}"

    return f"""    -- Cafe Đà Nẵng #{index:02d}: {name} ({package_key})
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
            NULL, NULL,
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
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;
"""


def _emit_sql() -> str:
    name_literals = ",\n            ".join(
        f"'{_sql_escape(cafe[0])}'" for cafe in DANANG_CAFES
    )

    header = f"""-- Seed 20 cafe page THẬT ở Đà Nẵng, mô phỏng full BE flow:
--   user → payment PAID → cafe page active.
-- Data từ danang_cafes.py (cross-check web search 2026-07-18).
-- Owner pool: user Đà Nẵng (province_code='{PROVINCE_CODE}') non-admin,
-- CHƯA phải reviewer (NOT EXISTS trong bảng reviewers).
-- 100 user ĐN − 20 reviewer = 80 available → pick 20 theo md5(user_id).
-- Payment method: STRIPE_CARD, payment_status: PAID.
-- Package: 10 cafe × 'Cafe Page 6 tháng' + 10 cafe × 'Cafe Page 3 tháng'.
-- Kết quả: cafe_pages.status = ACTIVE, page_active = true, page_expires_at = +duration.
-- Ảnh avatar/cover tạm NULL — update sau ở migration riêng.

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
        RAISE EXCEPTION 'Extra fee packages missing — chạy seed_extra_fee_packages.sql trước.';
    END IF;

    -- 2) Ensure CAFE_PAGE role exists (matches PaymentServiceImpl.assignRole)
    INSERT INTO roles (name) VALUES ('CAFE_PAGE') ON CONFLICT (name) DO NOTHING;
    SELECT id INTO v_role_cafepage FROM roles WHERE name = 'CAFE_PAGE';

    -- 3) Pick 20 owner user_ids: Đà Nẵng, non-admin, KHÔNG phải reviewer,
    --    deterministic theo md5(user_id)
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_owner_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '{PROVINCE_CODE}'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
          AND NOT EXISTS (
              SELECT 1 FROM reviewers rv WHERE rv.user_id = u.user_id
          )
        ORDER BY md5(u.user_id::text)
        LIMIT 20
    ) sub;
    IF v_owner_ids IS NULL OR array_length(v_owner_ids, 1) < 20 THEN
        RAISE EXCEPTION 'Chỉ tìm được % owner Đà Nẵng (non-reviewer), cần 20.',
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
        _emit_cafe_block(index, cafe)
        for index, cafe in enumerate(DANANG_CAFES, start=1)
    ]

    footer = """END $$;

COMMIT;
"""
    return header + "\n".join(body_parts) + "\n" + footer


def main() -> None:
    sql = _emit_sql()
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(sql, encoding="utf-8")

    count_6mo = sum(1 for c in DANANG_CAFES if c[4] == "6mo")
    count_3mo = sum(1 for c in DANANG_CAFES if c[4] == "3mo")
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  Cafes: {len(DANANG_CAFES)} ({count_6mo} × 6mo + {count_3mo} × 3mo)")
    print(f"  Unique names: {len({c[0] for c in DANANG_CAFES})}")
    print(f"  Wards used: {sorted({c[3] for c in DANANG_CAFES})}")
    print(f"  Note: avatar/cover = NULL (update sau)")


if __name__ == "__main__":
    main()
