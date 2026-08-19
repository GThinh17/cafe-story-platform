# Reviewer Dashboard mobile issues

## REV-MOB-001

- Classification: `CODE_BUG`
- Evidence: authenticated Expo Web runtime rendered the profile normally, then clicking `Open reviewer dashboard` blanked the application after payout data loaded. The backend `ReviewerEarningsResponseDTO` returns `status` and `totalFinalAmount`, while the mobile `ReviewerDashboardPayout` contract and payout components read `payoutStatus` and `totalAmount`. `PayoutHistoryList` then calls `replace` on the missing `payoutStatus`, causing the render crash.
- Affected behavior: Reviewer Dashboard cannot open for reviewer/admin accounts with payout history; displayed payout totals are also sourced from a nonexistent field.
- Likely owner: mobile Reviewer Dashboard API response typing and payout presentation.
- Proposed action: align the mobile DTO with the existing backend contract and use `status` plus `totalFinalAmount` throughout the dashboard, history, activities, and mocks.
- Auto-fix allowed: yes, scoped mobile contract repair.

## REV-MOB-ENV-001

- Classification: `CONFIG_ENV`
- Evidence: mobile `.env` points to the previous LAN address `10.35.220.35`, while the current host address is different. Expo remained on `Preparing CafeStory...` because `/api/auth/me` could not reach the backend.
- Affected behavior: authenticated mobile screens cannot initialize, so Reviewer Dashboard cannot be meaningfully tested from this runtime.
- Likely owner: local mobile API base URL configuration.
- Proposed action: use an ephemeral Expo Web override to `http://127.0.0.1:8080` for this test. For a physical device, set the environment-specific LAN URL outside this source fix.
- Auto-fix allowed: no.

## REV-MOB-COVERAGE-001

- Classification: `CONFIG_ENV`
- Evidence: the mobile package exposes `i18n:check` and `typecheck`, but no unit-test or coverage script.
- Affected behavior: changed-file frontend line and branch coverage cannot be measured with current tooling.
- Likely owner: mobile test tooling.
- Proposed action: use static checks and authenticated UI/API runtime evidence; report `FRONTEND_COVERAGE_TOOLING_MISSING`.
- Auto-fix allowed: no.
