-- Seed 20 reviewer ở Đà Nẵng theo flow BE:
--   user → payment STRIPE_CARD PAID → activate reviewer row + assign role REVIEWER.
-- Follow PaymentServiceImpl.activateReviewerSubscription: reviewer_active=true,
-- reviewer_expires_at = max(current, now) + duration_months.
-- Split: 10 × 'Reviewer 6 tháng' (99k) + 10 × 'Reviewer 3 tháng' (50k).
-- User pool: 100 user Đà Nẵng (province_code='48'),
-- ORDER BY md5(user_id::text) LIMIT 20.
-- Đà Nẵng chưa có cafe owner nên KHÔNG cần OFFSET skip.

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

    -- 3) Pick 20 user_ids Đà Nẵng, KHÔNG phải admin
    SELECT array_agg(user_id ORDER BY md5(user_id::text)) INTO v_reviewer_user_ids
    FROM (
        SELECT u.user_id
        FROM users u
        JOIN regions r ON r.region_id = u.region_id
        WHERE r.province_code = '48'
          AND u.user_email NOT IN ('vu@gmail.com', 'thinh@gmail.com')
        ORDER BY md5(u.user_id::text)
        LIMIT 20
    ) sub;
    IF v_reviewer_user_ids IS NULL OR array_length(v_reviewer_user_ids, 1) < 20 THEN
        RAISE EXCEPTION 'Chỉ pick được %/20 user Đà Nẵng, cần đủ 20.',
                        coalesce(array_length(v_reviewer_user_ids, 1), 0);
    END IF;

    -- 4) Preflight: không có user nào trong pool đã là reviewer sẵn
    SELECT count(*) INTO v_existing_count
    FROM reviewers r
    WHERE r.user_id = ANY(v_reviewer_user_ids);
    IF v_existing_count > 0 THEN
        RAISE EXCEPTION '% user trong pool đã có row reviewers — abort để tránh duplicate.', v_existing_count;
    END IF;

    -- Reviewer Đà Nẵng #01 (6mo)
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
            'seed_stripe_reviewer_dn_txn_001', 'seed_reviewer_dn_order_001',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #02 (6mo)
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
            'seed_stripe_reviewer_dn_txn_002', 'seed_reviewer_dn_order_002',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #03 (6mo)
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
            'seed_stripe_reviewer_dn_txn_003', 'seed_reviewer_dn_order_003',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #04 (6mo)
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
            'seed_stripe_reviewer_dn_txn_004', 'seed_reviewer_dn_order_004',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #05 (6mo)
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
            'seed_stripe_reviewer_dn_txn_005', 'seed_reviewer_dn_order_005',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #06 (6mo)
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
            'seed_stripe_reviewer_dn_txn_006', 'seed_reviewer_dn_order_006',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #07 (6mo)
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
            'seed_stripe_reviewer_dn_txn_007', 'seed_reviewer_dn_order_007',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #08 (6mo)
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
            'seed_stripe_reviewer_dn_txn_008', 'seed_reviewer_dn_order_008',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #09 (6mo)
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
            'seed_stripe_reviewer_dn_txn_009', 'seed_reviewer_dn_order_009',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #10 (6mo)
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
            'seed_stripe_reviewer_dn_txn_010', 'seed_reviewer_dn_order_010',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #11 (3mo)
    v_user_id := v_reviewer_user_ids[11];

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
            'seed_stripe_reviewer_dn_txn_011', 'seed_reviewer_dn_order_011',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #12 (3mo)
    v_user_id := v_reviewer_user_ids[12];

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
            'seed_stripe_reviewer_dn_txn_012', 'seed_reviewer_dn_order_012',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #13 (3mo)
    v_user_id := v_reviewer_user_ids[13];

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
            'seed_stripe_reviewer_dn_txn_013', 'seed_reviewer_dn_order_013',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #14 (3mo)
    v_user_id := v_reviewer_user_ids[14];

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
            'seed_stripe_reviewer_dn_txn_014', 'seed_reviewer_dn_order_014',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #15 (3mo)
    v_user_id := v_reviewer_user_ids[15];

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
            'seed_stripe_reviewer_dn_txn_015', 'seed_reviewer_dn_order_015',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #16 (3mo)
    v_user_id := v_reviewer_user_ids[16];

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
            'seed_stripe_reviewer_dn_txn_016', 'seed_reviewer_dn_order_016',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #17 (3mo)
    v_user_id := v_reviewer_user_ids[17];

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
            'seed_stripe_reviewer_dn_txn_017', 'seed_reviewer_dn_order_017',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #18 (3mo)
    v_user_id := v_reviewer_user_ids[18];

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
            'seed_stripe_reviewer_dn_txn_018', 'seed_reviewer_dn_order_018',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #19 (3mo)
    v_user_id := v_reviewer_user_ids[19];

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
            'seed_stripe_reviewer_dn_txn_019', 'seed_reviewer_dn_order_019',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Reviewer Đà Nẵng #20 (3mo)
    v_user_id := v_reviewer_user_ids[20];

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
            'seed_stripe_reviewer_dn_txn_020', 'seed_reviewer_dn_order_020',
            'Seeded PAID payment for reviewer registration demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_reviewer, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

END $$;

COMMIT;
