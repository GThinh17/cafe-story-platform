ALTER TABLE admin_report_ai_resolutions
    ADD COLUMN IF NOT EXISTS contract_version VARCHAR(32),
    ADD COLUMN IF NOT EXISTS correlation_id UUID,
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(64),
    ADD COLUMN IF NOT EXISTS automation_mode VARCHAR(40),
    ADD COLUMN IF NOT EXISTS policy_version VARCHAR(80),
    ADD COLUMN IF NOT EXISTS rule_catalog_version VARCHAR(80),
    ADD COLUMN IF NOT EXISTS prompt_version VARCHAR(80),
    ADD COLUMN IF NOT EXISTS workflow_version VARCHAR(80),
    ADD COLUMN IF NOT EXISTS target_snapshot_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS evidence_quality VARCHAR(24),
    ADD COLUMN IF NOT EXISTS evidence_sufficiency VARCHAR(24),
    ADD COLUMN IF NOT EXISTS violation_likelihood VARCHAR(24),
    ADD COLUMN IF NOT EXISTS harm_severity VARCHAR(24),
    ADD COLUMN IF NOT EXISTS action_risk VARCHAR(24),
    ADD COLUMN IF NOT EXISTS findings_json JSONB,
    ADD COLUMN IF NOT EXISTS evidence_summary_json JSONB,
    ADD COLUMN IF NOT EXISTS blocked_reasons_json JSONB;

CREATE UNIQUE INDEX IF NOT EXISTS ux_admin_report_ai_resolutions_correlation_v2
    ON admin_report_ai_resolutions(correlation_id)
    WHERE correlation_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_admin_report_ai_resolutions_idempotency_v2
    ON admin_report_ai_resolutions(idempotency_key)
    WHERE idempotency_key IS NOT NULL;

ALTER TABLE admin_report_ai_resolutions
    ADD CONSTRAINT chk_admin_report_ai_contract_version
        CHECK (contract_version IS NULL OR contract_version IN ('2.0', 'legacy-v1')),
    ADD CONSTRAINT chk_admin_report_ai_automation_mode
        CHECK (automation_mode IS NULL OR automation_mode = 'A0_RECOMMEND_ONLY'),
    ADD CONSTRAINT chk_admin_report_ai_evidence_quality
        CHECK (evidence_quality IS NULL OR evidence_quality IN ('HIGH', 'MEDIUM', 'LOW', 'UNUSABLE')),
    ADD CONSTRAINT chk_admin_report_ai_evidence_sufficiency
        CHECK (evidence_sufficiency IS NULL OR evidence_sufficiency IN ('SUFFICIENT', 'INSUFFICIENT', 'CONFLICTED', 'UNASSESSABLE')),
    ADD CONSTRAINT chk_admin_report_ai_violation_likelihood
        CHECK (violation_likelihood IS NULL OR violation_likelihood IN ('HIGH', 'MEDIUM', 'LOW', 'UNKNOWN')),
    ADD CONSTRAINT chk_admin_report_ai_harm_severity
        CHECK (harm_severity IS NULL OR harm_severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN')),
    ADD CONSTRAINT chk_admin_report_ai_action_risk
        CHECK (action_risk IS NULL OR action_risk IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')),
    ADD CONSTRAINT chk_admin_report_ai_manual_no_action
        CHECK (report_decision <> 'NEEDS_MANUAL_REVIEW' OR target_action IN ('NO_ACTION', 'NONE'));
