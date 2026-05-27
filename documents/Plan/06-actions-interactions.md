# Phase 06 - Actions And Interactions

## Mục tiêu

Kết nối các hành động người dùng trên UI với backend: like, unlike, share, follow, unfollow, comment.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/components/feed/post-card.tsx`
- `2-cafe-story-nextjs-web/src/components/review/blog-detail-review.tsx`
- `2-cafe-story-nextjs-web/src/components/cafe/cafe-page.tsx`
- `2-cafe-story-nextjs-web/src/components/cafe/cafe-card.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/blogs.ts`
- `2-cafe-story-nextjs-web/src/lib/api/comments.ts`
- `2-cafe-story-nextjs-web/src/lib/api/cafes.ts`
- `2-cafe-story-nextjs-web/src/lib/api/users.ts`
- `2-cafe-story-nextjs-web/src/features/interactions/`

## Backend liên quan

Blog:

- `POST /api/blogs/{blogId}/likes`
- `DELETE /api/blogs/{blogId}/likes`
- `GET /api/blogs/{blogId}/likes`
- `POST /api/blogs/{blogId}/shares`
- `GET /api/blogs/{blogId}/shares`

Comment:

- `POST /api/comments`
- `PATCH /api/comments/{commentId}`
- `DELETE /api/comments/{commentId}`

Cafe page:

- `POST /api/cafe-pages/{cafePageId}/likes`
- `DELETE /api/cafe-pages/{cafePageId}/likes`
- `POST /api/cafe-pages/{cafePageId}/follows`
- `DELETE /api/cafe-pages/{cafePageId}/follows`

User follow:

- `POST /api/users/{followingUserId}/followers`
- `DELETE /api/users/{followingUserId}/followers`

## Việc cần làm

1. Làm từng action nhỏ, không làm tất cả cùng lúc.
2. Thứ tự đề xuất:
   - blog like/unlike;
   - comment create;
   - blog share;
   - cafe like/follow;
   - user follow.
3. Mỗi action cần trạng thái:
   - idle;
   - submitting;
   - success;
   - error.
4. Ban đầu dùng refetch sau mutation.
5. Chỉ thêm optimistic update khi API đã ổn và UI model rõ.
6. Nếu backend chưa trả trạng thái "current user already liked", cần quyết định:
   - thêm endpoint backend;
   - hoặc FE chỉ hiển thị count, chưa hiển thị selected state.

## Cấu trúc đề xuất

```text
src/features/interactions/use-blog-like.ts
src/features/interactions/use-cafe-follow.ts
src/features/interactions/use-user-follow.ts
```

Nếu chưa muốn tạo hooks nhiều, có thể xử lý mutation trong wrapper component của domain, nhưng không đưa API call vào component thuần render.

## Tiêu chí hoàn thành

- Click like gọi API thật và cập nhật UI.
- Comment mới được lưu backend và xuất hiện lại sau reload.
- Share gọi API thật.
- Follow/unfollow hoạt động với cafe hoặc user theo scope đã chọn.
- Lỗi 401 đưa user về login hoặc hiển thị trạng thái cần đăng nhập.
- `npm run typecheck` pass.

