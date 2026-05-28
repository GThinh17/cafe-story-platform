# Phase 04 - Organic Feed Cursor Algorithm

## Goal

Tao organic feed cursor cho user blog + cafe page blog, chua chen quang cao.

## API

Co the giu endpoint feed hien co va nang contract:

```http
GET /api/feed/organic?size=20&cursor=<base64-json>
```

Hoac dung internal service truoc, de Phase 07 expose `/api/feed`.

## Cursor Contract

Cursor JSON truoc khi encode:

```json
{
  "afterScore": 123.45,
  "afterCreatedAt": "2026-05-28T10:00:00",
  "afterId": "blog-uuid",
  "version": 1
}
```

Sort order:

```text
organicScore DESC, createdAt DESC, id DESC
```

Keyset condition:

```text
score < afterScore
OR (score = afterScore AND createdAt < afterCreatedAt)
OR (score = afterScore AND createdAt = afterCreatedAt AND id < afterId)
```

## MVP Organic Score

Neu co ranking/recommendation service hien co, uu tien dung score do.

Neu can cong thuc MVP:

```text
engagementScore = likeCount * 2 + commentCount * 4 + shareCount * 5
freshnessScore = exp(-ageHours / 36) * 30
pageBonus = pageId != null ? 5 : 0
reportPenalty = reportCount * 10

organicScore = engagementScore + freshnessScore + pageBonus - reportPenalty
```

## Query Rule

```text
status = PUBLISHED
exclude HIDDEN / REMOVED
exclude AI moderation VIOLATION if moderation is available
```

## Files Expected To Change

- Feed service/repository hien co
- `BlogRepository.java`
- Cursor utility DTO/service
- Organic feed response DTO
- Tests for pagination and ordering

## MVP Algorithm

```java
limit = clamp(size, 1, 50);
fetch limit + 1 organic blog rows using ranking order and cursor;
map to BlogResponseDTO with display author fields;
hasMore = rows.size() > limit;
nextCursor = last returned organic item position;
```

## Acceptance Criteria

- User blog va cafe page blog deu co the xuat hien trong organic feed.
- Cursor khong lap item giua cac page.
- Feed sort on dinh khi co bai cung score/cung createdAt nho tie-break `id`.
- Khong tra blog hidden/removed.

## Tests

- Ranking order by score desc.
- Cursor next page.
- Same score tie-break by `createdAt`, then `id`.
- Page blog has `displayAuthorType=CAFE_PAGE`.

