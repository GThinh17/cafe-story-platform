# G0-12C — Workflow improvement

1. Thêm một Docker Compose/Testcontainers profile chuyên cho migration integration, có PostgreSQL version cố định.
2. Tạo sanitized pre-V2 fixture có ít nhất một legacy AI resolution để kiểm tra data compatibility.
3. Thêm kill switches chính thức cho scheduled workers và custom Redis cache trong integration runtime.
4. Tự động assert Flyway history, 17 cột, 8 CHECK và 2 unique index thay vì dùng probe thủ công.
5. Tách migration gate khỏi n8n/UI runtime để lỗi external service không làm nhiễu kết luận database.

