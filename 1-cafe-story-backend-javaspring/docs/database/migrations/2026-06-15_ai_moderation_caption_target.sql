BEGIN;

SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = '10s';

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS caption text;

UPDATE ai_moderation_results moderation_result
SET caption = blog.content
FROM blogs blog
WHERE moderation_result.blog_id = blog.id
  AND moderation_result.caption IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'ai_moderation_results'
          AND column_name = 'comment_id'
    ) THEN
        EXECUTE '
            UPDATE ai_moderation_results moderation_result
            SET caption = comment_entity.content
            FROM comments comment_entity
            WHERE moderation_result.comment_id = comment_entity.id
              AND moderation_result.caption IS NULL
        ';
    END IF;
END $$;

DO $$
DECLARE
    constraint_record record;
BEGIN
    FOR constraint_record IN
        SELECT constraint_info.conname AS constraint_name
        FROM pg_constraint constraint_info
        JOIN pg_attribute attribute
            ON attribute.attrelid = constraint_info.conrelid
           AND attribute.attnum = ANY(constraint_info.conkey)
        WHERE constraint_info.conrelid = 'ai_moderation_results'::regclass
          AND attribute.attname = 'comment_id'
    LOOP
        EXECUTE format(
            'ALTER TABLE ai_moderation_results DROP CONSTRAINT IF EXISTS %I',
            constraint_record.constraint_name
        );
    END LOOP;
END $$;

DO $$
DECLARE
    index_record record;
BEGIN
    FOR index_record IN
        SELECT index_class.relname AS index_name
        FROM pg_index index_info
        JOIN pg_class table_class
            ON table_class.oid = index_info.indrelid
        JOIN pg_class index_class
            ON index_class.oid = index_info.indexrelid
        JOIN pg_attribute attribute
            ON attribute.attrelid = table_class.oid
           AND attribute.attnum = ANY(index_info.indkey)
        WHERE table_class.relname = 'ai_moderation_results'
          AND attribute.attname = 'comment_id'
    LOOP
        EXECUTE format('DROP INDEX IF EXISTS %I', index_record.index_name);
    END LOOP;
END $$;

ALTER TABLE ai_moderation_results
    DROP COLUMN IF EXISTS comment_id;

COMMIT;
