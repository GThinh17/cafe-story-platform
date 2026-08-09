# Workflow Improvement — G0-12E

## Cải tiến áp dụng ngay

- Preflight phải kiểm tra Flyway history của configured DB trước khi khởi động UI E2E.
- G0-12E phải có explicit target snapshot trước/sau AI; kiểm tra report/job là chưa đủ.
- Safe fixture phải nằm trên PostgreSQL disposable, không phụ thuộc Supabase.
- E2E phải lưu raw no-mutation evidence riêng cho từng target type.
- Cached/idempotent response phải đi qua cùng compatibility decorator như response mới.
- Runtime phải chạy current source/classes; không dùng JAR cũ không xác định commit.
- Legacy cancel cần fixture có `scheduled_at` đủ xa để worker không claim ngoài ý muốn.

## Cải tiến runner kế tiếp

- Thêm fixture adapter có kiểm soát cho legacy job để `RAI-16` chạy tự động, không cần supplemental step.
- Tách kết quả command Playwright khỏi kết luận gate: exit code `0` không được che `BLOCKED/WARN`.
- Xuất một gate summary tổng hợp automated + supplemental thay vì để người review tự cộng.
- Đo n8n execution count theo correlation để runtime chứng minh USER/PAGE không gọi provider trực tiếp.
- Tắt hoặc cô lập legacy moderation scheduler trong profile E2E nếu mục tiêu chỉ là Admin Report AI V2.

## Cải tiến tương lai

- Tạo bug riêng và integration test cho lazy `imageUrls` trong legacy moderation worker.
- Tạo một disposable environment command duy nhất: start DB → migrate → seed → run BE/UI/n8n → test → cleanup.
- Lưu commit SHA, workflow version và migration version tự động trong mỗi evidence manifest.
