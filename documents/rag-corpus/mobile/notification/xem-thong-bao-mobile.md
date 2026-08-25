---
title: Xem và quản lý thông báo trên ứng dụng di động
slug: xem-thong-bao-mobile
platform: mobile
category: notification
tags: [notification, thong-bao, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Notifications
---

# Xem và quản lý thông báo trên ứng dụng di động CafeStory

## Giới thiệu

Màn hình "Notifications" trên ứng dụng di động CafeStory tổng hợp toàn bộ hoạt động liên quan tới bạn: lượt thích, bình luận, chia sẻ bài viết, tin nhắn mới, người theo dõi, tag và các cập nhật kiểm duyệt bài viết. Bạn mở màn hình này bằng cách nhấn biểu tượng chuông trên thanh tab dưới cùng của ứng dụng. Ngoài việc đọc thông báo, bạn có thể lọc theo nhóm, đánh dấu tất cả đã đọc, xoá từng thông báo và nhấn vào một dòng để đi thẳng tới bài viết, cuộc trò chuyện hoặc trang người dùng liên quan.

## Điều kiện tiên quyết

Bạn cần đã đăng nhập tài khoản CafeStory trong ứng dụng di động. Thiết bị cần có kết nối mạng để tải danh sách thông báo và số lượng chưa đọc. Nếu bạn muốn nhận thông báo đẩy ngoài ứng dụng, tài khoản phải cấp quyền notifications cho ứng dụng ở phần Cài đặt hệ thống của iOS hoặc Android.

## Các bước thực hiện

### Bước 1: Mở màn hình Notifications từ thanh tab dưới cùng

Từ bất kỳ tab nào của ứng dụng, nhấn biểu tượng chuông ở thanh tab dưới cùng để chuyển sang màn hình "Notifications". Phần đầu màn hình là header với tiêu đề "Notifications", ngay bên dưới hiển thị nhãn phụ. Nếu bạn còn thông báo chưa đọc, nhãn phụ sẽ hiển thị dạng "3 unread" tương ứng với số thông báo chưa đọc; nếu tất cả đã đọc, nhãn phụ hiển thị "All caught up".

### Bước 2: Đọc số lượng chưa đọc và đánh dấu tất cả đã đọc

Khi có thông báo chưa đọc, phía bên phải header xuất hiện nút hình tròn viền xám chứa biểu tượng "CheckCheck". Nhấn nút này để đánh dấu toàn bộ thông báo đang hiển thị là đã đọc; số "unread" sẽ giảm về không và nhãn phụ chuyển thành "All caught up". Trong lúc thao tác, ứng dụng cập nhật giao diện ngay lập tức mà không cần chờ máy chủ.

### Bước 3: Lọc thông báo bằng dải chip ngang

Ngay dưới header là dải chip lọc theo chiều ngang. Bạn có thể vuốt sang trái phải để nhìn thấy tất cả các mục "All", "Messages", "Tags", "Posts", "Follows". Nhấn một chip để chọn nhóm:

- "All" hiển thị mọi loại thông báo.
- "Messages" chỉ hiển thị các thông báo liên quan tới tin nhắn.
- "Tags" chỉ hiển thị các thông báo về việc bạn được tag trong bài viết.
- "Posts" hiển thị các thông báo về bài viết bao gồm lượt thích, bình luận, chia sẻ và cập nhật kiểm duyệt.
- "Follows" hiển thị các thông báo về người theo dõi mới.

Chip đang được chọn sẽ được tô nền theo màu chủ đạo của ứng dụng và chữ trắng, các chip còn lại có nền trắng và viền xám nhạt.

### Bước 4: Đọc chi tiết từng dòng thông báo

Mỗi thông báo là một hàng ngang gồm ba phần: một hình tròn chứa icon theo loại, khối nội dung gồm tiêu đề, mô tả và thời gian, và biểu tượng thùng rác ở cạnh phải. Icon được chọn tuỳ theo loại thông báo:

- Trái tim cho thông báo "New like".
- Bong bóng thoại cho "New comment" và "New message".
- Đánh dấu trang cho "Post shared".
- Người kèm dấu cộng cho "New follower".
- Nhãn cho "You were tagged".
- Chuông cho "Post update" khi bài viết có cập nhật kiểm duyệt.

Nếu thông báo chưa đọc, viền hàng được tô đậm theo màu tương ứng với loại thông báo và hình tròn icon được tô nền đậm với icon màu trắng. Sau khi bạn nhấn vào hàng, ứng dụng đánh dấu đã đọc và giảm bộ đếm "unread". Nhãn thời gian hiển thị dưới dạng "Just now", "5m ago", "3h ago" hoặc "2d ago".

### Bước 5: Mở nội dung liên quan tới thông báo

Khi nhấn vào một dòng, ứng dụng đọc trường điều hướng đính kèm và chuyển sang màn hình tương ứng:

- Thông báo dẫn tới bài viết mở màn hình "Blog detail" của bài viết đó.
- Thông báo dẫn tới cuộc trò chuyện mở màn hình "Chat detail" ở đúng conversation.
- Thông báo dẫn tới người dùng khác mở màn hình "Other user profile" của người đó.

Nếu thông báo không có mục tiêu điều hướng, ứng dụng chỉ đánh dấu đã đọc mà không chuyển màn hình.

### Bước 6: Xoá một thông báo cụ thể

Nhấn biểu tượng thùng rác nhỏ ở cạnh phải của hàng cần xoá. Ứng dụng loại bỏ hàng khỏi danh sách ngay lập tức và trừ bộ đếm "unread" nếu thông báo đó chưa đọc. Nếu máy chủ trả về lỗi, ứng dụng sẽ nạp lại danh sách để đồng bộ trạng thái thật.

### Bước 7: Kéo xuống để làm mới danh sách

Danh sách thông báo hỗ trợ cử chỉ pull-to-refresh: bạn kéo tay từ đỉnh danh sách xuống dưới. Vòng xoay tải sẽ xuất hiện và ứng dụng gọi lại API để lấy danh sách mới cùng số lượng chưa đọc. Đây là cách nhanh nhất để cập nhật khi bạn nghi ngờ đã có thông báo mới nhưng chưa hiển thị.

## Xử lý lỗi thường gặp

### Danh sách không tải được, hiện dòng lỗi màu đỏ nhạt

Khi xảy ra lỗi mạng, phần trên danh sách hiển thị dòng "Unable to load notifications. Pull down to try again." Bạn hãy kiểm tra kết nối Wi-Fi hoặc dữ liệu di động, sau đó kéo xuống để refresh lại danh sách.

### Nút đánh dấu tất cả đã đọc không phản hồi

Nút "CheckCheck" chỉ xuất hiện khi có ít nhất một thông báo chưa đọc và ứng dụng không đang xử lý một thao tác cập nhật khác. Nếu bạn vừa nhấn nút và ứng dụng đang gửi yêu cầu, nút sẽ tạm thời không nhận thao tác mới; hãy chờ vài giây rồi thử lại.

### Xoá thông báo nhưng nó xuất hiện lại sau khi refresh

Điều này xảy ra khi máy chủ từ chối yêu cầu xoá. Ứng dụng sẽ hiển thị dòng "Unable to delete notification." và tự động nạp lại danh sách để bạn nhìn thấy trạng thái thật. Thử xoá lại sau khi kết nối mạng ổn định.

### Nhấn vào thông báo nhưng không chuyển màn hình

Có thể thông báo không đi kèm mục tiêu điều hướng, hoặc mục tiêu bị máy chủ ẩn (ví dụ bài viết bị gỡ). Trong trường hợp này ứng dụng chỉ đánh dấu đã đọc. Nếu bạn nghĩ đây là lỗi, hãy chụp màn hình và gửi cho đội hỗ trợ.

## Câu hỏi thường gặp

### Tôi có thể tải thêm thông báo cũ hơn không

Danh sách mặc định tải trang đầu tiên với tối đa 30 thông báo mới nhất. Nếu muốn xem thông báo cũ hơn, bạn có thể chuyển bộ lọc để thu hẹp danh sách, hoặc chờ bản cập nhật hỗ trợ tải trang tiếp theo.

### Vì sao nhóm "Posts" hiển thị ít thông báo hơn tôi mong đợi

Nhóm "Posts" tổng hợp bốn loại: lượt thích, bình luận, chia sẻ và cập nhật kiểm duyệt bài viết. Các loại khác như tin nhắn hoặc người theo dõi mới sẽ không xuất hiện trong nhóm này.

### Tôi thấy nút thùng rác nhỏ, có xoá nhầm được không

Nút thùng rác nằm cạnh phải hàng và có vùng nhấn hạn chế. Sau khi bạn nhấn, thông báo được xoá ngay trên danh sách mà không có bước xác nhận trung gian. Nếu lỡ xoá, thông báo không thể khôi phục trong ứng dụng; bạn cần vào lại nội dung gốc để tương tác tiếp.

### Tại sao thời gian hiển thị đôi khi là "Just now" cho thông báo cũ

Nếu ứng dụng không đọc được thời gian tạo của thông báo, hệ thống fallback về "Just now". Kéo xuống refresh để lấy dữ liệu mới với thời gian chuẩn xác.
