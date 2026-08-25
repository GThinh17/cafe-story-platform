# Fix log

## LAYOUT-001

- Moved the AI recommendation history section into `report-detail-main-column`.
- Kept the latest AI recommendation/evidence section as `report-ai-evidence-column`.
- Added stable test IDs for layout geometry checks.
- Added desktop and mobile Playwright assertions plus dedicated screenshots.
- Focused E2E result after fix: `1/1 PASS` for BLOG and COMMENT.

No API, database, n8n, policy, permission, report action, or recommendation-only behavior changed.
