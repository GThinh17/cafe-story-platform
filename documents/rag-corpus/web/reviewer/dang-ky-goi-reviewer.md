---
title: Đăng ký gói Reviewer
slug: dang-ky-goi-reviewer
platform: web
category: reviewer
tags: [reviewer, pricing-plan, membership, thanh-toan, stripe, vnpay]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /reviewer-dashboard
---

# Đăng ký gói Reviewer trên web

## Giới thiệu

Gói Reviewer Membership dành cho người dùng cá nhân muốn trở thành reviewer chính thức trên CafeStory. Sau khi thanh toán thành công, tài khoản của bạn sẽ được tự động gắn vai trò REVIEWER và có thể truy cập bảng điều khiển Reviewer, tham gia bảng xếp hạng, nhận thưởng theo lượt tương tác cùng nhiều quyền lợi khác.

Bài viết này hướng dẫn cách mở hộp thoại chọn gói (PricingPlanModal) từ thanh sidebar, chọn gói Reviewer Membership và hoàn tất thanh toán qua Stripe hoặc VNPAY.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản CafeStory trước khi mở hộp thoại chọn gói. Tài khoản cần đầy đủ email và thông tin cá nhân cơ bản để hệ thống kích hoạt vai trò REVIEWER sau khi thanh toán.

Máy tính cần có kết nối Internet ổn định trong suốt quá trình chuyển hướng sang cổng thanh toán. Nếu bạn chọn VNPAY, hãy chuẩn bị sẵn thẻ ATM nội địa hoặc tài khoản ngân hàng đã đăng ký Internet Banking. Nếu bạn chọn Stripe, hãy chuẩn bị thẻ tín dụng hoặc thẻ ghi nợ quốc tế.

## Các bước thực hiện

### Bước 1: Mở hộp thoại chọn gói từ sidebar

Ở thanh điều hướng bên trái của mọi trang chính, tìm mục "Pricing plan" ở phần dưới sidebar. Nhấp vào nút này để mở hộp thoại "Upgrade CafeStory".

Hộp thoại sẽ hiện tiêu đề "Upgrade CafeStory" cùng mô tả ngắn: "Choose a package, then complete payment with Stripe or VNPAY."

### Bước 2: Chọn gói Reviewer Membership

Trong hộp thoại, hệ thống hiển thị hai thẻ gói bên cạnh nhau:

- Thẻ "Reviewer Membership" với nhãn "Personal", danh sách quyền lợi (huy hiệu Reviewer Pro trên trang cá nhân và bài đánh giá, vị trí ưu tiên trong danh sách reviewer nổi bật, thống kê chi tiết theo bài đánh giá, quyền nhận thưởng theo lượt xem và tương tác, quyền truy cập sự kiện thử vị cùng đối tác).
- Thẻ "Cafe Owner Plan" với nhãn "Business" dành cho chủ quán.

Giá của mỗi gói được nạp trực tiếp từ máy chủ (danh mục Extra Fee) và định dạng theo VND. Nếu giá chưa nạp xong, nút "Choose plan" sẽ hiển thị chữ "Loading..." và bị vô hiệu hóa cho đến khi có dữ liệu.

Nhấp nút "Choose plan" trên thẻ Reviewer Membership để chuyển sang bước chọn phương thức thanh toán.

### Bước 3: Chọn phương thức thanh toán

Màn hình kế tiếp có tiêu đề "Choose payment method" cùng dòng phụ hiển thị tên gói bạn vừa chọn. Có hai lựa chọn:

- "Stripe" – thanh toán qua trang thanh toán Stripe bằng thẻ.
- "VNPAY" – thanh toán qua cổng VNPAY.

Nhấp vào thẻ phương thức mong muốn. Hệ thống sẽ gọi API tạo phiên thanh toán, nhận về đường dẫn và tự chuyển bạn sang cổng thanh toán tương ứng. Trong khi chờ, nhãn trên thẻ đổi thành "Redirecting to Stripe..." hoặc "Redirecting to VNPAY...".

