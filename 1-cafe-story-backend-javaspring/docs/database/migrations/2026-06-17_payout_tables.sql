-- Migration: Payout system tables
-- Created: 2026-06-17
-- Tables: payout_formula, reviewer_income, admin_payout

-- ──────────────────────────────────────────────────────────────────────────────
-- Enum
-- ──────────────────────────────────────────────────────────────────────────────

CREATE TYPE admin_payout_status AS ENUM ('PENDING', 'APPROVED', 'PAID', 'CANCELLED');

-- ──────────────────────────────────────────────────────────────────────────────
-- payout_formula
-- Decoupled from reviewer_scoring_formula.
-- Stores payout amounts per action + badge multipliers.
-- Only one row can be active at a time.
-- ──────────────────────────────────────────────────────────────────────────────

CREATE TABLE payout_formula (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    like_payout_amount   BIGINT       NOT NULL DEFAULT 100,
    comment_payout_amount BIGINT      NOT NULL DEFAULT 500,
    share_payout_amount  BIGINT       NOT NULL DEFAULT 300,
    iron_multiplier      NUMERIC(5,2) NOT NULL DEFAULT 1.00,
    bronze_multiplier    NUMERIC(5,2) NOT NULL DEFAULT 1.20,
    silver_multiplier    NUMERIC(5,2) NOT NULL DEFAULT 1.50,
    gold_multiplier      NUMERIC(5,2) NOT NULL DEFAULT 2.00,
    diamond_multiplier   NUMERIC(5,2) NOT NULL DEFAULT 3.00,
    active               BOOLEAN      NOT NULL DEFAULT FALSE,
    description          TEXT,
    created_by           UUID         REFERENCES users(user_id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Only one active formula at a time
CREATE UNIQUE INDEX uidx_payout_formula_active ON payout_formula (active) WHERE active = TRUE;

-- ──────────────────────────────────────────────────────────────────────────────
-- reviewer_income
-- Daily income record per reviewer.
-- Upsert on (reviewer_id, income_date).
-- base_amount  = like*like_payout + comment*comment_payout + share*share_payout
-- final_amount = base_amount * badge_multiplier
-- ──────────────────────────────────────────────────────────────────────────────

CREATE TABLE reviewer_income (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    reviewer_id      UUID         NOT NULL REFERENCES reviewers(reviewer_id) ON DELETE CASCADE,
    income_date      DATE         NOT NULL,
    like_count       BIGINT       NOT NULL DEFAULT 0,
    comment_count    BIGINT       NOT NULL DEFAULT 0,
    share_count      BIGINT       NOT NULL DEFAULT 0,
    badge            VARCHAR(20)  NOT NULL,
    badge_multiplier NUMERIC(5,2) NOT NULL,
    base_amount      BIGINT       NOT NULL DEFAULT 0,
    final_amount     BIGINT       NOT NULL DEFAULT 0,
    formula_id       UUID         NOT NULL REFERENCES payout_formula(id),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reviewer_income UNIQUE (reviewer_id, income_date)
);

CREATE INDEX idx_reviewer_income_date     ON reviewer_income (income_date);
CREATE INDEX idx_reviewer_income_reviewer ON reviewer_income (reviewer_id);

-- ──────────────────────────────────────────────────────────────────────────────
-- admin_payout
-- Monthly payout per reviewer, managed by admin.
-- Upsert on (reviewer_id, payout_month).
-- Status flow: PENDING → APPROVED → PAID (or CANCELLED at any non-terminal step).
-- ──────────────────────────────────────────────────────────────────────────────

CREATE TABLE admin_payout (
    id                 UUID                 NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    reviewer_id        UUID                 NOT NULL REFERENCES reviewers(reviewer_id) ON DELETE CASCADE,
    payout_month       VARCHAR(7)           NOT NULL,   -- format: yyyy-MM
    total_base_amount  BIGINT               NOT NULL DEFAULT 0,
    badge              VARCHAR(20)          NOT NULL,
    badge_multiplier   NUMERIC(5,2)         NOT NULL,
    total_final_amount BIGINT               NOT NULL DEFAULT 0,
    status             admin_payout_status  NOT NULL DEFAULT 'PENDING',
    approved_by        UUID                 REFERENCES users(user_id) ON DELETE SET NULL,
    approved_at        TIMESTAMPTZ,
    paid_at            TIMESTAMPTZ,
    note               TEXT,
    formula_id         UUID                 NOT NULL REFERENCES payout_formula(id),
    created_at         TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_admin_payout UNIQUE (reviewer_id, payout_month)
);

CREATE INDEX idx_admin_payout_month  ON admin_payout (payout_month);
CREATE INDEX idx_admin_payout_status ON admin_payout (status);
