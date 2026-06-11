# CafeStory Backend API Documentation

Generated from the current Spring Boot controllers, security config, DTOs, and service tests.

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Current User Identity Rule](#current-user-identity-rule)
- [Common Response Format](#common-response-format)
- [Admin Blog Ranking](#admin-blog-ranking)
- [Blogs](#blogs)
- [Blog Feed](#blog-feed)
- [Blog Likes](#blog-likes)
- [Blog Shares](#blog-shares)
- [Blog Trending](#blog-trending)
- [Cafe Pages](#cafe-pages)
- [Chat](#chat)
- [Comments](#comments)
- [Content Reports](#content-reports)
- [Notifications](#notifications)
- [Page Follows](#page-follows)
- [Page Likes](#page-likes)
- [Page Members](#page-members)
- [Payments](#payments)
- [Reviewers](#reviewers)
- [Users](#users)
- [User Follows](#user-follows)
- [Realtime WebSocket API](#realtime-websocket-api)
- [Postman Notes](#postman-notes)

## Overview

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Database: PostgreSQL / Supabase PostgreSQL. ID fields are UUID strings.
- Auth model: JWT in HttpOnly cookies, not `Authorization: Bearer <token>`.
- Public REST endpoints: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/payments/stripe/webhook`, `GET /api/payments/vnpay/return`, `GET /api/payments/vnpay/ipn`, Swagger/OpenAPI, and `OPTIONS /**`.
- Authenticated REST endpoints: all other REST APIs require the `access_token` cookie.
- Admin REST endpoints: `/api/admin/**`, `/api/reviewers/payouts/**`, `/api/reviewers/badges/**`, `/api/reviewers/*/payouts`, `/api/reviewers/*/badges` require role `ADMIN`. Payment list and manual bank-transfer approval are also ADMIN-only at service level.

Headers for JSON requests:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `Content-Type` | yes for body requests | `application/json` | JSON body. |
| `Cookie` | yes for protected APIs | `access_token=<jwt>` | Sent automatically by browser/Postman Cookie Jar after login. Do not use Bearer. |

## Authentication

Login and refresh set tokens with `Set-Cookie`. Token values are not returned in JSON because `AuthResponse.accessToken` and `AuthResponse.refreshToken` are ignored in response serialization.

| Method | Path | Auth | Description | Status |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Create a new user and assign default `USER` role. | 201 |
| `POST` | `/api/auth/login` | Public | Login by username/email and password, then set cookies. | 200 |
| `GET` | `/api/auth/me` | `access_token` cookie | Get current authenticated user. | 200 |
| `POST` | `/api/auth/refresh` | `refresh_token` cookie | Issue a new `access_token` cookie. | 200 |
| `POST` | `/api/auth/logout` | `refresh_token` cookie optional | Revoke refresh token and clear cookies. | 200 |

Cookie details:

| Cookie | Path | HttpOnly | Note |
| --- | --- | --- | --- |
| `access_token` | `/` | yes | Used by `JwtAuthenticationFilter` for normal API auth. |
| `refresh_token` | `/api/auth` | yes | Used only by refresh/logout flow. |

Register body:

```json
{
  "userName": "auth_user_001",
  "userFullName": "Auth User 001",
  "password": "123456",
  "userEmail": "auth.user.001@example.com",
  "userPhone": 901234567,
  "userAvatar": "https://example.com/avatar-auth-user.png"
}
```

Login body:

```json
{
  "identifier": "auth.user.001@example.com",
  "password": "123456"
}
```

Postman test note:

1. Send `POST /api/auth/login`.
2. Keep Postman Cookie Jar enabled.
3. Send protected APIs normally. Postman will attach `access_token` automatically.
4. Do not add `Authorization: Bearer {{token}}`.

## Current User Identity Rule

The backend now takes the acting user from JWT principal in the `access_token` cookie. Client request bodies must not be trusted for actor identity.

Fields removed from client responsibility:

| Field | Used by old requests | Current source |
| --- | --- | --- |
| `authorUserId` | Create blog | JWT principal |
| `ownerUserId` | Create cafe page | JWT principal |
| `userId` | Like/follow/share/comment/event/notification current-user actions | JWT principal, except when clearly used as a target/filter path param |
| `actorUserId` | Page member and chat admin actions | JWT principal |
| `senderId` | Send chat message | JWT principal |
| `firstUserId` | Create direct conversation | JWT principal |
| `creatorUserId` | Create group conversation | JWT principal |
| `followerUserId` | Follow/unfollow user | JWT principal |
| `requesterId` | Reviewer stats/payout/badge requester | JWT principal |
| `createdBy` / `created_by` | Admin blog ranking override | JWT principal |
| `buyerId` | Create payment | JWT principal |

If `userId` remains in a path such as `/api/users/{userId}` or `/api/comments/users/{userId}`, it is a target/filter ID, not the authenticated actor.

## Common Response Format

REST responses are wrapped by `GlobalResponseAdvice`:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {}
}
```

Common error:

```json
{
  "statusCode": 401,
  "status": "Fail",
  "message": "Authentication is required",
  "data": null
}
```

Common status codes: `200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`, `500`.

## Admin Blog Ranking

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/admin/blogs/{blogId}/ranking-override` | ADMIN cookie | Create a manual ranking override. |

Path params: `blogId={{blog_id}}`

Body:

```json
{
  "boost_score": 50.0,
  "is_pinned": true,
  "reason": "Manual boost for campaign",
  "start_at": "2026-05-20T09:00:00",
  "end_at": "2026-05-23T09:00:00"
}
```

Note: `createdBy` is taken from the admin JWT principal.

## Blogs

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/blogs` | Cookie | Create blog for current user. |
| `GET` | `/api/blogs?authorUserId={{user_id}}` | Cookie | Get all blogs or filter by author. |
| `GET` | `/api/blogs/users/{userId}` | Cookie | Get blogs by target user. |
| `GET` | `/api/blogs/{blogId}` | Cookie | Get blog detail. |
| `PATCH` | `/api/blogs/{blogId}` | Cookie | Update blog. |
| `DELETE` | `/api/blogs/{blogId}` | Cookie | Delete blog. |

Create body:

```json
{
  "pageId": "{{cafe_page_id}}",
  "regionId": "{{region_id}}",
  "content": "Review ca phe sua da va khong gian lam viec tai Cafe Story Nguyen Hue.",
  "imageUrls": ["https://example.com/blog-1.png"],
  "isPinned": false,
  "allowComment": true
}
```

Update body:

```json
{
  "content": "Updated blog content",
  "status": "PUBLISHED",
  "imageUrls": ["https://example.com/blog-updated.png"]
}
```

Note: `authorUserId` is taken from the cookie token when creating a blog.

## Blog Feed

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/api/blogs/feed/personalized?windowType=DAY_7&regionId={{region_id}}&page=0&size=20` | Cookie | Get current user's personalized feed. |
| `POST` | `/api/blogs/feed/personalized/rebuild?windowType=DAY_7&regionId={{region_id}}` | Cookie | Rebuild current user's recommendation cache. |

Note: current `userId` is taken from JWT principal.

## Blog Likes

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/blogs/{blogId}/likes` | Cookie | Like a blog as current user. |
| `DELETE` | `/api/blogs/{blogId}/likes` | Cookie | Unlike a blog as current user. |
| `GET` | `/api/blogs/{blogId}/likes` | Cookie | Get likes by blog. |
| `GET` | `/api/blogs/likes/users/{userId}` | Cookie | Get likes by target user. |

Body: none for like/unlike.

## Blog Shares

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/blogs/{blogId}/shares` | Cookie | Share a blog as current user. |
| `GET` | `/api/blogs/{blogId}/shares` | Cookie | Get shares by blog. |
| `GET` | `/api/blogs/shares/users/{userId}` | Cookie | Get shares by target user. |

Share body:

```json
{
  "shareType": "PUBLIC"
}
```

Note: sharing `userId` is taken from JWT principal.

## Blog Trending

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/api/blogs/trending?windowType=HOUR_24&page=0&size=20` | Cookie | Get trending blogs. |
| `POST` | `/api/blogs/{blogId}/events` | Cookie | Record current user's blog event. |

Event body:

```json
{
  "eventType": "VIEW",
  "weight": 1.0
}
```

Note: event `userId` is taken from JWT principal.

## Cafe Pages

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/cafe-pages` | Cookie | Create cafe page for current user. |
| `GET` | `/api/cafe-pages?ownerUserId={{user_id}}` | Cookie | Get all cafe pages or filter by owner. |
| `GET` | `/api/cafe-pages/{cafePageId}` | Cookie | Get cafe page detail. |
| `GET` | `/api/cafe-pages/{cafePageId}/blogs` | Cookie | Get blogs by cafe page. |
| `PATCH` | `/api/cafe-pages/{cafePageId}` | Cookie | Update cafe page. |
| `DELETE` | `/api/cafe-pages/{cafePageId}` | Cookie | Delete cafe page. |

Create body:

```json
{
  "name": "Cafe Story Nguyen Hue",
  "address": "123 Nguyen Hue, District 1",
  "description": "Cafe lam viec yen tinh",
  "avatar": "https://example.com/cafe-avatar.png",
  "coverImage": "https://example.com/cafe-cover.png"
}
```

Note: `ownerUserId` is taken from JWT principal.

## Chat

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/chat/conversations/direct` | Cookie | Create/get direct conversation. |
| `POST` | `/api/chat/conversations/group` | Cookie | Create group conversation. |
| `GET` | `/api/chat/conversations` | Cookie | Get current user's conversations. |
| `GET` | `/api/chat/conversations/{conversationId}/messages?page=0&size=20` | Cookie | Get messages as current user. |
| `POST` | `/api/chat/conversations/{conversationId}/messages` | Cookie | Send message as current user. |
| `POST` | `/api/chat/conversations/{conversationId}/members` | Cookie | Add member as current actor. |
| `DELETE` | `/api/chat/conversations/{conversationId}/members/{memberUserId}` | Cookie | Remove member as current actor. |
| `POST` | `/api/chat/conversations/{conversationId}/leave` | Cookie | Leave group as current user. |
| `PATCH` | `/api/chat/conversations/{conversationId}/group` | Cookie | Update group info as current actor. |

Direct conversation body:

```json
{
  "secondUserId": "{{member_user_id}}"
}
```

Group conversation body:

```json
{
  "groupName": "Cafe planning team",
  "memberIds": ["{{member_user_id}}"],
  "imageUrl": "https://example.com/group.png"
}
```

Send message body:

```json
{
  "type": "TEXT",
  "text": "Hen gap o quan luc 9h nhe.",
  "imageUrls": ["https://example.com/chat-image.png"],
  "stickerUrl": null,
  "stickerId": null
}
```

Add member body:

```json
{
  "memberUserId": "{{member_user_id}}"
}
```

Update group body:

```json
{
  "groupName": "Cafe planning updated",
  "imageUrl": "https://example.com/group-updated.png"
}
```

Notes: `firstUserId`, `creatorUserId`, `senderId`, and `actorUserId` are taken from JWT principal.

## Comments

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/comments` | Cookie | Create comment as current user. |
| `GET` | `/api/comments?blogId={{blog_id}}&userId={{user_id}}` | Cookie | Get all comments or filter by blog/user. |
| `GET` | `/api/comments/blogs/{blogId}` | Cookie | Get comments by blog. |
| `GET` | `/api/comments/users/{userId}` | Cookie | Get comments by target user. |
| `GET` | `/api/comments/{commentId}/replies` | Cookie | Get replies. |
| `GET` | `/api/comments/{commentId}` | Cookie | Get comment detail. |
| `PATCH` | `/api/comments/{commentId}` | Cookie | Update comment. |
| `DELETE` | `/api/comments/{commentId}` | Cookie | Delete comment. |

Create body:

```json
{
  "blogId": "{{blog_id}}",
  "parentCommentId": null,
  "content": "Quan dep, do uong on.",
  "imageUrls": ["https://example.com/comment.png"]
}
```

Update body:

```json
{
  "content": "Updated comment",
  "imageUrls": ["https://example.com/comment-updated.png"],
  "status": "PUBLISHED"
}
```

Note: create comment `userId` is taken from JWT principal.

## Content Reports

Report APIs use the current user from the `access_token` cookie. The client should load active report reasons, let the user select one, then submit the selected `reasonId`.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/api/report-reasons?targetType=BLOG` | Cookie | Get active report reasons sorted by `severity desc`, then `sortOrder asc`. |
| `POST` | `/api/reports` | Cookie | Create a content report as the current user. |
| `GET` | `/api/admin/reports?status=OPEN&targetType=BLOG&page=0&size=20` | ADMIN cookie | List reports for moderation. |
| `GET` | `/api/admin/reports/{reportId}` | ADMIN cookie | Get report detail. |
| `PATCH` | `/api/admin/reports/{reportId}/status` | ADMIN cookie | Update report status. |

Create report body:

```json
{
  "targetType": "BLOG",
  "targetId": "{{blog_id}}",
  "reasonId": "{{report_reason_id}}",
  "description": "Optional details. Required when the selected reason requires description."
}
```

Reason response example:

```json
{
  "id": "{{report_reason_id}}",
  "code": "SCAM_FRAUD_OR_SPAM",
  "labelVi": "Lừa đảo, gian lận hoặc spam",
  "targetType": null,
  "severity": 4,
  "requiresDescription": false,
  "isActive": true,
  "sortOrder": 70
}
```

Report response includes selected reason metadata and the stored label snapshot:

```json
{
  "id": "{{report_id}}",
  "targetType": "BLOG",
  "targetId": "{{blog_id}}",
  "reasonId": "{{report_reason_id}}",
  "reasonCode": "SCAM_FRAUD_OR_SPAM",
  "reason": "Lừa đảo, gian lận hoặc spam",
  "reasonLabel": "Lừa đảo, gian lận hoặc spam",
  "reasonSeverity": 4,
  "status": "OPEN"
}
```

Notes: duplicate active reports are blocked while an existing report for the same reporter and target is `OPEN` or `REVIEWING`. Users cannot report their own blog, comment, account, or cafe page. Blog reports create a `BlogEvent.REPORT` penalty event for ranking/trending.

## Notifications

Base path is `/notifications`.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/notifications?page=0&limit=20&isRead=false&type=COMMENT` | Cookie | Get current user's notifications. |
| `GET` | `/notifications/unread-count` | Cookie | Get current user's unread count. |
| `PATCH` | `/notifications/{id}/read` | Cookie | Mark current user's notification as read. |
| `PATCH` | `/notifications/read-all` | Cookie | Mark all current user's notifications as read. |
| `DELETE` | `/notifications/{id}` | Cookie | Delete current user's notification. |

Note: notification `userId` is taken from JWT principal.

## Page Follows

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/cafe-pages/{cafePageId}/follows` | Cookie | Follow page as current user. |
| `DELETE` | `/api/cafe-pages/{cafePageId}/follows` | Cookie | Unfollow page as current user. |
| `GET` | `/api/cafe-pages/{cafePageId}/follows` | Cookie | Get followers by page. |
| `GET` | `/api/cafe-pages/follows/users/{userId}` | Cookie | Get followed pages by target user. |

Body: none for follow/unfollow.

## Page Likes

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/cafe-pages/{cafePageId}/likes` | Cookie | Like page as current user. |
| `DELETE` | `/api/cafe-pages/{cafePageId}/likes` | Cookie | Unlike page as current user. |
| `GET` | `/api/cafe-pages/{cafePageId}/likes` | Cookie | Get likes by page. |
| `GET` | `/api/cafe-pages/likes/users/{userId}` | Cookie | Get likes by target user. |

Body: none for like/unlike.

## Page Members

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/cafe-pages/{cafePageId}/members/requests` | Cookie | Request to join page as current user. |
| `POST` | `/api/cafe-pages/{cafePageId}/members` | Cookie | Add a target user as member. |
| `PATCH` | `/api/cafe-pages/{cafePageId}/members/{userId}/status` | Cookie | Update target member status. |
| `GET` | `/api/cafe-pages/{cafePageId}/members` | Cookie | Get page members. |
| `GET` | `/api/cafe-pages/{cafePageId}/members/pending` | Cookie | Get pending members. |

Add member body:

```json
{
  "userId": "{{member_user_id}}",
  "roleName": "MEMBER"
}
```

Update status body:

```json
{
  "status": "ACTIVE"
}
```

Notes: request-to-join has no body. `actorUserId` is taken from JWT principal. Path/body `userId` is the target member.

## Payments

Payment APIs use the authenticated user from the `access_token` cookie. Do not send `buyerId` in create-payment request bodies.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/payments` | Cookie | Create payment for current user. |
| `GET` | `/api/payments/{paymentId}` | Cookie | Get payment detail. Buyer or ADMIN only. |
| `GET` | `/api/payments?paymentStatus=PENDING` | ADMIN cookie | Get all payments, optionally filtered by status. |
| `POST` | `/api/payments/{paymentId}/bank-transfer/mark-paid` | ADMIN cookie | Manually mark a bank-transfer payment as paid. |
| `POST` | `/api/payments/stripe/webhook` | Public provider callback | Stripe webhook endpoint. |
| `GET` | `/api/payments/vnpay/return` | Public provider callback | VNPAY browser return endpoint. |
| `GET` | `/api/payments/vnpay/ipn` | Public provider callback | VNPAY server IPN endpoint. |

Create payment body:

```json
{
  "extraFeeId": "{{extra_fee_id}}",
  "paymentMethod": "VNPAY"
}
```

Supported `paymentMethod` values:

```text
STRIPE_CARD
BANK_TRANSFER
VNPAY
```

Create payment response example:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "{{payment_id}}",
    "buyerId": "{{current_user_id}}",
    "extraFeeId": "{{extra_fee_id}}",
    "paymentMethod": "VNPAY",
    "amount": 299000,
    "currency": "VND",
    "paymentStatus": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "qrCodeUrl": null,
    "transferContent": "CAFE_VNPAY_{{payment_id}}",
    "createdAt": "2026-05-25T10:00:00",
    "paidAt": null,
    "expiredAt": "2026-05-25T10:30:00"
  }
}
```

Bank-transfer create body:

```json
{
  "extraFeeId": "{{extra_fee_id}}",
  "paymentMethod": "BANK_TRANSFER"
}
```

Bank-transfer response includes `transferContent` like:

```text
CAFE_PAYMENT_{{payment_id}}
```

Manual mark-paid request:

```text
POST /api/payments/{{payment_id}}/bank-transfer/mark-paid
```

Body: none.

Required role: `ADMIN`.

Common payment test flow:

1. Login with `POST /api/auth/login`.
2. Confirm Postman Cookie Jar has `access_token`.
3. Create payment with `POST /api/payments`; do not include `buyerId`.
4. Save `data.paymentId`.
5. For `BANK_TRANSFER`, login as ADMIN and call `POST /api/payments/{{payment_id}}/bank-transfer/mark-paid`.
6. For `VNPAY`, open `data.paymentUrl` in browser and let VNPAY call return/IPN.
7. For `STRIPE_CARD`, open `data.paymentUrl`; Stripe confirms through `/api/payments/stripe/webhook`.

Error notes:

| HTTP Status | Condition | Message |
| --- | --- | --- |
| `400` | Missing `extraFeeId` or `paymentMethod` | DTO validation message. |
| `400` | Extra fee inactive | `Extra fee is inactive` |
| `400` | Manual mark-paid on non-bank-transfer payment | `Payment is not a bank transfer` |
| `403` | Non-admin calls payment list or manual mark-paid | `Admin role is required` |
| `403` | Non-buyer, non-admin reads a payment | `Admin role is required` |
| `404` | Payment not found | `Payment not found` |
| `404` | Extra fee not found | `Extra fee not found` |
| `404` | Authenticated buyer not found | `Buyer not found` |

## Reviewers

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/reviewers/create` | Cookie | Create reviewer for current user. |
| `GET` | `/api/reviewers/{userId}` | Cookie | Get reviewer by target user. |
| `GET` | `/api/reviewers` | Cookie | Get all reviewers. |
| `GET` | `/api/reviewers/{reviewerId}/stats?period=2026-05` | Cookie | Get reviewer stats as current requester. |
| `GET` | `/api/reviewers/ranking?period=2026-05&page=1&limit=20&city=Ho%20Chi%20Minh` | Cookie | Get reviewer ranking. |
| `GET` | `/api/reviewers/segments?month=2026-05&segment=TOP` | Cookie | Get reviewers by segment. |
| `GET` | `/api/reviewers/geo?period=2026-05&groupBy=city` | Cookie | Get reviewer geo analytics. |
| `GET` | `/api/reviewers/{reviewerId}/payouts` | ADMIN cookie | Get payout history. |
| `POST` | `/api/reviewers/payouts/generate?month=2026-05&overwrite=false` | ADMIN cookie | Generate monthly payouts. |
| `POST` | `/api/reviewers/badges/generate?month=2026-05&overwrite=false` | ADMIN cookie | Generate monthly badges. |
| `GET` | `/api/reviewers/{reviewerId}/badges` | ADMIN cookie | Get badge history. |

Note: `requesterId` is taken from JWT principal. Create reviewer uses current user from JWT principal.

## Users

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/users` | Cookie | Create user through legacy user API. Prefer `/api/auth/register` for auth flow. |
| `GET` | `/api/users` | Cookie | Get all users. |
| `GET` | `/api/users/{userId}` | Cookie | Get target user by ID. |
| `PATCH` | `/api/users/me` | Cookie | Update current user. |
| `PATCH` | `/api/users/me/region` | Cookie | Update current user's region. |
| `DELETE` | `/api/users/me` | Cookie | Delete current user. |

Create body:

```json
{
  "userName": "luan123",
  "userFullName": "Nguyen Van Luan",
  "userPassword": "123456",
  "userEmail": "luan123@example.com",
  "userPhone": 987654321,
  "userAvatar": "https://example.com/avatar.png",
  "region": {
    "city": "Ho Chi Minh",
    "province": "Ho Chi Minh",
    "district": "District 1",
    "ward": "Ben Nghe",
    "street": "Nguyen Hue"
  }
}
```

Update current user body:

```json
{
  "userFullName": "Nguyen Van Luan Updated",
  "userPhone": 987654322,
  "userAvatar": "https://example.com/avatar-updated.png"
}
```

Update current region body:

```json
{
  "city": "Ho Chi Minh",
  "province": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ben Nghe",
  "street": "Nguyen Hue"
}
```

Note: update/delete user ID is taken from JWT principal.

## User Follows

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/users/{followingUserId}/followers` | Cookie | Current user follows target user. |
| `DELETE` | `/api/users/{followingUserId}/followers` | Cookie | Current user unfollows target user. |
| `GET` | `/api/users/{userId}/followers` | Cookie | Get target user's followers. |
| `GET` | `/api/users/{userId}/following` | Cookie | Get target user's following list. |

Body: none for follow/unfollow. `followingUserId` is the target; `followerUserId` is taken from JWT principal.

## Realtime WebSocket API

Base endpoint is configured by WebSocket/STOMP config. Message mappings inferred from `ChatSocketController`.

| Send to | Description | Body |
| --- | --- | --- |
| `/app/chat/join_conversation` | Join conversation topic. | `{ "conversationId": "{{conversation_id}}", "userId": "{{user_id}}" }` |
| `/app/chat/leave_conversation` | Leave conversation topic. | `{ "conversationId": "{{conversation_id}}", "userId": "{{user_id}}" }` |
| `/app/chat/{conversationId}/send_message` | Send realtime message. | `SendMessageRequest`; current WebSocket controller still accepts `senderId` in body. |
| `/app/chat/typing_start` | Broadcast typing start. | `{ "conversationId": "{{conversation_id}}", "userId": "{{user_id}}" }` |
| `/app/chat/typing_stop` | Broadcast typing stop. | `{ "conversationId": "{{conversation_id}}", "userId": "{{user_id}}" }` |

Note: the cookie principal refactor was applied to REST controllers. WebSocket identity should be reviewed separately if you want STOMP messages to derive user identity from the authenticated session.

## Postman Notes

- Import/use an environment with `base_url=http://localhost:8080`.
- Run `POST /api/auth/login` first.
- Keep Postman's Cookie Jar enabled so `access_token` and `refresh_token` are sent automatically.
- Do not configure collection-level Bearer token auth for this backend flow.
- For role `ADMIN` APIs, login as a user whose JWT contains role `ADMIN`.
- For frontend tests, use `credentials: "include"`.
