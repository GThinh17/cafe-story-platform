# Workflow retrospective and improvement proposal

## What the verification loop revealed

The small-to-large verification order worked: source mapping and focused tests showed that the backend design was internally consistent, while runtime preflight immediately exposed that n8n was not serving the expected production webhook. Full E2E then confirmed the user-visible effect as HTTP 502.

The current E2E score model is too permissive. It reports 86% and a healthy latency classification even though every live AI call failed. Future runs must use functional prerequisites before awarding downstream points.

## Required E2E changes

1. Introduce a hard preflight gate: production webhook must not return 404.
2. A latency criterion is eligible only after a valid AI response contract is returned.
3. Bulk success requires at least one successful recommendation and per-item response verification.
4. Countdown/cancel scenarios become `SKIPPED_PREREQUISITE`, not `PASSED`, when no scheduled job exists.
5. Safety-gate tests must use controlled stubbed decisions around threshold boundaries.
6. Seed deterministic BLOG, COMMENT, USER, and CAFE_PAGE fixtures not owned by the admin account.
7. Separate scores for UI rendering, backend contract, n8n/OpenAI, safety, and cleanup; never collapse them into a single optimistic health number.
8. Persist backend correlation IDs and n8n execution IDs in sanitized evidence.

## Required n8n changes

1. One workflow ID and one published webhook per environment.
2. Timestamped HMAC/body hash, replay protection, and ingress allowlist.
3. Policy/schema version and idempotency key in every request.
4. Explicit prompt-injection boundary around untrusted report content.
5. Real image input only through guarded allowlisted URLs; otherwise manual review.
6. Timeout budget aligned with backend.
7. Retry only transient 429/5xx/timeout errors with bounded exponential backoff.
8. Structured error branch and global error workflow.
9. Allowlisted/redacted response and execution metrics.

## Next validation loop

1. Publish a single workflow and prove webhook registration before starting the app.
2. Run one direct signed webhook contract test with a non-destructive fixture.
3. Run backend-to-n8n integration for one BLOG report.
4. Run all four target types without auto-apply.
5. Run safety boundary cases with deterministic AI stubs.
6. Run one scheduled job, cancel it, then run one due job against an unchanged target.
7. Change a target after recommendation and prove the job is skipped as stale.
8. Run bulk selected and filtered modes with partial failures and retry-failed-only.
9. Run regression and cleanup checks.

## Process note

The UI critique snapshot was not persisted under `.impeccable/critique/` because the task bound allowed persistent changes only under `documents/`. This document preserves the critique findings without expanding the write scope.
