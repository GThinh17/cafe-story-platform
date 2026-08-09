# Issue

## BE-FLYWAY-001

- classification: CONFIG_ENV
- evidence: raw/backend-startup-failure.txt
- affected behavior: backend could not start against the configured Supabase database because Flyway rejected migration validation.
- likely owner: database migration state / repository migration history
- observed command: `mvn spring-boot:run`
- root cause: migration version `20260719.01` was already recorded in `flyway_schema_history` with checksum `698173291`, but the local migration file resolves to checksum `1984372332`.
- read-only schema check: the current database already had the expected score component columns and formula index from the local migration file.
- repair evidence: raw/flyway-repair-result.txt
- status after approval: fixed by approved Flyway repair.

## Why this should not be patched around in code

Disabling Flyway validation or setting ignore patterns would hide a real migration history mismatch. That can let the app start while the repository no longer describes the database state reliably.

The chosen fix was Flyway repair because the database schema already matched the current local migration content. This changed Supabase migration metadata only after explicit `APPROVE_REPAIR`.
