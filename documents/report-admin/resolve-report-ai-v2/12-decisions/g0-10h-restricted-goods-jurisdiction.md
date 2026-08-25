# G0-10H — Restricted Goods Registry and Jurisdiction

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-008` |
| Trạng thái | `APPROVED` |
| Approval chat | Ủy quyền hoàn thành toàn bộ `G0-10` theo phương án khuyến nghị, ngày `2026-07-23` |
| Rule | `CSR.COM.001` |
| Affected | `RESTRICTED_GOODS`, commerce finding, jurisdiction/evidence |
| Nature | Internal platform baseline; không thay thế legal review |

## 2. Vấn đề

Một label `RESTRICTED_GOODS` không đủ để quyết định:

- item/service cụ thể là gì;
- content đang bán, thảo luận hay đưa tin;
- rule áp ở jurisdiction nào;
- regulated item có license/exception hay không;
- một post có đủ để xử lý toàn bộ USER/CAFE_PAGE hay không.

AI không được dùng model memory để tự tạo danh mục hàng cấm.

## 3. Các phương án

### H1 — Versioned two-tier registry — Khuyến nghị

#### `PROHIBITED_BASELINE`

| Code | Category |
|---|---|
| `RG_WEAPONS_EXPLOSIVES` | Weapons, explosives hoặc destructive devices offered for transaction |
| `RG_ILLEGAL_DRUGS` | Illegal drugs hoặc controlled substances offered outside permitted context |
| `RG_STOLEN_GOODS_CREDENTIALS` | Stolen goods, credentials hoặc access tokens/accounts |
| `RG_COUNTERFEIT_GOODS` | Counterfeit goods represented for transaction |
| `RG_HUMAN_EXPLOITATION` | Human exploitation/trafficking-related transaction or service |
| `RG_SEXUAL_SERVICES` | Commercial sexual services |
| `RG_ILLICIT_FINANCIAL_ACCESS` | Illicit financial accounts, payment access hoặc laundering service |

Các category này là platform baseline không cho phép public transaction offer.
Mention, news, education hoặc prevention không tự là transaction.

#### `REGULATED_MANUAL`

| Code | Category |
|---|---|
| `RG_ALCOHOL` | Alcohol-related offer/promotion |
| `RG_TOBACCO_NICOTINE` | Tobacco, nicotine hoặc vaping product |
| `RG_REGULATED_MEDICINE_DEVICE` | Prescription/regulated medicine hoặc medical device |
| `RG_GAMBLING_LOTTERY` | Gambling, betting hoặc lottery service |

Các category này không tự `RESOLVE`. Cần jurisdiction, license/authorization, age/context và
human review. Cafe/merchant promotion hợp lệ không được kết luận chỉ từ keyword.

### H2 — Một global denylist trong prompt

Dễ triển khai nhưng khó version/audit, không xử lý jurisdiction và model có thể làm drift list.
Không khuyến nghị.

### H3 — Tắt Restricted Goods rule

Giảm scope nhưng bỏ trống commerce harm quan trọng. Không cần thiết nếu dùng H1 + A0.

## 4. Jurisdiction model

```text
platformPolicyVersion
restrictedGoodsRegistryVersion
marketJurisdiction
targetLocationEvidence
transactionDestinationEvidence
licenseOrAuthorizationEvidence
ageRestrictionContext
```

Rules:

- `marketJurisdiction` phải đến từ trusted platform/location evidence hoặc human input;
- AI không suy jurisdiction từ language, name hoặc IP không được cung cấp;
- jurisdiction missing/conflicting trong regulated category → `NEEDS_MANUAL_REVIEW`;
- legal status thay đổi theo thời gian phải pin source/version/date;
- platform MAY cấm một category rộng hơn local law, nhưng phải ghi đây là platform policy;
- không tuyên bố “illegal” nếu evidence chỉ chứng minh “platform-prohibited”.

## 5. Transaction-intent guard

Finding cần phân biệt:

- offer/sale/facilitation;
- request-to-buy;
- link/contact/payment instruction;
- informational/news/education/prevention;
- product mention hoặc cafe menu context;
- joke/fiction.

Item keyword một mình không đủ. Cần observation cho transaction intent và target association.

## 6. Decision/action boundary

- AI chỉ recommendation theo B1.
- `REGULATED_MANUAL` mặc định manual, không auto `REJECT/RESOLVE`.
- Content-level `HIDE` cần một Admin sau sufficiency.
- `REMOVE` hoặc actor/page suspension tuân C1 quorum.
- Một listing/post không mặc định đủ để suspend USER/CAFE_PAGE.
- Counterfeit/IP evidence MAY tạo multi-rule finding nhưng sufficiency đánh giá riêng.

## 7. Khuyến nghị

Chọn `H1`.

Registry hai tầng cho phép xử lý safety baseline ngay nhưng không gộp alcohol/tobacco/medicine
vào cùng một kết luận “bất hợp pháp” thiếu jurisdiction.

## 8. Acceptance record đã duyệt

```text
decisionId: BD-008
selectedOption: H1_VERSIONED_TWO_TIER_RESTRICTED_REGISTRY
registryVersion: RGR-1.0.0-proposed.1
prohibitedBaselineCount: 7
regulatedManualCount: 4
keywordAloneCanSubstantiate: false
modelCanInferJurisdiction: false
missingJurisdictionForRegulatedBehavior: NEEDS_MANUAL_REVIEW
platformProhibitedEqualsIllegal: false
```

## 9. Kết luận

Phương án `H1_VERSIONED_TWO_TIER_RESTRICTED_REGISTRY` đã được duyệt theo ủy quyền
hoàn thành toàn bộ `G0-10`. Registry vẫn ở trạng thái proposed cho tới khi có legal/privacy review,
implementation, test và activation gate riêng.
