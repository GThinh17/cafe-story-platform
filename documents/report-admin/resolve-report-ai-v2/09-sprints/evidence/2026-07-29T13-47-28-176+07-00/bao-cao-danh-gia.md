# Báo cáo đánh giá — DOD-FIX-02

## Kết luận

- Trạng thái: `COMPLETED_VERIFIED_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-02`.
- Hai `CODE_BUG` đã được chứng minh bằng baseline test đỏ và sửa:
  1. cache của snapshot cũ vẫn có thể được reuse khi target đã đổi;
  2. target đổi trong lúc provider xử lý nhưng output cũ vẫn có thể được persist.
- CT-010 và SAF-010 hiện có executable tests ở trust boundary Backend.
- `DOD-FIX-02` đã được đóng sau khi nhận approval riêng.
- Không đóng `G0-12-DONE`; còn `DOD-FIX-03`–`DOD-FIX-06`.

## Phạm vi thay đổi

- Backend freshness/idempotency guard trong `AdminReportAiResolutionServiceImpl`.
- Unit/regression tests trong `AdminReportAiResolutionServiceImplTest`.
- DD, CT/SAF traceability, roadmap và evidence của DOD-FIX-02.

Không thay đổi FE, n8n, database schema, secret, provider runtime hoặc production.

## Verification

| Tiêu chí | Lệnh/phương pháp | Expected | Actual |
|---|---|---|---|
| Baseline changed-cache | explicit JUnit test trước source fix | fail để chứng minh gap | `FAIL` đúng kỳ vọng |
| Baseline provider-window stale | explicit JUnit test trước source fix | fail để chứng minh gap | `FAIL` đúng kỳ vọng |
| Service focused | `mvn clean '-Dtest=AdminReportAiResolutionServiceImplTest' test` | pass | `25/25 PASS` |
| Admin Report AI focused | `mvn '-Dtest=AdminReportAiSemanticValidatorTest,AdminReportAiResolutionServiceImplTest,AdminReportAiWebhookSignerTest,AdminReportAiAutoApplyJobServiceImplTest' test` | pass | `63/63 PASS` |
| Backend regression | `mvn test` | 0 fail/error | `624`, fail `0`, error `0`, skipped `1` |
| Spring/JPA wiring | full suite context startup | context pass | `PASS` |
| Changed Java class coverage | JaCoCo XML | line 100%, branch >=85% | line `458/458 = 100%`, branch `172/196 = 87.76%` |
| Project structure | `check_project.py` | no warning | `PASS`, warnings `[]` |
| Secret scan | OpenAI credential-pattern scan | 0 hit | `0` |
| Diff hygiene | `git diff --check` | no whitespace error | `PASS`; chỉ có LF/CRLF warning |

## Evidence

- DD: `../../g0-12-done-fix-02-detailed-design.vi.md`.
- Issue register: `issue.md`.
- Fix log: `fix-log.md`.
- Machine summary: `summary.json`.
- Traceability: `raw/ct010-saf010-traceability.tsv`.
- Coverage: `coverage/admin-report-ai-resolution-service.txt`.

## Chưa kiểm chứng

- Chưa chạy race thật trên nhiều Backend instance hoặc unique-conflict đồng thời.
- Không chạy UI/E2E, n8n hay provider thật vì DOD-FIX-02 là Backend snapshot freshness gate.
- Một PostgreSQL integration test trong full suite bị skip theo điều kiện môi trường hiện có.
- Chưa production deploy; không được phép trong gate này.
