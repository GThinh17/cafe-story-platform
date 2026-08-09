# G0-10D — Threshold and Calibration Acceptance

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-004` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10D` |
| Dependency | B1 A0; canonical score semantics G0-04/G0-07 |
| Affected | AI output, Backend validation, UI display, future activation |

## 2. Vấn đề

Current `confidenceScore` và legacy `riskScore` chưa có calibration evidence và từng được dùng
theo semantics không nhất quán. Database audit còn cho thấy đa số `RESOLVE` nằm dưới legacy
auto threshold, nên không thể dùng giá trị cũ như business truth.

Cần phân biệt:

- **range validation**: score có nằm trong `0..100` không;
- **semantic validation**: score có phù hợp decision/evidence không;
- **business threshold**: score có được dùng để cho phép action/automation không;
- **calibration**: score có tương ứng với observed outcome trên dataset đại diện không.

Range `0..100` là validation dữ liệu, không phải threshold ra quyết định.

## 3. Các phương án

### D1 — Sprint 1 không dùng numeric business threshold — Khuyến nghị

- Giữ riêng:
  - `assessmentConfidenceScore`;
  - `violationLikelihoodScore`;
  - `harmSeverityScore`;
  - `actionRiskScore`.
- Legacy `riskScore` chỉ lưu `legacyRiskScore` cho audit/compatibility.
- Score MUST NOT tự quyết định `RESOLVE`, `REJECT`, action, priority hoặc authority.
- Decision dựa trên versioned rule finding và evidence sufficiency.
- Harm cao có thể tạo structured urgent-review factor, không tự tăng enforcement authority.
- UI ghi rõ score là model estimate chưa calibrated; không dùng màu/copy khiến score giống
  “xác suất vi phạm chắc chắn”.
- Backend vẫn validate type/range và semantic contradiction.

Sprint 1 thu thập evaluation evidence nhưng không kích hoạt threshold.

### D2 — Dùng provisional threshold hiện tại

Giữ ngưỡng legacy để ít thay đổi code.

Không khuyến nghị: semantics mơ hồ, dữ liệu runtime không nhất quán và không có calibration.

### D3 — Bỏ toàn bộ score khỏi V2

Giảm nguy cơ hiểu nhầm nhưng mất observability/evaluation signal. Không cần thiết nếu UI và
contract biểu diễn đúng semantics.

## 4. Calibration gate cho tương lai

Một threshold chỉ MAY được đề xuất sau khi có:

1. dataset version, provenance và representative coverage theo rule/target/action;
2. label process và human disagreement record;
3. metrics theo rule/action, không chỉ accuracy tổng;
4. false-positive/false-negative analysis;
5. calibration curve và calibration error;
6. abstention/manual-review rate;
7. subgroup/context bias review phù hợp dữ liệu;
8. threshold candidate + expected impact;
9. owner, version, rollback và monitoring plan;
10. business activation approval riêng.

Metrics cần báo cáo tối thiểu:

```text
precision / recall / F1 by rule
false-positive rate by candidate action
false-negative rate for high-harm rules
manual-review / abstention rate
semantic-invalid output rate
Brier score or equivalent
expected calibration error or reliability curve
human-AI disagreement
coverage by target, rule and evidence condition
```

G0-10D không tự đặt con số pass/fail vì dataset V2 chưa tồn tại. Exact acceptance values phải
được đề xuất từ evaluation report, review như policy change và version hóa.

## 5. Runtime behavior theo D1

| Tình huống | Behavior |
|---|---|
| Score ngoài `0..100` | Semantic/schema validation failure |
| Confidence cao nhưng critical evidence thiếu | `NEEDS_MANUAL_REVIEW + NO_ACTION` |
| Harm cao nhưng violation evidence thiếu | Urgent manual review, không punitive action tự động |
| Score thấp nhưng finding có evidence rõ | Không tự reject; Backend đánh giá record đầy đủ |
| Legacy risk có giá trị | Audit-only, không cấp authority |
| Model không trả score hợp lệ | Operational/semantic failure theo contract, không bịa default authority |

## 6. Khuyến nghị

Chọn `D1`.

Phương án này không làm score vô dụng; nó biến score thành signal có semantics rõ để đánh giá,
thay vì một con số mơ hồ điều khiển action.

## 7. Acceptance record đã duyệt

```text
decisionId: BD-004
selectedOption: D1_NO_NUMERIC_BUSINESS_THRESHOLD_IN_SPRINT1
scoreRangeValidation: 0_TO_100
scoreBasedDecisionAuthority: NONE
scoreBasedActionAuthority: NONE
legacyRiskAuthority: NONE
futureThresholdRequiresCalibrationReport: true
futureThresholdRequiresSeparateActivationApproval: true
```

## 8. Kết luận

Phương án `D1_NO_NUMERIC_BUSINESS_THRESHOLD_IN_SPRINT1` đã được duyệt.

Approval chỉ cho phép range/semantic validation và evaluation collection; mọi threshold
activation tương lai vẫn cần calibration report và business approval riêng.
