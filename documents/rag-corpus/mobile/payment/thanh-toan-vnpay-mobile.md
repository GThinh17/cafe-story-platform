---
title: Thanh toán qua VNPAY trên ứng dụng di động
slug: thanh-toan-vnpay-mobile
platform: mobile
category: payment
tags: [payment, vnpay, deep-link, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - PaymentOptions
---

# Thanh toán qua VNPAY trên ứng dụng di động CafeStory

## Giới thiệu

Ứng dụng di động CafeStory hỗ trợ thanh toán các gói dịch vụ (gói reviewer, gói mở cafe page, gói quảng cáo) qua VNPAY. Do luật cửa hàng ứng dụng, quá trình thanh toán thẻ và tài khoản ngân hàng được xử lý ở trình duyệt ngoài chứ không nhúng trực tiếp trong ứng dụng. Ứng dụng tạo một phiên thanh toán, mở URL VNPAY bằng "Linking.openURL" của React Native, và sau khi bạn quay lại, ứng dụng chủ động gọi API kiểm tra trạng thái để xác thực đã trả tiền hay chưa.

## Điều kiện tiên quyết

Bạn cần đã đăng nhập tài khoản CafeStory trong ứng dụng di động. Thiết bị cần có kết nối mạng và trình duyệt mặc định để mở trang thanh toán VNPAY. Bạn cần thẻ ATM nội địa, thẻ tín dụng quốc tế hoặc tài khoản ngân hàng liên kết VNPAY để hoàn thành giao dịch. Phiên thanh toán trên máy chủ có thời gian sống khoảng 5 phút; nếu bạn không hoàn tất trong khoảng thời gian đó thì phải tạo phiên mới.

## Các bước thực hiện

### Bước 1: Mở màn hình Payment options

Truy cập màn hình "Payment options" từ trang hồ sơ hoặc từ luồng đăng ký reviewer, mở trang cafe hoặc chạy quảng cáo. Thanh trên cùng hiển thị tiêu đề "Payment options", biểu tượng thẻ tín dụng và nhãn phụ mô tả tab đang được chọn.

### Bước 2: Chọn tab gói phù hợp

Ở phía cuối màn hình có dải tab dạng viên thuốc gồm ba mục: "Reviewer", "Cafe page" và "Ads". Tab đang được chọn được tô nền theo màu chủ đạo và hiển thị cả biểu tượng lẫn nhãn chữ; các tab còn lại chỉ hiển thị biểu tượng. Tab "Ads" chỉ xuất hiện khi tài khoản của bạn có vai trò admin, cafe page hoặc đã liên kết với một cafe page. Nếu bạn không có quyền truy cập "Ads" mà đang chọn tab đó, ứng dụng tự chuyển sang "Reviewer".

### Bước 3: Xem danh sách gói và chọn gói cần mua

Trong vùng cuộn chính, ứng dụng hiển thị các thẻ gói tương ứng với tab đang chọn. Mỗi thẻ gồm nhãn phân loại (Reviewer, Cafe hoặc Ads), tên gói, phần mô tả ngắn, khối giá bên phải theo định dạng "1,200,000 VND" cùng thời hạn ("3 months", "No expiry", "30 days"), danh sách tính năng đi kèm dấu tick tròn, ghi chú "note" nếu có, và nút hành động ở cuối thẻ. Nhấn nút hành động (ví dụ "Choose reviewer package") của gói bạn muốn.

### Bước 4: Chọn phương thức thanh toán VNPAY trong modal

Ứng dụng bật modal "Choose payment method". Modal gồm tiêu đề, dòng phụ hiển thị tên gói và giá, một khối "Package duration" nhắc lại thời hạn, và danh sách phương thức. Hiện tại có hai lựa chọn: "Stripe" cho thẻ quốc tế và "VNPAY" cho ngân hàng trong nước. Nhấn hàng "VNPAY - Pay through the VNPAY hosted payment page." Ứng dụng gọi API tạo payment trên máy chủ với id của gói. Trong lúc đợi, dòng "Redirecting to VNPAY..." kèm vòng xoay hiển thị bên trong hàng VNPAY để bạn biết ứng dụng đang xử lý.

### Bước 5: Ứng dụng mở URL VNPAY qua Linking.openURL

Ngay khi máy chủ trả về URL thanh toán, ứng dụng gọi "Linking.openURL" để mở URL đó. Trình duyệt mặc định của điện thoại (Safari trên iOS hoặc Chrome trên Android) sẽ mở trang VNPAY. Modal đóng lại và màn hình "Payment options" chuyển sang trạng thái "Finish payment in browser" với thẻ trạng thái hiển thị biểu tượng làm mới, tên gói, giá tiền và dòng "Complete payment in the browser, then return here to check status."

### Bước 6: Hoàn thành thanh toán trên trang VNPAY

Trên trang VNPAY, chọn ngân hàng hoặc phương thức bạn muốn, nhập thông tin thẻ hoặc quét mã QR để chuyển tiền, và xác thực bằng mã OTP theo hướng dẫn của ngân hàng. Sau khi VNPAY thông báo giao dịch thành công hoặc thất bại, quay lại ứng dụng CafeStory bằng cách vuốt ngang hoặc nhấn app switcher.

### Bước 7: Nhấn "Check status" để đối soát trạng thái

Trong thẻ trạng thái của màn hình "Payment options", nhấn nút "Check status". Ứng dụng gọi API "getPayment" với id giao dịch để đối chiếu với VNPAY. Trong lúc chờ, tiêu đề thẻ đổi thành "Checking payment..." và nút hiển thị "Checking...". Kết quả có thể là:

- Thành công: thẻ đổi sang trạng thái "Payment successful" với dòng "Payment verified. Your account has been updated." Ứng dụng tự làm mới thông tin người dùng để cập nhật vai trò hoặc quyền vừa mua.
- Đang chờ: thẻ hiển thị "Payment is still pending. Please check again after the provider finishes processing." Bạn có thể nhấn "Check status" lại sau vài giây.
- Thất bại: thẻ hiển thị "Payment not completed" cùng trạng thái từ máy chủ và nút "Choose another method" để chọn phương thức khác.

### Bước 8: Sau khi thành công, mở màn hình đích

Khi trạng thái là thành công, thẻ hiển thị một nút hành động phù hợp với gói. Với gói cafe page và tài khoản đã liên kết trang, nút hiển thị "Open cafe page" và mở màn hình quản lý cafe. Với các gói còn lại, nút hiển thị "Back to profile" và đưa bạn về màn hình hồ sơ để xem quyền lợi mới.

## Xử lý lỗi thường gặp

### Modal hiển thị "Payment URL was not returned by the server."

Máy chủ đã tạo payment nhưng chưa trả về URL. Đóng modal, thử tạo lại giao dịch. Nếu vẫn lỗi, kiểm tra kết nối mạng hoặc liên hệ đội hỗ trợ.

### Ứng dụng báo "Payment was created, but the payment page could not be opened."

Đây là lỗi khi "Linking.openURL" không mở được trình duyệt. Nguyên nhân thường gặp là thiết bị chưa cài trình duyệt mặc định hoặc URL không hợp lệ ở phía hệ điều hành. Bạn có thể sao chép URL từ email xác nhận (nếu có) hoặc quay lại nhấn "Check status" để giao dịch dở dang được tự dọn dẹp, sau đó tạo lại.

### Trạng thái vẫn là "pending" nhiều lần

VNPAY đôi khi cần vài chục giây để cập nhật kết quả về CafeStory. Nếu bạn đã hoàn tất OTP mà thẻ trạng thái vẫn "pending", hãy chờ khoảng 30 giây rồi nhấn "Check status" lại. Trong khoảng 5 phút phiên còn sống, hệ thống sẽ ghi nhận kết quả thành công khi VNPAY xác nhận.

### Phiên thanh toán hết hạn

Nếu bạn để trình duyệt VNPAY quá lâu, mã giao dịch có thể hết hạn (khoảng 5 phút). Khi bạn "Check status", máy chủ trả về trạng thái thất bại và bạn cần quay lại danh sách gói để tạo phiên mới.

### Modal đóng đột ngột khi đang xử lý

Nếu bạn cố đóng modal trong lúc ứng dụng đang gọi API tạo payment, nút đóng sẽ tạm không hoạt động cho tới khi có phản hồi. Chờ vòng xoay dừng rồi thao tác.

## Câu hỏi thường gặp

### Ứng dụng di động có thanh toán trong ứng dụng như Apple Pay hoặc Google Pay không

Hiện tại CafeStory dùng luồng thanh toán qua trình duyệt ngoài với VNPAY và Stripe. Các phương thức trong ứng dụng của cửa hàng sẽ được xem xét ở các phiên bản sau.

### Nếu tôi tắt ứng dụng ngay khi VNPAY xử lý xong, tiền có mất không

Không. VNPAY và CafeStory đối soát dựa trên id giao dịch. Khi bạn mở lại ứng dụng, quay lại màn hình "Payment options" và nhấn "Check status", hệ thống sẽ trả về trạng thái đúng. Nếu bạn không quay lại kịp trong 5 phút, có thể cần chờ máy chủ dọn phiên trước khi mua lại.

### Sao tab "Ads" không xuất hiện với tài khoản của tôi

Tab "Ads" chỉ hiện với vai trò admin, cafe page hoặc tài khoản đã có "cafePageId". Bạn cần mua và kích hoạt gói cafe page trước.

### Có thể quay lại danh sách gói trong lúc đang có phiên đang chờ không

Bạn có thể cuộn xuống cuối danh sách trong lúc thẻ trạng thái vẫn hiển thị phía trên. Khi giao dịch chưa thành công, danh sách gói vẫn hiển thị để bạn tham khảo. Sau khi thành công, ứng dụng ẩn danh sách gói và chỉ giữ thẻ trạng thái cùng nút mở màn hình đích.
