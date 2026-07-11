---
title: Dashboard reviewer trên ứng dụng di động
slug: dashboard-reviewer-mobile
platform: mobile
category: reviewer
tags: [reviewer, dashboard, mobile, ranking, payout, badge]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - ReviewerDashboard
---

# Dashboard reviewer trên ứng dụng di động

## Giới thiệu

Reviewer Dashboard là màn hình dành riêng cho tài khoản có vai trò REVIEWER trên ứng dụng di động CafeStory. Không gian làm việc này tổng hợp bốn nhóm dữ liệu: profile reviewer với badge và vùng hoạt động, hiệu suất theo chu kỳ (day, week, month, 3 months), payout wallet với lịch sử chi trả và ranking theo khu vực. Ngoài ra còn có lịch sử badge và một biểu đồ điểm dạng cột đơn giản.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản đã được cấp vai trò REVIEWER (hoặc ROLE_REVIEWER). Nếu tài khoản chưa phải reviewer, màn hình vẫn mở nhưng chỉ hiển thị khối "Reviewer access required" cùng nút "Become reviewer" để chuyển sang màn hình PaymentOptions với tab reviewer đã chọn sẵn. Thiết bị cần kết nối mạng để lấy stats, ranking, payouts và badges.

## Các bước thực hiện

### Bước 1: Mở màn hình dashboard

Từ tab Profile, chạm vào chip có biểu tượng huy hiệu và chữ "Reviewer" trong hàng chip dưới tên hồ sơ, hoặc chạm biểu tượng cúp (Trophy) trong ProfileTopBar nếu bạn có quyền. Ứng dụng chuyển sang màn hình có tiêu đề "Reviewer dashboard" và phụ đề "Reviewer-only workspace". Khối skeleton loading hiển thị trong lúc chờ tải dữ liệu đầu tiên: hero, stat grid, hai panel và dòng "Loading reviewer dashboard...".

### Bước 2: Đọc khối Hero

Khối hero là thẻ màu cam đậm ở đầu màn hình. Nó gồm ảnh đại diện reviewer bên trái, dòng chữ nhỏ "Reviewer workspace" phía trên tên, tên hiển thị và dòng vùng hoạt động (area, city). Bên dưới là hai pill thông tin: badge hiện tại (iron, silver, gold,...) và ngày hết hạn "Expires ..." dựa trên trường expireDate. Hàng cuối cùng của hero hiển thị ba stat: Followers, Likes và Score.

### Bước 3: Chọn chu kỳ và xem stat tiles

Ngay dưới tiêu đề "Performance" là dải nút chuyển đổi bốn chu kỳ: "Day", "Week", "Month", "3M". Chu kỳ đang chọn có nền cam và chữ trắng. Chạm một nút khác để đổi chu kỳ; hệ thống gọi lại API stats và ranking theo chu kỳ mới. Trong khi cập nhật, phụ đề của section chuyển thành "Updating performance..." và bốn stat tiles bên dưới sẽ dùng dữ liệu mới sau khi tải xong.

Bốn stat tiles hiển thị theo lưới hai cột: "Score" (biểu tượng cột), "Likes" (biểu tượng trái tim), "Shares" (biểu tượng chia sẻ), "Comments" (biểu tượng bong bóng lời nói). Mỗi ô có biểu tượng tròn màu hồng nhạt, giá trị lớn và nhãn nhỏ bên dưới. Giá trị lớn được rút gọn dạng "1.2k" hoặc "3.4m" khi vượt ngưỡng.

### Bước 4: Xem Payout wallet

Phần "Payout wallet" hiển thị thẻ payout gần nhất trên nền cam. Thẻ có biểu tượng ví (Wallet) trong ô tròn, nhãn "Current payout" và giá trị VND định dạng theo dấu phẩy. Bên dưới là ba dòng chi tiết: Likes, Shares, Comments với số tiền tương ứng. Cuối thẻ là hàng meta gồm trạng thái payout (PENDING, PAID,...) và tổng cộng toàn thời gian "Total ...". Nếu chưa có payout, khối trạng thái hiển thị "No payout history yet" kèm mô tả gợi ý chờ kỳ payout tiếp theo hoặc thông báo quyền truy cập ví.

