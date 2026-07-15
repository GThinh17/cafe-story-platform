---
title: Xem trang quán trên ứng dụng di động
slug: xem-trang-quan-mobile
platform: mobile
category: cafe-page
tags: [cafe-page, quan-cafe, mobile, xem-trang]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - CafeDetail
---

# Xem trang quán trên ứng dụng di động CafeStory

## Giới thiệu

Trang quán (Cafe Page) trên ứng dụng React Native của CafeStory là màn hình chi tiết dành cho một quán cà phê cụ thể. Người dùng có thể tìm hiểu về quán, theo dõi, thả tim, mở hội thoại nhắn tin, xem các bài viết mà quán đã đăng và (trong tương lai) xem reviews, members và requests. Chủ quán (Owner) hoặc quản lý (Manager) sẽ thấy thêm các nút quản lý ở thanh trên cùng.

Tài liệu này mô tả trải nghiệm xem trang quán trên thiết bị di động: cách mở, các thành phần trong header, bốn tab nội dung và các thao tác kéo xuống để làm mới. Phần thao tác chỉnh sửa được tách sang tài liệu riêng "Chỉnh sửa trang quán trên ứng dụng di động".

## Điều kiện tiên quyết

Bạn cần đăng nhập vào tài khoản CafeStory trên ứng dụng di động. Thiết bị cần kết nối mạng để tải thông tin quán và danh sách bài viết. Nếu bạn không phải chủ quán, các nút "Follow", "Message", tim (Like) sẽ hiển thị. Nếu bạn là chủ quán hoặc có quyền quản lý, bạn sẽ thấy các nút "Edit Page", "Share Page" và biểu tượng gợi ý.

## Các bước thực hiện

### Bước 1: Mở trang quán

Có nhiều đường dẫn để mở màn hình CafeDetail. Bạn có thể chạm vào tên quán hoặc ảnh đại diện quán trên bảng tin (Home), trong kết quả tìm kiếm ở tab Explore, trong danh sách gợi ý ở màn hình Profile, hoặc từ danh sách Following của một hồ sơ khác khi họ theo dõi trang quán đó. Khi mở thành công, ứng dụng chuyển tới màn hình trang quán và bắt đầu tải dữ liệu.

Trong lúc chờ tải lần đầu, màn hình hiển thị trạng thái "Loading cafe page...". Nếu không tải được, dải lỗi màu hồng sẽ hiện ở đầu nội dung với thông báo lỗi tương ứng và bạn có thể kéo xuống để thử lại.

### Bước 2: Đọc thông tin ở CafePageHeader

CafePageHeader là khối đầu tiên hiển thị dưới thanh trên cùng. Khối này gồm ảnh bìa (cover) chiếm chiều ngang, ảnh đại diện tròn viền trắng nằm chồng lên góc dưới ảnh bìa, tên quán bên phải ảnh đại diện, và huy hiệu "Owner" hoặc "Manager" nếu bạn có quyền tương ứng.

Bên dưới tên quán là hàng số liệu gồm biểu tượng trái tim với số likes, biểu tượng hai người với số followers, và biểu tượng ngôi sao với điểm đánh giá trung bình. Tiếp theo là phần mô tả quán, hoặc dòng "No description yet." nếu chưa có mô tả. Danh sách meta phía dưới sẽ liệt kê khu vực (ward, city, province), địa chỉ đường phố, và với người có quyền quản lý còn hiển thị dòng "Active until" kèm ngày hết hạn gói.

### Bước 3: Sử dụng các nút hành động ở đầu trang

Với người dùng thông thường, hàng nút hành động dưới header có nút "Follow" (khi chưa theo dõi) hoặc "Following" (khi đã theo dõi) chiếm phần lớn chiều ngang, kế bên là nút hình bao thư (Message) để mở hội thoại với trang quán, và nút hình trái tim (Like) để bày tỏ yêu thích. Khi bạn chạm Follow hoặc Like, số đếm được cập nhật lạc quan ngay và sẽ tự khôi phục nếu máy chủ trả về lỗi.

Với chủ quán hoặc quản lý, ba nút chuyển thành "Edit Page", "Share Page" và biểu tượng tia lửa (gợi ý). Ngoài ra, thanh trên cùng của bạn sẽ có thêm hai biểu tượng: dấu cộng để mở màn hình soạn bài đăng cho trang quán và biểu tượng bao thư để mở danh sách hội thoại của trang.

