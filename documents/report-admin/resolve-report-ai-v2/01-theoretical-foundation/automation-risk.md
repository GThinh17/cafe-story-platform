# Automation Risk — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05G` |
| Ngày cập nhật | 2026-07-23 |
| Tài liệu liên quan | `risk-and-harm-model.md`, `human-in-the-loop.md` |

> Tài liệu này thiết lập automation safety principles. Nó chưa thay đổi runtime auto-apply hiện tại và chưa phê duyệt automation allowlist hoặc threshold.

## 2. Automation risk

Automation risk là rủi ro phát sinh thêm khi hệ thống tự thực hiện action:

- ở tốc độ cao;
- trên nhiều report/target;
- không có người kiểm tra từng item;
- sau delay trong khi state có thể thay đổi;
- dựa trên model/prompt/policy có thể drift;
- thông qua retry, concurrency và distributed workflow.

Cùng một action có thể có action risk trung bình khi Admin thực hiện đơn lẻ nhưng automation risk cao khi áp dụng hàng loạt.

```text
Admin HIDE nhầm một comment
→ ảnh hưởng giới hạn

Model tự HIDE nhầm 10.000 comment
→ lỗi được khuếch đại ở quy mô hệ thống
```

## 3. Automation là capability độc lập

AI recommendation không mặc định có quyền automation.

```text
Recommendation quality
≠ Automation eligibility
```

Automation eligibility cần policy, evidence, risk, version, state và operational safeguards riêng.

## 4. Automation levels

| Cấp | Khả năng | Mutation |
|---|---|---|
| `A0 RECOMMEND_ONLY` | Chỉ tạo recommendation | Không |
| `A1 WORKFLOW_AUTOMATION` | Sắp xếp queue, thu thập evidence, cảnh báo | Không target mutation |
| `A2 REVERSIBLE_ENFORCEMENT` | Action có thể rollback, có delay/cancel/revalidation | Có giới hạn |
| `A3 DESTRUCTIVE_CONTENT_ENFORCEMENT` | `REMOVE` hoặc tương đương | Mặc định Human Review |
| `A4 ACCOUNT_RESTRICTION` | `SUSPEND_USER/PAGE` | Bắt buộc Human Review ban đầu |

Baseline an toàn của một capability mới là `A0 RECOMMEND_ONLY`. Nâng automation level cần approval riêng và evidence chất lượng hệ thống.

## 5. Failure modes

### 5.1. Correlated model error

Một systematic model/prompt error có thể tạo hàng nghìn recommendation sai cùng kiểu.

### 5.2. Stale state

Target/report/policy thay đổi giữa recommendation, scheduling và execution.

### 5.3. Duplicate execution

Retry, timeout hoặc hai worker cùng claim job có thể tạo mutation trùng.

### 5.4. Bulk validation bypass

Batch-level check pass nhưng một hoặc nhiều item không eligible.

### 5.5. Version drift

Job được schedule bởi policy/model/prompt version A nhưng execute khi version B đã active hoặc A bị thu hồi.

### 5.6. Evidence degradation

Evidence reference hết hạn, bị xóa hoặc không còn current trước execution.

### 5.7. Prompt injection amplification

Một target content độc hại có thể ảnh hưởng nhiều decision nếu prompt/data boundary yếu.

### 5.8. Rollback illusion

Action được gọi là reversible nhưng side effect thực tế không phục hồi đầy đủ.

### 5.9. Observability gap

System có thể trả HTTP success trong khi nhiều item `FAILED` hoặc `SKIPPED`.

### 5.10. Cancel/execute race

Cancel đến cùng thời điểm worker bắt đầu mutation.

### 5.11. Model/provider drift

Provider/model behavior thay đổi làm quality giảm nhưng workflow vẫn tiếp tục auto-apply.

### 5.12. Policy revocation lag

Rule/action bị thu hồi nhưng queued job vẫn dùng policy cũ.

