# Human-in-the-loop — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05F` |
| Ngày cập nhật | 2026-07-23 |
| Tài liệu liên quan | `moderation-model.md`, `risk-and-harm-model.md` |

> Tài liệu này xác lập Human-in-the-loop principles. Nó chưa thay đổi runtime auto-apply hiện tại và chưa chốt SLA, role matrix hoặc automation allowlist.

## 2. Mục tiêu

Human-in-the-loop bảo đảm:

- AI không trở thành người có thẩm quyền cuối cùng;
- Admin được cung cấp evidence thay vì chỉ nhận explanation;
- destructive action có checkpoint phù hợp;
- override và rollback có audit trail;
- Backend tiếp tục bảo vệ invariant sau khi Admin xác nhận;
- feedback của con người được dùng để đánh giá chất lượng hệ thống.

Human review không chỉ là một nút “Approve”. Nó là một quy trình ra quyết định có thông tin, quyền hạn và trách nhiệm rõ ràng.

## 3. Phân chia thẩm quyền

```text
AI model
→ phân tích và tạo recommendation

n8n
→ orchestration theo contract

Backend
→ validate evidence, policy, semantic consistency và quyền hạn

Admin
→ review và chọn decision/action được policy cho phép

Backend
→ revalidate và thực hiện mutation
```

### 3.1. AI/n8n

- không có final decision authority;
- không trực tiếp mutate CafeStory data;
- không tự tạo policy/rule;
- phải trả evidence reference, uncertainty và version metadata.

### 3.2. Backend

- là source of truth cho action eligibility;
- kiểm tra policy, role, state và idempotency;
- chặn action sai contract dù AI hoặc Admin yêu cầu;
- lưu recommendation, decision và mutation audit.

### 3.3. Admin

- review evidence và recommendation;
- chọn decision/action trong quyền được cấp;
- yêu cầu thêm evidence hoặc defer;
- cung cấp override reason khi cần;
- không được bypass policy-invalid action.

## 4. Mandatory Human Review

Human review bắt buộc khi:

- evidence thiếu hoặc mâu thuẫn;
- có critical uncertainty;
- policy/rule chưa rõ hoặc không được hỗ trợ;
- target/snapshot đã thay đổi;
- score, finding, decision và action mâu thuẫn;
- action khó rollback hoặc blast radius lớn;
- action ảnh hưởng toàn user/page;
- Admin muốn chọn action nặng hơn recommendation;
- model/prompt/policy/workflow version không thuộc allowlist;
- semantic validation thất bại;
- system phát hiện potential prompt injection hoặc integrity issue.

Semantic/schema/system failure có thể yêu cầu technical handling thay vì moderation review; hệ thống không được biến failure thành một recommendation giả.

## 5. Default checkpoint theo action

| Action | Default V2 principle |
|---|---|
| `NO_ACTION` | Dùng khi inconclusive hoặc action bị chặn |
| `KEEP_VISIBLE` | Recommendation có thể được Admin review; automation cần policy riêng |
| `KEEP_ACTIVE` | Recommendation có thể được Admin review; automation cần policy riêng |
| `HIDE` | Có thể được xem xét automation sau policy/evaluation gate |
| `REMOVE` | Mặc định bắt buộc Admin xác nhận |
| `SUSPEND_USER` | Bắt buộc Admin xác nhận |
| `SUSPEND_PAGE` | Bắt buộc Admin xác nhận |
| Action ngoài policy | Backend phải chặn |

Các nguyên tắc trên là target state cho V2. Auto-apply runtime hiện tại phải được audit ở G0-06 trước khi đưa ra kết luận về compliance.

## 6. Admin review actions

Audit-event semantics đề xuất:

```text
ACCEPT_RECOMMENDATION
MODIFY_DECISION
MODIFY_ACTION
REQUEST_MORE_EVIDENCE
DEFER_DECISION
REJECT_RECOMMENDATION
```

Đây chưa phải enum Backend chính thức. Domain contract sẽ quyết định tên và schema cuối.

### 6.1. Accept recommendation

Admin đồng ý recommendation nhưng Backend vẫn phải revalidate trước mutation.

### 6.2. Modify decision/action

Admin chọn kết quả khác trong phạm vi policy và role. Thay đổi phải được lưu như decision mới, không ghi đè recommendation cũ.

### 6.3. Request more evidence

Admin xác định missing evidence cụ thể. Hệ thống phải giữ trạng thái pending phù hợp, không tự chuyển thành `REJECT`.

### 6.4. Defer

Admin hoãn quyết định khi cần specialist, policy clarification hoặc dữ liệu khác. Defer cần reason và ownership để tránh report treo vô thời hạn.

## 7. Override governance

Khi decision/action cuối khác recommendation:

- giữ nguyên recommendation ban đầu;
- lưu final decision/action;
- lưu structured reason code;
- lưu optional free-text comment nếu cần;
- lưu actor, role và timestamp;
- lưu evidence/policy version tại thời điểm override;
- không ghi đè hoặc xóa history;
- đưa vào feedback/evaluation dataset theo privacy policy.

