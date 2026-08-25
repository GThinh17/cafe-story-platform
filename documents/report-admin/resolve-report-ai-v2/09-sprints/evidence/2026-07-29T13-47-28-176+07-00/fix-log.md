# Fix log — DOD-FIX-02

## Baseline

- Hai explicit stale-snapshot tests đã được thêm.
- Kết quả baseline: `2 tests`, `2 failures`.
- Phân loại: `CODE_BUG`.
- Source fix chưa thực hiện tại thời điểm ghi issue.

## FIX02-TEST-001

- Lần rerun đầu tiên sau source fix: test compile fail do constructor signature.
- Phân loại: `TEST_BUG`.
- Fix: thêm overload tương thích, không thay đổi business behavior.
- Kết quả: explicit stale suite `2/2 PASS`.

## FIX02-TEST-002

- Coverage trước bổ sung test: line `99.34%`, branch `87.24%`.
- Fix: cover `EntityManager=null` và target biến mất khi authoritative refresh.
- Kết quả: service suite `25/25 PASS`; line `458/458 = 100%`, branch
  `172/196 = 87.76%`.

## FIX02-CODE-001

- Fix: khi tìm thấy idempotency cache, Backend refresh report/target từ persistence context,
  rebuild snapshot và idempotency key trước khi reuse.
- Nếu key sau refresh khác key đã lookup, record cũ không được trả về; request mới tiếp tục
  đi qua provider với snapshot/key mới.
- Kết quả: `createResolution_changedSnapshotDoesNotReusePreviouslyMatchedCache_CT010_SAF010`
  pass.

## FIX02-CODE-002

- Fix: sau provider response và trước semantic validation/persistence, Backend refresh lại
  report/target rồi rebuild snapshot.
- Nếu idempotency key thay đổi trong provider window, provider output bị bỏ quyền quyết định và
  thay bằng `NEEDS_MANUAL_REVIEW + NO_ACTION`, findings rỗng, blocked reason
  `TARGET_SNAPSHOT_CHANGED_DURING_EVALUATION`.
- Nếu target biến mất khi refresh, Backend trả `409 CONFLICT` và không persist resolution.
- Kết quả:
  - explicit stale suite `3/3 PASS`;
  - Admin Report AI focused `63/63 PASS`;
  - full Backend `624`, failure/error `0`, skipped `1`;
  - Spring context khởi động thành công.

## Hygiene

- `check_project.py`: `PASS`, warnings `[]`.
- secret pattern hits: `0`.
- `git diff --check`: không có whitespace error; chỉ có cảnh báo LF/CRLF của Git trên Windows.
