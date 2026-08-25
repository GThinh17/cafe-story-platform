CREATE TABLE IF NOT EXISTS report_moderation_jobs (
    id UUID PRIMARY KEY,
    content_report_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    next_attempt_at TIMESTAMP NOT NULL,
    processing_started_at TIMESTAMP,
    last_error TEXT,
    last_duration_ms BIGINT,
    priority_score DOUBLE PRECISION,
    risk_score DOUBLE PRECISION,
    reason_severity_signal DOUBLE PRECISION,
    report_count_signal DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_report_moderation_jobs_content_report
        FOREIGN KEY (content_report_id) REFERENCES content_reports(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_report_moderation_jobs_content_report
    ON report_moderation_jobs(content_report_id);

CREATE INDEX IF NOT EXISTS ix_report_moderation_jobs_claim
    ON report_moderation_jobs(status, next_attempt_at, priority_score DESC, created_at);

CREATE INDEX IF NOT EXISTS ix_report_moderation_jobs_target
    ON report_moderation_jobs(target_type, target_id);

ALTER TABLE ai_moderation_results
    ADD COLUMN IF NOT EXISTS content_report_id UUID,
    ADD COLUMN IF NOT EXISTS priority_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS risk_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS reason_severity_signal DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS report_count_signal DOUBLE PRECISION;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_ai_moderation_results_content_report'
          AND table_name = 'ai_moderation_results'
    ) THEN
        ALTER TABLE ai_moderation_results
            ADD CONSTRAINT fk_ai_moderation_results_content_report
            FOREIGN KEY (content_report_id) REFERENCES content_reports(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_moderation_results_content_report
    ON ai_moderation_results(content_report_id)
    WHERE content_report_id IS NOT NULL;
