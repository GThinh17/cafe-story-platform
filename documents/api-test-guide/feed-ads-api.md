# Feed Ads API Guide

## Overview

Module quảng cáo MVP hiện chỉ hỗ trợ feed ads.

Luồng chính:

1. User mua gói quảng cáo bằng `adFeeId` qua Payment API.
2. Sau khi payment thành `PAID`, cafe page owner tạo ad campaign bằng `paymentId`.
3. Campaign có thể tạo dạng draft hoặc active ngay.
4. Khi feed service chèn ad vào feed, backend ghi impression, tăng `servedImpressions`, cập nhật daily stats.
5. Khi user bấm quảng cáo, frontend gọi API record click.

Campaign hết hạn khi một trong các điều kiện xảy ra:

- `servedImpressions >= 10000`
- đã chạy đủ 30 ngày từ `startAt`
- `now > endAt` nếu campaign có `endAt`

## 1. Mua Gói Quảng Cáo

```http
POST /api/payments
Content-Type: application/json
```

### Request

Chỉ truyền đúng một trong hai field:

- `extraFeeId`
- `adFeeId`

Để mua gói quảng cáo, truyền `adFeeId` và không truyền `extraFeeId`.

```json
{
  "buyerId": "uuid-cua-user-mua-goi",
  "adFeeId": "uuid-cua-ad-fee",
  "paymentMethod": "VNPAY"
}
```

`paymentMethod` hỗ trợ:

```text
STRIPE_CARD
BANK_TRANSFER
VNPAY
```

### Success Response

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "uuid-payment",
    "buyerId": "uuid-cua-user-mua-goi",
    "extraFeeId": null,
    "adFeeId": "uuid-cua-ad-fee",
    "paymentMethod": "VNPAY",
    "amount": 500000.00,
    "currency": "VND",
    "paymentStatus": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "qrCodeUrl": null,
    "transferContent": "CAFE_VNPAY_uuid-payment",
    "createdAt": "2026-05-25T14:00:00",
    "paidAt": null,
    "expiredAt": "2026-05-25T14:30:00"
  }
}
```

### Sau Khi Thanh Toán Thành Công

Khi VNPAY/Stripe callback thành công hoặc bank transfer được mark paid:

- Payment được đổi sang `PAID`.
- Nếu payment là `adFeeId`, backend không tự tạo campaign.
- Frontend/admin cần gọi API tạo campaign riêng và truyền `paymentId` đã `PAID`.

## 2. Kiểm Tra Payment

```http
GET /api/payments/{paymentId}
```

### Success Response

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "uuid-payment",
    "buyerId": "uuid-user",
    "extraFeeId": null,
    "adFeeId": "uuid-ad-fee",
    "paymentMethod": "VNPAY",
    "amount": 500000.00,
    "currency": "VND",
    "paymentStatus": "PAID",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "qrCodeUrl": null,
    "transferContent": "CAFE_VNPAY_uuid-payment",
    "createdAt": "2026-05-25T14:00:00",
    "paidAt": "2026-05-25T14:05:00",
    "expiredAt": "2026-05-25T14:30:00"
  }
}
```

## 3. Tạo Campaign Từ Paid Ad Payment

```http
POST /api/ad-campaigns
Content-Type: application/json
```

### Request

```json
{
  "paymentId": "uuid-payment-da-paid",
  "cafePageId": "uuid-cafe-page",
  "title": "Ưu đãi cà phê tháng này",
  "description": "Giảm 20% cho khách mới",
  "imageUrl": "https://example.com/ad.jpg",
  "targetUrl": "https://example.com/promo",
  "priority": 1,
  "targetRegions": [
    {
      "province": "Ho Chi Minh",
      "city": "Ho Chi Minh",
      "area": "District 1",
      "ward": "Ben Nghe"
    }
  ],
  "activateNow": true
}
```

Nếu `targetRegions` rỗng hoặc không truyền region nào, campaign là global campaign.

Nếu `activateNow = true`:

- `status = ACTIVE`
- `startAt = now` nếu chưa có
- `endAt = startAt + 30 ngày` nếu chưa có

Nếu `activateNow = false`:

- `status = DRAFT`
- Campaign chưa được serve lên feed.

