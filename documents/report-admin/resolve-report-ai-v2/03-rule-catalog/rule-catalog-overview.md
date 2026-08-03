# Rule Catalog Overview — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Approval chat | `APPROVE_G0-08` |
| Trạng thái | `PROPOSED` |
| Catalog version | `RC-2.0.0-proposed.1` |
| Policy dependency | `PF-2.0.0-proposed.1` |
| Phạm vi | Rule tổng quát và mapping 22 intake reasons; chưa target-specific deep policy |
| Automation baseline | `A0 RECOMMEND_ONLY` |

`APPROVE_G0-08` cho phép viết bản dự thảo catalog. Approval này không làm catalog thành
`APPROVED/ACTIVE`, không cấp quyền sửa source và không cấp quyền auto-apply.

## 2. Mục tiêu

Catalog giải quyết hai gap `CG-004` và `CG-005`:

1. tạo Rule ID ổn định thay cho free-form `ruleCode` do model tự sinh;
2. tách reporter intake reason khỏi policy rule;
3. mô tả tiêu chí, evidence, counter-evidence và missing evidence tối thiểu;
4. tạo một mapping đầy đủ cho 22 reason runtime đã audit;
5. buộc AI abstain khi không đủ căn cứ;
6. giữ các quyết định threshold, owner và target-specific depth cho gate phù hợp.

## 3. Rule record chuẩn

Mỗi rule MUST có các trường:

```text
ruleId
ruleVersion
catalogVersion
policyFamilyId
title
ruleType
status
applicableTargetTypes
violationCriteria
requiredEvidence
counterEvidence
criticalMissingEvidence
exceptions
harmFactors
allowedCandidateActions
automationEligibility
effectiveFrom
```

Quy ước:

- `ruleVersion` của bản đầu là `1.0.0-proposed.1`;
- `effectiveFrom` là `TBD_ACTIVATION`, không được tự suy từ ngày viết;
- `ruleType` gồm `VIOLATION`, `NON_VIOLATION_SIGNAL`, `ROUTING_CONTROL`,
  `EVIDENCE_CONTROL`;
- chỉ rule `VIOLATION` đủ evidence mới có thể tạo substantiated policy finding;
- control rule chỉ thay đổi cách route/review, không tự cấp punitive action.

## 4. Canonical registry

| Rule ID | Family | Type | Tên ngắn | Target khái quát | Automation |
|---|---|---|---|---|---|
| `CSR.SAF.001` | `PF-SAFETY` | VIOLATION | Credible violence threat | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.SAF.002` | `PF-SAFETY` | VIOLATION | Violent or exploitative content | BLOG, COMMENT | A0 |
| `CSR.SAF.003` | `PF-SAFETY` | VIOLATION | Self-harm encouragement or facilitation | BLOG, COMMENT, USER | A0 |
| `CSR.HATE.001` | `PF-HATE` | VIOLATION | Hateful attack on protected class | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.HAR.001` | `PF-HARASSMENT` | VIOLATION | Targeted harassment | BLOG, COMMENT, USER | A0 |
| `CSR.HAR.002` | `PF-HARASSMENT` | VIOLATION | Bullying or unwanted contact | BLOG, COMMENT, USER | A0 |
| `CSR.SEX.001` | `PF-SEXUAL` | VIOLATION | Nudity or explicit sexual activity | BLOG, COMMENT | A0 |
| `CSR.SEX.002` | `PF-SEXUAL` | VIOLATION | Sexualized or sensitive content | BLOG, COMMENT | A0 |
| `CSR.INT.001` | `PF-INTEGRITY` | VIOLATION | Impersonation | USER, CAFE_PAGE, BLOG | A0 |
| `CSR.INT.002` | `PF-INTEGRITY` | VIOLATION | Materially deceptive representation | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.INT.003` | `PF-INTEGRITY` | VIOLATION | Scam or fraud | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.SPAM.001` | `PF-SPAM` | VIOLATION | Spam or platform manipulation | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.PRIV.001` | `PF-PRIVACY` | VIOLATION | Unauthorized personal-data disclosure | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.IP.001` | `PF-IP` | VIOLATION | Copyright infringement claim | BLOG, COMMENT, CAFE_PAGE | A0 |
| `CSR.IP.002` | `PF-IP` | VIOLATION | Other intellectual-property claim | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.COM.001` | `PF-COMMERCE` | VIOLATION | Restricted goods or services | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.REL.001` | `PF-RELEVANCE` | NON_VIOLATION_SIGNAL | Off-topic or irrelevant | BLOG, COMMENT, CAFE_PAGE | A0 |
| `CSR.ROUTE.001` | CONTROL-ROUTING | NON_VIOLATION_SIGNAL | Dislike/preference signal | Tất cả | A0 |
| `CSR.ROUTE.002` | CONTROL-ROUTING | ROUTING_CONTROL | Other report reason | Tất cả | A0 |
| `CSR.ROUTE.003` | CONTROL-ROUTING | ROUTING_CONTROL | Unspecified inappropriate image | BLOG, COMMENT, USER, CAFE_PAGE | A0 |
| `CSR.EVD.001` | CONTROL-EVIDENCE | EVIDENCE_CONTROL | Critical evidence missing | Tất cả | A0 |
| `CSR.EVD.002` | CONTROL-EVIDENCE | EVIDENCE_CONTROL | Material evidence conflict | Tất cả | A0 |
| `CSR.EVD.003` | CONTROL-EVIDENCE | EVIDENCE_CONTROL | Evidence unreadable or unsupported | Tất cả | A0 |
| `CSR.EVD.004` | CONTROL-EVIDENCE | EVIDENCE_CONTROL | Sufficient non-substantiation | Tất cả | A0 |

