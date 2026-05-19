# Reviewer Service Endpoints

Base URL:

```http
http://localhost:8080/api/reviewers
```

## Important Notes

- Use `userId` only when creating a reviewer from an existing user.
- After a reviewer is created, use `reviewerId` for reviewer stats, payouts, badges, and history APIs.
- `requesterId` is the user who is calling the API.
- A normal user can only view their own reviewer profile data.
- Admin-only APIs require the requester user to have the `ADMIN` role in `user_roles`.
- Most reviewer endpoints do not require a request body. Send data through path variables and query params.
- Valid `period` values: `day`, `week`, `month`, `3months`.
- Valid `month` format: `YYYY-MM`, for example `2026-05`.
- Valid `segment` values: `inactive`, `new`, `active`, `strong`, `top`, `elite`.
- Valid `groupBy` values: `city`, `province`, `area`.

## Database Role Setup

Seed roles before testing admin or reviewer creation:

```sql
INSERT INTO roles (name)
VALUES ('USER'), ('REVIEWER'), ('ADMIN')
ON CONFLICT (name) DO NOTHING;
```

Assign admin role to a user:

```sql
INSERT INTO user_roles (user_id, role_id, created_at)
SELECT '<ADMIN_USER_ID>'::uuid, id, NOW()
FROM roles
WHERE name = 'ADMIN'
ON CONFLICT DO NOTHING;
```

## 1. Create Reviewer

Creates a reviewer profile for an existing user and assigns the `REVIEWER` role through `user_roles`.

```http
POST /api/reviewers/create/{userId}
```

Postman example:

```http
POST http://localhost:8080/api/reviewers/create/10101010-1010-1010-1010-101010101010
```

Body: none

Response example:

```json
{
  "reviewerId": "11111111-1111-1111-1111-111111111111",
  "userId": "10101010-1010-1010-1010-101010101010",
  "role": "REVIEWER"
}
```

## 2. Get Reviewer Stats

Gets reviewer engagement stats for a period.

```http
GET /api/reviewers/{reviewerId}/stats?requesterId={userId}&period={period}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `requesterId` | yes | `10101010-1010-1010-1010-101010101010` | User calling the API |
| `period` | yes | `month` | `day`, `week`, `month`, `3months` |

Postman example:

```http
GET http://localhost:8080/api/reviewers/11111111-1111-1111-1111-111111111111/stats?requesterId=10101010-1010-1010-1010-101010101010&period=month
```

Response example:

```json
{
  "reviewerId": "11111111-1111-1111-1111-111111111111",
  "period": "month",
  "likeCount": 10,
  "shareCount": 3,
  "commentCount": 5,
  "score": 44
}
```

Score formula:

```text
score = likeCount + shareCount * 3 + commentCount * 5
```

## 3. Get Reviewer Ranking

Gets reviewer leaderboard by engagement score.

```http
GET /api/reviewers/ranking?period={period}&page={page}&limit={limit}
```

Optional location filters:

```http
GET /api/reviewers/ranking?period={period}&page={page}&limit={limit}&city={city}&province={province}&area={area}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `period` | yes | `month` | `day`, `week`, `month`, `3months` |
| `page` | no | `1` | Default `1` |
| `limit` | no | `20` | Default `20`, max `100` |
| `city` | no | `HCM` | Case-insensitive |
| `province` | no | `HCM` | Case-insensitive |
| `area` | no | `D1` | Case-insensitive. `district` is still accepted as a backward-compatible alias. |

Postman example:

```http
GET http://localhost:8080/api/reviewers/ranking?period=month&page=1&limit=20
```

With location filter:

```http
GET http://localhost:8080/api/reviewers/ranking?period=month&page=1&limit=20&city=HCM
```

Response example:

```json
[
  {
    "rank": 1,
    "reviewerId": "11111111-1111-1111-1111-111111111111",
    "score": 44,
    "likeCount": 10,
    "shareCount": 3,
    "commentCount": 5,
    "badge": "IRON",
    "location": "HCM"
  }
]
```

Ranking sort:

```text
score desc, commentCount desc, shareCount desc, likeCount desc, reviewerId asc
```

## 4. Get Reviewers By Segment

Gets reviewers in a monthly segment.

```http
GET /api/reviewers/segments?month={YYYY-MM}&segment={segment}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `month` | yes | `2026-05` | Format `YYYY-MM` |
| `segment` | yes | `active` | `inactive`, `new`, `active`, `strong`, `top`, `elite` |

Postman example:

```http
GET http://localhost:8080/api/reviewers/segments?month=2026-05&segment=active
```

Response example:

```json
[
  {
    "reviewerId": "11111111-1111-1111-1111-111111111111",
    "segment": "active",
    "score": 150,
    "likeCount": 50,
    "shareCount": 10,
    "commentCount": 14
  }
]
```

Segment rules:

| Segment | Score |
| --- | --- |
| `inactive` | `0` |
| `new` | `1 - 99` |
| `active` | `100 - 299` |
| `strong` | `300 - 699` |
| `top` | `700 - 1499` |
| `elite` | `1500+` |

## 5. Get Geo Analytics

Gets reviewer analytics grouped by location.

```http
GET /api/reviewers/geo?period={period}&groupBy={groupBy}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `period` | yes | `month` | `day`, `week`, `month`, `3months` |
| `groupBy` | yes | `city` | `city`, `province`, `area` |

Postman example:

```http
GET http://localhost:8080/api/reviewers/geo?period=month&groupBy=city
```

