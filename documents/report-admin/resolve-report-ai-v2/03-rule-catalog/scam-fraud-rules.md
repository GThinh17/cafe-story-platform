# Scam and Fraud Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Rule | `CSR.INT.003` |
| Family | `PF-INTEGRITY` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.INT.003` — Scam or fraud

### Criteria

Target có representation hoặc hành vi nhằm chiếm đoạt tiền, tài sản, credential hoặc lợi ích
thông qua deception có tính material. Chỉ có từ “lừa đảo” trong reporter description không đạt criteria.

### Evidence model

- **Required:** exact offer/request/representation; payment/credential solicitation nếu liên quan;
  context; provenance; evidence kết nối target với hành vi.
- **Supporting:** pattern/history đã xác minh, destination/link metadata hoặc transaction reference
  đã sanitize; supporting item không thay observation trực tiếp.
- **Counter-evidence/exceptions:** giao dịch hợp lệ, misunderstanding đã được chứng minh, refund/correction,
  satire/awareness content.
- **Critical missing:** không truy cập được content/link; thiếu material representation; external fact
  không được xác minh; target identity không chắc chắn.

### Decision/action effect

- Đủ evidence cho finding có thể đề xuất `RESOLVE`.
- Chưa đủ để kết luận có hay không có scam phải `NEEDS_MANUAL_REVIEW`, không `REJECT`.
- Content action và actor/page action có burden khác nhau; một post đơn lẻ không mặc định đủ để suspend.
- `automationEligibility=A0`; version `1.0.0-proposed.1`; effective date `TBD_ACTIVATION`.
