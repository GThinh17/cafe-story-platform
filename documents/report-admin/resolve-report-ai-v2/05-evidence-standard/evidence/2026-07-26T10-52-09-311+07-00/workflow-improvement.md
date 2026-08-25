# Workflow improvement — G0-12M-07

- Kiểm tra validator availability trước khi chọn implementation language; lần này Python thiếu
  `jsonschema`, Ajv đã có sẵn.
- Compile schema ngay sau khi tạo `$defs`, trước khi viết fixtures; strict typing đã bắt hai lỗi sớm.
- Giữ schema validation và semantic validation tách biệt để không tạo cảm giác JSON Schema có thể
  enforce toàn bộ cross-record policy.
- Dùng base + JSON Pointer overlays giúp negative fixture rõ thay đổi và giảm duplicate Common
  Envelope.
- M08 nên thêm một traceability table tự động từ M06 requirement sang validator error code để giảm
  review thủ công.
