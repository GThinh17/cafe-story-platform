# Fix log

## Phase 1 - Implemented

- Added a shared mobile image-source adapter.
- Rewrote DiceBear `/svg` delivery to `/png` on mobile only.
- Added a descriptive User-Agent for Wikimedia delivery.
- Added feed-image fallback UI and Android resize decoding.

## Phase 2 - Implemented with Wikimedia thumbnails

- Added deterministic Wikimedia 1280px standard-thumbnail URL generation.
- Kept the original DB URL as an automatic second source.
- Verified the affected image shrank from 11096171 bytes to 267088 bytes.
- Preserved backend responses and all database values.

## MRI-001 - NOT FIXED, NOT USED

Cloudinary Fetch remains restricted. The user explicitly approved Wikimedia thumbnail CDN as the temporary Phase 2 route.

## MRI-003 - DIAGNOSED

The unchanged baseline reproduces the TypeScript stack overflow. The same compiler passes with a larger Node stack, so no unrelated package script change was made.

## MRI-004 - FIXED

The bulk check was rerun from the mobile directory and resolved the local TypeScript package.

## MRI-005 - NOT MUTATED

The Redis feed cache had expired. Existing data was not regenerated solely for evidence; representative previously observed URLs were checked instead.

## MRI-006 - MITIGATED

Transient Wikimedia `429` responses were not hidden. The carousel falls back to the original URL, and subsequent sequential requests returned `200` for all five representative thumbnails.

## MRI-007 - FIXED IN CODE

- Added `expo-file-system` using the SDK 54-compatible version.
- Wikimedia thumbnails are now downloaded with the CafeStory User-Agent through `File.downloadFileAsync`, whose Android implementation applies supplied headers directly to its OkHttp request.
- Successful downloads are moved from a temporary file into the Expo cache and rendered through a local `file://` URI.
- Concurrent requests for the same URL share one download promise.
- Thumbnail download failure still falls back to downloading the original DB URL.
- Post-fix device UI verification remains pending because this run has no connected Android runtime or `adb`.

## MRI-008 - FIXED

Captured the validated remote URI before entering the asynchronous closure. The exact increased-stack TypeScript command passed after the change.

## MRI-009 - NOT FIXED

Expo doctor passed 16 of 17 checks. The existing Expo patch mismatch is dependency-maintenance scope and was not bundled into the image fix.

## MRI-010 - FIXED

Reran the dependency-tree check with `npm --prefix 3-cafe-story-reactnative-mobile`; `expo-file-system@19.0.23` is installed and deduplicated for Expo.

## MRI-011 - FIXED

Reran Android export to an explicit temporary path. Metro bundled 2808 modules successfully, produced 26 output files, and the verified temporary directory was removed afterward.
