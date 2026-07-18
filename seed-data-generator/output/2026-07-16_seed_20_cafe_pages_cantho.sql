-- Seed 20 real Cần Thơ cafe pages, mô phỏng full BE flow:
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
        WHERE r.province_code = '92'
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
            'Phúc Long Coffee & Tea',
            'Highlands Coffee Vincom Cần Thơ',
            'Trung Nguyên Legend Cà Phê',
            'Katinat Saigon Kafe',
            'The 80''s iCafe',
            'Đậu Ơi Coffee & Tea',
            'Trầm Coffee & Tea',
            'Cà phê Nhà Phạm',
            'Nhà Phạm trong rừng',
            'Nhà Phạm bên cầu',
            'Là Cafe',
            'Raw Coffee',
            'Tiệm Trà Cỏ Ngọt',
            'Time Cafe',
            'Sky Bar Iris',
            'Aurora Coffee',
            'HiHi Onigiri',
            'Mật Ngọt Coffee',
            'Chịn Cà Phê',
            'Tiệm Cà Phê Nhà Có Khách'
        )
    ) THEN
        RAISE EXCEPTION 'Một trong 20 cafe name đã tồn tại — abort để tránh duplicate.';
    END IF;

    -- Cafe #01: Phúc Long Coffee & Tea (6mo)
    v_owner_id := v_owner_ids[1];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '209 Đường 30 Tháng 4', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Phúc Long Coffee & Tea',
            '209 Đường 30 Tháng 4, Phường Ninh Kiều, Cần Thơ',
            'Chuỗi trà và cà phê Việt Nam nổi tiếng, kết hợp hương vị trà truyền thống với cà phê và các món bánh ngọt tinh tế.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291018/cafestory/cafes/mmubymqyja7opniy6wzb.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291057/cafestory/cafes/rn4uhwqzqqw3tl2v3jpz.jpg',
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
            'seed_stripe_txn_001', 'seed_order_001',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #02: Highlands Coffee Vincom Cần Thơ (6mo)
    v_owner_id := v_owner_ids[2];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            'Vincom Plaza, 2 Hùng Vương', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Highlands Coffee Vincom Cần Thơ',
            'Vincom Plaza, 2 Hùng Vương, Phường Ninh Kiều, Cần Thơ',
            'Không gian hiện đại tại Vincom Plaza Cần Thơ, mặt bằng rộng cả indoor và outdoor, phù hợp gặp gỡ bạn bè và làm việc.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291020/cafestory/cafes/sekxebzyycbv0taqdsx0.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291060/cafestory/cafes/awfmxcgqhqkildnrtbfv.jpg',
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
            'seed_stripe_txn_002', 'seed_order_002',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #03: Trung Nguyên Legend Cà Phê (6mo)
    v_owner_id := v_owner_ids[3];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '259 Đường 30 Tháng 4', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Trung Nguyên Legend Cà Phê',
            '259 Đường 30 Tháng 4, Phường Ninh Kiều, Cần Thơ',
            'Cà phê Việt trong không gian sang trọng, sáng tạo, đậm bản sắc văn hóa cà phê Tây Đô với diện tích rộng rãi.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291022/cafestory/cafes/fl4nq7wvoudo07bpyh0u.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291062/cafestory/cafes/h5xakyim9x8dfbdctan3.png',
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
            'seed_stripe_txn_003', 'seed_order_003',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #04: Katinat Saigon Kafe (6mo)
    v_owner_id := v_owner_ids[4];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '48 Xô Viết Nghệ Tĩnh', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Katinat Saigon Kafe',
            '48 Xô Viết Nghệ Tĩnh, Phường Ninh Kiều, Cần Thơ',
            'Cafe hiện đại giới trẻ yêu thích, thiết kế sang-sáng, nhiều góc check-in đẹp ngay trung tâm Ninh Kiều sầm uất.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291024/cafestory/cafes/wgmqoq8klsr0hgrwxydn.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291066/cafestory/cafes/rjugbihwccrv3vjtgwm1.png',
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
            'seed_stripe_txn_004', 'seed_order_004',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #05: The 80's iCafe (6mo)
    v_owner_id := v_owner_ids[5];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '6/46 Mạc Thiên Tích', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'The 80''s iCafe',
            '6/46 Mạc Thiên Tích, Phường Ninh Kiều, Cần Thơ',
            'Quán quen của dân chạy deadline 24/7, không gian rộng rãi và ít ồn ào, gần Đại học Cần Thơ, có thể đặt bàn trước.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291026/cafestory/cafes/d8ahq97rc2ynsxlsq3lr.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291068/cafestory/cafes/ejenjrdrekiwpejpqypx.jpg',
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
            'seed_stripe_txn_005', 'seed_order_005',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #06: Đậu Ơi Coffee & Tea (6mo)
    v_owner_id := v_owner_ids[6];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '372B Nguyễn Văn Cừ', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Đậu Ơi Coffee & Tea',
            '372B Nguyễn Văn Cừ, Phường Ninh Kiều, Cần Thơ',
            'Quán học bài và làm việc xuyên đêm, không gian minimalism sáng thoáng, thức uống ngon với mức giá học sinh - sinh viên.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291029/cafestory/cafes/rsltdsjx8rlff0flz7ss.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291070/cafestory/cafes/hunu0ssi3pvt4qdkfyoh.jpg',
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
            'seed_stripe_txn_006', 'seed_order_006',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #07: Trầm Coffee & Tea (6mo)
    v_owner_id := v_owner_ids[7];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31150',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường An Bình',
            '321-323 Nguyễn Văn Cừ nối dài', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Trầm Coffee & Tea',
            '321-323 Nguyễn Văn Cừ nối dài, Phường An Bình, Cần Thơ',
            'Cafe & trà mở 24/7, không gian trẻ trung, phục vụ đông sinh viên khu vực Nguyễn Văn Cừ nối dài.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291031/cafestory/cafes/zhh7deqe3wc900y41yoa.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291072/cafestory/cafes/mic9mksat24evxqkb8lj.jpg',
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
            'seed_stripe_txn_007', 'seed_order_007',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #08: Cà phê Nhà Phạm (6mo)
    v_owner_id := v_owner_ids[8];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            'Đường 3 Tháng 2', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Cà phê Nhà Phạm',
            'Đường 3 Tháng 2, Phường Ninh Kiều, Cần Thơ',
            'Không gian mộc mạc pha vintage, decor nhiều cây xanh và ánh sáng vàng, mang lại cảm giác ''về nhà'' giữa lòng thành phố.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291032/cafestory/cafes/grtiidpxfujju5jgrbf0.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291075/cafestory/cafes/ucftikktx2egmemzxdtc.jpg',
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
            'seed_stripe_txn_008', 'seed_order_008',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #09: Nhà Phạm trong rừng (6mo)
    v_owner_id := v_owner_ids[9];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '219 Đường số 3, khu Giảng viên ĐH Cần Thơ', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Nhà Phạm trong rừng',
            '219 Đường số 3, khu Giảng viên ĐH Cần Thơ, Phường Ninh Kiều, Cần Thơ',
            'Chi nhánh nằm trong khu Giảng viên ĐH Cần Thơ, không gian xanh mát như ''ốc đảo'' giữa thành phố, thích hợp học bài, làm việc.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291034/cafestory/cafes/k72enlm7vz1ttijhqd6g.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291078/cafestory/cafes/awbf3ejniv9iaqjwlydc.jpg',
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
            'seed_stripe_txn_009', 'seed_order_009',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #10: Nhà Phạm bên cầu (6mo)
    v_owner_id := v_owner_ids[10];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31186',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Răng',
            'Cầu Đầu Sấu', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Nhà Phạm bên cầu',
            'Cầu Đầu Sấu, Phường Cái Răng, Cần Thơ',
            'Cafe view sông ngay chân cầu Đầu Sấu, sáng sớm và chiều mát view rất chill, phù hợp uống cà phê ngắm cảnh.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291035/cafestory/cafes/k5xpep0bbakd1qlfcs2x.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291081/cafestory/cafes/gayv5tyupow4nvwg932p.jpg',
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
            'seed_stripe_txn_010', 'seed_order_010',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #11: Là Cafe (3mo)
    v_owner_id := v_owner_ids[11];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '122/6 Mạc Thiên Tích', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Là Cafe',
            '122/6 Mạc Thiên Tích, Phường Ninh Kiều, Cần Thơ',
            'Không gian ấm cúng, chỗ ngồi rộng rãi, thực đơn đa dạng với cacao và espresso được đánh giá cao ở khu vực Ninh Kiều.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291037/cafestory/cafes/zh3wtfn7sew6hdtpfu5u.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291084/cafestory/cafes/y7yqcj0hl3umziznmzf3.jpg',
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
            'seed_stripe_txn_011', 'seed_order_011',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #12: Raw Coffee (3mo)
    v_owner_id := v_owner_ids[12];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '224 Đường 30 Tháng 4', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Raw Coffee',
            '224 Đường 30 Tháng 4, Phường Ninh Kiều, Cần Thơ',
            'Không gian gần gũi thiên nhiên độc đáo, view cầu Quang Trung, phong cách phục vụ và decor riêng biệt.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291038/cafestory/cafes/cbqbvzmoqeycnnxqwejg.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291087/cafestory/cafes/xulejov2hdu9fu6vbbhw.jpg',
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
            'seed_stripe_txn_012', 'seed_order_012',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #13: Tiệm Trà Cỏ Ngọt (3mo)
    v_owner_id := v_owner_ids[13];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '139/105 Đường 30 Tháng 4', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Tiệm Trà Cỏ Ngọt',
            '139/105 Đường 30 Tháng 4, Phường Ninh Kiều, Cần Thơ',
            'Không gian phong cách Nhật Bản mộc mạc, chủ yếu trang trí gỗ, phù hợp uống trà chiều và thư giãn.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291041/cafestory/cafes/f1h5bbdxiwm4arugd0md.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291089/cafestory/cafes/tjotbn8fbtzzi3zhhfrg.jpg',
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
            'seed_stripe_txn_013', 'seed_order_013',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #14: Time Cafe (3mo)
    v_owner_id := v_owner_ids[14];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '89A Võ Văn Tần', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Time Cafe',
            '89A Võ Văn Tần, Phường Ninh Kiều, Cần Thơ',
            'Cafe bình dân trên đường Võ Văn Tần, mức giá 20k-50k, không gian đơn giản dễ chịu cho học sinh sinh viên.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291042/cafestory/cafes/xpmbfhharnuomlrigz0f.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291090/cafestory/cafes/q6vglth27dbdtp9qo4wf.jpg',
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
            'seed_stripe_txn_014', 'seed_order_014',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #15: Sky Bar Iris (3mo)
    v_owner_id := v_owner_ids[15];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '224 Đường 30 Tháng 4', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Sky Bar Iris',
            '224 Đường 30 Tháng 4, Phường Ninh Kiều, Cần Thơ',
            'Sân thượng có view sông và cầu Quang Trung, đồ uống và không gian lên hình đẹp, chill về đêm.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291044/cafestory/cafes/j8v1ruvem7jxaecsxq7g.webp',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291094/cafestory/cafes/deaytkql4mio9vy1weii.jpg',
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
            'seed_stripe_txn_015', 'seed_order_015',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #16: Aurora Coffee (3mo)
    v_owner_id := v_owner_ids[16];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '419 Đường 30 Tháng 4', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Aurora Coffee',
            '419 Đường 30 Tháng 4, Phường Ninh Kiều, Cần Thơ',
            'Cafe view đẹp, wifi mạnh, không gian sạch sẽ không khói thuốc, nhân viên phục vụ thân thiện gần Cao đẳng Cần Thơ.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291046/cafestory/cafes/xlbbj9dwc4c7yenftoan.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291095/cafestory/cafes/u7mrumijxldehyv9a13j.jpg',
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
            'seed_stripe_txn_016', 'seed_order_016',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #17: HiHi Onigiri (3mo)
    v_owner_id := v_owner_ids[17];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31186',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Răng',
            '172/16B Lê Bình', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'HiHi Onigiri',
            '172/16B Lê Bình, Phường Cái Răng, Cần Thơ',
            'Phong cách bánh bèo hường phấn đáng yêu, ngoài đồ uống còn phục vụ bánh ngọt và đồ ăn vặt nhẹ.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291048/cafestory/cafes/bzveir77mtbfj9pqdczw.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291098/cafestory/cafes/mllzt50iotdrubwygorx.jpg',
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
            'seed_stripe_txn_017', 'seed_order_017',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #18: Mật Ngọt Coffee (3mo)
    v_owner_id := v_owner_ids[18];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '132x12 Hẻm Liên Tổ 1-2, Nguyễn Văn Cừ', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Mật Ngọt Coffee',
            '132x12 Hẻm Liên Tổ 1-2, Nguyễn Văn Cừ, Phường Ninh Kiều, Cần Thơ',
            'Không gian ngọt ngào ấm cúng, decor tone pastel, phục vụ nước ngọt và đồ ăn nhẹ, mới mở gần đây.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291051/cafestory/cafes/yviiamnyzocspyfhnx4z.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291100/cafestory/cafes/jleds9m6rhaatcitt2dt.jpg',
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
            'seed_stripe_txn_018', 'seed_order_018',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #19: Chịn Cà Phê (3mo)
    v_owner_id := v_owner_ids[19];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '82 Đường Số 3, KDC Đại Ngân', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Chịn Cà Phê',
            '82 Đường Số 3, KDC Đại Ngân, Phường Ninh Kiều, Cần Thơ',
            'Cafe KDC Đại Ngân, không gian yên tĩnh phù hợp học tập và làm việc lâu, mức giá bình dân.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291053/cafestory/cafes/lacqttlo7nszcr4wgccw.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291102/cafestory/cafes/wa60vaspklf6qa6wowiw.jpg',
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
            'seed_stripe_txn_019', 'seed_order_019',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Cafe #20: Tiệm Cà Phê Nhà Có Khách (3mo)
    v_owner_id := v_owner_ids[20];

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            '359/19B Nguyễn Văn Cừ', NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO cafe_pages (id, owner_user_id, region_id, name, address, description,
                            avatar_url, cover_url, status,
                            like_count, follower_count, max_members,
                            page_active, page_expires_at, created_at, updated_at)
    VALUES (gen_random_uuid(), v_owner_id, v_region_id,
            'Tiệm Cà Phê Nhà Có Khách',
            '359/19B Nguyễn Văn Cừ, Phường Ninh Kiều, Cần Thơ',
            'Cafe mới mở phong cách ''về nhà'', không gian ấm cúng phục vụ đông khách sinh viên và người ghé nghỉ chân.',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291055/cafestory/cafes/fy3ofercywtkiofhk1jt.jpg',
            'https://res.cloudinary.com/dwdjlzl9h/image/upload/v1784291105/cafestory/cafes/vkneugcqehbbzk1b73el.jpg',
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
            'seed_stripe_txn_020', 'seed_order_020',
            'Seeded PAID payment for demo', now(), now());

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_owner_id, v_role_cafepage, now())
    ON CONFLICT (user_id, role_id) DO NOTHING;

END $$;

COMMIT;
