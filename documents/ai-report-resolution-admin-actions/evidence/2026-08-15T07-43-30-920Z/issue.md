# AI Report Resolution issues

## ISSUE-001 — Duplicate Target ID locator

- Classification: `TEST_BUG`
- Discovered by: `E2E-REPORT-ACTIONS BLOG and COMMENT require admin confirmation and expose policy`
- Symptom: Playwright strict mode found the same Target ID in the report metadata, BLOG field, and current-target card.
- Product impact: none; the runtime UI rendered the expected target information.
- Narrow fix: scope the visibility assertion to the first exact Target ID match. Apply the same explicit first-match rule to repeated policy identifiers.
- Cleanup: the test `finally` block restored target status and changed created report status to `REJECTED`.

## ISSUE-002 — Policy API envelope parsing

- Classification: `TEST_BUG`
- Discovered by: focused E2E retry after ISSUE-001.
- Symptom: the policy request returned HTTP 200, but the test read `recommendationOnly` from the envelope root instead of the standard API `data` field.
- Product impact: none; the Admin API client already unwraps the same envelope correctly.
- Narrow fix: parse the Playwright response through the existing generic `parseResponse<AiPolicy>` helper.
- Cleanup: the test `finally` block restored target status and closed created reports.

## ISSUE-003 — Policy Sheet layered below report dialog

- Classification: `PRODUCT_BUG`
- Discovered by: focused E2E retry after ISSUE-002.
- Symptom: the Policy Sheet rendered and its controls were visible, but the parent report dialog overlay (`z-[70]`) sat above the Sheet overlay/content (`z-50`) and intercepted pointer events.
- Product impact: an admin could open the policy panel but could not reliably switch tabs or interact with its controls.
- Narrow fix: allow a `SheetContent` caller to set a scoped overlay class, then render only the AI policy Sheet overlay at `z-[80]` and its content at `z-[81]`.
- Cleanup: the test `finally` block restored target status and closed created reports.
