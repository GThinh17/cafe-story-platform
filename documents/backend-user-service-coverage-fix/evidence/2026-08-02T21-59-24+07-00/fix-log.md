# Fix Log

## BE-COV-001

- action: added `getUserByUsername_fail_notFound_TC006_2`.
- action: extended `updateUser_success_updateAllFields_TC008` to assert `userDescription` and `hideCafePageOnProfile`.
- result: FIXED.
- verification: `.\mvnw.cmd -q '-Dtest=UserServiceImplTest' test` passed.
- verification: `.\mvnw.cmd -q verify` passed.

## BE-DB-001

- action: no source deletion.
- reason: read-only DB probe found migration `20260730.01` successful and column length `71`.
- result: NO_CHANGE_REQUIRED.
