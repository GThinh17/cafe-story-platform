BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

-- notifications.type has a CHECK constraint restricting values to the original
-- 6-member NotificationType enum. The new BLOG_MODERATION type is rejected by
-- Postgres before the row can be inserted, aborting the outer transaction that
-- also writes ai_moderation_results.
--
-- Drop and recreate the constraint with the extra value.

ALTER TABLE notifications
    DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications
    ADD CONSTRAINT notifications_type_check
    CHECK (type IN ('LIKE', 'SHARE', 'COMMENT', 'MESSAGE', 'FOLLOW', 'TAG', 'BLOG_MODERATION'));

COMMIT;
