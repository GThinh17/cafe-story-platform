---
title: Thanh toán bằng Stripe
slug: thanh-toan-stripe
platform: web
category: payment
tags: [thanh-toan, stripe, checkout, membership, upgrade]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /payments/stripe/success
---

# Thanh toán bằng Stripe trên web

## Giới thiệu

CafeStory hỗ trợ nâng cấp gói dịch vụ (Reviewer Membership và Cafe Owner Plan) thông qua cổng thanh toán Stripe với thẻ tín dụng hoặc thẻ ghi nợ. Quy trình gồm ba bước chính: chọn gói trong cửa sổ Upgrade CafeStory, chọn phương thức Stripe, và hoàn tất thanh toán trên trang do Stripe cung cấp. Sau khi thanh toán, bạn được đưa về trang xác nhận `/payments/stripe/success` để hệ thống kiểm tra và xác nhận trạng thái đơn hàng trước khi mở quyền lợi tương ứng.

Phiên thanh toán Stripe có hiệu lực khoảng năm phút. Nếu quyền lợi không kích hoạt được vì lỗi hệ thống sau khi thanh toán thành công, cơ chế hoàn tiền tự động sẽ được kích hoạt. Chi tiết chính sách được mô tả trong tài liệu về chính sách hoàn tiền.

## Điều kiện tiên quyết

Bạn cần đăng nhập tài khoản CafeStory và có thẻ được Stripe chấp nhận. Trình duyệt cần cho phép chuyển hướng sang tên miền của Stripe. Vì thao tác chuyển hướng có thể mất vài giây, hãy tránh đóng tab trong lúc trình duyệt đang mở trang Stripe.

## Các bước thực hiện

### Bước 1: Mở cửa sổ Upgrade CafeStory

Từ trang chủ hoặc nút nâng cấp trong tài khoản, mở hộp thoại "Upgrade CafeStory". Hộp thoại hiển thị hai thẻ gói dịch vụ: **Reviewer Membership** (dành cho người dùng cá nhân muốn trở thành reviewer) và **Cafe Owner Plan** (dành cho chủ quán muốn mở trang cafe chính thức). Mỗi thẻ liệt kê giá tháng, danh sách quyền lợi và nút "Choose plan".

### Bước 2: Chọn gói và mở bước thanh toán

Nhấp nút "Choose plan" ở gói bạn muốn mua. Hộp thoại chuyển sang trang "Choose payment method" với thông tin gói đã chọn ở đầu. Nếu cần đổi ý, nhấp nút "Back" để quay lại danh sách gói.

### Bước 3: Chọn phương thức Stripe

Trong lưới hai lựa chọn, nhấp vào ô "Stripe" (Pay with Stripe checkout using a card). Nút chuyển sang trạng thái "Redirecting to Stripe..." và trình duyệt tự chuyển hướng sang trang checkout của Stripe.

### Bước 4: Hoàn tất trên trang Stripe

Trên trang Stripe, nhập thông tin thẻ và bấm xác nhận thanh toán. Đây là bước diễn ra ngoài CafeStory, tuân theo giao diện và quy trình bảo mật của Stripe. Sau khi giao dịch được ngân hàng của bạn duyệt, Stripe sẽ tự động điều hướng trở lại CafeStory.

### Bước 5: Chờ xác nhận tại trang trạng thái

Bạn được đưa về `/payments/stripe/success` với tiêu đề "Stripe payment". Hệ thống hiển thị dòng "Checking payment status..." trong lúc kiểm tra máy chủ. Khi trạng thái chuyển sang "Payment verified", một thông báo màu xanh hiện lên và trang tự chuyển hướng bạn tới đúng khu vực quyền lợi mới:

- Gói Reviewer Membership: chuyển tới bảng điều khiển reviewer.
- Gói Cafe Owner Plan: chuyển tới trang cafe của bạn nếu đã được kích hoạt, hoặc màn hình chỉnh sửa cafe để bạn hoàn tất thông tin quán.

Nếu trạng thái vẫn là "pending" (Stripe còn đang xử lý), nút "Check again" sẽ hiện ra để bạn buộc hệ thống đồng bộ lại với Stripe.

## Xử lý lỗi thường gặp

### Thẻ bị từ chối trên trang Stripe

Đây là lỗi do ngân hàng phát hành, không phụ thuộc CafeStory. Kiểm tra hạn mức, số dư, hoặc thử thẻ khác. Bạn có thể quay lại `/messages` hoặc trang chủ và mở lại hộp thoại Upgrade để tạo phiên mới.

### Trang trạng thái báo "Missing paymentId in the Stripe return URL."

Đường dẫn quay về bị thiếu tham số. Nguyên nhân thường do bạn mở lại trang bằng bookmark cũ hoặc paste URL không đầy đủ. Mở lại quy trình thanh toán từ đầu bằng cửa sổ Upgrade.

### Trang trạng thái báo "Payment not completed"

Trạng thái này xuất hiện khi Stripe chưa xác nhận hoặc giao dịch chưa thành công. Nhấp "Check again" để hệ thống đồng bộ lại. Nếu sau vài lần thử vẫn không chuyển sang "Payment verified", đợi tối đa năm phút để phiên hết hạn, sau đó khởi tạo phiên mới.

### Đã trừ tiền nhưng quyền lợi không kích hoạt

Trong trường hợp thanh toán thành công nhưng CafeStory không thể kích hoạt quyền lợi tương ứng, cơ chế hoàn tiền tự động được kích hoạt. Bạn xem chi tiết trong tài liệu về xử lý thanh toán lỗi và chính sách hoàn tiền.

## Câu hỏi thường gặp

### Tôi có thể thanh toán bằng phương thức khác ngoài Stripe không?

Có. Trong bước chọn phương thức, bạn có thể chọn VNPAY thay vì Stripe. Cả hai phương thức đều dẫn tới cùng gói dịch vụ và quyền lợi.

### Vì sao trang xác nhận không tự động chuyển hướng?

Trang chỉ chuyển hướng khi trạng thái được xác nhận là "paid". Nếu Stripe còn đang xử lý, hãy nhấp "Check again" để đồng bộ; hệ thống sẽ chuyển hướng ngay khi có kết quả xác nhận.

### Bao lâu thì phiên thanh toán hết hạn?

Phiên checkout của Stripe có hiệu lực khoảng năm phút. Sau thời gian này, bạn cần mở lại cửa sổ Upgrade để tạo phiên mới.

### Tôi có nhận hóa đơn không?

Stripe gửi biên nhận về email đăng ký thẻ (nếu email đó cấu hình nhận biên nhận). CafeStory ghi nhận đơn hàng trong lịch sử giao dịch của tài khoản để bạn tra cứu lại.