Nếu muốn quay lại chọn gói khác, hãy nhấp nút "Back" ở góc trên. Nhấp "Cancel" để đóng hộp thoại mà không thanh toán.

### Bước 4: Hoàn tất thanh toán trên cổng thanh toán

Tại cổng thanh toán bên thứ ba, nhập thông tin thẻ (Stripe) hoặc chọn ngân hàng và xác thực OTP (VNPAY). Sau khi thanh toán thành công, cổng thanh toán sẽ tự chuyển bạn về CafeStory.

### Bước 5: Xác nhận vai trò REVIEWER được kích hoạt

Khi giao dịch được xác nhận, hệ thống cấp vai trò REVIEWER cho tài khoản của bạn. Bạn có thể mở menu Cài đặt hồ sơ và thấy mục "Reviewer dashboard" xuất hiện trong danh sách, hoặc truy cập trực tiếp đường dẫn bảng điều khiển Reviewer để bắt đầu sử dụng.

## Xử lý lỗi thường gặp

### Nút "Choose plan" bị mờ và không bấm được

Trạng thái này xảy ra khi máy chủ chưa trả về mức phí cho gói, hoặc cấu hình phí đang bị tắt. Đợi một lát để hệ thống nạp xong danh sách phí. Nếu vẫn không bấm được, hãy đóng hộp thoại rồi mở lại. Trường hợp thông báo "This package is not available right now." xuất hiện, gói tạm thời không được bán – vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ.

### Hộp thoại hiển thị "Unable to load membership plans."

Kết nối tới máy chủ đang gặp trục trặc. Kiểm tra Internet, tải lại trang (Ctrl+F5) và mở lại hộp thoại. Nếu lỗi lặp lại, đăng xuất rồi đăng nhập lại để làm mới phiên.

### Không được chuyển hướng sau khi bấm phương thức thanh toán

Thông báo "Payment URL was not returned by the server." nghĩa là máy chủ không trả về đường dẫn thanh toán. Đợi 30 giây rồi thử lại. Nếu vẫn không được, chuyển sang phương thức thanh toán còn lại (Stripe hoặc VNPAY).

### Đã thanh toán nhưng chưa thấy vai trò REVIEWER

Sau khi cổng thanh toán chuyển bạn về, hãy đợi vài giây để webhook cập nhật vai trò. Đăng xuất rồi đăng nhập lại để làm mới phiên. Nếu sau 10 phút vẫn chưa được kích hoạt, cung cấp mã giao dịch từ email biên nhận cho bộ phận hỗ trợ.

## Câu hỏi thường gặp

### Tôi có thể xem giá gói ở đâu trước khi thanh toán?

Giá hiển thị trực tiếp trên thẻ gói trong hộp thoại "Upgrade CafeStory". Đơn vị là VND, định dạng theo chuẩn Việt Nam. Giá lấy từ máy chủ theo cấu hình khuyến mãi hiện tại nên có thể khác giá tham khảo trong tài liệu.

### Tôi có thể đổi phương thức thanh toán sau khi đã chọn không?

Có. Ở màn hình chọn phương thức, bấm nút "Back" để quay về danh sách gói, rồi bấm lại "Choose plan" trên gói mong muốn để chọn phương thức khác.

### Sau khi kích hoạt gói, tôi có được hoàn tiền không?

Chính sách hoàn tiền tuân theo điều khoản dịch vụ của CafeStory và quy định của cổng thanh toán được sử dụng. Liên hệ bộ phận hỗ trợ với mã giao dịch để được tư vấn cụ thể.

### Vai trò REVIEWER áp dụng cho toàn bộ tài khoản hay chỉ trên web?

Vai trò được lưu ở tài khoản, có hiệu lực trên tất cả nền tảng CafeStory bạn đăng nhập (web và mobile).
