# Issue register — DOD-FIX-05

## Baseline

- Gate: `DOD-FIX-05`.
- Traceability: `DOD-18`, `SAF-011`, `E2E-S1-09`.
- Scenario: chọn hai report OPEN trong bulk UI, sau đó chuyển một report sang terminal trước khi
  bấm Run AI để tạo per-item failure thật.
- Expected:
  - bulk tiếp tục xử lý item còn lại;
  - UI tách success và failure;
  - failed item không có resolution;
  - successful item có đúng một recommendation;
  - report/target không bị AI mutation;
  - auto-apply job bằng `0`.
- Production source fix: chưa thực hiện trước baseline.

## FIX05-CONFIG-001

- Classification: `CONFIG_ENV`.
- Evidence: Admin package không có unit-test/coverage script.
- Affected behavior: không thể tuyên bố frontend unit coverage.
- Proposed action: dùng typecheck, production build và focused Playwright E2E.
- Auto-fix allowed: no.
- Status: `DOCUMENTED_LIMITATION`.

## FIX05-CONFIG-002

- Classification: `CONFIG_ENV`.
- Evidence: terminal wrapper từ chối compound Docker start + readiness command trước khi chạy.
- Affected behavior: orchestration local, không phải CafeStory runtime.
- Proposed action: xác minh container name rồi tách exact `docker start` và readiness probe.
- Auto-fix allowed: no.
- Status: `WORKAROUND_VERIFIED`; PostgreSQL và n8n readiness đều pass.

## FIX05-TEST-001

- Classification: `TEST_BUG`.
- Evidence: `npm run typecheck`.
- Actual: browser `page.on("response")` listener bị annotate bằng `APIResponse` của
  `APIRequestContext`, nên TypeScript không có `request()` và overload event không khớp.
- Expected: để Playwright suy luận browser `Response` từ event listener.
- Likely owner: Playwright test harness.
- Proposed action: bỏ annotation sai.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: `npm run typecheck` exit `0`.

## FIX05-TEST-002

- Classification: `TEST_BUG`.
- Evidence: `npm run typecheck`.
- Actual: test gọi `shortId`, helper chỉ có trong Admin component.
- Expected: assertion dùng trực tiếp prefix 8 ký tự của report UUID.
- Likely owner: Playwright test harness.
- Proposed action: thay bằng `report.id.slice(0, 8)`.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: `npm run typecheck` exit `0`.

## FIX05-TEST-003

- Classification: `TEST_BUG`.
- Evidence: combined Playwright command exit `1` trước khi runner khởi động.
- Actual: regex grep chứa `|` bị `npm.cmd` trên Windows diễn giải như shell pipe.
- Expected: chạy hai focused scenario tuần tự bằng hai grep literal.
- Likely owner: verification command.
- Proposed action: tách `E2E-S1-09` và `E2E-S1-10` thành hai command.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: hai Playwright grep literal đều chạy và pass `1/1`.

## FIX05-TEST-004

- Classification: `TEST_BUG`.
- Evidence: visual inspection của
  `screenshots/E2E-S1-09-bulk-partial-failure.png`.
- Actual: screenshot thấy counters partial failure nhưng failure-detail nằm dưới vùng scroll.
- Expected: ảnh evidence phải hiển thị cả counters và alert từng item.
- Likely owner: Playwright screenshot preparation.
- Proposed action: scroll failure alert vào viewport trước khi chụp.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: screenshot sau rerun hiển thị counters và failure-detail.

## FIX05-TEST-005

- Classification: `TEST_BUG`.
- Evidence: traceability review sau E2E.
- Actual: report no-mutation assertion mới so sánh `status`, chưa so sánh toàn DTO ổn định.
- Expected: snapshot report ngay trước bulk và so sánh toàn bộ response fields sau bulk.
- Likely owner: Playwright state assertion.
- Proposed action: thêm canonical full-object comparison cho cả success và failed item.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: canonical report state before/after được ghi vào raw evidence và bằng nhau cho cả hai
  item; focused Playwright `1/1 PASS`.

## FIX05-TEST-006

- Classification: `TEST_BUG`.
- Evidence: rerun `E2E-S1-09` sau full-object assertion.
- Actual: terminal report PATCH response và GET response sau bulk không JSON-equal, trong khi status
  vẫn `RESOLVED`, resolution `0`, target unchanged và auto job `0`.
- Expected: so sánh canonical persisted report state, không so field dẫn xuất/representation không ổn định.
- Likely owner: Playwright snapshot normalization.
- Root cause: baseline của terminal item lấy trực tiếp từ PATCH response trước khi PostgreSQL chuẩn
  hóa `resolvedAt` về microsecond; GET sau đó có thể khác số chữ số thập phân dù persisted value không
  bị AI thay đổi.
- Action: GET lại persisted report ngay sau status PATCH, rồi mới chụp canonical state gồm identity,
  target, reason, description, status và timestamps; ghi before/after vào raw evidence.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: hai persisted canonical snapshot bằng nhau, `reportMutation=false`, Playwright
  `1/1 PASS`.

## Kết luận baseline production

- Không phát hiện `CODE_BUG`.
- Behavior bulk hiện tại đã fail-isolated đúng: một item HTTP `409`, item còn lại HTTP `200`.
- Gap của audit là thiếu executable E2E/evidence, không phải bằng chứng source sai.

## Ngoài phạm vi

- Không thay đổi bulk concurrency/rate limiting.
- Không triển khai production retry queue.
- Không sửa/publish n8n workflow.
- Không deploy production.
