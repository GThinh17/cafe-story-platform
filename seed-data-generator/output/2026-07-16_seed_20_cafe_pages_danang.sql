-- Seed 20 cafe page THẬT ở Đà Nẵng, mô phỏng full BE flow:
--   user → payment PAID → cafe page active.
-- Data từ danang_cafes.py (cross-check web search 2026-07-18).
-- Owner pool: user Đà Nẵng (province_code='48') non-admin,
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
        WHERE r.province_code = '48'
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
            'Highlands Coffee Vincom Đà Nẵng',
            'Phúc Long Đà Nẵng',
            'Trung Nguyên Legend Đà Nẵng',
            'Cộng Cà Phê Đà Nẵng',
            'Katinat Đà Nẵng',
            'Starbucks Đà Nẵng',
            'The Coffee House Đà Nẵng',
            'Phê La Đà Nẵng',
            '43 Factory Coffee Roaster',
            'XLIII Coffee Đà Nẵng',
            'The Local Beans Đà Nẵng',
            'Wonderlust Coffee & Souvenir',
            'Boulevard Gelato & Coffee',
            'Nối Cafe',
            'Gé Cafe',
            'Faifo Coffee Hội An',
            'Nối - The Cabin',
            'Sơn Trà Marina',
            'OM Herbal Tea & Coffee',
            'Mirko Coffee & Chill Beer'
        )
    ) THEN
        RAISE EXCEPTION 'Một trong 20 cafe name đã tồn tại — abort để tránh duplicate.';
    END IF;

    -- Cafe Đà Nẵng #01: Highlands Coffee Vincom Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[1];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20275',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Hải',
            'Tầng Trệt, Vincom Center, 910A Ngô Quyền', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Highlands Coffee Vincom Đà Nẵng',
            'Tầng Trệt, Vincom Center, 910A Ngô Quyền, Phường An Hải, Đà Nẵng',
            'Chi nhánh Highlands tại tầng trệt Vincom Center Ngô Quyền, không gian rộng máy lạnh, tiện cho khách mua sắm ghé nghỉ chân.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_001', 'seed_dn_cafe_order_001',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #02: Phúc Long Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[2];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '61 Nguyễn Văn Linh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Phúc Long Đà Nẵng',
            '61 Nguyễn Văn Linh, Phường Hải Châu, Đà Nẵng',
            'Phúc Long chi nhánh Đà Nẵng — trục đường sầm uất Nguyễn Văn Linh, không gian 2 tầng, trà và cà phê Phúc Long đặc trưng.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_002', 'seed_dn_cafe_order_002',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #03: Trung Nguyên Legend Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[3];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '102-104 Đống Đa', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Trung Nguyên Legend Đà Nẵng',
            '102-104 Đống Đa, Phường Hải Châu, Đà Nẵng',
            'Trung Nguyên Legend giữa lòng Hải Châu, không gian sang trọng đậm bản sắc cà phê Buôn Ma Thuột, khách doanh nhân nhiều.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_003', 'seed_dn_cafe_order_003',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #04: Cộng Cà Phê Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[4];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '96-98 Bạch Đằng', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Cộng Cà Phê Đà Nẵng',
            '96-98 Bạch Đằng, Phường Hải Châu, Đà Nẵng',
            'Cộng Cà Phê phong cách bao cấp retro, view thẳng sông Hàn và cầu Rồng, signature cốt dừa Cộng bán chạy nhất chi nhánh.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_004', 'seed_dn_cafe_order_004',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #05: Katinat Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[5];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '34 Bạch Đằng', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Katinat Đà Nẵng',
            '34 Bạch Đằng, Phường Hải Châu, Đà Nẵng',
            'Katinat mặt tiền Bạch Đằng, decor xanh-vàng bắt mắt, giới trẻ Đà Nẵng ưa thích check-in ngay từ khi khai trương.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_005', 'seed_dn_cafe_order_005',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #06: Starbucks Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[6];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '50 Bạch Đằng', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Starbucks Đà Nẵng',
            '50 Bạch Đằng, Phường Hải Châu, Đà Nẵng',
            'Cửa hàng Starbucks đầu tiên tại Đà Nẵng, mặt tiền sông Hàn, không gian 2 tầng view trực diện cầu Rồng.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_006', 'seed_dn_cafe_order_006',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #07: The Coffee House Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[7];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            'Lô A2, Nguyễn Văn Linh, Bình Hiên', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'The Coffee House Đà Nẵng',
            'Lô A2, Nguyễn Văn Linh, Bình Hiên, Phường Hải Châu, Đà Nẵng',
            'The Coffee House chi nhánh Đà Nẵng gần cầu Rồng, không gian 2 tầng rộng, wifi mạnh — dân freelancer thường ghé.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_007', 'seed_dn_cafe_order_007',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #08: Phê La Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[8];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '36 Bạch Đằng, Thạch Thang', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Phê La Đà Nẵng',
            '36 Bạch Đằng, Thạch Thang, Phường Hải Châu, Đà Nẵng',
            'Phê La — thương hiệu trà ô long sương từ Đà Lạt, chi nhánh Đà Nẵng view sông Hàn, decor gỗ trầm ấm.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_008', 'seed_dn_cafe_order_008',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #09: 43 Factory Coffee Roaster (6mo)
    v_owner_id := v_owner_ids[9];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            '422 Ngô Thì Sỹ, Mỹ An', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            '43 Factory Coffee Roaster',
            '422 Ngô Thì Sỹ, Mỹ An, Phường Ngũ Hành Sơn, Đà Nẵng',
            'Specialty coffee roaster nổi tiếng nhất Đà Nẵng, không gian công nghiệp minimalism, hạt rang mộc từ Ethiopia đến Việt Nam.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_009', 'seed_dn_cafe_order_009',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #10: XLIII Coffee Đà Nẵng (6mo)
    v_owner_id := v_owner_ids[10];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '258 Bạch Đằng, Phước Ninh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'XLIII Coffee Đà Nẵng',
            '258 Bạch Đằng, Phước Ninh, Phường Hải Châu, Đà Nẵng',
            'XLIII (Bốn Ba) Coffee chi nhánh Đà Nẵng — không gian đô thị hiện đại, menu cà phê specialty từ hạt rang tại 43 Factory.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '6 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_6mo, v_cafe_id,
            'STRIPE_CARD', v_price_6mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_010', 'seed_dn_cafe_order_010',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #11: The Local Beans Đà Nẵng (3mo)
    v_owner_id := v_owner_ids[11];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '186 Phan Châu Trinh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'The Local Beans Đà Nẵng',
            '186 Phan Châu Trinh, Phường Hải Châu, Đà Nẵng',
            'The Local Beans — quán specialty rang xay tại chỗ, không gian 2 tầng minimalism với gỗ và cây xanh, chill cho dân yêu cà phê thuần vị.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_011', 'seed_dn_cafe_order_011',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #12: Wonderlust Coffee & Souvenir (3mo)
    v_owner_id := v_owner_ids[12];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '96 Trần Phú', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Wonderlust Coffee & Souvenir',
            '96 Trần Phú, Phường Hải Châu, Đà Nẵng',
            'Wonderlust decor minimalism đen-trắng kết hợp cây xanh, ánh sáng tự nhiên tràn ngập, phục vụ cả cà phê và đồ lưu niệm.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_012', 'seed_dn_cafe_order_012',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #13: Boulevard Gelato & Coffee (3mo)
    v_owner_id := v_owner_ids[13];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '77 Trần Quốc Toản, Phước Ninh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Boulevard Gelato & Coffee',
            '77 Trần Quốc Toản, Phước Ninh, Phường Hải Châu, Đà Nẵng',
            'Boulevard phong cách châu Âu hiện đại, chuyên gelato Ý kết hợp cà phê, gần biển và cầu Rồng, khách du lịch nhiều.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_013', 'seed_dn_cafe_order_013',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #14: Nối Cafe (3mo)
    v_owner_id := v_owner_ids[14];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '113/18 Nguyễn Chí Thanh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Nối Cafe',
            '113/18 Nguyễn Chí Thanh, Phường Hải Châu, Đà Nẵng',
            'Nối Cafe — chuỗi cafe local Đà Nẵng, 3 cơ sở liền nhau trong hẻm Nguyễn Chí Thanh, decor vintage ấm cúng.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_014', 'seed_dn_cafe_order_014',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #15: Gé Cafe (3mo)
    v_owner_id := v_owner_ids[15];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            '60 Nguyễn Chí Thanh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Gé Cafe',
            '60 Nguyễn Chí Thanh, Phường Hải Châu, Đà Nẵng',
            'Gé Cafe — quán local Đà Nẵng khu Nguyễn Chí Thanh, không gian nhỏ xinh giá bình dân, khách sinh viên và dân văn phòng.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_015', 'seed_dn_cafe_order_015',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #16: Faifo Coffee Hội An (3mo)
    v_owner_id := v_owner_ids[16];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20410',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An',
            '130 Trần Phú, phố cổ Hội An', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Faifo Coffee Hội An',
            '130 Trần Phú, phố cổ Hội An, Phường Hội An, Đà Nẵng',
            'Faifo Coffee giữa phố cổ Hội An, rooftop 3 tầng ngắm toàn mái ngói phố cổ, món egg coffee và cốt dừa nổi tiếng.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_016', 'seed_dn_cafe_order_016',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #17: Nối - The Cabin (3mo)
    v_owner_id := v_owner_ids[17];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20263',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Sơn Trà',
            '118 Chu Huy Mân, Nại Hiên Đông', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Nối - The Cabin',
            '118 Chu Huy Mân, Nại Hiên Đông, Phường Sơn Trà, Đà Nẵng',
            'Nối - The Cabin view cảng cá Thọ Quang, style vintage với vật liệu tái chế từ thuyền cũ, không gian hoài niệm.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_017', 'seed_dn_cafe_order_017',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #18: Sơn Trà Marina (3mo)
    v_owner_id := v_owner_ids[18];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20263',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Sơn Trà',
            'Hồ Xanh, Thọ Quang', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Sơn Trà Marina',
            'Hồ Xanh, Thọ Quang, Phường Sơn Trà, Đà Nẵng',
            'Sơn Trà Marina được mệnh danh ''Santorini thu nhỏ'' với kiến trúc Địa Trung Hải xanh-trắng bên hồ Xanh, chân bán đảo Sơn Trà.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_018', 'seed_dn_cafe_order_018',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #19: OM Herbal Tea & Coffee (3mo)
    v_owner_id := v_owner_ids[19];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20263',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Sơn Trà',
            '116 Thích Thiện Chiếu', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'OM Herbal Tea & Coffee',
            '116 Thích Thiện Chiếu, Phường Sơn Trà, Đà Nẵng',
            'OM Herbal Tea & Coffee — quán trà thảo mộc và cà phê phong cách zen, decor gỗ và cây xanh, hợp cho ai muốn detox và tĩnh lặng.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_019', 'seed_dn_cafe_order_019',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe Đà Nẵng #20: Mirko Coffee & Chill Beer (3mo)
    v_owner_id := v_owner_ids[20];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20263',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Sơn Trà',
            '100 Nguyễn Hữu An, Nại Hiên Đông', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Mirko Coffee & Chill Beer',
            '100 Nguyễn Hữu An, Nại Hiên Đông, Phường Sơn Trà, Đà Nẵng',
            'Mirko Coffee & Chill Beer kết hợp cafe ban ngày và craft beer buổi tối, không gian outdoor gần biển Sơn Trà.',
            NULL, NULL,
            'ACTIVE', 0, 0, 2, true, now() + interval '3 months',
            now(), now())
    RETURNING id INTO v_cafe_id;

    INSERT INTO page_members (page_id, user_id, role_name, status, created_at)
    VALUES (v_cafe_id, v_owner_id, 'OWNER', 'ACTIVE', now());

    INSERT INTO payments (payment_id, buyer_id, extra_fee_id, cafe_page_id,
                          payment_method, amount, currency, payment_status,
                          paid_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_fee_3mo, v_cafe_id,
            'STRIPE_CARD', v_price_3mo::numeric, 'VND', 'PAID',
            now(), now(), now())
    RETURNING payment_id INTO v_payment_id;

    INSERT INTO payment_details (payment_detail_id, payment_id, provider_name,
                                 provider_transaction_id, provider_order_id,
                                 note, created_at, updated_at)
    VALUES (gen_random_uuid(), v_payment_id, 'stripe',
            'seed_stripe_dn_cafe_txn_020', 'seed_dn_cafe_order_020',
            'Seeded PAID payment for cafe page demo (Da Nang)', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

END $$;

COMMIT;
