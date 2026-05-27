# CafeStory FE-BE Integration Plan

Folder này chứa kế hoạch triển khai từng phần để gắn API backend vào Next.js frontend theo cấu trúc hiện có của dự án.

## Nguyên tắc chung

- Không gọi `fetch` trực tiếp trong component.
- API client nằm trong `2-cafe-story-nextjs-web/src/lib/api/`.
- Type dùng chung nằm trong `2-cafe-story-nextjs-web/src/types/`.
- Mapping từ backend DTO sang UI model nên nằm trong `2-cafe-story-nextjs-web/src/features/<domain>/`.
- Component trong `2-cafe-story-nextjs-web/src/components/` chỉ render props và xử lý interaction UI cục bộ.
- Route trong `2-cafe-story-nextjs-web/src/app/` chỉ compose layout, gọi data cần thiết, truyền props xuống component.
- Backend auth đang dùng HttpOnly cookie, frontend phải gọi API với `credentials: "include"`.

## Thứ tự triển khai đề xuất

1. `01-api-foundation-auth.md` - Chuẩn hóa API client, endpoint map, auth/current user.
2. `02-home-feed.md` - Gắn home feed với blog feed API.
3. `03-blog-detail-comments.md` - Gắn blog detail và comment.
4. `04-cafe-explore-detail.md` - Gắn explore cafe, cafe detail và blog của cafe.
5. `05-profile.md` - Gắn profile với current user, blog của user, stats.
6. `06-actions-interactions.md` - Like, unlike, share, follow, comment actions.
7. `07-create-post.md` - Tạo blog/review mới từ form FE.
8. `08-notifications-messages.md` - Notifications và chat/messages.

## Cách dùng

Mỗi phase nên được làm thành một pull request hoặc một nhánh nhỏ. Sau mỗi phase chạy:

```powershell
npm run typecheck
npm run build
```

Nếu phase có đụng backend thì chạy thêm test Maven liên quan:

```powershell
mvn test
```

