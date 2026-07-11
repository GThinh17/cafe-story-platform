---
title: Xem trang quán cà phê
slug: xem-trang-quan
platform: web
category: cafe-page
tags: [cafe-page, xem-quan, review, menu, ban-do]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /cafes/[id]
---

# Xem trang quán cà phê trên web

## Giới thiệu

Trang quán cà phê là nơi tập trung thông tin về một quán trên CafeStory: ảnh bìa, ảnh đại diện, đánh giá trung bình, địa chỉ, giờ mở cửa, danh sách bài đăng gần đây và menu. Khách hàng có thể xem bài viết của khách khác, mở menu, xem quán trên bản đồ, hoặc bấm nút hành động để theo dõi và thích quán.

## Điều kiện tiên quyết

Bạn không cần đăng nhập để xem thông tin cơ bản của quán. Tuy nhiên, để thích, theo dõi hoặc để lại bình luận cho bài đăng của quán, bạn cần đăng nhập tài khoản CafeStory. Trang quán chỉ hiển thị khi gói dịch vụ của quán còn hiệu lực; nếu gói đã hết hạn, bạn sẽ thấy hộp thoại "Cafe page expired" thay vì nội dung quán.

## Các bước thực hiện

### Bước 1: Mở trang quán từ liên kết hoặc kết quả tìm kiếm

Bấm vào tên quán trong danh sách gợi ý, feed hoặc kết quả tìm kiếm để chuyển đến trang chi tiết của quán. Đường dẫn có dạng `/cafes/<mã quán>`. Khi trang đang tải, bạn sẽ thấy các khối trống (skeleton) ở vị trí ảnh bìa, ảnh đại diện, tên và các nút hành động.

### Bước 2: Xem khu vực đầu trang

Phía trên cùng của trang là ảnh bìa (cover) rộng, phía dưới là ảnh đại diện tròn (avatar) của quán. Bên phải avatar là tên quán, dòng thông tin nhanh gồm sao đánh giá, loại quán và trạng thái. Ví dụ: nếu quán đang hoạt động bạn sẽ thấy "Active - Active page"; nếu quán đã ngưng thì thấy "Inactive". Nếu quán chưa có điểm đánh giá, cột sao sẽ hiển thị "New" thay vì số cụ thể.

Ngay dưới đó là địa chỉ đầy đủ của quán (nếu quán đã điền). Địa chỉ được ghép từ số nhà, phường, khu vực, thành phố và tỉnh mà chủ quán đã khai báo.

### Bước 3: Sử dụng các nút hành động

Bên phải khối tên quán có ba nút hành động:

- Nút trái tim: thích quán, kèm số lượt thích hiển thị dạng rút gọn (ví dụ 1.2K).
- Nút Follow: theo dõi quán để nhận cập nhật.
- Nút "View Menu": mở hộp thoại xem thực đơn của quán.

Chi tiết cách dùng ba nút này được mô tả trong tài liệu "Theo dõi và đánh giá quán".

### Bước 4: Xem thẻ "Vibe & Features" và giờ mở cửa

Bên dưới khu vực đầu trang có hai thẻ nằm cạnh nhau:

- Thẻ "Vibe & Features": liệt kê các đặc điểm nổi bật của quán dưới dạng huy hiệu tròn (ví dụ tên thành phố, khu vực, số người theo dõi, số lượt thích) và một đoạn mô tả ngắn.
- Thẻ "Opening Hours": liệt kê giờ mở cửa theo từng khoảng ngày, ngày có chữ đậm là ngày cao điểm (thông thường là cuối tuần). Cuối thẻ có dòng gợi ý khung giờ đông khách nhất.

### Bước 5: Xem bài đăng gần đây (Recent Posts)

Phía dưới là mục "Recent Posts" liệt kê bài viết gần đây liên quan tới quán dưới dạng lưới 3 cột. Mỗi thẻ bài viết có:

- Ảnh chính của bài (ảnh vuông).
- Huy hiệu điểm đánh giá ở góc phải trên (từ 1 đến 5).
- Ảnh đại diện và tên của người viết, kèm địa điểm hoặc thời gian.
- Đoạn trích caption.
- Bốn nút: Like (trái tim), Comment (bong bóng), Share (mũi tên vòng), Bookmark (dấu trang).

Nếu chưa có bài nào, mục này sẽ hiển thị dòng "No cafe posts yet". Khi còn dữ liệu, nút "Load more posts" ở cuối danh sách cho phép tải thêm bài.

### Bước 6: Mở chế độ xem bản đồ (Map View)

Ở đầu mục "Recent Posts" có hai nút nhỏ ở góc phải: "Feed" (đang chọn mặc định) và "Map View". Bấm "Map View" để mở hộp thoại bản đồ xem vị trí quán dựa trên địa chỉ đã lưu.

### Bước 7: Mở menu quán

Trở lại khu vực đầu trang, bấm nút "View Menu" để mở hộp thoại thực đơn. Bạn có thể lướt xem các món và giá tham khảo mà chủ quán đã khai báo.

## Xử lý lỗi thường gặp

### Trang hiển thị "Cafe not found"

Mã quán trong đường dẫn có thể sai hoặc quán đã bị xoá. Hãy kiểm tra lại liên kết hoặc quay về trang chủ để tìm lại quán.

### Trang hiển thị "Unable to load cafe page"

Kết nối mạng không ổn định hoặc máy chủ tạm thời không phản hồi. Tải lại trang (Ctrl+F5) sau vài giây. Nếu vẫn lỗi, thử mở lại sau ít phút.

### Xuất hiện hộp thoại "Cafe page expired"

Gói dịch vụ của quán đã hết hạn nên trang tạm ẩn với khách. Bấm "Go back" để quay về trang trước. Nếu bạn là chủ quán, xem tài liệu "Gia hạn gói Cafe Page" để mở gói mới.

### Không thấy nút bánh răng "Cafe page settings"

Nút bánh răng chỉ hiển thị cho chủ quán. Nếu bạn không phải chủ, nút này sẽ không xuất hiện cạnh tên quán.

### Danh sách bài đăng trống dù quán đã hoạt động

Có thể quán chưa đăng bài nào hoặc bài đang ở trạng thái nháp/đang duyệt. Bạn có thể theo dõi quán để được thông báo khi có bài mới.

## Câu hỏi thường gặp

### Tôi có phải đăng nhập để xem trang quán không?

Không bắt buộc. Ai cũng xem được thông tin cơ bản, giờ mở và bài đăng. Nhưng để thích, theo dõi, bình luận hay đăng bài review, bạn cần có tài khoản.

### Điểm sao bên cạnh tên quán được tính thế nào?

Điểm là trung bình các bài review đã duyệt của quán, làm tròn tới 1 chữ số. Quán chưa có review sẽ hiển thị chữ "New".

### Giờ mở cửa hiển thị lấy từ đâu?

Từ thông tin do chủ quán khai báo. Nếu chủ quán chưa cập nhật, hệ thống dùng khung giờ mẫu mặc định.

### Vì sao có bài đăng của người dùng chứ không phải chỉ của chủ quán?

Mục "Recent Posts" tổng hợp tất cả bài review có gắn quán này. Bạn sẽ thấy cả bài của chủ quán và bài của khách hàng đã đến quán.

### Tôi có thể chia sẻ trang quán cho bạn bè không?

Có. Sao chép đường dẫn trên thanh địa chỉ trình duyệt (dạng `/cafes/<mã quán>`) và gửi cho bạn.
