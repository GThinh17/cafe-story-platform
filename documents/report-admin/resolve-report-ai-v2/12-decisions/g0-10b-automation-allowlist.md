# G0-10B — Automation Allowlist for Sprint 1

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-002` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10B` |
| Affected | Auto-apply scheduling, worker execution, Admin UI copy, action authority |
| Current approved theory | `A0 RECOMMEND_ONLY`; destructive actions human-required |

## 2. Vấn đề

Current source có delayed auto-apply và latent capability cho `HIDE`, `REMOVE`,
`SUSPEND_USER`, `SUSPEND_PAGE`. Tuy nhiên V2 chưa triển khai:

- canonical evidence/sufficiency contract;
- calibrated threshold;
- approved target-specific burden;
- complete rollback/appeal workflow;
- representative evaluation dataset;
- production kill-switch/monitoring contract.

Việc một action reversible về kỹ thuật không có nghĩa false positive của action đó vô hại.

## 3. Các phương án

### B1 — Sprint 1 giữ `A0 RECOMMEND_ONLY` — Khuyến nghị

- AI/n8n chỉ tạo recommendation và history.
- Admin review evidence, decision và candidate action.
- Không tự schedule hoặc execute bất kỳ target mutation nào:
  `HIDE`, `REMOVE`, `SUSPEND_USER`, `SUSPEND_PAGE`.
- Không tự đóng `REJECT/RESOLVE` chỉ vì score cao.
- Manual execution vẫn phải qua Backend validation và pre-mutation revalidation.
- Existing pending job khi rollout phải cancel hoặc revalidate theo migration design.
- Code path auto-apply có thể được giữ sau feature flag/default deny để phát triển tương lai,
  nhưng production authority là none.

Điều kiện nâng khỏi A0 trong tương lai:

```text
approved rule/action allowlist
representative evaluation and calibration
evidence sufficiency implemented
pre-mutation guard and idempotency
rollback + kill switch
monitoring by rule/action/error
separate business activation approval
```

### B2 — Cho phép A1 với non-punitive report closure

Có thể tự `REJECT + KEEP_VISIBLE/KEEP_ACTIVE`, không target mutation.

Rủi ro: tự dismiss report là false negative có tác động nghiệp vụ; evidence hiện chưa đủ để
chứng minh valid non-substantiation. Không khuyến nghị cho Sprint 1.

### B3 — Cho phép A2 với `HIDE`

Cho auto-hide content vì có thể rollback.

Rủi ro: false positive vẫn làm mất visibility; current score chưa calibrated, image evidence
có thể unreadable và COMMENT fixture còn thiếu. Không khuyến nghị trước safety/evaluation gate.

## 4. Explicit denylist trong B1

| Operation | Sprint 1 automation |
|---|---|
| Generate AI recommendation | ALLOWED |
| Persist recommendation/history | ALLOWED |
| Notify/show recommendation to Admin | ALLOWED |
| Auto-change report status | DENIED |
| Auto `HIDE` | DENIED |
| Auto `REMOVE` | DENIED |
| Auto `SUSPEND_USER` | DENIED |
| Auto `SUSPEND_PAGE` | DENIED |
| Admin manual action after validation | ALLOWED, subject to G0-10C and rule/action validity |

Default là deny. Bảng này không biến recommendation thành business decision.

## 5. Khuyến nghị

Chọn `B1`.

Đây không phải loại bỏ chức năng AI: giá trị Sprint 1 nằm ở recommendation có evidence,
rule citation, uncertainty và Admin decision support. Automation chỉ nên mở sau khi chứng minh
false-positive risk và operational safeguards.

## 6. Acceptance record đã duyệt

```text
decisionId: BD-002
selectedOption: B1_A0_RECOMMEND_ONLY
autoTargetMutationAllowlist: []
autoReportClosureAllowed: false
manualValidatedActionAllowed: true
futureAutomationRequiresSeparateActivationApproval: true
```

## 7. Kết luận

Phương án `B1_A0_RECOMMEND_ONLY` đã được duyệt cho Sprint 1.

Approval này không xóa code/history auto-apply hiện hữu; cách disable/default-deny,
migration pending job và compatibility được thiết kế tại G0-11 trước khi sửa source.
