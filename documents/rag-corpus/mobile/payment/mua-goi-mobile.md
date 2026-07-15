---
title: Mua gói dịch vụ CafeStory trên ứng dụng di động
slug: mua-goi-mobile
platform: mobile
category: payment
tags: [payment, reviewer, cafe-page, ads, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - PaymentOptions
---

# Mua gói dịch vụ CafeStory trên ứng dụng di động

## Giới thiệu

Màn hình "Payment options" trên ứng dụng di động CafeStory là nơi tập trung để bạn mua các gói dịch vụ chính: gói đăng ký reviewer để trở thành người đánh giá, gói mở cafe page để tạo và quản lý trang cafe, và gói quảng cáo để đẩy bài viết hoặc cafe của mình. Ứng dụng nạp danh sách gói cố định từ máy chủ (extra fees) và danh sách gói quảng cáo (ad fees), rồi cho phép bạn chuyển tab để so sánh, chọn gói, và thanh toán qua trình duyệt ngoài.

## Điều kiện tiên quyết

Bạn cần đã đăng nhập ứng dụng CafeStory với vai trò người dùng bình thường trở lên. Thiết bị cần có kết nối mạng để tải danh sách gói và tạo giao dịch. Với tab "Ads", bạn cần có vai trò admin, cafe page hoặc tài khoản đã liên kết với một cafe page.

## Các bước thực hiện

### Bước 1: Mở màn hình Payment options theo ngữ cảnh phù hợp

Có nhiều cách vào màn hình "Payment options" tuỳ mục đích:

- Từ hồ sơ cá nhân, nhấn liên kết "Trở thành reviewer" để mở màn hình với tab mặc định là "Reviewer".
- Từ trang cafe hoặc luồng mở cafe page, hệ thống điều hướng tới màn hình với tab mặc định là "Cafe page".
- Từ luồng quảng cáo bài viết hoặc trang, hệ thống mở với tab mặc định là "Ads".

Ứng dụng đọc tham số "initialTab" khi mở màn hình để chọn tab hiển thị đầu tiên. Nếu bạn không có quyền vào "Ads", tab đó bị ẩn và ứng dụng chuyển về "Reviewer".

### Bước 2: So sánh gói trong tab "Reviewer"

Tab "Reviewer" hiển thị các gói do máy chủ trả về từ "getExtraFees" với loại "REVIEWER_REGISTRATION". Thẻ gói nổi bật được tô nền theo màu chủ đạo, hiển thị nhãn "Reviewer", tên gói, mô tả và giá theo VND. Danh sách tính năng thường bao gồm: kích hoạt vai trò reviewer sau khi thanh toán, thời hạn truy cập tính theo tháng, mở khoá bảng điều khiển reviewer và điều kiện nhận nhuận bút, huy hiệu hồ sơ và công cụ khám phá reviewer. Ghi chú "Existing reviewer access is extended by the server payment rule." nhắc rằng nếu bạn đã là reviewer, hệ thống sẽ gia hạn thay vì cấp mới.

### Bước 3: So sánh gói trong tab "Cafe page"

Tab "Cafe page" hiển thị các gói loại "CAFE_PAGE_OPENING". Thẻ gói nêu rõ số tháng truy cập, số thành viên tối đa của trang (nếu máy chủ trả về "maxMembers"), điều kiện tự tạo bản nháp cafe page nếu bạn chưa có, và ghi chú về việc gia hạn tự động nếu bạn đã có cafe page đang hoạt động. Đây là gói bắt buộc để mở trang cafe chính thức trên CafeStory.

### Bước 4: So sánh gói trong tab "Ads"

Tab "Ads" chỉ hiển thị khi tài khoản đủ điều kiện. Ứng dụng gọi "getAdFees" để lấy danh sách gói quảng cáo. Mỗi thẻ hiển thị nhãn "Ads", tên loại gói được viết chuẩn dạng "Feed Ad", giá VND, thời hạn cố định "30 days" và các tính năng liên quan tới quảng cáo. Ghi chú "Ads packages are available for cafe page and admin accounts." nhắc điều kiện quyền.

### Bước 5: Nhấn nút hành động của gói bạn muốn

Mỗi thẻ có nút hành động ở cuối. Nhấn nút "Choose reviewer package", "Choose cafe page package" hoặc "Choose ads package" tuỳ theo gói. Ứng dụng bật modal "Choose payment method" với thông tin nhắc lại: tên gói, giá tiền và thời hạn.

### Bước 6: Chọn phương thức và thanh toán qua trình duyệt

Trong modal, chọn "VNPAY" nếu bạn muốn thanh toán bằng ngân hàng Việt Nam hoặc "Stripe" nếu muốn dùng thẻ quốc tế. Ứng dụng gọi API tạo payment kèm "extraFeeId" hoặc "adFeeId" tuỳ nguồn gốc gói. Sau khi máy chủ trả về URL, ứng dụng gọi "Linking.openURL" mở trang thanh toán trên trình duyệt mặc định. Xem hướng dẫn chi tiết trong tài liệu "Thanh toán qua VNPAY trên ứng dụng di động" cho phần này.

### Bước 7: Quay lại kiểm tra trạng thái và mở màn hình đích

Sau khi hoàn tất thanh toán trên trình duyệt, quay về ứng dụng và nhấn "Check status" trong thẻ trạng thái ở màn hình "Payment options". Khi thành công, ứng dụng tự làm mới hồ sơ người dùng và hiển thị nút mở màn hình đích: "Open cafe page" nếu gói cafe page và tài khoản đã có "cafePageId", hoặc "Back to profile" cho các trường hợp còn lại.

## Xử lý lỗi thường gặp

### Danh sách gói trống với thông báo "No packages"

Khi máy chủ không trả về gói nào cho tab đang chọn, ứng dụng hiển thị màn hình trống. Với tab "Ads", mô tả sẽ là "No active ad fee packages are available for this account." Bạn có thể chuyển tab khác hoặc chờ đội vận hành cập nhật gói mới.

### Nhấn nút "Retry" khi tải gói thất bại

Nếu API tải gói lỗi, ứng dụng hiển thị màn hình trống với tiêu đề là thông báo lỗi và nút "Retry". Nhấn "Retry" để ứng dụng gọi lại đồng thời "getExtraFees" và "getAdFees" (nếu có quyền).

### Modal không tạo được payment

Khi máy chủ trả về lỗi, modal hiển thị dòng cảnh báo "Unable to create payment." trong khối cảnh báo màu đỏ nhạt. Kiểm tra kết nối và thử lại. Nếu vẫn lỗi, có thể gói đã bị vô hiệu hoá bởi admin và cần chọn gói khác.

### Không thấy tab "Ads"

Tab "Ads" chỉ dành cho tài khoản admin, cafe page hoặc có "cafePageId". Nếu bạn muốn chạy quảng cáo, mua gói cafe page trước, sau đó liên kết trang cafe với tài khoản để mở khoá tab "Ads".

### Vẫn thấy gói cũ dù admin đã cập nhật

Kéo xuống refresh không áp dụng cho màn hình này; hãy đóng màn hình "Payment options" và mở lại từ hồ sơ, ứng dụng sẽ gọi lại API và cập nhật danh sách gói.

## Câu hỏi thường gặp

### Tôi có thể mua nhiều gói cùng lúc không

Mỗi lần bạn chỉ tạo được một phiên thanh toán. Sau khi phiên hiện tại thành công hoặc thất bại, bạn có thể chọn gói tiếp theo. Trong thời gian đang có phiên, danh sách gói vẫn hiển thị nhưng thẻ trạng thái sẽ ưu tiên hiển thị ở phía trên.

### Gói reviewer đã hết hạn, mua lại có bị mất huy hiệu không

Không. Máy chủ áp dụng quy tắc gia hạn: nếu bạn từng là reviewer và mua lại, thời hạn mới sẽ nối tiếp trạng thái hiện tại. Huy hiệu và dữ liệu hồ sơ reviewer được giữ nguyên.

### Sau khi mua gói cafe page, làm sao vào trang cafe của mình

Khi phiên thanh toán thành công, thẻ trạng thái hiển thị nút "Open cafe page" đưa thẳng bạn tới màn hình quản lý cafe. Nếu bạn bỏ qua nút này, bạn có thể vào lại qua mục "My cafe page" trong hồ sơ.

### Tôi có thể mua gói quảng cáo trên di động rồi quản lý trên web không

Có. Dữ liệu gói và trạng thái quảng cáo được đồng bộ giữa hai nền tảng. Bạn có thể mua trên di động, sau đó vào phiên bản web để cấu hình chi tiết chiến dịch và theo dõi hiệu quả.

### Giá gói có bao gồm thuế không

Giá hiển thị trên thẻ gói là giá cuối cùng người dùng trả cho CafeStory theo cấu hình của admin. Nếu chính sách thuế thay đổi, thông tin sẽ được ghi vào phần mô tả hoặc ghi chú của gói.
