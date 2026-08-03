# Fix log — DOD-FIX-03

## Baseline

- Đã thêm fixture/test `E2E-S1-04`.
- Chưa sửa production source trước khi chạy baseline.

## FIX03-TEST-001

- Baseline attempt 1: `FAIL` tại cleanup do FK runtime không cascade.
- Classification: `TEST_BUG`.
- Fix: dependency-ordered cleanup và early sanitized raw capture.
- Kết quả: harness ghi được runtime result; cleanup `0/0/0`.

## FIX03-CODE-001

- Baseline E2E attempt 2: `FAIL` vì thiếu deterministic critical-context guard.
- Response vẫn manual/no-action do model, nhưng blocked reason/sufficiency không đúng contract.
- Đã chuẩn hóa usable parent-context predicate tại packet builder.
- Unit regression `TC015`: `FAIL` trước sửa, `PASS` sau sửa.
- E2E attempt 3: đã có `CRITICAL_EVIDENCE_MISSING`, không mutation, không auto-apply.

## FIX03-CODE-002

- E2E attempt 3: `FAIL` vì persisted/UI evidence sufficiency là `INSUFFICIENT`, chưa phải
  `UNASSESSABLE`.
- Classification: `CODE_BUG`.
- Root cause: semantic validator chỉ đổi `SUFFICIENT -> UNASSESSABLE`; khi provider trả
  `INSUFFICIENT`, giá trị đó được giữ lại dù critical evidence bị thiếu.
- Fix: semantic validator dùng cùng cờ `criticalEvidenceMissing` để thêm blocked reason và bắt
  buộc sufficiency thành `UNASSESSABLE`.
- Unit regression: `FAIL` trước sửa, `PASS` sau sửa.
- Full-path E2E attempt 4: `1/1 PASS`.

## Final verification

- Service + semantic: `37/37 PASS`.
- Admin Report AI focused: `63/63 PASS`.
- Full Backend: `624`, fail/error `0`, skipped `1`.
- Admin typecheck/build: `PASS`.
- E2E-S1-04: `1/1 PASS`.
- Target mutation: `false`; auto-apply job: `0`; cleanup: `0/0/0`.
- Changed production coverage:
  - `AdminReportAiResolutionServiceImpl`: line `100%`, branch `87.75%`;
  - `AdminReportAiSemanticValidator`: line `100%`, branch `96.72%`.
