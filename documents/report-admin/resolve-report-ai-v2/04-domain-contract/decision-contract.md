# Report Decision Contract 2.0

| Decision | Required condition | Action compatibility |
|---|---|---|
| `NEEDS_MANUAL_REVIEW` | Critical missing/conflict/out-of-scope/invalid semantics | `NO_ACTION` |
| `REJECT` | All material findings not supported with sufficient evaluated evidence | `KEEP_VISIBLE` for content |
| `RESOLVE` | At least one applicable finding supported with sufficient evidence and no blocking conflict | `HIDE/REMOVE` candidate for content |

Rules:

- missing evidence is not evidence report is false;
- provider failure is not a report decision;
- report count/reason severity/model confidence cannot directly select decision;
- each `RESOLVE` cites Rule ID/version and Evidence IDs;
- USER/CAFE_PAGE Sprint 1 always manual/no action;
- report decision is distinct from target action and execution.
