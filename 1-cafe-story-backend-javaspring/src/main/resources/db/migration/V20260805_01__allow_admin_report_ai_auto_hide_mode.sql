ALTER TABLE admin_report_ai_resolutions
    DROP CONSTRAINT IF EXISTS chk_admin_report_ai_automation_mode;

ALTER TABLE admin_report_ai_resolutions
    ADD CONSTRAINT chk_admin_report_ai_automation_mode
        CHECK (
            automation_mode IS NULL
            OR automation_mode IN ('A0_RECOMMEND_ONLY', 'A1_AUTO_HIDE_BLOG_COMMENT')
        );
