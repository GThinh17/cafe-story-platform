# Phase 08 - Ad Tracking And Frequency Cap

## Goal

Them tracking impression/click va frequency cap cho sponsored cafe.

Nen lam phase nay sau khi FE da render sponsored card, vi impression nen duoc ghi khi client that su thay item.

## Data Model Impact

Phase nay moi nen them bang.

Suggested tables:

```text
ad_impressions
ad_clicks
```

`ad_impressions` fields:

```text
id
campaign_id
cafe_page_id
user_id nullable
session_id nullable
feed_request_id
position
created_at
```

`ad_clicks` fields:

```text
id
campaign_id
cafe_page_id
user_id nullable
session_id nullable
impression_id nullable
target_url
created_at
```

## APIs

Record impression:

```http
POST /api/ads/impressions
```

Record click:

```http
POST /api/ads/clicks
```

## Frequency Cap MVP

```text
max 3 impressions / campaign / user / day
max 8 sponsored impressions / user / day
max 2 sponsored items / 20 feed items
do not repeat same campaign in one response
```

For anonymous users:

```text
use sessionId from request/client-generated id
```

## Candidate Filtering With Cap

Before returning ad candidates:

```java
exclude campaigns where impression count for user today >= 3;
exclude cafe pages already sponsored in same feed response;
sort remaining candidates by adScore;
```

## Tracking Token

Each sponsored item should include opaque tracking token:

```text
trackingToken = signed/base64 payload containing campaignId, cafePageId, feedRequestId, position, expiresAt
```

Server validates token when recording impression/click.

## Files Expected To Change

- New entities:
  - `AdImpression`
  - `AdClick`
- New repositories:
  - `AdImpressionRepository`
  - `AdClickRepository`
- New DTOs:
  - `AdImpressionRequestDTO`
  - `AdClickRequestDTO`
  - tracking response if needed
- New controller:
  - `AdTrackingController`
- Candidate service cap filtering
- DBML/schema docs
- Tests

## Acceptance Criteria

- Impression can be recorded once sponsored item is rendered.
- Click can be recorded with tracking token.
- Candidate service excludes campaigns over cap.
- Invalid/expired tracking token is rejected.
- Feed still works if tracking endpoint is not called.

## Tests

- Record impression success.
- Record click success.
- Frequency cap excludes campaign.
- Invalid tracking token returns 400/401.
- Anonymous session tracking works.

