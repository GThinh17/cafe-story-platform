# Enforcement Action Framework

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Version draft | `PF-2.0.0-proposed.1` |
| Baseline | `A0 RECOMMEND_ONLY` |

## 2. Nguyên tắc

Action selection tách khỏi report decision. Mỗi action phải xét:

- applicable target type;
- policy finding và burden;
- reversibility;
- blast radius;
- action risk;
- authority;
- evidence freshness;
- rollback/appeal capability.

## 3. Canonical action vocabulary

| Action | Target | Tác động | Baseline V2 |
|---|---|---|---|
| `NO_ACTION` | Tất cả | Không mutation | Dùng cho manual/error |
| `KEEP_VISIBLE` | BLOG/COMMENT | Giữ content hiển thị | Legacy adapter → `APPROVE` |
| `HIDE` | BLOG/COMMENT | Hạn chế visibility, có thể rollback | Human by default; automation chưa duyệt |
| `REMOVE` | BLOG/COMMENT | Gỡ content, khó rollback hơn | Human required |
| `KEEP_ACTIVE` | USER/CAFE_PAGE | Giữ hoạt động | Không đồng nghĩa report tự động bị reject |
| `SUSPEND_USER` | USER | Vô hiệu hóa tài khoản | Human required |
| `SUSPEND_PAGE` | CAFE_PAGE | Đình chỉ page | Human required |

## 4. Decision/action compatibility

| Decision | BLOG/COMMENT | USER/CAFE_PAGE |
|---|---|---|
| `NEEDS_MANUAL_REVIEW` | `NO_ACTION` | `NO_ACTION` |
| `REJECT` | `KEEP_VISIBLE` | `KEEP_ACTIVE` |
| `RESOLVE` | `HIDE` hoặc `REMOVE` theo rule/burden | `SUSPEND_USER` hoặc `SUSPEND_PAGE` theo rule/burden |

Compatibility không đồng nghĩa automation eligibility.

## 5. Automation levels

| Level | Ý nghĩa |
|---|---|
| `A0 RECOMMEND_ONLY` | Không auto mutation; Admin quyết định |
| `A1 NON_PUNITIVE` | Chỉ action không trừng phạt được allowlist |
| `A2 REVERSIBLE_LIMITED` | Action reversible, blast radius hạn chế, có guard/rollback |
| `A3 HIGH_IMPACT` | High-impact/destructive; không dùng trong V2 ban đầu |

G0-07 đề xuất active baseline là `A0`. Chuyển level cần business approval, evaluation evidence và versioned rollout.

## 6. Mandatory human review

Các action sau MUST Human Review trong V2 ban đầu:

- `REMOVE`;
- `SUSPEND_USER`;
- `SUSPEND_PAGE`.

Ngoài ra Human Review bắt buộc khi:

- critical evidence missing;
- material evidence conflict;
- exception/precedence unresolved;
- policy/rule/version invalid;
- action risk vượt authority;
- target thay đổi sau snapshot;
- model/provider degraded;
- request có prompt-injection signal chưa xử lý.

## 7. Pre-mutation guard

Backend MUST kiểm tra lại ngay trước mutation:

```text
report status/eligibility
target identity and current state
target snapshot/version/hash
policy/rule/schema versions
evidence availability and sufficiency
decision/action compatibility
action authority and allowlist
idempotency key
existing active/executed action
kill switch
rollback capability
```

Guard fail phải tạo explicit outcome:

- `SKIPPED_STALE_REPORT`;
- `SKIPPED_STALE_TARGET`;
- `BLOCKED_POLICY_VERSION`;
- `BLOCKED_EVIDENCE`;
- `BLOCKED_AUTHORITY`;
- `DUPLICATE_IDEMPOTENT`;
- `FAILED_TRANSIENT`;
- `FAILED_PERMANENT`.

Không gộp tất cả vào free-text `lastError`.

## 8. Normative rules

| ID | Policy |
|---|---|
| `EA-001` | Backend MUST là source of truth cho action eligibility. |
| `EA-002` | Automation MUST dùng allowlist; default deny. |
| `EA-003` | `REMOVE/SUSPEND_USER/SUSPEND_PAGE` MUST NOT auto-apply trong V2 ban đầu. |
| `EA-004` | Evidence missing/critical uncertainty MUST block automation. |
| `EA-005` | Confidence MUST NOT là automation authority duy nhất. |
| `EA-006` | Mỗi action MUST có action risk và required burden riêng. |
| `EA-007` | Pre-mutation revalidation MUST chạy trong execution transaction boundary phù hợp. |
| `EA-008` | Stale target/report MUST chuyển explicit skipped outcome, không silently succeed. |
| `EA-009` | Mutation MUST idempotent và correlation được xuyên suốt. |
| `EA-010` | Hệ thống MUST có cancel trước execution, kill switch và rollback strategy. |
| `EA-011` | Bulk MUST validate từng item và trả recommendation/scheduling/execution outcome riêng. |
| `EA-012` | Admin approval MUST NOT bypass policy-invalid action hoặc revalidation. |

## 9. Bulk outcome model

```text
itemId
recommendationOutcome
schedulingOutcome
executionOutcome
decision
action
blockedReasons[]
retryable
correlationId
```

UI không được cộng recommendation success và auto-apply skipped thành một success metric không giải thích.

## 10. Rollback và appeal

Mỗi punitive action phải định nghĩa:

- rollback operation;
- state cần restore;
- audit record;
- actor/authority;
- time limit nếu có;
- impact tới report/appeal;
- notification behavior.

Exact SLA/role thuộc G0-10; technical design thuộc G0-11.

## 11. Migration behavior

- Legacy `APPROVE` chỉ là adapter của `KEEP_VISIBLE`.
- Existing scheduled jobs tạo trước policy version activation phải được revalidate hoặc cancel.
- Không tự backfill canonical evidence/action risk cho history cũ.
- Active destructive jobs phải được inventory trước rollout; mutation cần approval riêng.

## 12. Chưa chốt

- có cho A1/A2 trong Sprint 1 hay giữ toàn bộ A0;
- HIDE có automation hay không;
- two-person approval;
- exact rollback window;
- kill-switch owner;
- bulk concurrency/rate limit.
