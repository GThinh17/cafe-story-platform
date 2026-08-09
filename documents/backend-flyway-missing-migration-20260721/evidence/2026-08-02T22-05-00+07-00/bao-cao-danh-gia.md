# Backend Flyway Missing Migration 20260721

## Result

DONE for the attached backend startup failure.

The attached error was:

`Detected applied migration not resolved locally: 20260721.01`

## Bound

- Target: backend startup failure in `1-cafe-story-backend-javaspring`.
- Allowed changes: restore missing backend SQL migration source and write evidence.
- Out of scope: frontend, mobile, n8n workflow behavior, and destructive DB metadata changes.
- Not touched: `flyway_schema_history` was queried read-only only; no repair or delete was executed.

## Fix

Restored:

`1-cafe-story-backend-javaspring/src/main/resources/db/migration/V20260721_01__performance_indexes.sql`

The content was recovered from the historical performance-index migration and matches the DB-applied Flyway row:

`20260721.01|performance indexes|V20260721_01__performance_indexes.sql|698173291|true`

## Validation

| Check | Expected | Actual |
| --- | --- | --- |
| Read attached error | Identify real runtime failure | Flyway missing migration `20260721.01` |
| Search local source/git history | Find matching migration source | Historical performance-index SQL found |
| Read-only DB metadata probe | Confirm DB-applied migration | `success=true`, checksum `698173291` |
| Backend runtime smoke | Backend starts past Flyway | `/v3/api-docs` returned HTTP 200 |
| Process cleanup | No smoke server left running | port `18080` had no listener afterward |
| `.\mvnw.cmd -q verify` | Backend tests and coverage pass | PASS: 643 tests, 0 failures, 0 errors, 1 skipped |

## Evidence

- `raw/read-only-flyway-probe.txt`
- `raw/startup-smoke.txt`

## Residual Risk

- The broader worktree still contains pre-existing Admin Report AI/admin/docker/document changes not owned by this fix.
- Docker/n8n checks were not run because the reported failure is backend Flyway startup.
