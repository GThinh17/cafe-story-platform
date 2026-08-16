# Báo cáo đánh giá AI Report Resolution admin actions

## Phạm vi

Đánh giá endpoint chính sách read-only, giao diện xử lý BLOG/COMMENT, Policy Sheet, ranh giới recommendation-only, lập luận AI dựa trên text/Evidence ID và routing ngôn ngữ. Không thay đổi database, quyền admin, request contract hoặc cơ chế quyết định cuối của admin/backend.

## Kết quả runtime

- Workflow `cafestory-admin-report-ai-resolution-v2` đã được backup, import, publish riêng và xác nhận active.
- Container `cafestory-n8n` vẫn chạy tại cổng `5678`; webhook thật và OpenAI thật pass ba case ngôn ngữ.
- Policy endpoint `GET /api/admin/reports/{reportId}/ai-policy` trả candidate rules từ runtime catalog và `recommendationOnly: true`.
- E2E thật pass cho BLOG và COMMENT:
  - tải nội dung/trạng thái hiện tại từ Admin API;
  - mở Policy Sheet và đọc Rule ID/version kỹ thuật;
  - đổi `PUBLISHED → HIDDEN` chỉ sau confirm;
  - report status không đổi;
  - đổi lại `HIDDEN → PUBLISHED` và xác nhận target được khôi phục;
  - các report test được đóng `REJECTED` trong cleanup;
  - Ask AI không tự đổi BLOG target/report;
  - focus bàn phím/Enter, light/dark, desktop/mobile và horizontal overflow đều được kiểm tra.

## Kiểm thử

- n8n workflow sync: `PASS`
- Contract V2/schema boundaries: `7/7 PASS`
- Language routing: English, Vietnamese và description precedence đều `PASS`
- Prompt adversarial: `12/12 PASS`
- HMAC/replay/tamper/signed response: `PASS`
- Real n8n/OpenAI runtime: `3/3 PASS`
- Backend focused tests: `41/41 PASS`
- Backend full Maven regression: `1369` tests, `0` failures, `0` errors, `1` skipped
- Backend changed executable lines: `44/44` covered; changed branches: `2/2` covered
- Admin `i18n:check`, `typecheck`, production `build`: `PASS`
- Focused Playwright E2E: `1/1 PASS`
- `git diff --check`: không có whitespace error; chỉ có cảnh báo CRLF của Git trên Windows.

## Evidence

- Runtime language: `../../../ai-report-language-routing/evidence/2026-08-15T08-11-16-314Z/`
- Workflow backup/export: `../2026-08-15T07-43-30-920Z/raw/`
- E2E summary: `raw/report-actions-policy-summary.json`
- Screenshots: `screenshots/`
- Coverage: `coverage/backend-policy-coverage.md`

Không có secret được ghi vào các summary hoặc screenshot. Workflow export chứa source code cấu hình nhưng không chứa giá trị HMAC/OpenAI secret.

## Giới hạn đã biết

- Admin chưa có unit/line coverage instrumentation: `FRONTEND_COVERAGE_TOOLING_MISSING`.
- Broad evaluation dataset vẫn bị chặn bởi checksum drift có sẵn và không được thay đổi trong task này.
