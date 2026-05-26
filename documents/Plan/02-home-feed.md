# Phase 02 - Home Feed Integration

## Mục tiêu

Thay mock feed ở trang home bằng dữ liệu blog feed thật từ backend, nhưng vẫn giữ UI component hiện có.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/app/(home)/page.tsx`
- `2-cafe-story-nextjs-web/src/components/feed/post-card.tsx`
- `2-cafe-story-nextjs-web/src/components/feed/story-rail.tsx`
- `2-cafe-story-nextjs-web/src/components/feed/top-cafes-nearby.tsx`
- `2-cafe-story-nextjs-web/src/components/feed/cafe-feed-skeleton.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/blogs.ts`
- `2-cafe-story-nextjs-web/src/types/blog.ts`
- `2-cafe-story-nextjs-web/src/types/feed.ts`
- `2-cafe-story-nextjs-web/src/features/blogs/`

## Backend liên quan

- `GET /api/blogs/feed`
- `GET /api/blogs`
- `GET /api/blogs/trending`
- `POST /api/blogs/{blogId}/events` nếu cần track view/click sau này.

## Việc cần làm

1. Tạo `src/lib/api/blogs.ts`:
   - `getBlogFeed()`;
   - `getBlogs()`;
   - `getTrendingBlogs()`.
2. Tạo type backend DTO trong `src/types/blog.ts`.
3. Giữ hoặc chỉnh `FeedPost` trong `src/types/feed.ts` làm UI model.
4. Tạo adapter:

```text
src/features/blogs/blog-feed-adapter.ts
```

Adapter chịu trách nhiệm map:

- `blogId` -> `id`;
- `title/content` -> `caption`;
- `author/user` -> `author`;
- `cafePage` -> `cafe`, `location`;
- image field backend -> `image`;
- like/comment/share count -> UI counters;
- rating nếu backend có, nếu chưa có thì hiển thị fallback an toàn.

5. Cập nhật `src/app/(home)/page.tsx`:
   - gọi API;
   - map DTO sang `FeedPost[]`;
   - truyền xuống `PostCard`.
6. Thêm loading/error/empty state ở route hoặc component wrapper.
7. Không xóa mock ngay nếu các màn khác còn dùng; chỉ ngừng dùng mock ở home feed.

## Rủi ro cần kiểm tra

- Backend DTO có thể thiếu ảnh hoặc rating so với mock UI.
- Một số bài blog có thể không gắn cafe page.
- Feed có thể cần pagination nhưng UI hiện chưa có.

## Tiêu chí hoàn thành

- Trang `/` hiển thị bài thật từ `GET /api/blogs/feed`.
- Không còn import `mockFeedPosts` trong home route.
- Khi API lỗi, UI không crash.
- Khi feed rỗng, có empty state gọn.
- `npm run typecheck` pass.

