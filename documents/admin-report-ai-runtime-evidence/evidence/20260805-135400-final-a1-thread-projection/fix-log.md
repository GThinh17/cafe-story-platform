# Fix Log

## FIX-001
- Status: FIXED
- Changed: provider schema source, canonical provider schema, generated n8n workflow.
- Verification: n8n S2 contracts and security validators passed; runtime AI calls returned HTTP 200.

## FIX-002
- Status: FIXED
- Changed: backend policy lifecycle values and focused tests.
- Verification: focused Maven tests passed.

## FIX-003
- Status: FIXED
- Changed: Flyway forward migration for A1 automation mode.
- Verification: backend started against current DB and persisted A1 AI resolution rows.

## FIX-004
- Status: FIXED
- Changed: auto-apply worker transaction handling.
- Verification: final runtime showed eligible BLOG/COMMENT jobs reached `APPLIED`.

## FIX-005
- Status: FIXED
- Changed: provider thread projection and backend scam/spam auto-hide target-text guard.
- Verification: final runtime kept normal blog/comment `PUBLISHED` with no auto job and hid only severe scam targets.
