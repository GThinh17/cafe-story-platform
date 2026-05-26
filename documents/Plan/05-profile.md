# Phase 05 - Profile Integration

## Mục tiêu

Thay phần profile mock bằng dữ liệu current user và blog/review thật của user.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/app/(main)/profile/page.tsx`
- `2-cafe-story-nextjs-web/src/components/profile/profile-page-content.tsx`
- `2-cafe-story-nextjs-web/src/components/profile/profile-header.tsx`
- `2-cafe-story-nextjs-web/src/components/profile/profile-review-grid.tsx`
- `2-cafe-story-nextjs-web/src/components/profile/profile-story-highlights.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/users.ts`
- `2-cafe-story-nextjs-web/src/lib/api/blogs.ts`
- `2-cafe-story-nextjs-web/src/types/user.ts`
- `2-cafe-story-nextjs-web/src/features/profile/`

## Backend liên quan

- `GET /api/auth/me`
- `GET /api/users/{userId}`
- `PATCH /api/users/me`
- `PATCH /api/users/me/region`
- `GET /api/blogs/users/{userId}`
- `GET /api/users/{userId}/followers`
- `GET /api/users/{userId}/following`
- `GET /api/reviewers/{userId}` nếu profile cần reviewer metadata.

## Việc cần làm

1. Giữ `useCurrentUser()` làm nguồn user hiện tại.
2. Tạo `src/lib/api/users.ts`:
   - `getUserById(userId)`;
   - `updateCurrentUser(request)`;
   - `updateCurrentUserRegion(request)`;
   - `getUserFollowers(userId)`;
   - `getUserFollowing(userId)`.
3. Dùng `getBlogsByUser(userId)` trong `src/lib/api/blogs.ts`.
4. Tạo adapter:

```text
src/features/profile/profile-adapter.ts
```

Adapter map:

- auth/current user -> `UserProfile`;
- user blogs -> `ProfileReview[]`;
- followers/following/blog count -> profile stats.

5. Giai đoạn đầu có thể giữ `mockProfileHighlights` nếu backend chưa có story/highlight domain.
6. Tách rõ fallback:
   - fallback avatar;
   - fallback bio/location;
   - empty state nếu user chưa có blog.

## Quy tắc quan trọng

- Không tự lấy `user` từ localStorage.
- Không để `profile-page-content.tsx` import mock reviews khi phase hoàn tất.
- Nếu backend chưa có đủ stats, ghi chú field nào tạm tính từ list response.

## Tiêu chí hoàn thành

- `/profile` hiển thị user thật từ session.
- Review grid hiển thị blog của user thật.
- Stats không còn hard-code từ `mockUserProfile`.
- Empty state rõ nếu user chưa đăng bài.
- `npm run typecheck` pass.

