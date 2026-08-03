# Workflow Improvement — Sau G0-06C

## Điều nên làm sớm hơn

- So sánh initializer với DB catalog trước khi tin source là current.
- Query score theo decision/action, không chỉ min/max toàn bảng.
- Escape Unicode khi terminal có dấu hiệu mojibake trước khi kết luận data corrupt.
- Luôn tách “code allows” khỏi “DB đã từng execute”.

## Evidence còn thiếu

- Representative cases cho COMMENT, RESOLVED và 20 reason chưa được dùng.
- Policy-labeled evaluation dataset.
- Historical retention/audit completeness.
- Published n8n version/runtime state.

## Cải tiến quy trình tiếp theo

1. G0-06D hợp nhất source/FE/n8n/DB finding, không lặp lại inventory.
2. G0-07 định nghĩa canonical taxonomy và source of truth.
3. G0-08 cấp stable rule ID/version thay free-form model code.
4. Chỉ tạo migration/data reconciliation plan sau business approval.
