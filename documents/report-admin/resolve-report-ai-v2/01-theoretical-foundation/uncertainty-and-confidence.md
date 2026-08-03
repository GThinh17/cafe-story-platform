# Uncertainty and Confidence — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05D` |
| Ngày cập nhật | 2026-07-23 |
| Tài liệu liên quan | `evidence-hierarchy.md`, `12-decisions/glossary.md` |

> Tài liệu này định nghĩa semantics và safety principles. Nó chưa chốt threshold bằng số, calibration method hoặc auto-apply policy.

## 2. Mục tiêu

Mô hình uncertainty/confidence phải ngăn các lỗi phổ biến:

- đồng nhất confidence với khả năng target vi phạm;
- dùng confidence cao để che thiếu evidence;
- trình bày model-reported score như xác suất đã calibration;
- biến timeout/schema error thành recommendation confidence thấp;
- dùng một score duy nhất để quyết định auto-apply.

## 3. Hai trục độc lập

### 3.1. Assessment confidence

`assessmentConfidenceScore` biểu diễn mức hệ thống tin rằng recommendation hiện tại phù hợp với evidence và giới hạn đã biết.

```text
assessmentConfidenceScore: 0..100
```

Confidence không phải:

- accuracy đã đo;
- evidence quality;
- evidence sufficiency;
- probability target vi phạm;
- safety của target action.

### 3.2. Violation likelihood

`violationLikelihoodScore` biểu diễn mức evidence đang nghiêng về khả năng target vi phạm policy.

```text
violationLikelihoodScore: 0..100
```

Tên `violationProbability` chỉ được dùng trong tương lai nếu score đã được calibration phù hợp.

### 3.3. Ví dụ kết hợp

| Tình huống | Recommendation | Violation likelihood | Assessment confidence |
|---|---|---:|---:|
| Context rõ là bài cảnh báo scam | `REJECT` | 5 | 95 |
| Hành vi vi phạm trực tiếp, rule rõ | `RESOLVE` | 95 | 93 |
| Không đọc được ảnh là nội dung chính | `NEEDS_MANUAL_REVIEW` | Chưa xác định | 95 |
| Câu chữ mơ hồ, evidence nghiêng về vi phạm | `NEEDS_MANUAL_REVIEW` | 70 | 35 |

Confidence cao có thể có ở cả `RESOLVE`, `REJECT` và `NEEDS_MANUAL_REVIEW`. Nó phải được đọc cùng đối tượng recommendation mà score đang mô tả.

## 4. Confidence luôn có điều kiện

Confidence được hiểu là:

```text
Mức tin cậy của assessment
trong điều kiện evidence bundle hiện có,
target snapshot hiện có,
policy/rule version hiện có,
và khả năng của model/tool hiện tại.
```

Confidence cao không chứng minh:

- evidence bundle đầy đủ;
- model đúng;
- target chưa thay đổi;
- burden of proof đã đạt;
- action tương xứng;
- auto-apply an toàn.

Điều kiện tạo score phải được lưu trong audit context để tránh score bị sử dụng ngoài bối cảnh.

## 5. Taxonomy của uncertainty

| Loại | Ý nghĩa | Ví dụ |
|---|---|---|
| `DATA_UNCERTAINTY` | Thiếu hoặc không truy xuất được dữ liệu | Thiếu ảnh, parent comment, history |
| `SEMANTIC_AMBIGUITY` | Nhiều cách hiểu hợp lý | Châm biếm, trích dẫn, từ đa nghĩa |
| `CONTEXT_UNCERTAINTY` | Thiếu bối cảnh làm thay đổi meaning | Không rõ đang cảnh báo hay thực hiện scam |
| `POLICY_UNCERTAINTY` | Policy/rule chồng lấn hoặc chưa bao phủ | Một hành vi phù hợp nhiều rule trái nhau |
| `TEMPORAL_UNCERTAINTY` | Snapshot có thể stale | Target đã chỉnh sửa sau capture |
| `MODEL_UNCERTAINTY` | Model không ổn định hoặc reasoning yếu | Re-run cho kết luận khác nhau |
| `SOURCE_UNCERTAINTY` | Provenance/integrity không đủ | Evidence không rõ nguồn hoặc bị cắt |
| `OPERATIONAL_FAILURE` | Hệ thống/provider không hoàn thành contract | Timeout, rate limit, invalid schema |

`OPERATIONAL_FAILURE` không phải content uncertainty. Nó phải đi theo error contract, không được biến thành một recommendation giả.

## 6. Structured uncertainty

Uncertainty phải được biểu diễn có cấu trúc thay vì chỉ ẩn trong một score:

```json
{
  "uncertaintyFactors": [
    {
      "type": "CONTEXT_UNCERTAINTY",
      "description": "Thiếu parent comment",
      "severity": "CRITICAL",
      "affectedFindings": ["RULE-HARASSMENT-001"],
      "resolvableBy": "LOAD_PARENT_COMMENT"
    }
  ]
}
```

Đây là conceptual representation. Field/enum chính thức thuộc AI contract.

Mỗi uncertainty factor nên chỉ rõ:

- type;
- description;
- severity;
- evidence/finding bị ảnh hưởng;
- có thể giải quyết hay không;
- dữ liệu hoặc human action cần thiết.

## 7. Critical uncertainty

