# Issue Register

## Defect trong phạm vi

| ID | Classification | Phát hiện | Kết quả |
|---|---|---|---|
| `G010-TEST-001` | `TEST_BUG` | Regex mojibake ban đầu không phân biệt hoa/thường, nên ký tự tiếng Việt `ã` bị match nhầm với marker Unicode `U+00C3` | Đổi validator sang case-sensitive; `0` file có marker mojibake |

Không phát hiện lỗi nội dung/consistency bắt buộc sau khi sửa validator.

## Residual issues chuyển bước sau

| ID | Classification | Nội dung | Route |
|---|---|---|---|
| `G010-RES-001` | `DESIGN_PENDING` | Chưa có Sprint 1 Detailed Design ánh xạ H1–L1 sang FE/BE/n8n | G0-11 |
| `G010-RES-002` | `LEGAL_PRIVACY_REVIEW` | Retention/jurisdiction là engineering baseline, chưa legal-adopt | Trước activation |
| `G010-RES-003` | `IMPLEMENTATION_PENDING` | Runtime hiện tại chưa được tuyên bố compliant với 12 quyết định | Sprint implementation/test |

Không sửa source để che các residual issue này.
