# Báo cáo đánh giá Mobile Search, Feed và Comments

## Bound

- Phạm vi thay đổi: hợp đồng tìm kiếm backend và luồng Explore Search của React Native mobile.
- Không thay đổi database, migration, Admin UI, AI Report, quyền endpoint hoặc luồng Feed/Comments.
- Dùng tài khoản admin do người dùng cho phép chỉ trong runtime cục bộ; không ghi credential/token vào evidence.

## Kết quả chẩn đoán

Feed và Comments không có lỗi contract trong runtime được kiểm tra. Chúng tải được sau khi backend và Expo chạy đúng origin được cho phép. Explore Search có lỗi thật: mobile gọi đồng thời ba API lấy toàn bộ users, cafe pages và blogs rồi lọc trên thiết bị. Riêng `/api/users` bị timeout và làm UI giữ trạng thái `Searching` quá 31 giây.

## Thay đổi

- Thêm endpoint read-only `GET /api/search?query=...&size=8`.
- Truy vấn riêng users, cafe pages và published blogs với giới hạn tối đa 20 mỗi nhóm; mobile dùng 8.
- Response chỉ chứa các trường Explore cần hiển thị/điều hướng.
- Mobile thay ba lần tải toàn bộ dữ liệu bằng một request có cache và giữ nguyên giao diện hiện tại.

## Verify

- Expo Web trên origin được backend cho phép: Feed PASS, Comments PASS.
- Search `cafe`: kết quả xuất hiện khoảng 1.38 giây, không còn treo.
- Sau clean build và restart backend, Search được xác nhận lại khoảng 1.57 giây; Feed và Comments tiếp tục PASS.
- Search reviewer: hiển thị đúng nhóm users/reviewers và posts.
- Backend full suite: 1373 tests, 0 failures, 0 errors, 1 skipped.
- Changed backend production code: 100% line; service 100% branch.
- Mobile `i18n:check` và `typecheck`: PASS.

## Giới hạn

- Chưa kiểm thử trên Android native vì máy không có `adb`/Android SDK khả dụng.
- Mobile chưa có unit-test/coverage tooling.
- Một tài khoản test tạm từ probe đăng ký vẫn còn vì API tự xóa trả HTTP 500 và admin API không có delete; không dùng database delete để che lỗi.

Trạng thái evidence: `PARTIAL` do thiếu Android native runtime, dù source, backend, Expo Web Search/Feed/Comments và regression tests đều pass.
