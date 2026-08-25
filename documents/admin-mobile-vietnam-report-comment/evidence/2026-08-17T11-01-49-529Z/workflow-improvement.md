# Workflow improvement

- Chạy focused compile/test trước full Maven giúp phân biệt nhanh code bug với Docker/Testcontainers blocker.
- Tách `n8n import` và `publish`, sau đó export lại workflow theo ID để chứng minh active state.
- Khi chạy backend trên development database dùng chung, luôn tắt scheduler và initializer ngay từ lần khởi động đầu tiên.
- E2E cho control nằm trong clickable row phải kiểm tra event bubbling, overlay và keyboard focus.
- Giữ static invariant test cho raw content bên cạnh UI runtime để bắt việc vô tình đưa backend text vào translator.
- Frontend hiện không có changed-file unit coverage instrumentation; giữ nhãn `FRONTEND_COVERAGE_TOOLING_MISSING` thay vì suy diễn coverage từ build/E2E.
