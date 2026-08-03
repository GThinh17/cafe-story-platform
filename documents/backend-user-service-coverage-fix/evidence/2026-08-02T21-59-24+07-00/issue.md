# Issue

## BE-COV-001

- classification: TEST_BUG
- command: `.\mvnw.cmd -q verify`
- evidence: JaCoCo failed `check-user-service-coverage`.
- affected behavior: backend verification could not complete because `UserServiceImpl` line coverage was below the hard 100% gate.
- details: uncovered lines were the not-found branch in `getUserByUsername` and the update branches for `userDescription` and `hideCafePageOnProfile`.
- owner: backend unit tests
- proposed action: add focused unit coverage in `UserServiceImplTest`.
- auto-fix allowed: yes

## BE-DB-001

- classification: CONFIG_ENV
- command: read-only JDBC metadata probe
- evidence: `flyway_schema_history` contains `20260730.01`; target column length is `71`.
- affected behavior: user asked whether SQL may be deleted if DB does not contain it.
- owner: database/source alignment
- proposed action: keep the migration because DB history and schema both confirm it is active.
- auto-fix allowed: no
