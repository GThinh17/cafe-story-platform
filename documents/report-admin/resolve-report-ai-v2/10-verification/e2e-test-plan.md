# E2E Test Plan — Admin Report AI Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| App | Admin Next.js → Spring Boot → n8n → OpenAI → Spring Boot → Admin |
| Account | Existing Admin test account; credential must not enter evidence |
| Test file | `5-cafe-story-nextjs-admin/tests/e2e/admin-report-ai.spec.ts` |

## Required scenarios

| ID | Scenario | Expected |
|---|---|---|
| `E2E-S1-01` | Login/open Reports | ADMIN authorized |
| `E2E-S1-02` | OPEN BLOG Ask AI | V2 recommendation card |
| `E2E-S1-03` | COMMENT with context | Evidence-first result |
| `E2E-S1-04` | COMMENT missing critical context | Manual/no action |
| `E2E-S1-05` | USER report | Local manual-only; no suspension |
| `E2E-S1-06` | CAFE_PAGE report | Local manual-only; no suspension |
| `E2E-S1-07` | Auto-apply UI | Creation controls absent |
| `E2E-S1-08` | Legacy scheduled job | Read/cancel only |
| `E2E-S1-09` | Bulk mixed results | Counts separated by stage |
| `E2E-S1-10` | Terminal report | Ask AI disabled/rejected |
| `E2E-S1-11` | V1 history | Legacy badge; raw hidden |
| `E2E-S1-12` | Prompt injection fixture | No scope/rule escape |
| `E2E-S1-13` | Provider unavailable | Operational error + retry, no manual fiction |
| `E2E-S1-14` | Side effects | Report/target states unchanged |
| `E2E-S1-15` | Cleanup | No active test report/job remains |

## Rewrite of legacy E2E

Existing cases that expect schedule/countdown/replacement must be changed:

- RAI-14/15/17/18 assert A0 blocked/no job/no mutation;
- RAI-16 retains cancel only when a pre-existing safe scheduled fixture exists;
- RAI-26/27 no longer enable bulk auto-apply; assert controls absent;
- contract assertion requires evidence/version fields and forbids `rawResponse`.

## Evidence

- screenshots for recommendation/manual/error/legacy/bulk;
- sanitized request/response with IDs masked when needed;
- target/report before-after state;
- n8n execution ID/status without provider raw content;
- cleanup verification.

Missing COMMENT fixture keeps COMMENT scenarios `BLOCKED/TEST_DATA`; it cannot be silently omitted
from a full-pass claim.

## DOD-FIX-03 execution record

- `E2E-S1-04`: `PASS` ngày `2026-07-29`.
- Fixture: COMMENT `"Đúng vậy."` có parent BLOG content whitespace trên PostgreSQL disposable.
- Full path: Admin UI → Backend → n8n/OpenAI → Backend → Admin UI.
- Actual: HTTP `200`, `NEEDS_MANUAL_REVIEW + NO_ACTION`, findings rỗng,
  `CRITICAL_EVIDENCE_MISSING`, `UNASSESSABLE`.
- Side effects: target không mutation; auto-apply job `0`; cleanup report/comment/blog `0/0/0`.
- Evidence:
  `../09-sprints/evidence/2026-07-29T14-22-18-183+07-00/`.

## DOD-FIX-04 execution record

- `E2E-S1-13`: `PASS` ngày `2026-07-29`.
- Fault injection: dừng chính xác local container `cafestory-n8n`, gửi Ask AI trên Admin UI,
  khởi động lại container, chờ exact webhook rồi retry cùng report.
- Failure actual: HTTP `502`, `AI_PROVIDER_BOUNDARY_FAILED`, request correlation ID,
  `retryable=true`, `stage=N8N_PROVIDER`; resolution count `0 -> 0`.
- UI actual: safe message, error code, stage, support reference và `Retry available`.
- Recovery actual: HTTP `200`, Contract V2, resolution count `1`.
- Side effects: report/target không mutation; auto-apply job `0`; cleanup report/blog `0/0`.
- Evidence:
  `../09-sprints/evidence/2026-07-29T14-53-04-516+07-00/`.

## DOD-FIX-05 execution record

- `E2E-S1-09`: `PASS` ngày `2026-07-29`.
- Fault injection: chọn hai OPEN report trong bulk UI, chuyển một report thành `RESOLVED` sau
  selection rồi chạy bulk.
- Actual: per-item HTTP `200/409`; UI Completed `1`, Manual review `1`, Failed `1`, Remaining `0`.
- Safety: success resolution `0 -> 1`; failed resolution `0 -> 0`; no target/report AI mutation;
  auto job `0`; cleanup `0/0`.
- Evidence:
  `../09-sprints/evidence/2026-07-29T15-20-53-107+07-00/`.

## DOD-FIX-06 execution record

- `E2E-S1-10`: `PASS` ngày `2026-07-29`.
- Fixture: synthetic BLOG report được chuyển thành `RESOLVED`.
- FE actual: Ask AI disabled; DOM click không phát sinh request.
- BE actual: direct bypass HTTP `409`.
- Safety: resolution `0 -> 0`; report/target không mutation; auto job `0`; cleanup `0/0`.
- Evidence:
  `../09-sprints/evidence/2026-07-29T15-20-53-108+07-00/`.
