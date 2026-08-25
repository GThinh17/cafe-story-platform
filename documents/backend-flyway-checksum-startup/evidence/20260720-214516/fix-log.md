# Fix Log

## 2026-07-20 21:45 ICT

- Reproduced backend startup failure with `mvn spring-boot:run`.
- Confirmed full backend unit/regression test command `mvn test` passes: 571 tests, 0 failures, 0 errors, 1 skipped.
- Did not edit Java source.
- Did not run Flyway repair before approval because it mutates Supabase migration metadata.
- Ran a read-only schema check: all expected score component columns and the formula index already exist.

## 2026-07-20 21:54 ICT

- User approved with `APPROVE_REPAIR`.
- Ran Flyway repair against the backend configured database.
- `flyway_schema_history` row `20260719.01` changed from description `performance indexes`, checksum `698173291` to description `feed expert v1 score components`, checksum `1984372332`.
- Verified backend is listening on port `8080`.
- Smoke checks:
  - `/v3/api-docs`: HTTP 200
  - `/swagger-ui.html`: HTTP 200
  - `/`: HTTP 401, expected protected/default response
- Re-ran `mvn test`: 571 tests, 0 failures, 0 errors, 1 skipped.
