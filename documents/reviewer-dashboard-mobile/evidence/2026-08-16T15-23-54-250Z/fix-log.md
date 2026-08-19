# Reviewer Dashboard mobile fix log

## Scope

- Mobile Reviewer Dashboard payout contract and presentation only.
- No backend endpoint, database, Admin UI, AI Report workflow, authentication rule, or authorization rule changed.

## Root cause

The backend `ReviewerEarningsResponseDTO` exposes `status`, `totalBaseAmount`, and `totalFinalAmount`. The mobile type exposed the nonexistent fields `payoutStatus` and `totalAmount`. After authenticated dashboard data loaded, `PayoutHistoryList` evaluated `statusLabel(item.payoutStatus)`, and `statusLabel` called `replace` on `undefined`. That render exception blanked the Expo Web application.

## Changes

- Aligned `ReviewerDashboardPayout` with the backend response fields, including badge, multiplier, final amount, status, and paid timestamp.
- Changed dashboard wallet, all-time total, activity feed, and payout history to use `totalFinalAmount`.
- Changed payout status rendering to use `status`.
- Preserved the payout badge returned by the backend when monthly badge history is unavailable.
- Updated reviewer payout mocks to the same contract and valid backend status values.

## Verification

- `npm run typecheck`: PASS.
- `npm run i18n:check`: PASS, 27 dictionary keys and 527 UI phrase catalog entries.
- `rg -n "payoutStatus|totalAmount" src -g "*.ts" -g "*.tsx"`: no matches.
- Metro Web bundle request: HTTP 200, 7,620,423 bytes.
- Focused `git diff --check`: PASS; CRLF conversion warnings only.
- Ports 8080 and 8081 after testing: STOPPED.

## Runtime limitation

The blank screen was reproduced through an authenticated real UI before the fix. Post-fix interactive browser verification and screenshot capture could not be completed because the available browser tool denied further localhost inspection under its security policy. The post-fix source passed type/i18n checks and Metro bundling, but these do not replace an authenticated UI rerun.

`FRONTEND_COVERAGE_TOOLING_MISSING`: the mobile package has no unit-test or coverage script for changed-file coverage.
