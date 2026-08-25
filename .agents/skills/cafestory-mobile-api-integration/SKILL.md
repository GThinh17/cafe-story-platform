---
name: cafestory-mobile-api-integration
description: Connect CafeStory React Native screens, hooks, components, and services to backend APIs. Use when Codex replaces mobile mock data with real API calls, adds mobile service files, updates mobile endpoint constants, creates request/response types, wires loading/empty/error/unauthorized states, or ports API behavior from the Next.js web app into the Expo mobile app.
---

# CafeStory Mobile API Integration

## Purpose

Use this skill for API integration in `3-cafe-story-reactnative-mobile`. It keeps API logic out of screens, mirrors backend/web contracts, and preserves the mobile app's existing navigation, theme, auth, and UI conventions.

## Always Pair With

- Use `cafestory-engineering-workflows` for the general CafeStory workflow.
- Use `cafestory-api-contract-sync` when adding or changing endpoint contracts.
- Use `cafestory-mobile-auth-session` when the endpoint requires authentication.

## Required Discovery

Before editing:

1. Read `3-cafe-story-reactnative-mobile/AGENTS.md`.
2. Inspect `3-cafe-story-reactnative-mobile/package.json` scripts.
3. Search current mobile API usage:
   - `rg "fetch\\(|apiFetch|apiEndpoints|useAuth" 3-cafe-story-reactnative-mobile/src`
4. For the target domain, inspect matching web API files:
   - `2-cafe-story-nextjs-web/src/lib/api`
   - `2-cafe-story-nextjs-web/src/types`
5. Confirm backend routes and DTOs with `cafestory-api-contract-sync`.

## Placement Rules

- Screens: `src/screens/<domain>/<screen-name>.tsx`
- Domain components: `src/components/<domain>`
- Shared UI primitives: `src/components/ui`
- API clients and services: `src/services/api`
- Endpoint constants: `src/services/api/endpoints.ts`
- Shared types: `src/types`
- Shared hooks: `src/hooks`
- Mock data: `src/mocks`

Do not put reusable UI, mock data, endpoint paths, API functions, or auth orchestration directly inside screen files.
Keep screens focused on data orchestration and composition. Put domain-specific forms, modals, sheets, cards, and repeated UI pieces in `src/components/<domain>` so they stay easy to edit and maintain.

## Integration Workflow

1. Add or extend contract types.
   - Keep request/response types in `src/types`.
   - Match backend DTO and existing web types where they are correct.
2. Add endpoint constants.
   - Put paths in `src/services/api/endpoints.ts`.
   - URL-encode dynamic path segments.
3. Add service functions.
   - Use `apiFetch` from `src/services/api/client.ts`.
   - Use service files per domain, such as `blogs.ts`, `users.ts`, or `cafes.ts`.
   - Export new service functions from `src/services/api/index.ts`.
4. Wire the screen or hook.
   - Keep screen code focused on state, composition, and user actions.
   - Handle loading, empty, error, refresh, mutation pending, and unauthorized states when relevant.
   - Preserve existing navigation route constants and typed params.
5. Remove mock usage only for the screen/domain being integrated.
   - Leave unrelated mock data alone.

## UI State Rules

- Use existing `LoadingState`, `EmptyState`, `Screen`, `Button`, and theme tokens when possible.
- Keep mobile UI copy in English by default. Do not introduce hardcoded Vietnamese labels, placeholders, empty states, errors, or action text unless the user explicitly requests Vietnamese for that screen.
- Do not create one-off colors, spacing, or button variants in screens.
- Keep social actions responsive: like, unlike, comment, follow, save, share, and report should show pending/error feedback when wired.
- Do not add new state or data-fetching libraries unless the project already uses them or the user explicitly asks.

## Mobile Networking Notes

- Base URL must come through `src/config/env.ts` as `EXPO_PUBLIC_API_BASE_URL`.
- Android emulator usually uses `http://10.0.2.2:8080`.
- iOS simulator usually uses `http://localhost:8080`.
- Physical devices should use the LAN IP of the backend machine.
- Protected requests must follow `cafestory-mobile-auth-session`.

## Validation

Run from `3-cafe-story-reactnative-mobile`:

```powershell
npm run typecheck
```

Run Expo only when the change needs manual runtime verification:

```powershell
npm run start
```

Report any backend dependency, missing endpoint, or auth/session limitation explicitly.
