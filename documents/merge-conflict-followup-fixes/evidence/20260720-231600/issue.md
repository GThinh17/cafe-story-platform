# Issues

## MCF-001

- Classification: `CONFIG_ENV`
- Evidence: focused Maven run at 2026-07-20 23:19 +07 failed during `testCompile`.
- Affected behavior: backend tests could not start.
- Observed: Maven reported main classes as up to date, then test compilation produced broad `class file not found` and incompatible-type errors across unchanged feed tests.
- Likely owner: stale incremental Maven output under `target/` after branch/merge changes.
- Proposed action: clean backend build output and rerun the exact focused test command.
- Auto-fix allowed: no source-code fix; build-cache cleanup only.

## MCF-002

- Classification: `CONFIG_ENV`
- Evidence: `2-cafe-story-nextjs-web/package.json` exposes only `dev`, `build`, `typecheck`, and `start` scripts.
- Affected behavior: changed frontend files cannot be measured with unit coverage.
- Likely owner: frontend test tooling is not configured.
- Proposed action: use typecheck, production build, browser smoke, DOM/network inspection, and screenshots.
- Auto-fix allowed: no; adding a test framework is outside this task.

## MCF-003

- Classification: `CONFIG_ENV`
- Evidence: first `npm run build` at 2026-07-20 23:25 +07 ran concurrently with full Maven tests.
- Affected behavior: Next.js production build stopped while collecting page data.
- Observed: compilation and TypeScript passed; a Next.js build worker then exited with code 1 without a source diagnostic.
- Likely owner: local worker/resource contention during parallel full-stack validation.
- Proposed action: rerun the same web build independently; only classify as a code bug if a reproducible source diagnostic appears.
- Auto-fix allowed: no source-code fix at this stage.

## MCF-004

- Classification: `CODE_BUG`
- Evidence: browser smoke at 2026-07-20 23:34 +07 liked blog `ad57b382-66ec-44d0-8f92-389d8cc54e48`; the UI became active and Hibernate inserted `blog_likes`, but a full page reload returned `isLike=false` and count `0` for the same first post.
- Affected behavior: signed-in mixed feed can show stale follow/like/save state whenever personalized rankings are served from cache, so the P0 viewer-state contract is not reliable outside the organic fallback path.
- Root cause: `personalizedFeedRankings` caches fully hydrated `BlogFeedResponse` objects and returns them without refreshing viewer-specific fields.
- Proposed action: retain cached ranking/content payload, but batch re-hydrate follow/like/save state on every personalized-cache hit; reset self-follow to `false`; add a regression test proving state can change while ranking cache remains reused.
- Auto-fix allowed: yes; narrow `CODE_BUG` fix within P0 and no ranking, cursor, schema, ad, payment, or mobile changes.

## MCF-005

- Classification: `CODE_BUG`
- Evidence: final call-site audit found `getPersonalizedFeed` returning `fallbackOrganicFeedResponses(safeSize)` when `latestComputedAt == null`, while that helper called the compatibility overload `getOrganicFeed(null, size)` and therefore forced anonymous viewer state.
- Affected behavior: a signed-in user with no personalized snapshot can receive organic fallback posts with `isLike/isSave/isAuthorFollowing/isPageFollowing=false` even though the mixed-feed exception fallback is viewer-aware.
- Root cause: current-branch stale-while-revalidate fallback retained the old anonymous organic call when develop's batch viewer-state contract was merged.
- Proposed action: pass `userId` through the internal personalized fallback and add a regression test that verifies the viewer-aware organic overload is used.
- Auto-fix allowed: yes; narrow P0 call-site fix with no formula, cache key, cursor, feed size, ad pacing, schema, payment, or mobile change.

## MCF-006

- Classification: `TEST_BUG`
- Evidence: first focused rerun of `BlogFeedRankingServiceImplTest` after MCF-005 produced 18 tests with 1 failure; the new test expected one batch follow query but observed three.
- Affected behavior: regression test mixed the foreground organic fallback with the test fixture's synchronous execution of the normally asynchronous recommendation rebuild.
- Root cause: the shared test factory executes `TaskExecutor` runnables inline; this added rebuild scoring and hydration calls that are unrelated to the fallback assertion.
- Proposed action: stub the executor to `doNothing()` in this test, matching the existing stale-snapshot async isolation test, then rerun the same focused check.
- Auto-fix allowed: yes; test-only isolation fix.

## MCF-007

- Classification: `TEST_DATA`
- Evidence: runtime account `th***@gmail.com` owns no cafe page, has no paid Ads package/campaign, and the live mixed feed places sponsored items at configured later slots rather than before the first post.
- Affected behavior: owner-only Feed Advertising Pack rendering, paid-package campaign creation, and the exact ad-at-index-zero eager-load edge cannot be demonstrated with live seeded data.
- Verified subset: non-owner pricing correctly omits both the owner-only pack and the removed static Ads card; `/ads` shows valid empty states; first real post media is `loading=eager/fetchpriority=high`, second post is lazy; code selects the first `kind === "post"` independent of raw array position; legacy redirects preserve `paymentId`.
- Proposed action: keep source unchanged; add a dedicated owner + paid-package + ad-first fixture in a separate test-data task if full runtime coverage of those states is required.
- Auto-fix allowed: no; changing seed data is outside bounds.
