# Rule Catalog — G0-08

## Trạng thái

- Gate: `G0-08`
- Approval: `APPROVE_G0-08`
- Catalog: `RC-2.0.0-proposed.1`
- Lifecycle: `PROPOSED`
- Automation: `A0 RECOMMEND_ONLY`

## Nội dung

| Tài liệu | Vai trò |
|---|---|
| `rule-catalog-overview.md` | Schema, canonical registry 24 rules/controls, decision và version rules |
| `intake-reason-mapping.md` | Mapping đủ 22 runtime reasons sang candidate rule/control |
| `violence-rules.md` | Violence, exploitation và self-harm |
| `hate-speech-rules.md` | Hateful conduct |
| `harassment-rules.md` | Harassment, bullying và unwanted contact |
| `sexual-content-rules.md` | Nudity, sexual và sensitive content |
| `impersonation-rules.md` | Impersonation và materially deceptive representation |
| `scam-fraud-rules.md` | Scam/fraud |
| `spam-rules.md` | Spam/platform manipulation |
| `privacy-rules.md` | Personal-data disclosure/misuse |
| `intellectual-property-rules.md` | Copyright và IP |
| `restricted-commerce-rules.md` | Restricted goods/services |
| `relevance-and-routing-rules.md` | Dislike, off-topic, other và inappropriate-image routing |
| `insufficient-evidence-rules.md` | Missing/conflicting/unreadable evidence và valid reject guard |
| `rule-traceability-matrix.md` | Boundary marker; nội dung đầy đủ để G0-09 |

## Điều G0-08 đã chốt ở mức đề xuất

- AI chỉ được chọn Rule ID từ catalog version Backend pin.
- Reporter reason chỉ là routing hint.
- Mỗi finding cần evidence reference, counter-evidence và sufficiency riêng.
- Thiếu evidence critical, evidence conflict hoặc media unreadable dẫn tới
  `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- `REJECT` chỉ hợp lệ khi evidence đủ để kết luận candidate rule không substantiated.
- Tất cả rule/control ở baseline A0; catalog không cấp quyền auto-apply.

## Điều chưa chốt

Catalog chưa business-adopt/activate và chưa chốt threshold, owner, legal lists,
target-specific deep evidence, exact action allowlist hoặc migration.
