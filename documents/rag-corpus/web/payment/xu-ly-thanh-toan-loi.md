---
title: Xử lý thanh toán thất bại và hoàn tiền
slug: xu-ly-thanh-toan-loi
platform: web
category: payment
tags: [payment, stripe, vnpay, refund, error]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /payments/stripe/success
---

## Giới thiệu

Cafe Story hỗ trợ thanh toán qua Stripe (thẻ quốc tế) và VNPAY (nội địa). Sau khi hoàn tất bước thanh toán ở cổng, trình duyệt được chuyển về trang trạng thái của Cafe Story để xác minh giao dịch. Trạng thái thanh toán được đọc theo giá trị máy chủ trả về, không phải theo đường dẫn.

Tài liệu này tập trung vào các trạng thái thanh toán bất thường: giao dịch thất bại, phiên thanh toán đã hết hạn, giao dịch đã hoàn tiền, và các bước cần làm khi bạn không được kích hoạt gói dịch vụ đã trả tiền. Việc hiểu đúng luồng giúp bạn thao tác nhanh, biết khi nào chỉ cần bấm **Check again** và khi nào phải liên hệ hỗ trợ.

## Điều kiện tiên quyết

- Bạn đã bắt đầu một giao dịch thanh toán (Reviewer registration, Cafe page opening, hoặc các gói ExtraFee khác) và đang ở trang trạng thái `/payments/stripe/success` (Stripe) hoặc `/payments/vnpay/return` (VNPAY).
- Bạn đăng nhập bằng đúng tài khoản đã tạo phiên thanh toán. Trạng thái thanh toán được xác minh dựa trên tài khoản và `paymentId`, khác tài khoản sẽ không thấy dữ liệu.
- Trình duyệt kết nối được với máy chủ Cafe Story để gọi các API kiểm tra trạng thái và đồng bộ với Stripe.

## Các bước thực hiện

### Bước 1: Đọc thẻ trạng thái sau khi thanh toán

Trang trạng thái hiển thị tiêu đề "Stripe payment" hoặc "VNPAY payment" cùng thông báo mô tả tình trạng hiện tại. Thẻ **Verification in progress** hiện trong lúc hệ thống đang gọi máy chủ. Khi có kết quả, thẻ chuyển thành **Payment verified** (thành công) hoặc **Payment not completed** (chưa xong hoặc lỗi).

### Bước 2: Nhận diện trạng thái thanh toán

Cafe Story chuẩn hóa các trạng thái sau:

- **PAID / PAID_SUCCESS / SUCCESS / COMPLETED**: giao dịch đã thanh toán. Bạn được chuyển tự động đến bảng điều khiển reviewer, trang cafe hoặc trang chủ tuỳ vào loại gói.
- **PENDING**: giao dịch đang chờ xác nhận. Với Stripe, bấm **Check again** để đồng bộ nhanh.
- **FAILED**: giao dịch thất bại (thẻ bị từ chối, sai OTP, thanh toán không hoàn tất).
- **EXPIRED**: phiên thanh toán đã hết hạn. Phiên thanh toán mặc định sống khoảng 5 phút; sau thời gian đó, cổng thanh toán không nhận nữa.
- **REFUNDED**: giao dịch đã được hoàn tiền, thường do lỗi kích hoạt gói phía Cafe Story.

### Bước 3: Thử lại đối với Stripe pending

Khi Stripe hiển thị **Payment not completed** kèm dòng "Stripe may still be processing", bấm **Check again**. Hệ thống gọi API đồng bộ Stripe (`syncStripePayment`) để lấy trạng thái mới nhất từ Stripe. Nếu đã PAID, bạn được chuyển hướng ngay; nếu vẫn PENDING, thử lại sau vài giây.

### Bước 4: Xử lý FAILED hoặc EXPIRED

Với các trạng thái **FAILED** hoặc **EXPIRED**, việc bấm **Check again** không thay đổi kết quả. Quay lại trang chọn gói dịch vụ và tạo phiên thanh toán mới. Nếu tiền đã trừ thẻ nhưng trạng thái vẫn FAILED, kiểm tra sao kê ngân hàng — đôi khi ngân hàng giữ tạm và sẽ nhả sau 24–72 giờ.

### Bước 5: Nhận và xác nhận hoàn tiền

