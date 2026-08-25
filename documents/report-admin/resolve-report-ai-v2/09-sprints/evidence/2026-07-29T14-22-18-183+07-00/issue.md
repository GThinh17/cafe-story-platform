# Issue register — DOD-FIX-03

## Baseline

- Gate: `DOD-FIX-03`.
- Scenario: `E2E-S1-04` — COMMENT thiếu critical parent context.
- Fixture: synthetic BLOG có context whitespace + COMMENT phụ thuộc context, trên PostgreSQL disposable.
- Expected: `NEEDS_MANUAL_REVIEW + NO_ACTION`, findings rỗng,
  `CRITICAL_EVIDENCE_MISSING`, `UNASSESSABLE`, không target/report mutation, không auto job.
- Source fix: chưa thực hiện trước baseline.

## FIX03-TEST-001

- Classification: `TEST_BUG`.
- Evidence: baseline Playwright dừng tại cleanup với FK
  `admin_report_ai_resolutions -> content_reports`.
- Actual: fixture xóa `content_reports` trước resolution phụ thuộc; cleanup exception che mất
  assertion semantic ban đầu.
- Expected: xóa dependency theo thứ tự, luôn ghi sanitized runtime result trước UI assertion.
- Affected behavior: độ tin cậy và khả năng cleanup của E2E harness; chưa chứng minh production bug.
- Likely owner: Playwright fixture.
- Proposed action: xóa job/resolution/moderation dependencies trước report và ghi raw result sớm.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: attempt 2 đã ghi được sanitized runtime result và cleanup còn `0/0/0`.

## FIX03-CODE-001

- Classification: `CODE_BUG`.
- Evidence: `raw/E2E-S1-04-runtime-result.json`.
- Actual:
  - Provider tự trả manual, nhưng Backend packet coi parent BLOG content whitespace là evidence
    `HIGH/AVAILABLE`.
  - Response chỉ có `EVIDENCE_NOT_SUFFICIENT`, evidence sufficiency `INSUFFICIENT`.
  - Không có deterministic guard `CRITICAL_EVIDENCE_MISSING`.
- Expected: blank/unreadable parent context phải được Backend đánh dấu `UNUSABLE/MISSING`,
  `criticalEvidenceMissing=true`; mọi provider output đều bị clamp về manual/no-action,
  `UNASSESSABLE`.
- Affected behavior: `DOD-16`, `E2E-S1-04`, `SEM-009`.
- Likely owner: Backend `AdminReportAiResolutionServiceImpl`.
- Proposed action: chuẩn hóa predicate usable parent context và dùng thống nhất cho snapshot,
  evidence packet và execution constraints.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification:
  - Unit regression `TC015` đỏ trước sửa (`expected NEEDS_MANUAL_REVIEW, actual RESOLVE`) và xanh
    sau sửa.
  - E2E attempt 3 xác nhận Backend đã phát sinh `CRITICAL_EVIDENCE_MISSING`, target không mutation
    và không tạo auto-apply job.

## FIX03-CODE-002

- Classification: `CODE_BUG`.
- Evidence: `raw/E2E-S1-04-runtime-result.json`, E2E attempt 3.
- Actual:
  - Backend đã phát sinh đúng `CRITICAL_EVIDENCE_MISSING`.
  - Tuy nhiên semantic validator giữ nguyên giá trị provider `INSUFFICIENT`; UI/API không có
    trạng thái bắt buộc `UNASSESSABLE`.
- Expected: khi `criticalEvidenceMissing=true`, Backend trust boundary phải khóa kết quả thành
  `NEEDS_MANUAL_REVIEW + NO_ACTION + UNASSESSABLE`, không phụ thuộc provider trả
  `SUFFICIENT` hay `INSUFFICIENT`.
- Affected behavior: `DOD-16`, `E2E-S1-04`, `SEM-009`.
- Likely owner: Backend `AdminReportAiSemanticValidator`.
- Proposed action: dùng cờ `criticalEvidenceMissing` nhất quán khi thêm blocked reason và khi
  chuẩn hóa evidence sufficiency.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification:
  - Unit baseline đỏ đúng semantic gap:
    `expected UNASSESSABLE, actual INSUFFICIENT`.
  - Unit sau sửa `1/1 PASS`.
  - Full-path E2E `E2E-S1-04` `1/1 PASS`; persisted/UI value là `UNASSESSABLE`.

## Ngoài phạm vi

- Không thay đổi production data.
- Không triển khai DOD-FIX-04–06.
- Không thay đổi policy authority hoặc bật auto-apply.
