# Backend Mixed Feed Plan

## Scope

Bo ke hoach nay chi tap trung backend cho luong feed CafeStory gom:

- Bai cua user.
- Bai cua cafe page.
- Sponsored cafe/ad campaign.

Frontend se xu ly sau. Backend can chuan hoa contract truoc de FE chi render theo `itemType`, `displayName`, va `displayAvatarUrl`.

## Existing Foundation

- `blogs.author_user_id` la nguoi that tao bai.
- `blogs.page_id` neu co gia tri thi bai thuoc cafe page.
- `ad_campaigns` da la nguon ung vien quang cao.
- Blog feed/ranking hien co co the duoc tan dung cho organic ranking.
- Auth dung JWT trong HttpOnly cookie.

## Phase Order

1. [Phase 01 - Blog Author Display Contract](01-blog-author-display-contract.md)
2. [Phase 02 - Cafe Page Blog Listing Cursor API](02-cafe-page-blog-listing-cursor-api.md)
3. [Phase 03 - Cafe Page Posting Authorization](03-cafe-page-posting-authorization.md)
4. [Phase 04 - Organic Feed Cursor Algorithm](04-organic-feed-cursor-algorithm.md)
5. [Phase 05 - Mixed Feed Contract](05-mixed-feed-contract.md)
6. [Phase 06 - Sponsored Cafe Candidate Service](06-sponsored-cafe-candidate-service.md)
7. [Phase 07 - Feed Assembly With Ad Slots](07-feed-assembly-with-ad-slots.md)
8. [Phase 08 - Ad Tracking And Frequency Cap](08-ad-tracking-and-frequency-cap.md)

## MVP Recommendation

Lam toi Phase 07 la co MVP backend du de FE consume:

- Blog response phan biet user/page.
- Cafe page lay duoc bai rieng bang cursor.
- Feed organic co cursor.
- Mixed feed tra `USER_BLOG`, `CAFE_PAGE_BLOG`, `SPONSORED_CAFE`.
- Ads duoc chen theo slot 6 va 14 trong page size 20.

Phase 08 nen lam sau khi FE render sponsored card xong de tracking impression/click dung hanh vi that.

## Global API Shape

Cursor-based API:

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

Feed item type:

```text
USER_BLOG
CAFE_PAGE_BLOG
SPONSORED_CAFE
```

## Global Validation

After each backend phase:

```powershell
mvn test
```

For focused changes, run target tests first:

```powershell
mvn test "-Dtest=BlogServiceImplTest,BlogControllerTest"
```

