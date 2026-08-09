# Báo cáo đánh giá — DOD-FIX-01

## Kết luận

- Trạng thái: `COMPLETED_VERIFIED_APPROVED`.
- Approval: `APPROVE_G0-12-DONE-FIX-01`.
- ADV executable matrix: `12/12 PASS`.
- Hai `CODE_BUG` được phát hiện bằng baseline test và đã sửa:
  1. prior AI/reporter claim có thể làm evidence dương duy nhất;
  2. allowlist bỏ sót `missingEvidenceIds`/`evidenceSummary`.
- Không đóng `G0-12-DONE`; còn `DOD-FIX-02`–`DOD-FIX-06`.

## Phạm vi thay đổi

- Backend semantic guard và unit test.
- n8n workflow export canonical.
- deterministic ADV fixture/test harness.
- DD, traceability và evidence của DOD-FIX-01.

Không thay đổi UI, database, secret, published n8n runtime hoặc production.

## Verification

| Tiêu chí | Lệnh/phương pháp | Expected | Actual |
|---|---|---|---|
| ADV-001–012 thực thi | `node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs` | 12 pass | `12/12 PASS` |
| n8n security không regression | `node docker/tests/validate-admin-report-ai-security.mjs` | pass | `PASS` |
| Backend semantic focused | `mvn '-Dtest=AdminReportAiSemanticValidatorTest' test` | pass | `12/12 PASS` |
| Admin Report AI focused | `mvn '-Dtest=AdminReportAiSemanticValidatorTest,AdminReportAiResolutionServiceImplTest,AdminReportAiWebhookSignerTest,AdminReportAiAutoApplyJobServiceImplTest' test` | pass | `60/60 PASS` |
| Backend regression | `mvn test` | 0 fail/error | `621`, fail `0`, error `0`, skipped `1` |
| Changed Java class coverage | JaCoCo XML | line 100%, branch ≥85% | line `100%`, branch `96.61%` |
| Workflow JSON | JSON parse | pass | `PASS` |
| Project structure | `check_project.py` | no warning | `PASS`, warnings `[]` |
| Secret scan | credential-pattern scan trên changed files | 0 hit | `0` |
| Diff hygiene | `git diff --check` | không whitespace error | `PASS` |

## Evidence

- Traceability: `../../../../10-verification/prompt-adversarial-tests.md`.
- DD: `../../g0-12-done-fix-01-detailed-design.vi.md`.
- Issue register: `issue.md`.
- Fix log: `fix-log.md`.
- Machine summary: `summary.json`.
- Raw matrix: `raw/adv-traceability.tsv`.
- Coverage summary: `coverage/admin-report-ai-semantic-validator.txt`.

## Chưa kiểm chứng

- Không gọi OpenAI thật nên không tuyên bố chống được mọi biến thiên model.
- Không publish/cập nhật n8n runtime.
- Full suite skip một PostgreSQL integration test cần môi trường riêng.
- Không kiểm tra UI trong gate này.
