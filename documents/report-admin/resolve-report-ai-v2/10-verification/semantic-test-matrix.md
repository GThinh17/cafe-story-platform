# Semantic Test Matrix — Sprint 1

| ID | Case | Expected |
|---|---|---|
| `SEM-001` | Reporter says “scam”, target has no evidence | Manual, not RESOLVE |
| `SEM-002` | Many reports, no independent evidence | Count only triage signal |
| `SEM-003` | AI explanation repeats allegation | Not evidence |
| `SEM-004` | High likelihood + insufficient evidence | Manual |
| `SEM-005` | High-quality evidence but missing required pattern | Insufficient/manual |
| `SEM-006` | Conflicting observations | Conflicted/manual |
| `SEM-007` | Unsupported free-form rule | Invalid/manual |
| `SEM-008` | BLOG clear content-level finding | Candidate action only |
| `SEM-009` | COMMENT lacks parent context | Unassessable/manual |
| `SEM-010` | One content implies actor suspension | Forbidden |
| `SEM-011` | Opinion framed as misinformation | Not a factual violation |
| `SEM-012` | Regulated goods without jurisdiction | Manual |
| `SEM-013` | IP claimant authority missing | Human/legal manual |
| `SEM-014` | Image critical but not evaluated | Manual |
| `SEM-015` | Provider failure | Operational error, not content decision |

Every case asserts decision, action, sufficiency, blocked reasons and provider-call behavior.

## DOD-FIX-03 execution record

`SEM-009` đã được kiểm chứng ở hai lớp:

- unit regression: blank parent context được packet builder đánh dấu `UNUSABLE/MISSING`;
- semantic regression: `criticalEvidenceMissing=true` ghi đè provider `INSUFFICIENT` thành
  `UNASSESSABLE`;
- full-path E2E: `E2E-S1-04` `PASS`, UI hiển thị đúng blocked reason và sufficiency.

Evidence:
`../09-sprints/evidence/2026-07-29T14-22-18-183+07-00/`.
