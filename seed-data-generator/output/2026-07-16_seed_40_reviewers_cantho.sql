-- Seed 40 reviewer ở Cần Thơ theo flow BE:
--   user → payment STRIPE_CARD PAID → activate reviewer row + assign role REVIEWER.
-- Follow PaymentServiceImpl.activateReviewerSubscription: reviewer_active=true,
-- reviewer_expires_at = now + duration_months.
-- Split: 20 × 'Reviewer 6 tháng' (99k) + 20 × 'Reviewer 3 tháng' (50k).
-- User pool: 100 user Cần Thơ, ORDER BY md5(user_id::text) OFFSET 20 LIMIT 40
-- → skip 20 md5-first users (đã là cafe owners), lấy 40 tiếp theo.

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

    -- 3) Pick 40 user_ids Cần Thơ, KHÔNG phải admin, skip cafe owners
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_reviewer_user_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '92'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        OFFSET 20 LIMIT 40
    ) sub;
    IF v_reviewer_user_ids IS NULL OR array_length(v_reviewer_user_ids, 1) < 40 THEN
        RAISE EXCEPTION 'Chỉ pick được %/40 user Cần Thơ, cần đủ 40.',
                        coalesce(array_length(v_reviewer_user_ids, 1), 0);
    END IF;

    -- 4) Preflight: không có user nào trong pool đã là reviewer sẵn
    SELECT count(*) INTO v_existing_count
    FROM reviewers r
    WHERE r.user_id = ANY(v_reviewer_user_ids);
    IF v_existing_count > 0 THEN
        RAISE EXCEPTION '% user trong pool đã có row reviewers — abort để tránh duplicate.', v_existing_count;
    END IF;

    -- Reviewer #01 (6mo)
    v_user_id := v_reviewer_user_ids[1];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_001', 'seed_reviewer_order_001',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #02 (6mo)
    v_user_id := v_reviewer_user_ids[2];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_002', 'seed_reviewer_order_002',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #03 (6mo)
    v_user_id := v_reviewer_user_ids[3];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_003', 'seed_reviewer_order_003',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #04 (6mo)
    v_user_id := v_reviewer_user_ids[4];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_004', 'seed_reviewer_order_004',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #05 (6mo)
    v_user_id := v_reviewer_user_ids[5];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_005', 'seed_reviewer_order_005',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #06 (6mo)
    v_user_id := v_reviewer_user_ids[6];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_006', 'seed_reviewer_order_006',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #07 (6mo)
    v_user_id := v_reviewer_user_ids[7];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_007', 'seed_reviewer_order_007',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #08 (6mo)
    v_user_id := v_reviewer_user_ids[8];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_008', 'seed_reviewer_order_008',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #09 (6mo)
    v_user_id := v_reviewer_user_ids[9];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_009', 'seed_reviewer_order_009',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #10 (6mo)
    v_user_id := v_reviewer_user_ids[10];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_010', 'seed_reviewer_order_010',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #11 (6mo)
    v_user_id := v_reviewer_user_ids[11];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_011', 'seed_reviewer_order_011',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #12 (6mo)
    v_user_id := v_reviewer_user_ids[12];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_012', 'seed_reviewer_order_012',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #13 (6mo)
    v_user_id := v_reviewer_user_ids[13];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_013', 'seed_reviewer_order_013',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #14 (6mo)
    v_user_id := v_reviewer_user_ids[14];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_014', 'seed_reviewer_order_014',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #15 (6mo)
    v_user_id := v_reviewer_user_ids[15];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_015', 'seed_reviewer_order_015',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #16 (6mo)
    v_user_id := v_reviewer_user_ids[16];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_016', 'seed_reviewer_order_016',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #17 (6mo)
    v_user_id := v_reviewer_user_ids[17];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_017', 'seed_reviewer_order_017',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #18 (6mo)
    v_user_id := v_reviewer_user_ids[18];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_018', 'seed_reviewer_order_018',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #19 (6mo)
    v_user_id := v_reviewer_user_ids[19];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_019', 'seed_reviewer_order_019',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #20 (6mo)
    v_user_id := v_reviewer_user_ids[20];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '6 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_6mo, NULL,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_020', 'seed_reviewer_order_020',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #21 (3mo)
    v_user_id := v_reviewer_user_ids[21];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_021', 'seed_reviewer_order_021',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #22 (3mo)
    v_user_id := v_reviewer_user_ids[22];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_022', 'seed_reviewer_order_022',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #23 (3mo)
    v_user_id := v_reviewer_user_ids[23];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_023', 'seed_reviewer_order_023',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #24 (3mo)
    v_user_id := v_reviewer_user_ids[24];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_024', 'seed_reviewer_order_024',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #25 (3mo)
    v_user_id := v_reviewer_user_ids[25];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_025', 'seed_reviewer_order_025',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #26 (3mo)
    v_user_id := v_reviewer_user_ids[26];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_026', 'seed_reviewer_order_026',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #27 (3mo)
    v_user_id := v_reviewer_user_ids[27];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_027', 'seed_reviewer_order_027',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #28 (3mo)
    v_user_id := v_reviewer_user_ids[28];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_028', 'seed_reviewer_order_028',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #29 (3mo)
    v_user_id := v_reviewer_user_ids[29];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_029', 'seed_reviewer_order_029',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #30 (3mo)
    v_user_id := v_reviewer_user_ids[30];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_030', 'seed_reviewer_order_030',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #31 (3mo)
    v_user_id := v_reviewer_user_ids[31];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_031', 'seed_reviewer_order_031',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #32 (3mo)
    v_user_id := v_reviewer_user_ids[32];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_032', 'seed_reviewer_order_032',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #33 (3mo)
    v_user_id := v_reviewer_user_ids[33];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_033', 'seed_reviewer_order_033',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #34 (3mo)
    v_user_id := v_reviewer_user_ids[34];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_034', 'seed_reviewer_order_034',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #35 (3mo)
    v_user_id := v_reviewer_user_ids[35];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_035', 'seed_reviewer_order_035',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #36 (3mo)
    v_user_id := v_reviewer_user_ids[36];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_036', 'seed_reviewer_order_036',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #37 (3mo)
    v_user_id := v_reviewer_user_ids[37];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_037', 'seed_reviewer_order_037',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #38 (3mo)
    v_user_id := v_reviewer_user_ids[38];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_038', 'seed_reviewer_order_038',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #39 (3mo)
    v_user_id := v_reviewer_user_ids[39];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_039', 'seed_reviewer_order_039',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer #40 (3mo)
    v_user_id := v_reviewer_user_ids[40];

    INSERT INTO reviewers (reviewer_id, user_id, reviewer_active,
                           reviewer_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, true,
            now() + interval '3 months', now(), now())
    RETURNING reviewer_id INTO v_reviewer_id;

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_user_id, v_fee_3mo, NULL,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_reviewer_txn_040', 'seed_reviewer_order_040',
            'Seeded PAID payment for reviewer registration demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

END $$;

COMMIT;
