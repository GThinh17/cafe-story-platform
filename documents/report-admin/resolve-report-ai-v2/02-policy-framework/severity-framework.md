# Severity Framework

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Version draft | `PF-2.0.0-proposed.1` |
| Ràng buộc | Không chốt công thức hoặc threshold số |

## 2. Mục tiêu

Loại bỏ việc dùng một `riskScore` cho nhiều ý nghĩa khác nhau. Canonical V2 tách:

1. reporter severity hint;
2. violation likelihood;
3. harm severity;
4. assessment confidence;
5. action risk;
6. review urgency.

## 3. Canonical concepts

| Concept | Câu hỏi trả lời | Không được dùng để |
|---|---|---|
| `reporterSeverityHint` | Intake catalog cho rằng claim có thể nghiêm trọng đến đâu? | Chứng minh violation |
| `violationLikelihood` | Với evidence hiện có, khả năng rule bị vi phạm là bao nhiêu? | Đại diện harm/action risk |
| `harmSeverity` | Nếu violation là thật, hậu quả tiềm năng nghiêm trọng đến đâu? | Chứng minh violation |
| `assessmentConfidence` | Hệ thống chắc đến đâu về chính assessment hiện tại? | Override missing evidence |
| `actionRisk` | Candidate action có nguy cơ gây sai/hại/khó rollback thế nào? | Thay thế finding |
| `reviewUrgency` | Human review cần nhanh đến đâu? | Tự cấp destructive action |

## 4. Harm severity bands

| Code | Ý nghĩa đề xuất |
|---|---|
| `HS_UNKNOWN` | Không đủ basis để đánh giá harm |
| `HS_LOW` | Tác động hạn chế, dễ phục hồi, phạm vi nhỏ |
| `HS_MODERATE` | Tác động đáng kể nhưng có giới hạn hoặc có thể phục hồi |
| `HS_HIGH` | Tác động nghiêm trọng, lan rộng hoặc ảnh hưởng đối tượng dễ tổn thương |
| `HS_CRITICAL` | Nguy cơ tức thời/rất nghiêm trọng, khó phục hồi hoặc blast radius lớn |

Harm assessment MUST ghi factors:

- immediacy;
- reach;
- vulnerability;
- recoverability;
- persistence;
- coordination/repetition nếu rule yêu cầu.

## 5. Action risk bands

| Code | Ví dụ tính chất |
|---|---|
| `AR_LOW` | Không mutation hoặc thay đổi dễ rollback, blast radius nhỏ |
| `AR_MODERATE` | Hạn chế visibility tạm thời, có review/rollback rõ |
| `AR_HIGH` | Xóa/đình chỉ ảnh hưởng đáng kể, recovery khó |
| `AR_CRITICAL` | Irreversible hoặc ảnh hưởng rộng/nhạy cảm |

Action risk MUST được tính cho từng candidate action, không gắn cố định chỉ theo target.

## 6. Review urgency

| Code | Ý nghĩa |
|---|---|
| `RU_ROUTINE` | Queue bình thường |
| `RU_PRIORITY` | Cần review sớm |
| `RU_URGENT` | Harm cao hoặc time-sensitive |
| `RU_IMMEDIATE` | Potential imminent critical harm; ưu tiên tức thời |

Urgency cao MAY đi cùng `NEEDS_MANUAL_REVIEW`; không đồng nghĩa violation đã được chứng minh.

## 7. Normative rules

| ID | Policy |
|---|---|
| `SF-001` | Generic `riskScore` MUST NOT tồn tại trong canonical V2 contract. |
| `SF-002` | Legacy `riskScore` MAY được giữ dưới tên `legacyRiskScore` trong migration/audit, không dùng làm authority. |
| `SF-003` | `assessmentConfidence` và `violationLikelihood` MUST là field khác nhau. |
| `SF-004` | Confidence MUST ghi đối tượng assessment, evidence snapshot và version context. |
| `SF-005` | Model-reported confidence MUST được xem là uncalibrated cho tới khi evaluation được duyệt. |
| `SF-006` | Confidence cao MUST NOT override critical missing evidence hoặc insufficient burden. |
| `SF-007` | Harm cao + evidence thiếu MUST tăng urgency, không tự tăng enforcement authority. |
| `SF-008` | Reporter severity MUST NOT được copy thành official harm severity. |
| `SF-009` | Action risk MUST được đánh giá trước khi chọn action. |
| `SF-010` | Threshold số MUST có evaluation dataset, calibration report, owner và version trước khi activation. |

## 8. Semantic consistency checks

Backend MUST reject hoặc chuyển manual review khi:

- decision/action không khớp rule;
- violation likelihood không có finding/evidence;
- harm severity không có factors;
- confidence cao nhưng critical evidence missing;
- action risk/burden không đạt;
- score ngoài range hoặc không finite;
- score/version field thiếu;
- model output dùng legacy risk để cấp action.

## 9. Legacy evidence

Current DB cho thấy:

- 16/17 `RESOLVE` có legacy `riskScore < 70`;
- 12 suspend recommendation confidence 85 nhưng risk khoảng 7–8.5.

Do đó không thể suy ra mapping tin cậy từ legacy score sang field canonical. Migration MUST giữ raw value để audit nhưng MUST NOT tự chuyển đổi semantics.

## 10. Chưa chốt

- numeric scale/range cuối cùng;
- formula;
- thresholds theo rule/action;
- calibration dataset và acceptance metric;
- SLA mapping từ urgency.

Các mục này cần G0-08/G0-10/evaluation evidence.
