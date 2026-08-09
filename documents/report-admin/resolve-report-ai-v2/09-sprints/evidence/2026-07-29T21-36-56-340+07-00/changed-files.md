# S2-02 — Changed files

## Production/backend

- `AdminReportAiPolicyCatalog.java`
- `AdminReportAiSemanticValidator.java`

## Backend tests

- `AdminReportAiResolutionServiceImplTest.java`
- `AdminReportAiSemanticValidatorTest.java`

## Canonical contract và n8n

- `docker/contracts/admin-report-ai-runtime-request-s2.schema.json`
- `docker/contracts/admin-report-ai-provider-output-s2.schema.json`
- `docker/n8n-code/admin-report-ai-resolution/validate-contract-v2-and-build-request.js`
- `docker/n8n-code/admin-report-ai-resolution/validate-and-normalize-recommendation-v2.js`
- `docker/cafestory-admin-report-ai-resolution-n8n-workflow.json`
- `docker/tests/validate-admin-report-ai-s2-contracts.mjs`
- `docker/tests/validate-admin-report-ai-prompt-adversarial.mjs`

## Governance/DD/evidence

- `status.json`, `MASTER-ROADMAP-CHECKLIST.md`, `current-handoff.md`
- `00-governance/approval-gates.md`
- `09-sprints/sprint-02-prompt-schema-validator.md`
- `09-sprints/s2-dd-01-runtime-rule-evidence-prompt-evaluation.vi.md`
- evidence package hiện tại.

Worktree có thay đổi từ các package trước và FE files đã tồn tại trước S2-02. Chúng được bảo toàn; danh
sách này chỉ mô tả ownership của package S2-02, không claim toàn bộ `git status` là do lượt này tạo ra.