Critical uncertainty là uncertainty làm mất evidence hoặc context cốt lõi cần cho burden of proof.

```text
Critical uncertainty
→ evidenceSufficiency != SUFFICIENT
→ burden of proof không đạt
→ NEEDS_MANUAL_REVIEW + NO_ACTION
```

Ví dụ:

- media chứa nội dung chính nhưng pipeline không đọc được;
- thiếu parent thread quyết định ý nghĩa comment;
- target thay đổi sau snapshot;
- policy version không hỗ trợ rule;
- evidence reference không còn truy xuất được.

Assessment confidence cao không được override critical uncertainty.

## 8. Confidence source và calibration status

Confidence cần ghi rõ nguồn:

```text
confidenceSource:
MODEL_REPORTED
SYSTEM_DERIVED
CALIBRATED
```

### 8.1. Model-reported

`MODEL_REPORTED` là score model tự đánh giá. Nó phải được xem là uncalibrated cho đến khi có evaluation evidence.

### 8.2. System-derived

`SYSTEM_DERIVED` được tính từ deterministic quality gate hoặc rule rõ ràng. Công thức và input phải được version/audit.

### 8.3. Calibrated

`CALIBRATED` chỉ được sử dụng khi:

- có evaluation dataset phù hợp;
- có nhãn human-reviewed;
- calibration được đo theo rule/target/action;
- model/prompt/policy version trùng với version được đánh giá;
- calibration drift được theo dõi.

Không được gọi score là calibrated chỉ vì đã normalize về `0..100`.

## 9. Semantic consistency

Backend phải phát hiện tổ hợp mâu thuẫn, ví dụ:

- `RESOLVE` nhưng violation likelihood rất thấp;
- `REJECT` nhưng violation likelihood rất cao;
- destructive action khi evidence sufficiency không đạt;
- critical uncertainty nhưng action khác `NO_ACTION`;
- model báo confidence cao nhưng không có evidence reference;
- score đúng range nhưng rule/finding không hỗ trợ decision;
- `CALIBRATED` nhưng thiếu calibration version.

Tổ hợp sau có thể hợp lệ:

```text
NEEDS_MANUAL_REVIEW + assessment confidence cao
```

Hệ thống có thể rất chắc chắn rằng evidence chưa đủ để kết luận.

## 10. Quan hệ với evidence quality và sufficiency

Các trục phải được giữ riêng:

```text
assessmentConfidenceScore
violationLikelihoodScore
evidenceQualityScore
evidenceSufficiency
harmSeverityScore
actionRiskScore
```

Ví dụ:

```json
{
  "assessmentConfidenceScore": 92,
  "violationLikelihoodScore": null,
  "evidenceQualityScore": 90,
  "evidenceSufficiency": "INSUFFICIENT",
  "decision": "NEEDS_MANUAL_REVIEW",
  "targetAction": "NO_ACTION"
}
```

Evidence item có thể nguyên vẹn nhưng thiếu phần dữ liệu quyết định, dẫn đến quality cao và sufficiency thấp.

## 11. Confidence và automation

Không được dùng rule đơn:

```text
assessmentConfidenceScore >= X → auto-apply
```

Automation gate tối thiểu còn phải xét:

- evidence sufficiency;
- violation likelihood;
- harm severity;
- action risk;
- burden of proof;
- critical uncertainty;
- policy/rule allowlist;
- target freshness;
- model/prompt/policy version;
- revalidation và idempotency;
- Human-in-the-loop policy.

Tài liệu này không chốt ngưỡng `X` và không phê duyệt action auto-apply.

## 12. UI interpretation

UI sau này không nên chỉ hiển thị “Confidence: 90%”. Tối thiểu phải chỉ rõ:

- score đang nói về điều gì;
- source/calibration status;
- decision và violation likelihood;
- evidence sufficiency;
- uncertainty factors quan trọng;
- missing evidence;
- lý do manual review hoặc automation bị chặn.

Ký hiệu `%` không nên dùng nếu score chưa phải calibrated probability.

## 13. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `U1` | Assessment confidence và violation likelihood là hai khái niệm độc lập | `APPROVED` |
| `U2` | Confidence luôn có điều kiện theo evidence, snapshot, policy và model hiện có | `APPROVED` |
| `U3` | Confidence cao không override critical missing evidence hoặc burden chưa đạt | `APPROVED` |
| `U4` | `NEEDS_MANUAL_REVIEW` có thể có assessment confidence cao | `APPROVED` |
| `U5` | Uncertainty phải được trả về có cấu trúc, không chỉ qua một score | `APPROVED` |
| `U6` | Model-reported confidence là uncalibrated cho đến khi có evaluation evidence | `APPROVED` |
| `U7` | Không dùng confidence làm điều kiện duy nhất cho auto-apply | `APPROVED` |
| `U8` | Operational failure phải tách khỏi content uncertainty | `APPROVED` |
| `U9` | Backend phải validate semantic consistency giữa decision, score, evidence và uncertainty | `APPROVED` |

## 14. Giới hạn của tài liệu

Tài liệu chưa chốt:

- threshold score;
- calibration algorithm;
- confidence computation formula;
- UI component chi tiết;
- auto-apply policy;
- target/rule-specific uncertainty behavior.

Các nội dung này thuộc policy, AI contract, system design và verification gates tiếp theo.

