---
target: 2-cafe-story-nextjs-web/src/app/(main)
total_score: 19
p0_count: 0
p1_count: 4
timestamp: 2026-06-18T16-44-30Z
slug: 2-cafe-story-nextjs-web-src-app-main
---
## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 2/4 | No loading/empty/error states on Notifications, Messages. Story rail has no seen/unseen visual distinction. |
| 2 | Match System / Real World | 3/4 | BadgeDollarSignIcon reads as "payment" not "upgrade". Notification drawer looks like a nav link. |
| 3 | User Control and Freedom | 2/4 | No category filter reset, no search X-button, notification Sheet showCloseButton=false. |
| 4 | Consistency and Standards | 2/4 | Create Post: modal on desktop, route on mobile, different labels. |
| 5 | Error Prevention | 2/4 | loadTopCafes silently falls to mockCafeSummaries. activeCategoryId never read by filtering logic. |
| 6 | Recognition Rather Than Recall | 2/4 | Sidebar icon-only at rest. Story rail button has no aria-label. |
| 7 | Flexibility and Efficiency | 2/4 | No keyboard shortcuts. focus-within sidebar works. URL-persisted filters good. |
| 8 | Aesthetic and Minimalist Design | 2/4 | font-serif italic in Explore h1 and TrendingCafeCard. shadow-2xl on hover (spec: shadow-lg). |
| 9 | Error Recovery | 1/4 | Silent mock data on error. No error boundary. Notifications cannot distinguish empty from failed. |
| 10 | Help and Documentation | 1/4 | No tooltips, no onboarding, no contextual guidance. Empty notification pane has no message. |
| **Total** | | **19/40** | **Acceptable — significant improvements needed** |

## Anti-Patterns Verdict

BORDERLINE FAIL. Post card, sidebar expand, and auth shell are on-brand. ExploreSearchHeader uses font-serif italic h1 (exact anti-reference). TrendingCafeCard uses font-serif for cafe names (violates single-font rule). Explore page structure (serif hero → category pills → 3-up photo grid → editorial collections) is the warm-SaaS-discovery-scaffold DESIGN.md rejects. Detector: 0 automated findings.

## Priority Issues

[P1] Category filter is entirely decorative — activeCategoryId never consumed by filtering logic. Fix: wire to filtering or hide until backend supports.
[P1] font-serif usage in ExploreSearchHeader h1 and TrendingCafeCard violates single-font contract. Fix: replace with Inter font-black/font-bold.
[P1] Silent mock data fallback on API errors destroys trustworthiness. Fix: implement error/empty/loading states, remove mock as production fallback.
[P1] Notifications and Messages pages have no data states (loading, empty, error). Fix: implement all three states with meaningful messages.
[P2] Create Post split: modal on desktop vs route on mobile, different labels. Fix: unify mechanism and label.

## Persona Red Flags

Casey: Category filter does nothing. No label on "Post" CTA in bottom nav. Filter state resets on context switch.
Jordan: Icon-only sidebar. Notification panel has no drawer affordance. Empty notifications show nothing. No onboarding.
Sam: No prefers-reduced-motion guard. Story rail button has no aria-label. disabled on profile removes from tab order. Sheet showCloseButton=false.
Linh: Repeated marketing h1 on Explore for habitual users. Category filter cosmetic only. No location context on trending. No near-me affordance.

## Minor Observations

- Ghost button hover missing Silver Skin background (only color change, not background shift)
- TrendingCafeCard uses native <a> not Next.js <Link>
- Two different negative-margin sidebar offset hacks across pages
- StoryRail key={story.name} — collision risk, should use stable id
- sidebar-2xl on hover not in DESIGN.md vocabulary
