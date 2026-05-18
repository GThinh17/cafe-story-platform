# Cafe Page API Test Guide

Tài liệu này dùng để test nhanh các API `CafePage`, `PageLike`, và `PageFollow` vừa tạo.

## Base URL

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

API docs JSON:

```text
http://localhost:8080/v3/api-docs
```

Hiện tại `SecurityConfig` đang `permitAll`, nên các API bên dưới chưa cần token.

## Test Data Cần Chuẩn Bị

Bạn cần ít nhất 1 user `ACTIVE` để làm owner và thực hiện like/follow.

Nếu chưa có user, tạo trước bằng API:

```http
POST /api/users
Content-Type: application/json
```

Body:

```json
{
  "userName": "cafe_owner_001",
  "userFullName": "Cafe Owner 001",
  "userPassword": "123456",
  "userEmail": "cafe.owner.001@example.com",
  "userPhone": 901234567,
  "userAvatar": "https://example.com/avatar-owner.png"
}
```

Response mẫu:

```json
{
  "userId": "replace-with-owner-user-id",
  "userName": "cafe_owner_001",
  "userFullName": "Cafe Owner 001",
  "userEmail": "cafe.owner.001@example.com",
  "userPhone": 901234567,
  "userAvatar": "https://example.com/avatar-owner.png",
  "userLike": 0,
  "userFollower": 0,
  "accountStatus": true
}
```

Lưu lại:

```text
ownerUserId = userId từ response
actorUserId = userId từ response hoặc user khác đang ACTIVE
```

## 1. Create Cafe Page

Tạo cafe page mới.

```http
POST /api/cafe-pages
Content-Type: application/json
```

Body tối thiểu:

```json
{
  "ownerUserId": "replace-with-owner-user-id",
  "name": "Cafe Story Nguyen Hue",
  "address": "123 Nguyen Hue, District 1, Ho Chi Minh City"
}
```

Body đầy đủ:

```json
{
  "ownerUserId": "replace-with-owner-user-id",
  "regionId": null,
  "name": "Cafe Story Nguyen Hue",
  "address": "123 Nguyen Hue, District 1, Ho Chi Minh City",
  "description": "A quiet cafe for reading, working, and sharing coffee stories.",
  "avatarUrl": "https://example.com/cafe-avatar.png",
  "coverUrl": "https://example.com/cafe-cover.png"
}
```

Expected status:

```text
201 Created
```

Response mẫu:

```json
{
  "id": "replace-with-cafe-page-id",
  "ownerUserId": "replace-with-owner-user-id",
  "regionId": null,
  "name": "Cafe Story Nguyen Hue",
  "address": "123 Nguyen Hue, District 1, Ho Chi Minh City",
  "description": "A quiet cafe for reading, working, and sharing coffee stories.",
  "avatarUrl": "https://example.com/cafe-avatar.png",
  "coverUrl": "https://example.com/cafe-cover.png",
  "status": "DRAFT",
  "likeCount": 0,
  "followerCount": 0,
  "createdAt": "2026-05-18T22:00:00",
  "updatedAt": null
}
```

Lưu lại:

```text
cafePageId = id từ response
```

## 2. Get All Cafe Pages

Lấy toàn bộ cafe page.

```http
GET /api/cafe-pages
```

Expected status:

```text
200 OK
```

Response mẫu:

```json
[
  {
    "id": "replace-with-cafe-page-id",
    "ownerUserId": "replace-with-owner-user-id",
    "regionId": null,
    "name": "Cafe Story Nguyen Hue",
    "address": "123 Nguyen Hue, District 1, Ho Chi Minh City",
    "description": "A quiet cafe for reading, working, and sharing coffee stories.",
    "avatarUrl": "https://example.com/cafe-avatar.png",
    "coverUrl": "https://example.com/cafe-cover.png",
    "status": "DRAFT",
    "likeCount": 0,
    "followerCount": 0,
    "createdAt": "2026-05-18T22:00:00",
    "updatedAt": null
  }
]
```

## 3. Get Cafe Pages By Owner

Lấy cafe page theo owner.

```http
GET /api/cafe-pages?ownerUserId=replace-with-owner-user-id
```

Expected status:

```text
200 OK
```

## 4. Get Cafe Page By Id

Lấy chi tiết 1 cafe page.

```http
GET /api/cafe-pages/replace-with-cafe-page-id
```

Expected status:

```text
200 OK
```

## 5. Update Cafe Page

Cập nhật cafe page. API này là `PATCH`, chỉ gửi field nào muốn đổi.

