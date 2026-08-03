# Contract Test Matrix — Sprint 1

| ID | Layer | Input | Expected |
|---|---|---|---|
| `CT-001` | BE DTO | Valid V2 | Accepted |
| `CT-002` | BE DTO | Missing contract/correlation | Reject |
| `CT-003` | BE | Unknown Rule ID | Manual fallback |
| `CT-004` | BE | Unknown Evidence ID | Manual fallback |
| `CT-005` | BE | Critical missing + RESOLVE | Clamp manual/no action |
| `CT-006` | BE | REJECT + HIDE | Clamp semantic invalid |
| `CT-007` | BE | USER + SUSPEND_USER | Local manual; no provider |
| `CT-008` | BE | CAFE_PAGE + RESOLVE | Local manual; no provider |
| `CT-009` | BE | duplicate idempotency | One persisted result |
| `CT-010` | BE | stale snapshot after provider/cache lookup | Stale/manual, no reuse; executable Backend tests `PASS` in `DOD-FIX-02` |
| `CT-011` | n8n | invalid signature | Reject before provider |
| `CT-012` | n8n | stale timestamp | Reject before provider |
| `CT-013` | n8n | replay nonce | Reject before provider |
| `CT-014` | n8n | invented field/rule | Strip/fallback |
| `CT-015` | API | V2 response | No raw response |
| `CT-016` | API | Legacy row | Readable, `LEGACY V1` semantics |
| `CT-017` | API | autoApply=true | Recommendation + A0 blocked, no job |
| `CT-018` | DB | invalid categorical value | Constraint reject |

Contract tests must assert both shape and semantics; JSON Schema alone is insufficient.
