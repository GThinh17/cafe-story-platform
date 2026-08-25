---
title: Gói Cafe Page là gì
slug: goi-cafe-page
platform: both
category: cafe-page
tags: [cafe-page, goi-dich-vu, chu-quan]
version: 2
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Gói Cafe Page là gói dịch vụ dành cho chủ quán cafe muốn tạo trang chính thức cho quán trên CafeStory. Trang quán được dùng để giới thiệu không gian, menu, địa chỉ, kết nối với cộng đồng người yêu cafe, và đăng bài viết dưới danh nghĩa của quán.

Cafe Page có ba trạng thái vòng đời cố định trong hệ thống: `DRAFT` (bản nháp khi chủ quán vừa tạo), `ACTIVE` (đang hoạt động sau khi mua gói hợp lệ), và `SUSPENDED` (bị tạm đình chỉ). Khi trang mới được khởi tạo, hệ thống luôn đặt trạng thái mặc định là `DRAFT` và cờ `pageActive = false`, nghĩa là trang chưa được phát hành ra công chúng cho đến khi chủ quán thanh toán gói mở trang thành công.

Gói mở trang có thời hạn theo tháng (`durationMonths`) và giá do CafeStory cấu hình động thông qua bảng ExtraFee với `feeType = CAFE_PAGE_OPENING`. Giá không được hard-code trong mã nguồn, admin có thể thay đổi giá và thời hạn mà không cần deploy lại backend.

## Quy định

**1. Một tài khoản chỉ được sở hữu một Cafe Page.**

Hệ thống kiểm tra chặt ràng buộc này tại tầng validation. Nếu một tài khoản đã từng tạo Cafe Page (bất kể trạng thái) mà cố gắng tạo thêm trang thứ hai, API sẽ trả về HTTP `409 CONFLICT` với thông báo lỗi rõ ràng. Ràng buộc này áp dụng cho toàn bộ người dùng, không có ngoại lệ theo vai trò thông thường.

**2. Số thành viên quản lý mặc định là 2.**

Khi tạo Cafe Page mới, giá trị `maxMembers` mặc định trong entity `CafePage` là `2`. Đây là giới hạn tổng số nhân sự có thể được gán vai trò quản lý trang (bao gồm cả OWNER). Số này có thể tăng lên khi chủ quán mua gói có `maxMembers` cao hơn — logic thanh toán sẽ cập nhật lại trường này trên trang.

Giá trị `maxMembers` phải luôn ≥ 1. Nếu admin cấu hình một gói ExtraFee có `maxMembers < 1`, hệ thống sẽ báo lỗi validation khi tạo hoặc cập nhật gói.

**3. Chỉ vai trò OWNER và CO_OWNER mới được đăng bài và quản lý trang.**

Các thao tác quản lý (đăng bài dưới danh nghĩa quán, sửa thông tin trang, thêm/xóa thành viên, cập nhật menu) đều được validator kiểm tra và chỉ chấp nhận nếu người thực hiện có vai trò OWNER hoặc CO_OWNER trên chính Cafe Page đó. Các thành viên với vai trò thấp hơn (nếu có) chỉ có quyền xem hoặc hỗ trợ theo cấu hình riêng, không được đăng bài.

**4. Thanh toán gói kích hoạt trang theo cơ chế cộng dồn.**

Khi giao dịch thanh toán gói Cafe Page chuyển sang trạng thái PAID, hệ thống thực hiện đồng thời các bước sau trên trang tương ứng:

- Đặt `pageActive = true` để trang hiển thị công khai.
- Tính `pageExpiresAt = max(now, current_expiresAt) + durationMonths`. Nếu trang chưa hết hạn, thời gian còn lại được cộng thêm chứ không bị mất; nếu đã hết hạn, thời hạn mới tính từ thời điểm thanh toán.
- Cập nhật `maxMembers` theo cấu hình của gói vừa mua.
- Gán role `CAFE_PAGE` cho tài khoản chủ trang nếu chưa có.

**5. Giá gói do CafeStory cấu hình động.**

Toàn bộ giá gói mở trang được lưu trong bảng ExtraFee với `feeType = CAFE_PAGE_OPENING`. Admin có thể thêm nhiều gói với `durationMonths` và `maxMembers` khác nhau. Người dùng chọn gói ở giao diện thanh toán, backend đọc từ database chứ không dùng hằng số trong mã.

## Ngoại lệ

- Chủ tài khoản đã từng có Cafe Page nhưng trang đó đã bị xóa cứng khỏi hệ thống có thể tạo lại trang mới; ràng buộc "1 tài khoản 1 Cafe Page" chỉ tính trên các trang còn tồn tại.
- Trạng thái `SUSPENDED` không phải là điều kiện mở cho việc tạo trang mới — trang bị SUSPENDED vẫn tính là 1 Cafe Page đang sở hữu.
- Trang ở trạng thái `DRAFT` vẫn được tính vào ràng buộc sở hữu duy nhất, dù chưa được phát hành.
- Thời hạn `pageExpiresAt` không tự động gia hạn miễn phí; nếu hết hạn mà không thanh toán, `pageActive` sẽ chuyển về `false` và trang không còn hiển thị công khai.

## Câu hỏi thường gặp

**Tôi có thể tạo hai Cafe Page cho hai chi nhánh khác nhau không?**
Không. Mỗi tài khoản chỉ sở hữu tối đa một Cafe Page. Nếu bạn muốn quản lý nhiều chi nhánh, hãy dùng cùng một trang và mời nhân sự chi nhánh khác vào làm thành viên (theo `maxMembers` của gói).

**Số thành viên tối đa mặc định là bao nhiêu?**
Mặc định là 2 người (bao gồm chính chủ trang). Nếu bạn cần nhiều hơn, hãy chọn gói ExtraFee có `maxMembers` lớn hơn.

**Nếu tôi gia hạn khi gói chưa hết hạn thì có bị mất thời gian còn lại không?**
Không. Hệ thống cộng dồn theo công thức `expiresAt = max(now, current) + durationMonths`, nên thời gian còn lại được giữ nguyên và cộng thêm.

**Ai được đăng bài dưới danh nghĩa quán?**
Chỉ vai trò OWNER và CO_OWNER trên trang đó mới có quyền đăng bài. Người dùng thường theo dõi quán không thể đăng bài dưới danh nghĩa quán.

**Trang mới tạo có hiển thị ngay không?**
Không. Trang mới tạo mặc định ở trạng thái `DRAFT` với `pageActive = false`, chỉ hiển thị công khai sau khi thanh toán gói mở trang thành công.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/validation/CafePageValidator.java:34-38` — ràng buộc 1 tài khoản 1 Cafe Page (409 CONFLICT).
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/validation/CafePageValidator.java:40-68` — kiểm tra vai trò OWNER/CO_OWNER khi đăng bài, quản lý trang.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/CafePage.java:66,79` — mặc định `status = DRAFT` và `pageActive = false`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/CafePage.java:74-76` — `maxMembers` mặc định là 2.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/enums/PageStatus.java` — 3 trạng thái DRAFT, ACTIVE, SUSPENDED.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ExtraFeeServiceImpl.java:20` — mặc định `maxMembers = 2`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ExtraFeeServiceImpl.java:85-88` — kiểm tra `maxMembers` phải ≥ 1.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/PaymentServiceImpl.java:447-485` — logic thanh toán: bật `pageActive`, cộng dồn `pageExpiresAt`, cập nhật `maxMembers`, gán role `CAFE_PAGE`.
