# Phase 01 - API Foundation And Auth

## Mục tiêu

Chuẩn hóa tầng gọi API để các phase sau chỉ cần thêm endpoint/domain client, không phải sửa lại cách fetch hoặc auth nhiều lần.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/lib/api/client.ts`
- `2-cafe-story-nextjs-web/src/lib/api/endpoints.ts`
- `2-cafe-story-nextjs-web/src/lib/api/auth.ts`
- `2-cafe-story-nextjs-web/src/lib/auth.ts`
- `2-cafe-story-nextjs-web/src/types/api.ts`
- `2-cafe-story-nextjs-web/src/types/auth.ts`
- `2-cafe-story-nextjs-web/src/hooks/use-current-user.ts`
- `2-cafe-story-nextjs-web/src/proxy.ts`

## Backend liên quan

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

## Việc cần làm

1. Kiểm tra `apiFetch` đang:
   - dùng `NEXT_PUBLIC_API_BASE_URL`;
   - gửi `credentials: "include"`;
   - parse đúng envelope `{ data, message, ... }`;
   - throw `ApiError` ổn định cho UI.
2. Chuẩn hóa `apiEndpoints` theo domain:
   - `auth`;
   - `blogs`;
   - `cafes`;
   - `comments`;
   - `users`;
   - `notifications`;
   - `chat`.
3. Giữ auth chính bằng HttpOnly cookie, không dùng Bearer token làm luồng chính.
4. Logout phải:
   - gọi `POST /api/auth/logout`;
   - xóa localStorage fallback như `auth_token`, `user`, `access_token`, `refresh_token`;
   - redirect về `/login`;
   - refresh route state.
5. `useCurrentUser` chỉ gọi `getMe()` và trả về:
   - `user`;
   - `isLoading`;
   - `error`;
   - `refetch`.
6. Không để component tự đọc cookie/token.

## Cấu trúc mong muốn

```text
src/lib/api/client.ts
src/lib/api/endpoints.ts
src/lib/api/auth.ts
src/lib/auth.ts
src/hooks/use-current-user.ts
src/types/api.ts
src/types/auth.ts
```

## Tiêu chí hoàn thành

- Login xong vào được protected routes.
- Refresh trang protected route vẫn giữ session nếu cookie còn hợp lệ.
- Logout xóa cookie backend và localStorage fallback.
- `npm run typecheck` pass.
- Không có component nào gọi `fetch("/api/auth...")` trực tiếp.

