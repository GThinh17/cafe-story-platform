-- Fix sai FK của reviewer_payouts.reviewer_id.
--
-- Entity ReviewerPayout.reviewer là @ManyToOne tới Reviewer (bảng reviewers, PK reviewer_id).
-- DB đang giữ song song hai FK trên cùng cột reviewer_id:
--   fk24fn7s8l3x7wpuvc9raxyfj7m -> users(user_id)      (sai, tàn dư ddl-auto=update)
--   fk5qd0gjaybmjwm65sri5e9bl0j -> reviewers(reviewer_id) (đúng)
-- Cái sai làm mọi insert đều vỡ:
--   POST /api/reviewers/payouts/generate → "could not execute batch [Batch entry 0 insert into reviewer_payouts ...]"
--
-- Bảng anh em reviewer_badges đã được sửa y hệt tại
-- docs/database/migrations/2026-06-29_fix_reviewer_badges_fk.sql.
--
-- Tên constraint hash do Hibernate sinh khác nhau theo từng schema nên không hard-code:
-- duyệt pg_constraint và drop mọi FK trên reviewer_id không trỏ tới reviewers.
-- Nếu FK đúng đã tồn tại thì block ADD được bỏ qua → idempotent.

SET statement_timeout = 0;
SET lock_timeout = '30s';

DO $$
DECLARE
    stale_constraint text;
BEGIN
    IF to_regclass('public.reviewer_payouts') IS NULL THEN
        RETURN;
    END IF;

    FOR stale_constraint IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_attribute att
          ON att.attrelid = con.conrelid
         AND att.attnum = con.conkey[1]
        WHERE con.conrelid = 'public.reviewer_payouts'::regclass
          AND con.contype = 'f'
          AND array_length(con.conkey, 1) = 1
          AND att.attname = 'reviewer_id'
          AND con.confrelid <> 'public.reviewers'::regclass
    LOOP
        EXECUTE format(
            'ALTER TABLE reviewer_payouts DROP CONSTRAINT %I',
            stale_constraint
        );
    END LOOP;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint con
        JOIN pg_attribute att
          ON att.attrelid = con.conrelid
         AND att.attnum = con.conkey[1]
        WHERE con.conrelid = 'public.reviewer_payouts'::regclass
          AND con.contype = 'f'
          AND array_length(con.conkey, 1) = 1
          AND att.attname = 'reviewer_id'
          AND con.confrelid = 'public.reviewers'::regclass
    ) THEN
        ALTER TABLE reviewer_payouts
            ADD CONSTRAINT fk_reviewer_payouts_reviewer
            FOREIGN KEY (reviewer_id) REFERENCES reviewers(reviewer_id);
    END IF;
END $$;
