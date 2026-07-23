# Merge fix log

## FIXED - textual conflicts

- Resolved all eight conflicted paths without choosing one branch wholesale.
- Preserved the current branch mixed feed, cursor pagination, sponsored items, and Ads dashboard.
- Integrated `develop` batching for follow/like/save state, Story Rail single-API loading, image optimization, Suspense sections, payment history API, and cafe campaign screens.
- Kept both `getPayments` and `getMyPayments` because each has a live caller and a matching authenticated backend endpoint.

## FIXED - MERGE-001

- Removed the duplicate auto-merged `adFees` and `adCampaigns` endpoint blocks.
- Retained one complete `adCampaigns` definition including the `stats` endpoint.
- Re-ran `npm run typecheck`: pass.
- Re-ran `npm run build`: pass.

## NOT AUTO-FIXED - MERGE-002

- Cache warnings were classified as local stale cache data because database fallbacks returned valid pages.
- No source or cache deletion was performed.

## Test-data cleanup

- The smoke test toggled the first post like on and then off; the visible count returned from 1 to 0.
- No payment or campaign was created.
