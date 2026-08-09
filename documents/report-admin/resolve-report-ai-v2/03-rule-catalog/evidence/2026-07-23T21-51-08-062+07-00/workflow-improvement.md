# Workflow Improvement — G0-09

1. Dùng validator ID-set để kiểm tra source IDs và matrix IDs hai chiều, tránh orphan.
2. G0-10 nên review theo 12 decision package thay vì đọc tuần tự toàn bộ 59 trace rows.
3. Khi viết Sprint 1, mỗi acceptance criterion phải tham chiếu Trace ID, Policy ID và Rule ID.
4. Test report sau này phải phân biệt `DESIGN_COVERED`, `IMPLEMENTED`, `TESTED`,
   `RUNTIME_VERIFIED`.
5. Machine-readable trace manifest nên được tạo từ source of truth sau khi contract được chốt.
