---
title: Rút tiền qua Stripe Connect
slug: rut-tien-stripe
platform: web
category: reviewer
tags: [reviewer, earnings, payout, stripe-connect, thu-nhap]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /reviewer-dashboard/earnings
---

# Rút tiền qua Stripe Connect trên web

## Giới thiệu

CafeStory chi trả thu nhập cho reviewer thông qua Stripe Connect. Trước khi nhận tiền, bạn phải liên kết tài khoản Stripe qua quy trình onboarding, đợi trạng thái onboarding hoàn tất (COMPLETE) và bật quyền chi trả (payoutsEnabled=true). Sau đó, hệ thống chốt payout hàng tháng và chuyển tiền tự động về tài khoản đã liên kết.

Bài viết hướng dẫn cách khởi tạo onboarding Stripe, xác nhận trạng thái sau khi quay về CafeStory, xem lịch sử payout và hiểu vòng đời payout PENDING → APPROVED → PAID.

## Điều kiện tiên quyết

Tài khoản cần có vai trò REVIEWER và đã có ít nhất một chu kỳ tính thu nhập. Bạn cần đăng nhập trên trình duyệt và có kết nối Internet ổn định để chuyển hướng sang Stripe.

Chuẩn bị sẵn giấy tờ pháp lý (CMND/CCCD hoặc hộ chiếu, thông tin ngân hàng nhận tiền) theo yêu cầu của Stripe cho khu vực Việt Nam. Stripe có thể yêu cầu bổ sung tùy loại tài khoản.

## Các bước thực hiện

### Bước 1: Mở bảng điều khiển Reviewer và khởi tạo onboarding

Đăng nhập rồi mở bảng điều khiển Reviewer. Ở khu vực đầu trang tổng quan, nếu chưa liên kết Stripe, bạn sẽ thấy nút "Connect Stripe Account" (kèm biểu tượng liên kết ngoài).

Nhấp nút này. Trong khi hệ thống tạo đường dẫn onboarding, chữ trên nút đổi thành "Redirecting..." và bạn sẽ được chuyển tới trang onboarding của Stripe.

### Bước 2: Hoàn tất onboarding trên Stripe

Trên trang Stripe, làm theo các bước:

- Xác nhận email, số điện thoại.
- Cung cấp thông tin pháp lý theo yêu cầu.
- Nhập thông tin tài khoản ngân hàng nhận tiền.
- Xác nhận và hoàn tất onboarding.

Sau khi hoàn tất, Stripe sẽ tự chuyển bạn về đường dẫn return của CafeStory.

### Bước 3: Xác nhận trạng thái sau khi quay về CafeStory

Trang trả về hiển thị:

- Vòng biểu tượng màu xanh với biểu tượng xoay khi đang đồng bộ.
- Tiêu đề "Stripe account connected".
- Dòng phụ "Syncing account status..." khi đang đồng bộ, đổi thành "Returning to dashboard..." khi xong.

Hệ thống gọi API đồng bộ trạng thái Stripe Connect, sau đó tự chuyển bạn về trang tổng quan Reviewer sau khoảng 1,5 giây. Bạn không cần thao tác gì thêm ở bước này.

### Bước 4: Kiểm tra nhãn "Payouts enabled"

Trên trang tổng quan Reviewer, thay cho nút "Connect Stripe Account" giờ đây sẽ hiện nhãn "Payouts enabled" (kèm chấm xanh Wifi). Nhãn này xuất hiện khi cả hai điều kiện thỏa: onboardingStatus bằng COMPLETE và payoutsEnabled bằng true. Nếu chỉ hoàn thành một trong hai, nút "Connect Stripe Account" vẫn hiện – bạn cần quay lại Stripe để bổ sung thông tin còn thiếu.

### Bước 5: Mở tab Earnings để xem lịch sử payout

Ở sidebar bên trái, bấm mục "Earnings" (biểu tượng ví). Trang Earnings detail hiển thị:

- Bốn thẻ tổng quan: Latest payout, All-time total, Total breakdown (phân bổ theo Like amount, Share amount, Comment amount).
- ReviewerPayoutPanel bên trái và biểu đồ cột "Monthly payout chart" bên phải.
- Bảng Payout history với các cột: Month, Likes, Shares, Comments, Like amount, Share amount, Comment amount, Total, Status.