## 6. Automation eligibility gate

Một recommendation chỉ được xem xét automation khi đồng thời:

1. Input/output schema hợp lệ.
2. Semantic validation pass.
3. Policy, rule, model, prompt và workflow version nằm trong allowlist.
4. Evidence sufficiency đáp ứng candidate action.
5. Không có critical uncertainty.
6. Candidate action nằm trong automation allowlist.
7. Action risk đáp ứng policy.
8. Report và target còn eligible.
9. Snapshot/version còn current.
10. Request/job có idempotency và correlation key.
11. Không có concurrent action xung đột.
12. Required audit metadata đầy đủ.

Thiếu một mandatory condition:

```text
Automation blocked
→ NO_ACTION hoặc SKIPPED
→ không fallback sang action khác
```

## 7. Schedule và execution là hai trust boundaries

```text
Recommendation
→ schedule eligibility check
→ delay/cancel window
→ atomic job claim
→ execution-time revalidation
→ mutation
→ outcome audit
```

Eligibility tại thời điểm schedule không bảo đảm eligibility tại thời điểm execute.

Execution-time revalidation phải kiểm tra lại tối thiểu:

- report state;
- target existence/state/version;
- policy/rule/action validity;
- actor/system permission;
- idempotency state;
- conflicting mutation;
- kill-switch state;
- snapshot freshness.

Nếu state đã thay đổi:

```text
outcome: SKIPPED
reason: TARGET_CHANGED | REPORT_NOT_ELIGIBLE | POLICY_REVOKED
```

`SKIPPED` là safety outcome hợp lệ, không được che thành success hoặc retry mù.

## 8. Retry và idempotency

- mỗi logical recommendation request có idempotency key;
- mỗi scheduled action có execution identity riêng;
- retry cùng identity không tạo thêm mutation;
- chỉ retry transient operational failure theo policy;
- không retry schema, policy, permission hoặc semantic conflict;
- timeout phải kiểm tra execution state trước khi retry;
- result phải phân biệt `SUCCEEDED`, `FAILED`, `SKIPPED`, `CANCELLED`;
- raw provider retry và Backend mutation retry phải có correlation nhưng không nhập nhằng identity.

## 9. Concurrency

Worker claim phải có cơ chế chống hai executor cùng xử lý một job.

Trước mutation phải kiểm tra:

- job vẫn ở state executable;
- lock/lease hợp lệ;
- không có terminal outcome;
- cancel chưa thắng race;
- target state chưa bị action khác thay đổi.

State transition và lock mechanism cụ thể thuộc system design, không được giả định trong tài liệu này.

## 10. Bulk automation

Bulk không được giảm safety requirement.

Mỗi item trong batch phải có:

- eligibility riêng;
- evidence sufficiency riêng;
- uncertainty riêng;
- candidate action risk riêng;
- idempotency/execution identity riêng;
- revalidation riêng;
- outcome và reason riêng;
- audit record riêng.

Aggregate result phải phân biệt:

```text
Selected: 10
Succeeded: 3
Skipped: 5
Failed: 2
```

HTTP 200 hoặc việc nhận batch thành công không đồng nghĩa mọi item đã được xử lý thành công.

### 10.1. Bulk partial failure

Partial failure:

- không được rollback item đã thành công một cách tự động nếu policy không định nghĩa transaction semantics;
- không được retry toàn batch mù;
- phải retry đúng item transient-failed;
- phải giữ terminal outcome của item khác;
- phải hiển thị per-item reason cho Admin.

## 11. Fail-safe behavior

Khi automation không chắc chắn:

- fail closed đối với mutation;
- giữ recommendation cho Admin review khi phù hợp;
- không tự đổi sang action khác;
- không dùng legacy `riskScore` làm fallback;
- không tự `REJECT` khi evidence thiếu;
- không retry business/semantic error;
- không che partial failure;
- cảnh báo khi error/drift vượt operational threshold.