Registry có 24 Rule ID. AI MUST chỉ trả Rule ID nằm trong catalog version được Backend pin.
Unknown/free-form Rule ID phải bị semantic validation từ chối hoặc chuyển operationally valid
recommendation thành `NEEDS_MANUAL_REVIEW + NO_ACTION` theo contract được chốt ở Sprint 1.

## 5. Điều kiện chung để substantiated finding

Một violation rule chỉ được xem là substantiated khi đồng thời:

1. target và snapshot hợp lệ;
2. có observation trực tiếp hoặc evidence độc lập hỗ trợ từng tiêu chí bắt buộc;
3. evidence có provenance và có thể truy ngược;
4. counter-evidence liên quan đã được xét;
5. không còn critical missing evidence của chính rule;
6. exception liên quan đã được loại trừ hoặc chuyển human review;
7. sufficiency được đánh giá riêng cho decision và candidate action;
8. finding ghi đúng `ruleId`, `ruleVersion`, evidence references và uncertainty.

Reporter description, report count, reason severity, AI explanation hoặc recommendation cũ
MUST NOT tự mình đáp ứng các điều kiện trên.

## 6. Decision effect chung

| Kết quả đánh giá | Decision recommendation | Candidate action |
|---|---|---|
| Ít nhất một violation rule đủ burden cho decision | `RESOLVE` | Theo target/action matrix; A0 nên human quyết định |
| Evidence đủ để kết luận các candidate rule không substantiated | `REJECT` | `KEEP_VISIBLE` hoặc `KEEP_ACTIVE` |
| Thiếu/mâu thuẫn/unreadable evidence; exception hoặc rule applicability chưa rõ | `NEEDS_MANUAL_REVIEW` | `NO_ACTION` |
| Provider/schema/network failure | Không tạo content decision | Operational error |

`assessmentConfidenceScore` không thay đổi trực tiếp bảng này.

## 7. Candidate action và authority

- Catalog chỉ mô tả action candidate hợp lệ về mặt loại target.
- Bản catalog này không phê duyệt automation cho bất kỳ rule nào.
- `REMOVE`, `SUSPEND_USER`, `SUSPEND_PAGE` luôn cần human ở baseline hiện tại.
- `HIDE` vẫn human-by-default; exact allowlist thuộc G0-10.
- Rule finding không được bypass pre-mutation revalidation.
- Rule tác động USER/CAFE_PAGE phải có evidence về chính actor/entity; một content item đơn lẻ
  không mặc định đủ để suspend toàn bộ actor/page.

## 8. Version và lifecycle

```text
DRAFT → PROPOSED → APPROVED → ACTIVE → DEPRECATED → RETIRED
```

- `RC-2.0.0-proposed.1` hiện chỉ ở `PROPOSED`.
- Thay criteria/evidence/action ceiling là thay đổi rule version.
- Đổi label không đổi semantics có thể là patch.
- Merge/split/deprecate reason không được làm thay đổi lịch sử report.
- Recommendation phải lưu immutable catalog/rule version đã dùng.

## 9. Những phần chưa chốt

- policy owner và approver;
- exact threshold/calibration;
- exact action allowlist;
- two-person approval;
- target-specific evidence sâu cho BLOG/COMMENT/USER/CAFE_PAGE;
- jurisdiction/legal review cho IP, privacy và restricted commerce;
- effective date;
- mapping migration của 14 legacy free-form rule codes.

Các nội dung trên không được hiểu là đã phê duyệt chỉ vì catalog có bản dự thảo.
