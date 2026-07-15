---
title: Bảng điều khiển Reviewer
slug: dashboard-reviewer
platform: web
category: reviewer
tags: [reviewer, dashboard, overview, thong-ke, huy-hieu, thu-nhap]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /reviewer-dashboard
---

# Bảng điều khiển Reviewer trên web

## Giới thiệu

Bảng điều khiển Reviewer là không gian làm việc riêng cho reviewer, tập trung mọi thông tin về hiệu suất, thứ hạng, huy hiệu và thu nhập. Trang tổng quan hiển thị nhiều khối panel cùng lúc để bạn nắm nhanh tình hình tháng hiện tại, còn thanh sidebar giúp chuyển sang các trang chi tiết khi cần đào sâu.

## Điều kiện tiên quyết

Tài khoản của bạn phải có vai trò REVIEWER (đăng ký qua gói Reviewer Membership hoặc do quản trị viên gán). Nếu tài khoản chưa được gán reviewer, các panel sẽ trống hoặc không tải được dữ liệu.

Trước khi mở trang, bạn cần đăng nhập trên trình duyệt. Trang sử dụng thông tin phiên hiện tại (hook use-current-user) để xác định reviewer tương ứng.

## Các bước thực hiện

### Bước 1: Mở bảng điều khiển Reviewer

Có ba cách để truy cập:

- Mở menu Cài đặt hồ sơ ở trang cá nhân rồi chọn "Reviewer dashboard".
- Nhập trực tiếp đường dẫn bảng điều khiển vào thanh địa chỉ trình duyệt.
- Ở thanh sidebar bên trái của bảng điều khiển, chọn mục "Overview" nếu bạn đang ở tab con khác.

Khi vào lần đầu, hệ thống sẽ hiện biểu tượng xoay đợi trong khi nạp dữ liệu reviewer, bảng xếp hạng, huy hiệu, thống kê và lịch sử chi trả.

### Bước 2: Đọc thanh sidebar bên trái

Sidebar cố định bên trái hiển thị:

- Logo CafeStory cùng dòng chữ "Reviewer workspace".
- Nút "Home" để quay về trang chủ.
- Thẻ hồ sơ tóm tắt: ảnh đại diện, tên hiển thị và huy hiệu hiện tại (ReviewerBadgeChip).
- Danh sách liên kết điều hướng: "Overview", "Performance", "Ranking", "Badges", "Earnings".
- Ô nhắc nhở "Keep reviewing" ở cuối cùng.

Mục nào đang được chọn sẽ tô nền và chữ theo màu chủ đạo. Bấm vào mỗi liên kết để mở trang chi tiết tương ứng.

### Bước 3: Xem tổng quan (Overview)

Tab Overview gồm nhiều panel xếp theo hàng dọc:

- Hàng đầu tiên là hàng nút chuyển kỳ (day, week, month, 3months) và trạng thái Stripe Connect (nút "Connect Stripe Account" nếu chưa liên kết, hoặc nhãn "Payouts enabled" nếu đã liên kết xong).
- ReviewerStatsCards – bốn thẻ số liệu Likes, Shares, Comments, Engagement Score cho kỳ đang chọn, cùng dòng phụ "Current [kỳ] period".
- ReviewerPerformanceChart – biểu đồ hiệu suất theo tháng, dựng từ danh sách huy hiệu (badges) đã đạt.
- ReviewerRecentActivityPanel – nhật ký hoạt động gần đây: huy hiệu mới nhất, đợt chi trả gần nhất và lượt tương tác trong kỳ.
- ReviewerSegmentCard – phân khúc của bạn dựa trên vị trí trong bảng xếp hạng (elite, top, strong, active, new hoặc inactive).
- ReviewerLeaderboardPanel – rút gọn bảng xếp hạng tháng cùng vị trí hiện tại (dòng "#N this month").
- ReviewerBadgeProgress – tiến độ điểm để tiến lên huy hiệu tiếp theo.
- ReviewerPayoutPanel – hiển thị đợt chi trả gần nhất và bảng lịch sử ngắn gọn (chỉ hiện khi có dữ liệu payout).

