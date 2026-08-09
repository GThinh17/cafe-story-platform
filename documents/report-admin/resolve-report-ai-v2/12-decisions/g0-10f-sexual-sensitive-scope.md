# G0-10F — Sexual and Sensitive Content Scope

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-006` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10F` |
| Rules | `CSR.SEX.001`, `CSR.SEX.002` |
| Dependency | B1 A0; C1 risk-tiered quorum |
| Affected | Sexual rule criteria, media evidence, age uncertainty, exceptions |

## 2. Vấn đề

Nếu chỉ dùng reason `NUDITY_OR_SEXUAL_ACTIVITY`, `SEXUAL_CONTENT` hoặc
`INAPPROPRIATE_IMAGE`, AI dễ:

- kết luận từ reason/URL thay vì đọc media;
- trộn explicit sexual activity với non-sexual nudity;
- suy tuổi từ ngoại hình;
- bỏ qua medical/educational/artistic context;
- coi sexualized content và sexual exploitation là cùng mức burden.

CafeStory hiện không có age-gated adult-content product được chứng minh trong scope này.

## 3. Các phương án

### F1 — Conservative public-platform policy with contextual exceptions — Khuyến nghị

#### Không cho phép trong public content

- explicit sexual activity;
- explicit genital nudity;
- sexual solicitation/exploitation;
- non-consensual intimate content;
- sexualized content liên quan người chưa thành niên;
- content mà age là yếu tố critical nhưng evidence cho thấy minor.

#### Sensitive/manual-review scope

- partial/non-explicit nudity;
- sexually suggestive content;
- ambiguous visual/text content;
- age không rõ nhưng age là yếu tố quyết định;
- exception context chưa đủ evidence.

#### Candidate exceptions cần context

- medical/health;
- education/research;
- breastfeeding;
- childbirth;
- documentary/news/public-interest;
- artistic context không nhằm sexual exploitation.

Exception không tự động được approve chỉ vì label “education” hoặc “art”.

### F2 — Cấm mọi nudity và mọi sexual reference

Dễ implement nhưng over-remove health, education, breastfeeding và discussion hợp lệ.
Không khuyến nghị.

### F3 — Cho adult explicit content nếu có label/age gate

Chỉ phù hợp khi có verified-age, age gate, consent, sensitive-media controls và operations
riêng. Các capability này chưa nằm trong Sprint 1. Không khuyến nghị.

## 4. Age and consent guards

- AI MUST NOT suy age chỉ từ face/body/appearance.
- Age self-declaration không tự là verified fact.
- Khi age critical nhưng unknown/conflicting:
  `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- Không dùng confidence cao để override missing age evidence.
- Non-consensual intimate-content signal phải route thêm Privacy rule và restricted human review.
- Evidence nhạy cảm không được copy vào explanation/log; dùng protected reference.
- Exact retention/access control chuyển G0-10L và G0-11.

## 5. Media-readability guard

| Input state | Result |
|---|---|
| Media đã được verified tool đọc và có provenance | Evaluate applicable sexual rule |
| Chỉ có URL/image reason | Không tạo violation finding |
| Media fetch/OCR/vision failed và media là critical | `CSR.EVD.003` + manual review |
| Text context đủ cho solicitation/exploitation finding | Evaluate text evidence; không bịa visual observation |
| Media/context conflict | `CSR.EVD.002` + manual review |

## 6. Decision/action boundary

- AI chỉ recommendation theo B1.
- `HIDE` có thể do một Admin quyết định sau evidence/revalidation.
- `REMOVE` cần two-person approval theo C1.
- Actor/page suspension không được suy từ một sexual-content item; cần rule/evidence riêng.
- Urgent safety review không đồng nghĩa tự động punitive action.

## 7. Khuyến nghị

Chọn `F1`.

F1 phù hợp nền tảng công khai và không yêu cầu giả định rằng CafeStory đã có adult-content
infrastructure. Nó vẫn giữ exception hợp lệ nhưng buộc evidence/context.

## 8. Acceptance record đã duyệt

```text
decisionId: BD-006
selectedOption: F1_CONSERVATIVE_PUBLIC_PLATFORM_WITH_EXCEPTIONS
publicExplicitSexualContentAllowed: false
sexualContentInvolvingMinorAllowed: false
ageInferenceFromAppearanceAloneAllowed: false
unknownCriticalAgeBehavior: NEEDS_MANUAL_REVIEW
urlOrReasonAloneCanSubstantiate: false
contextualExceptionsRequireEvidence: true
adultAgeGatingInSprint1: false
```

## 9. Kết luận

Phương án `F1_CONSERVATIVE_PUBLIC_PLATFORM_WITH_EXCEPTIONS` đã được duyệt.

Exact implementation của protected media reference, age uncertainty và exception review
được thiết kế tại G0-11; không suy rằng runtime hiện đã compliant.
