# G0-10J — Intake Reason Versioning and Disposition

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-010` |
| Trạng thái | `APPROVED` |
| Approval chat | Ủy quyền hoàn thành toàn bộ `G0-10` theo phương án khuyến nghị, ngày `2026-07-23` |
| Runtime baseline | 22 reasons đã audit |
| Selected option | `J1_VERSIONED_COMPATIBLE_ALIAS_DEPRECATION` |

## 2. Nguyên tắc đã chốt

- Intake reason chỉ là routing claim, không phải policy finding hoặc action.
- Giữ khả năng đọc toàn bộ 22 reason lịch sử; không rewrite snapshot cũ.
- Client mới không tạo report bằng reason đã deprecated.
- Alias/split chỉ chọn candidate rules; không tự suy subtype hoặc violation.
- Backend là source of truth cho intake catalog version và compatibility.

## 3. Disposition 22 reasons

### `ACTIVE_POLICY_ROUTE` — 16

`SPAM`, `BULLYING_OR_UNWANTED_CONTACT`, `SCAM_OR_FRAUD`, `HARASSMENT`,
`SELF_HARM_OR_ABNORMAL_EATING`, `HATE_SPEECH`, `VIOLENCE_THREAT`,
`NUDITY_OR_SEXUAL_ACTIVITY`, `SEXUAL_CONTENT`, `PRIVACY_VIOLATION`,
`COPYRIGHT_VIOLATION`, `FALSE_INFORMATION`, `INTELLECTUAL_PROPERTY`,
`IMPERSONATION`, `FAKE_OR_MISLEADING`, `RESTRICTED_GOODS`.

Các reason broad vẫn phải chạy rule selection/evidence; tên reason không đủ tạo finding.

### `DEPRECATED_MIXED_ROUTE` — 2

| Legacy reason | Candidate route | New submission |
|---|---|---|
| `VIOLENCE_HATE_OR_EXPLOITATION` | Safety + Hate; human disambiguation | Không hiển thị |
| `SCAM_FRAUD_OR_SPAM` | Scam/Fraud + Spam; human/evidence disambiguation | Không hiển thị |

Backend tiếp tục accept khi đọc lịch sử hoặc trong compatibility window. Không map cưỡng bức
sang một canonical reason duy nhất.

### `ROUTING_ONLY_NON_VIOLATION` — 4

| Reason | Behavior |
|---|---|
| `DISLIKE_CONTENT` | Preference/product feedback; không tạo violation |
| `INAPPROPRIATE_IMAGE` | Yêu cầu vision/manual routing; label không phải evidence |
| `OFF_TOPIC_OR_IRRELEVANT` | Quality/relevance route; mặc định non-violation |
| `OTHER` | Description-based manual triage; bắt buộc description |

## 4. Version và migration

Version đầu: `IRC-2.0.0-proposed.1`.

```text
reasonCode
catalogVersion
status
candidateRuleIds[]
validTargetTypes[]
replacementOrSplitRoutes[]
effectiveFrom
deprecatedFrom
```

Migration bắt buộc:

1. inventory caller và usage count;
2. publish catalog read API hoặc generated contract;
3. cập nhật FE/mobile theo version;
4. giữ backend compatibility cho legacy codes;
5. không xóa row hoặc đổi historical reason;
6. theo dõi unknown/deprecated submission trước khi kết thúc compatibility.

## 5. Acceptance record

```text
decisionId: BD-010
selectedOption: J1_VERSIONED_COMPATIBLE_ALIAS_DEPRECATION
runtimeReasonCount: 22
activePolicyRouteCount: 16
deprecatedMixedRouteCount: 2
routingOnlyCount: 4
deleteHistoricalReasons: false
reasonDirectlyCreatesFinding: false
reasonDirectlySelectsAction: false
```

## 6. Kết luận

Chốt disposition ở mức contract. Chưa migrate database, chưa đổi API/client và chưa activate
`IRC-2.0.0-proposed.1`.
