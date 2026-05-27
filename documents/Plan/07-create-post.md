# Phase 07 - Create Post Or Review

## Mục tiêu

Kết nối form tạo review/blog mới với backend `POST /api/blogs`.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/app/(main)/reviews/new/page.tsx`
- `2-cafe-story-nextjs-web/src/components/review/create-post-form.tsx`
- `2-cafe-story-nextjs-web/src/components/review/create-post-modal.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/blogs.ts`
- `2-cafe-story-nextjs-web/src/lib/api/cafes.ts`
- `2-cafe-story-nextjs-web/src/types/blog.ts`
- `2-cafe-story-nextjs-web/src/features/blogs/`

## Backend liên quan

- `POST /api/blogs`
- `PATCH /api/blogs/{blogId}`
- `DELETE /api/blogs/{blogId}`
- `GET /api/cafe-pages` để chọn cafe/page khi tạo bài.

## Việc cần làm

1. Xác định `BlogCreateDTO` backend cần field nào.
2. Map form hiện tại sang request DTO.
3. Nếu form có chọn cafe:
   - load cafe list từ `GET /api/cafe-pages`;
   - truyền options vào form.
4. Nếu form có upload ảnh:
   - kiểm tra hiện FE đang dùng Cloudinary helper;
   - upload ảnh trước;
   - gửi URL/public id vào `POST /api/blogs`.
5. Tạo `createBlog(request)` trong `src/lib/api/blogs.ts`.
6. Sau khi tạo thành công:
   - redirect sang `/blogs/{blogId}` nếu backend trả id;
   - hoặc redirect về `/` và refresh feed.
7. Form cần validation client-side tối thiểu:
   - title/content không rỗng;
   - cafe/page hợp lệ nếu required;
   - ảnh hợp lệ nếu required.

## Rủi ro cần kiểm tra

- Backend có thể lấy author từ cookie, FE không nên gửi `authorUserId` nếu backend đã tự lấy principal.
- Form mock có thể nhiều field hơn `BlogCreateDTO`.
- Upload ảnh có thể là flow riêng, không nên trộn vào API blog nếu chưa rõ.

## Tiêu chí hoàn thành

- Tạo bài mới thành công từ `/reviews/new`.
- Bài mới hiện trong feed sau reload.
- Bài mới mở được bằng `/blogs/[id]`.
- Form hiển thị lỗi validation/API rõ ràng.
- `npm run typecheck` pass.