### Bước 5: Xem Ranking

Phần "Ranking" mở đầu bằng khối rank summary: bên trái là nhãn "Current rank" và số thứ hạng lớn "#N", bên phải là pill segment mô tả nhóm hiệu suất (inactive, new, active, strong, top, elite) suy ra từ điểm số. Danh sách năm ranking rows bên dưới hiển thị top của khu vực với số thứ hạng, badge, vùng và điểm. Hàng của bạn được tô nền hồng nhạt và chữ "You" thay vì badge. Nếu chưa có ranking, khối trạng thái "No ranking data yet" hiện lên.

### Bước 6: Xem Badges và biểu đồ điểm

Phần "Badges and activity" liệt kê lịch sử badge dạng hàng với biểu tượng cup (Award) màu hồng, tên badge và dòng "YYYY-MM - N score". Ngay dưới là "Score trend": thẻ biểu đồ cột đơn giản (không dùng thư viện) với tối đa 6 cột, mỗi cột đại diện một tháng gần nhất, chiều cao được scale từ điểm số. Nhãn tháng viết tắt (Jan, Feb,...) hiện dưới mỗi cột.

### Bước 7: Xem hoạt động gần đây

Cuối cùng là danh sách activity rows tổng hợp tự động từ stats, badge mới nhất, payout hiện tại và ranking hiện tại. Mỗi row có biểu tượng tương ứng: trái tim (Heart) cho stats, huy hiệu (BadgeCheck) cho badge, ví (Wallet) cho payout, cúp (Trophy) cho ranking, chia sẻ (Share2) cho hoạt động chia sẻ. Nếu chưa có gì, khối "No recent activity yet" hiện thay thế.

## Xử lý lỗi thường gặp

### Màn hình hiện "Reviewer access required"

Bạn chưa có vai trò REVIEWER. Chạm nút "Become reviewer" để chuyển sang PaymentOptions với tab reviewer, chọn gói và thanh toán để kích hoạt.

### Khối "Reviewer dashboard unavailable" xuất hiện

Đây là lỗi tải profile reviewer. Chạm nút "Retry" để thử tải lại. Nếu vẫn lỗi, kiểm tra mạng, đăng xuất và đăng nhập lại. Nếu tài khoản mới được cấp quyền reviewer, chờ vài phút để dữ liệu đồng bộ.

### Payout wallet báo "Payout wallet is unavailable for this account."

Máy chủ trả về mã 403 khi lấy payouts, thường vì tài khoản chưa đủ điều kiện xem ví (chưa có payout hoặc chưa hoàn tất kiểm tra). Đợi kỳ payout tiếp theo hoặc liên hệ hỗ trợ nếu bạn nghĩ đây là sai sót.

### Ranking không cập nhật khi đổi chu kỳ

Khi mạng chậm, dashboard vẫn dùng dữ liệu cũ cho tới khi tải xong. Nếu phụ đề "Updating performance..." bị treo quá lâu, quay lại rồi mở lại màn hình để tải mới.

## Câu hỏi thường gặp

### Có kéo xuống để làm mới không?

Màn hình dashboard hiện chưa hỗ trợ pull-to-refresh. Để làm mới toàn bộ, quay lại và mở lại màn hình, hoặc chạm nút "Retry" khi có lỗi.

### Vì sao segment pill của tôi là "inactive"?

Segment được suy ra từ điểm số hiện tại: 0 là "inactive", dưới 100 là "new", dưới 300 là "active", dưới 700 là "strong", dưới 1500 là "top", còn lại là "elite".

### Số tiền payout hiển thị đơn vị gì?

Toàn bộ payout hiển thị dạng "N VND" với dấu phẩy phân cách hàng nghìn, khớp với định dạng ở phía backend Việt Nam.

### Có xuất được báo cáo không?

Bản hiện tại chưa có nút xuất báo cáo trên di động. Bạn có thể xem chi tiết trên web nếu cần.
