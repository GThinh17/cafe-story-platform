# Audit Design — Sprint 1

## Events

```text
RECOMMENDATION_REQUESTED
SNAPSHOT_CAPTURED
PROVIDER_REQUESTED
PROVIDER_FAILED
RECOMMENDATION_VALIDATED
RECOMMENDATION_CLAMPED
RECOMMENDATION_PERSISTED
AUTOMATION_BLOCKED
LEGACY_JOB_CANCELLED_OR_SKIPPED
```

## Minimum fields

`eventId`, `eventType`, `occurredAt`, `correlationId`, `reportId`, `targetType`, `targetId`,
`actedAsRole`, `actorAdminId` where relevant, contract/policy/rule/prompt/workflow/model versions,
snapshot hash, outcome code and duration.

## Prohibited fields

Raw prompt/response, HMAC/signature, API token, reporter contact, unnecessary target content and
unredacted PII.

## Separation

Recommendation, Admin decision and execution are different events. Sprint 1 records recommendation
only; it must not imply execution occurred.
