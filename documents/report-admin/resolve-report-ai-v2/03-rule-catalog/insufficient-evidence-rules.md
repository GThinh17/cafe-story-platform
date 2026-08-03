# Evidence Control Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Family | `CONTROL-EVIDENCE` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.EVD.001` — Critical evidence missing

Kích hoạt khi thiếu evidence được rule ứng viên đánh dấu critical. Kết quả bắt buộc:
`NEEDS_MANUAL_REVIEW + NO_ACTION`; không được dùng `REJECT`.

## `CSR.EVD.002` — Material evidence conflict

Kích hoạt khi evidence và counter-evidence tạo kết luận đối nghịch chưa giải quyết được.
Kết quả: `NEEDS_MANUAL_REVIEW + NO_ACTION`, ghi rõ conflict references.

## `CSR.EVD.003` — Evidence unreadable or unsupported

Kích hoạt khi image/link/media là critical nhưng pipeline chỉ nhận URL/text hoặc không xác thực/read được.
Kết quả: `NEEDS_MANUAL_REVIEW + NO_ACTION`; không được tưởng tượng nội dung media.

## `CSR.EVD.004` — Sufficient non-substantiation

Chỉ kích hoạt khi evidence đủ để đánh giá candidate rule và kết luận criteria không đạt,
không còn critical missing evidence. Kết quả có thể là `REJECT` với `KEEP_VISIBLE/KEEP_ACTIVE`.
Đây không phải “không tìm thấy bằng chứng nên reject”.

Mọi control dùng version `1.0.0-proposed.1`, automation `A0`, effective date `TBD_ACTIVATION`.
Control Rule ID phải được lưu cạnh decision reason; nó không giả làm violation finding.
