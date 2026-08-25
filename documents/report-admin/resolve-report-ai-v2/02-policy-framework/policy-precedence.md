# Policy Precedence

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Version draft | `PF-2.0.0-proposed.1` |

## 2. Mục tiêu

Định nghĩa cách chọn rule/policy khi:

- nhiều policy family cùng áp dụng;
- general và target-specific rule khác nhau;
- exception xung đột rule;
- policy version thay đổi;
- legal/safety constraint cao hơn product rule;
- evidence dẫn tới finding/action khác nhau.

## 3. Authority precedence

Theo thứ tự cao xuống thấp:

1. Formally adopted legal/regulatory obligation có scope/version rõ.
2. Approved emergency safety restriction.
3. Active CafeStory Policy Framework.
4. Active versioned Rule Catalog.
5. Approved target-specific rule/profile.
6. Approved exception record.
7. Intake reason/routing metadata.
8. Model prompt/model suggestion.

Nguồn thấp hơn MUST NOT override nguồn cao hơn.

Model không được tự tạo legal obligation, policy, exception hoặc precedence.

## 4. Rule resolution algorithm

```text
1. Validate policy/rule versions are ACTIVE for assessment time.
2. Filter rules by target type, scope and jurisdiction/profile if applicable.
3. Apply higher-authority constraints.
4. Apply more-specific rule only within higher-level bounds.
5. Evaluate each finding with its own evidence burden.
6. Resolve candidate actions using action burden and authority.
7. If material conflict remains → NEEDS_MANUAL_REVIEW + NO_ACTION.
```

## 5. Normative rules

| ID | Policy |
|---|---|
| `PP-001` | Chỉ policy/rule version `ACTIVE` MAY dùng cho new recommendation. |
| `PP-002` | Recommendation MUST pin policy/rule version tại assessment time. |
| `PP-003` | Target-specific rule MAY cụ thể hóa general rule nhưng MUST NOT hạ burden cho destructive action nếu chưa được duyệt. |
| `PP-004` | Exception MUST NOT override mandatory authority/security/evidence controls. |
| `PP-005` | Intake reason/model suggestion MUST NOT quyết định precedence. |
| `PP-006` | Nhiều finding MAY cùng tồn tại; không ép chọn một family duy nhất nếu evidence hỗ trợ nhiều rule. |
| `PP-007` | Action cuối MUST thỏa mọi higher-authority constraint áp dụng. |
| `PP-008` | Unresolved material conflict MUST block automation. |
| `PP-009` | Policy activation mới MUST NOT reinterpret history cũ; re-evaluation tạo record mới. |
| `PP-010` | Missing/invalid version MUST tạo policy validation failure, không fallback silent sang prompt text. |

## 6. Conflict types

| Conflict | Xử lý |
|---|---|
| Hai rule khác family cùng substantiated | Giữ hai finding; chọn action theo authority/burden |
| General và specific rule khác action | Specific chỉ thắng nếu active/applicable và không trái higher-level guard |
| Current rule và legacy prompt khác nhau | Active Rule Catalog thắng; prompt phải bị reject/update |
| Rule yêu cầu evidence không có | Manual review |
| Exception chưa approved/expired | Không áp exception |
| Policy version không tìm thấy | Operational/policy validation failure |
| Candidate actions incompatible | Human review; không auto chọn action mạnh hơn |

## 7. High-harm conflict

Harm cao không tự ưu tiên punitive action. Khi evidence chưa đủ:

- tăng review urgency;
- bảo toàn evidence;
- có thể áp emergency non-final safety hold nếu policy riêng được approved;
- không ghi final violation chỉ vì harm potential.

## 8. Version transition

- In-flight recommendation dùng version đã pin.
- Job chưa execute phải revalidate với active version và transition policy.
- Breaking policy change MAY cancel/review pending jobs.
- Không auto-migrate final decision cũ.

## 9. Chưa chốt

- legal/jurisdiction profiles thực tế;
- target-specific precedence;
- emergency hold action;
- policy owner có quyền resolve conflict;
- transition window.

Các mục này cần G0-08/G0-10.
