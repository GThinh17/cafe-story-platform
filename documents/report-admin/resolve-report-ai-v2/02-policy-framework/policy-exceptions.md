# Policy Exceptions

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Version draft | `PF-2.0.0-proposed.1` |

## 2. Nguyên tắc

Exception là rule có scope hẹp và approval rõ, không phải cách bỏ qua policy khi model/Admin không thích kết quả.

Không có undocumented exception.

## 3. Exception categories

| Category | Ý nghĩa |
|---|---|
| `CONTEXTUAL_EXCEPTION` | Context như educational, documentary, reporting, satire, self-disclosure hoặc consent có thể thay đổi finding |
| `AUTHORIZED_USE` | Sử dụng được chủ thể/quyền sở hữu cho phép |
| `PUBLIC_INTEREST_REVIEW` | Context public-interest cần review đặc biệt |
| `EMERGENCY_SAFETY_HOLD` | Biện pháp tạm thời để giảm imminent harm; không tự là final violation |
| `POLICY_TRANSITION` | Xử lý item/job trong giai đoạn đổi policy version |
| `TECHNICAL_DEGRADATION` | Vision/provider/evidence service unavailable; thường dẫn manual review |

Các category này chỉ là framework. Criteria cụ thể thuộc Rule Catalog.

## 4. Exception record

Mỗi exception MUST có:

```text
exceptionId
exceptionVersion
category
applicableRuleIds
scope/targetTypes
eligibilityCriteria
requiredEvidence
counterEvidence
disallowedActions
approverRole
effectiveFrom
expiresAt
status
rationale
auditReferences
```

## 5. Normative rules

| ID | Policy |
|---|---|
| `PX-001` | Exception MUST có ID/version/status/effective period. |
| `PX-002` | Exception MUST chỉ áp trong scope và rule được khai báo. |
| `PX-003` | Exception MUST NOT bypass evidence sufficiency, authentication, authorization hoặc audit. |
| `PX-004` | AI MAY recommend candidate exception nhưng MUST NOT tự approve. |
| `PX-005` | Admin exception/override MUST có authority và structured reason. |
| `PX-006` | Expired/inactive exception MUST NOT áp cho assessment mới. |
| `PX-007` | Ambiguous exception eligibility MUST dẫn manual review. |
| `PX-008` | Emergency safety hold MUST tách khỏi final violation decision và có expiry/review. |
| `PX-009` | Technical degradation MUST NOT tạo `REJECT`/`RESOLVE`; dùng operational error hoặc manual review. |
| `PX-010` | Exception application MUST được lưu trong same audit record với rule/evidence/version. |

## 6. Context handling

Context có thể:

- thay đổi interpretation của content;
- giảm/tăng harm;
- xác định target/audience;
- tạo counter-evidence;
- kích hoạt manual review.

Context không được biến thành “ngoại lệ mặc định”. Model phải cite evidence cho context claim.

## 7. Emergency handling

Nếu có potential imminent critical harm nhưng final evidence chưa đủ:

1. tạo urgent/immediate review;
2. bảo toàn snapshot/evidence;
3. chỉ dùng temporary hold nếu policy/action đã được business approve;
4. đặt expiry;
5. bắt buộc human review;
6. không ghi final RESOLVE chỉ vì emergency suspicion.

Exact emergency action chưa được G0-07 phê duyệt.

## 8. Override

Admin override record MUST chứa:

- original recommendation;
- original evidence/finding;
- final decision/action;
- override reason code;
- free-text note nếu cần;
- actor/role;
- timestamp;
- policy/rule version;
- approval chain.

Policy-invalid action bị Backend block, không thể override bằng note.

## 9. Chưa chốt

- exception criteria theo policy family;
- approver roles;
- temporary hold action;
- two-person approval;
- expiry/SLA;
- public-interest authority.

Chuyển G0-08/G0-10.
