# G0-10L — Retention, Review/Appeal SLA and Operations Ownership

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-012` |
| Trạng thái | `APPROVED` |
| Approval chat | Ủy quyền hoàn thành toàn bộ `G0-10` theo phương án khuyến nghị, ngày `2026-07-23` |
| Selected option | `L1_MINIMUM_SAFE_OPERATING_BASELINE` |
| Nature | Engineering/operations baseline; không phải legal retention advice hoặc public SLA |

## 2. Retention baseline

| Data class | Default retention | Guard |
|---|---:|---|
| Secret/token/credential | `0 ngày` | Không persist/log; redact trước evidence |
| Raw provider request/response hoặc parse body | Tắt mặc định; tối đa `7 ngày` khi incident flag được duyệt | Sanitize, encrypt, restricted access, auto-delete |
| Sanitized target/evidence snapshot | `90 ngày` sau case close | Access control; legal/privacy hold MAY override |
| Structured recommendation, Admin decision và rationale | `365 ngày` sau case close | Không chứa raw PII; immutable audit linkage |
| Action/rollback/appeal/security audit | `730 ngày` sau final closure | Append-only; minimum necessary fields |

Rules:

- legal/privacy owner có thể rút ngắn hoặc áp hold theo jurisdiction;
- delete/expiry phải có job result và audit metric, không silently fail;
- retention clock và hold không được do AI quyết định;
- raw retention không được dùng thay cho structured evidence design;
- current legacy raw persistence là gap Sprint 1, không được xem là compliant mặc định.

## 3. Internal service objectives

Đây là mục tiêu vận hành nội bộ trước production calibration:

| Queue | Initial triage target | Resolution/review target |
|---|---:|---:|
| `P0` imminent safety hoặc active severe privacy exposure | `1 giờ` | Safeguard/manual owner ngay; review tiếp trong `4 giờ` |
| `P1` high harm/high-impact decision | `8 giờ làm việc` | `2 ngày làm việc` |
| `P2` standard moderation | `2 ngày làm việc` | `5 ngày làm việc` |
| Appeal acknowledgement | `2 ngày làm việc` | — |
| Appeal decision | — | `7 ngày làm việc`, hoặc cập nhật trạng thái/lý do nếu cần legal review |

Không auto-resolve, auto-reject hoặc hạ priority chỉ vì hết SLA. SLA breach tạo Ops alert.

## 4. Approval freshness và rollback

- Content `HIDE/REMOVE` approval hết hiệu lực sau `24 giờ` nếu chưa execute.
- USER/CAFE_PAGE high-impact approval hết hiệu lực sau `4 giờ`.
- Mọi execution vẫn revalidate target/report/policy/evidence ngay trước mutation.
- Approved rollback của reversible content action: mục tiêu execute trong `4 giờ`.
- Approved rollback của remove/suspension: mục tiêu `1 ngày làm việc`, nếu restoration technically
  possible; nếu không phải ghi blocker và user-impact mitigation.
- Appeal/rollback tạo event mới; không xóa hoặc rewrite decision cũ.

## 5. Operations ownership

| Responsibility | Accountable role |
|---|---|
| Policy/rule content và version approval | `Policy Owner` |
| Privacy, retention, legal-sensitive review | `Security/Privacy Reviewer` + legal delegate khi cần |
| Activation, monitoring, kill switch, rollback execution | `Operations/Release Operator` |
| Case decision theo authority/quorum | `Admin Reviewer` |
| Schema/persistence/action guard | `Backend Owner` |
| Prompt/workflow implementation | AI/n8n maintainer; không sở hữu policy |

Named owner/on-call/escalation contact phải có trước activation. Một người có thể kiêm role trong
đồ án, nhưng audit phải ghi `actedAsRole`; quorum C1 vẫn yêu cầu distinct actor khi áp dụng.

## 6. Minimum production gates

- retention/delete job tested;
- raw payload logging disabled hoặc gated;
- monitoring cho backlog, SLA breach, provider failure, override, appeal overturn và rollback;
- kill switch drill và restore test;
- on-call/escalation owner được đặt tên;
- incident runbook và status communication;
- no unresolved P0 security/privacy finding.

## 7. Acceptance record

```text
decisionId: BD-012
selectedOption: L1_MINIMUM_SAFE_OPERATING_BASELINE
rawProviderRetentionDefault: DISABLED
rawProviderRetentionIncidentMaxDays: 7
sanitizedEvidenceRetentionDays: 90
structuredDecisionRetentionDays: 365
actionAuditRetentionDays: 730
slaBreachChangesDecision: false
namedOwnersRequiredBeforeActivation: true
```

## 8. Kết luận

Các con số là baseline để viết Detailed Design và test vận hành. Trước production, Policy Owner
và Security/Privacy Reviewer phải rà soát theo dữ liệu thực, pháp lý áp dụng và năng lực vận hành.
