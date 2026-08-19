# Workflow improvement

## Observed gap

The mobile package does not have a component/unit test runner or changed-file coverage instrumentation. A response-field regression therefore reached runtime even though the TypeScript client asserted an incorrect local type.

## Recommended follow-up

- Generate or share reviewer response types from one contract source across backend, web, and mobile.
- Add a focused contract fixture test for `getReviewerPayouts` that renders `PayoutHistoryList` with a backend-shaped payload.
- Add a Reviewer Dashboard smoke test that signs in, opens the reviewer route, waits for payout history, and asserts the root view remains mounted.
- Add frontend coverage reporting before enforcing changed-file thresholds.

These are workflow improvements only and were not added in this scoped repair.