```http
PATCH /api/cafe-pages/replace-with-cafe-page-id
Content-Type: application/json
```

Body mẫu:

```json
{
  "name": "Cafe Story Nguyen Hue Updated",
  "address": "456 Le Loi, District 1, Ho Chi Minh City",
  "description": "Updated cafe description.",
  "avatarUrl": "https://example.com/cafe-avatar-updated.png",
  "coverUrl": "https://example.com/cafe-cover-updated.png",
  "status": "ACTIVE"
}
```

Các giá trị `status` hợp lệ:

```text
DRAFT
ACTIVE
SUSPENDED
```

Expected status:

```text
200 OK
```

## 6. Like Cafe Page

User like cafe page.

```http
POST /api/cafe-pages/replace-with-cafe-page-id/likes
Content-Type: application/json
```

Body:

```json
{
  "userId": "replace-with-actor-user-id"
}
```

Expected status:

```text
201 Created
```

Response mẫu:

```json
{
  "id": "replace-with-page-like-id",
  "userId": "replace-with-actor-user-id",
  "cafePageId": "replace-with-cafe-page-id",
  "createdAt": "2026-05-18T22:05:00"
}
```

Kiểm tra lại count:

```http
GET /api/cafe-pages/replace-with-cafe-page-id
```

Expected:

```json
{
  "likeCount": 1
}
```

## 7. Get Likes By Cafe Page

Lấy danh sách user đã like cafe page.

```http
GET /api/cafe-pages/replace-with-cafe-page-id/likes
```

Expected status:

```text
200 OK
```

Response mẫu:

```json
[
  {
    "id": "replace-with-page-like-id",
    "userId": "replace-with-actor-user-id",
    "cafePageId": "replace-with-cafe-page-id",
    "createdAt": "2026-05-18T22:05:00"
  }
]
```

## 8. Get Liked Pages By User

Lấy danh sách page mà user đã like.

```http
GET /api/cafe-pages/likes/users/replace-with-actor-user-id
```

Expected status:

```text
200 OK
```

## 9. Unlike Cafe Page

User bỏ like cafe page.

```http
DELETE /api/cafe-pages/replace-with-cafe-page-id/likes?userId=replace-with-actor-user-id
```

Expected status:

```text
204 No Content
```

Kiểm tra lại count:

```http
GET /api/cafe-pages/replace-with-cafe-page-id
```

Expected:

```json
{
  "likeCount": 0
}
```

## 10. Follow Cafe Page

User follow cafe page.

```http
POST /api/cafe-pages/replace-with-cafe-page-id/follows
Content-Type: application/json
```

Body:

```json
{
  "userId": "replace-with-actor-user-id"
}
```

Expected status:

```text
201 Created
```

Response mẫu:

```json
{
  "id": "replace-with-page-follow-id",
  "userId": "replace-with-actor-user-id",
  "cafePageId": "replace-with-cafe-page-id",
  "createdAt": "2026-05-18T22:10:00"
}
```

Kiểm tra lại count:

```http
GET /api/cafe-pages/replace-with-cafe-page-id
```

Expected:

```json
{
  "followerCount": 1
}
```

## 11. Get Followers By Cafe Page

Lấy danh sách user đang follow cafe page.

```http
GET /api/cafe-pages/replace-with-cafe-page-id/follows
```

Expected status:

```text
200 OK
```

Response mẫu:

```json
[
  {
    "id": "replace-with-page-follow-id",
    "userId": "replace-with-actor-user-id",
    "cafePageId": "replace-with-cafe-page-id",
    "createdAt": "2026-05-18T22:10:00"
  }
]
```

## 12. Get Followed Pages By User

Lấy danh sách page mà user đang follow.

```http
GET /api/cafe-pages/follows/users/replace-with-actor-user-id
```

Expected status:

```text
200 OK
```

## 13. Unfollow Cafe Page

User bỏ follow cafe page.

```http
DELETE /api/cafe-pages/replace-with-cafe-page-id/follows?userId=replace-with-actor-user-id
```

Expected status:

```text
204 No Content
```

Kiểm tra lại count:

```http
GET /api/cafe-pages/replace-with-cafe-page-id
```

Expected:

```json
{
  "followerCount": 0
}
```

## 14. Get Blogs By Cafe Page

Cafe page không có comment/share riêng. Page đăng bài thông qua `blogs.pageId`, sau đó comment/share vẫn dùng API của blog.

Lấy blog thuộc cafe page:

```http
GET /api/cafe-pages/replace-with-cafe-page-id/blogs
```

Expected status:

```text
200 OK
```

