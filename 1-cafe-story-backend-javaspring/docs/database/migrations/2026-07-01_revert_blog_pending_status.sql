BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

-- Reverts 2026-07-01_blog_status_pending_moderation.sql.
-- Async moderation flow (PENDING_MODERATION intermediate status) is dropped;
-- createModeratedBlog is back to synchronous AI call so the DB no longer needs
-- the PENDING_MODERATION value.
--
-- Notification columns (moderation_status, moderation_reason) and the
-- BLOG_MODERATION notification type stay in place: the push-notification
-- feature is still active, only the request lifecycle changed.

-- Any leftover PENDING rows are demoted to HIDDEN so they route to admin
-- review instead of leaking into the public feed.
UPDATE blogs
SET status = 'HIDDEN'
WHERE status = 'PENDING_MODERATION';

ALTER TABLE blogs
    DROP CONSTRAINT IF EXISTS blogs_status_check;

ALTER TABLE blogs
    ADD CONSTRAINT blogs_status_check
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN', 'REMOVED'));

COMMIT;
