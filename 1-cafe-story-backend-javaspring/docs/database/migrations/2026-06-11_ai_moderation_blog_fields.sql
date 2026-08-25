BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS blog_id uuid;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS caption_score integer;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS caption_reason text;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS image_score integer;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS image_reason text;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS tags jsonb;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS ai_status varchar(50);

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS raw_response jsonb;

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS updated_at timestamp;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_ai_moderation_results_blog'
    ) THEN
        ALTER TABLE ai_moderation_results
            ADD CONSTRAINT fk_ai_moderation_results_blog
            FOREIGN KEY (blog_id)
            REFERENCES blogs(id)
            ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_ai_moderation_results_blog_id
    ON ai_moderation_results(blog_id);

CREATE INDEX IF NOT EXISTS idx_ai_moderation_results_ai_status
    ON ai_moderation_results(ai_status);

COMMIT;
