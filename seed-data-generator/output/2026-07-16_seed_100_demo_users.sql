-- Seed 100 demo users (50 male + 50 female) for CafeStory.
-- Full names generated from Vietnamese family + middle + first name lists.
-- Addresses: random Phường in Thành phố Cần Thơ (province_code 92,
-- city_code 92). No street/area — user requested ward-level only.
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
            'ho.nghia@gmail.com',
            'doan.dao@gmail.com',
            'ho.loc@gmail.com',
            'dinh.hien@gmail.com',
            'kieu.binh@gmail.com',
            'nguyen.pha@gmail.com',
            'ho.thang@gmail.com',
            'le.tien@gmail.com',
            'kieu.khoi@gmail.com',
            'to.lam@gmail.com',
            'dao.uyen@gmail.com',
            'le.hao@gmail.com',
            'le.chau@gmail.com',
            'le.lam@gmail.com',
            'ho.mai@gmail.com',
            'hoang.vu@gmail.com',
            'trinh.thai@gmail.com',
            'le.anh@gmail.com',
            'kieu.loc@gmail.com',
            'vu.bao@gmail.com',
            'dao.thinh@gmail.com',
            'kieu.mai@gmail.com',
            'lam.kien@gmail.com',
            'cao.duyen@gmail.com',
            'ho.xuan@gmail.com',
            'dang.hung@gmail.com',
            'doan.hai@gmail.com',
            'ly.huy@gmail.com',
            'le.lan@gmail.com',
            'kieu.hung@gmail.com',
            'ho.phuong@gmail.com',
            'tang.hoang@gmail.com',
            'kieu.vy@gmail.com',
            'nguyen.nhu@gmail.com',
            'huynh.than@gmail.com',
            'nguyen.my@gmail.com',
            'la.loc@gmail.com',
            'dang.duc@gmail.com',
            'dinh.son@gmail.com',
            'la.trung@gmail.com',
            'mai.tuyet@gmail.com',
            'dam.bich@gmail.com',
            'tran.yen@gmail.com',
            'pham.tram@gmail.com',
            'le.dao@gmail.com',
            'dao.dao@gmail.com',
            'ho.khoi@gmail.com',
            'bui.xuan@gmail.com',
            'dao.chau@gmail.com',
            'pham.hanh@gmail.com',
            'nguyen.duy@gmail.com',
            'nguyen.tu@gmail.com',
            'dang.thang@gmail.com',
            'bui.tu@gmail.com',
            'dao.my@gmail.com',
            'vu.hoa@gmail.com',
            'ngo.hai@gmail.com',
            'mai.quynh@gmail.com',
            'tang.quang@gmail.com',
            'dao.trinh@gmail.com',
            'dao.loan@gmail.com',
            'trinh.dong@gmail.com',
            'dinh.huong@gmail.com',
            'ho.chau@gmail.com',
            'phan.huong@gmail.com',
            'pham.nghia@gmail.com',
            'dinh.hong@gmail.com',
            'duong.an@gmail.com',
            'bui.uyen@gmail.com',
            'tang.kien@gmail.com',
            'huynh.duye@gmail.com',
            'doan.tu@gmail.com',
            'dam.vu@gmail.com',
            'dao_chau@gmail.com',
            'trinh.dung@gmail.com',
            'hoang.hien@gmail.com',
            'ho_chau@gmail.com',
            'cao.van@gmail.com',
            'le.diem@gmail.com',
            'cao.tien@gmail.com',
            'ly.dung@gmail.com',
            'dam.loc@gmail.com',
            'vo.nga@gmail.com',
            'vu.tin@gmail.com',
            'duong.vuon@gmail.com',
            'dinh.anh@gmail.com',
            'doan.nam@gmail.com',
            'huynh.lam@gmail.com',
            'dam.tu@gmail.com',
            'ngo.duc@gmail.com',
            'ngo.thao@gmail.com',
            'hoang.phuo@gmail.com',
            'truong.dan@gmail.com',
            'la.tram@gmail.com',
            'huynh.huon@gmail.com',
            'trinh_thai@gmail.com',
            'kieu.nhung@gmail.com',
            'bui.quynh@gmail.com',
            'bui.linh@gmail.com',
            'huynh.duy@gmail.com'
        )
    ) THEN
        RAISE EXCEPTION 'One of the seed emails already exists — abort to avoid duplicates.';
    END IF;

    -- User 001: Hồ Duy Nghĩa (M) — Phường Vĩnh Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31783',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vĩnh Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.nghia', 'Hồ Duy Nghĩa',
            crypt('123456', gen_salt('bf', 10)),
            'ho.nghia@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.nghia',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 002: Đoàn Công Đạo (M) — Phường Ninh Kiều
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.dao', 'Đoàn Công Đạo',
            crypt('123456', gen_salt('bf', 10)),
            'doan.dao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.dao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 003: Hồ Xuân Lộc (M) — Phường Trung Nhứt
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31217',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Trung Nhứt',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.loc', 'Hồ Xuân Lộc',
            crypt('123456', gen_salt('bf', 10)),
            'ho.loc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.loc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 004: Đinh Bảo Hiền (F) — Phường Phú Lợi
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31510',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phú Lợi',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.hien', 'Đinh Bảo Hiền',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.hien@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.hien',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 005: Kiều Minh Bình (F) — Phường Thốt Nốt
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31207',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thốt Nốt',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.binh', 'Kiều Minh Bình',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.binh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.binh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 006: Nguyễn Đức Phát (M) — Phường Vĩnh Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31783',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vĩnh Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.pha', 'Nguyễn Đức Phát',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.pha@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.pha',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 007: Hồ Đình Thắng (M) — Phường Phú Lợi
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31510',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phú Lợi',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.thang', 'Hồ Đình Thắng',
            crypt('123456', gen_salt('bf', 10)),
            'ho.thang@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.thang',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 008: Lê Duy Tiến (M) — Phường Khánh Hòa
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31789',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Khánh Hòa',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.tien', 'Lê Duy Tiến',
            crypt('123456', gen_salt('bf', 10)),
            'le.tien@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.tien',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 009: Kiều Đình Khôi (M) — Phường Bình Thủy
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31168',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Bình Thủy',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.khoi', 'Kiều Đình Khôi',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.khoi@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.khoi',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 010: Tô Trung Lâm (M) — Phường Tân An
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31147',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Tân An',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'to.lam', 'Tô Trung Lâm',
            crypt('123456', gen_salt('bf', 10)),
            'to.lam@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=to.lam',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 011: Đào Khánh Uyên (F) — Phường Cái Răng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31186',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Răng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.uyen', 'Đào Khánh Uyên',
            crypt('123456', gen_salt('bf', 10)),
            'dao.uyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.uyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 012: Lê Việt Hào (M) — Phường Phước Thới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31162',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phước Thới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.hao', 'Lê Việt Hào',
            crypt('123456', gen_salt('bf', 10)),
            'le.hao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.hao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 013: Lê Xuân Châu (F) — Phường Trung Nhứt
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31217',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Trung Nhứt',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.chau', 'Lê Xuân Châu',
            crypt('123456', gen_salt('bf', 10)),
            'le.chau@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.chau',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 014: Lê Bá Lâm (M) — Phường Cái Răng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31186',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Răng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.lam', 'Lê Bá Lâm',
            crypt('123456', gen_salt('bf', 10)),
            'le.lam@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.lam',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 015: Hồ Ngọc Mai (F) — Phường Vị Tân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31333',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vị Tân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.mai', 'Hồ Ngọc Mai',
            crypt('123456', gen_salt('bf', 10)),
            'ho.mai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.mai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 016: Hoàng Thanh Vũ (M) — Phường Cái Khế
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31120',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Khế',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'hoang.vu', 'Hoàng Thanh Vũ',
            crypt('123456', gen_salt('bf', 10)),
            'hoang.vu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=hoang.vu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 017: Trịnh Trọng Thái (M) — Phường Long Phú 1
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31480',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Phú 1',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh.thai', 'Trịnh Trọng Thái',
            crypt('123456', gen_salt('bf', 10)),
            'trinh.thai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh.thai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 018: Lê Mỹ Ánh (F) — Phường Ô Môn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31153',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ô Môn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.anh', 'Lê Mỹ Ánh',
            crypt('123456', gen_salt('bf', 10)),
            'le.anh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.anh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 019: Kiều Văn Lộc (M) — Phường Đại Thành
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31411',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Đại Thành',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.loc', 'Kiều Văn Lộc',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.loc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.loc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 020: Vũ Đình Bảo (M) — Phường Long Mỹ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31471',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Mỹ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vu.bao', 'Vũ Đình Bảo',
            crypt('123456', gen_salt('bf', 10)),
            'vu.bao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vu.bao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 021: Đào Công Thịnh (M) — Phường Bình Thủy
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31168',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Bình Thủy',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.thinh', 'Đào Công Thịnh',
            crypt('123456', gen_salt('bf', 10)),
            'dao.thinh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.thinh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 022: Kiều Hoài Mai (F) — Phường Phước Thới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31162',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phước Thới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.mai', 'Kiều Hoài Mai',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.mai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.mai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 023: Lâm Duy Kiên (M) — Phường Ngã Năm
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31732',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Năm',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'lam.kien', 'Lâm Duy Kiên',
            crypt('123456', gen_salt('bf', 10)),
            'lam.kien@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=lam.kien',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 024: Cao Lan Duyên (F) — Phường An Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31150',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường An Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.duyen', 'Cao Lan Duyên',
            crypt('123456', gen_salt('bf', 10)),
            'cao.duyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.duyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 025: Hồ Hồng Xuân (F) — Phường Ô Môn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31153',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ô Môn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.xuan', 'Hồ Hồng Xuân',
            crypt('123456', gen_salt('bf', 10)),
            'ho.xuan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.xuan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 026: Đặng Anh Hùng (M) — Phường Đại Thành
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31411',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Đại Thành',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.hung', 'Đặng Anh Hùng',
            crypt('123456', gen_salt('bf', 10)),
            'dang.hung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.hung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 027: Đoàn Chí Hải (M) — Phường Tân An
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31147',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Tân An',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.hai', 'Đoàn Chí Hải',
            crypt('123456', gen_salt('bf', 10)),
            'doan.hai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.hai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 028: Lý Hữu Huy (M) — Phường Phước Thới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31162',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phước Thới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ly.huy', 'Lý Hữu Huy',
            crypt('123456', gen_salt('bf', 10)),
            'ly.huy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ly.huy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 029: Lê Bảo Lan (F) — Phường Long Phú 1
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31480',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Phú 1',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.lan', 'Lê Bảo Lan',
            crypt('123456', gen_salt('bf', 10)),
            'le.lan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.lan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 030: Kiều Minh Hùng (M) — Phường Ngã Năm
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31732',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Năm',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.hung', 'Kiều Minh Hùng',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.hung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.hung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 031: Hồ Thanh Phượng (F) — Phường Ninh Kiều
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.phuong', 'Hồ Thanh Phượng',
            crypt('123456', gen_salt('bf', 10)),
            'ho.phuong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.phuong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 032: Tăng Bảo Hoàng (M) — Phường Ô Môn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31153',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ô Môn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tang.hoang', 'Tăng Bảo Hoàng',
            crypt('123456', gen_salt('bf', 10)),
            'tang.hoang@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tang.hoang',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 033: Kiều Kim Vy (F) — Phường Phú Lợi
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31510',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phú Lợi',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.vy', 'Kiều Kim Vy',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.vy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.vy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 034: Nguyễn Minh Nhung (F) — Phường An Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31150',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường An Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.nhu', 'Nguyễn Minh Nhung',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.nhu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.nhu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 035: Huỳnh Thành Thắng (M) — Phường Khánh Hòa
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31789',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Khánh Hòa',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.than', 'Huỳnh Thành Thắng',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.than@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.than',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 036: Nguyễn Minh My (F) — Phường Thuận Hưng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31228',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thuận Hưng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.my', 'Nguyễn Minh My',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.my@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.my',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 037: La Anh Lộc (M) — Phường Cái Khế
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31120',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Khế',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'la.loc', 'La Anh Lộc',
            crypt('123456', gen_salt('bf', 10)),
            'la.loc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=la.loc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 038: Đặng Xuân Đức (M) — Phường Phước Thới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31162',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phước Thới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.duc', 'Đặng Xuân Đức',
            crypt('123456', gen_salt('bf', 10)),
            'dang.duc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.duc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 039: Đinh Anh Sơn (M) — Phường Thới An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31174',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.son', 'Đinh Anh Sơn',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.son@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.son',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 040: La Xuân Trung (M) — Phường Mỹ Quới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31753',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Mỹ Quới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'la.trung', 'La Xuân Trung',
            crypt('123456', gen_salt('bf', 10)),
            'la.trung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=la.trung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 041: Mai Khánh Tuyết (F) — Phường Cái Khế
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31120',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Khế',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'mai.tuyet', 'Mai Khánh Tuyết',
            crypt('123456', gen_salt('bf', 10)),
            'mai.tuyet@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=mai.tuyet',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 042: Đàm Thị Bích (F) — Phường Cái Khế
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31120',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Khế',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.bich', 'Đàm Thị Bích',
            crypt('123456', gen_salt('bf', 10)),
            'dam.bich@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.bich',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 043: Trần Minh Yến (F) — Phường Sóc Trăng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31507',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Sóc Trăng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tran.yen', 'Trần Minh Yến',
            crypt('123456', gen_salt('bf', 10)),
            'tran.yen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tran.yen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 044: Phạm Ngân Trâm (F) — Phường Sóc Trăng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31507',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Sóc Trăng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'pham.tram', 'Phạm Ngân Trâm',
            crypt('123456', gen_salt('bf', 10)),
            'pham.tram@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=pham.tram',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 045: Lê Minh Đạo (M) — Phường Thới An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31174',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.dao', 'Lê Minh Đạo',
            crypt('123456', gen_salt('bf', 10)),
            'le.dao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.dao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 046: Đào Minh Đạo (M) — Phường Long Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31473',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.dao', 'Đào Minh Đạo',
            crypt('123456', gen_salt('bf', 10)),
            'dao.dao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.dao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 047: Hồ Đình Khôi (M) — Phường Vĩnh Phước
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31804',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vĩnh Phước',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.khoi', 'Hồ Đình Khôi',
            crypt('123456', gen_salt('bf', 10)),
            'ho.khoi@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.khoi',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 048: Bùi Ngọc Xuân (F) — Phường Ninh Kiều
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.xuan', 'Bùi Ngọc Xuân',
            crypt('123456', gen_salt('bf', 10)),
            'bui.xuan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.xuan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 049: Đào Thúy Châu (F) — Phường Long Tuyền
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31183',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Tuyền',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.chau', 'Đào Thúy Châu',
            crypt('123456', gen_salt('bf', 10)),
            'dao.chau@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.chau',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 050: Phạm Diễm Hạnh (F) — Phường Cái Răng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31186',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Răng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'pham.hanh', 'Phạm Diễm Hạnh',
            crypt('123456', gen_salt('bf', 10)),
            'pham.hanh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=pham.hanh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 051: Nguyễn Minh Duy (M) — Phường Vĩnh Châu
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31783',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vĩnh Châu',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.duy', 'Nguyễn Minh Duy',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.duy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.duy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 052: Nguyễn Thị Tú (F) — Phường Thới Long
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31157',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới Long',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'nguyen.tu', 'Nguyễn Thị Tú',
            crypt('123456', gen_salt('bf', 10)),
            'nguyen.tu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=nguyen.tu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 053: Đặng Hữu Thắng (M) — Phường Vị Thanh
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31321',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vị Thanh',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dang.thang', 'Đặng Hữu Thắng',
            crypt('123456', gen_salt('bf', 10)),
            'dang.thang@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dang.thang',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 054: Bùi Nhật Tú (F) — Phường Thới Long
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31157',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới Long',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.tu', 'Bùi Nhật Tú',
            crypt('123456', gen_salt('bf', 10)),
            'bui.tu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.tu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 055: Đào Nhật My (F) — Phường Ô Môn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31153',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ô Môn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.my', 'Đào Nhật My',
            crypt('123456', gen_salt('bf', 10)),
            'dao.my@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.my',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 056: Vũ Công Hòa (M) — Phường Ô Môn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31153',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ô Môn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vu.hoa', 'Vũ Công Hòa',
            crypt('123456', gen_salt('bf', 10)),
            'vu.hoa@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vu.hoa',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 057: Ngô Bảo Hải (M) — Phường An Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31150',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường An Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ngo.hai', 'Ngô Bảo Hải',
            crypt('123456', gen_salt('bf', 10)),
            'ngo.hai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ngo.hai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 058: Mai Phương Quỳnh (F) — Phường Bình Thủy
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31168',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Bình Thủy',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'mai.quynh', 'Mai Phương Quỳnh',
            crypt('123456', gen_salt('bf', 10)),
            'mai.quynh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=mai.quynh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 059: Tăng Nhật Quang (M) — Phường Thới An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31174',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tang.quang', 'Tăng Nhật Quang',
            crypt('123456', gen_salt('bf', 10)),
            'tang.quang@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tang.quang',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 060: Đào Ngọc Trinh (F) — Phường Cái Răng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31186',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Răng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.trinh', 'Đào Ngọc Trinh',
            crypt('123456', gen_salt('bf', 10)),
            'dao.trinh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.trinh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 061: Đào Ánh Loan (F) — Phường Long Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31473',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao.loan', 'Đào Ánh Loan',
            crypt('123456', gen_salt('bf', 10)),
            'dao.loan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao.loan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 062: Trịnh Văn Đông (M) — Phường Long Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31473',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh.dong', 'Trịnh Văn Đông',
            crypt('123456', gen_salt('bf', 10)),
            'trinh.dong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh.dong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 063: Đinh Phương Hương (F) — Phường Vị Thanh
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31321',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vị Thanh',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.huong', 'Đinh Phương Hương',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.huong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.huong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 064: Hồ Minh Châu (F) — Phường Vị Tân
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31333',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vị Tân',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho.chau', 'Hồ Minh Châu',
            crypt('123456', gen_salt('bf', 10)),
            'ho.chau@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho.chau',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 065: Phan Diễm Hương (F) — Phường Đại Thành
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31411',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Đại Thành',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'phan.huong', 'Phan Diễm Hương',
            crypt('123456', gen_salt('bf', 10)),
            'phan.huong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=phan.huong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 066: Phạm Chí Nghĩa (M) — Phường Khánh Hòa
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31789',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Khánh Hòa',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'pham.nghia', 'Phạm Chí Nghĩa',
            crypt('123456', gen_salt('bf', 10)),
            'pham.nghia@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=pham.nghia',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 067: Đinh Uyên Hồng (F) — Phường Vị Thanh
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31321',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vị Thanh',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.hong', 'Đinh Uyên Hồng',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.hong@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.hong',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 068: Dương Đình An (M) — Phường Thốt Nốt
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31207',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thốt Nốt',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'duong.an', 'Dương Đình An',
            crypt('123456', gen_salt('bf', 10)),
            'duong.an@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=duong.an',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 069: Bùi Bích Uyên (F) — Phường Thới An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31174',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.uyen', 'Bùi Bích Uyên',
            crypt('123456', gen_salt('bf', 10)),
            'bui.uyen@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.uyen',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 070: Tăng Nhật Kiên (M) — Phường Ninh Kiều
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'tang.kien', 'Tăng Nhật Kiên',
            crypt('123456', gen_salt('bf', 10)),
            'tang.kien@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=tang.kien',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 071: Huỳnh Bảo Duyên (F) — Phường Long Mỹ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31471',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Mỹ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.duye', 'Huỳnh Bảo Duyên',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.duye@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.duye',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 072: Đoàn Uyên Tú (F) — Phường Long Tuyền
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31183',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Tuyền',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.tu', 'Đoàn Uyên Tú',
            crypt('123456', gen_salt('bf', 10)),
            'doan.tu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.tu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 073: Đàm Anh Vũ (M) — Phường Khánh Hòa
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31789',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Khánh Hòa',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.vu', 'Đàm Anh Vũ',
            crypt('123456', gen_salt('bf', 10)),
            'dam.vu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.vu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 074: Đào Thanh Châu (F) — Phường An Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31150',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường An Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dao_chau', 'Đào Thanh Châu',
            crypt('123456', gen_salt('bf', 10)),
            'dao_chau@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dao_chau',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 075: Trịnh Hoàng Dũng (M) — Phường Long Mỹ
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31471',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Mỹ',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh.dung', 'Trịnh Hoàng Dũng',
            crypt('123456', gen_salt('bf', 10)),
            'trinh.dung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh.dung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 076: Hoàng Vân Hiền (F) — Phường Long Tuyền
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31183',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Tuyền',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'hoang.hien', 'Hoàng Vân Hiền',
            crypt('123456', gen_salt('bf', 10)),
            'hoang.hien@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=hoang.hien',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 077: Hồ Thanh Châu (F) — Phường Phước Thới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31162',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phước Thới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ho_chau', 'Hồ Thanh Châu',
            crypt('123456', gen_salt('bf', 10)),
            'ho_chau@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ho_chau',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 078: Cao Hoài Vân (F) — Phường Sóc Trăng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31507',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Sóc Trăng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.van', 'Cao Hoài Vân',
            crypt('123456', gen_salt('bf', 10)),
            'cao.van@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.van',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 079: Lê Hồng Diễm (F) — Phường Trung Nhứt
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31217',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Trung Nhứt',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'le.diem', 'Lê Hồng Diễm',
            crypt('123456', gen_salt('bf', 10)),
            'le.diem@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=le.diem',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 080: Cao Khắc Tiến (M) — Phường Vĩnh Phước
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31804',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vĩnh Phước',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'cao.tien', 'Cao Khắc Tiến',
            crypt('123456', gen_salt('bf', 10)),
            'cao.tien@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=cao.tien',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 081: Lý Hoàng Dũng (M) — Phường Trung Nhứt
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31217',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Trung Nhứt',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ly.dung', 'Lý Hoàng Dũng',
            crypt('123456', gen_salt('bf', 10)),
            'ly.dung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ly.dung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 082: Đàm Văn Lộc (M) — Phường Thuận Hưng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31228',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thuận Hưng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.loc', 'Đàm Văn Lộc',
            crypt('123456', gen_salt('bf', 10)),
            'dam.loc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.loc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 083: Võ Thanh Nga (F) — Phường Ngã Năm
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31732',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Năm',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vo.nga', 'Võ Thanh Nga',
            crypt('123456', gen_salt('bf', 10)),
            'vo.nga@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vo.nga',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 084: Vũ Xuân Tín (M) — Phường Tân Lộc
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31213',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Tân Lộc',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vu.tin', 'Vũ Xuân Tín',
            crypt('123456', gen_salt('bf', 10)),
            'vu.tin@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=vu.tin',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 085: Dương Minh Vượng (M) — Phường Đại Thành
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31411',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Đại Thành',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'duong.vuon', 'Dương Minh Vượng',
            crypt('123456', gen_salt('bf', 10)),
            'duong.vuon@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=duong.vuon',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 086: Đinh Nhật Ánh (F) — Phường Cái Khế
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31120',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Cái Khế',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dinh.anh', 'Đinh Nhật Ánh',
            crypt('123456', gen_salt('bf', 10)),
            'dinh.anh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dinh.anh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 087: Đoàn Hải Nam (M) — Phường Bình Thủy
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31168',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Bình Thủy',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'doan.nam', 'Đoàn Hải Nam',
            crypt('123456', gen_salt('bf', 10)),
            'doan.nam@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=doan.nam',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 088: Huỳnh Diệu Lâm (F) — Phường Đại Thành
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31411',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Đại Thành',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.lam', 'Huỳnh Diệu Lâm',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.lam@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.lam',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 089: Đàm Thị Tú (F) — Phường Ngã Năm
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31732',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Năm',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'dam.tu', 'Đàm Thị Tú',
            crypt('123456', gen_salt('bf', 10)),
            'dam.tu@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=dam.tu',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 090: Ngô Chí Đức (M) — Phường Long Bình
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31473',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Bình',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ngo.duc', 'Ngô Chí Đức',
            crypt('123456', gen_salt('bf', 10)),
            'ngo.duc@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ngo.duc',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 091: Ngô Nhật Thảo (F) — Phường Phước Thới
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31162',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Phước Thới',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'ngo.thao', 'Ngô Nhật Thảo',
            crypt('123456', gen_salt('bf', 10)),
            'ngo.thao@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=ngo.thao',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 092: Hoàng Thị Phượng (F) — Phường Bình Thủy
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31168',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Bình Thủy',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'hoang.phuo', 'Hoàng Thị Phượng',
            crypt('123456', gen_salt('bf', 10)),
            'hoang.phuo@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=hoang.phuo',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 093: Trương Nhật Danh (M) — Phường Long Phú 1
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31480',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Long Phú 1',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'truong.dan', 'Trương Nhật Danh',
            crypt('123456', gen_salt('bf', 10)),
            'truong.dan@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=truong.dan',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 094: La Thanh Trâm (F) — Phường Ô Môn
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31153',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ô Môn',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'la.tram', 'La Thanh Trâm',
            crypt('123456', gen_salt('bf', 10)),
            'la.tram@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=la.tram',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 095: Huỳnh Khánh Hương (F) — Phường Ngã Năm
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31732',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Năm',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.huon', 'Huỳnh Khánh Hương',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.huon@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.huon',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 096: Trịnh Thanh Thái (M) — Phường Ngã Bảy
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31340',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Bảy',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'trinh_thai', 'Trịnh Thanh Thái',
            crypt('123456', gen_salt('bf', 10)),
            'trinh_thai@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=trinh_thai',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 097: Kiều Mỹ Nhung (F) — Phường Thới An Đông
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31174',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thới An Đông',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'kieu.nhung', 'Kiều Mỹ Nhung',
            crypt('123456', gen_salt('bf', 10)),
            'kieu.nhung@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=kieu.nhung',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 098: Bùi Kim Quỳnh (F) — Phường Thuận Hưng
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31228',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Thuận Hưng',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.quynh', 'Bùi Kim Quỳnh',
            crypt('123456', gen_salt('bf', 10)),
            'bui.quynh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.quynh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 099: Bùi Diệu Linh (F) — Phường Ngã Năm
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31732',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ngã Năm',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'bui.linh', 'Bùi Diệu Linh',
            crypt('123456', gen_salt('bf', 10)),
            'bui.linh@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=bui.linh',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

    -- User 100: Huỳnh Hải Duy (M) — Phường Vị Thanh
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31321',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Vị Thanh',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'huynh.duy', 'Huỳnh Hải Duy',
            crypt('123456', gen_salt('bf', 10)),
            'huynh.duy@gmail.com',
            'https://api.dicebear.com/9.x/avataaars/svg?seed=huynh.duy',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());

END $$;

COMMIT;