### Bước 4: Đổi kỳ thống kê

Ở hàng nút phía trên, nhấp vào một trong bốn kỳ: "day", "week", "month" hoặc "3months". Bảng ReviewerStatsCards sẽ nạp lại từ máy chủ theo kỳ đã chọn. Nút đang chọn nổi bật, các nút còn lại có viền mờ.

### Bước 5: Chuyển sang tab chi tiết

Bấm vào các mục trên sidebar để mở trang chi tiết tương ứng:

- "Performance" – biểu đồ và bảng chi tiết hiệu suất theo tháng.
- "Ranking" – bảng xếp hạng đầy đủ và so sánh điểm với các reviewer khác.
- "Badges" – toàn bộ huy hiệu bạn đã đạt cùng tiến độ leo hạng.
- "Earnings" – chi tiết thu nhập, phân bổ theo Like / Share / Comment và toàn bộ lịch sử payout.

Mỗi tab con có tiêu đề riêng và bố cục riêng, chia sẻ chung sidebar và thanh header.

## Xử lý lỗi thường gặp

### Trang chỉ hiện biểu tượng xoay và không nạp dữ liệu

Đợi vài giây để dữ liệu tải xong. Nếu quá 30 giây vẫn chỉ có biểu tượng xoay, tải lại trang (Ctrl+F5). Nếu vẫn không xong, tài khoản có thể chưa được đăng ký reviewer – đăng ký gói Reviewer Membership trước.

### Panel Payout hoàn toàn không hiển thị

Panel Payout chỉ hiện khi có ít nhất một đợt chi trả. Reviewer mới thường chưa có payout nào, nên panel sẽ bị ẩn. Sau khi hoàn thành chu kỳ chi trả đầu tiên, panel sẽ tự xuất hiện.

### Biểu đồ hiệu suất báo "No performance data yet"

Bạn cần có ít nhất một huy hiệu tháng để biểu đồ có điểm dữ liệu. Tiếp tục đăng bài đánh giá và tương tác để hoàn thành chu kỳ tháng đầu tiên.

### Nhãn "Payouts enabled" chưa xuất hiện dù đã hoàn tất Stripe

Trạng thái phụ thuộc vào trường onboardingStatus (phải bằng COMPLETE) và payoutsEnabled (phải bằng true). Sau khi hoàn tất Stripe onboarding, hệ thống tự đồng bộ khi bạn quay về trang. Nếu chưa cập nhật, tải lại trang một lần.

## Câu hỏi thường gặp

### Dữ liệu trên bảng điều khiển cập nhật thời gian thực không?

Các con số nạp mỗi khi bạn mở trang hoặc đổi kỳ thống kê. Chưa có cập nhật thời gian thực – muốn xem số liệu mới, hãy tải lại trang.

### Tôi có thể xem hồ sơ reviewer công khai của mình từ đây không?

Từ sidebar, bấm "Home" để về trang chủ rồi truy cập trang cá nhân của bạn để xem hồ sơ công khai. Bảng điều khiển chỉ hiển thị số liệu nội bộ dành cho reviewer.

### Vì sao phần Segment của tôi ghi "new" dù đã đăng nhiều bài?

Phân khúc "new" áp dụng khi bạn chưa nằm trong bảng xếp hạng tháng hoặc điểm còn thấp. Duy trì đăng bài chất lượng và thu hút tương tác để bước vào các phân khúc cao hơn (active, strong, top, elite).

### Tôi thấy nút "Connect Stripe Account" – đó là gì?

Đây là bước liên kết tài khoản Stripe Connect để nhận chi trả. Xem bài hướng dẫn "Rút tiền qua Stripe" để biết chi tiết cấu hình.
