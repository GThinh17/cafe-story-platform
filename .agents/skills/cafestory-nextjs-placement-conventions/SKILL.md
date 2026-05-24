---
name: cafestory-nextjs-placement-conventions
description: Enforce CafeStory Next.js web file placement. Use when Codex creates, moves, reviews, or refactors Next.js routes, pages, layouts, reusable UI components, feature modules, hooks, types, mocks, API clients, utilities, or assets in `2-cafe-story-nextjs-web`.
---

# CafeStory Next.js Placement Conventions

## Rule

Put each new web file in the folder that matches its responsibility. Do not place reusable UI, mock data, types, API helpers, or feature logic directly in `page.tsx`.

Before adding files, inspect the existing folders and reuse the closest matching one.

## Placement Map

Use this structure for `2-cafe-story-nextjs-web`:

```text
src/app/                 Route groups, pages, layouts, loading/error files
src/components/ui/       Generic reusable primitives: Button, Input, Modal
src/components/layout/   App shell/navigation/layout-only components
src/components/feed/     Feed-specific UI: posts, stories, side rail feed widgets
src/components/cafe/     Cafe cards, cafe headers, cafe galleries
src/components/review/   Review composer, review cards, rating UI
src/components/message/  Message dock, chat preview, conversation UI
src/components/profile/  Profile header, review grid, profile stats
src/features/<domain>/   Domain orchestration, hooks, actions, adapters
src/lib/api/             API client, endpoint definitions, fetch wrappers
src/lib/                 App-wide utilities, constants, auth helpers
src/hooks/               Shared React hooks
src/types/               Reusable TypeScript shapes
src/mocks/               Fake/sample/demo data only
public/images/           Static image assets
public/icons/            Static icon assets
```

## Route Files

- Route groups have meaning in this project:
  - `src/app/(home)/` is reserved for the home feed only. It should contain the `/` feed route and its route-group layout; do not place Explore, Notifications, Messages, Profile, Create Post, or other non-feed screens under `(home)`.
  - `src/app/(main)/` is for non-admin authenticated/main app screens that share the sidebar shell, such as `/explore`, `/notifications`, `/messages`, `/profile`, and `/reviews/new`.
  - `src/app/(auth)/` is for login/register and other auth-only screens without the sidebar.
  - Admin screens, when implemented, should live outside `(home)` and `(main)` in their own admin route group.
- Keep `page.tsx` focused on route composition: import data, import components, arrange layout.
- Keep route group layout in `src/app/(group)/layout.tsx` when the layout applies only to that group.
- Avoid large JSX blocks in `page.tsx`; extract them into `src/components/<domain>/`.

## Component Files

- Put a component in the domain folder that describes what it renders.
- If a component is usable across multiple domains and has no business meaning, put it in `src/components/ui/`.
- If a component is a shell/navigation/sidebar/topbar, put it in `src/components/layout/`.
- Name files in kebab-case, such as `post-card.tsx`, and exported components in PascalCase, such as `PostCard`.

## Data, Types, And Logic

- Put mock data in `src/mocks/` using named exports with a `mock` prefix.
- Put shared types in `src/types/`; do not define reusable data shapes inside components or pages.
- Put API calling code in `src/lib/api/`, not inside components.
- Put domain-specific orchestration in `src/features/<domain>/` when a feature needs multiple helpers, hooks, or adapters.

## Quick Decision

- New route screen? `src/app/.../page.tsx`
- Home feed route? `src/app/(home)/page.tsx`
- Main non-admin app route? `src/app/(main)/.../page.tsx`
- Auth route? `src/app/(auth)/.../page.tsx`
- Reusable visual component? `src/components/<domain>/`
- Shared primitive? `src/components/ui/`
- Sidebar/navigation/shell? `src/components/layout/`
- Fake data? `src/mocks/`
- Type/interface? `src/types/`
- API fetch/client? `src/lib/api/`
- Shared hook? `src/hooks/`

## Validation

After moving or creating web files, run:

```powershell
npm run build
```
