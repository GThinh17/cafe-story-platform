BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

-- The blogs.status column has a CHECK constraint restricting values to the
-- original PostStatus enum (DRAFT, PUBLISHED, HIDDEN, REMOVED). The async
-- moderation flow now writes PENDING_MODERATION immediately after blog
-- creation, which the old constraint rejects. Drop and recreate the constraint
-- with the extra value.

ALTER TABLE blogs
    DROP CONSTRAINT IF EXISTS blogs_status_check;

ALTER TABLE blogs
    ADD CONSTRAINT blogs_status_check
    CHECK (status IN ('DRAFT', 'PENDING_MODERATION', 'PUBLISHED', 'HIDDEN', 'REMOVED'));

COMMIT;
