# Issue register — DOD-FIX-04

## Baseline

- Gate: `DOD-FIX-04`.
- Scenario: `E2E-S1-13` — provider boundary unavailable.
- Fault injection: dừng chính xác local container `cafestory-n8n` trong lúc Admin UI gửi Ask AI,
  sau đó khởi động lại và retry.
- Expected failure contract:
  `502 + AI_PROVIDER_BOUNDARY_FAILED + retryable=true + stage=N8N_PROVIDER + correlationId`.
- Expected UI: operational error rõ ràng, support reference, retry available.
- Expected safety: failure không persist resolution, không mutation report/target, không auto job.
- Source fix: chưa thực hiện trước baseline.

## FIX04-CONFIG-001

- Classification: `CONFIG_ENV`.
- Evidence: Admin package không có unit-test/coverage script.
- Affected behavior: không thể tuyên bố frontend unit coverage.
- Proposed action: dùng typecheck, production build và focused Playwright E2E theo coverage policy.
- Auto-fix allowed: no.
- Status: `DOCUMENTED_LIMITATION`.

## FIX04-TEST-001

- Classification: `TEST_BUG`.
- Evidence: Playwright baseline attempt 1 và PostgreSQL FK
  `blog_events.blog_id -> blogs.id`.
- Actual: background analytics tạo một `blog_events` row cho synthetic BLOG; cleanup xóa BLOG
  trước dependency nên exception che assertion contract.
- Expected: cleanup toàn bộ dependency theo đúng blog ID trước khi xóa BLOG và luôn khôi phục n8n.
- Affected behavior: độ tin cậy/khả năng tái chạy của E2E harness; chưa phải production bug.
- Likely owner: Playwright fixture.
- Proposed action: dependency-ordered cleanup cho các bảng tham chiếu BLOG.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: baseline attempt 2 cleanup còn `0/0`, n8n được khôi phục.

## FIX04-CODE-001

- Classification: `CODE_BUG`.
- Evidence: `raw/E2E-S1-13-provider-boundary-failure.json`.
- Actual: Backend trả `502` nhưng body chỉ có envelope chung và message
  `Admin report AI resolution service unavailable`; thiếu `code`, `correlationId`, `retryable`
  và `stage`.
- Expected: error contract có
  `AI_PROVIDER_BOUNDARY_FAILED`, canonical safe message, request correlation ID,
  `retryable=true`, `stage=N8N_PROVIDER`.
- Affected behavior: error contract Sprint 1 và `E2E-S1-13`.
- Likely owner: Backend provider boundary + exception advice.
- Proposed action: typed operational exception/response riêng cho provider transport/non-2xx.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification:
  - focused Backend `2/2 PASS`;
  - Admin Report AI focused `64/64 PASS`;
  - E2E failure response có đủ canonical message, code, request correlation ID,
    `retryable=true`, `stage=N8N_PROVIDER`;
  - resolution count vẫn `0 -> 0` khi n8n unavailable.

## FIX04-CODE-002

- Classification: `CODE_BUG`.
- Evidence:
  `screenshots/E2E-S1-13-provider-boundary-operational-error.png`,
  Playwright baseline attempt 2.
- Actual: UI chỉ hiển thị message chung; không hiển thị error code, support correlation reference,
  stage hoặc retryable state.
- Expected: UI phân biệt operational error với content decision, hiển thị safe message,
  `AI_PROVIDER_BOUNDARY_FAILED`, correlation ID và `Retry available`; Ask AI được enable lại.
- Affected behavior: `E2E-S1-13` và Frontend Error Contract.
- Likely owner: Admin API error parsing + Reports Ask AI dialog.
- Proposed action: preserve typed fields trong `ApiError` và render operational metadata.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification:
  - Admin typecheck và production build `PASS`;
  - Playwright `E2E-S1-13` `1/1 PASS`;
  - screenshot hiển thị safe message, error code, stage, support reference và `Retry available`;
  - Ask AI được enable lại và retry sau recovery thành công.

## Ngoài phạm vi

- Không triển khai DOD-FIX-05/06.
- Không thay đổi secret hoặc production data.
- Không publish workflow và không deploy production.
