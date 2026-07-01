BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

-- Rollback for:
--   2026-07-01_blog_moderation_notification.sql
--   2026-07-01_blog_status_pending_moderation.sql
--
-- Any blogs currently in PENDING_MODERATION would violate the restored
-- blogs_status_check constraint, so demote them to HIDDEN first (admin can
-- decide manually). BLOG_MODERATION notifications remain intact but their
-- moderation_status / moderation_reason columns will be dropped.

UPDATE blogs
SET status = 'HIDDEN'
WHERE status = 'PENDING_MODERATION';

-- Delete moderation notifications since their payload columns are being
-- removed. Comment out if you prefer to keep the rows (they will just lose
-- their moderation_status / moderation_reason values).
DELETE FROM notifications
WHERE type = 'BLOG_MODERATION';

DROP INDEX IF EXISTS idx_notifications_recipient_type_created;

ALTER TABLE notifications
    DROP COLUMN IF EXISTS moderation_reason,
    DROP COLUMN IF EXISTS moderation_status;

ALTER TABLE blogs
    DROP CONSTRAINT IF EXISTS blogs_status_check;

ALTER TABLE blogs
    ADD CONSTRAINT blogs_status_check
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN', 'REMOVED'));

COMMIT;
