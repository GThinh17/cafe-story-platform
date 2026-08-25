"""
Sinh SQL seed 40 reviewer ở Cần Thơ theo flow BE:
  user tạo payment PAID (STRIPE_CARD) → activate reviewer row + assign role REVIEWER.

40 = 20 × gói 6 tháng (99k) + 20 × gói 3 tháng (50k).

User pool: 100 user Cần Thơ, loại 2 admin (vu, thinh) + loại 20 cafe owners
(ORDER BY md5 OFFSET 20 LIMIT 40) → không chồng lấn với batch cafe pages.

Mỗi reviewer sinh 4 INSERT trong DO block:
  1. reviewers   (user_id UNIQUE, reviewer_active=true, reviewer_expires_at=+duration)
  2. payments    (buyer_id=user, extra_fee_id=pkg, cafe_page_id=NULL, STRIPE_CARD, PAID)
  3. payment_details (provider=stripe, stub txn/order id)
  4. user_roles  (REVIEWER role) — ON CONFLICT DO NOTHING (idempotent)

Chạy: python generate_seed_reviewers.py
Output: output/2026-07-16_seed_40_reviewers_cantho.sql
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

OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_40_reviewers_cantho.sql"

TOTAL_REVIEWERS = 40
COUNT_6MO = 20
COUNT_3MO = TOTAL_REVIEWERS - COUNT_6MO
CAFE_OWNER_SKIP = 20  # Skip 20 md5-first users (họ là cafe owners)


def _emit_reviewer_block(index: int, package_key: str) -> str:
    """Sinh SQL block cho 1 reviewer (index 1-based, 1-40)."""
    if package_key == "6mo":
        fee_var = "v_fee_6mo"
        price_var = "v_price_6mo"
        interval_literal = "6 months"
    else:
        fee_var = "v_fee_3mo"
        price_var = "v_price_3mo"
        interval_literal = "3 months"

    txn_id = f"seed_stripe_reviewer_txn_{index:03d}"
    order_id = f"seed_reviewer_order_{index:03d}"

    return f"""    -- Reviewer #{index:02d} ({package_key})
    v_user_id := v_reviewer_user_ids[{index}];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '{interval_literal}', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, {fee_var}, NULL,
            'STRIPE_CARD', {price_var}::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            '{txn_id}', '{order_id}',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;
"""


def _emit_sql() -> str:
    header = f"""-- Seed {TOTAL_REVIEWERS} reviewer ở Cần Thơ theo flow BE:
--   user → payment STRIPE_CARD PAID → activate reviewer row + assign role REVIEWER.
-- Follow PaymentServiceImpl.activateReviewerSubscription: reviewer_active=true,
-- reviewer_expires_at = now + duration_months.
-- Split: {COUNT_6MO} × 'Reviewer 6 tháng' (99k) + {COUNT_3MO} × 'Reviewer 3 tháng' (50k).
-- User pool: 100 user Cần Thơ, ORDER BY md5(user_id::text) OFFSET {CAFE_OWNER_SKIP} LIMIT {TOTAL_REVIEWERS}
-- → skip {CAFE_OWNER_SKIP} md5-first users (đã là cafe owners), lấy {TOTAL_REVIEWERS} tiếp theo.

BEGIN;

DO $$
DECLARE
    v_reviewer_user_ids uuid[];
    v_user_id           uuid;
    v_reviewer_id       uuid;
    v_payment_id        uuid;
    v_fee_6mo           uuid;
    v_fee_3mo           uuid;
    v_price_6mo         bigint;
    v_price_3mo         bigint;
    v_role_reviewer     integer;
    v_existing_count    integer;
BEGIN
    -- 1) Resolve reviewer extra_fee packages
    SELECT extra_fee_id, price INTO v_fee_6mo, v_price_6mo
    FROM extra_fees WHERE name = 'Reviewer 6 tháng';
    SELECT extra_fee_id, price INTO v_fee_3mo, v_price_3mo
    FROM extra_fees WHERE name = 'Reviewer 3 tháng';
    IF v_fee_6mo IS NULL OR v_fee_3mo IS NULL THEN
        RAISE EXCEPTION 'Reviewer packages missing — chạy 2026-07-16_seed_extra_fee_packages.sql trước.';
    END IF;

    -- 2) Ensure REVIEWER role exists (matches PaymentServiceImpl REVIEWER_ROLE constant)
    INSERT INTO roles (name) VALUES ('REVIEWER') ON CONFLICT (name) DO NOTHING;
    SELECT id INTO v_role_reviewer FROM roles WHERE name = 'REVIEWER';

    -- 3) Pick {TOTAL_REVIEWERS} user_ids Cần Thơ, KHÔNG phải admin, skip cafe owners
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_reviewer_user_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '92'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        OFFSET {CAFE_OWNER_SKIP} LIMIT {TOTAL_REVIEWERS}
    ) sub;
    IF v_reviewer_user_ids IS NULL OR array_length(v_reviewer_user_ids, 1) < {TOTAL_REVIEWERS} THEN
        RAISE EXCEPTION 'Chỉ pick được %/{TOTAL_REVIEWERS} user Cần Thơ, cần đủ {TOTAL_REVIEWERS}.',
                        coalesce(array_length(v_reviewer_user_ids, 1), 0);
    END IF;

    -- 4) Preflight: không có user nào trong pool đã là reviewer sẵn
    SELECT count(*) INTO v_existing_count
    FROM reviewers r
    WHERE r.user_id = ANY(v_reviewer_user_ids);
    IF v_existing_count > 0 THEN
        RAISE EXCEPTION '% user trong pool đã có row reviewers — abort để tránh duplicate.', v_existing_count;
    END IF;

"""

    body_parts: List[str] = []
    for index in range(1, TOTAL_REVIEWERS + 1):
        # 20 đầu → 6mo, 20 sau → 3mo
        package_key = "6mo" if index <= COUNT_6MO else "3mo"
        body_parts.append(_emit_reviewer_block(index, package_key))

    footer = """END $$;

COMMIT;
"""
    return header + "\n".join(body_parts) + "\n" + footer


def main() -> None:
    sql = _emit_sql()
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(sql, encoding="utf-8")
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  Total reviewers: {TOTAL_REVIEWERS} ({COUNT_6MO} × 6mo + {COUNT_3MO} × 3mo)")
    print(f"  User pool: Cần Thơ, ORDER BY md5 OFFSET {CAFE_OWNER_SKIP} LIMIT {TOTAL_REVIEWERS}")
    print(f"  Payment method: STRIPE_CARD, status: PAID")


if __name__ == "__main__":
    main()
