# S2-DONE-FIX-02-DB-HASH-LENGTH — Bound

- Token: `IMPLEMENT_S2_DONE_FIX_02_DB_HASH_LENGTH`.
- Mục tiêu: sửa mismatch giữa canonical snapshot hash 71 ký tự và cột
  `target_snapshot_hash VARCHAR(64)`, sau đó chạy lại Lean Functional Gate bị
  chặn ở FIX-01.
- Được thay đổi:
  - một Flyway migration mới;
  - annotation schema của entity `AdminReportAiResolution`;
  - focused schema-contract test;
  - evidence và Lean Roadmap.
- Không được thay đổi:
  - migration `V20260723_01__admin_report_ai_contract_v2.sql` đã áp dụng;
  - format canonical `sha256:<64-hex>`;
  - business rule, prompt, n8n workflow hoặc Admin UI;
  - production/external database và production deployment;
  - S2-05, Sprint 3, Sprint 4.
- Môi trường verify:
  - Backend Maven/JUnit/JaCoCo;
  - PostgreSQL disposable `cafestory-g0-12c-postgres`;
  - local n8n và Admin UI cho đúng hai E2E `E2E-S1-13`, `E2E-S1-04`.
- Tiêu chí chấp nhận:
  1. migration cũ không đổi checksum/source;
  2. Flyway áp dụng migration mới trên PostgreSQL disposable;
  3. DB metadata và entity đều có length `71`;
  4. JPA `validate` khởi động thành công;
  5. focused/full Backend không failure/error;
  6. hai E2E pass, không mutation, không auto-apply ngoài ý muốn và cleanup `0`.

