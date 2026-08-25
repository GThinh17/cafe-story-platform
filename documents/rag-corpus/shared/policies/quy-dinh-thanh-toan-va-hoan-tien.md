---
title: Quy định thanh toán và hoàn tiền
slug: quy-dinh-thanh-toan-va-hoan-tien
platform: both
category: payment
tags: [payment, stripe, vnpay, bank-transfer, refund, expiration]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài liệu này mô tả vòng đời phiên thanh toán (payment session) trên CafeStory, các phương thức thanh toán được hỗ trợ, cơ chế hoàn tiền tự động và cách xử lý các mã lỗi VNPay phổ biến. Chính sách áp dụng cho toàn bộ giao dịch mua gói dịch vụ, kích hoạt sản phẩm và các luồng thanh toán tương tự trên cả web và mobile.

## Quy định

### 1. Thời hạn phiên thanh toán

Mỗi phiên thanh toán có thời gian sống **5 phút** kể từ lúc tạo. Sau 5 phút, phiên tự động chuyển sang trạng thái `EXPIRED` và không thể tiếp tục thanh toán.

Chi tiết tại `service/serviceImplement/PaymentServiceImpl.java` (dòng 135). Người dùng cần tạo phiên mới nếu muốn thanh toán lại.

### 2. Job auto-expire

Job nền `PaymentExpirationJob` chạy định kỳ **mỗi 60 giây**, quét các phiên đã quá hạn 5 phút và cập nhật trạng thái sang `EXPIRED`.

Chi tiết tại `until/schedule/PaymentExpirationJob.java` (dòng 22). Vì job chạy mỗi 60s, có thể có độ trễ tối đa 60s giữa thời điểm phiên thực tế hết hạn và lúc trạng thái được cập nhật.

### 3. Phương thức thanh toán hỗ trợ

Hệ thống hỗ trợ 3 phương thức:

- `STRIPE_CARD`: thanh toán bằng thẻ quốc tế qua Stripe.
- `BANK_TRANSFER`: chuyển khoản ngân hàng thủ công.
- `VNPAY`: cổng thanh toán VNPay.

Chi tiết tại `PaymentServiceImpl.java` (dòng 140-160, 191-199).

### 4. Bank transfer - xác nhận thủ công

Với `BANK_TRANSFER`, sau khi người dùng chuyển khoản, hệ thống **không tự xác nhận**. Giao dịch chờ admin đối soát và xác nhận thủ công qua trang quản trị. Chi tiết tại `PaymentServiceImpl.java` (dòng 191-199).

Trong lúc chờ, phiên thanh toán không bị job auto-expire ảnh hưởng nếu đã chuyển sang trạng thái chờ xác nhận thủ công (không còn ở trạng thái `PENDING` gốc).

### 5. Sync Stripe sau khi phiên hết hạn

Nếu client cố gắng sync trạng thái Stripe cho một phiên đã `EXPIRED`, hệ thống trả về **HTTP 410 GONE**. Client phải tạo phiên mới thay vì tiếp tục phiên cũ.

Chi tiết tại `PaymentServiceImpl.java` (dòng 296-298).

### 6. Hoàn tiền tự động khi kích hoạt sản phẩm thất bại

Khi thanh toán thành công nhưng bước kích hoạt sản phẩm (ví dụ gia hạn cafe page, mở khoá tính năng) **thất bại**, hệ thống:

1. Gọi Stripe Refund API để hoàn tiền tự động.
2. Cập nhật trạng thái payment sang `REFUNDED`.
3. Ghi log lý do thất bại để đối soát.

Chi tiết tại `PaymentServiceImpl.java` (dòng 352-367).

### 7. Mã lỗi VNPay phổ biến

Khi callback từ VNPay không hợp lệ, hệ thống trả về mã tương ứng chuẩn VNPay:

| Response code | Ý nghĩa                                     |
|---------------|---------------------------------------------|
| 97            | Chữ ký không hợp lệ                         |
| 04            | Số tiền callback lệch với số tiền đơn hàng  |
| 02            | Đơn hàng đã được xác nhận trước đó          |

Chi tiết tại `PaymentServiceImpl.java` (dòng 248-278).

### 8. Xử lý phía admin

Admin có thể xem danh sách payment, xác nhận thủ công (bank transfer), khởi tạo hoàn tiền và xuất báo cáo qua service `AdminPaymentServiceImpl`. Logic chính ở `service/serviceImplement/AdminPaymentServiceImpl.java` (dòng 93-125).

## Ngoại lệ

- Phiên `BANK_TRANSFER` đã chuyển sang trạng thái chờ xác nhận thủ công không bị job 60s ép hết hạn theo mốc 5 phút.
- Trường hợp mạng lỗi ngay khi Stripe đã charge, hệ thống dựa vào webhook Stripe để đồng bộ trạng thái; nếu vẫn không kích hoạt được sản phẩm, luồng refund tự động ở mục 6 sẽ chạy.
- Lỗi VNPay ngoài các mã 97/04/02 (ví dụ 24 - user huỷ) không được coi là thanh toán thành công; phiên trở lại `PENDING` cho đến khi hết 5 phút.
- Sync Stripe sau khi phiên `EXPIRED` luôn trả 410 GONE, kể cả khi Stripe đã charge thành công phía họ - trường hợp này admin xử lý refund thủ công qua Stripe Dashboard.

## Câu hỏi thường gặp

**1. Vì sao trang thanh toán của tôi báo hết hạn dù mới mở?**
Phiên chỉ sống 5 phút. Nếu bạn để tab lâu hơn, hãy tạo phiên mới.

**2. Tôi đã chuyển khoản ngân hàng nhưng chưa được kích hoạt?**
`BANK_TRANSFER` cần admin đối soát thủ công, thường trong giờ hành chính. Vui lòng chờ hoặc liên hệ hỗ trợ kèm bằng chứng chuyển khoản.

**3. Bị lỗi 410 khi bấm "Kiểm tra trạng thái"?**
Phiên đã hết hạn phía server. Tạo phiên mới; nếu đã bị trừ tiền thực sự, gửi giao dịch cho hỗ trợ để refund thủ công.

**4. VNPay báo lỗi 97 nghĩa là gì?**
Chữ ký callback không hợp lệ, thường do trung gian sửa query hoặc cấu hình secret sai. Thử lại; nếu lặp lại, liên hệ hỗ trợ.

**5. Bao lâu tiền refund về tài khoản?**
Refund được gửi lệnh ngay khi kích hoạt thất bại. Thời gian tiền về phụ thuộc ngân hàng phát hành thẻ (Stripe) - thường 5-10 ngày làm việc.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/PaymentServiceImpl.java` (dòng 135, 140-160, 191-199, 248-278, 296-298, 352-367)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/until/schedule/PaymentExpirationJob.java` (dòng 22)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminPaymentServiceImpl.java` (dòng 93-125)
