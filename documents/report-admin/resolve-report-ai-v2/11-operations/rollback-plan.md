# Sprint 1 Rollback Plan

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` design |
| Trạng thái | `DESIGNED_NOT_EXECUTED` |

## Invariants

- A0 kill switch stays enabled during rollback.
- Rollback cannot restore auto-apply creation/execution.
- Additive DB columns remain; no destructive down migration.
- Legacy and V2 rows remain readable.

## Sequence

1. Disable Ask AI request path if incident requires.
2. Keep worker mutation disabled.
3. Stop/rollback FE to last build that does not expose auto-apply creation.
4. Roll n8n back only to a signed safe export; otherwise keep it inactive.
5. Roll BE back only if A0 guard remains independently enforced.
6. Verify active job count, target/report side effects and audit trail.
7. Record incident/correlation IDs and user impact.

Any observed mutation requires incident handling and target-specific recovery; do not hide it by
rewriting audit rows.