Nếu chưa có blog nào thuộc page:

```json
[]
```

## 15. Delete Cafe Page

Xóa cafe page.

```http
DELETE /api/cafe-pages/replace-with-cafe-page-id
```

Expected status:

```text
204 No Content
```

## Negative Test Cases

### Create Cafe Page Thiếu Owner

```http
POST /api/cafe-pages
Content-Type: application/json
```

Body:

```json
{
  "name": "Cafe Missing Owner",
  "address": "123 Nguyen Hue"
}
```

Expected:

```text
400 Bad Request
```

### Create Cafe Page Thiếu Name

```json
{
  "ownerUserId": "replace-with-owner-user-id",
  "address": "123 Nguyen Hue"
}
```

Expected:

```text
400 Bad Request
```

### Create Cafe Page Thiếu Address

```json
{
  "ownerUserId": "replace-with-owner-user-id",
  "name": "Cafe Missing Address"
}
```

Expected:

```text
400 Bad Request
```

### Get Cafe Page Không Tồn Tại

```http
GET /api/cafe-pages/00000000-0000-0000-0000-000000000000
```

Expected:

```text
404 Not Found
```

Message:

```text
Cafe page not found
```

### Like Cafe Page 2 Lần

Gọi 2 lần cùng request:

```http
POST /api/cafe-pages/replace-with-cafe-page-id/likes
Content-Type: application/json
```

Body:

```json
{
  "userId": "replace-with-actor-user-id"
}
```

Expected lần 1:

```text
201 Created
```

Expected lần 2:

```text
409 Conflict
```

Message:

```text
Cafe page already liked by user
```

### Unlike Khi Chưa Like

```http
DELETE /api/cafe-pages/replace-with-cafe-page-id/likes?userId=replace-with-actor-user-id
```

Expected:

```text
404 Not Found
```

Message:

```text
Cafe page like not found
```

### Follow Cafe Page 2 Lần

Gọi 2 lần cùng request:

```http
POST /api/cafe-pages/replace-with-cafe-page-id/follows
Content-Type: application/json
```

Body:

```json
{
  "userId": "replace-with-actor-user-id"
}
```

Expected lần 1:

```text
201 Created
```

Expected lần 2:

```text
409 Conflict
```

Message:

```text
Cafe page already followed by user
```

### Unfollow Khi Chưa Follow

```http
DELETE /api/cafe-pages/replace-with-cafe-page-id/follows?userId=replace-with-actor-user-id
```

Expected:

```text
404 Not Found
```

Message:

```text
Cafe page follow not found
```

### User Không ACTIVE Thực Hiện Like/Follow

Nếu `user.accountStatus = false`, các action sau sẽ bị chặn:

```text
POST /api/cafe-pages/{cafePageId}/likes
DELETE /api/cafe-pages/{cafePageId}/likes
POST /api/cafe-pages/{cafePageId}/follows
DELETE /api/cafe-pages/{cafePageId}/follows
```

Expected:

```text
403 Forbidden
```

Message:

```text
User account is inactive
```

## Recommended Manual Test Flow

Chạy theo thứ tự này để ít bị lệch dữ liệu:

```text
1. POST /api/users
2. POST /api/cafe-pages
3. GET /api/cafe-pages/{cafePageId}
4. PATCH /api/cafe-pages/{cafePageId}
5. POST /api/cafe-pages/{cafePageId}/likes
6. GET /api/cafe-pages/{cafePageId} để check likeCount = 1
7. GET /api/cafe-pages/{cafePageId}/likes
8. GET /api/cafe-pages/likes/users/{userId}
9. DELETE /api/cafe-pages/{cafePageId}/likes?userId={userId}
10. GET /api/cafe-pages/{cafePageId} để check likeCount = 0
11. POST /api/cafe-pages/{cafePageId}/follows
12. GET /api/cafe-pages/{cafePageId} để check followerCount = 1
13. GET /api/cafe-pages/{cafePageId}/follows
14. GET /api/cafe-pages/follows/users/{userId}
15. DELETE /api/cafe-pages/{cafePageId}/follows?userId={userId}
16. GET /api/cafe-pages/{cafePageId} để check followerCount = 0
```

## Postman Environment Variables

Bạn có thể tạo các variable sau trong Postman:

```text
baseUrl=http://localhost:8080
ownerUserId=replace-after-create-user
actorUserId=replace-after-create-user
cafePageId=replace-after-create-cafe-page
```

Ví dụ URL trong Postman:

```text
{{baseUrl}}/api/cafe-pages/{{cafePageId}}/likes
```
