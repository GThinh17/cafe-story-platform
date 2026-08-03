# Explainability and Auditability — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05H` |
| Ngày cập nhật | 2026-07-23 |
| Tài liệu liên quan | `claim-fact-evidence.md`, `human-in-the-loop.md`, `automation-risk.md` |

> Tài liệu này định nghĩa explainability/auditability principles. Nó chưa chốt persistence schema, retention duration, event transport hoặc UI implementation.

## 2. Mục tiêu

Một recommendation chỉ có giá trị vận hành khi người review có thể trả lời:

1. Reporter đang claim điều gì?
2. Evidence nào được sử dụng?
3. AI quan sát trực tiếp điều gì?
4. AI suy luận điều gì?
5. Rule/policy/version nào được áp dụng?
6. Counter-evidence nào đã được xem xét?
7. Evidence nào còn thiếu?
8. Vì sao decision/action này được đề xuất?
9. Vì sao action khác không phù hợp?
10. Vì sao automation được phép hoặc bị block?
11. Ai đã đưa ra decision cuối?
12. Action thực tế nào đã được thực hiện?

Explainability giúp con người hiểu và phản biện. Auditability giúp hệ thống tái dựng sự kiện và chứng minh điều đã xảy ra.

## 3. Explanation không phải evidence

Explanation là derived rationale. Nó không tự chứng minh conclusion và không được dùng làm evidence cho recommendation sau.

```text
Evidence ID
→ Observation
→ Inference
→ Policy finding
→ Decision rationale
```

Nếu explanation không truy được về evidence ID, nó không đạt grounded explainability.

## 4. Không yêu cầu chain-of-thought

Hệ thống không cần yêu cầu, lưu hoặc hiển thị reasoning nội bộ dài của model.

Cần lưu structured rationale có thể kiểm tra:

```json
{
  "finding": "RULE-SCAM-ADVANCE-FEE-001",
  "evidenceIds": ["EV-001"],
  "observation": "Nội dung yêu cầu chuyển tiền trước",
  "conclusion": "Finding được hỗ trợ",
  "counterEvidenceIds": ["EV-004"],
  "missingEvidence": [],
  "decisionRationale": "Evidence đáp ứng rule nhưng chỉ đủ burden cho HIDE"
}
```

Đây là conceptual representation, chưa phải output schema chính thức.

## 5. Các lớp explanation

### 5.1. Admin explanation

Admin cần:

- reporter claim được tách khỏi evidence;
- evidence có thể mở và kiểm tra;
- observation/excerpt;
- counter-evidence;
- missing evidence và uncertainty;
- policy/rule/version;
- score semantics/source;
- proposed decision/action;
- burden/automation blocker;
- action impact và reversibility.

### 5.2. Technical explanation

Engineer/auditor cần:

- schema/model/prompt/policy/workflow version;
- sanitized input và normalized output;
- semantic validation result;
- correlation/idempotency;
- retry và error;
- schedule/cancel/execution state;
- mutation outcome;
- integrity/freshness metadata.

### 5.3. Appeal/user-facing explanation

Người bị ảnh hưởng cần:

- policy reason có thể hiểu được;
- action đã thực hiện;
- thời điểm và phạm vi action;
- cách appeal hoặc yêu cầu review;
- nội dung không làm lộ reporter identity, secret hoặc evidence riêng tư không được phép.

Ba lớp phải dựa trên cùng underlying audit record, không tạo ba phiên bản sự thật mâu thuẫn.

## 6. Explainability quality

### 6.1. Grounded

Mọi finding/rationale quan trọng có evidence reference thực.

### 6.2. Faithful

Explanation phù hợp với normalized decision, final action và validation result.

### 6.3. Complete

Không bỏ qua counter-evidence, critical uncertainty hoặc blocker quan trọng.

### 6.4. Contestable

Admin có thể kiểm tra, yêu cầu thêm evidence, override hoặc appeal.

### 6.5. Comprehensible

Thuật ngữ, score và action được giải thích theo canonical glossary.

### 6.6. Versioned

Có thể xác định policy, rule, model, prompt, schema và workflow version.

### 6.7. Action-specific

Giải thích vì sao burden đủ hoặc chưa đủ cho candidate action cụ thể.

Explanation như “Nội dung có nguy cơ vi phạm cao nên cần xử lý” không đạt vì không chỉ ra evidence, rule, counter-evidence hoặc action reasoning.

## 7. Traceability chain

```text
Final executed action
→ Admin decision hoặc automation eligibility
→ AI recommendation
→ Policy finding
→ Rule/policy version
→ Inference/observation
→ Evidence ID
→ Target snapshot/version
```

Nếu một mắt xích bắt buộc không tồn tại, recommendation không được coi là fully explainable hoặc automation-eligible.

## 8. Audit record tối thiểu

### 8.1. Identity và correlation

- request ID;
- correlation ID;
- idempotency key;
- job/execution ID;
- report ID;
- target ID/type/version.

### 8.2. Input và evidence

- sanitized input snapshot;
- evidence bundle/reference;
- provenance;
- target hash/version;
- captured timestamp;
- missing/counter-evidence.

### 8.3. AI processing

