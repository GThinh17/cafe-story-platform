# Phase 07 - Feed Assembly With Ad Slots

## Goal

Expose mixed feed API tra user blog, cafe page blog, va sponsored cafe trong cung response.

## API

```http
GET /api/feed?size=20&cursor=<base64-json>
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
  "organicAfterScore": 123.45,
  "organicAfterCreatedAt": "2026-05-28T10:00:00",
  "organicAfterId": "blog-uuid",
  "adOffset": 2,
  "seed": "user-or-session-id",
  "version": 1
}
```

## MVP Slot Rule

Voi `size=20`:

```text
organicLimit = 18
maxAds = 2
ad slots = [6, 14]
```

Meaning:

```text
items 1-6: organic
item 7: sponsored cafe
items 8-14: organic
item 15: sponsored cafe
items 16-20: organic
```

Neu khong co ad:

```text
tra organic items binh thuong
```

Neu chi co 1 ad:

```text
chen vao slot 6
```

## MVP Algorithm

```java
size = clamp(request.size, 1, 50);
maxAds = size >= 12 ? 2 : size >= 6 ? 1 : 0;
organicLimit = size - maxAds;

organicPage = organicFeedService.getNext(
    userContext,
    organicCursor,
    organicLimit
);

ads = sponsoredCafeCandidateService.getNext(
    userContext,
    adOffset,
    maxAds
);

feedItems = map organic blogs to feed items;
insert ads at slots [6, 14] if available and slot <= feed size;

nextCursor = {
    organicAfterScore: organicPage.last.score,
    organicAfterCreatedAt: organicPage.last.createdAt,
    organicAfterId: organicPage.last.id,
    adOffset: adOffset + ads.size(),
    seed: userContext.seed,
    version: 1
};
```

## Duplicate Protection

MVP duplicate rules:

```text
khong lap cung blogId trong response
khong lap cung cafePageId sponsored trong response
khong chen sponsored cafe ngay sau organic blog cua cung cafe page neu tranh duoc
```

## Files Expected To Change

- `FeedController.java`
- `FeedService.java`
- `FeedServiceImpl.java`
- Cursor encode/decode utility
- `FeedResponseDTO.java`
- `FeedItemResponseDTO.java`
- `SponsoredCafeResponseDTO.java`
- Controller/service tests

## Acceptance Criteria

- `GET /api/feed?size=20` tra mixed response.
- Blog user/page co item type dung.
- Sponsored cafe chen toi da 2 item.
- Ads nam gan slot 6 va 14 khi co candidate.
- Cursor page 2 khong lap organic item page 1.
- Feed van tra thanh cong khi khong co ad.

## Tests

- Feed with 20 size inserts 2 ads.
- Feed with no ads returns organic only.
- Cursor next page uses last organic item.
- No duplicate sponsored cafe page in same response.
- Small size feed handles ad count safely.

