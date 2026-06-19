---
timestamp: 2026-06-18T15-57-33Z
slug: 2-cafe-story-nextjs-web-src-app-main
---
## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 2 | No post timestamps; notifications panel hardcoded empty |
| 2 | Match System / Real World | 3 | Familiar social metaphors; "Pricing plan" in nav feels commercial |
| 3 | User Control and Freedom | 2 | Category filters no click handler; search not wired |
| 4 | Consistency and Standards | 3 | Consistent shadcn; inactive sidebar hover:text-muted has zero feedback |
| 5 | Error Prevention | 2 | Search submits nowhere; category chips non-interactive |
| 6 | Recognition Rather Than Recall | 2 | Icon-only sidebar by default; no tooltips on collapsed state |
| 7 | Flexibility and Efficiency | 2 | No keyboard shortcuts; category filter not interactive |
| 8 | Aesthetic and Minimalist Design | 3 | Clean overall; uppercase tracked eyebrow 3x on Explore page |
| 9 | Error Recovery | 2 | Page-level fallback exists; individual component error states absent |
| 10 | Help and Documentation | 1 | No help; no onboarding |
| **Total** | | **22/40** | **Acceptable** |

## Anti-Patterns Verdict
Detector: clean (0 findings). LLM: uppercase tracked eyebrow used 3x on Explore page (ExploreSearchHeader, TrendingCafeCard, CafeCategoryList). Story ring gradient teal->rose is strongest Instagram clone signal.

## Priority Issues
- [P1] Uppercase tracked eyebrow 3x on Explore — ExploreSearchHeader:9, TrendingCafeCard:40, CafeCategoryList:23
- [P1] Icon-only sidebar requires recall — no tooltips in collapsed state
- [P1] Story ring gradient (teal->rose) is loudest Instagram clone tell
- [P2] No mobile navigation — MessageDock hidden xl+, story rail wraps instead of scrolls
- [P2] Search and category filters non-functional

## Persona Red Flags
- Jordan: no CTA for unauthenticated users; search and filters silently fail
- Casey: left-side nav on mobile, no bottom nav, messages hidden on mobile
- Coffee Enthusiast: Explore search and category filters do nothing

## Minor Observations
- Inactive sidebar hover:text-muted has zero visual feedback
- Post cards w-[85%] no rationale
- No post timestamps in PostCard
- Notifications items=[] hardcoded empty
