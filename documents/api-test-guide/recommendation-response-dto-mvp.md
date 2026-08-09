# CafeStory Recommendation Response DTO MVP

## Goal

Thiết kế response DTO tối giản cho UI gợi ý kiểu Instagram:

- Gợi ý user gần khu vực
- Gợi ý reviewer
- Gợi ý cafe page
- Gợi ý mixed gồm cả 3 loại trong cùng một API

UI hiện tại chỉ cần hiển thị:

- `avatar`
- `username`
- `fullName`
- `reason`
- `city`

Tuy nhiên backend nên trả thêm `targetId` và `targetType` để frontend có thể bấm follow, mở profile/page, hoặc tracking impression. Hai field này không cần render ra UI nhưng rất cần cho thao tác.

## Recommended Response DTO

```java
public class RecommendationCardResponseDTO {
    private UUID targetId;
    private RecommendationTargetType targetType;
    private String avatar;
    private String username;
    private String fullName;
    private String reason;
    private String city;
}
```

## Target Type Enum

```java
public enum RecommendationTargetType {
    USER,
    REVIEWER,
    CAFE_PAGE
}
```

## Why Keep `targetId` and `targetType`

Frontend có thể chỉ render `avatar`, `username`, `fullName`, `reason`, `city`, nhưng vẫn cần:

| Field | Why needed |
| --- | --- |
| `targetId` | Dùng để follow user/page, mở profile/page detail, hoặc tracking click. |
| `targetType` | Biết item là `USER`, `REVIEWER`, hay `CAFE_PAGE` để điều hướng đúng màn hình. |

Nếu bỏ 2 field này, frontend sẽ hiển thị được card nhưng không thể xử lý button `Theo dõi` một cách chắc chắn.

## API Endpoints

```http
GET /api/recommendations/users?page=0&size=20
GET /api/recommendations/reviewers?page=0&size=20
GET /api/recommendations/cafe-pages?page=0&size=20
GET /api/recommendations/mixed?page=0&size=30
```

Current user phải lấy từ JWT principal trong cookie, không nhận `userId` từ request body.

Implemented backend files:

- `RecommendationController`
- `RecommendationService`
- `RecommendationServiceImpl`
- `RecommendationCardResponseDTO`
- `RecommendationTargetType`

## Response Shape

Nên dùng page response nếu backend đang theo pattern phân trang:

```json
{
  "content": [
    {
      "targetId": "b8fb0a33-2f6b-4b9e-9cf4-8b93e769ccf1",
      "targetType": "USER",
      "avatar": "https://example.com/avatar.png",
      "username": "jordialbaoficial",
      "fullName": "Jordi Alba",
      "reason": "Cùng khu vực với bạn",
      "city": "Ho Chi Minh"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Nếu muốn đơn giản cho MVP, API cũng có thể trả `List<RecommendationCardResponseDTO>` trước. Nhưng về lâu dài nên dùng pagination.

## Field Mapping

### USER

```text
targetId = users.user_id
targetType = USER
avatar = users.user_avatar
username = users.user_name
fullName = users.user_full_name
city = users.region.city
reason = reason built from score components
```

Reason ví dụ:

```text
Cùng khu vực với bạn
Hoạt động gần đây trong khu vực của bạn
Có nhiều người bạn theo dõi cũng quan tâm
```

### REVIEWER

```text
targetId = reviewers.reviewer_id or users.user_id
targetType = REVIEWER
avatar = reviewer.user.user_avatar
username = reviewer.user.user_name
fullName = reviewer display name or user full name
city = reviewer.user.region.city
reason = reason built from reviewer score
```

Gợi ý: nếu frontend cần mở reviewer detail thì dùng `reviewer_id`. Nếu mở user profile thì dùng `user_id`. Nên thống nhất ngay từ đầu. Với CafeStory, mình khuyên:

```text
targetId = reviewer_id
```

Reason ví dụ:

```text
Reviewer nổi bật gần bạn
Có nhiều bài đánh giá gần đây
Được cộng đồng tương tác cao
```

### CAFE_PAGE

```text
targetId = cafe_pages.id
targetType = CAFE_PAGE
avatar = cafe_pages.avatar_url
username = cafe_pages.name
fullName = cafe_pages.name
city = cafe_pages.region.city
reason = reason built from page score
```

Với cafe page, vì không có `username` thật, có thể map:

```text
username = page slug if later available
username = page name if no slug yet
fullName = page name
```

Reason ví dụ:

```text
Gần khu vực của bạn
Được nhiều người theo dõi
Có bài viết mới gần đây
Được đánh giá cao
```

## Minimal Frontend Card Contract

Frontend chỉ cần render các field này:

```ts
type RecommendationCard = {
  avatar: string | null;
  username: string;
  fullName: string;
  reason: string;
  city: string | null;
};
```

Nhưng API contract nên giữ thêm:

```ts
type RecommendationCardApiResponse = RecommendationCard & {
  targetId: string;
  targetType: "USER" | "REVIEWER" | "CAFE_PAGE";
};
```

## Follow Button Logic

Khi user bấm `Theo dõi`:

```text
if targetType = USER:
  POST /api/users/{targetId}/follow or existing user follow endpoint

if targetType = REVIEWER:
  follow reviewer user, or open reviewer profile depending on product decision

if targetType = CAFE_PAGE:
  POST /api/cafe-pages/{targetId}/follows
```

Khuyến nghị MVP:

```text
Reviewer card click -> mở reviewer profile
Reviewer follow button -> follow reviewer user nếu reviewer có userId trong backend mapping
```

Nếu cần follow reviewer trực tiếp, response nên thêm `userId` cho reviewer:

```java
private UUID userId;
```

Nhưng nếu frontend chưa cần, có thể để sau.

## Mixed API Rule

`GET /api/recommendations/mixed` trả cùng một DTO cho cả 3 loại:

```json
[
  {
    "targetId": "page-id",
    "targetType": "CAFE_PAGE",
    "avatar": "https://example.com/cafe.png",
    "username": "Cafe Story Nguyen Hue",
    "fullName": "Cafe Story Nguyen Hue",
    "reason": "Gần khu vực của bạn",
    "city": "Ho Chi Minh"
  },
  {
    "targetId": "user-id",
    "targetType": "USER",
    "avatar": "https://example.com/user.png",
    "username": "minhanh",
    "fullName": "Nguyen Minh Anh",
    "reason": "Cùng khu vực với bạn",
    "city": "Ho Chi Minh"
  }
]
```

## Optional Fields I Recommend Later

Không cần cho UI hiện tại, nhưng nên cân nhắc khi làm follow/navigate/tracking:

| Field | Reason |
| --- | --- |
| `isFollowing` | Đổi button từ `Theo dõi` sang `Đang theo dõi`. |
| `isVerified` | Hiển thị tick xanh cho reviewer/page xác thực. |
| `score` | Debug ranking trong giai đoạn dev, có thể ẩn ở production. |
| `mutualCount` | Hiển thị kiểu "3 người bạn theo dõi cũng quan tâm". |
| `userId` | Cần nếu `targetType = REVIEWER` nhưng follow theo user account. |

MVP nên giữ API gọn:

```java
private UUID targetId;
private RecommendationTargetType targetType;
private String avatar;
private String username;
private String fullName;
private String reason;
private String city;
```
