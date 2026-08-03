# Restricted Goods and Commerce Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Rule | `CSR.COM.001` |
| Family | `PF-COMMERCE` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.COM.001` — Restricted goods or services

### Criteria

Target chào bán, môi giới hoặc tạo điều kiện giao dịch goods/services nằm trong danh mục
restricted/prohibited đã được policy owner phê duyệt. Mention, news hoặc education không tự là offer.

### Evidence model

- **Required:** exact item/service, transaction intent, target/content snapshot và policy-list version.
- **Counter-evidence/exceptions:** informational discussion, legal/authorized sale hoặc item không thuộc danh mục.
- **Critical missing:** chưa có approved restricted list; không xác định được item/intent/jurisdiction.
- **Candidate action:** content-level human review; actor/page action cần evidence scope rộng hơn.

Rule version `1.0.0-proposed.1`, automation `A0`, effective date `TBD_ACTIVATION`.
Danh mục goods/services và jurisdiction là blocker business cho activation, không phải việc AI tự suy.
