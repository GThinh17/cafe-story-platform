# G0-10C — Admin Authority, Override and Two-person Approval

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-003` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10C` |
| Dependency | A1 role-based ownership; B1 A0 recommendation-only |
| Affected | Manual Admin decision/action, override, high-impact execution |
| Chưa chốt tại đây | Approval expiry/SLA — để G0-10L |

## 2. Vấn đề

B1 tắt automation nhưng không tự làm manual action an toàn. Cần xác định:

- một Admin được làm gì;
- action nào cần người thứ hai;
- override AI recommendation có được tăng mức xử lý hay không;
- cách ngăn một người tự tạo và tự duyệt high-impact action.

Current system có role `ADMIN` ở route boundary nhưng chưa chứng minh có operation-level
quorum cho `REMOVE/SUSPEND`.

## 3. Các phương án

### C1 — Risk-tiered quorum — Khuyến nghị

| Operation | Authority |
|---|---|
| Xem/chạy AI recommendation | Một authenticated Admin |
| Final `REJECT + KEEP_VISIBLE/KEEP_ACTIVE` | Một Admin, structured decision reason |
| Final `RESOLVE + HIDE` | Một Admin, evidence sufficiency + structured reason + revalidation |
| `REMOVE` | Hai Admin khác actor ID |
| `SUSPEND_USER` | Hai Admin khác actor ID |
| `SUSPEND_PAGE` | Hai Admin khác actor ID |
| Override giảm mức xử lý | Một Admin, structured override reason |
| Override tăng lên `REMOVE/SUSPEND` | Hai Admin và burden tương ứng |
| Policy-invalid decision/action | Backend block; không role nào override |

Two-person flow cho high-impact action:

```text
Decision Maker proposes action
→ Backend validates evidence/rule/action
→ distinct Second Approver reviews same immutable snapshot
→ Backend revalidates current state/version
→ execute once with idempotency
```

Controls:

- `decisionMakerId != secondApproverId`;
- second approver không được sửa proposal rồi duyệt trong cùng record;
- thay decision/action/evidence tạo proposal version mới và invalidates approval cũ;
- recommendation gốc được giữ nguyên;
- execution lưu cả hai actor, role, timestamp và structured reasons;
- stale report/target/policy/evidence block execution;
- nếu chỉ có một Admin account, high-impact action ở trạng thái
  `PENDING_SECOND_APPROVAL`, không hạ guard để demo.

Implementation compatibility:

- Sprint 1 MAY tiếp tục dùng global role `ADMIN` để vào module;
- quorum là operation-level authority với distinct actor IDs;
- có cần thêm RBAC granular role hay không để G0-11 thiết kế, không đổi quyết định C1.

### C2 — Một Admin cho mọi action

Đơn giản và phù hợp demo một account, nhưng một lỗi/compromised account có thể remove hoặc
suspend ngay. Không khuyến nghị.

### C3 — Hai Admin cho mọi punitive action, kể cả `HIDE`

Safety cao hơn nhưng tạo bottleneck cho reversible moderation và tăng scope Sprint 1.
Có thể dùng trong tương lai nếu incident/evaluation cho thấy cần thiết.

## 4. Override rules trong C1

1. Admin MAY chọn khác AI recommendation.
2. Override MUST có structured reason code và note phù hợp.
3. Override không được dùng AI confidence làm authority.
4. Override tăng severity cần evidence burden của action mới.
5. `NEEDS_MANUAL_REVIEW` không ngăn Admin quyết định sau khi đã bổ sung/review evidence.
6. Missing evidence không được đổi thành `REJECT` chỉ để đóng report.
7. Backend MUST block invalid target/action combination kể cả đủ hai người duyệt.

## 5. Khuyến nghị

Chọn `C1`.

Mô hình này bảo vệ high-impact action nhưng không làm mọi report cần hai người. Nó cũng phù hợp
B1: AI chỉ recommendation, authority nằm ở human record và Backend revalidation.

## 6. Acceptance record đã duyệt

```text
decisionId: BD-003
selectedOption: C1_RISK_TIERED_QUORUM
singleAdminActions: [REJECT_KEEP, RESOLVE_HIDE, LOWER_SEVERITY_OVERRIDE]
twoPersonActions: [REMOVE, SUSPEND_USER, SUSPEND_PAGE, ESCALATION_TO_HIGH_IMPACT]
distinctActorRequired: true
policyInvalidOverrideAllowed: false
missingSecondApproverBehavior: PENDING_SECOND_APPROVAL
approvalExpiry: DEFERRED_G0_10L
```

## 7. Kết luận

Phương án `C1_RISK_TIERED_QUORUM` đã được duyệt.

Approval expiry/SLA vẫn để G0-10L; cách lưu proposal/approval và operation-level
authority được thiết kế tại G0-11.
