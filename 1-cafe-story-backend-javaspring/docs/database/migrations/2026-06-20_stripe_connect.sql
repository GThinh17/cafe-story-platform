-- Migration: Stripe Connect payout
-- Created: 2026-06-20
-- Tables: reviewer_stripe_accounts
-- Alter:  admin_payout (add stripe_transfer_id, stripe_idempotency_key)

-- ──────────────────────────────────────────────────────────────────────────────
-- reviewer_stripe_accounts
-- Stores Stripe Express account info per reviewer.
-- onboarding_status: PENDING | INCOMPLETE | COMPLETE
-- ──────────────────────────────────────────────────────────────────────────────

CREATE TABLE reviewer_stripe_accounts (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    reviewer_id         UUID          NOT NULL UNIQUE REFERENCES reviewers(reviewer_id) ON DELETE CASCADE,
    stripe_account_id   VARCHAR(100)  NOT NULL UNIQUE,
    onboarding_status   VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    charges_enabled     BOOLEAN       NOT NULL DEFAULT FALSE,
    payouts_enabled     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviewer_stripe_accounts_reviewer ON reviewer_stripe_accounts (reviewer_id);

-- ──────────────────────────────────────────────────────────────────────────────
-- admin_payout: add Stripe transfer tracking columns
-- stripe_idempotency_key prevents double transfer if updatePayoutStatus retries
-- ──────────────────────────────────────────────────────────────────────────────

ALTER TABLE admin_payout
    ADD COLUMN IF NOT EXISTS stripe_transfer_id    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS stripe_idempotency_key VARCHAR(200);
