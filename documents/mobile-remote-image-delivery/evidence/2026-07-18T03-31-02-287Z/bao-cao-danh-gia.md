# Evaluation report

## Result

- Phase 1 mobile image compatibility: PASS at code, compiler, and remote HTTP-check level.
- Phase 2 temporary Wikimedia thumbnail CDN plus native cache: PASS at code, compiler, dependency, and remote HTTP-check level.
- Cloudinary Fetch: still unavailable, but it is not used by the approved temporary route.
- Post-fix device-level UI validation: NOT RUN because `adb` is unavailable.

## Verified changes

- DiceBear `/svg` URLs are normalized to `/png` before reaching React Native `Image`.
- Wikimedia requests receive a descriptive CafeStory mobile User-Agent through `expo-file-system`, independently of React Native `Image` header forwarding.
- Original Wikimedia bitmap URLs are converted to standard 1280px thumbnail URLs on mobile only.
- Native clients download the thumbnail into the Expo cache, atomically promote a completed temporary download, and render the local file URI.
- Concurrent requests share one download, while the original DB URL remains the network fallback if the thumbnail cannot be cached.
- Feed images use Android resize decoding and show a fallback icon after all downloads or local decoding fail.
- Backend responses and database URLs are unchanged.

## Validation

- `npm run typecheck`: FAIL on the unchanged baseline and changed code because TypeScript exceeds Node's default stack.
- `node --stack_size=8192 node_modules/typescript/bin/tsc --noEmit`: PASS.
- Pure helper smoke check through TypeScript `transpileModule`: PASS.
- `npm --prefix 3-cafe-story-reactnative-mobile ls expo-file-system --depth=1`: PASS (`19.0.23`, deduplicated).
- `npx expo export --platform android --output-dir <temporary-path>`: PASS (2808 modules bundled); temporary output cleaned.
- `npx expo-doctor`: 16/17 checks PASS; existing Expo patch mismatch recorded as MRI-009.
- Project structure check: PASS with no warnings.
- Live DiceBear PNG request: PASS (`200 image/png`).
- Live `ngo_duc1` Wikimedia 1280px thumbnail with the CafeStory User-Agent: PASS (`200 image/jpeg`, 121710 bytes).
- Earlier affected 11 MB original is reduced to a 267088-byte thumbnail.
- Five representative seed thumbnails: PASS after retry; two first-generation HEAD requests were transiently rate-limited.
- `git diff --check`: PASS.

## Coverage

The mobile app has no unit coverage tooling. See `coverage/coverage-limitation.txt` and `issue.md`.

## Residual risks

- Cloudinary Fetch remains restricted for a future migration away from Wikimedia delivery.
- Wikimedia can transiently rate-limit thumbnail creation; mobile falls back to the original image URL.
- The cache can grow until the operating system evicts it; no application-level LRU policy was added for this temporary route.
- The post-fix local-file rendering path has not been observed on a connected Android device in this run.

## Cleanup

No test users, database rows, Cloudinary assets, or background jobs were created. The temporary Android bundle was removed.
