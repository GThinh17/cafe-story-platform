-- Seed 100 demo users (50 male + 50 female) for CafeStory.
-- Full names generated from Vietnamese family + middle + first name lists.
-- Addresses: random Phường in Thành phố Đà Nẵng (province_code
-- 48, city_code 48). No street/area —
-- user requested ward-level only.
-- Avatars: DiceBear avataaars seeded by username → each user gets a stable
-- unique avatar URL.
-- Password '123456' hashed via pgcrypto.crypt(...gen_salt('bf', 10)) at INSERT
-- time so Spring's BCryptPasswordEncoder can verify ($2a$10$... format).
-- Role assignment: USER only.

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    v_role_user integer;
    v_region_id uuid;
    v_user_id   uuid;
BEGIN
    -- Ensure USER role exists (idempotent, matches AuthServiceImpl.assignDefaultUserRole)
    INSERT INTO roles (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;
    SELECT id INTO v_role_user FROM roles WHERE name = 'USER';

    IF v_role_user IS NULL THEN
        RAISE EXCEPTION 'USER role could not be resolved.';
    END IF;

    -- Abort if any seed email already present — keeps this migration safely re-runnable
    IF EXISTS (
        SELECT 1 FROM users
        WHERE user_email IN (
            'trinh_dung@gmail.com',
            'doan.nhung@gmail.com',
            'tran.huyen@gmail.com',
            'le.hoa@gmail.com',
            'dam.binh@gmail.com',
            'duong.dat@gmail.com',
            'tang.anh@gmail.com',
            'cao.bao@gmail.com',
            'dao_uyen@gmail.com',
            'bui.huy@gmail.com',
            'cao.hao@gmail.com',
            'cao.loan@gmail.com',
            'le.nga@gmail.com',
            'dam.hung@gmail.com',
            'phan.danh@gmail.com',
            'le.phuc@gmail.com',
            'cao.anh@gmail.com',
            'huynh.hoan@gmail.com',
            'le_hoa@gmail.com',
            'tran.phuon@gmail.com',
            'dao.dung@gmail.com',
            'kieu.linh@gmail.com',
            'nguyen.thu@gmail.com',
            'cao.oanh@gmail.com',
            'vu.tung@gmail.com',
            'do.hoa@gmail.com',
            'dao.thanh@gmail.com',
            'nguyen.nam@gmail.com',
            'dang.son@gmail.com',
            'dang.dat@gmail.com',
            'vo.trinh@gmail.com',
            'dao.khanh@gmail.com',
            'phan.khanh@gmail.com',
            'dao.thai@gmail.com',
            'bui.tung@gmail.com',
            'la.hung@gmail.com',
            'ly.huyen@gmail.com',
            'huynh.nhi@gmail.com',
            'to.hanh@gmail.com',
            'cao.an@gmail.com',
            'truong.tu@gmail.com',
            'doan.an@gmail.com',
            'phan.cuong@gmail.com',
            'vu.vy@gmail.com',
            'do.dung@gmail.com',
            'nguyen.tua@gmail.com',
            'doan.dieu@gmail.com',
            'kieu.long@gmail.com',
            'le.linh@gmail.com',
            'la.duyen@gmail.com',
            'nguyen.phu@gmail.com',
            'dinh.bich@gmail.com',
            'mai.loan@gmail.com',
            'cao.duy@gmail.com',
            'dinh.linh@gmail.com',
            'trinh.cuon@gmail.com',
            'do.nhung@gmail.com',
            'tran.tram@gmail.com',
            'ho.trang@gmail.com',
            'hoang.phu@gmail.com',
            'dao_thanh@gmail.com',
            'duong.tuye@gmail.com',
            'le.kim@gmail.com',
            'huynh.vu@gmail.com',
            'pham.sang@gmail.com',
            'hoang.tung@gmail.com',
            'dang.kiet@gmail.com',
            'doan_dao@gmail.com',
            'ly.danh@gmail.com',
            'dang.hong@gmail.com',
            'kieu.kiet@gmail.com',
            'mai.uyen@gmail.com',
            'tran.kim@gmail.com',
            'phan_khanh@gmail.com',
            'dang.thai@gmail.com',
            'truong.tha@gmail.com',
            'mai.phong@gmail.com',
            'dinh.quynh@gmail.com',
            'chu.dao@gmail.com',
            'dang.loc@gmail.com',
            'dinh.hung@gmail.com',
            'truong_tha@gmail.com',
            'bui.phuong@gmail.com',
            'ho.nam@gmail.com',
            'le.nhat@gmail.com',
            'nguyen.nha@gmail.com',
            'bui.yen@gmail.com',
            'dinh.thao@gmail.com',
            'dam.toan@gmail.com',
            'la_loc@gmail.com',
            'nguyen.chu@gmail.com',
            'kieu.y@gmail.com',
            'ngo_duc@gmail.com',
            'vo.khanh@gmail.com',
            'phan.linh@gmail.com',
            'dao.danh@gmail.com',
            'trinh.viet@gmail.com',
            'do.nga@gmail.com',
            'kieu.nhi@gmail.com',
            'lam.cuong@gmail.com'
        )
    ) THEN
        RAISE EXCEPTION 'One of the seed emails already exists — abort to avoid duplicates.';
    END IF;

    -- User 001: Trịnh Hoàng Dũng (M) — Phường Bàn Thạch
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20335',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Bàn Thạch',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh_dung', 'Trịnh Hoàng Dũng',
            crypt('123456', gen_salt('bf', 10)),
            'trinh_dung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh_dung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 002: Đoàn Phương Nhung (F) — Phường An Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20305',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.nhung', 'Đoàn Phương Nhung',
            crypt('123456', gen_salt('bf', 10)),
            'doan.nhung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.nhung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 003: Trần Hồng Huyền (F) — Phường Cẩm Lệ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20260',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Cẩm Lệ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tran.huyen', 'Trần Hồng Huyền',
            crypt('123456', gen_salt('bf', 10)),
            'tran.huyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tran.huyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 004: Lê Ánh Hòa (F) — Phường An Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20305',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.hoa', 'Lê Ánh Hòa',
            crypt('123456', gen_salt('bf', 10)),
            'le.hoa@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.hoa',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 005: Đàm Thu Bình (F) — Phường An Thắng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20575',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Thắng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.binh', 'Đàm Thu Bình',
            crypt('123456', gen_salt('bf', 10)),
            'dam.binh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.binh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 006: Dương Khắc Đạt (M) — Phường Hội An
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20410',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'duong.dat', 'Dương Khắc Đạt',
            crypt('123456', gen_salt('bf', 10)),
            'duong.dat@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=duong.dat',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 007: Tăng Nhật Ánh (F) — Phường Hòa Khánh
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20200',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Khánh',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tang.anh', 'Tăng Nhật Ánh',
            crypt('123456', gen_salt('bf', 10)),
            'tang.anh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tang.anh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 008: Cao Việt Bảo (M) — Phường Hải Vân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20194',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Vân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.bao', 'Cao Việt Bảo',
            crypt('123456', gen_salt('bf', 10)),
            'cao.bao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.bao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 009: Đào Uyên Uyên (F) — Phường Điện Bàn Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20579',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao_uyen', 'Đào Uyên Uyên',
            crypt('123456', gen_salt('bf', 10)),
            'dao_uyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao_uyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 010: Bùi Tuấn Huy (M) — Phường Hội An Tây
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20401',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Tây',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.huy', 'Bùi Tuấn Huy',
            crypt('123456', gen_salt('bf', 10)),
            'bui.huy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.huy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 011: Cao Đình Hào (M) — Phường Hương Trà
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20350',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hương Trà',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.hao', 'Cao Đình Hào',
            crypt('123456', gen_salt('bf', 10)),
            'cao.hao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.hao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 012: Cao Thị Loan (F) — Phường Hội An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20413',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.loan', 'Cao Thị Loan',
            crypt('123456', gen_salt('bf', 10)),
            'cao.loan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.loan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 013: Lê Diễm Nga (F) — Phường Ngũ Hành Sơn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.nga', 'Lê Diễm Nga',
            crypt('123456', gen_salt('bf', 10)),
            'le.nga@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.nga',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 014: Đàm Trung Hùng (M) — Phường An Hải
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20275',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Hải',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.hung', 'Đàm Trung Hùng',
            crypt('123456', gen_salt('bf', 10)),
            'dam.hung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.hung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 015: Phan Bảo Danh (M) — Phường Cẩm Lệ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20260',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Cẩm Lệ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'phan.danh', 'Phan Bảo Danh',
            crypt('123456', gen_salt('bf', 10)),
            'phan.danh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=phan.danh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 016: Lê Minh Phúc (M) — Phường Điện Bàn Bắc
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20557',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Bắc',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.phuc', 'Lê Minh Phúc',
            crypt('123456', gen_salt('bf', 10)),
            'le.phuc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.phuc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 017: Cao Uyên Ánh (F) — Phường Hội An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20413',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.anh', 'Cao Uyên Ánh',
            crypt('123456', gen_salt('bf', 10)),
            'cao.anh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.anh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 018: Huỳnh Đình Hoàng (M) — Phường Liên Chiểu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20197',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Liên Chiểu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.hoan', 'Huỳnh Đình Hoàng',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.hoan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.hoan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 019: Lê Hoài Hòa (F) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le_hoa', 'Lê Hoài Hòa',
            crypt('123456', gen_salt('bf', 10)),
            'le_hoa@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le_hoa',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 020: Trần Ngân Phương (F) — Phường Điện Bàn Bắc
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20557',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Bắc',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tran.phuon', 'Trần Ngân Phương',
            crypt('123456', gen_salt('bf', 10)),
            'tran.phuon@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tran.phuon',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 021: Đào Kim Dung (F) — Phường Điện Bàn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20551',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.dung', 'Đào Kim Dung',
            crypt('123456', gen_salt('bf', 10)),
            'dao.dung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.dung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 022: Kiều Yến Linh (F) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.linh', 'Kiều Yến Linh',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.linh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.linh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 023: Nguyễn Xuân Thu (F) — Phường Hương Trà
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20350',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hương Trà',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.thu', 'Nguyễn Xuân Thu',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.thu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.thu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 024: Cao Phương Oanh (F) — Phường An Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20305',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.oanh', 'Cao Phương Oanh',
            crypt('123456', gen_salt('bf', 10)),
            'cao.oanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.oanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 025: Vũ Văn Tùng (M) — Phường Quảng Phú
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20356',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Quảng Phú',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vu.tung', 'Vũ Văn Tùng',
            crypt('123456', gen_salt('bf', 10)),
            'vu.tung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vu.tung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 026: Đỗ Hà Hoa (F) — Phường Điện Bàn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20551',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'do.hoa', 'Đỗ Hà Hoa',
            crypt('123456', gen_salt('bf', 10)),
            'do.hoa@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=do.hoa',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 027: Đào Thúy Thanh (F) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.thanh', 'Đào Thúy Thanh',
            crypt('123456', gen_salt('bf', 10)),
            'dao.thanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.thanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 028: Nguyễn Khắc Nam (M) — Phường Điện Bàn Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20579',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.nam', 'Nguyễn Khắc Nam',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.nam@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.nam',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 029: Đặng Tuấn Sơn (M) — Phường Thanh Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20209',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Thanh Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.son', 'Đặng Tuấn Sơn',
            crypt('123456', gen_salt('bf', 10)),
            'dang.son@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.son',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 030: Đặng Hữu Đạt (M) — Phường Cẩm Lệ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20260',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Cẩm Lệ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.dat', 'Đặng Hữu Đạt',
            crypt('123456', gen_salt('bf', 10)),
            'dang.dat@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.dat',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 031: Võ Mỹ Trinh (F) — Phường An Thắng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20575',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Thắng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vo.trinh', 'Võ Mỹ Trinh',
            crypt('123456', gen_salt('bf', 10)),
            'vo.trinh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vo.trinh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 032: Đào Khắc Khánh (M) — Phường An Thắng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20575',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Thắng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.khanh', 'Đào Khắc Khánh',
            crypt('123456', gen_salt('bf', 10)),
            'dao.khanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.khanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 033: Phan Anh Khánh (M) — Phường Cẩm Lệ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20260',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Cẩm Lệ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'phan.khanh', 'Phan Anh Khánh',
            crypt('123456', gen_salt('bf', 10)),
            'phan.khanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=phan.khanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 034: Đào Hữu Thái (M) — Phường Hải Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.thai', 'Đào Hữu Thái',
            crypt('123456', gen_salt('bf', 10)),
            'dao.thai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.thai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 035: Bùi Trung Tùng (M) — Phường Cẩm Lệ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20260',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Cẩm Lệ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.tung', 'Bùi Trung Tùng',
            crypt('123456', gen_salt('bf', 10)),
            'bui.tung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.tung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 036: La Anh Hùng (M) — Phường Điện Bàn Bắc
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20557',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Bắc',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'la.hung', 'La Anh Hùng',
            crypt('123456', gen_salt('bf', 10)),
            'la.hung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=la.hung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 037: Lý Thị Huyền (F) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ly.huyen', 'Lý Thị Huyền',
            crypt('123456', gen_salt('bf', 10)),
            'ly.huyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ly.huyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 038: Huỳnh Thúy Nhi (F) — Phường Điện Bàn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20551',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.nhi', 'Huỳnh Thúy Nhi',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.nhi@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.nhi',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 039: Tô Thu Hạnh (F) — Phường Hòa Cường
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20257',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Cường',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'to.hanh', 'Tô Thu Hạnh',
            crypt('123456', gen_salt('bf', 10)),
            'to.hanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=to.hanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 040: Cao Bảo An (M) — Phường Điện Bàn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20551',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.an', 'Cao Bảo An',
            crypt('123456', gen_salt('bf', 10)),
            'cao.an@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.an',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 041: Trương Ngân Tú (F) — Phường Ngũ Hành Sơn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'truong.tu', 'Trương Ngân Tú',
            crypt('123456', gen_salt('bf', 10)),
            'truong.tu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=truong.tu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 042: Đoàn Xuân An (M) — Phường Hội An
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20410',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.an', 'Đoàn Xuân An',
            crypt('123456', gen_salt('bf', 10)),
            'doan.an@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.an',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 043: Phan Việt Cường (M) — Phường Hải Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'phan.cuong', 'Phan Việt Cường',
            crypt('123456', gen_salt('bf', 10)),
            'phan.cuong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=phan.cuong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 044: Vũ Nhật Vy (F) — Phường An Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20305',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vu.vy', 'Vũ Nhật Vy',
            crypt('123456', gen_salt('bf', 10)),
            'vu.vy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vu.vy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 045: Đỗ Chí Dũng (M) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'do.dung', 'Đỗ Chí Dũng',
            crypt('123456', gen_salt('bf', 10)),
            'do.dung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=do.dung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 046: Nguyễn Trọng Tuấn (M) — Phường An Thắng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20575',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Thắng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.tua', 'Nguyễn Trọng Tuấn',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.tua@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.tua',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 047: Đoàn Thanh Diệu (F) — Phường Hội An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20413',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.dieu', 'Đoàn Thanh Diệu',
            crypt('123456', gen_salt('bf', 10)),
            'doan.dieu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.dieu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 048: Kiều Thành Long (M) — Phường Hội An
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20410',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.long', 'Kiều Thành Long',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.long@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.long',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 049: Lê Kim Linh (F) — Phường Quảng Phú
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20356',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Quảng Phú',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.linh', 'Lê Kim Linh',
            crypt('123456', gen_salt('bf', 10)),
            'le.linh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.linh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 050: La Nhật Duyên (F) — Phường Điện Bàn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20551',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'la.duyen', 'La Nhật Duyên',
            crypt('123456', gen_salt('bf', 10)),
            'la.duyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=la.duyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 051: Nguyễn Bảo Phương (F) — Phường Cẩm Lệ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20260',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Cẩm Lệ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.phu', 'Nguyễn Bảo Phương',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.phu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.phu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 052: Đinh Thúy Bích (F) — Phường Hòa Khánh
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20200',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Khánh',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.bich', 'Đinh Thúy Bích',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.bich@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.bich',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 053: Mai Ngân Loan (F) — Phường Quảng Phú
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20356',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Quảng Phú',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'mai.loan', 'Mai Ngân Loan',
            crypt('123456', gen_salt('bf', 10)),
            'mai.loan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=mai.loan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 054: Cao Công Duy (M) — Phường Hải Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.duy', 'Cao Công Duy',
            crypt('123456', gen_salt('bf', 10)),
            'cao.duy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.duy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 055: Đinh Hồng Linh (F) — Phường Tam Kỳ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20341',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Tam Kỳ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.linh', 'Đinh Hồng Linh',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.linh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.linh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 056: Trịnh Tuấn Cường (M) — Phường Quảng Phú
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20356',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Quảng Phú',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh.cuon', 'Trịnh Tuấn Cường',
            crypt('123456', gen_salt('bf', 10)),
            'trinh.cuon@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh.cuon',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 057: Đỗ Ánh Nhung (F) — Phường Thanh Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20209',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Thanh Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'do.nhung', 'Đỗ Ánh Nhung',
            crypt('123456', gen_salt('bf', 10)),
            'do.nhung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=do.nhung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 058: Trần Uyên Trâm (F) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tran.tram', 'Trần Uyên Trâm',
            crypt('123456', gen_salt('bf', 10)),
            'tran.tram@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tran.tram',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 059: Hồ Thanh Trang (F) — Phường Ngũ Hành Sơn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.trang', 'Hồ Thanh Trang',
            crypt('123456', gen_salt('bf', 10)),
            'ho.trang@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.trang',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 060: Hoàng Bá Phú (M) — Phường Bàn Thạch
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20335',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Bàn Thạch',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'hoang.phu', 'Hoàng Bá Phú',
            crypt('123456', gen_salt('bf', 10)),
            'hoang.phu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=hoang.phu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 061: Đào Hà Thanh (F) — Phường Tam Kỳ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20341',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Tam Kỳ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao_thanh', 'Đào Hà Thanh',
            crypt('123456', gen_salt('bf', 10)),
            'dao_thanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao_thanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 062: Dương Hoài Tuyết (F) — Phường An Hải
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20275',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Hải',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'duong.tuye', 'Dương Hoài Tuyết',
            crypt('123456', gen_salt('bf', 10)),
            'duong.tuye@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=duong.tuye',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 063: Lê Thanh Kim (F) — Phường An Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20305',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.kim', 'Lê Thanh Kim',
            crypt('123456', gen_salt('bf', 10)),
            'le.kim@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.kim',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 064: Huỳnh Thanh Vũ (M) — Phường Ngũ Hành Sơn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.vu', 'Huỳnh Thanh Vũ',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.vu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.vu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 065: Phạm Bảo Sang (M) — Phường Thanh Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20209',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Thanh Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'pham.sang', 'Phạm Bảo Sang',
            crypt('123456', gen_salt('bf', 10)),
            'pham.sang@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=pham.sang',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 066: Hoàng Xuân Tùng (M) — Phường Hải Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'hoang.tung', 'Hoàng Xuân Tùng',
            crypt('123456', gen_salt('bf', 10)),
            'hoang.tung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=hoang.tung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 067: Đặng Hữu Kiệt (M) — Phường Tam Kỳ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20341',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Tam Kỳ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.kiet', 'Đặng Hữu Kiệt',
            crypt('123456', gen_salt('bf', 10)),
            'dang.kiet@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.kiet',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 068: Đoàn Ngọc Đạo (M) — Phường Hội An Tây
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20401',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Tây',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan_dao', 'Đoàn Ngọc Đạo',
            crypt('123456', gen_salt('bf', 10)),
            'doan_dao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan_dao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 069: Lý Công Danh (M) — Phường Liên Chiểu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20197',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Liên Chiểu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ly.danh', 'Lý Công Danh',
            crypt('123456', gen_salt('bf', 10)),
            'ly.danh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ly.danh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 070: Đặng Ngân Hồng (F) — Phường Điện Bàn Bắc
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20557',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Bắc',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.hong', 'Đặng Ngân Hồng',
            crypt('123456', gen_salt('bf', 10)),
            'dang.hong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.hong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 071: Kiều Thanh Kiệt (M) — Phường An Thắng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20575',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Thắng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.kiet', 'Kiều Thanh Kiệt',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.kiet@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.kiet',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 072: Mai Phương Uyên (F) — Phường Thanh Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20209',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Thanh Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'mai.uyen', 'Mai Phương Uyên',
            crypt('123456', gen_salt('bf', 10)),
            'mai.uyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=mai.uyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 073: Trần Mỹ Kim (F) — Phường Liên Chiểu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20197',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Liên Chiểu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tran.kim', 'Trần Mỹ Kim',
            crypt('123456', gen_salt('bf', 10)),
            'tran.kim@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tran.kim',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 074: Phan Lan Khánh (F) — Phường Ngũ Hành Sơn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'phan_khanh', 'Phan Lan Khánh',
            crypt('123456', gen_salt('bf', 10)),
            'phan_khanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=phan_khanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 075: Đặng Nhật Thái (M) — Phường Hội An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20413',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.thai', 'Đặng Nhật Thái',
            crypt('123456', gen_salt('bf', 10)),
            'dang.thai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.thai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 076: Trương Trung Thái (M) — Phường Hội An
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20410',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'truong.tha', 'Trương Trung Thái',
            crypt('123456', gen_salt('bf', 10)),
            'truong.tha@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=truong.tha',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 077: Mai Hữu Phong (M) — Phường Tam Kỳ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20341',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Tam Kỳ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'mai.phong', 'Mai Hữu Phong',
            crypt('123456', gen_salt('bf', 10)),
            'mai.phong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=mai.phong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 078: Đinh Minh Quỳnh (F) — Phường Bàn Thạch
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20335',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Bàn Thạch',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.quynh', 'Đinh Minh Quỳnh',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.quynh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.quynh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 079: Chu Hoàng Đạo (M) — Phường Hòa Cường
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20257',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Cường',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'chu.dao', 'Chu Hoàng Đạo',
            crypt('123456', gen_salt('bf', 10)),
            'chu.dao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=chu.dao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 080: Đặng Tuấn Lộc (M) — Phường Ngũ Hành Sơn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20285',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Ngũ Hành Sơn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.loc', 'Đặng Tuấn Lộc',
            crypt('123456', gen_salt('bf', 10)),
            'dang.loc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.loc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 081: Đinh Hải Hùng (M) — Phường Điện Bàn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20551',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.hung', 'Đinh Hải Hùng',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.hung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.hung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 082: Trương Hồng Thảo (F) — Phường Hải Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'truong_tha', 'Trương Hồng Thảo',
            crypt('123456', gen_salt('bf', 10)),
            'truong_tha@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=truong_tha',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 083: Bùi Lan Phương (F) — Phường Bàn Thạch
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20335',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Bàn Thạch',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.phuong', 'Bùi Lan Phương',
            crypt('123456', gen_salt('bf', 10)),
            'bui.phuong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.phuong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 084: Hồ Tuấn Nam (M) — Phường Bàn Thạch
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20335',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Bàn Thạch',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.nam', 'Hồ Tuấn Nam',
            crypt('123456', gen_salt('bf', 10)),
            'ho.nam@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.nam',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 085: Lê Quốc Nhật (M) — Phường Điện Bàn Bắc
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20557',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Điện Bàn Bắc',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.nhat', 'Lê Quốc Nhật',
            crypt('123456', gen_salt('bf', 10)),
            'le.nhat@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.nhat',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 086: Nguyễn Hữu Nhật (M) — Phường Hải Vân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20194',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Vân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.nha', 'Nguyễn Hữu Nhật',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.nha@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.nha',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 087: Bùi Khánh Yến (F) — Phường Hải Vân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20194',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Vân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.yen', 'Bùi Khánh Yến',
            crypt('123456', gen_salt('bf', 10)),
            'bui.yen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.yen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 088: Đinh Ngân Thảo (F) — Phường Hải Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20242',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hải Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.thao', 'Đinh Ngân Thảo',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.thao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.thao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 089: Đàm Việt Toàn (M) — Phường Tam Kỳ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20341',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Tam Kỳ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.toan', 'Đàm Việt Toàn',
            crypt('123456', gen_salt('bf', 10)),
            'dam.toan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.toan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 090: La Trọng Lộc (M) — Phường An Hải
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20275',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Hải',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'la_loc', 'La Trọng Lộc',
            crypt('123456', gen_salt('bf', 10)),
            'la_loc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=la_loc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 091: Nguyễn Thành Chương (M) — Phường An Hải
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20275',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Hải',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.chu', 'Nguyễn Thành Chương',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.chu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.chu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 092: Kiều Xuân Ý (F) — Phường Hòa Cường
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20257',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Cường',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.y', 'Kiều Xuân Ý',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.y@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.y',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 093: Ngô Bá Đức (M) — Phường Tam Kỳ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20341',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Tam Kỳ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ngo_duc', 'Ngô Bá Đức',
            crypt('123456', gen_salt('bf', 10)),
            'ngo_duc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ngo_duc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 094: Võ Ngọc Khánh (F) — Phường Sơn Trà
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20263',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Sơn Trà',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vo.khanh', 'Võ Ngọc Khánh',
            crypt('123456', gen_salt('bf', 10)),
            'vo.khanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vo.khanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 095: Phan Thúy Linh (F) — Phường Hội An Tây
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20401',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hội An Tây',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'phan.linh', 'Phan Thúy Linh',
            crypt('123456', gen_salt('bf', 10)),
            'phan.linh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=phan.linh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 096: Đào Bảo Danh (M) — Phường An Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20305',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.danh', 'Đào Bảo Danh',
            crypt('123456', gen_salt('bf', 10)),
            'dao.danh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.danh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 097: Trịnh Bảo Việt (M) — Phường Hòa Xuân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20314',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Xuân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh.viet', 'Trịnh Bảo Việt',
            crypt('123456', gen_salt('bf', 10)),
            'trinh.viet@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh.viet',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 098: Đỗ Bảo Nga (F) — Phường Thanh Khê
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20209',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Thanh Khê',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'do.nga', 'Đỗ Bảo Nga',
            crypt('123456', gen_salt('bf', 10)),
            'do.nga@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=do.nga',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 099: Kiều Thúy Nhi (F) — Phường Hòa Cường
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20257',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường Hòa Cường',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.nhi', 'Kiều Thúy Nhi',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.nhi@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.nhi',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 100: Lâm Hải Cường (M) — Phường An Thắng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '48', '48', '20575',
            'Thành phố Đà Nẵng', 'Đà Nẵng', 'Phường An Thắng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'lam.cuong', 'Lâm Hải Cường',
            crypt('123456', gen_salt('bf', 10)),
            'lam.cuong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=lam.cuong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

END $$;

COMMIT;
