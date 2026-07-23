# Merge validation issues

## MERGE-001

- Classification: `CODE_BUG`
- Evidence: `npm run typecheck` reported `TS1117` twice in `src/lib/api/endpoints.ts` and `TS2339` for `adCampaigns.stats` in `src/lib/api/ads.ts`.
- Affected behavior: the web application cannot pass TypeScript validation after merging `develop` into the current branch.
- Likely owner: code produced by Git auto-merge.
- Proposed action: merge the duplicate `adFees` and `adCampaigns` endpoint objects into one complete definition that includes `stats`.
- Auto-fix allowed: yes.

## MERGE-002

- Classification: `CONFIG_ENV`
- Evidence: `logs/backend.log` contains cache deserialization warnings for stale `cafePageDetails` and `userProfileBlogs` entries.
- Affected behavior: cache reads fall back to the database; the tested pages still returned HTTP 200 and rendered normally.
- Likely owner: local Redis/cache data created with an older serialized shape.
- Proposed action: clear only the affected local cache namespace if the warnings persist; do not change application source during this merge repair.
- Auto-fix allowed: no.

## MERGE-003

- Classification: `CONFIG_ENV`
- Evidence: the web package has no unit coverage script in `package.json`.
- Affected behavior: frontend line/branch coverage cannot be measured in the current toolchain.
- Likely owner: frontend test configuration.
- Proposed action: rely on typecheck, production build, and browser smoke evidence for this merge; add frontend coverage tooling as a separate task.
- Auto-fix allowed: no.
