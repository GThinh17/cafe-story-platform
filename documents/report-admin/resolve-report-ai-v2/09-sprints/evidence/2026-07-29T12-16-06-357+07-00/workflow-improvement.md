# Workflow improvement sau G0-12-DONE Audit

1. Từ Sprint sau, tạo một canonical acceptance-ID map ngay khi viết test để tránh RAI IDs lệch với SAF/CT/E2E IDs.
2. Mỗi gate runtime phải ghi rõ scenario nào của master matrix đã chạy, không chỉ tổng số pass.
3. Tách `Implementation DoD` khỏi `Production Readiness` ngay trong roadmap.
4. Biến prompt-adversarial và failure injection thành executable fixtures sớm hơn.
5. Không dùng “effective N/N” để suy ra full DoD nếu N chỉ thuộc một runner riêng.

