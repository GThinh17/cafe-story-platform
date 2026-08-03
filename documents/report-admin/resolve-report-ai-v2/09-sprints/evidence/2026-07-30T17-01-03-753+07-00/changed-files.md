# S2-DONE-FIX-02-DB-HASH-LENGTH — Changed files

## Production

- `AdminReportAiResolution.java`
  - `target_snapshot_hash` length `64 → 71`.
- `V20260730_01__admin_report_ai_snapshot_hash_length.sql`
  - migration mới đổi physical column thành `VARCHAR(71)`.

## Test

- `AdminReportAiResolutionSchemaContractTest.java`
  - khóa canonical/entity/migration length contract.

## Documentation/evidence

- evidence folder của gate hiện tại;
- DD tiếng Việt của fix;
- Lean Roadmap cập nhật sang `APPROVE_S2_DONE`.

Migration đã áp dụng `V20260723_01__admin_report_ai_contract_v2.sql` không bị sửa.
Các thay đổi worktree khác được bảo toàn.
