# Decision and Design References

Primary internal references:

- `00-governance/current-state-audit/current-state-gap-register.md`;
- `01-theoretical-foundation/*`;
- `02-policy-framework/*`;
- `03-rule-catalog/rule-catalog-overview.md`;
- `03-rule-catalog/rule-traceability-matrix.md`;
- `12-decisions/business-decision-review.md`;
- `09-sprints/sprint-01-safety-contract.md`.

Live checkout references used by G0-11:

- Spring Boot `AdminContentReportController`;
- `AdminReportAiResolutionServiceImpl`;
- `AdminReportAiAutoApplyJobServiceImpl` and worker;
- Admin `admin-reports-page.tsx`, API types/client and Playwright E2E;
- `docker/cafestory-admin-report-ai-resolution-n8n-workflow.json`;
- applied migration source `V20260705_01` and `V20260706_01`.

Line numbers from earlier audit may drift; implementation must re-run `rg` and inspect the current
checkout before editing.
