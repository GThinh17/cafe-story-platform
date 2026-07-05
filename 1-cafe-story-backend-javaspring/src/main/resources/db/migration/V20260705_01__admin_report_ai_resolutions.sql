CREATE TABLE IF NOT EXISTS admin_report_ai_resolutions (
    id UUID PRIMARY KEY,
    content_report_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    report_decision VARCHAR(50) NOT NULL,
    target_action VARCHAR(50) NOT NULL,
    confidence_score DOUBLE PRECISION,
    risk_score DOUBLE PRECISION,
    labels JSONB,
    rule_code VARCHAR(120),
    explanation TEXT,
    model_name VARCHAR(120),
    raw_response JSONB,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_admin_report_ai_resolutions_content_report
        FOREIGN KEY (content_report_id) REFERENCES content_reports(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_admin_report_ai_resolutions_report_created
    ON admin_report_ai_resolutions(content_report_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_admin_report_ai_resolutions_target
    ON admin_report_ai_resolutions(target_type, target_id);
