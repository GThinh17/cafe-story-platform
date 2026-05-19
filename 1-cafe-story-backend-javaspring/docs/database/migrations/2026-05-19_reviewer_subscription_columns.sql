-- PostgreSQL migration proposal for reviewer subscription columns.
-- Apply manually because this project does not currently use Flyway or Liquibase.

ALTER TABLE reviewers
ADD COLUMN IF NOT EXISTS reviewer_active boolean NOT NULL DEFAULT false;

ALTER TABLE reviewers
ADD COLUMN IF NOT EXISTS reviewer_expires_at timestamp;
