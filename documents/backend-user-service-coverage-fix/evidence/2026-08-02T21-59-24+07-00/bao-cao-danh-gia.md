# Backend User Service Coverage Fix

## Result

DONE for the reproduced backend build failure.

The failing backend gate was `mvnw verify`, caused by JaCoCo `check-user-service-coverage` for `UserServiceImpl`.

## Changes

- Added a not-found test for `UserServiceImpl#getUserByUsername`.
- Extended the update-all-fields test to cover `userDescription` and `hideCafePageOnProfile`.
- Did not delete `V20260730_01__admin_report_ai_snapshot_hash_length.sql`.

## Validation

| Check | Expected | Actual |
| --- | --- | --- |
| `python .agents/skills/cafestory-engineering-workflows/scripts/check_project.py .` | Project structure valid | PASS |
| `.\mvnw.cmd -q -DskipTests compile` | Backend compiles | PASS |
| `.\mvnw.cmd -q '-Dtest=UserServiceImplTest' test` | Focused user service tests pass | PASS |
| `.\mvnw.cmd -q '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest,AdminReportAiExceptionAdviceTest,AdminReportAiResolutionSchemaContractTest' test` | Related Admin Report AI tests pass | PASS |
| `.\mvnw.cmd -q test` | Backend tests pass | PASS: 643 tests, 0 failures, 0 errors, 1 skipped |
| `.\mvnw.cmd -q verify` | Backend verification and JaCoCo gate pass | PASS |

## Coverage

`UserServiceImpl` after the fix:

- line: 82 covered / 0 missed = 100%
- branch: 34 covered / 2 missed = 94.44%
- method: 19 covered / 0 missed = 100%

## Database / SQL Decision

Read-only DB metadata check:

- `flyway_schema_history` contains version `20260730.01`, description `admin report ai snapshot hash length`, `success=true`.
- `admin_report_ai_resolutions.target_snapshot_hash` has length `71`.

Conclusion: the SQL migration file is not orphaned and was kept.

## Residual Risk

- Docker Desktop is not running in this shell, so Docker-based backend/n8n checks were not run.
- Runtime startup against the real DB was not executed to avoid applying migrations as a side effect; the DB check used read-only JDBC queries only.