Nếu chưa có dữ liệu, trang hiện dòng "No payout data yet."

### Bước 6: Đọc trạng thái payout

Ở cột Status của bảng, mỗi đợt payout gắn một badge trạng thái:

- PENDING – payout đã được tính toán, đang chờ duyệt.
- APPROVED – payout đã được duyệt, chờ chuyển tiền qua Stripe.
- PAID – Stripe đã chuyển tiền thành công về tài khoản của bạn.

Khi trạng thái chuyển sang PAID, bạn có thể kiểm tra tài khoản ngân hàng để nhận tiền theo lịch của Stripe (thường mất 1–3 ngày làm việc tùy khu vực).

## Xử lý lỗi thường gặp

### Nút "Connect Stripe Account" không phản hồi

Kiểm tra chặn cửa sổ pop-up hoặc chặn chuyển hướng của trình duyệt. Tắt các tiện ích chặn (ad-blocker, chặn cookie bên thứ ba) cho miền CafeStory và thử lại. Nếu vẫn không được, tải lại trang và bấm lại.

### Đã hoàn tất onboarding nhưng nhãn vẫn hiện "Connect Stripe Account"

Có hai khả năng: Stripe chưa duyệt xong hồ sơ (payoutsEnabled=false) hoặc trạng thái chưa được đồng bộ. Bấm lại nút "Connect Stripe Account" để quay lại Stripe bổ sung thông tin còn thiếu. Sau khi Stripe xác nhận, quay về CafeStory và tải lại trang tổng quan để hệ thống đồng bộ lại.

### Trang Earnings hiện "No payout data yet."

Bạn chưa có kỳ payout nào được tính. Thu nhập được chốt theo tháng, nên reviewer mới đăng ký cần đợi qua chu kỳ tháng đầu tiên.

### Payout mãi ở trạng thái PENDING

Trạng thái PENDING nghĩa là hệ thống đã tính toán thu nhập nhưng chưa được duyệt để chi. Việc duyệt do đội vận hành thực hiện định kỳ. Nếu quá 7 ngày làm việc vẫn ở PENDING, liên hệ bộ phận hỗ trợ.

### Payout ở APPROVED nhưng chưa nhận được tiền

Sau khi duyệt, Stripe sẽ chuyển tiền theo lịch. Thời gian xử lý phụ thuộc ngân hàng nhận (thường 1–3 ngày làm việc). Nếu quá 5 ngày làm việc chưa nhận được, kiểm tra dashboard Stripe hoặc liên hệ hỗ trợ CafeStory kèm mã payout.

## Câu hỏi thường gặp

### Tôi có thể liên kết Stripe cho nhiều tài khoản CafeStory không?

Mỗi tài khoản Reviewer chỉ liên kết với một tài khoản Stripe Connect. Nếu muốn đổi tài khoản nhận tiền, cập nhật thông tin ngân hàng trên chính tài khoản Stripe đã liên kết.

### Thu nhập được tính từ đâu?

Thu nhập gồm ba khoản: Like amount, Share amount và Comment amount, tương ứng với số lượt like, share, comment mà bài đánh giá của bạn nhận được trong kỳ. Trang Earnings hiển thị từng khoản riêng để bạn dễ theo dõi.

### Tôi có bị trừ phí khi rút tiền không?

Phí phụ thuộc chính sách của Stripe và ngân hàng nhận. CafeStory không thu phí trên phần chi trả của bạn – số tiền hiển thị ở cột Total là số trước phí Stripe. Chi tiết phí xem trong dashboard Stripe.

### Tôi có thể yêu cầu rút sớm hơn lịch không?

Payout được xử lý theo lịch hàng tháng. Chưa hỗ trợ rút sớm ngoài lịch trên giao diện.

### Đơn vị tiền tệ hiển thị là gì?

Toàn bộ số tiền trên trang Earnings hiển thị theo VND (định dạng vi-VN). Stripe sẽ chuyển đổi sang tiền tệ của tài khoản ngân hàng nhận nếu cần.
