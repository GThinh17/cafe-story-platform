# S2-DONE-FIX-02-DB-HASH-LENGTH — Coverage

Production Java chỉ thay đổi annotation schema trên
`AdminReportAiResolution.targetSnapshotHash`; annotation không tạo executable
line/branch để JaCoCo tính riêng.

Coverage hành vi thay đổi được khóa bằng:

- reflection assertion entity `@Column(length = 71)`;
- migration resource assertion `TYPE VARCHAR(71)`;
- PostgreSQL metadata probe length `71`;
- hai full-path E2E thực sự persist canonical hash dài `71`.

Không có business branch hoặc executable production logic mới.
