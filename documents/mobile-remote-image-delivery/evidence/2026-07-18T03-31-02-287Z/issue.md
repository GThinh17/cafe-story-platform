# Issues

## MRI-001 - Cloudinary Fetch is restricted

- Classification: `CONFIG_ENV`
- Evidence: `raw/cloudinary-fetch-check.txt`
- Affected behavior: Cloudinary Fetch remains unavailable, but the user approved Wikimedia thumbnail CDN as the temporary Phase 2 route.
- Likely owner: Cloudinary account configuration.
- Proposed action: Enable authenticated/allowed Fetch delivery for `upload.wikimedia.org`, or explicitly approve a Wikimedia thumbnail-CDN fallback.
- Auto-fix allowed: no.

## MRI-002 - Mobile unit coverage tooling is unavailable

- Classification: `CONFIG_ENV`
- Evidence: `coverage/coverage-limitation.txt` (created after validation).
- Affected behavior: Changed-file line and branch coverage cannot be measured for this Expo app.
- Likely owner: Mobile test tooling/configuration.
- Proposed action: Use TypeScript validation for this scoped fix; add a test runner only as a separate requested task.
- Auto-fix allowed: no.

## MRI-003 - TypeScript checker exceeds the default Node stack

- Classification: `CONFIG_ENV`
- Evidence: `logs/typecheck-attempt-1.txt` and `logs/typecheck-baseline.txt`.
- Affected behavior: The standard mobile typecheck command cannot complete with Node's default stack.
- Likely owner: Existing project/compiler complexity; the unchanged baseline fails with the same stack trace.
- Proposed action: Validate this task with the same compiler using an increased Node stack. Treat changing the project-wide script as a separate tooling task.
- Auto-fix allowed: no.

## MRI-004 - Bulk thumbnail smoke script used the wrong working directory

- Classification: `TEST_BUG`
- Evidence: `logs/thumbnail-bulk-attempt-1.txt`.
- Affected behavior: The first bulk check could not import the mobile app's local TypeScript package.
- Likely owner: Test command.
- Proposed action: Rerun the same read-only check from `3-cafe-story-reactnative-mobile`.
- Auto-fix allowed: yes.

## MRI-005 - Personalized feed cache expired before the bulk URL check

- Classification: `TEST_DATA`
- Evidence: `logs/thumbnail-bulk-attempt-2.txt`.
- Affected behavior: The read-only bulk check could not rediscover all cached feed image URLs.
- Likely owner: Ephemeral Redis test data.
- Proposed action: Validate the affected URL and representative previously observed seed URLs without mutating cache or database state.
- Auto-fix allowed: no.

## MRI-006 - Wikimedia briefly rate-limited first-time thumbnail checks

- Classification: `EXTERNAL_SERVICE`
- Evidence: `raw/image-endpoint-check.txt`.
- Affected behavior: Two of five representative thumbnail HEAD requests initially returned `429`; all five subsequent GET requests returned `200`.
- Likely owner: Wikimedia thumbnail generation/delivery.
- Proposed action: Keep the original URL as a client fallback and avoid prefetch bursts.
- Auto-fix allowed: no.

## MRI-007 - Android image loader does not deliver the Wikimedia User-Agent reliably

- Classification: `CODE_BUG`
- Evidence: the affected `ngo_duc1` Wikimedia original and 1280px thumbnail both return `403` with Android's generic `okhttp/4.12.0` User-Agent and `200` with the CafeStory User-Agent; the device screenshot reaches the carousel fallback after both network sources fail.
- Affected behavior: Wikimedia post images remain unavailable on Android even though the thumbnail URL is valid and small enough to decode.
- Likely owner: Mobile remote-image delivery implementation.
- Proposed action: Download Wikimedia images through a header-aware file request, cache the result locally, and render the local URI instead of relying on React Native `Image` to forward the header.
- Auto-fix allowed: yes.

## MRI-008 - URI narrowing was lost inside the asynchronous cache closure

- Classification: `CODE_BUG`
- Evidence: increased-stack TypeScript check reported `TS2345` at `src/services/api/image-cache.ts` for three uses of `source.uri` inside an async closure.
- Affected behavior: the new cache implementation does not pass strict TypeScript validation.
- Likely owner: Mobile image-cache implementation.
- Proposed action: capture the validated URI in a local string before creating the asynchronous closure.
- Auto-fix allowed: yes.

## MRI-009 - Expo doctor reports an existing Expo patch mismatch

- Classification: `CONFIG_ENV`
- Evidence: `npx expo-doctor` passed 16 of 17 checks and reported installed `expo 54.0.34` while the current validator expects `~54.0.36`.
- Affected behavior: the project is not on the latest Expo SDK 54 patch recommended by the current validator; no image-cache type error was reported.
- Likely owner: Mobile dependency maintenance.
- Proposed action: upgrade Expo in a separate dependency-maintenance change and run the full mobile regression suite.
- Auto-fix allowed: no.

## MRI-010 - Dependency-tree check ran from the repository root

- Classification: `TEST_BUG`
- Evidence: the combined validation command ended with `npm ls expo-file-system --depth=1` reporting an empty tree because it ran above the mobile package.
- Affected behavior: the first dependency verification did not prove the installed mobile module version.
- Likely owner: Validation command.
- Proposed action: rerun the dependency-tree check with the mobile package as its npm prefix.
- Auto-fix allowed: yes.

## MRI-011 - Combined export-and-cleanup command was rejected by shell safety policy

- Classification: `TEST_BUG`
- Evidence: the first Android export command combined creation and recursive cleanup of a computed temporary path and was rejected before execution.
- Affected behavior: no Android bundle evidence was produced by the first attempt; application files were not changed.
- Likely owner: Validation command composition.
- Proposed action: export to a new explicit temporary path, verify it in a separate read-only step, then clean that exact resolved path separately.
- Auto-fix allowed: yes.
