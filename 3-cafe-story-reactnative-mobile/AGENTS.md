# CafeStory Mobile Agent Guide

This is the Expo React Native app for CafeStory. It owns mobile UI, navigation, client API calls, mobile state, and mobile-specific composition only.

## Placement Rules

- Screens go in `src/screens/<domain>/<screen-name>.tsx`.
- Shared UI primitives go in `src/components/ui`.
- Domain components go in `src/components/<domain>`.
- Navigation files go in `src/navigation`.
- API clients and endpoint definitions go in `src/services/api`.
- Shared hooks go in `src/hooks`.
- Shared types go in `src/types`.
- Mock data goes in `src/mocks`.

Do not put reusable UI, mock data, API logic, route names, or auth orchestration directly inside screen files.

## Config Rules

- Do not read `process.env` directly inside screens or components.
- All environment access must go through `src/config/env.ts`.
- The backend base URL is `EXPO_PUBLIC_API_BASE_URL`.
- Android emulator usually uses `http://10.0.2.2:8080`.
- iOS simulator usually uses `http://localhost:8080`.
- A physical device should use the LAN IP of the machine running the backend.

## Theme Rules

- Do not repeat hardcoded colors, spacing, or typography scale in screens.
- Use `src/theme` for shared colors, spacing, and typography.
- Keep screen styling consistent with CafeStory's calm cafe/social review feel.
- The global CafeStory palette is:
  - Primary: `#3E2723`
  - Secondary: `#8D6E63`
  - Tertiary: `#1D312B`
  - Neutral: `#757575`
- Use `colors.primary`, `colors.secondary`, `colors.tertiary`, and `colors.neutral` instead of raw hex values.
- Button styling must use the shared `Button` component and its supported variants: `primary`, `secondary`, `inverted`, `outlined`.
- Do not create one-off button colors inside screens. Add a variant to `src/components/ui/button.tsx` only when the design system needs it.

## Icon Rules

- Recommended icon library: `lucide-react-native`.
- Use icon components from `lucide-react-native` for common actions such as home, search, user, edit, delete, bookmark, heart, comment, notification, and logout.
- Keep icon color tied to `src/theme/colors.ts`; do not hardcode icon colors in screens.
- Prefer icon buttons for compact actions. Pair icon plus label only when the action needs text clarity.

## Navigation Rules

- Do not use raw string route names inside screens.
- Use route constants from `src/navigation/routes.ts`.
- Keep navigation params typed in `src/navigation/types.ts`.
- Auth-only screens live in AuthStack.
- Main app screens live in MainTabs.
- RootNavigator decides whether the app shows AuthStack or MainTabs.

## API Rules

- Do not call `fetch` directly from screens.
- Use `src/services/api/client.ts` for HTTP calls.
- Keep endpoint paths in `src/services/api/endpoints.ts`.
- Keep request/response types in `src/types`.

## Auth Rules

- Auth state belongs in `src/features/auth`.
- Screens should use `useAuth()` instead of deciding auth flow themselves.
- Logout should call the auth service, clear auth state, and return to the auth flow.

## Validation Rules

- Use `npx tsc --noEmit` for a quick type check.
- Use `npm run start` to run Expo.
- Do not run heavy native builds unless native configuration changed or the user asks.
