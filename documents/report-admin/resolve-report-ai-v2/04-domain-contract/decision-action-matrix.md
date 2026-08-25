# Decision–Action Matrix 2.0

| Target | Manual | Reject | Resolve |
|---|---|---|---|
| BLOG | `NO_ACTION` | `KEEP_VISIBLE` | `HIDE` or `REMOVE` candidate |
| COMMENT | `NO_ACTION` | `KEEP_VISIBLE` | `HIDE` or `REMOVE` candidate |
| USER | `NO_ACTION` | Not produced by Sprint 1 AI | Not produced |
| CAFE_PAGE | `NO_ACTION` | Not produced by Sprint 1 AI | Not produced |

Invalid combinations are semantic failures:

- manual + any mutation/keep action;
- reject + hide/remove;
- resolve + keep/no action;
- USER/PAGE + resolve/suspend in Sprint 1;
- sufficient decision with critical missing/conflict.

Backend clamps invalid trusted-correlation response to manual/no action and records structured
blocked reasons.
