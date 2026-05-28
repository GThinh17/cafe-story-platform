# Phase 05 - Mixed Feed Contract

## Goal

Chuan hoa response feed tong hop de sau nay co the tra blog va ad trong cung mot list.

## Feed Item Types

```text
USER_BLOG
CAFE_PAGE_BLOG
SPONSORED_CAFE
```

## DTO Contract

Top-level response:

```json
{
  "items": [
    {
      "itemType": "USER_BLOG",
      "blog": {},
      "ad": null
    }
  ],
  "nextCursor": "...",
  "hasMore": true
}
```

Feed item:

```text
itemType
blog
ad
position
trackingToken
```

MVP validation:

```text
USER_BLOG: blog != null, ad == null
CAFE_PAGE_BLOG: blog != null, ad == null
SPONSORED_CAFE: blog == null, ad != null
```

## Type Rule

```java
if (blog.getPage() == null) {
    itemType = USER_BLOG;
} else {
    itemType = CAFE_PAGE_BLOG;
}
```

## Files Expected To Change

- `FeedItemType.java`
- `FeedItemResponseDTO.java`
- `FeedResponseDTO.java`
- Feed service adapter/helper
- Tests for DTO mapping

## MVP Algorithm

```java
organicItems = organicFeedService.getNext(...);
feedItems = organicItems.map(blog -> {
    type = blog.pageId == null ? USER_BLOG : CAFE_PAGE_BLOG;
    return FeedItemResponse(type, blog, null);
});
return FeedResponse(feedItems, organicNextCursor, organicHasMore);
```

## Acceptance Criteria

- Feed response khong con bat FE doan blog thuoc user hay page.
- FE co the render dua tren `itemType`.
- Sponsored field co san trong contract nhung phase nay chua can co data.

## Tests

- User blog maps to `USER_BLOG`.
- Cafe page blog maps to `CAFE_PAGE_BLOG`.
- Response keeps next cursor and hasMore from organic feed.

