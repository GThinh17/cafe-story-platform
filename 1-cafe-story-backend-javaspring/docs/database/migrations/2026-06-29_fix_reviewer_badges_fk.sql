-- Fix sai FK của reviewer_badges.reviewer_id.
-- Tên constraint cũ do Hibernate sinh: fkgtk8dxg6htc3ipjnd42qqvkq → đang point sang users.user_id.
-- Đúng phải point sang reviewers.reviewer_id (theo entity ReviewerBadgeHistory.reviewer).
SET statement_timeout = 0;
SET lock_timeout = '30s';

-- Drop bad FK + new one if it already exists (idempotent)
ALTER TABLE reviewer_badges DROP CONSTRAINT IF EXISTS fkgtk8dxg6htc3ipjnd42qqvkq;
ALTER TABLE reviewer_badges DROP CONSTRAINT IF EXISTS fk_reviewer_badges_reviewer;

ALTER TABLE reviewer_badges
    ADD CONSTRAINT fk_reviewer_badges_reviewer
    FOREIGN KEY (reviewer_id) REFERENCES reviewers(reviewer_id);
