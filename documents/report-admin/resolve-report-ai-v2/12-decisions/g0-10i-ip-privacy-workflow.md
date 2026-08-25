# G0-10I — IP and Privacy Human/Legal Workflow

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-009` |
| Trạng thái | `APPROVED` |
| Approval chat | Ủy quyền hoàn thành toàn bộ `G0-10` theo phương án khuyến nghị, ngày `2026-07-23` |
| Rules | `CSR.IP.001`, `CSR.IP.002`, `CSR.PRIV.001` |
| Selected option | `I1_DUAL_LANE_HUMAN_LEGAL_TRIAGE` |

## 2. Vấn đề

AI không có authority để xác định quyền sở hữu trí tuệ, consent, public-interest exception,
jurisdiction hoặc tính hợp pháp chỉ từ report và target content. Privacy còn có nguy cơ làm
lộ thêm PII nếu raw evidence được đưa vào prompt, log hoặc explanation.

## 3. Các phương án

### I1 — Dual-lane human/legal triage — Được chọn

- **IP lane:** xác minh claimant authority, loại quyền, work/reference, reported use, license,
  permission, exception và jurisdiction.
- **Privacy lane:** ưu tiên containment khi disclosure đang gây nguy cơ; xác minh data category,
  subject/claimant relationship, consent, public availability, context và exception.
- AI chỉ phân loại, trích xuất sanitized observation, phát hiện evidence thiếu/conflict và tạo
  recommendation có cấu trúc.
- Human reviewer quyết định content action. Legal/Privacy reviewer bắt buộc với disputed ownership,
  unclear consent/exception, cross-jurisdiction hoặc high-impact action.

### I2 — Cho AI quyết định infringement/privacy violation

Không chọn vì model không chứng minh được authority, legal status hoặc consent.

### I3 — Bỏ IP/privacy khỏi hệ thống AI

Không chọn vì AI vẫn hữu ích cho triage, redaction check và evidence checklist.

## 4. Intake và authority

Claim tối thiểu cần:

```text
claimantRole
claimantAuthorityReference
rightOrDataCategory
reportedTargetReference
jurisdictionIfKnown
requestedRemedy
supportingEvidenceReferences[]
```

Rules:

- reporter claim và claimant declaration là investigation input, không phải violation evidence;
- không yêu cầu hoặc lưu government ID trong AI payload;
- evidence nhạy cảm dùng reference có access control, không copy vào explanation;
- thiếu claimant authority, consent, ownership hoặc exception material
  → `NEEDS_MANUAL_REVIEW + NO_ACTION`;
- unresolved legal dispute không được đổi thành `REJECT`;
- urgent exposed credential, precise location hoặc sensitive identifier được route Privacy/Safety
  reviewer để cân nhắc temporary human containment, không auto mutation.

## 5. Decision/action boundary

- Automation giữ `A0 RECOMMEND_ONLY`.
- AI không tuyên bố “illegal”, “copyright owner” hoặc “consent invalid”.
- Content `HIDE/REMOVE` cần human decision; `REMOVE` tuân quorum C1.
- Một IP/privacy finding ở một content không đủ để suspend USER/CAFE_PAGE.
- Counter-notice, appeal hoặc ownership dispute phải liên kết original case và audit trail.

## 6. Acceptance record

```text
decisionId: BD-009
selectedOption: I1_DUAL_LANE_HUMAN_LEGAL_TRIAGE
aiMayMakeLegalConclusion: false
aiMayMutateTarget: false
claimantDeclarationIsEvidence: false
missingAuthorityBehavior: NEEDS_MANUAL_REVIEW
sensitiveEvidenceInExplanation: false
disputedCaseOwner: HUMAN_LEGAL_PRIVACY_REVIEW
```

## 7. Kết luận

G0-10 chỉ chốt authority và route. Form intake, secure evidence store, redaction, notification,
counter-notice và jurisdiction adapter phải được thiết kế/kiểm thử ở bước sau.
