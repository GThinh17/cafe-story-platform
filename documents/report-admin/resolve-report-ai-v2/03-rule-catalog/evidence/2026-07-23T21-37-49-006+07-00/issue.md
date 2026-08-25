# Issue Register — G0-08

## Đã phát hiện và xử lý

| ID | Class | Vấn đề | Xử lý |
|---|---|---|---|
| `G008-DOC-001` | `DOC_CONSISTENCY` | Bốn rule có metadata Rule ID nhưng heading chưa theo mẫu canonical, khiến validator không nhận là definition | Đổi heading sang `## CSR... — title`, chạy lại validator |

## Deferred, không phải lỗi đã fix

| ID | Loại | Nội dung | Gate |
|---|---|---|---|
| `G008-DEC-001` | BUSINESS | Protected characteristic list | G0-10 |
| `G008-DEC-002` | BUSINESS/LEGAL | IP, privacy, restricted-commerce scope/jurisdiction | G0-10 |
| `G008-DEC-003` | BUSINESS | Reason disposition active/deprecated/alias | G0-10 |
| `G008-DEC-004` | BUSINESS | Exact auto-apply allowlist và thresholds | G0-10 |
| `G008-DES-001` | DESIGN | Target-specific deep evidence | G0-11 hoặc future approved phase |
| `G008-MIG-001` | MIGRATION | 14 legacy free-form Rule Code mapping | Approved migration plan |

Không có blocker cho việc hoàn thành bản `PROPOSED`.
