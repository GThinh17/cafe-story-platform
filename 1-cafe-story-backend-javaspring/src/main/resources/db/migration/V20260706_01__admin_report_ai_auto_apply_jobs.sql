CREATE TABLE IF NOT EXISTS admin_report_ai_auto_apply_jobs (
    id UUID PRIMARY KEY,
    content_report_id UUID NOT NULL,
    ai_resolution_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    report_decision VARCHAR(50) NOT NULL,
    target_action VARCHAR(50) NOT NULL,
    confidence_score DOUBLE PRECISION,
    risk_score DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    applied_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_by_admin_user_id UUID,
    cancelled_by_admin_user_id UUID,
    cancellation_reason VARCHAR(160),
    last_error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_admin_report_ai_auto_jobs_content_report
        FOREIGN KEY (content_report_id) REFERENCES content_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_report_ai_auto_jobs_resolution
        FOREIGN KEY (ai_resolution_id) REFERENCES admin_report_ai_resolutions(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_report_ai_auto_jobs_created_by
        FOREIGN KEY (created_by_admin_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_admin_report_ai_auto_jobs_cancelled_by
        FOREIGN KEY (cancelled_by_admin_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_report_ai_auto_jobs_report_created
    ON admin_report_ai_auto_apply_jobs(content_report_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_admin_report_ai_auto_jobs_status_scheduled
    ON admin_report_ai_auto_apply_jobs(status, scheduled_at);

CREATE INDEX IF NOT EXISTS idx_admin_report_ai_auto_jobs_target
    ON admin_report_ai_auto_apply_jobs(target_type, target_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_admin_report_ai_auto_jobs_active_report
    ON admin_report_ai_auto_apply_jobs(content_report_id)
    WHERE status IN ('SCHEDULED', 'APPLYING');
