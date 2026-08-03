# Candidate Action Contract 2.0

| Action | Target | Reversibility | Sprint 1 |
|---|---|---|---|
| `NO_ACTION` | All | N/A | Required manual/error |
| `KEEP_VISIBLE` | BLOG/COMMENT | No mutation | Candidate |
| `HIDE` | BLOG/COMMENT | Reversible | Candidate, human only |
| `REMOVE` | BLOG/COMMENT | Higher impact | Candidate, C1 quorum before execution |
| `KEEP_ACTIVE` | USER/PAGE | No mutation | Legacy/manual compatibility |
| `SUSPEND_USER` | USER | High impact | Forbidden AI candidate |
| `SUSPEND_PAGE` | PAGE | High impact | Forbidden AI candidate |

Action risk is derived from target, action, reversibility and blast radius. Model suggestion never
authorizes execution. A0 blocks every AI-triggered mutation and report closure.
