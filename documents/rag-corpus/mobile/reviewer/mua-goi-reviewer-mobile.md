---
title: Mua gói reviewer trên ứng dụng di động
slug: mua-goi-reviewer-mobile
platform: mobile
category: reviewer
tags: [reviewer, payment, mobile, vnpay, stripe]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - PaymentOptions
---

# Mua gói reviewer trên ứng dụng di động CafeStory

## Giới thiệu

Màn hình PaymentOptions trên ứng dụng di động cho phép người dùng nâng cấp thành reviewer, mở gói cafe page hoặc mua gói quảng cáo. Với luồng "Trở thành reviewer", ứng dụng mở tab "Reviewer" mặc định, tải danh sách gói từ API, cho phép chọn phương thức thanh toán, rồi mở trang thanh toán bên ngoài (VNPAY hoặc Stripe) qua deep link Linking.openURL. Sau khi hoàn tất trên trình duyệt, người dùng quay lại ứng dụng và bấm kiểm tra trạng thái để đồng bộ.

## Điều kiện tiên quyết

Bạn cần đăng nhập tài khoản CafeStory. Thiết bị phải có trình duyệt mặc định hoạt động để mở trang thanh toán bên ngoài. Cần kết nối mạng ổn định trong suốt quá trình thanh toán và khi quay lại kiểm tra trạng thái. Với luồng reviewer, không có yêu cầu vai trò trước.

## Các bước thực hiện

### Bước 1: Mở màn hình PaymentOptions với tab reviewer

Từ tab Profile, chạm biểu tượng thẻ (CreditCard) trong ProfileTopBar để chuyển sang PaymentOptions. Ứng dụng truyền tham số initialTab là "reviewer" nên tab "Reviewer" đã được chọn sẵn ở dải tab dưới đáy màn hình. Bạn cũng có thể vào từ nút "Become reviewer" trong màn hình Reviewer Dashboard khi chưa có quyền.

Header hiển thị mũi tên quay lại, biểu tượng thẻ tròn, tiêu đề "Payment options" và phụ đề "Become a paid reviewer" tương ứng tab đang chọn. Dải tab ở dưới đáy có ba mục: "Reviewer", "Cafe page", "Ads". Tab "Ads" chỉ hiện với admin, chủ trang quán hoặc tài khoản có liên kết cafe page.

### Bước 2: Chờ danh sách gói tải xong

Ứng dụng gọi getExtraFees để lấy các gói reviewer registration và cafe page opening; đồng thời gọi getAdFees nếu bạn có quyền xem tab Ads. Trong lúc chờ, khối state hiển thị "Loading payment plans...". Sau khi có dữ liệu, ứng dụng lọc theo tab đang chọn và sắp xếp theo giá tăng dần.

### Bước 3: Đọc thẻ gói và chọn gói phù hợp

Mỗi gói hiện dưới dạng PlanCard. Gói được đánh dấu highlighted có nền cam đậm với chữ trắng, các gói khác có nền trắng viền xám nhạt. Thẻ gồm badge phân loại ("Reviewer", "Cafe", "Ads"), tên gói, mô tả ngắn và giá bên phải theo định dạng "N VND" với dấu phẩy phân cách. Dòng dưới giá ghi thời hạn và kiểu billing (ví dụ "3 months - one-time").

Danh sách tính năng liệt kê điểm chính: vai trò reviewer kích hoạt sau khi thanh toán được xác minh, thời hạn truy cập, quyền vào dashboard reviewer, huy hiệu và công cụ khám phá reviewer. Ghi chú "Existing reviewer access is extended by the server payment rule." báo hiệu server sẽ tự cộng dồn thời hạn nếu bạn đã là reviewer. Chạm nút "Choose reviewer package" ở cuối thẻ để mở modal chọn phương thức thanh toán.

### Bước 4: Chọn phương thức thanh toán trong PaymentMethodModal

Modal fade lên từ dưới, hiển thị tiêu đề "Choose payment method", tên gói và giá. Bên dưới là ô meta "Package duration". Danh sách phương thức hiện hai lựa chọn: "Stripe" (biểu tượng thẻ) và "VNPAY" (biểu tượng landmark). Mỗi lựa chọn có mô tả ngắn.

Chạm vào phương thức bạn muốn dùng. Biểu tượng bên trái chuyển thành vòng xoay và dòng tiêu đề đổi thành "Redirecting to Stripe..." hoặc "Redirecting to VNPAY...". Trong lúc này, ứng dụng gọi createPayment với extraFeeId của gói và paymentMethod tương ứng để lấy paymentUrl.

### Bước 5: Hoàn tất thanh toán trên trình duyệt bên ngoài

