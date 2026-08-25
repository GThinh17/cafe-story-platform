# Safety Test Matrix — Sprint 1

| ID | Safety property | Proof |
|---|---|---|
| `SAF-001` | A0 default when config missing | Config/service test |
| `SAF-002` | Request cannot enable automation | Controller/service test |
| `SAF-003` | Worker cannot mutate under A0 | Worker/service regression |
| `SAF-004` | Existing scheduled job skipped/cancelled | Integration/ops evidence |
| `SAF-005` | USER/PAGE no provider call | Mock verification |
| `SAF-006` | Raw provider body absent from logs | Captured logs/secret scan |
| `SAF-007` | Raw response absent from V2 API | Contract test |
| `SAF-008` | HMAC invalid/stale/replay blocked | n8n negative probes |
| `SAF-009` | Prompt injection cannot change rule/action scope | Adversarial test |
| `SAF-010` | Stale snapshot not reused | Explicit cache/provider-window freshness tests `PASS` in `DOD-FIX-02` |
| `SAF-011` | Bulk partial failure does not mutate successes | `E2E-S1-09 PASS` trong DOD-FIX-05 |
| `SAF-012` | Terminal reports cannot Ask AI | `E2E-S1-10 PASS`: FE disabled/request 0 + BE 409 |
| `SAF-013` | Manual never equals REJECT | Semantic assertion |
| `SAF-014` | Score never authorizes action | Search/test threshold absence |
| `SAF-015` | No secret in evidence bundle | Automated scan |

Any failure `SAF-001`–`SAF-010` blocks production readiness.
