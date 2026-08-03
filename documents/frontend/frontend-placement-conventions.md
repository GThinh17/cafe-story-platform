# CafeStory Frontend Placement Conventions

This document defines the file placement rules for `2-cafe-story-nextjs-web`.
Use it as the source of truth when creating, moving, or reviewing frontend files.

## Core Rule

Place files by responsibility, not by whatever feature folder is nearby.

```text
src/app/                 Next.js route groups, pages, layouts, loading/error files
src/components/          React UI components, grouped by rendered domain
src/components/ui/       Generic reusable UI primitives
src/components/layout/   Shells, sidebars, navigation, app layout components
src/components/providers/ App-level React providers
src/features/            Domain adapters, mappers, feature logic, mocks, feature-only types
src/hooks/               React hooks
src/lib/api/             API clients, endpoint paths, fetch wrappers
src/lib/                 Shared utilities, constants, auth helpers
src/types/               Shared reusable TypeScript types
src/mocks/               Shared mock/demo data
public/images/           Static image assets
public/icons/            Static icon assets
```

## Components

All React UI components must live under `src/components`.

Use domain folders for UI that renders a specific product area:

```text
src/components/auth/
src/components/cafe/
src/components/feed/
src/components/layout/
src/components/message/
src/components/notification/
src/components/profile/
src/components/providers/
src/components/review/
src/components/reviewer-dashboard/
src/components/ui/
```

Do not place UI components in `src/features`.

Examples:

```text
Correct:
src/components/cafe/cafe-action-buttons.tsx
src/components/cafe/cafe-menu-modal.tsx
src/components/reviewer-dashboard/reviewer-dashboard-page.tsx

Incorrect:
src/features/cafes/components/cafe-action-buttons.tsx
src/features/reviewer-dashboard/components/reviewer-dashboard-page.tsx
```

## Features

`src/features` is for domain logic that is not UI rendering.

Allowed in `src/features/<domain>/`:

```text
adapters
mappers
normalizers
formatters that are feature-specific
domain orchestration helpers
feature-only mock data
feature-only types
```

Examples:

```text
src/features/blogs/blog-feed-adapter.ts
src/features/cafes/cafe-ranking-adapter.ts
src/features/reviewer-dashboard/reviewer-dashboard.mock.ts
src/features/reviewer-dashboard/reviewer-dashboard.types.ts
```

Do not add `components/` or `hooks/` folders under `src/features`.

## Hooks

All React hooks must live under `src/hooks`.

Examples:

```text
src/hooks/use-current-user.ts
src/hooks/use-bfcache-restore.ts
src/hooks/use-cafe-menu-modal.ts
src/hooks/use-media-query.ts
```

Do not place hooks in `src/features/<domain>/hooks`.

## Types

Use `src/types` for reusable app-wide types and API response/request shapes.

Use `src/features/<domain>/*.types.ts` only when the type is tightly scoped to feature logic and not reused by UI or API clients outside that feature.

Examples:

```text
src/types/cafe.ts
src/types/blog.ts
src/types/feed.ts
src/features/reviewer-dashboard/reviewer-dashboard.types.ts
```

## API Clients

All API request functions and endpoint path definitions must stay under `src/lib/api`.

Examples:

```text
src/lib/api/client.ts
src/lib/api/endpoints.ts
src/lib/api/blogs.ts
src/lib/api/cafes.ts
src/lib/api/users.ts
```

Do not call `fetch` directly from UI components unless there is a framework-specific reason and the pattern is already established.

## Routes

Route files stay in `src/app`.

`page.tsx` should compose routes and delegate UI to components. Avoid large JSX blocks, reusable logic, mapping logic, or direct API helper definitions inside route files.

Route groups:

```text
src/app/(home)/       Home feed route
src/app/(main)/       Main authenticated app routes with sidebar
src/app/(auth)/       Login/register routes
src/app/(dashboard)/  Reviewer/admin-style dashboard routes
```

Only the root `src/app/layout.tsx` should contain `<html>`, `<body>`, global CSS imports, and global providers.

## Current Refactor Targets

The following existing files should be moved to match this convention:

```text
src/features/cafes/components/*        -> src/components/cafe/*
src/features/cafes/hooks/*             -> src/hooks/*
src/features/reviewer-dashboard/components/* -> src/components/reviewer-dashboard/*
```

After moving files, update imports and remove empty folders.

## Validation

After any placement refactor, run:

```powershell
cd 2-cafe-story-nextjs-web
npm run typecheck
npm run build
```

If validation cannot run, report the exact blocker.
