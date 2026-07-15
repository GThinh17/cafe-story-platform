---
title: Thanh toán bằng VNPAY
slug: thanh-toan-vnpay
platform: web
category: payment
tags: [thanh-toan, vnpay, membership, upgrade]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /payments/vnpay/return
---

# Thanh toán bằng VNPAY trên web

## Giới thiệu

Ngoài Stripe, CafeStory hỗ trợ thanh toán các gói nâng cấp bằng VNPAY, tiện dụng cho người dùng tại Việt Nam sử dụng thẻ nội địa hoặc tài khoản ngân hàng liên kết. Sau khi bạn chọn VNPAY trong cửa sổ Upgrade CafeStory, trình duyệt chuyển sang trang thanh toán do VNPAY vận hành. Khi giao dịch hoàn tất, bạn được đưa về trang `/payments/vnpay/return` để hệ thống kiểm tra chữ ký và trạng thái đơn hàng.

Sau khi xác nhận thành công, trang tự chuyển hướng bạn đến khu vực quyền lợi mới tương ứng với gói vừa mua.

## Điều kiện tiên quyết

Bạn cần đăng nhập tài khoản CafeStory và có thẻ nội địa hoặc tài khoản ngân hàng đã đăng ký dịch vụ thanh toán trực tuyến với VNPAY. Trình duyệt cần cho phép chuyển hướng sang tên miền VNPAY và giữ nguyên tab thanh toán cho đến khi VNPAY tự đưa bạn về CafeStory.

## Các bước thực hiện

### Bước 1: Mở cửa sổ Upgrade CafeStory

Từ trang chủ hoặc nút nâng cấp trong khu vực tài khoản, mở hộp thoại "Upgrade CafeStory". Xem lại hai thẻ gói: **Reviewer Membership** (cá nhân) và **Cafe Owner Plan** (chủ quán). Đối chiếu quyền lợi và giá tháng trước khi quyết định.

### Bước 2: Chọn gói và mở bước thanh toán

Nhấp "Choose plan" ở gói bạn mong muốn. Hộp thoại chuyển sang bước "Choose payment method" và hiển thị tên gói phía trên.

### Bước 3: Chọn phương thức VNPAY

Trong hai lựa chọn, nhấp vào ô "VNPAY" (Pay through the VNPAY hosted payment page). Nút chuyển sang "Redirecting to VNPAY..." và trình duyệt tự chuyển hướng sang trang thanh toán VNPAY.

### Bước 4: Hoàn tất trên trang VNPAY

Trên trang của VNPAY, chọn ngân hàng phát hành thẻ, nhập số thẻ hoặc thông tin tài khoản, sau đó xác nhận bằng mã OTP do ngân hàng gửi. Toàn bộ bước này tuân theo giao diện và quy trình an toàn của VNPAY.

### Bước 5: Chờ xác nhận tại trang trạng thái

Khi VNPAY tự đưa bạn về CafeStory, trang `/payments/vnpay/return` mở ra với tiêu đề "VNPAY payment". Hệ thống hiển thị dòng "Checking payment status..." trong lúc kiểm tra thông tin trả về. Khi hợp lệ, một thông báo màu xanh xác nhận "Payment verified" và trang tự chuyển hướng bạn tới đúng khu vực quyền lợi mới:

- Gói Reviewer Membership: chuyển tới bảng điều khiển reviewer.
- Gói Cafe Owner Plan: chuyển tới trang cafe đã kích hoạt, hoặc màn hình chỉnh sửa cafe để bạn hoàn tất thông tin nếu cần.

Nếu trạng thái còn "pending", nút "Check again" xuất hiện để bạn buộc hệ thống kiểm tra lại.

## Xử lý lỗi thường gặp

### Trang báo "Missing VNPAY return parameters."

Điều này xảy ra khi trang được mở mà không có tham số quay về từ VNPAY (ví dụ bạn paste URL cũ, hoặc trình duyệt cắt bỏ query string). Mở lại quy trình từ cửa sổ Upgrade CafeStory để có phiên mới.

### Trạng thái "Payment not completed"

Nghĩa là VNPAY chưa xác nhận thành công. Có thể giao dịch bị hủy giữa chừng, OTP sai, hoặc ngân hàng chưa duyệt. Nhấp "Check again" để hệ thống kiểm tra lại; nếu vẫn không thành công, bắt đầu phiên thanh toán mới.

### Bị chuyển về CafeStory nhưng không thấy trạng thái

Kiểm tra lại URL: đường dẫn phải là `/payments/vnpay/return` kèm chuỗi tham số dài do VNPAY thêm vào. Nếu URL không có chuỗi tham số, giao dịch chưa được VNPAY xác nhận.

### Đã trừ tiền nhưng quyền lợi chưa mở

Đợi vài giây để hệ thống hoàn tất kiểm tra chữ ký từ VNPAY. Nếu sau đó vẫn không thấy quyền lợi được kích hoạt, tham khảo tài liệu về xử lý thanh toán lỗi và chính sách hoàn tiền để biết các bước tiếp theo.

## Câu hỏi thường gặp

### Tôi có phải cài ứng dụng VNPAY để thanh toán không?

Không. Việc thanh toán diễn ra trên trang web do VNPAY cung cấp. Bạn chỉ cần thẻ hoặc tài khoản ngân hàng đã đăng ký dịch vụ thanh toán trực tuyến.

### Vì sao trang xác nhận không tự chuyển hướng?

Trang chỉ chuyển hướng khi trạng thái được xác nhận là thành công. Nếu VNPAY còn đang xử lý, dùng nút "Check again" để hệ thống đồng bộ lại; ngay khi có xác nhận, trang sẽ đưa bạn đến khu vực quyền lợi.

### Tôi có thể chuyển đổi giữa Stripe và VNPAY sau khi đã chọn không?

Có. Nhấn "Back" trong bước chọn phương thức để quay lại danh sách gói, sau đó chọn lại gói và phương thức khác. Việc này không tạo giao dịch trùng lặp vì phiên thanh toán chỉ khởi tạo khi bạn xác nhận phương thức.

### Tôi có nhận biên nhận qua email không?

VNPAY và ngân hàng phát hành gửi thông báo giao dịch theo cấu hình của bạn. CafeStory lưu lại đơn hàng trong lịch sử giao dịch của tài khoản để bạn kiểm tra bất cứ lúc nào.
