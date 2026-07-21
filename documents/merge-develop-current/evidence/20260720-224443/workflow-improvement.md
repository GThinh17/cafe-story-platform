# Workflow improvement

- Run frontend typecheck immediately after textual conflict resolution; it caught duplicate object keys that Git did not mark as conflicts.
- For future branch merges, compare live callers before choosing between renamed API functions so both active flows are preserved.
- Clear or version local Redis cache before runtime smoke testing to reduce stale-serialization noise.
- Add a small frontend unit test setup for feed adapters and payment redirect helpers so merge validation is less dependent on manual UI checks.