Fail closed không có nghĩa nuốt lỗi. System phải trả error/outcome có cấu trúc và audit được.

## 12. Cancel, kill switch và rollback

### 12.1. Cancel

Cancel phải xác định rõ:

- job state nào còn cancel được;
- kết quả khi cancel/execute race;
- actor có quyền cancel;
- terminal state và audit event;
- UI có phản ánh outcome thực tế hay không.

### 12.2. Kill switch

Kill switch cần có scope:

- toàn bộ auto-apply;
- từng action;
- từng policy/rule;
- model/prompt/workflow version;
- target type.

Khi bật kill switch phải xác định cách xử lý job mới, job scheduled và job đang execute.

### 12.3. Rollback

Rollback strategy cần biết:

- action nào phục hồi được;
- batch/job nào bị ảnh hưởng;
- model/policy/version tạo action;
- evidence/recommendation gốc;
- side effect nào không thể hoàn tác hoàn toàn;
- ai có quyền thực hiện rollback.

## 13. Monitoring và model-change gate

Theo dõi tối thiểu:

- scheduled/executed/skipped/cancelled/failed rate;
- skip/failure reason;
- stale-target rate;
- duplicate-prevention count;
- retry count;
- rollback rate;
- Admin override và appeal overturn;
- outcome theo rule/action/model/policy version;
- false-positive spike;
- bulk partial-failure rate;
- schedule-to-execution latency;
- kill-switch activation;
- model/prompt version distribution.

Model/prompt/workflow version mới không được tự thừa hưởng automation permission nếu chưa vượt model-change gate.

## 14. Quan hệ với runtime hiện tại

Tài liệu chưa kết luận implementation hiện tại compliant hay không. G0-06 phải audit thực tế:

- delayed auto-apply;
- cancel behavior;
- execution-time revalidation;
- per-item bulk eligibility;
- retry/idempotency;
- workflow/model/prompt version;
- audit outcome;
- trạng thái `SKIPPED`;
- current allowlist/threshold;
- concurrency behavior.

Không được dùng target-state theory để mô tả current implementation khi chưa có source/runtime evidence.

## 15. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `AR1` | Automation là capability riêng cần policy approval, không mặc định đi kèm AI recommendation | `APPROVED` |
| `AR2` | Baseline an toàn là `A0 RECOMMEND_ONLY` | `APPROVED` |
| `AR3` | `REMOVE`, `SUSPEND_USER`, `SUSPEND_PAGE` không được automation trong V2 ban đầu | `APPROVED` |
| `AR4` | Automation dùng allowlist, không dùng nguyên tắc cho phép mọi thứ trừ denylist | `APPROVED` |
| `AR5` | Revalidate ngay trước mutation; target/report stale chuyển `SKIPPED` | `APPROVED` |
| `AR6` | Recommendation và mutation phải có idempotency/correlation | `APPROVED` |
| `AR7` | Bulk automation phải validate và báo outcome theo từng item | `APPROVED` |
| `AR8` | Evidence thiếu hoặc critical uncertainty phải block automation | `APPROVED` |
| `AR9` | Policy, prompt, model và workflow version phải được pin/audit | `APPROVED` |
| `AR10` | Hệ thống phải có cancel, kill switch và rollback strategy | `APPROVED` |
| `AR11` | Chỉ retry transient failure; không retry schema/business/semantic error | `APPROVED` |
| `AR12` | HTTP/request success không đồng nghĩa mọi item đã được xử lý thành công | `APPROVED` |

## 16. Giới hạn của tài liệu

Tài liệu chưa chốt:

- automation allowlist;
- numeric thresholds;
- delay duration;
- retry/backoff values;
- lock/lease implementation;
- kill-switch implementation;
- rollback mechanics;
- current-runtime compliance.

Các nội dung này thuộc current-state audit, policy, domain contract, system design và verification gates tiếp theo.

