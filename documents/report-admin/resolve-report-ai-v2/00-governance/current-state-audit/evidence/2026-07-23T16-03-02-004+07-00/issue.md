# Issue Register — G0-06B

| ID | Mức | Loại | Tóm tắt | Trạng thái xử lý |
|---|---|---|---|---|
| `ISS-G0-06B-01` | Nghiêm trọng | `CONFLICT` | Destructive auto-apply trái `HI3/AR3` | Ghi nhận; chưa sửa trước policy gate |
| `ISS-G0-06B-02` | Cao | `MISSING` | Contract không có evidence/counter/missing/sufficiency | Input cho canonical contract |
| `ISS-G0-06B-03` | Cao | `LEGACY` | Generic riskScore và threshold hardcode | Deprecate trong V2 design |
| `ISS-G0-06B-04` | Cao | `CONFLICT` | Reporter claim/report count/AI output cũ có thể làm tăng risk | Chuẩn hóa trust/evidence |
| `ISS-G0-06B-05` | Cao | `MISSING` | Không thực sự phân tích image pixels/OCR | Bắt buộc manual review khi ảnh là critical evidence |
| `ISS-G0-06B-06` | Cao | `MISSING` | Thiếu version pinning, correlation và idempotency | Input Sprint 1 contract/audit |
| `ISS-G0-06B-07` | Cao | `MISSING` | Admin UI không hiển thị căn cứ và uncertainty | Input UI DD |
| `ISS-G0-06B-08` | Trung bình | `MISSING` | Webhook không có application-level auth/replay protection | Input security design |
| `ISS-G0-06B-09` | Trung bình | `LEGACY` | Raw response persist/expose và body preview log | Cần retention/redaction policy |
| `ISS-G0-06B-10` | Trung bình | `UNKNOWN` | DB reason/schema/data và n8n runtime chưa audit | DB: G0-06C; runtime: approval riêng |

Chi tiết và source line nằm trong:

- `source-inventory.md`;
- `frontend-inventory.md`;
- `n8n-workflow-inventory.md`.
