# Issue

## BE-FLYWAY-20260721-001

- classification: CODE_BUG
- command/evidence: attached runtime log and read-only JDBC query against `flyway_schema_history`.
- affected behavior: backend startup fails before `entityManagerFactory` initialization because Flyway validation cannot resolve an already-applied migration.
- root cause: DB has applied `20260721.01` with script `V20260721_01__performance_indexes.sql`, checksum `698173291`, but the local checkout does not contain the corresponding migration file.
- owner: backend migration source history.
- proposed action: restore the missing SQL migration file from git history content that matches the DB-applied performance-index migration.
- auto-fix allowed: yes.

## BE-FLYWAY-20260721-002

- classification: CONFIG_ENV
- command/evidence: read-only JDBC query.
- affected behavior: external DB metadata repair would also clear the missing migration, but it mutates `flyway_schema_history`.
- proposed action: do not repair DB metadata because source restoration is available and less destructive.
- auto-fix allowed: no.
