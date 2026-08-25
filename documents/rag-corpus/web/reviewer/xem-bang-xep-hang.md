---
title: Xem bảng xếp hạng Reviewer
slug: xem-bang-xep-hang
platform: web
category: reviewer
tags: [reviewer, ranking, leaderboard, huy-hieu, diem]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /reviewer-dashboard/ranking
---

# Xem bảng xếp hạng Reviewer trên web

## Giới thiệu

Trang Ranking trong bảng điều khiển Reviewer giúp bạn theo dõi vị trí của mình trong tháng hiện tại so với các reviewer khác. Trang liệt kê thứ hạng, điểm, số lượt like, share, comment và khu vực địa lý của từng reviewer, đồng thời hiển thị biểu đồ so sánh điểm bằng thanh tiến trình.

## Điều kiện tiên quyết

Tài khoản của bạn cần có vai trò REVIEWER. Người dùng thường không có bảng điều khiển Reviewer.

Bạn cần đăng nhập trước khi mở trang. Dữ liệu bảng xếp hạng lấy theo kỳ tháng ("month") từ máy chủ, vì vậy cần có kết nối Internet.

## Các bước thực hiện

### Bước 1: Mở trang Ranking

Vào bảng điều khiển Reviewer, ở thanh sidebar bên trái, bấm mục "Ranking" (biểu tượng cúp). Trang sẽ mở tab con Ranking detail với hai đầu mục:

- Dòng nhãn nhỏ "Ranking detail" màu chủ đạo.
- Tiêu đề "Leaderboard and score comparison".

Trong khi dữ liệu chưa tải xong, trang hiển thị biểu tượng xoay.

### Bước 2: Đọc bảng xếp hạng rút gọn (Leaderboard Position)

Bên trái là thẻ Leaderboard Position gồm:

- Dòng "Leaderboard Position" và tiêu đề lớn "#N this month" cho biết vị trí của bạn trong tháng hiện tại. Nếu chưa có dữ liệu, dấu "-" thay cho số thứ hạng.
- Biểu tượng cúp bên phải.
- Danh sách các reviewer trong bảng xếp hạng, mỗi mục hiện thứ hạng, khu vực và điểm, cùng số lượt comment. Dòng của chính bạn được tô nền và có nhãn "You" để dễ nhận biết.

### Bước 3: Đọc thẻ Current rank và so sánh điểm

Thẻ bên phải hiển thị:

- Dòng "Current rank".
- Số thứ hạng rất lớn (ví dụ "#5") và câu chú thích: nếu bạn có trong bảng, câu này ghi "…appear in the monthly ranking."; nếu không, ghi "…do not appear in the current ranking."
- Bên dưới là dãy thanh tiến trình so sánh điểm: mỗi dòng gồm thứ hạng, thanh ngang biểu diễn tỉ lệ so với điểm cao nhất, và số điểm ở cuối.

Nhờ thanh tiến trình, bạn thấy nhanh khoảng cách giữa mình và các thứ hạng trên hoặc dưới.

### Bước 4: Xem bảng xếp hạng đầy đủ (Full leaderboard)

Cuộn xuống mục "Full leaderboard". Bảng đầy đủ có các cột:

- Rank – thứ hạng.
- Location – khu vực địa lý của reviewer.
- Score – điểm tổng.
- Likes – số lượt thích.
- Shares – số lượt chia sẻ.
- Comments – số lượt bình luận.

Nếu chưa có dữ liệu, dòng "No ranking data yet." hiện thay bảng. Dòng của chính bạn được tô nền để phân biệt với các reviewer khác. Bảng có thanh cuộn ngang khi màn hình hẹp.

### Bước 5: Đối chiếu huy hiệu và điểm

Điểm càng cao càng gần bậc huy hiệu cao hơn (IRON là bậc mặc định, DIAMOND là bậc cao nhất). Kết hợp trang này với trang Badges để biết điểm ngưỡng cho bậc kế tiếp và lên kế hoạch cải thiện.

## Xử lý lỗi thường gặp

### Trang chỉ hiện biểu tượng xoay

Đây là trạng thái loading khi hệ thống đang tải thông tin reviewer và danh sách xếp hạng. Đợi vài giây. Nếu quá 30 giây vẫn xoay, tải lại trang. Nếu tài khoản không phải reviewer, dữ liệu sẽ không nạp được – hãy đăng ký gói Reviewer Membership trước.

### Bảng hiện dòng "No ranking data yet."

Chưa có reviewer nào trong tháng hiện tại. Trường hợp này ít xảy ra – nếu gặp phải, hãy đợi qua ngày và kiểm tra lại, hoặc liên hệ bộ phận hỗ trợ.

### Số thứ hạng của bạn là "-"

Nghĩa là bạn chưa đạt điểm trong kỳ hiện tại hoặc chưa được đưa vào bảng. Tiếp tục đăng bài đánh giá và tương tác để tích điểm.

### Cột Location hiện dấu gạch ngang hoặc trống

Khu vực lấy từ thông tin vùng miền của reviewer (regionCity, regionProvince). Nếu bạn hoặc reviewer khác chưa cập nhật địa chỉ, cột này sẽ trống. Cập nhật địa chỉ trong trang chỉnh sửa hồ sơ để thông tin hiển thị đúng.

## Câu hỏi thường gặp

### Bảng xếp hạng cập nhật theo chu kỳ nào?

Trang mặc định lấy dữ liệu tháng ("month"). Số liệu cập nhật khi bạn tải lại trang hoặc khi cron backend chấm điểm lại.

### Điểm được tính như thế nào?

Điểm là tổng hợp có trọng số của lượt like, share và comment mà reviewer nhận được trong kỳ. Chi tiết công thức do backend quản lý; trang chỉ hiển thị kết quả.

### Tôi có thể lọc theo tỉnh/thành phố không?

Phiên bản hiện tại không có bộ lọc khu vực trên giao diện; bảng liệt kê toàn bộ reviewer trong kỳ. Bạn có thể quan sát cột Location để nhận biết reviewer cùng khu vực.

### Nhãn "You" trong danh sách rút gọn có ý nghĩa gì?

Nhãn "You" đánh dấu dòng của chính bạn, giúp phân biệt nhanh trong danh sách nhiều người.

### Tôi có xuất được bảng xếp hạng ra file không?

Tính năng xuất chưa được cung cấp trên giao diện. Nếu cần dữ liệu để phân tích, liên hệ bộ phận hỗ trợ.