Khi có paymentUrl, ứng dụng dùng Linking.openURL để mở URL trong trình duyệt mặc định của thiết bị (hoặc app VNPAY/MoMo/ngân hàng nếu URL là deep link). Modal tự đóng và màn hình PaymentOptions hiển thị PaymentStatusCard với tiêu đề "Finish payment in browser" cùng hướng dẫn quay lại sau khi thanh toán.

Trên trình duyệt, thực hiện các bước theo quy trình của cổng thanh toán: chọn ngân hàng, nhập OTP hoặc dùng thẻ. Sau khi thấy màn hình xác nhận thành công của cổng, quay lại ứng dụng CafeStory bằng cách chuyển ứng dụng hoặc bấm nút quay lại.

### Bước 6: Kiểm tra trạng thái thanh toán

Trong PaymentStatusCard, chạm nút "Check status". Ứng dụng gọi getPayment với paymentId đã lưu để lấy trạng thái mới. Trong lúc đợi, thẻ hiển thị "Checking payment..." với vòng xoay.

- Nếu trạng thái là COMPLETED, PAID, PAID_SUCCESS hoặc SUCCESS, thẻ chuyển sang trạng thái thành công với viền hồng nhạt, thông báo "Payment verified. Your account has been updated." và ứng dụng gọi refreshCurrentUser để cập nhật vai trò REVIEWER trong phiên hiện tại.
- Nếu trạng thái là PENDING, thẻ hiển thị "Payment is still pending." Bạn có thể chờ vài giây rồi bấm "Check status" lần nữa.
- Nếu là trạng thái khác hoặc lỗi, thẻ hiện "Payment not completed" với biểu tượng cảnh báo và nút "Choose another method" để mở lại modal.

### Bước 7: Điều hướng sau khi thành công

Ở trạng thái success, nút chính hiển thị "Back to profile" cho gói reviewer. Chạm vào để quay lại tab Profile, nơi bạn sẽ thấy chip "Reviewer" mới và có thể mở Reviewer Dashboard. Với gói cafe page, nút sẽ chuyển thành "Open cafe page" và mở trực tiếp trang quán.

## Xử lý lỗi thường gặp

### "Payment URL was not returned by the server."

Máy chủ tạo được payment record nhưng chưa trả về URL thanh toán. Đóng modal, chờ vài giây rồi chọn lại phương thức khác, hoặc thử phương thức còn lại.

### "Payment was created, but the payment page could not be opened."

Linking.openURL thất bại (ví dụ thiết bị không có trình duyệt mặc định hoặc URL bị chặn). Ứng dụng vẫn giữ paymentId, bạn có thể chạm "Check status" sau khi mở link bằng cách khác, hoặc chọn "Choose another method" để tạo lại thanh toán bằng phương thức khác.

### Đã thanh toán xong nhưng "Check status" vẫn báo PENDING

Cổng thanh toán có thể chưa gọi webhook về máy chủ CafeStory. Chờ 30 đến 60 giây rồi bấm lại. Nếu sau nhiều phút vẫn PENDING, liên hệ hỗ trợ và cung cấp paymentId hiển thị trên thẻ.

### "Unable to create payment."

Lỗi mạng hoặc máy chủ từ chối tạo payment (ví dụ gói đã bị tắt). Kiểm tra kết nối, đóng modal, tải lại danh sách gói bằng nút "Retry" nếu có, hoặc chọn gói khác.

## Câu hỏi thường gặp

### Mất bao lâu để vai trò reviewer được kích hoạt?

Sau khi thanh toán được cổng xác nhận và ứng dụng nhận trạng thái PAID/SUCCESS, ứng dụng gọi refreshCurrentUser ngay để cập nhật vai trò trong phiên. Bạn có thể mở Reviewer Dashboard ngay lập tức.

### Tôi thanh toán bằng MoMo được không?

Trong ứng dụng, hai phương thức chính hiển thị là Stripe (thẻ) và VNPAY. VNPAY có thể chuyển tiếp sang các phương thức nội địa khác tuỳ theo cấu hình phía cổng.

### Có bị mất tiền nếu tôi đóng ứng dụng giữa chừng không?

Không. Payment record đã được tạo trên máy chủ với paymentId cụ thể. Khi quay lại và bấm "Check status", ứng dụng vẫn kiểm tra được trạng thái. Nếu bạn chưa hoàn tất thanh toán ở cổng, giao dịch sẽ được huỷ theo timeout của cổng.

### Tôi đã là reviewer thì mua thêm có được cộng dồn thời hạn không?

Có. Ghi chú trên thẻ gói ghi rõ "Existing reviewer access is extended by the server payment rule." - máy chủ tự cộng dồn thời hạn cho tài khoản hiện tại.
