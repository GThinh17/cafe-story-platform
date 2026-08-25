BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

-- Add PENDING_MODERATION status for blogs awaiting async AI moderation.
-- Blogs land here immediately after POST /api/blogs/moderated and are
-- transitioned to PUBLISHED / HIDDEN / REMOVED once moderateBlogAsync finishes.
-- Nothing to do at the enum-column level because Postgres stores it as text via
-- Hibernate @Enumerated(EnumType.STRING); listed here for documentation only.

-- Add BLOG_MODERATION notification type + moderation payload columns.
-- moderation_status:  "APPROVED" | "DENIED" | "SEND_ADMIN" (nullable, only set
--                     for type = BLOG_MODERATION)
-- moderation_reason:  human-readable Vietnamese reason shown in the
--                     ModerationReasonDialog on click (nullable, only set for
--                     denied / send-admin outcomes).
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS moderation_status varchar(32),
    ADD COLUMN IF NOT EXISTS moderation_reason varchar(1024);

-- Fast lookup of a user's moderation notifications (bell dropdown filter tab).
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_type_created
    ON notifications (recipient_id, type, created_at DESC);

COMMIT;
