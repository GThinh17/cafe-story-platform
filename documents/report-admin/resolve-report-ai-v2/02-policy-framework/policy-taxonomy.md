# Policy Taxonomy

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Version draft | `PF-2.0.0-proposed.1` |
| Scope | General taxonomy; chưa phải Rule Catalog G0-08 |

## 2. Vấn đề cần giải quyết

Current state đang trộn:

- report reason do user chọn;
- broad category;
- model-generated rule code;
- policy conclusion;
- action.

DB có 22 reasons, source initializer có 9 và có field drift. Vì vậy reason code hiện tại không thể là policy source of truth.

## 3. Taxonomy layers

```text
Reporter Intake Reason
        ↓
Investigation Signal / Claim
        ↓
Policy Family
        ↓
Versioned Policy Rule
        ↓
Policy Finding + Evidence References
        ↓
Report Decision
        ↓
Candidate Target Action
```

| Layer | Mục đích | Source of truth |
|---|---|---|
| Intake reason | Giúp reporter mô tả vấn đề | Versioned intake catalog |
| Policy family | Nhóm rule cùng mục tiêu | Policy Framework |
| Policy rule | Điều kiện violation cụ thể | Rule Catalog G0-08 |
| Finding | Kết quả áp rule lên evidence snapshot | Backend audit record |
| Decision/action | Kết luận report và enforcement | Backend policy engine |

## 4. Proposed policy families

| Family ID | Tên | Scope khái quát |
|---|---|---|
| `PF-SAFETY` | Safety and Physical Harm | Violence, threat, self-harm hoặc nguy cơ thể chất |
| `PF-HATE` | Hateful Conduct | Tấn công/phân biệt dựa trên protected characteristic |
| `PF-HARASSMENT` | Harassment and Bullying | Quấy rối, bắt nạt, unwanted contact, targeted abuse |
| `PF-SEXUAL` | Sexual and Sensitive Content | Nudity, sexual activity, sexualized/sensitive material |
| `PF-INTEGRITY` | Integrity and Authenticity | Scam, fraud, impersonation, misleading behavior |
| `PF-SPAM` | Spam and Platform Abuse | Repetition, unsolicited promotion, manipulation |
| `PF-PRIVACY` | Privacy and Personal Data | Disclosure/misuse of personal or sensitive information |
| `PF-IP` | Intellectual Property | Copyright/trademark/ownership claims |
| `PF-COMMERCE` | Restricted Goods and Commerce | Restricted or prohibited goods/services |
| `PF-RELEVANCE` | Relevance and Quality Signals | Off-topic/low-quality/preference signals; không mặc định là violation |

Family chỉ là namespace. Violation criteria và exception phải nằm trong Rule Catalog.

## 5. Intake reason principles

| ID | Policy |
|---|---|
| `PT-001` | Intake reason MUST được version hóa độc lập với policy rule. |
| `PT-002` | Một reason MAY map tới nhiều candidate policy families. |
| `PT-003` | Reason MUST NOT map trực tiếp tới action. |
| `PT-004` | `DISLIKE_CONTENT` và preference signal MUST NOT tự tạo violation finding. |
| `PT-005` | `OTHER` MUST yêu cầu description và chỉ dùng để route review. |
| `PT-006` | Reason severity hiện tại MUST được coi là `reporterSeverityHint`, không phải official harm severity. |
| `PT-007` | Target applicability MUST nằm trong versioned mapping/rule, không phụ thuộc null `target_type` ngầm hiểu ALL. |
| `PT-008` | Overlapping reason codes MUST được reconcile bằng migration plan; không xóa/merge trước impact review. |

## 6. Rule identity

Mỗi rule tại G0-08 phải có tối thiểu:

```text
ruleId
ruleVersion
policyFamilyId
title
scope
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
status
```

Model MUST chọn từ rule catalog do Backend pin. Model MUST NOT tự phát minh `ruleCode`.

## 7. Multi-rule evaluation

- Một target MAY có nhiều finding.
- Mỗi finding MUST có evidence sufficiency riêng.
- Finding mạnh không tự bù critical missing evidence của finding khác.
- Candidate action cuối phải xét toàn bộ finding, counter-evidence và action risk.
- Nếu các rule dẫn tới action conflict chưa có precedence rõ, decision là manual review.

## 8. Target applicability

Framework hỗ trợ `BLOG`, `COMMENT`, `USER`, `CAFE_PAGE`.

Không giả định một rule áp dụng giống nhau cho mọi target:

- content rule có thể áp BLOG/COMMENT;
- identity/behavior rule có thể áp USER;
- commerce/page representation rule có thể áp CAFE_PAGE;
- cross-target rule phải định nghĩa evidence và action riêng cho từng target.

Chi tiết target-specific để G0-08 hoặc phase sau; G0-07 không tự phát minh criteria.

## 9. Source of truth

Sau migration:

1. Approved versioned Rule Catalog là policy source of truth.
2. Backend pin version và validate rule identity.
3. Database lưu immutable reference/snapshot.
4. n8n/model chỉ consume catalog subset.
5. UI render cùng underlying finding record.
6. Initializer/report reason table không được dùng thay Rule Catalog.

## 10. Current catalog disposition

| Current artifact | Proposed treatment |
|---|---|
| 22 DB reasons | Giữ làm audit input; reconcile tại migration plan |
| 9 source initializer reasons | Không coi là canonical |
| 14 free-form AI rule codes | Legacy data; không dùng làm stable rule ID |
| Legacy severity 1–5 | Giữ migration field, đổi tên semantics |
| Reason label snapshot | Giữ để audit user intake tại thời điểm report |

## 11. Chưa chốt

- mapping đầy đủ 22 reason → policy family/rule;
- rule criteria;
- policy owner;
- active/inactive/merge decision cho từng reason;
- target-specific deep implementation.

Các phần này thuộc G0-08 và G0-10.
