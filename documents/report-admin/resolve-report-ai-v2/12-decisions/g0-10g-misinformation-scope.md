# G0-10G — Misinformation Scope and Authoritative-source Model

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-007` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10G` |
| Rule | `CSR.INT.002` |
| Affected | `FALSE_INFORMATION`, `FAKE_OR_MISLEADING`, evidence sources, AI prompt |

## 2. Vấn đề

Nếu AI được phép tự xác định mọi nội dung “sai” bằng model knowledge:

- opinion/review cảm nhận có thể bị xử lý như fact;
- thông tin thay đổi theo thời gian có thể dùng knowledge cũ;
- disputed claim có thể bị kết luận một chiều;
- AI explanation hoặc web snippet có thể bị dùng thay evidence;
- rule mở rộng sang chính trị, y tế hoặc xã hội mà chưa có policy/authority.

## 3. Các phương án

### G1 — Narrow material and objectively verifiable claims — Khuyến nghị

Sprint 1 chỉ áp `CSR.INT.002` khi claim:

1. được trích dẫn chính xác từ target snapshot;
2. có thể kiểm chứng khách quan trong evaluated scope;
3. có tính material đối với quyết định/giao dịch/trust;
4. có nguồn phản chứng đủ provenance và freshness;
5. không chỉ là opinion, prediction, satire hoặc unresolved dispute.

Scope ưu tiên:

- platform/system facts mà Backend trực tiếp chứng minh;
- cafe identity, address, opening state hoặc ownership khi có authoritative platform record;
- price/promotion/payment/offer representation có material commercial impact;
- deceptive representation liên quan scam, impersonation hoặc transaction;
- fabricated platform status/badge/authority.

Broad political, medical, scientific hoặc societal misinformation không được AI tự `RESOLVE`
trong Sprint 1. Nếu có harm signal, route manual review/Safety theo rule phù hợp.

### G2 — Broad false-information moderation

Cho AI đánh giá mọi factual claim bằng model/web knowledge. Coverage rộng nhưng evidence,
freshness và authority khó kiểm soát. Không khuyến nghị.

### G3 — Bỏ misinformation rule khỏi Sprint 1

Giảm risk nhưng để trống material deception ngoài scam/impersonation. Quá hẹp nếu đã có
platform facts đáng tin cậy.

## 4. Source authority model

| Tier | Source type | Điều có thể chứng minh |
|---|---|---|
| `AS-1` | Versioned Backend/system record | Field/state mà hệ thống trực tiếp sở hữu |
| `AS-2` | Official/primary source có provenance và timestamp | Claim nằm đúng authority/scope của nguồn |
| `AS-3` | Nhiều independent reliable secondary sources | Supporting/corroboration; vẫn xét conflict |
| `AS-4` | Reporter claim, report count, anonymous allegation | Investigation signal, không phải fact |
| `AS-5` | Model memory, AI explanation, recommendation cũ | Derived signal, không phải evidence |

Không có tier nào là “đúng tuyệt đối”. Evidence vẫn cần relevance, freshness, counter-evidence
và phạm vi nó thực sự chứng minh.

## 5. Claim classification guard

| Claim type | Default |
|---|---|
| Objectively verifiable + material + supported counter-source | Evaluate `CSR.INT.002` |
| Subjective cafe review/taste/service opinion | Không phải misinformation violation |
| Prediction hoặc estimate có uncertainty disclosure | Không mặc định violation |
| Satire/parody rõ context | Candidate exception |
| Disputed claim chưa đủ authority | `NEEDS_MANUAL_REVIEW` |
| Claim đã corrected | Đánh giá current snapshot và audit correction |
| External source unavailable/stale | Missing evidence/manual review |
| Model “biết” claim sai nhưng không có evidence reference | Không tạo finding |

## 6. Action boundary

- AI chỉ recommendation theo B1.
- Candidate action mặc định ở content scope.
- Một false claim đơn lẻ không đủ để suspend USER/CAFE_PAGE.
- `REMOVE` cần C1 two-person approval.
- Scam/impersonation evidence có thể tạo multi-rule findings nhưng mỗi rule có sufficiency riêng.

## 7. Khuyến nghị

Chọn `G1`.

G1 giữ được use case hữu ích của false information trong CafeStory mà không biến model thành
“máy phán sự thật” cho mọi chủ đề.

## 8. Acceptance record đã duyệt

```text
decisionId: BD-007
selectedOption: G1_NARROW_MATERIAL_OBJECTIVELY_VERIFIABLE
sprint1Scope: PLATFORM_AND_MATERIAL_COMMERCIAL_CLAIMS
subjectiveOpinionIsViolation: false
modelMemoryCanServeAsEvidence: false
reporterClaimCanServeAsFact: false
broadPoliticalMedicalScientificResolutionByAI: false
unresolvedDisputeBehavior: NEEDS_MANUAL_REVIEW
sourceAuthorityModelVersion: ASM-1.0.0-proposed.1
```

## 9. Kết luận

Phương án `G1_NARROW_MATERIAL_OBJECTIVELY_VERIFIABLE` đã được duyệt.

Source authority registry và external evidence adapters vẫn phải được thiết kế/kiểm thử
trước runtime activation.
