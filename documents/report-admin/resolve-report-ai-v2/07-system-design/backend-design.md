# Backend Design — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| Architecture | Controller → Service → Repository → PostgreSQL |

## Request pipeline

```text
AdminContentReportController
→ AdminReportAiResolutionService
→ report eligibility
→ target snapshot/evidence factory
→ policy catalog candidate-rule selection
→ signed n8n client
→ semantic validator
→ structured persistence
→ response mapper
```

## Mandatory validation order

1. authenticated Admin;
2. report exists and is `OPEN/REVIEWING`;
3. target association exists;
4. USER/PAGE local manual clamp;
5. candidate reason/rules recognized;
6. snapshot/evidence references complete;
7. webhook response signature/freshness/schema;
8. Rule ID/version allowlist;
9. Evidence ID referential integrity;
10. sufficiency/decision/action compatibility;
11. Backend-derived action risk;
12. sanitized persistence.

## New class boundaries

- `AdminReportEvidenceSnapshotFactory`: read target and generate evidence.
- `AdminReportAiPolicyCatalog`: immutable policy/rule metadata.
- `AdminReportAiSemanticValidator`: no repository/network side effect.
- `AdminReportAiContractV2Mapper`: DTO/entity mapping.
- `AdminReportAiWebhookSigner`: canonical digest/signature.
- `AdminReportAiSanitizer`: log/API/persistence redaction.
- `AdminReportAiActionRiskResolver`: deterministic action risk.

## Compatibility behavior

| Input/history | Behavior |
|---|---|
| Empty create request | Create V2 recommendation |
| `autoApplyEnabled=false` | Create V2 recommendation |
| `autoApplyEnabled=true` | Create recommendation; no job; blocked outcome |
| Legacy V1 row | Map to legacy response view; no fake evidence |
| V2 row | Full structured response; raw omitted |

## Transaction boundaries

- Network/provider call không giữ DB row lock.
- Snapshot is captured before provider call and hashed.
- Persist transaction rechecks report eligibility and target `updatedAt/hash`.
- Stale after provider call → persist optional operational audit, return stale/manual; no action.
- Unique idempotency key resolves concurrent duplicate create.

## Log policy

Allowed:

```text
correlationId, reportId, targetType, rule IDs, outcome code,
duration, provider status class, contract/version
```

Forbidden:

```text
Authorization, HMAC secret/signature, raw request/response,
reporter description, target content, PII, provider body preview
```

## Backend acceptance

- all semantic invalid paths fail closed;
- provider error never becomes `REJECT/RESOLVE`;
- V2 record never populates raw provider field;
- scheduler/worker cannot mutate under A0;
- changed production files meet coverage gate.
