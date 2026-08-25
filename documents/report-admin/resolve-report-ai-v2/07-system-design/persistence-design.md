# Persistence Design — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |

## Additive migration

- Allocate next unused Flyway version after read-only schema-history verification.
- Never edit applied `V20260705_01` or `V20260706_01`.
- Add V2 audit/search fields listed in Sprint 1 DD.
- Preserve legacy rows and legacy auto-job history.
- Do not backfill evidence, rule or version claims not present historically.

## Storage split

| Storage | Content |
|---|---|
| Scalar columns | decision/action, categorical semantics, versions, correlation/idempotency, hash |
| `findings_json` | structured findings and Evidence ID references |
| `evidence_summary_json` | sanitized minimum observations/availability |
| `blocked_reasons_json` | machine-readable reasons |
| Legacy `raw_response` | read-only legacy; V2 writes null |

## Constraints/indexes

- unique correlation and idempotency keys for V2;
- indexes by report, created time, decision, sufficiency;
- categorical checks;
- manual/no-action compatibility;
- JSON object/array type checks where PostgreSQL supports them.

## Retention

Use L1 defaults: V2 sanitized evidence 90 days after closure, structured decision 365 days,
action/audit 730 days. Deletion jobs and legal holds require later runtime implementation/test.
