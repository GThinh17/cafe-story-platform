---
name: cafestory-api-contract-sync
description: Keep CafeStory API contracts aligned across the Spring Boot backend, Next.js web client, and React Native mobile client. Use when Codex adds, ports, reviews, or fixes endpoints, request/response TypeScript types, backend DTO mappings, endpoint constants, API service functions, or cross-app API behavior.
---

# CafeStory API Contract Sync

## Purpose

Use this skill when an API contract crosses app boundaries. The backend is the source of truth for routes and DTOs, the Next.js web app often contains the most complete TypeScript client, and the React Native app should mirror the same contract without copying screen-specific UI assumptions.

## Source Order

1. Backend controller route and method:
   - `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/controller`
2. Backend request/response DTOs:
   - `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/requestDTO`
   - `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/responseDTO`
3. Backend response envelope:
   - `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/config/GlobalResponseAdvice.java`
   - `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/until/FormatResponse.java`
4. Existing web API pattern:
   - `2-cafe-story-nextjs-web/src/lib/api`
   - `2-cafe-story-nextjs-web/src/types`
5. Mobile target API pattern:
   - `3-cafe-story-reactnative-mobile/src/services/api`
   - `3-cafe-story-reactnative-mobile/src/types`

## Workflow

1. Locate the backend endpoint.
   - Search controller annotations with `rg "@RequestMapping|@GetMapping|@PostMapping|@PatchMapping|@PutMapping|@DeleteMapping" 1-cafe-story-backend-javaspring/src/main/java/com/cafestory/controller`.
   - Read the exact controller and DTO files for the domain.
2. Confirm the response shape.
   - Backend responses are wrapped as `{ statusCode, status, message, data }` unless explicitly excluded.
   - Avoid inventing `data` fields that are not present in the DTO.
3. Compare the web client.
   - Reuse endpoint names, query parameter names, request types, and response type names when they match backend evidence.
   - If web and backend disagree, treat backend as source of truth and mention the mismatch.
4. Port to mobile.
   - Add endpoint constants in `3-cafe-story-reactnative-mobile/src/services/api/endpoints.ts`.
   - Add service functions under `3-cafe-story-reactnative-mobile/src/services/api`.
   - Add shared types under `3-cafe-story-reactnative-mobile/src/types`.
   - Export new services/types from existing index files.
5. Validate contract usage.
   - Protected endpoints must follow `cafestory-mobile-auth-session`.
   - List endpoints should preserve pagination, cursor, page, size, and filter query names.
   - Mutations should use backend request DTO fields, not UI model names.

## Checks

- No JPA entity class is exposed as a frontend type source.
- No endpoint path is duplicated inline in components or screens.
- Dynamic path params are URL-encoded consistently.
- Optional and nullable fields match backend DTO behavior or the existing web type.
- API client handles empty responses and wrapped responses.
- Error handling preserves backend `message` when available.

## Validation

Use narrow validation first:

- Mobile TypeScript: `npm run typecheck` from `3-cafe-story-reactnative-mobile`.
- Web TypeScript if web contracts changed: `npm run typecheck` from `2-cafe-story-nextjs-web`.
- Backend tests if controllers or DTOs changed: `mvn test` from `1-cafe-story-backend-javaspring`.

Report any API contract assumptions in the final answer.
