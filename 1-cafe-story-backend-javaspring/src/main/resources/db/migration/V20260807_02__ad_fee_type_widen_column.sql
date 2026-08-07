-- Gỡ Postgres ENUM ad_fee_type khỏi cột ad_fees.fee_type (nếu môi trường nào còn dùng).
--
-- Enum Java AdFeeType nay có thêm CAFE_AD_STARTER / CAFE_AD_GROWTH / CAFE_AD_PREMIUM.
-- Trên DB chính cột fee_type đã là varchar nên migration này là no-op, nhưng
-- docs/database/migrations/2026-05-25_feed_ads_mvp.sql có tạo
-- `CREATE TYPE ad_fee_type AS ENUM ('FEED_10000_IMPRESSIONS_OR_30_DAYS')` — chỉ một label —
-- nên môi trường dev nào đã apply file thủ công đó sẽ vỡ khi insert gói mới.
--
-- Chọn đổi cột sang varchar thay vì `ALTER TYPE ... ADD VALUE` vì:
--   1. ADD VALUE không chạy được bên trong DO block / transaction mà Flyway dùng;
--   2. varchar mới đúng với @Enumerated(EnumType.STRING) của Hibernate;
--   3. thêm gói quảng cáo sau này không còn cần migration DB nào nữa.
--
-- Dùng to_regtype (trả NULL) thay cho ::regtype (ném lỗi) để an toàn khi kiểu không tồn tại.
-- UNIQUE index trên fee_type được giữ nguyên — Postgres tự mang theo khi đổi kiểu cột.

SET statement_timeout = 0;
SET lock_timeout = '30s';

DO $$
BEGIN
    IF to_regclass('public.ad_fees') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'ad_fees'
          AND column_name = 'fee_type'
          AND udt_name = 'ad_fee_type'
    ) THEN
        ALTER TABLE ad_fees
            ALTER COLUMN fee_type TYPE varchar(64) USING fee_type::text;
    END IF;
END $$;

-- Kiểu enum chỉ được drop khi không còn cột nào tham chiếu.
DO $$
DECLARE
    enum_oid oid := to_regtype('ad_fee_type');
BEGIN
    IF enum_oid IS NULL THEN
        RETURN;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_attribute att
        JOIN pg_class cls ON cls.oid = att.attrelid
        WHERE att.atttypid = enum_oid
          AND att.attisdropped = false
          AND cls.relkind = 'r'
    ) THEN
        DROP TYPE ad_fee_type;
    END IF;
END $$;
