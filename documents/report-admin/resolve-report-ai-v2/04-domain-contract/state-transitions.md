# State Transitions — Sprint 1

## Recommendation lifecycle

```text
REQUESTED
→ SNAPSHOT_CAPTURED
→ PROVIDER_PENDING
→ VALIDATING
→ PERSISTED

Failure branches:
REQUESTED → REJECTED_INPUT
PROVIDER_PENDING → FAILED_OPERATIONAL
VALIDATING → MANUAL_FALLBACK
VALIDATING → STALE
```

## Report state

Ask AI is allowed only from `OPEN/REVIEWING`. Recommendation creation does not transition report
status. `RESOLVED/REJECTED` are terminal for Ask AI eligibility.

## Auto-job state

No new auto-job transition is reachable in Sprint 1. Legacy jobs remain readable:

```text
SCHEDULED → CANCELLED or SKIPPED_A0
APPLYING → deployment blocker/incident verification
APPLIED/FAILED/SKIPPED/CANCELLED → retained history
```

No state is silently rewritten to “success”.