- schema version;
- model/provider/version;
- prompt version;
- policy/rule version;
- workflow version;
- raw output theo retention policy;
- normalized output;
- validation/fallback result.

### 8.4. Human decision

- Admin actor/role;
- displayed recommendation version;
- final decision/action;
- override reason/comment;
- decision timestamp.

### 8.5. Automation/execution

- eligibility result và blockers;
- schedule/delay/cancel;
- claim/revalidation;
- execution outcome/reason;
- mutation result;
- retry/error;
- rollback/appeal event.

## 9. Privacy và security

Auditability không có nghĩa lưu mọi dữ liệu vô hạn.

- không ghi secret/token;
- mask dữ liệu cá nhân không cần thiết;
- raw input/output có access control;
- retention theo data classification;
- appeal view không lộ reporter identity khi policy không cho phép;
- log không trở thành kho dữ liệu nhạy cảm thứ hai;
- hash không thay thế access control;
- sanitized copy không được làm mất evidence reference cần audit;
- quyền truy cập audit phải được ghi lại.

## 10. Append-only và tamper evidence

Không ghi đè lịch sử để chỉ giữ trạng thái cuối. Mỗi thay đổi tạo event mới:

```text
RECOMMENDATION_CREATED
RECOMMENDATION_VALIDATION_FAILED
ADMIN_OVERRIDE_RECORDED
AUTO_APPLY_SCHEDULED
AUTO_APPLY_CANCELLED
EXECUTION_SKIPPED
ACTION_EXECUTED
ACTION_ROLLED_BACK
APPEAL_OPENED
APPEAL_DECIDED
```

Storage có thể dùng append-only log, event hash hoặc immutable retention để phát hiện chỉnh sửa. Implementation thuộc system design.

## 11. Reproducible context

Model có thể không deterministic, nên không hứa rằng chạy lại sẽ cho output giống hệt.

Audit phải tái dựng được:

- input/evidence model đã thấy;
- version/configuration đã dùng;
- raw output model đã trả;
- normalization/validation đã áp dụng;
- recommendation hiển thị cho Admin;
- decision/action cuối;
- mutation outcome.

Đây là reproducible context, không phải guaranteed identical generation.

## 12. Validation và fail-safe

Automation phải bị block hoặc recommendation chuyển manual review khi:

- finding tham chiếu evidence ID không tồn tại;
- rule code/version không hợp lệ;
- explanation nói về evidence ngoài bundle;
- decision/action không khớp rationale;
- counter-evidence quan trọng bị bỏ qua;
- critical missing evidence bị che giấu;
- version metadata bắt buộc bị thiếu;
- audit/correlation chain bị đứt;
- UI hiển thị khác normalized/executed action.

Schema-valid explanation vẫn có thể semantic-invalid.

## 13. Recommendation, decision và action phải tách riêng

Audit record phải giữ ba lớp:

```text
AI recommendation
Admin decision hoặc automation decision
Executed action/outcome
```

Ví dụ:

```text
AI: RESOLVE + HIDE
Admin: RESOLVE + REMOVE
Backend: BLOCKED vì burden chưa đủ
Executed action: NO_ACTION
```

Chỉ hiển thị recommendation hoặc Admin click mà không hiển thị executed outcome sẽ tạo audit sai lệch.

## 14. Metrics

- evidence citation accuracy;
- invalid evidence-reference rate;
- generic-explanation rate;
- missing-counter-evidence rate;
- explanation/decision consistency;
- Admin request-more-evidence rate;
- override do explanation sai;
- audit completeness rate;
- correlation-break rate;
- sanitized-data leakage rate;
- appeal overturn theo rule/model/policy version.

## 15. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `XA1` | AI explanation không phải evidence | `APPROVED` |
| `XA2` | Không yêu cầu/lưu chain-of-thought; dùng structured rationale có evidence reference | `APPROVED` |
| `XA3` | Mỗi policy finding tham chiếu evidence ID và rule/policy version | `APPROVED` |
| `XA4` | Explanation phải hiển thị counter-evidence và missing evidence quan trọng | `APPROVED` |
| `XA5` | Admin, technical và appeal view dùng cùng underlying audit record | `APPROVED` |
| `XA6` | Phải lưu schema/model/prompt/policy/workflow version | `APPROVED` |
| `XA7` | Audit history phải append-only hoặc phát hiện được chỉnh sửa | `APPROVED` |
| `XA8` | Raw input/output phải sanitize, có retention/access control và không lưu secret | `APPROVED` |
| `XA9` | Request có correlation/idempotency xuyên Backend–n8n–model–execution | `APPROVED` |
| `XA10` | Audit phân biệt recommendation, Admin decision và executed action | `APPROVED` |
| `XA11` | Không tuyên bố model output tái tạo giống hệt; chỉ bảo đảm reproducible context | `APPROVED` |
| `XA12` | Citation/rule/version không hợp lệ phải block automation và có thể bắt buộc manual review | `APPROVED` |

## 16. Giới hạn của tài liệu

Tài liệu chưa chốt:

- persistence/event schema;
- retention duration;
- audit access-role matrix;
- UI layout;
- tamper-evidence implementation;
- appeal copy;
- current implementation compliance.

Các nội dung này thuộc current-state audit, policy, system design, operations và business decision gates tiếp theo.

