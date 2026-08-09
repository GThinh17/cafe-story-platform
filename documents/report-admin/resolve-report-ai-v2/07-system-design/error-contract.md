# Error Contract — Sprint 1

## API shape

```json
{
  "code": "AI_PROVIDER_BOUNDARY_FAILED",
  "message": "AI recommendation service is unavailable.",
  "correlationId": "uuid",
  "retryable": true,
  "stage": "N8N_PROVIDER"
}
```

## Canonical codes

`REPORT_NOT_AI_ELIGIBLE`, `TARGET_NOT_AVAILABLE`, `AI_RECOMMENDATION_IN_PROGRESS`,
`AI_AUTOMATION_BLOCKED_A0`, `AI_BOUNDARY_AUTH_FAILED`, `AI_PROVIDER_BOUNDARY_FAILED`,
`AI_RESPONSE_SCHEMA_INVALID`, `AI_RESPONSE_SEMANTIC_INVALID`, `AI_SNAPSHOT_STALE`,
`AI_RECOMMENDATION_REUSED`.

## Rules

- client message contains no raw provider body;
- operational errors do not create `REJECT/RESOLVE`;
- evidence insufficiency is a valid manual recommendation, not HTTP failure;
- retryable is explicit;
- bulk stores error code per item;
- FE may show correlation ID for support, never secret/signature.

## DOD-FIX-04 as-built record

- Backend transport/non-2xx tại n8n boundary được map sang đúng
  `AI_PROVIDER_BOUNDARY_FAILED`.
- Response thực tế giữ envelope hiện hành và bổ sung top-level `code`, `correlationId`,
  `retryable`, `stage`.
- Admin giữ và hiển thị operational metadata; không diễn giải lỗi vận hành thành content decision.
- `E2E-S1-13` ngày `2026-07-29`: failure HTTP `502`, resolution `0 -> 0`; sau n8n recovery,
  retry HTTP `200`, report/target không mutation, auto job `0`.
- Evidence:
  `../09-sprints/evidence/2026-07-29T14-53-04-516+07-00/`.
