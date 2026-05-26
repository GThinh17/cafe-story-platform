# Phase 03 - Blog Detail And Comments

## Mục tiêu

Gắn trang chi tiết blog với dữ liệu thật, bao gồm nội dung blog, ảnh, reviewer/author, like count và comments.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/app/(main)/blogs/[id]/page.tsx`
- `2-cafe-story-nextjs-web/src/components/review/blog-detail-review.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/blogs.ts`
- `2-cafe-story-nextjs-web/src/lib/api/comments.ts`
- `2-cafe-story-nextjs-web/src/types/blog.ts`
- `2-cafe-story-nextjs-web/src/types/comment.ts`
- `2-cafe-story-nextjs-web/src/types/review.ts`
- `2-cafe-story-nextjs-web/src/features/blogs/`
- `2-cafe-story-nextjs-web/src/features/comments/`

## Backend liên quan

- `GET /api/blogs/{blogId}`
- `GET /api/comments/blogs/{blogId}`
- `POST /api/comments`
- `PATCH /api/comments/{commentId}`
- `DELETE /api/comments/{commentId}`

## Việc cần làm

1. Mở rộng `src/lib/api/blogs.ts`:
   - `getBlogById(blogId)`.
2. Tạo `src/lib/api/comments.ts`:
   - `getCommentsByBlog(blogId)`;
   - `createComment(request)`;
   - `updateComment(commentId, request)`;
   - `deleteComment(commentId)`.
3. Tạo DTO types cho comment.
4. Tạo adapter cho blog detail:
   - backend blog response -> `BlogDetailReview` UI model hiện có.
5. Tách phần comment form nếu component hiện quá lớn:

```text
src/components/review/comment-composer.tsx
```

6. Giai đoạn đầu chỉ load comments.
7. Giai đoạn sau mới bật create/update/delete comment.

## Quy tắc UI

- Blog detail component không gọi API trực tiếp.
- Form comment có trạng thái submitting.
- Nếu user chưa đăng nhập hoặc session hết hạn, action comment phải xử lý lỗi 401 rõ ràng.
- Không optimistic update trước khi API ổn định; có thể thêm sau.

## Tiêu chí hoàn thành

- `/blogs/[id]` load blog thật bằng `blogId`.
- Comments hiển thị từ backend.
- Không còn phụ thuộc `mockBlogDetailReviews` trong route detail.
- Comment create hoạt động nếu nằm trong scope phase.
- `npm run typecheck` pass.

