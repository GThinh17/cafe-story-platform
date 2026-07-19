---
title: Khám phá và tìm kiếm quán, reviewer
slug: kham-pha-va-tim-kiem
platform: web
category: explore
tags: [explore, tim-kiem, search, cafe, reviewer, trending]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /explore
---

# Khám phá và tìm kiếm trên trang Explore

## Giới thiệu

Trang Explore giúp bạn tìm quán cà phê, reviewer nổi bật và các bài viết đang thịnh hành trên CafeStory. Trang gồm thanh tìm kiếm ở đầu, ba tab chuyển đổi nhanh và lưới nội dung tương ứng cho từng tab.

## Điều kiện tiên quyết

- Có thể mở trang Explore mà không cần đăng nhập, nhưng nút theo dõi và bình luận sẽ yêu cầu đăng nhập.
- Với người dùng đã đăng nhập, kết quả gợi ý sẽ ưu tiên theo khu vực (khu phố, thành phố, tỉnh) đã đăng ký ở hồ sơ.

## Các bước thực hiện

### Bước 1: Mở trang Explore

Truy cập đường dẫn "/explore" hoặc bấm mục "Explore" ở cuối Story Rail trên trang chủ. Trang mở với tab "Cafes" được chọn sẵn.

### Bước 2: Dùng thanh tìm kiếm

Thanh tìm kiếm ở đầu trang có placeholder "Find your next story...". Gõ từ khoá cần tìm; sau khoảng nửa giây, CafeStory tự cập nhật kết quả cho tab đang chọn mà không cần nhấn Enter. Bấm biểu tượng dấu X ở cuối ô để xoá nhanh nội dung tìm kiếm.

### Bước 3: Chuyển giữa các tab

Ba nút hình viên thuốc phía dưới thanh tìm kiếm:

- "Cafes": danh sách trang quán cà phê.
- "Reviewers": danh sách reviewer.
- "Trending": lưới bài viết đang thịnh hành.

Tab đang chọn hiển thị nền đậm; các tab còn lại có viền nhạt và đổi màu khi rê chuột.

### Bước 4: Duyệt tab "Cafes"

Kết quả hiển thị dạng lưới hai cột trên màn hình trung/rộng. Mỗi thẻ gồm ảnh đại diện quán, tên quán và thành phố (nếu có). Bấm vào thẻ để mở trang chi tiết quán.

Trên mỗi thẻ có nút "Follow"/"Following":

- Nếu bạn chưa theo dõi, nút hiển thị "Follow"; bấm để bắt đầu theo dõi ngay tại đây.
- Nếu đã theo dõi, nút hiển thị "Following"; bấm để bỏ theo dõi.

Khi không có từ khoá, hệ thống ưu tiên xếp hạng các quán theo khu vực bạn đã đăng ký; khi có từ khoá, kết quả được lọc theo tên/thông tin quán khớp từ khoá.

### Bước 5: Duyệt tab "Reviewers"

Chuyển sang tab "Reviewers" để xem các reviewer. Thẻ hiển thị tên (dạng @username hoặc họ tên đầy đủ), phù hiệu reviewer (nếu có) và tên hiển thị phía dưới. Bấm thẻ để mở trang cá nhân reviewer; bấm nút "Follow" ngay trên thẻ để theo dõi trực tiếp.

Tương tự Cafes, khi để trống thanh tìm kiếm, kết quả sẽ được gợi ý theo khu vực; khi tìm kiếm, danh sách được lọc theo từ khoá.

### Bước 6: Duyệt tab "Trending"

Chuyển sang tab "Trending" để xem lưới các bài viết đang thịnh hành trong cửa sổ 24 giờ. Bấm vào một bài để mở hộp thoại chi tiết với bình luận, thao tác thả tim và lưu bài.

### Bước 7: Theo dõi trạng thái tải

- Khi đang tải, tab Cafes và Reviewers hiển thị thẻ giữ chỗ nhấp nháy; tab Trending hiển thị ô vuông giữ chỗ dạng lưới.
- Khi không có kết quả, thông báo tương ứng ("No cafes found.", "No reviewers found.", "No trending posts") xuất hiện thay danh sách.
- Khi có lỗi mạng hoặc máy chủ, dòng thông báo lỗi ngắn hiện ra ở giữa khu vực nội dung.

## Xử lý lỗi thường gặp

### Kết quả không thay đổi sau khi gõ từ khoá

CafeStory chờ khoảng nửa giây rồi mới cập nhật URL và kết quả. Nếu sau vài giây vẫn không đổi, kiểm tra mạng và tải lại trang.

### Bấm "Follow" nhưng nút không đổi trạng thái

Có thể bạn chưa đăng nhập hoặc phiên hết hạn. Đăng nhập lại rồi thử theo dõi lần nữa.

### Tab "Trending" hiển thị "Unable to load trending posts."

Đây là lỗi tạm thời từ dịch vụ xếp hạng. Chờ ít phút rồi mở lại tab; các bài viết ở tab Trending được lấy theo cửa sổ 24 giờ và có thể cần thời gian để cập nhật.

### Muốn thấy quán/reviewer đúng khu vực nhưng vẫn thấy kết quả toàn quốc

Cập nhật địa chỉ (tỉnh/thành, quận/huyện, phường/xã) ở cài đặt cá nhân. Khi có địa chỉ, hệ thống ưu tiên gợi ý theo khu vực từ gần đến xa; nếu không có kết quả tại khu vực, mới chuyển sang danh sách xếp hạng chung.

## Câu hỏi thường gặp

### Có cần đăng nhập để dùng Explore không?

Không bắt buộc để xem, nhưng thao tác theo dõi và bình luận yêu cầu tài khoản đã đăng nhập.

### Vì sao khi tôi chuyển giữa các tab dữ liệu thỉnh thoảng được tải lại?

Tab Cafes và Reviewers tải lại mỗi khi bạn quay lại để đảm bảo hiển thị dữ liệu mới nhất theo từ khoá và khu vực; tab Trending chỉ tải trong lần đầu mở để tiết kiệm băng thông, sau đó dùng lại kết quả đã tải.

### Có thể chia sẻ đường dẫn kèm từ khoá tìm kiếm không?

Có. Từ khoá được lưu vào URL dưới dạng tham số "query", nên bạn có thể sao chép URL để chia sẻ trạng thái tìm kiếm hiện tại với người khác.
