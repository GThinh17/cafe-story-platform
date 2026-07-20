-- Seed 2 demo accounts (vu@gmail.com, thinh@gmail.com) with USER + ADMIN roles.
-- Address: Phường Ninh Kiều — Cần Thơ, using master data from
-- 2026-06-13_normalized_vietnam_regions.sql.
-- Password '123456' hashed via pgcrypto blowfish (strength 10) so
-- Spring's BCryptPasswordEncoder can verify it (both use $2a$10$... format).

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    v_region_vu    uuid;
    v_region_thinh uuid;
    v_user_vu      uuid;
    v_user_thinh   uuid;
    v_role_user    integer;
    v_role_admin   integer;
BEGIN
    -- 1) Ensure roles USER + ADMIN exist (idempotent)
    INSERT INTO roles (name) VALUES ('USER'), ('ADMIN')
    ON CONFLICT (name) DO NOTHING;

    SELECT id INTO v_role_user  FROM roles WHERE name = 'USER';
    SELECT id INTO v_role_admin FROM roles WHERE name = 'ADMIN';

    IF v_role_user IS NULL OR v_role_admin IS NULL THEN
        RAISE EXCEPTION 'Failed to resolve USER/ADMIN role rows.';
    END IF;

    -- 2) Abort if either email already exists (this migration is not
    --    idempotent on users to avoid silent duplicates)
    IF EXISTS (SELECT 1 FROM users WHERE user_email IN ('vu@gmail.com','thinh@gmail.com')) THEN
        RAISE EXCEPTION 'Seed email already present — aborting to avoid duplicate rows.';
    END IF;

    -- 3) Create one region row per user, pointing at Ninh Kiều — Cần Thơ.
    --    province_code / city_code / ward_code FK into region_* master tables.
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            'Đường 3/2', NULL, now(), now())
    RETURNING region_id INTO v_region_vu;

    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '92', '92', '31135',
            'Thành phố Cần Thơ', 'Cần Thơ', 'Phường Ninh Kiều',
            'Đường 3/2', NULL, now(), now())
    RETURNING region_id INTO v_region_thinh;

    -- 4) Insert users. account_status/hide_cafe_page_on_profile explicit
    --    even though NOT NULL defaults exist, to make the seed self-documenting.
    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'vu.admin', 'Phạm Thanh Vũ',
            crypt('123456', gen_salt('bf', 10)),
            'vu@gmail.com', 0, 0, true, false, v_region_vu)
    RETURNING user_id INTO v_user_vu;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), 'thinh.dev', 'Trần Gia Thịnh',
            crypt('123456', gen_salt('bf', 10)),
            'thinh@gmail.com', 0, 0, true, false, v_region_thinh)
    RETURNING user_id INTO v_user_thinh;

    -- 5) Assign both USER + ADMIN to each user
    INSERT INTO user_roles (user_id, role_id, created_at) VALUES
        (v_user_vu,    v_role_user,  now()),
        (v_user_vu,    v_role_admin, now()),
        (v_user_thinh, v_role_user,  now()),
        (v_user_thinh, v_role_admin, now());
END $$;

COMMIT;