Response example:

```json
[
  {
    "locationName": "HCM",
    "groupBy": "city",
    "reviewerCount": 3,
    "totalLikes": 20,
    "totalShares": 5,
    "totalComments": 8,
    "totalScore": 75,
    "averageScore": 25.0,
    "topReviewer": "11111111-1111-1111-1111-111111111111"
  }
]
```

Note:

- Reviewers without location are grouped as `unknown`.

## 6. Generate Monthly Payouts

Admin-only. Generates payout records for all reviewers for a month.

```http
POST /api/reviewers/payouts/generate?requesterId={adminUserId}&month={YYYY-MM}&overwrite={true|false}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `requesterId` | yes | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | Must have `ADMIN` role |
| `month` | yes | `2026-05` | Format `YYYY-MM` |
| `overwrite` | no | `false` | Default `false` |

Postman example:

```http
POST http://localhost:8080/api/reviewers/payouts/generate?requesterId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa&month=2026-05&overwrite=false
```

Recalculate existing records:

```http
POST http://localhost:8080/api/reviewers/payouts/generate?requesterId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa&month=2026-05&overwrite=true
```

Body: none

Response example:

```json
[
  {
    "id": "99999999-9999-9999-9999-999999999999",
    "reviewerId": "11111111-1111-1111-1111-111111111111",
    "payoutMonth": "2026-05",
    "likeCount": 10,
    "shareCount": 3,
    "commentCount": 5,
    "likeAmount": 1000,
    "shareAmount": 900,
    "commentAmount": 2500,
    "totalAmount": 4400,
    "payoutStatus": "CALCULATED"
  }
]
```

Payout formula: (VND)

```text
totalAmount = likeCount * 100 + shareCount * 300 + commentCount * 500
```

## 7. Get Reviewer Payout History

Gets payout history for one reviewer.

```http
GET /api/reviewers/{reviewerId}/payouts?requesterId={userId}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `requesterId` | yes | `10101010-1010-1010-1010-101010101010` | Reviewer owner or admin |

Postman example:

```http
GET http://localhost:8080/api/reviewers/11111111-1111-1111-1111-111111111111/payouts?requesterId=10101010-1010-1010-1010-101010101010
```

Response example:

```json
[
  {
    "id": "99999999-9999-9999-9999-999999999999",
    "reviewerId": "11111111-1111-1111-1111-111111111111",
    "payoutMonth": "2026-05",
    "likeCount": 10,
    "shareCount": 3,
    "commentCount": 5,
    "likeAmount": 1000,
    "shareAmount": 900,
    "commentAmount": 2500,
    "totalAmount": 4400,
    "payoutStatus": "CALCULATED"
  }
]
```

## 8. Generate Monthly Badges

Admin-only. Generates monthly badge records for all reviewers.

```http
POST /api/reviewers/badges/generate?requesterId={adminUserId}&month={YYYY-MM}&overwrite={true|false}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `requesterId` | yes | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | Must have `ADMIN` role |
| `month` | yes | `2026-05` | Format `YYYY-MM` |
| `overwrite` | no | `false` | Default `false` |

Postman example:

```http
POST http://localhost:8080/api/reviewers/badges/generate?requesterId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa&month=2026-05&overwrite=false
```

Recalculate existing records:

```http
POST http://localhost:8080/api/reviewers/badges/generate?requesterId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa&month=2026-05&overwrite=true
```

Body: none

Response example:

```json
[
  {
    "id": "88888888-8888-8888-8888-888888888888",
    "reviewerId": "11111111-1111-1111-1111-111111111111",
    "month": "2026-05",
    "score": 44,
    "badge": "IRON",
    "likeCount": 10,
    "shareCount": 3,
    "commentCount": 5
  }
]
```

Badge rules:

| Badge | Score |
| --- | --- |
| `IRON` | `0 - 99` |
| `BRONZE` | `100 - 299` |
| `SILVER` | `300 - 699` |
| `GOLD` | `700 - 1499` |
| `DIAMOND` | `1500+` |

## 9. Get Reviewer Badge History

Gets badge history for one reviewer.

```http
GET /api/reviewers/{reviewerId}/badges?requesterId={userId}
```

Query params:

| Name | Required | Example | Note |
| --- | --- | --- | --- |
| `requesterId` | yes | `10101010-1010-1010-1010-101010101010` | Reviewer owner or admin |

Postman example:

```http
GET http://localhost:8080/api/reviewers/11111111-1111-1111-1111-111111111111/badges?requesterId=10101010-1010-1010-1010-101010101010
```

Response example:

```json
[
  {
    "id": "88888888-8888-8888-8888-888888888888",
    "reviewerId": "11111111-1111-1111-1111-111111111111",
    "month": "2026-05",
    "score": 44,
    "badge": "IRON",
    "likeCount": 10,
    "shareCount": 3,
    "commentCount": 5
  }
]
```

## Common Errors

| Case | Expected Result |
| --- | --- |
| Invalid `period` | `400 BAD_REQUEST` |
| Invalid `month` format | `400 BAD_REQUEST` |
| Invalid `segment` | `400 BAD_REQUEST` |
| Invalid `groupBy` | `400 BAD_REQUEST` |
| Reviewer not found | `404 NOT_FOUND` |
| Non-owner user reads reviewer private history | `403 FORBIDDEN` |
| Non-admin generates payout/badge | `403 FORBIDDEN` |
| Generate duplicate payout/badge with `overwrite=false` | `409 CONFLICT` |


