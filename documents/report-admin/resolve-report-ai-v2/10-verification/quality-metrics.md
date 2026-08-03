# Quality Metrics — Sprint 1

## Safety/contract metrics

| Metric | Gate target |
|---|---:|
| AI-triggered mutations under A0 | `0` |
| New auto-apply jobs under A0 | `0` |
| Unknown rule accepted | `0` |
| Dangling evidence reference accepted | `0` |
| Raw/secret leak in API/log/evidence | `0` |
| USER/PAGE provider call | `0` |
| Required version fields present | `100%` V2 |
| Changed-file line coverage | `100%` |
| Changed-file branch coverage | `>=85%` |

## Diagnostic metrics, not release thresholds

- manual-review rate by target/reason/rule;
- critical-missing and conflict frequency;
- provider/schema/semantic failure rate;
- recommendation latency p50/p95;
- duplicate idempotency reuse;
- Admin override and future appeal overturn;
- bulk per-item outcome.

No model accuracy or automation SLA is claimed until representative evaluation data exists.
