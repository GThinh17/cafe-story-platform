# S2-DONE-FIX-02-DB-HASH-LENGTH — Issue log

## S2DONEFIX02-ISSUE-001

- Classification: `CODE_BUG`.
- Evidence nguồn:
  `../2026-07-30T16-39-59-779+07-00/issue.md#s2donefix-issue-007`.
- Actual:
  - service sinh `targetSnapshotHash` theo canonical format
    `sha256:<64-hex>` có length `71`;
  - entity annotation khai báo `length = 64`;
  - migration đã áp dụng tạo `target_snapshot_hash VARCHAR(64)`;
  - PostgreSQL từ chối persist bằng
    `value too long for type character varying(64)`.
- Affected behavior:
  - E2E happy/retry recovery không persist được AI resolution;
  - E2E manual fail-safe không hiển thị được persisted recommendation.
- Likely owner: Backend entity/database schema contract.
- Proposed action:
  - thêm immutable Flyway migration mới để đổi column thành `VARCHAR(71)`;
  - đồng bộ entity annotation;
  - khóa bằng focused source-contract test và PostgreSQL runtime probe.
- Auto-fix allowed: `yes`, được người dùng mở scope bằng token hiện tại.
- Trạng thái: `RESOLVED_VERIFIED`.
- Verification:
  - focused contract + Admin Report AI suite `54/54 PASS`;
  - Flyway applied `20260730.01`;
  - PostgreSQL metadata length `71`;
  - JPA `validate` startup và API smoke `PASS`;
  - `E2E-S1-13` và `E2E-S1-04` đều `1/1 PASS`.

## S2DONEFIX02-ISSUE-002

- Classification: `TEST_BUG`.
- Actual: command PowerShell gộp port preflight, `docker start` và HTTP health
  loop bị local runner policy từ chối tại `CreateProcess`; command chưa được
  thực thi và chưa thay đổi container.
- Affected behavior: chỉ ảnh hưởng orchestration của môi trường test.
- Likely owner: test command construction.
- Proposed action: tách port probe, container start và health probes thành các
  command nhỏ, không thay đổi source.
- Auto-fix allowed: `yes`.
- Trạng thái: `RESOLVED_VERIFIED`; các probe được tách nhỏ và đều chạy thành công.

## S2DONEFIX02-ISSUE-003

- Classification: `CONFIG_ENV`.
- Actual: Backend local runtime ghi warning không kết nối được Redis trong một
  background cache-clear path, dù gate đã cấu hình simple cache.
- Affected behavior: không ảnh hưởng Flyway, Admin Report AI request, E2E hoặc
  tiêu chí chấp nhận của package.
- Likely owner: local optional Redis/runtime profile.
- Proposed action: chỉ cấu hình Redis khi chạy test cache riêng; không sửa
  product source để che warning.
- Auto-fix allowed: `no`.
- Trạng thái: `OBSERVED_NON_BLOCKING`.

## S2DONEFIX02-ISSUE-004

- Classification: `CODE_BUG`.
- Actual: background `ReportModerationJobWorker` xử lý hai report synthetic ghi
  lỗi lazy initialization khi serialize `Blog.imageUrls` hoặc
  `Comment.imageUrls` ngoài Hibernate session.
- Affected behavior: workflow Report Moderation nền, không phải
  Admin Resolve Report with AI V2. Hai Admin AI E2E vẫn pass.
- Likely owner: `ReportModerationServiceImpl`/entity-to-webhook mapping.
- Evidence: Backend runtime log trong lúc E2E; cleanup helper xóa
  `report_moderation_jobs` theo `content_report_id` trước khi xóa fixture.
- Proposed action: gate riêng phải materialize webhook DTO trong transaction hoặc
  fetch/copy image URLs trước khi rời persistence context, kèm focused regression.
- Auto-fix allowed: `no` trong Bound hiện tại vì sẽ mở rộng sang workflow
  Report Moderation production source.
- Trạng thái: `OUT_OF_SCOPE_NON_BLOCKING`; không còn test-data residue.
