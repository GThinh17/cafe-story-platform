# Issue register — DOD-FIX-06

## Baseline

- Gate: `DOD-FIX-06`.
- Traceability: `DOD-19`, `SAF-012`, `E2E-S1-10`.
- Scenario: tạo report synthetic, chuyển sang terminal `RESOLVED`, mở report detail trên Admin UI
  và thử gọi Backend trực tiếp.
- Expected:
  - nút Ask AI trên FE bị disabled;
  - không có AI request phát sinh từ UI;
  - Backend reject direct request;
  - không tạo resolution/auto-apply job;
  - report/target không mutation.
- Production source fix: chưa thực hiện trước baseline.

## FIX06-CONFIG-001

- Classification: `CONFIG_ENV`.
- Evidence: Admin package không có unit-test/coverage script.
- Affected behavior: không thể tuyên bố frontend unit coverage.
- Proposed action: dùng typecheck, production build và focused Playwright E2E.
- Auto-fix allowed: no.
- Status: `DOCUMENTED_LIMITATION`.

## FIX06-TEST-001

- Classification: `TEST_BUG`.
- Evidence: traceability review của no-mutation criterion.
- Actual: assertion ban đầu chỉ kiểm report vẫn `RESOLVED`, chưa so toàn state do report sở hữu.
- Expected: canonical persisted report state trước/sau Backend bypass phải bằng nhau.
- Likely owner: Playwright state assertion.
- Action: thêm canonical snapshot before/after và `reportMutation` vào raw evidence.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: `reportMutation=false`, focused Playwright `1/1 PASS`.

## FIX06-TEST-002

- Classification: `TEST_BUG`.
- Evidence: strict assertion rerun từng fail dù mọi field khác giống nhau.
- Actual: PATCH response có `resolvedAt` 7 chữ số thập phân; PostgreSQL lưu microsecond và GET trả
  6 chữ số, làm baseline trước persistence khác representation sau persistence.
- Expected: đo AI no-mutation từ persisted baseline, không từ pre-normalization PATCH response.
- Likely owner: Playwright fixture baseline.
- Action: GET lại report ngay sau status PATCH rồi mới chụp canonical baseline.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: persisted before/after bằng nhau, `reportMutation=false`, Playwright `1/1 PASS`.

## Ngoài phạm vi

- Không đổi danh sách terminal status.
- Không thay đổi Backend eligibility policy.
- Không deploy production.

## Kết luận baseline production

- Không phát hiện `CODE_BUG`.
- FE hiện tại đã disable Ask AI cho `RESOLVED`.
- DOM click trên disabled button không phát sinh request.
- Backend direct bypass trả HTTP `409`.
- Gap của audit là thiếu assertion/evidence riêng cho FE và report-owned state; đã bổ sung, không còn
  open issue trong phạm vi.
