---
target: 5-cafe-story-nextjs-admin/src/app
total_score: 21
p0_count: 0
p1_count: 4
p2_count: 1
timestamp: 2026-06-18T17-23-13Z
slug: 5-cafe-story-nextjs-admin-src-app
---
## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Skeletons + optimistic updates work; no success toast after moderation resolve |
| 2 | Match System / Real World | 2 | Backend enum values exposed verbatim: NEEDS_REVIEW, VIOLATION, APPROVE as button labels |
| 3 | User Control and Freedom | 2 | No logout in sidebar; access-denied screen offers no escape path; no undo |
| 4 | Consistency and Standards | 2 | AdminStatusBadge + charts use raw Tailwind colors outside the token system; eyebrow label repeated on every surface |
| 5 | Error Prevention | 3 | Confirm dialog before destructive actions; login guards; filter resets fine |
| 6 | Recognition Rather Than Recall | 3 | Sidebar labels visible; filters show current state; UUIDs sliced to 8 chars force context-switching |
| 7 | Flexibility and Efficiency | 1 | No keyboard shortcuts; no bulk actions; no page-size control; serial-only moderation workflow |
| 8 | Aesthetic and Minimalist Design | 2 | Uppercase eyebrow labels on every surface; 4 action buttons per moderation row; chart colors escape design system |
| 9 | Error Recovery | 2 | Error messages name the problem but offer no retry; access-denied gives no actionable next step |
| 10 | Help and Documentation | 1 | No tooltips; no explanation of scores; no contextual help anywhere |
| **Total** | | **21/40** | **Acceptable — significant improvements needed** |

## Anti-Patterns Verdict

**LLM assessment**: Two patterns flag: (1) Uppercase eyebrow label reflex — text-xs font-bold uppercase tracking appears on AdminStatCard labels, the "Admin" kicker in every AdminPageHeader, every AdminDetailField dt, and inline labels in the moderation dialog. Not one deliberate eyebrow; an eyebrow on every surface. (2) 8 identical stat cards with zero visual priority differentiation.

**Deterministic scan**: detect.mjs returned [] — no structural violations. The uppercase overuse is a density-of-use problem not caught by the scanner. Clean pass on gradient text, nested cards, side-stripe borders.

## Overall Impression

Solid functional foundation — data fetching patterns are correct, optimistic updates work, design token system is largely respected. Two failure modes: (1) visual-language inconsistency where AdminStatusBadge and charts import raw Tailwind colors that ignore the CafeStory palette; (2) operator experience gap where the interface treats admins like they know the backend schema rather than people doing a job.

## What's Working

1. Loading and error states are complete — skeletons, error cards, optimistic row updates all present.
2. Shell architecture is clean — AdminAuthGuard separation, usePagedAdminResource hook correctly handles abort/race conditions, aria-current on active sidebar links.
3. Login page is appropriately secure-feeling with correct register for an admin entry point.

## Priority Issues

### [P1] AdminStatusBadge breaks the design token system
Uses bg-emerald-100/text-emerald-800, bg-amber-100/text-amber-800, bg-red-100/text-red-800, bg-blue-100/text-blue-800. In dark mode these won't invert and will look like imported artifacts. Appears on every data page.
Fix: Map to system tokens — success: bg-primary/10 text-primary-strong; warning: bg-rating/10 text-rating; danger: bg-accent/10 text-accent; neutral: bg-surface-muted text-coffee-muted.
Suggested command: /impeccable polish

### [P1] Chart components use raw color utilities
bg-emerald-500, bg-red-500, bg-amber-500, bg-blue-500 in AdminDashboardCharts. Same dark-mode failure. Doesn't share semantic meaning with status badge colors.
Fix: Define semantic chart tokens using design system hues. Reference in both charts and badges.
Suggested command: /impeccable colorize

### [P1] No navigation on viewports below lg (1024px)
AdminSidebar is lg:fixed only. Below lg it renders in normal document flow stacked above content. On tablets/small laptops: broken layout.
Fix: Add mobile drawer using existing Sheet component. Hamburger trigger in main content area at <lg. Keep fixed sidebar at >=lg.
Suggested command: /impeccable adapt

### [P1] Backend enum values exposed as UI labels
Buttons say "APPROVE", "HIDE", "REMOVE". Badges show "NEEDS_REVIEW", "VIOLATION". Non-technical ops admins need plain language.
Fix: Add display-name map in admin-status-badge.tsx. Normalize button labels to Title Case.
Suggested command: /impeccable clarify

### [P2] Sidebar: 10 ungrouped items, no logout
10 flat nav links exceed working memory ceiling. No sign-out action anywhere. Security concern.
Fix: Group into Content / Operations / Finance sections. Add Sign out button pinned to sidebar bottom.
Suggested command: /impeccable layout

## Persona Red Flags

**Alex (Power User)**: No bulk resolve. Fixed page size of 12. REMOVE button adjacent to HIDE with no visual distance. 300+ clicks/session for high-volume moderation queues.

**Sam (Accessibility-Dependent User)**: Status conveyed by color alone in AdminStatusBadge. All-caps badge text may be spelled out by screen readers. Charts have no aria-label or accessible alternative.

**CafeStory Ops Admin (project-specific)**: Raw score floats (0.73) with no explanation. No warning when AI status is PROCESSING. Confirm dialog is generic ("This applies the selected moderation resolution") with no content/author context.

## Minor Observations

- AdminSidebar user card has no action and no sign-out.
- AdminAccessDenied has no "Sign out and try again" link.
- AdminPageHeader has hardcoded "Admin" eyebrow above every page title — adds no information.
- formatDate hardcodes "en" locale; Vietnamese admins get US date format.
- No debounce on FilterSelect onChange — stacks fetches on slow connections.

## Questions to Consider

- "Does a moderator need raw floats (0.73) or a verdict in plain language?"
- "What if Pending moderation had a red accent and Failed payments had different weight than Comments?"
- "Is there a workflow to review a moderation result and the related report in the same view?"
