# Workflow Improvement — G0-12B

1. Tách test pure semantic validator khỏi service test ngay khi tạo class mới; việc này làm gap coverage rõ hơn.
2. Luôn đo per-class từ JaCoCo XML; aggregate coverage có thể che class thiếu line/branch.
3. Test fail-closed cần bao gồm non-2xx, empty response, valid-signature nhưng sai JSON shape và unsigned response.
4. Khi coverage phát hiện dead branch, ghi `CODE_BUG` trước khi sửa; không exclude class hoặc giảm threshold.
5. G0-12C cần chạy PostgreSQL/Flyway trên môi trường disposable và không dùng H2 pass để suy ra migration runtime pass.

