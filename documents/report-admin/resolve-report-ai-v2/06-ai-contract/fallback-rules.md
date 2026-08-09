# Fallback Rules

| Failure | Fallback |
|---|---|
| Evidence insufficient/conflicted | Valid manual/no action |
| Model unknown rule/evidence/action | Semantic manual fallback |
| Schema invalid with trusted correlation | Operational audit + manual fallback only if safe fields parse |
| Auth/signature mismatch | Reject boundary; no recommendation |
| Provider timeout/429/5xx | Bounded retry then operational error |
| Model unavailable | No silent model switch |
| Snapshot changed | Stale; request new snapshot |
| USER/PAGE | Local manual result; no provider |

Fallback must never silently create `REJECT`, `RESOLVE`, target mutation or fake evidence.