Cafe Story tự động hoàn tiền nếu bước kích hoạt gói (gán role, cộng thời hạn) thất bại sau khi đã ghi nhận thanh toán. Bạn sẽ thấy trạng thái **REFUNDED** cùng thông báo tương ứng. Tiền hoàn về cùng phương tiện thanh toán ban đầu (thẻ Stripe hoặc tài khoản VNPAY) trong 1–14 ngày làm việc tuỳ ngân hàng.

### Bước 6: Liên hệ hỗ trợ khi không tự giải quyết được

Nếu sau khi thử lại nhiều lần trạng thái vẫn không cập nhật, ghi lại `paymentId`, thời điểm giao dịch, phương thức và ảnh chụp thông báo lỗi. Gửi thông tin cho đội hỗ trợ Cafe Story. Không thanh toán lại cho cùng gói khi chưa xác minh rõ giao dịch gốc đã được xử lý.

## Xử lý lỗi thường gặp

**"Missing paymentId in the Stripe return URL."**
Đường dẫn trở về không chứa `paymentId`. Có thể bạn đã sao chép sai URL hoặc bấm mở trang thủ công. Quay lại lịch sử thanh toán trong tài khoản để mở lại đúng phiên hoặc bắt đầu lại giao dịch.

**"Missing VNPAY return parameters."**
Trình duyệt không giữ được các tham số VNPAY (`vnp_ResponseCode`, `vnp_TxnRef`, ...). Không tự nhập tay tham số. Kiểm tra email/SMS xác nhận từ VNPAY và liên hệ hỗ trợ nếu tiền đã bị trừ.

**"Payment cannot be verified."**
Máy chủ không tìm được phiên thanh toán tương ứng. Có thể phiên đã EXPIRED do vượt quá thời gian sống 5 phút, hoặc `paymentId` không thuộc tài khoản hiện tại. Đăng nhập đúng tài khoản và thử lại.

**"Failed to sync payment status."**
API đồng bộ Stripe trả lỗi. Nguyên nhân thường gặp: mất kết nối tạm thời hoặc Stripe đang giữ phiên. Đợi 10–30 giây rồi bấm **Check again**.

**Trang liên tục hiển thị "Checking payment status..." không dừng.**
Có thể do kết nối chậm. Đợi thêm hoặc làm mới trang. Không đóng tab nếu chưa thấy trạng thái cuối.

**Tiền đã trừ nhưng trạng thái vẫn FAILED sau khi phiên hết hạn.**
Đây là tình huống cần liên hệ hỗ trợ. Cafe Story sẽ tra soát với Stripe/VNPAY. Nếu Cafe Story đã thu tiền nhưng chưa kích hoạt gói, hoàn tiền sẽ được thực hiện tự động; nếu tiền chưa vào Cafe Story, cần chờ ngân hàng nhả tạm giữ.

## Câu hỏi thường gặp

**Phiên thanh toán sống bao lâu?**
Khoảng 5 phút kể từ khi tạo. Sau đó phiên chuyển sang EXPIRED và bạn phải tạo phiên mới.

**Vì sao Cafe Story tự hoàn tiền cho tôi?**
Vì thanh toán đã ghi nhận nhưng bước kích hoạt gói (gán vai trò, cộng thời hạn) thất bại. Để tránh tính phí sai, hệ thống tự hoàn tiền đầy đủ về phương tiện thanh toán ban đầu.

**Tôi đã bấm Check again mà vẫn PENDING, có nên thanh toán lại không?**
Không. Với Stripe, PENDING có thể chỉ là chậm đồng bộ; đợi vài phút rồi thử lại. Thanh toán lại có thể tạo giao dịch trùng và mất tiền hai lần.

**Refund mất bao lâu để về tài khoản?**
Tuỳ ngân hàng và cổng thanh toán, thường 1–14 ngày làm việc. Kiểm tra sao kê hoặc liên hệ ngân hàng phát hành thẻ nếu quá thời hạn này.

**Tôi được chuyển hướng đi đâu sau khi giao dịch thành công?**
Tuỳ loại gói: **REVIEWER_REGISTRATION** đưa bạn đến `/reviewer-dashboard`, **CAFE_PAGE_OPENING** đưa đến trang cafe của bạn (hoặc trang chỉnh sửa nếu chưa có), các gói khác về trang chủ.

**Tôi có thể huỷ giao dịch giữa chừng không?**
Có, chỉ cần đóng cửa sổ cổng thanh toán trước khi xác nhận. Phiên sẽ tự hết hạn sau 5 phút. Nếu đã bấm xác nhận, giao dịch có thể tiếp tục và cần xử lý theo các trạng thái ở trên.
