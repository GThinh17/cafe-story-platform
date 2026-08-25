---
title: Gia hạn gói Cafe Page
slug: gia-han-goi-cafe-page
platform: web
category: cafe-page
tags: [thanh-toan, goi-dich-vu, stripe, vnpay, cafe-page]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /cafes/[id]
---

# Gia hạn gói Cafe Page

## Giới thiệu

Trang quán trên CafeStory hoạt động theo mô hình gói dịch vụ. Khi gói của bạn hết hạn, trang quán tự động ẩn khỏi cộng đồng và người truy cập chỉ thấy hộp thoại "Cafe page expired". Chủ quán có thể gia hạn ngay từ hộp thoại này hoặc chủ động vào modal thanh toán "Upgrade CafeStory" từ menu Settings của trang quán.

## Điều kiện tiên quyết

Bạn phải đăng nhập bằng tài khoản chủ quán. Chỉ chủ quán mới thấy nút "Renew subscription" khi trang quán hết hạn hoặc trong hộp thoại "Cafe page settings".

Cần có thẻ hoặc tài khoản thanh toán tương thích với Stripe hoặc VNPAY.

## Các bước thực hiện

### Bước 1: Nhận biết gói đã hết hạn

Khi bạn mở trang quán mà gói đã hết hạn, thay vì nội dung quán, hệ thống mở hộp thoại "Cafe page expired" với dòng "Sorry! This cafe page subscription has expired.". Ở dưới có hai nút:

- "Go back": quay lại trang trước; đóng hộp thoại sẽ chuyển bạn về trang chủ.
- "Renew subscription": chỉ hiện khi bạn là chủ quán, mở modal thanh toán.

### Bước 2: Mở modal thanh toán từ trang quán còn hoạt động

Nếu quán chưa hết hạn nhưng bạn muốn gia hạn sớm, bấm biểu tượng bánh răng cạnh tên quán để mở "Cafe page settings", rồi chọn "Renew subscription". Modal "Upgrade CafeStory" sẽ xuất hiện.

### Bước 3: Chọn gói

Modal "Upgrade CafeStory" hiển thị tối đa hai gói:

- "Reviewer Membership" (Personal) — dành cho người viết review.
- "Cafe Owner Plan" (Business) — dành cho chủ quán, có huy hiệu Official, quản lý menu, giờ mở cửa, gallery, công cụ khuyến mãi và thống kê.

Giá và mô tả được lấy động từ máy chủ. Trong trường hợp máy chủ chưa trả về giá, hộp thoại hiển thị giá tham khảo tạm thời và nút bấm tạm khoá tới khi có dữ liệu.

Với mục đích gia hạn quán, chọn gói "Cafe Owner Plan" và bấm "Choose plan".

Nếu không có gói nào khả dụng, hệ thống hiện lỗi "This package is not available right now.". Thử lại sau.

### Bước 4: Chọn phương thức thanh toán

Sau khi chọn gói, modal chuyển sang bước "Choose payment method". Có hai lựa chọn:

- "Stripe" — "Pay with Stripe checkout using a card." Dành cho thanh toán bằng thẻ quốc tế hoặc thẻ hỗ trợ Stripe.
- "VNPAY" — "Pay through the VNPAY hosted payment page." Dành cho thẻ nội địa và các cổng Việt Nam.

Bấm vào thẻ tương ứng. Nút tạm chuyển sang "Redirecting to Stripe..." hoặc "Redirecting to VNPAY..." khi hệ thống đang chuẩn bị.

Bấm "Back" ở góc trên bên trái để quay lại chọn gói khác. Bấm "Cancel" ở cuối để đóng modal.

### Bước 5: Hoàn tất thanh toán ở cổng bên ngoài

Trình duyệt chuyển sang trang thanh toán của Stripe hoặc VNPAY. Hoàn tất theo hướng dẫn của cổng thanh toán. Sau khi thành công, cổng sẽ đưa bạn trở về CafeStory.

### Bước 6: Kiểm tra trạng thái quán

Sau khi thanh toán, quay lại trang quán `/cafes/<mã quán>`. Nếu giao dịch được xác nhận, trang quán mở bình thường và hộp thoại "Cafe page expired" không còn xuất hiện. Nếu vẫn thấy hộp thoại, thử tải lại trang sau vài phút để hệ thống đồng bộ trạng thái.

## Xử lý lỗi thường gặp

### Cảnh báo "Payment unavailable" trong modal

Hệ thống không tạo được phiên thanh toán. Kiểm tra kết nối mạng và thử lại. Nếu vẫn lỗi, đóng modal, tải lại trang và mở lại từ đầu.

### Lỗi "Unable to load membership plans."

Máy chủ không trả về danh sách gói. Có thể do sự cố tạm thời. Đóng modal, chờ ít phút rồi mở lại.

### Lỗi "Payment URL was not returned by the server."

Máy chủ không cung cấp đường dẫn tới cổng thanh toán. Thử lại bằng cách bấm lại nút phương thức thanh toán, hoặc chuyển sang phương thức còn lại (Stripe/VNPAY) để so sánh.

### Lỗi "Unable to create payment."

Không tạo được giao dịch. Kiểm tra lại tài khoản đăng nhập, đảm bảo phiên vẫn còn hiệu lực. Đăng xuất và đăng nhập lại nếu cần.

### Không thấy nút "Renew subscription" trong hộp thoại hết hạn

Nút chỉ hiển thị với chủ quán. Nếu bạn không phải chủ, không thể gia hạn thay chủ; hãy nhờ chủ quán thực hiện.

### Sau thanh toán vẫn thấy trang bị ẩn

Có thể trạng thái chưa được đồng bộ. Tải lại trang sau vài phút. Nếu quá lâu, kiểm tra email xác nhận thanh toán và liên hệ đội hỗ trợ kèm mã giao dịch.

## Câu hỏi thường gặp

### Tôi có thể gia hạn khi quán chưa hết hạn không?

Có. Mở "Cafe page settings" trên trang quán và chọn "Renew subscription" bất cứ lúc nào.

### Stripe và VNPAY có khác nhau về giá không?

Giá gói lấy từ máy chủ, không phụ thuộc phương thức thanh toán. Tuy nhiên chính sách quy đổi tiền tệ và phí giao dịch của từng cổng có thể khác nhau — tham khảo trên cổng thanh toán tương ứng.

### Sau khi thanh toán bao lâu thì quán mở lại?

Thông thường ngay sau khi cổng thanh toán chuyển bạn về CafeStory. Trong trường hợp giao dịch cần xác thực bổ sung, có thể chậm hơn vài phút.

### Tôi có thể chuyển gói cho tài khoản khác không?

Gói gắn với tài khoản chủ quán đã thanh toán. Việc chuyển đổi cần liên hệ đội hỗ trợ.

### Hoá đơn thanh toán tôi lấy ở đâu?

Sử dụng biên lai/xác nhận từ cổng Stripe hoặc VNPAY. Bạn có thể tra cứu trong lịch sử giao dịch của cổng tương ứng.