### 7.1. Override không phải bypass

Nếu action:

- bị policy cấm;
- vượt role/permission;
- không đạt mandatory precondition;
- target không còn eligible;
- vi phạm state transition;

Backend phải block. Việc nhập lý do không biến action không hợp lệ thành hợp lệ.

## 8. UI và automation bias

Admin có thể bị anchoring hoặc automation bias do:

- confidence score cao;
- explanation trôi chảy;
- màu severity nổi bật;
- số lượng report lớn;
- recommendation được đặt trước evidence;
- model được trình bày như nguồn thẩm quyền.

UI phải:

- phân biệt reporter claim với verified/system-captured evidence;
- hiển thị evidence reference và provenance;
- hiển thị counter-evidence;
- hiển thị missing evidence và uncertainty;
- giải thích semantics/source của score;
- cảnh báo confidence chưa calibration;
- hiển thị policy/rule/version;
- không che target content phía sau recommendation;
- hiển thị impact/reversibility của action;
- không dùng reporter severity như verdict.

### 8.1. Evaluation chống automation bias

Có thể sử dụng trên evaluation sample:

- blinded human review trước khi thấy AI recommendation;
- so sánh decision trước/sau AI;
- inter-reviewer agreement;
- đo override và appeal overturn;
- kiểm tra theo từng rule/action.

## 9. Revalidation trước mutation

Sau khi Admin xác nhận, Backend vẫn phải kiểm tra:

- report còn eligible;
- target còn tồn tại;
- snapshot/version còn current;
- action còn hợp lệ với policy version;
- actor còn role/permission;
- cùng logical request chưa được thực thi;
- không có concurrent action đã thay đổi state;
- action vẫn đáp ứng burden và safety precondition;
- required audit metadata còn đầy đủ.

Admin approval không bypass runtime validation.

## 10. Post-decision Human-in-the-loop

Human-in-the-loop tiếp tục sau mutation thông qua:

- audit review;
- rollback;
- appeal;
- incident investigation;
- decision correction;
- quality sampling;
- feedback vào evaluation dataset;
- model/policy drift review.

### 10.1. Rollback

Rollback phải xác định:

- action nào có thể hoàn tác;
- dữ liệu nào cần phục hồi;
- side effect nào không thể phục hồi đầy đủ;
- ai có quyền rollback;
- rollback có tạo notification/audit event mới không.

### 10.2. Appeal

Appeal workflow chi tiết chưa được chốt, nhưng quyết định và evidence phải được lưu đủ để có thể xem xét lại.

## 11. Human-review metrics

- AI–Admin agreement rate;
- override rate theo reason;
- time-to-decision;
- request-more-evidence rate;
- defer rate và aging;
- rollback rate;
- appeal overturn rate;
- inter-reviewer agreement;
- outcome theo rule/action/model/policy version;
- dấu hiệu automation bias.

Agreement cao không tự chứng minh AI đúng nếu Admin chỉ làm theo recommendation mà không review evidence.

## 12. Business decisions được hoãn đến G0-10

- two-person approval cho account restriction;
- SLA urgent review;
- role nào được `REMOVE` hoặc `SUSPEND`;
- structured reason code bắt buộc;
- appeal workflow chi tiết;
- automation allowlist;
- workload/manual-review capacity;
- specialist escalation path.

Việc hoãn phải được ghi vào decision log; không được tự chọn default trong code.

## 13. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `HI1` | AI/n8n chỉ recommendation/orchestration, không có final authority | `APPROVED` |
| `HI2` | Backend validate và block action sai policy/quyền hạn | `APPROVED` |
| `HI3` | `REMOVE`, `SUSPEND_USER`, `SUSPEND_PAGE` mặc định bắt buộc Human Review trong V2 ban đầu | `APPROVED` |
| `HI4` | Critical uncertainty hoặc evidence thiếu bắt buộc manual review | `APPROVED` |
| `HI5` | Override giữ recommendation cũ và structured reason; policy-invalid action không được override | `APPROVED` |
| `HI6` | UI phải hiển thị evidence, counter-evidence và missing evidence để giảm automation bias | `APPROVED` |
| `HI7` | Admin approval không bypass revalidation trước mutation | `APPROVED` |
| `HI8` | Rollback, appeal và decision feedback là phần của Human-in-the-loop | `APPROVED` |
| `HI9` | Two-person approval, SLA, role matrix và auto-apply allowlist là business decisions tại G0-10 | `APPROVED` |

## 14. Giới hạn của tài liệu

Tài liệu chưa chốt:

- role/permission matrix;
- SLA;
- reason-code catalog;
- appeal/rollback workflow chi tiết;
- automation allowlist;
- UI wireframe;
- Backend event schema;
- current-runtime compliance.

Các nội dung này thuộc business decision, domain contract, system design và current-state audit gates tiếp theo.

