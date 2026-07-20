# Fix log

## P0 — viewer-state contract

- Added viewer-aware `getOrganicFeed(UUID viewerUserId, String cursor, int size)` while retaining the two-argument anonymous compatibility method.
- Passed viewer identity through `/api/feed/organic`, anonymous mixed feed, exception fallback, and the internal no-snapshot personalized fallback.
- Kept organic ranking cache viewer-neutral; follow/like/save state is hydrated after ranking retrieval with batch queries.
- Re-hydrated viewer state on personalized cache hits so cached rankings do not make like/save/follow stale after interactions.
- Preserved self-follow=false and verified no per-post `exists...` queries.

## P1 — eager media

- Replaced raw `index === 0` behavior with the ID of the first item whose `kind === "post"`.
- Sponsored rendering, pagination, slot placement, and later-post lazy loading are unchanged.

## P2 — payment history

- Both `/api/payments` and `/api/payments/me` now delegate to `PaymentService.getMyPayments`.
- Removed the duplicate `PaymentHistoryService`, implementation, and implementation test after caller audit.
- Ported buyer/status/mapping regression coverage into `PaymentServiceImplTest`.
- Web Ads dashboard now uses typed `getMyPayments`; mobile remains unchanged and still calls `/api/payments`.

## P3 — Ads UX

- Removed the static pricing card that only opened `/ads`; kept the owner-only purchasable Feed Advertising Pack.
- Changed paid-package campaign links to `/ads?paymentId=...`.
- Replaced `/cafes/campaigns/new` with a server compatibility redirect and removed the unused 415-line duplicate campaign form.
- Kept `/ads` as the canonical package/payment/campaign flow and retained the sidebar link and payment-return target.

## Issue loop

- `MCF-001 CONFIG_ENV`: stale Maven incremental output; cleaned `target` and reran exact checks — resolved.
- `MCF-002 CONFIG_ENV`: frontend coverage tooling missing — documented, not source-fixed.
- `MCF-003 CONFIG_ENV`: first Next build worker failed under concurrent Maven load; independent rerun and final rerun passed — resolved.
- `MCF-004 CODE_BUG`: personalized cache returned stale viewer fields; added batch re-hydration and regression test — resolved.
- `MCF-005 CODE_BUG`: no-snapshot personalized fallback forced anonymous state; threaded viewer ID and added regression test — resolved.
- `MCF-006 TEST_BUG`: new test executed async rebuild inline; isolated executor and reran — resolved.
- `MCF-007 TEST_DATA`: no owner/paid-package/ad-first fixture; documented without changing seed — accepted limitation.

## Cleanup

- Like smoke data was restored to its initial unliked state and verified after refresh.
- Backend/web runtime processes on ports 8080/3000 were stopped after evidence capture.
- No payment, campaign, post, migration, or seed record was created.
