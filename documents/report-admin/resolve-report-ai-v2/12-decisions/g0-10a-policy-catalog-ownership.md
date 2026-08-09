# G0-10A — Policy and Catalog Ownership

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-001` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10A` |
| Affected | Policy Framework, Rule Catalog, release manifest, change process |
| Không quyết định tại đây | Approval quorum/two-person rule — để G0-10C |

## 2. Vấn đề

Nếu không có owner rõ:

- prompt hoặc n8n có thể vô tình thay policy semantics;
- engineering change có thể tự trở thành business approval;
- không ai chịu trách nhiệm deprecate/rollback rule;
- version `APPROVED/ACTIVE` không có authority hợp lệ.

## 3. Các phương án

### A1 — Role-based ownership, named person required before activation — Khuyến nghị

| Role | Authority |
|---|---|
| Product/Policy Owner | Sở hữu semantics, taxonomy và Rule Catalog; quyết định proposal có đủ để đưa review |
| Business Approver | Chấp thuận risk, action authority, automation và priority |
| Engineering Owner | Thiết kế/implement/verify contract; không tự biến implementation thành policy approval |
| Security/Privacy Reviewer | Review bắt buộc với trust boundary, PII, retention, high-impact action |
| Operations/Release Operator | Chỉ activate immutable manifest đã đủ approval/test; sở hữu rollback execution |

Nguyên tắc:

- tên người thật MAY để `TBD_BEFORE_ACTIVATION` trong giai đoạn hồ sơ;
- một người MAY kiêm nhiều role trong đồ án nhỏ, nhưng audit phải ghi `actedAsRole`;
- AI, n8n và model provider không bao giờ là Policy Owner/Business Approver;
- thay semantics cần change record, impact, version và evidence;
- release operator không được tự sửa policy trong lúc activation;
- exact quorum và trường hợp cần hai người duyệt chuyển G0-10C.

### A2 — Một Project Owner sở hữu toàn bộ

Ưu: đơn giản. Nhược: dễ trộn policy approval, implementation và release; audit yếu.

### A3 — Engineering Owner đồng thời là policy authority mặc định

Ưu: nhanh. Nhược: technical implementation dễ quyết định business semantics ngoài ý muốn.
Không khuyến nghị.

## 4. Khuyến nghị

Chọn `A1`.

Lý do:

1. phù hợp policy change process đã viết;
2. vẫn khả thi cho đồ án nhỏ vì cho phép một người kiêm role;
3. giữ separation of duties ở cấp audit mà chưa ép two-person approval;
4. ngăn prompt/workflow/source tự trở thành policy source of truth;
5. không cần biết tên cá nhân ngay để tiếp tục Detailed Design.

## 5. Acceptance record đã duyệt

```text
decisionId: BD-001
selectedOption: A1_ROLE_BASED_OWNERSHIP
namedPeopleRequiredBy: BEFORE_POLICY_ACTIVATION
multiRoleAllowed: true
actedAsRoleAuditRequired: true
aiOrchestratorAuthority: NONE
approvalQuorum: DEFERRED_G0_10C
```

## 6. Kết luận

Phương án `A1_ROLE_BASED_OWNERSHIP` đã được duyệt.

Approval chưa điền tên người và chưa chốt two-person approval; hai nội dung này giữ đúng
boundary `BEFORE_POLICY_ACTIVATION` và `G0-10C`.
