-- PostgreSQL migration for CafeStory reviewer ranking & scoring config refactor.
-- Creates 3 new tables: reviewer_scoring_formula, reviewer_badge_threshold, reviewer_ranking_snapshot.
-- Apply manually because this project does not currently use Flyway or Liquibase.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Scoring formula: stores configurable weights and payout amounts
CREATE TABLE IF NOT EXISTS reviewer_scoring_formula (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    like_weight integer NOT NULL DEFAULT 1,
    comment_weight integer NOT NULL DEFAULT 5,
    share_weight integer NOT NULL DEFAULT 3,
    like_payout_amount bigint NOT NULL DEFAULT 100,
    comment_payout_amount bigint NOT NULL DEFAULT 500,
    share_payout_amount bigint NOT NULL DEFAULT 300,
    active boolean NOT NULL DEFAULT false,
    description text,
    created_by uuid REFERENCES users(user_id) ON DELETE SET NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_reviewer_scoring_formula_active ON reviewer_scoring_formula(active) WHERE active = true;

-- 2. Badge thresholds: min score per badge level, tied to a formula
CREATE TABLE IF NOT EXISTS reviewer_badge_threshold (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    badge varchar(20) NOT NULL,
    min_score bigint NOT NULL DEFAULT 0,
    formula_id uuid NOT NULL REFERENCES reviewer_scoring_formula(id) ON DELETE CASCADE,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    CONSTRAINT uk_badge_threshold_formula_badge UNIQUE (formula_id, badge),
    CONSTRAINT ck_badge_threshold_badge CHECK (badge IN ('IRON', 'BRONZE', 'SILVER', 'GOLD', 'DIAMOND'))
);

CREATE INDEX IF NOT EXISTS idx_reviewer_badge_threshold_formula_id ON reviewer_badge_threshold(formula_id);

-- 3. Ranking snapshot: cached ranking results per period
CREATE TABLE IF NOT EXISTS reviewer_ranking_snapshot (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    reviewer_id uuid NOT NULL REFERENCES reviewers(reviewer_id) ON DELETE CASCADE,
    period varchar(20) NOT NULL,
    period_type varchar(10) NOT NULL,
    rank_position integer NOT NULL,
    score bigint NOT NULL DEFAULT 0,
    like_count bigint NOT NULL DEFAULT 0,
    share_count bigint NOT NULL DEFAULT 0,
    comment_count bigint NOT NULL DEFAULT 0,
    badge varchar(20) NOT NULL,
    formula_id uuid NOT NULL REFERENCES reviewer_scoring_formula(id) ON DELETE CASCADE,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    CONSTRAINT uk_ranking_snapshot_reviewer_period UNIQUE (reviewer_id, period, period_type),
    CONSTRAINT ck_ranking_snapshot_period_type CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT ck_ranking_snapshot_badge CHECK (badge IN ('IRON', 'BRONZE', 'SILVER', 'GOLD', 'DIAMOND'))
);

CREATE INDEX IF NOT EXISTS idx_ranking_snapshot_period ON reviewer_ranking_snapshot(period, period_type);
CREATE INDEX IF NOT EXISTS idx_ranking_snapshot_reviewer_id ON reviewer_ranking_snapshot(reviewer_id);
CREATE INDEX IF NOT EXISTS idx_ranking_snapshot_rank ON reviewer_ranking_snapshot(period, period_type, rank_position);

-- 4. Seed default formula and badge thresholds
INSERT INTO reviewer_scoring_formula (like_weight, comment_weight, share_weight, like_payout_amount, comment_payout_amount, share_payout_amount, active, description)
VALUES (1, 5, 3, 100, 500, 300, true, 'Default formula')
ON CONFLICT DO NOTHING;

-- Seed badge thresholds for the default formula
WITH default_formula AS (
    SELECT id FROM reviewer_scoring_formula WHERE active = true LIMIT 1
)
INSERT INTO reviewer_badge_threshold (badge, min_score, formula_id)
SELECT badge, min_score, df.id
FROM default_formula df,
     (VALUES
         ('IRON', 0),
         ('BRONZE', 100),
         ('SILVER', 300),
         ('GOLD', 700),
         ('DIAMOND', 1500)
     ) AS t(badge, min_score)
ON CONFLICT (formula_id, badge) DO NOTHING;
