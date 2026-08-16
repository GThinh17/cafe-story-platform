# Workflow improvement retrospective

## Điều đã cải thiện

- Runtime test nay dùng exact Contract V2 thay vì payload cũ và xác minh cả HMAC response, schema, evidence bounds, ngôn ngữ và execution-field absence.
- E2E dùng API để seed/cleanup nhưng bắt buộc thao tác target mutation qua UI và confirm dialog.
- Cleanup nằm trong `finally`, nhờ đó target được khôi phục và report test được đóng ngay cả khi assertion thất bại.
- Issue được phân loại trước khi sửa: test bug được sửa trong test; product layering bug được sửa hẹp trong UI; không thay source để che lỗi môi trường.

## Cải tiến đề xuất sau task

- Thêm frontend unit/component coverage tooling để thay `FRONTEND_COVERAGE_TOOLING_MISSING` bằng changed-file coverage định lượng.
- Thêm instrumentation cho n8n Code node để đo changed-line/branch coverage thay vì chỉ static/adversarial/runtime contract tests.
- Tách summary reporter dùng chung để các lần retry E2E ghi vào cùng một run manifest nhưng vẫn giữ raw artifacts riêng.

Các đề xuất này không phải tiêu chí bắt buộc của thay đổi hiện tại và chưa được triển khai.
