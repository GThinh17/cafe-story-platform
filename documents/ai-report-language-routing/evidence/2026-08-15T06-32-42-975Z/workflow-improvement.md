# Workflow improvement

- Kiểm tra n8n port `5678` trước khi gọi runtime suite để phân loại môi trường sớm hơn.
- Broad evaluation dataset nên có lệnh riêng để refresh/verify manifest có giải thích, tránh checksum drift che khuất các hard gate độc lập.
- Nên tách language detection khỏi n8n Code node thành utility thuần để đo coverage trực tiếp mà không làm thay đổi contract.
- Runtime evidence còn thiếu một lần gọi provider thật cho mỗi ngôn ngữ.
