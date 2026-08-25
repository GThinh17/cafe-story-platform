# S2-01 — Changed-file scope

## Backend production

- `AdminReportAiResolutionRequestDTO.java`
- `AdminReportAiPolicyContextRequestDTO.java`
- `AdminReportAiCandidateRuleRequestDTO.java`
- `AdminReportAiRuleRequirementRequestDTO.java`
- `AdminReportAiMissingRequirementRequestDTO.java`
- `AdminReportAiEvidenceItemRequestDTO.java`
- `AdminReportAiPolicyCatalog.java`
- `AdminReportAiResolutionServiceImpl.java`
- `AdminReportAiSemanticValidator.java`

## Backend tests

- `AdminReportAiResolutionServiceImplTest.java`
- `AdminReportAiSemanticValidatorTest.java`

## n8n source/test

- `docker/cafestory-admin-report-ai-resolution-n8n-workflow.json`
- `docker/n8n-code/admin-report-ai-resolution/validate-contract-v2-and-build-request.js`
- `docker/n8n-code/admin-report-ai-resolution/validate-and-normalize-recommendation-v2.js`
- `docker/tests/sync-admin-report-ai-workflow-code.mjs`
- `docker/tests/validate-admin-report-ai-prompt-adversarial.mjs`

## Governance/DD/evidence

- `status.json`
- `MASTER-ROADMAP-CHECKLIST.md`
- `current-handoff.md`
- `00-governance/approval-gates.md`
- `09-sprints/s2-dd-01-runtime-rule-evidence-prompt-evaluation.vi.md`
- package evidence hiện tại.

## Ngoài phạm vi không sửa bởi S2-01

- Admin Next.js source/E2E đang dirty từ package trước;
- database/Flyway;
- mobile;
- Python AI service;
- n8n published runtime và secrets;
- production deployment.
