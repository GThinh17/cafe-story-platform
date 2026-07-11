---
title: Xem newsfeed trang chủ
slug: xem-newsfeed
platform: web
category: home
tags: [newsfeed, home, feed, bai-viet]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /
---

# Xem newsfeed trang chủ CafeStory

## Giới thiệu

Newsfeed là trang chủ sau khi bạn đăng nhập, tập hợp các bài viết mới từ những người dùng và trang quán cà phê bạn quan tâm, kèm dải story và các khu vực khám phá phụ. Trang được thiết kế ba cột trên màn hình rộng và tự thu gọn thành một cột trên thiết bị nhỏ.

## Điều kiện tiên quyết

- Đã đăng nhập tài khoản CafeStory. Nếu chưa, feed sẽ hiển thị thông báo "Sign in to view your personalized feed." kèm lời mời đăng nhập.
- Trình duyệt hiện đại hỗ trợ cuộn mượt và IntersectionObserver để tải thêm bài theo cách vô tận.

## Các bước thực hiện

### Bước 1: Mở trang chủ

Truy cập đường dẫn "/" của CafeStory. Nếu đã đăng nhập, hệ thống tải song song bài viết trong feed, dữ liệu story và các khu vực bên phải.

### Bước 2: Quan sát bố cục ba cột

Trên màn hình từ 1280px trở lên, trang chia thành:

- Cột trái (cột nội dung chính): "Story Rail" ở trên cùng, tiếp theo là "Feed Post List" hiển thị lần lượt từng bài viết.
- Cột phải (thanh bên): "HomeAccountPanel" tóm tắt tài khoản, "TopCafesNearby" liệt kê tối đa năm trang quán tiêu biểu, và cụm liên kết chân trang "About - Help - Privacy - Terms - Locations".
- Khu vực nổi ở góc dưới phải: hộp "MessageDock" hiện tiêu đề "Messages", số tin chưa đọc và tối đa ba cuộc trò chuyện gần đây.

Trên tablet và mobile, hệ thống ẩn cột phải và MessageDock để dồn toàn bộ chiều rộng cho feed.

### Bước 3: Duyệt story theo dõi

Story Rail nằm ở đầu cột chính, chỉ hiển thị khi bạn đã đăng nhập. Xem chi tiết cách sử dụng ở tài liệu Story Rail.

### Bước 4: Đọc bài viết trong feed

Mỗi bài trong feed được hiển thị dạng thẻ, bao gồm ảnh đại diện, tên tác giả hoặc trang quán, ảnh/video đính kèm, chú thích và các nút hành động. Bấm vào tên tác giả để mở trang cá nhân, bấm vào ảnh đại diện để đi tới hồ sơ tương ứng.

### Bước 5: Tải thêm bài bằng cuộn vô tận

Cuộn xuống cuối danh sách hiện có; ngay khi vùng "sentinel" ở cuối lọt vào tầm nhìn, CafeStory tự tải thêm trang bài viết kế tiếp và nối vào cuối feed. Không cần bấm nút "Tải thêm".

### Bước 6: Xem đề xuất quán ở cột phải

Khu vực "TopCafesNearby" liệt kê các trang quán được đề xuất theo khu vực bạn đăng ký. Điểm hiển thị bên cạnh mỗi quán là chỉ số xếp hạng nội bộ; quán chưa có dữ liệu sẽ hiển thị "New".

### Bước 7: Trò chuyện nhanh từ MessageDock

Bấm vào tiêu đề "Messages" hoặc bất kỳ liên hệ nào trong MessageDock để mở khung chat tương ứng ngay tại trang chủ, không cần rời feed.

## Xử lý lỗi thường gặp

### Feed hiển thị "Unable to load your feed right now."

Đây là lỗi tạm thời từ dịch vụ feed. Tải lại trang bằng Ctrl+F5; nếu vẫn không được, thử lại sau ít phút.

### Feed hiển thị lời nhắc đăng nhập dù bạn nghĩ đã đăng nhập

Có thể phiên đăng nhập đã hết hạn. Bấm vào lời nhắn để chuyển tới trang đăng nhập và đăng nhập lại; sau đó feed sẽ tải bình thường.

### Cuộn xuống nhưng không có bài mới

Có thể bạn đã đến cuối danh sách bài viết khả dụng cho hôm nay. Feed lấy dữ liệu theo cửa sổ 24 giờ, quay lại vào ngày hôm sau để thấy nội dung mới.

### Cột phải hoặc MessageDock không xuất hiện

Cột phải và MessageDock chỉ hiển thị từ ngưỡng màn hình lớn (khoảng 1280px trở lên). Trên tablet/mobile, hai khu vực này được ẩn có chủ đích để tối ưu hiển thị feed.

## Câu hỏi thường gặp

### Vì sao thứ tự bài trong feed thay đổi khi tôi tải lại?

Thứ tự các bài trong newsfeed do bộ xếp hạng ở máy chủ quyết định theo dữ liệu tương tác và mức độ liên quan tại thời điểm truy vấn. Chi tiết cách tính điểm xếp hạng nằm trong tài liệu chính sách xếp hạng riêng, không nằm trong bài này.

### Tôi có thể tuỳ chỉnh cột phải không?

Hiện tại "HomeAccountPanel" và "TopCafesNearby" được điền tự động dựa trên tài khoản và khu vực của bạn; chưa có thao tác tuỳ chỉnh trực tiếp trên giao diện.

### Feed hiển thị nội dung bao xa về quá khứ?

Feed chính lấy các bài trong cửa sổ 24 giờ gần nhất theo thứ tự do máy chủ xếp hạng.
