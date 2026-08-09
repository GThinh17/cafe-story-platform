# S2-DONE-FIX-02-DB-HASH-LENGTH — Test log

| Gate | Phương pháp | Kết quả thực tế | Status |
|---|---|---|---|
| Source contract | `AdminReportAiResolutionSchemaContractTest` | Canonical `71`, entity `71`, migration SQL `VARCHAR(71)` | `1/1 PASS` |
| Focused Backend | Maven schema + resolution + semantic tests | `54`, failure/error/skipped `0/0/0` | `PASS` |
| Full Backend | `mvn test` | `642`, failure `0`, error `0`, skipped `1` | `PASS` |
| Migration immutability | `git diff` migration `20260723.01` | Không có diff | `PASS` |
| Flyway apply | Backend startup trên PostgreSQL disposable | Applied `20260730.01`, validated `11` migrations | `PASS` |
| Physical schema | `information_schema.columns` | `target_snapshot_hash|varchar|71` | `PASS` |
| JPA/API | `ddl-auto=validate`, `/v3/api-docs` | Startup thành công, HTTP `200` | `PASS` |
| Security | `validate-admin-report-ai-security.mjs` | HMAC/freshness/replay/tamper/signed response pass | `PASS` |
| Provider recovery E2E | Playwright `E2E-S1-13` | `1/1 PASS` trong `38.3s` | `PASS` |
| Critical evidence E2E | Playwright `E2E-S1-04` | `1/1 PASS` trong `38.1s` | `PASS` |
| Raw assertions | Node assertions trên evidence JSON | Hash `71`, status/action, no-mutation, cleanup | `PASS` |

## E2E-S1-13

- Provider unavailable: HTTP `502`, code `AI_PROVIDER_BOUNDARY_FAILED`,
  `retryable=true`, stage `N8N_PROVIDER`.
- Resolution count: `0 → 0` trong failure phase.
- Sau n8n recovery: HTTP `200`, persisted resolution count `1`.
- Canonical persisted hash length: `71`.
- Report/target mutation: `false/false`.
- Auto-apply job: `0`.
- Cleanup: `0/0`.

## E2E-S1-04

- HTTP `200`.
- Decision/action: `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- Evidence sufficiency: `UNASSESSABLE`.
- Blocked reason có `CRITICAL_EVIDENCE_MISSING`.
- Target mutation: `false`.
- Auto-apply job: `0`.
- Cleanup: `0/0/0`.
