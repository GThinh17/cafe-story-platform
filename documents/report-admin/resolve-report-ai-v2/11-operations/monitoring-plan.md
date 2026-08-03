# Sprint 1 Monitoring Plan

## Metrics

- recommendation requests/success/manual/failure;
- provider timeout/429/5xx/schema/semantic failure;
- HMAC/freshness/replay rejection;
- idempotency hit/conflict;
- missing/conflicted evidence by target/rule;
- USER/PAGE local-manual count and provider-call count;
- new/active auto-apply jobs under A0;
- target/report mutation detector;
- p50/p95 latency by BE/n8n/provider stage;
- raw/secret scan violations.

## Alerts

Immediate:

- any AI-triggered mutation under A0;
- any new auto-apply job;
- secret/raw leak;
- HMAC verification disabled/failing broadly;
- replay/forgery spike.

Operational:

- provider failure/latency increase;
- queue/SLA breach;
- excessive semantic fallback;
- cleanup/retention job failure.

Metrics use codes/IDs, not raw content.
