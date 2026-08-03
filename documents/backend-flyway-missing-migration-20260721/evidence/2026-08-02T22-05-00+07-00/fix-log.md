# Fix Log

## BE-FLYWAY-20260721-001

- action: restored `V20260721_01__performance_indexes.sql`.
- source: historical performance-index migration content.
- reason: DB has already applied `20260721.01`; restoring source is safer than mutating Flyway metadata.
- result: FIXED.
- verification: backend runtime smoke returned `/v3/api-docs` HTTP 200.
- verification: `.\mvnw.cmd -q verify` passed.

## BE-FLYWAY-20260721-002

- action: no DB repair.
- reason: repair would mutate external DB metadata and was unnecessary after restoring source.
- result: NO_CHANGE_REQUIRED.
