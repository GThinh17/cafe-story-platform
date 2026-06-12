-- PostgreSQL migration proposal for CafeStory chat message read state.
-- Apply manually because this project does not currently use Flyway or Liquibase.
-- This is safe to run whether Hibernate already created the columns or not.

ALTER TABLE chat_messages
    ADD COLUMN IF NOT EXISTS is_read boolean;

UPDATE chat_messages
SET is_read = false
WHERE is_read IS NULL;

ALTER TABLE chat_messages
    ALTER COLUMN is_read SET DEFAULT false;

ALTER TABLE chat_messages
    ALTER COLUMN is_read SET NOT NULL;

ALTER TABLE chat_messages
    ADD COLUMN IF NOT EXISTS read_at timestamp;
