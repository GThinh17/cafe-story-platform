# Privacy and Personal Data Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Rule | `CSR.PRIV.001` |
| Family | `PF-PRIVACY` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.PRIV.001` — Unauthorized personal-data disclosure

### Criteria

Target disclosure, solicitation hoặc misuse personal/sensitive data trái quyền/context được phép.
Catalog không tự định nghĩa danh mục pháp lý cuối cùng của sensitive data.

### Evidence model

- **Required:** sanitized observation cho thấy loại dữ liệu và cách disclosure/misuse; target snapshot;
  provenance; context consent/public availability khi kiểm chứng được.
- **Counter-evidence/exceptions:** consent hợp lệ, chủ thể tự công khai trong đúng context, public-interest
  exception đã được policy/legal review.
- **Critical missing:** evidence đã bị redact tới mức không kiểm chứng được; consent/context không rõ;
  legal/jurisdiction exception chưa chốt.
- **Candidate action:** content restriction qua human; actor-level action cần evidence riêng.

Không ghi PII thô vào explanation/evidence report. Rule version `1.0.0-proposed.1`;
automation `A0`; effective date `TBD_ACTIVATION`.
