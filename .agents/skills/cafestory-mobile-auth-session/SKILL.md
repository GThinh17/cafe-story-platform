---
name: cafestory-mobile-auth-session
description: Enforce CafeStory React Native authentication and session handling for mobile API work. Use when Codex edits mobile auth flows, API client authorization, token persistence, logout/refresh behavior, protected mobile endpoints, EXPO_PUBLIC_API_BASE_URL usage, or any React Native screen/service that depends on authenticated backend requests.
---

# CafeStory Mobile Auth Session

## Purpose

Use this skill before connecting protected backend APIs to `3-cafe-story-reactnative-mobile`. CafeStory backend supports both `Authorization: Bearer <accessToken>` and HTTP-only cookies, but React Native cookie behavior is not the same as the Next.js browser client.

## Required Context

Read these files before changing auth or protected API calls:

- `3-cafe-story-reactnative-mobile/AGENTS.md`
- `3-cafe-story-reactnative-mobile/src/services/api/client.ts`
- `3-cafe-story-reactnative-mobile/src/services/api/auth.ts`
- `3-cafe-story-reactnative-mobile/src/features/auth/auth-provider.tsx`
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/controller/AuthController.java`
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/until/security/JwtAuthenticationFilter.java`
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/config/SecurityConfig.java`

## Rules

- Do not rely on web-only cookie assumptions for React Native.
- Prefer backend-supported bearer auth for protected mobile requests when access tokens are available.
- Keep environment access inside `src/config/env.ts`; do not read `process.env` from screens or components.
- Keep auth orchestration in `src/features/auth`; screens must use `useAuth()`.
- Keep HTTP behavior in `src/services/api/client.ts`; do not add direct `fetch` calls in screens.
- Keep endpoint paths in `src/services/api/endpoints.ts`.
- Preserve backend response envelope handling: `{ statusCode, status, message, data }`.
- If refresh-token support is needed, verify the backend exposes a mobile-safe refresh strategy before implementing it.

## Workflow

1. Inspect the backend auth contract.
   - Confirm login/register response fields from `AuthResponse`.
   - Confirm whether the endpoint sets cookies, returns tokens, or both.
   - Confirm protected endpoints accept bearer tokens through `JwtAuthenticationFilter`.
2. Inspect mobile auth state.
   - Check whether access tokens are stored in memory only or persisted.
   - Check how logout clears user and credentials.
   - Check how startup calls `/api/auth/me`.
3. Update mobile API plumbing.
   - Add token attachment in the shared API client if the app stores an access token.
   - Keep token state updates inside auth provider or a small auth session helper.
   - Do not scatter Authorization header construction across service files.
4. Handle user states.
   - Unauthorized responses should clear stale auth state or surface a re-login path.
   - Loading and error states should be explicit in screens that depend on auth.
5. Validate.
   - Run from `3-cafe-story-reactnative-mobile`: `npm run typecheck`.
   - If backend auth changed, run focused Maven tests or `mvn test` from `1-cafe-story-backend-javaspring`.

## Implementation Notes

When modifying auth, report the chosen session strategy clearly:

- access-token storage location
- whether requests use bearer auth or cookies
- how logout clears credentials
- what happens on 401
- any backend limitation still unresolved
