# Khuyến nghị cải tiến workflow sau G0-12F

## Làm ngay trước khi đóng Sprint 1

1. G0-12E và G0-12F đã được chốt bằng approval riêng.
2. Chạy audit Definition of Done của Sprint 1.
3. Chỉ đánh dấu `G0-12-DONE` sau khi audit xác nhận mọi tiêu chí bắt buộc.
4. Không dùng evidence disposable để tuyên bố external/production database ready.

## Hardening gần

1. Chuẩn hóa n8n error response thành HTTP 4xx/5xx với payload an toàn.
2. Dùng shared replay/idempotency store khi scale nhiều instance.
3. Xử lý unique-conflict thành idempotent reuse thay vì lỗi kỹ thuật.
4. Chốt policy/rule lifecycle khỏi trạng thái `proposed`.
5. Tạo legacy job fixture chính thức cho RAI-16.
6. Sửa lỗi lazy `imageUrls` trong legacy moderation worker bằng gate riêng.

## Evidence và policy sâu hơn

1. Xây trusted media ingestion với hash, MIME, OCR/vision và provenance.
2. Thiết kế evidence target-specific cho USER/CAFE_PAGE trước khi cho provider đánh giá sâu.
3. Bổ sung authoritative-source adapters cho rule cần kiểm chứng bên ngoài.
4. Thiết kế counter-evidence và conflict handling theo từng rule family.
5. Tạo labeled evaluation dataset và adversarial/prompt-injection suite.
6. Đo cost, latency, error rate, load và rollback drill.

## Nguyên tắc không thay đổi

- AI explanation không phải evidence.
- Reporter claim không phải fact.
- Thiếu evidence quan trọng phải manual/no action.
- Numeric score không cấp action authority.
- Backend giữ quyền validation, persistence và mutation boundary.
