# Fix log

## Implemented

- Thêm suy luận `en`/`vi` có giới hạn từ report description và reason label.
- Ưu tiên description khi nhận diện được; dùng reason label khi description thiếu/không đủ tín hiệu; fallback English.
- Thêm system instruction chỉ điều khiển ngôn ngữ của `explanation` và `finding.rationale`.
- Đồng bộ Code node canonical vào workflow JSON.
- Bổ sung test English, Vietnamese và description precedence.

## Validation failures

- `AIRL-001` không sửa source: n8n local không chạy là lỗi môi trường.
- `AIRL-002` không sửa fixture manifest: checksum drift không do task này tạo và nằm ngoài phạm vi.
- `AIRL-003` không tự thêm coverage framework vì sẽ mở rộng phạm vi đáng kể.