### Success Response

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "adCampaignId": "uuid-campaign",
    "cafePageId": "uuid-cafe-page",
    "paymentId": "uuid-payment-da-paid",
    "title": "Ưu đãi cà phê tháng này",
    "description": "Giảm 20% cho khách mới",
    "imageUrl": "https://example.com/ad.jpg",
    "targetUrl": "https://example.com/promo",
    "status": "ACTIVE",
    "startAt": "2026-05-25T14:10:00",
    "endAt": "2026-06-24T14:10:00",
    "priority": 1,
    "maxImpressions": 10000,
    "servedImpressions": 0,
    "maxDurationDays": 30,
    "targetRegions": [
      {
        "adTargetRegionId": "uuid-target-region",
        "province": "Ho Chi Minh",
        "city": "Ho Chi Minh",
        "area": "District 1",
        "ward": "Ben Nghe"
      }
    ],
    "createdAt": "2026-05-25T14:10:00",
    "updatedAt": "2026-05-25T14:10:00"
  }
}
```

### Validation Chính

| Case | Result |
| --- | --- |
| `paymentId` không tồn tại | `404 Payment not found` |
| Payment chưa `PAID` | `400 Ad payment must be paid before creating campaign` |
| Payment không phải ad fee payment | `400 Payment is not an ad fee payment` |
| Payment đã gắn với campaign khác | `400 Payment is already attached to an ad campaign` |
| `cafePageId` không tồn tại | `404 Cafe page not found` |
| Buyer của payment không phải owner của cafe page | `403 Payment buyer must own the cafe page` |

## 4. Lấy Chi Tiết Campaign

```http
GET /api/ad-campaigns/{adCampaignId}
```

### Success Response

Response data giống object campaign ở API tạo campaign.

## 5. List Campaign Theo Cafe Page

```http
GET /api/ad-campaigns?cafePageId={cafePageId}
```

### Success Response

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": [
    {
      "adCampaignId": "uuid-campaign",
      "cafePageId": "uuid-cafe-page",
      "paymentId": "uuid-payment",
      "title": "Ưu đãi cà phê tháng này",
      "status": "ACTIVE",
      "maxImpressions": 10000,
      "servedImpressions": 12,
      "maxDurationDays": 30,
      "targetRegions": []
    }
  ]
}
```

## 6. Activate Campaign

```http
POST /api/ad-campaigns/{adCampaignId}/activate
```

### Behavior

- Dùng cho campaign đang `DRAFT` hoặc `PAUSED`.
- Set `status = ACTIVE`.
- Set `startAt = now` nếu chưa có.
- Set `endAt = startAt + 30 ngày` nếu chưa có.

## 7. Pause Campaign

```http
POST /api/ad-campaigns/{adCampaignId}/pause
```

### Behavior

- Chỉ pause campaign đang `ACTIVE`.
- Set `status = PAUSED`.
- Campaign `PAUSED` không được chọn để serve trong feed.

## 8. Record Ad Click

Frontend gọi API này khi user click vào ad card.

```http
POST /api/ad-campaigns/{adCampaignId}/clicks
Content-Type: application/json
```

### Request

Nếu có user login:

```json
{
  "userId": "uuid-user"
}
```

Nếu anonymous hoặc không cần tracking user:

```json
{}
```

### Behavior

- Ghi row vào `ad_clicks`.
- Tăng `clicks` trong `ad_daily_stats` theo ngày hiện tại.

## 9. Feed Response Khi Có Ads

Feed controller hiện tại vẫn trả bài viết organic từ:

```http
GET /api/blogs/feed
```

Backend đã có service để chèn ads vào feed:

```java
insertAdsIntoFeed(UUID userId, List<BlogFeedResponse> organicPosts)
```

Khi controller được nối vào service này, response mixed feed sẽ có dạng:

```json
[
  {
    "itemType": "BLOG",
    "blog": {
      "blogId": "uuid-blog",
      "contentPreview": "Nội dung bài viết...",
      "authorUserId": "uuid-author",
      "authorUserName": "user123",
      "pageId": "uuid-page",
      "regionId": "uuid-region",
      "windowType": "HOUR_24",
      "feedScore": 91.2,
      "rankPosition": 1,
      "createdAt": "2026-05-25T14:00:00",
      "computedAt": "2026-05-25T14:05:00"
    },
    "ad": null
  },
  {
    "itemType": "AD",
    "blog": null,
    "ad": {
      "adCampaignId": "uuid-campaign",
      "cafePageId": "uuid-cafe-page",
      "paymentId": "uuid-payment",
      "title": "Ưu đãi cà phê tháng này",
      "description": "Giảm 20% cho khách mới",
      "imageUrl": "https://example.com/ad.jpg",
      "targetUrl": "https://example.com/promo",
      "status": "ACTIVE",
      "priority": 1,
      "maxImpressions": 10000,
      "servedImpressions": 13,
      "maxDurationDays": 30,
      "targetRegions": []
    }
  }
]
```

Frontend render theo `itemType`:

```text
BLOG -> render BlogCard từ field blog
AD   -> render AdCard từ field ad
```

## 10. Những Gì Đã Thực Thi

- Đã chạy migration `2026-05-25_feed_ads_mvp.sql`.
- Đã verify DB có các bảng ads:
  - `ad_fees`
  - `ad_campaigns`
  - `ad_target_regions`
  - `ad_impressions`
  - `ad_clicks`
  - `ad_daily_stats`
- Đã verify `payments.extra_fee_id` nullable và `payments.ad_fee_id` nullable.
- Đã verify constraint chính:
  - `chk_payments_exactly_one_fee`
  - `chk_ad_fees_price_positive`
  - `chk_ad_campaigns_max_impressions`
  - `chk_ad_campaigns_max_duration_days`
  - `chk_ad_campaigns_served_impressions_non_negative`
  - `chk_ad_campaigns_served_impressions_max`
  - `uk_ad_daily_stats_campaign_date`
- Đã cập nhật `cafestory-schema.dbml` theo cấu trúc ads/payment mới.
