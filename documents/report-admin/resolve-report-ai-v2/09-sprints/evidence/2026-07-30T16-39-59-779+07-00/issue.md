# S2-DONE-FIX-01 — Issue log

## S2DONEFIX-ISSUE-001

- Classification: `TEST_BUG`.
- Lỗi: security fixture dùng request shape cũ, bị strict S2 contract reject
  trước HMAC/replay assertions.
- Auto-fix allowed: `yes`.
- Trạng thái: `RESOLVED_VERIFIED`.

## S2DONEFIX-ISSUE-002

- Classification: `TEST_BUG`.
- Lỗi: ba nhánh safety-relevant chưa được focused test trực tiếp.
- Auto-fix allowed: `yes`.
- Trạng thái: `RESOLVED_VERIFIED`.

## S2DONEFIX-ISSUE-003

- Classification: `TEST_DATA`.
- Lỗi: historical evidence S2-01 thiếu machine-readable `summary.json`.
- Auto-fix allowed: `no`; reconstruction được thực hiện theo token người dùng,
  chỉ từ artifact/evidence đã approved, không tạo claim mới.
- Trạng thái: `RESOLVED_VERIFIED`.

## S2DONEFIX-ISSUE-004

- Classification: `TEST_BUG`.
- Lỗi: evaluation harness hard-code focused count bằng `50`, nên báo fail khi
  suite tăng hợp lệ lên `53/53`.
- Evidence: `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs`
  trả `AssertionError: 53 !== 50`.
- Auto-fix allowed: `yes`.
- Trạng thái: `RESOLVED_VERIFIED`; harness chấp nhận số test tăng hợp lệ
  (`passed >= 50`) và đã chạy lại thành công với `53/53`.

## S2DONEFIX-ISSUE-005

- Classification: `TEST_BUG`.
- Lỗi: PowerShell coverage parser lần đầu có empty pipeline element.
- Auto-fix allowed: `yes`.
- Trạng thái: `RESOLVED_VERIFIED`; command đã sửa bằng biến `$rows`.

## S2DONEFIX-ISSUE-006

- Classification: `TEST_BUG`.
- Lỗi: lệnh Playwright gộp hai test bằng biểu thức có ký tự `|` bị `cmd.exe`
  diễn giải thành pipe, nên lần gọi đầu không chạy test.
- Auto-fix allowed: `yes`.
- Trạng thái: `RESOLVED_VERIFIED`; chạy từng test E2E bằng tên riêng, không thay
  đổi source hoặc hành vi sản phẩm.

## S2DONEFIX-ISSUE-007

- Classification: `CODE_BUG`.
- Lỗi: luồng retry sau khi n8n phục hồi đi đến bước persist nhưng PostgreSQL
  từ chối insert với `value too long for type character varying(64)`.
- Root cause đã xác nhận:
  - service sinh `targetSnapshotHash` theo format canonical
    `sha256:<64-hex>` (71 ký tự);
  - focused test cũng khóa contract này bằng `hasSize(71)`;
  - entity và migration hiện khai báo `target_snapshot_hash VARCHAR(64)`.
- Evidence:
  - Admin E2E `E2E-S1-13` nhận đúng lỗi provider-boundary `502` ở lần đầu và
    không persist recommendation;
  - sau khi n8n phục hồi, retry thất bại ở câu insert
    `admin_report_ai_resolutions`;
  - truy vấn `information_schema.columns` xác nhận
    `target_snapshot_hash|character varying|64`.
- Auto-fix allowed: `no` trong Bound hiện tại vì cần thay đổi database bằng
  Flyway migration mới; không được sửa migration đã áp dụng và không được cắt
  hash làm mất format contract.
- Trạng thái: `BLOCKED_OUT_OF_SCOPE`.
