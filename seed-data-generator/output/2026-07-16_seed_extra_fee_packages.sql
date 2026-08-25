-- Seed 4 gói dịch vụ cho bảng extra_fees.
-- fee_type khớp enum ExtraFeeType (REVIEWER_REGISTRATION | CAFE_PAGE_OPENING).
-- price lưu VND dưới dạng BIGINT (199k → 199000).
-- max_members = NULL: BE (AdminExtraFeeService.resolveMaxMembers) tự resolve = 2
-- khi fee_type = CAFE_PAGE_OPENING và giá trị null.
-- Idempotent: check theo name trước khi insert, có thể chạy lại an toàn.

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM extra_fees WHERE name = 'Cafe Page 6 tháng') THEN
        INSERT INTO extra_fees (extra_fee_id, name, description, fee_type, price,
                                duration_months, max_members, status,
                                created_at, updated_at)
        VALUES (gen_random_uuid(), 'Cafe Page 6 tháng',
                'Mở khóa trang Cafe Page trong 6 tháng',
                'CAFE_PAGE_OPENING', 199000, 6, NULL, true, now(), now());
    END IF;

    IF NOT EXISTS (SELECT 1 FROM extra_fees WHERE name = 'Cafe Page 3 tháng') THEN
        INSERT INTO extra_fees (extra_fee_id, name, description, fee_type, price,
                                duration_months, max_members, status,
                                created_at, updated_at)
        VALUES (gen_random_uuid(), 'Cafe Page 3 tháng',
                'Mở khóa trang Cafe Page trong 3 tháng',
                'CAFE_PAGE_OPENING', 100000, 3, NULL, true, now(), now());
    END IF;

    IF NOT EXISTS (SELECT 1 FROM extra_fees WHERE name = 'Reviewer 6 tháng') THEN
        INSERT INTO extra_fees (extra_fee_id, name, description, fee_type, price,
                                duration_months, max_members, status,
                                created_at, updated_at)
        VALUES (gen_random_uuid(), 'Reviewer 6 tháng',
                'Đăng ký làm Reviewer trong 6 tháng',
                'REVIEWER_REGISTRATION', 99000, 6, NULL, true, now(), now());
    END IF;

    IF NOT EXISTS (SELECT 1 FROM extra_fees WHERE name = 'Reviewer 3 tháng') THEN
        INSERT INTO extra_fees (extra_fee_id, name, description, fee_type, price,
                                duration_months, max_members, status,
                                created_at, updated_at)
        VALUES (gen_random_uuid(), 'Reviewer 3 tháng',
                'Đăng ký làm Reviewer trong 3 tháng',
                'REVIEWER_REGISTRATION', 50000, 3, NULL, true, now(), now());
    END IF;
END $$;

COMMIT;
