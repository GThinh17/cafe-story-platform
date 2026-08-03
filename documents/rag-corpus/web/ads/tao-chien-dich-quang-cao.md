---
title: Chạy quảng cáo cho quán trên web
slug: tao-chien-dich-quang-cao
platform: web
category: ads
tags: [ads, quang-cao, campaign, cafe-page, stripe, sponsored, marketing]
version: 1
updated_at: 2026-07-23
owner: team-frontend
related_ui:
  - /ads
  - /cafes/campaigns/new
---

# Chạy quảng cáo cho quán trên web

## Giới thiệu

Tính năng Quảng cáo (Ads) cho phép chủ quán trả phí để bài quảng cáo của quán được hiển thị ưu tiên (sponsored) trong bảng tin trộn (mixed feed) của CafeStory. Mỗi gói quảng cáo đã thanh toán dùng để tạo đúng một chiến dịch (campaign). Quảng cáo được phân phối cho tới khi đạt một trong hai giới hạn trước: đủ số lượt hiển thị của gói hoặc hết thời hạn ngày.

Bài viết này hướng dẫn mua gói quảng cáo và tạo chiến dịch từ trang Ads (`/ads`).

## Điều kiện tiên quyết

Bạn phải đăng nhập bằng tài khoản chủ quán và đã có ít nhất một trang quán (cafe page) do bạn sở hữu và đã kích hoạt. Nếu chưa có, hệ thống hiện thông báo "No owned cafe page" — hãy tạo và kích hoạt trang quán trước khi mua quảng cáo.

Hệ thống cần có gói quảng cáo đang mở bán (do quản trị viên kích hoạt). Nếu chưa có, tab Packages hiển thị "No active Ads package". Bạn cũng cần thẻ thanh toán để thanh toán qua Stripe.

## Các bước thực hiện

### Bước 1: Mở trang Ads

Truy cập đường dẫn `/ads`. Trang có hai tab: "Packages" (mua gói) và "Campaigns" (tạo và quản lý chiến dịch).

### Bước 2: Mua gói quảng cáo ở tab Packages

Ở tab "Packages", bạn sẽ thấy gói "Fixed Ads MVP" với mô tả "10,000 served impressions or 30 days" — nghĩa là chiến dịch chạy tới khi đạt 10.000 lượt hiển thị hoặc đủ 30 ngày, tùy điều kiện nào đến trước. Giá hiển thị theo VND, lấy từ cấu hình phí trên máy chủ.

Bấm nút "Pay with Stripe". Hệ thống tạo phiên thanh toán và chuyển bạn sang trang thanh toán Stripe. Sau khi thanh toán thành công, Stripe chuyển bạn về trang Ads kèm thông báo "Ads payment verified — Your paid package is ready for a new campaign".

### Bước 3: Tạo chiến dịch ở tab Campaigns

Sang tab "Campaigns". Nếu bạn có gói đã trả tiền nhưng chưa dùng, khối "Create campaign" sẽ hiện biểu mẫu. Điền các trường:

- **Paid Ads payment**: chọn gói đã thanh toán còn chưa dùng. Mỗi gói chỉ tạo được một chiến dịch.
- **Cafe page**: chọn trang quán bạn muốn quảng cáo.
- **Campaign headline**: tiêu đề quảng cáo, bắt buộc, tối đa 160 ký tự.
- **Description**: mô tả ngắn, tối đa 1000 ký tự (không bắt buộc).
- **Destination URL**: đường dẫn khi người xem bấm vào quảng cáo. Để trống thì bấm vào sẽ mở trang quán của bạn.
- **Creative image**: ảnh quảng cáo, tải lên từ máy (ảnh được lưu trên Cloudinary trong thư mục cafestory/ads).
- **Target regions**: chọn khu vực nhắm mục tiêu theo Tỉnh → Thành phố → Phường (Phường không bắt buộc), rồi bấm "Add region". Có thể thêm nhiều khu vực. Nếu không chọn khu vực nào, quảng cáo sẽ phân phối toàn quốc.
- **Activate immediately after creation**: tích chọn để chiến dịch chạy ngay sau khi tạo; bỏ tích nếu muốn lưu ở dạng nháp trước.

Bấm "Create campaign" để tạo. Chiến dịch mới sẽ xuất hiện trong mục "Campaign history" bên dưới.

### Bước 4: Theo dõi và quản lý chiến dịch

Trong "Campaign history", mỗi chiến dịch hiển thị dưới dạng thẻ với trạng thái (nháp/đang chạy) và các số liệu phân phối. Bấm "Refresh" để cập nhật danh sách mới nhất.

## Xử lý lỗi thường gặp

### Không thấy nút tạo chiến dịch

Nếu tab Campaigns hiện "No unused paid package", nghĩa là bạn chưa có gói đã trả tiền nào còn chưa dùng. Hãy mua thêm một gói ở tab Packages trước khi tạo chiến dịch mới.

### Thông báo "No owned cafe page"

Bạn chưa có trang quán do mình sở hữu và đã kích hoạt. Tạo và kích hoạt trang quán trước, sau đó quay lại mua quảng cáo.

### Thông báo "No active Ads package"

Hiện chưa có gói quảng cáo nào được mở bán. Quản trị viên cần kích hoạt phí quảng cáo (Ads fee) trước khi bạn có thể thanh toán. Vui lòng thử lại sau.

### Không được chuyển sang Stripe

Thông báo "Stripe checkout URL was not returned." nghĩa là máy chủ chưa trả về đường dẫn thanh toán. Đợi một lát rồi bấm "Pay with Stripe" lại.

### Biểu mẫu báo thiếu thông tin

Thông báo "Choose a paid Ads payment, cafe page, and campaign title." xuất hiện khi bạn chưa chọn gói đã thanh toán, chưa chọn trang quán, hoặc chưa nhập tiêu đề chiến dịch. Hãy điền đủ ba trường này rồi tạo lại.

## Câu hỏi thường gặp

### Một gói quảng cáo tạo được mấy chiến dịch?

Mỗi gói đã thanh toán chỉ tạo được một chiến dịch. Muốn chạy thêm chiến dịch, bạn mua thêm gói.

### Quảng cáo chạy trong bao lâu?

Gói "Fixed Ads MVP" kết thúc khi đạt 10.000 lượt hiển thị hoặc đủ 30 ngày, tùy điều kiện nào đến trước.

### Nếu không chọn khu vực thì quảng cáo hiển thị ở đâu?

Không chọn khu vực nào đồng nghĩa với phân phối toàn quốc (nationwide).

### Người xem bấm vào quảng cáo sẽ đi tới đâu?

Nếu bạn điền Destination URL, quảng cáo dẫn tới đường dẫn đó. Để trống thì quảng cáo mở trang quán của bạn.
