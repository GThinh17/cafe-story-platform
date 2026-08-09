# Input Schema V2

Required top-level:

```text
contractVersion=2.0
correlationId
idempotencyKey
requestedAt
automationMode=A0_RECOMMEND_ONLY
reportClaim
targetSnapshot
evidence[]
policyContext
executionConstraints
```

Limits:

- one report/target per request;
- target only BLOG/COMMENT at provider boundary;
- maximum bounded text/media/reference lengths;
- unique Evidence IDs;
- unique candidate Rule IDs;
- candidate rules must apply to target/reason;
- snapshot hash required;
- no unknown top-level properties;
- no reporter identity/contact or secret.

Canonical example is in `09-sprints/sprint-01-safety-contract.md`.
