-- Migration: Unified reviewer_formula table
-- Merges payout_formula + reviewer_scoring_formula into a single table.
-- Ranking and payout both reference reviewer_formula.

-- ─── 1. Tạo bảng mới ────────────────────────────────────────────────────────

CREATE TABLE reviewer_formula (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    like_weight           INTEGER     NOT NULL DEFAULT 1,
    comment_weight        INTEGER     NOT NULL DEFAULT 5,
    share_weight          INTEGER     NOT NULL DEFAULT 3,
    like_payout_amount    BIGINT      NOT NULL DEFAULT 100,
    comment_payout_amount BIGINT      NOT NULL DEFAULT 500,
    share_payout_amount   BIGINT      NOT NULL DEFAULT 300,
    iron_multiplier       NUMERIC(5,2) NOT NULL DEFAULT 1.00,
    bronze_multiplier     NUMERIC(5,2) NOT NULL DEFAULT 1.20,
    silver_multiplier     NUMERIC(5,2) NOT NULL DEFAULT 1.50,
    gold_multiplier       NUMERIC(5,2) NOT NULL DEFAULT 2.00,
    diamond_multiplier    NUMERIC(5,2) NOT NULL DEFAULT 3.00,
    active                BOOLEAN     NOT NULL DEFAULT FALSE,
    description           TEXT,
    created_by            UUID        REFERENCES users(user_id) ON DELETE SET NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uidx_reviewer_formula_active
    ON reviewer_formula (active) WHERE active = TRUE;

-- ─── 2. Migrate data từ reviewer_scoring_formula ─────────────────────────────
-- Lấy scoring weights + payout amounts từ reviewer_scoring_formula.
-- Lấy badge multipliers từ active payout_formula (nếu có), fallback về defaults.

INSERT INTO reviewer_formula (
    id,
    like_weight, comment_weight, share_weight,
    like_payout_amount, comment_payout_amount, share_payout_amount,
    iron_multiplier, bronze_multiplier, silver_multiplier, gold_multiplier, diamond_multiplier,
    active, description, created_by, created_at, updated_at
)
SELECT
    rsf.id,
    rsf.like_weight, rsf.comment_weight, rsf.share_weight,
    rsf.like_payout_amount, rsf.comment_payout_amount, rsf.share_payout_amount,
    COALESCE(pf.iron_multiplier,    1.00),
    COALESCE(pf.bronze_multiplier,  1.20),
    COALESCE(pf.silver_multiplier,  1.50),
    COALESCE(pf.gold_multiplier,    2.00),
    COALESCE(pf.diamond_multiplier, 3.00),
    rsf.active,
    rsf.description,
    rsf.created_by,
    rsf.created_at,
    rsf.updated_at
FROM reviewer_scoring_formula rsf
LEFT JOIN payout_formula pf ON pf.active = TRUE;

-- ─── 3. reviewer_badge_threshold: đổi FK → reviewer_formula ─────────────────
-- formula_id hiện đang trỏ vào reviewer_scoring_formula.id
-- reviewer_formula.id được migrate với cùng id nên không cần update dữ liệu.

ALTER TABLE reviewer_badge_threshold
    DROP CONSTRAINT IF EXISTS reviewer_badge_threshold_formula_id_fkey;

ALTER TABLE reviewer_badge_threshold
    ADD CONSTRAINT reviewer_badge_threshold_formula_id_fkey
    FOREIGN KEY (formula_id) REFERENCES reviewer_formula(id) ON DELETE CASCADE;

-- ─── 4. reviewer_ranking_snapshot: đổi FK → reviewer_formula ────────────────

ALTER TABLE reviewer_ranking_snapshot
    DROP CONSTRAINT IF EXISTS reviewer_ranking_snapshot_formula_id_fkey;

ALTER TABLE reviewer_ranking_snapshot
    ADD CONSTRAINT reviewer_ranking_snapshot_formula_id_fkey
    FOREIGN KEY (formula_id) REFERENCES reviewer_formula(id);

-- ─── 5. reviewer_income: đổi FK từ payout_formula → reviewer_formula ────────
-- Các record cũ trỏ vào payout_formula.id (khác với reviewer_formula.id).
-- Set về active reviewer_formula.

ALTER TABLE reviewer_income
    DROP CONSTRAINT IF EXISTS reviewer_income_formula_id_fkey;

UPDATE reviewer_income
SET formula_id = (SELECT id FROM reviewer_formula WHERE active = TRUE LIMIT 1)
WHERE formula_id NOT IN (SELECT id FROM reviewer_formula);

ALTER TABLE reviewer_income
    ADD CONSTRAINT reviewer_income_formula_id_fkey
    FOREIGN KEY (formula_id) REFERENCES reviewer_formula(id);

-- ─── 6. admin_payout: đổi FK từ payout_formula → reviewer_formula ────────────

ALTER TABLE admin_payout
    DROP CONSTRAINT IF EXISTS admin_payout_formula_id_fkey;

UPDATE admin_payout
SET formula_id = (SELECT id FROM reviewer_formula WHERE active = TRUE LIMIT 1)
WHERE formula_id NOT IN (SELECT id FROM reviewer_formula);

ALTER TABLE admin_payout
    ADD CONSTRAINT admin_payout_formula_id_fkey
    FOREIGN KEY (formula_id) REFERENCES reviewer_formula(id);

-- ─── 7. Drop bảng cũ ─────────────────────────────────────────────────────────

DROP TABLE IF EXISTS reviewer_scoring_formula CASCADE;
DROP TABLE IF EXISTS payout_formula CASCADE;
