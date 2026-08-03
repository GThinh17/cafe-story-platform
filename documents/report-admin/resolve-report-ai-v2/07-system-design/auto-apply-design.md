# Auto-apply Design — Sprint 1 A0

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| Mode | `A0_RECOMMEND_ONLY` |

## Default-deny controls

1. Config default A0 when property missing.
2. Controller/service request cannot override server mode.
3. `scheduleIfRequested` never inserts under A0.
4. Worker checks mode before claim and immediately before mutation.
5. Kill switch has higher precedence than all feature flags.
6. Numeric score cannot enable a job.
7. Admin approval cannot bypass policy-invalid action or stale revalidation.

## Legacy job transition

| Job state | Deployment handling |
|---|---|
| `SCHEDULED` | Inventory, then cancel/skip with structured reason |
| `APPLYING` | Deployment blocker until transaction/state verified |
| `APPLIED` | Preserve audit; no rewrite |
| `FAILED/SKIPPED/CANCELLED` | Preserve until retention expiry |

No Flyway migration silently converts job state. Operations step must record counts before/after.

## API outcome

Legacy client sending auto-apply receives:

```json
{
  "automationMode": "A0_RECOMMEND_ONLY",
  "automationOutcome": "BLOCKED_AUTOMATION_MODE",
  "autoApplyJob": null
}
```

Recommendation creation may still succeed. FE counts this as recommendation outcome, not scheduling
success/failure.

## Future activation

A1/A2 requires separate policy/version, evaluation, allowlist, rollback drill and user approval.
Sprint 1 must not leave a hidden config value capable of restoring destructive A3 behavior.