### Bước 4: Chuyển đổi giữa bốn tab nội dung

Ngay dưới header là dải bốn tab: "Posts", "Reviews", "Members", "Requests". Tab đang chọn có biểu tượng đậm màu, gạch chân dưới đáy và chữ tối màu. Các tab còn lại có màu xám mờ.

Tab "Posts" hiển thị lưới bài đăng của quán (UserPostGrid). Mỗi ô là ảnh thu nhỏ của một bài đăng. Chạm vào một ô sẽ mở màn hình chi tiết bài đăng ở chế độ cuộn dọc, giữ ngữ cảnh của quán. Nếu quán chưa có bài đăng, khối trống hiển thị tiêu đề "No cafe posts yet".

Tab "Reviews" hiển thị các bài review dành cho quán khi có reviewer đăng. Nếu chưa có, khối trống ghi "No reviews yet".

Tab "Members" hiển thị danh sách thành viên của trang khi có dữ liệu. Tab "Requests" dành cho chủ quán để duyệt yêu cầu tham gia. Cả hai tab này hiện đang hiển thị trạng thái trống ban đầu, sẽ được bật khi API tương ứng sẵn sàng.

### Bước 5: Kéo xuống để làm mới

Trên vùng nội dung có thể kéo xuống (pull-to-refresh) để tải lại toàn bộ dữ liệu của trang: cả thông tin quán và danh sách bài đăng. Khi đang tải lại, vòng xoay của RefreshControl xuất hiện ở đầu ScrollView và tự động ẩn khi xong.

### Bước 6: Nhắn tin cho trang quán

Chạm biểu tượng bao thư trên thanh header (nếu bạn là quản lý) hoặc trong hàng nút hành động (nếu bạn là khách) để mở hội thoại. Nếu bạn là khách, ứng dụng tạo hội thoại trực tiếp với trang quán rồi mở màn hình ChatDetail với tên hội thoại là tên quán và avatar là ảnh đại diện của quán. Nếu bạn là quản lý, ứng dụng mở danh sách hội thoại của trang để bạn chọn khách hàng cần trả lời.

## Xử lý lỗi thường gặp

### Không tải được trang quán

Khi màn hình hiển thị khối trống "Cafe page unavailable" hoặc dải lỗi màu ở đầu, hãy kéo xuống để thử lại. Nếu vẫn không được, kiểm tra kết nối mạng, mở lại ứng dụng, và thử vào lại từ một đường dẫn khác (ví dụ mở trực tiếp từ hồ sơ chủ quán).

### Nút Follow hoặc Like bị treo

Khi mạng chậm, nút có thể tạm chuyển sang trạng thái mờ (disabled). Chờ vài giây để hoàn tất. Nếu số đếm bị bật ngược trở lại giá trị cũ, nghĩa là máy chủ đã từ chối yêu cầu và ứng dụng đã tự hoàn tác. Kiểm tra dải lỗi ở đầu để biết chi tiết và thử lại.

### Không thấy nút quản lý dù bạn là chủ quán

Đảm bảo bạn đang đăng nhập bằng đúng tài khoản đã tạo quán. Kéo xuống làm mới trang. Nếu vẫn không thấy, có thể quyền quản lý đang được cập nhật, hãy thử đăng xuất và đăng nhập lại.

## Câu hỏi thường gặp

### Số followers cập nhật ngay khi tôi bấm Follow đúng không?

Đúng. Ứng dụng cập nhật lạc quan ngay khi bạn bấm và chỉ hoàn tác nếu máy chủ báo lỗi.

### Tôi có thể chỉnh sửa trang ngay tại đây không?

Có, nếu bạn là chủ quán. Chạm "Edit Page" để mở EditCafePageModal. Chi tiết xem tài liệu "Chỉnh sửa trang quán trên ứng dụng di động".

### Vì sao tab Members và Requests hiển thị trống?

Hai tab này đang ở trạng thái sẵn sàng cho API. Khi tính năng được bật ở phía máy chủ, danh sách thành viên và yêu cầu tham gia sẽ hiển thị tự động.
