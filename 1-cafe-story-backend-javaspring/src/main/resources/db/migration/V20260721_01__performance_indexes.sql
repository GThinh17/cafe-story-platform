-- Performance indexes for admin ranking/payout aggregation and admin cafe-page filter.
--
-- Ranking generation, daily income and monthly payout run GROUP BY user_id queries
-- filtered by a created_at range. The existing composite indexes lead with user_id
-- or blog_id, so a pure created_at range predicate falls back to a full table scan.
-- These (created_at, user_id) composites let the aggregate use an index-only scan.
--
-- reviewer_income already has a unique constraint on (reviewer_id, income_date) that
-- can serve income lookups by reviewer, but not the payout gap-detection query
-- `select distinct income_date where income_date between ...`; the standalone index
-- on income_date fixes that.
--
-- cafe_pages admin list filters by status and (optionally) owner_user_id — no index
-- exists on either column today.

CREATE INDEX IF NOT EXISTS idx_blog_likes_created_at_user
    ON blog_likes (created_at, user_id);

CREATE INDEX IF NOT EXISTS idx_blog_shares_created_at_user
    ON blog_shares (created_at, user_id);

CREATE INDEX IF NOT EXISTS idx_comments_created_at_user
    ON comments (created_at, user_id);

CREATE INDEX IF NOT EXISTS idx_reviewer_income_income_date
    ON reviewer_income (income_date);

CREATE INDEX IF NOT EXISTS idx_cafe_pages_status_owner
    ON cafe_pages (status, owner_user_id);
