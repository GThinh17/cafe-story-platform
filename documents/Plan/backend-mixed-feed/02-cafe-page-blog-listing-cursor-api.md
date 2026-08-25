# Phase 02 - Cafe Page Blog Listing Cursor API

## Goal

Them API de cafe page detail lay rieng danh sach bai cua page do.

## API

```http
GET /api/cafe-pages/{pageId}/blogs?size=20&cursor=<base64-json>
```

Response:

```json
{
  "items": [],
  "nextCursor": "...",
  "hasMore": true
}
```

## Cursor Contract

Cursor JSON truoc khi encode:

```json
{
  "afterCreatedAt": "2026-05-28T10:00:00",
  "afterId": "blog-uuid",
  "version": 1
}
```

Sort order:

```text
createdAt DESC, id DESC
```

Keyset condition:

```text
createdAt < afterCreatedAt
OR (createdAt = afterCreatedAt AND id < afterId)
```

## Query Rule

```text
page_id = :pageId
status = PUBLISHED
```

Neu moderation da co decision:

```text
exclude AI moderation VIOLATION
```

## Files Expected To Change

- `CafePageController.java`
- `CafePageService.java`
- `CafePageServiceImpl.java` hoac tao service rieng neu repo da co pattern
- `BlogRepository.java`
- Cursor/page response DTOs
- Controller/service tests

## MVP Algorithm

```java
validate cafe page exists;
limit = clamp(size, 1, 50);
query limit + 1 blogs by pageId and cursor;
hasMore = rows.size() > limit;
items = first limit rows mapped to BlogResponseDTO;
nextCursor = cursor from last item if hasMore else null;
```

## Acceptance Criteria

- Page khong ton tai tra 404.
- Chi tra blog `PUBLISHED`.
- Cursor page 2 khong trung item page 1.
- Response dung display author contract cua Phase 01.

## Tests

- Get first page by cafe page.
- Get next page using cursor.
- Hidden/removed blogs khong xuat hien.
- Invalid page id tra 404.

