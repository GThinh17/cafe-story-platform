# Workflow improvement

- Kiểm tra ancestor và ahead/behind trước merge giúp dự báo chính xác trường hợp no-op.
- Với worktree đang dirty, luôn xác nhận staged index rỗng và không dùng stash/reset nếu người dùng yêu cầu bảo toàn thay đổi.
- Chỉ chạy test ứng dụng khi merge thực sự thay đổi source; trường hợp `Already up to date` chỉ cần kiểm chứng Git graph, branch, index và conflict markers.
