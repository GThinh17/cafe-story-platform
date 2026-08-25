# Relevance and Intake Routing Controls

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Families | `PF-RELEVANCE`, `CONTROL-ROUTING` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.REL.001` — Off-topic or irrelevant

Đây là `NON_VIOLATION_SIGNAL`. Nó chỉ mô tả độ liên quan/chất lượng theo context cộng đồng.
Nếu chưa có community-specific relevance policy, signal này không được tạo `RESOLVE` hoặc punitive action.

## `CSR.ROUTE.001` — Dislike/preference signal

`DISLIKE_CONTENT` là preference signal, không phải evidence về violation. Route mặc định:
không tạo policy finding; có thể dùng cho product feedback ngoài moderation.

## `CSR.ROUTE.002` — Other report reason

`OTHER` yêu cầu description nhưng description vẫn là claim. Hệ thống phải xác định candidate rule
từ nội dung; nếu không xác định được thì `NEEDS_MANUAL_REVIEW + NO_ACTION`.

## `CSR.ROUTE.003` — Unspecified inappropriate image

`INAPPROPRIATE_IMAGE` không phải rule. Khi media đọc được, route observation sang Safety, Hate,
Sexual, Privacy, Integrity hoặc rule liên quan. Khi media là critical nhưng không đọc được,
áp `CSR.EVD.003`; URL ảnh đơn thuần không chứng minh nội dung.

Mọi control dùng version `1.0.0-proposed.1`, automation `A0`, effective date `TBD_ACTIVATION`.
