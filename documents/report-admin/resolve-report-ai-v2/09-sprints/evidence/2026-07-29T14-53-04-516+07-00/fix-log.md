# Fix log — DOD-FIX-04

## Baseline

- Đã thêm executable fixture `E2E-S1-13`.
- Chưa sửa production source trước baseline.
- Attempt 1 ghi được raw `502`; cleanup sau đó fail do `blog_events` FK.
- `FIX04-TEST-001`: dependency-ordered cleanup đã verify `0/0`.
- `FIX04-CODE-001`: đã chứng minh Backend chưa trả structured operational-error contract.
- Attempt 2: UI baseline screenshot chứng minh chỉ có generic message, không có code/correlation/
  retry state.
- `FIX04-CODE-002`: đã ghi issue trước khi sửa source.

## Production fix

- Backend:
  - thêm `AdminReportAiProviderBoundaryException` giữ request correlation ID;
  - thêm response DTO chuẩn hóa `502 + AI_PROVIDER_BOUNDARY_FAILED`;
  - thêm exception advice riêng, không làm lộ raw provider response;
  - transport/non-2xx từ n8n được map sang operational error có `retryable=true`.
- Admin:
  - `ApiError` giữ `code`, `correlationId`, `retryable`, `stage`;
  - Ask AI dialog hiển thị operational metadata và hướng dẫn retry;
  - content decision và operational failure không bị nhập làm một.
- E2E harness:
  - dừng/khởi động đúng container `cafestory-n8n`;
  - tạo BLOG/report synthetic;
  - snapshot report/target, kiểm tra resolution/job và cleanup theo dependency.

## Verification

- Focused BE nhỏ nhất: `2/2 PASS`.
- Admin Report AI focused: `64/64 PASS`.
- Full Backend: `625` tests, failure `0`, error `0`, skipped `1`.
- Admin: typecheck `PASS`, production build `PASS`.
- Playwright `E2E-S1-13`: `1/1 PASS`.
- Failure phase: HTTP `502`, structured contract đầy đủ, resolution `0 -> 0`.
- Recovery phase: HTTP `200`, resolution `1`, report/target không mutation, auto job `0`.
- Cleanup: synthetic report và BLOG còn `0/0`.
- Changed BE classes: line `100%`; service branch `87.75%`.
