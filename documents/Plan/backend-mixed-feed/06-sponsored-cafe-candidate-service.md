# Phase 06 - Sponsored Cafe Candidate Service

## Goal

Tao service noi bo de lay danh sach sponsored cafe hop le cho feed.

## Data Model Impact

MVP co the dung `ad_campaigns` hien co. Chua can them bang moi neu chi chen quang cao don gian.

Chi can them bang khi lam tracking/frequency cap o Phase 08.

## Candidate Rule

Campaign hop le:

```text
campaign active
current time nam trong startAt/endAt neu campaign co field do
cafe page ACTIVE
cafe page pageActive = true neu co field
campaign con budget/cap neu bang hien co co field
```

Region matching MVP:

```text
neu user co city/region:
  uu tien campaign cafe page cung city
neu khong:
  lay active campaigns moi nhat/uu tien nhat
```

## Ad Score MVP

Neu campaign co priority/bid:

```text
regionScore = sameCity ? 30 : 0
priorityScore = priority or bid
freshnessScore = exp(-campaignAgeDays / 14) * 5

adScore = regionScore + priorityScore + freshnessScore
```

Neu campaign chua co priority/bid:

```text
adScore = regionScore + freshnessScore
```

## Ad DTO

```text
campaignId
cafePageId
cafeName
cafeAvatarUrl
cafeCoverUrl
headline
description
ctaLabel
targetUrl
trackingToken
```

## Files Expected To Change

- `AdCampaignRepository.java`
- `AdCampaignService.java` or new candidate service interface
- `AdCampaignServiceImpl.java` or new candidate service impl
- `SponsoredCafeResponseDTO.java`
- Service tests

## MVP Algorithm

```java
limit = maxAdsNeeded + reserve;
candidates = find active campaign candidates by region/context;
sort by adScore desc, createdAt desc, id desc;
dedupe by cafePageId;
return first requested count using adOffset rotation;
```

## Ad Offset Cursor

Trong mixed feed cursor se co:

```json
{
  "adOffset": 2
}
```

`adOffset` dung de rotate ung vien ad giua cac page response.

## Acceptance Criteria

- Chi tra ad cua active campaign.
- Chi tra ad cua active cafe page.
- Khong lap cung `cafePageId` trong mot candidate batch.
- Neu khong co ad hop le thi tra list rong, feed van hoat dong.

## Tests

- Active campaign is returned.
- Inactive/expired campaign is excluded.
- Suspended cafe page campaign is excluded.
- Candidate list dedupes by cafe page.

