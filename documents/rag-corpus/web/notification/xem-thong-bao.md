---
title: Xem và quản lý thông báo
slug: xem-thong-bao
platform: web
category: notification
tags: [thong-bao, notification, moderation, hoat-dong]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /notifications
---

# Xem và quản lý thông báo trên web

## Giới thiệu

Trang Notifications hiển thị mọi hoạt động liên quan tới tài khoản của bạn: người khác theo dõi, thích, bình luận, chia sẻ hoặc gắn thẻ (tag) bạn trong bài viết; tin nhắn mới trong hộp thư; và kết quả kiểm duyệt (moderation) đối với các bài đăng bạn đã tạo. Trang được chia thành hai khu vực theo thời gian là "This month" (trong tháng này) và "Earlier" (trước đó), giúp bạn nhanh chóng nắm bắt các hoạt động gần nhất.

Mỗi thông báo hiển thị ảnh đại diện của người thực hiện hành động, nội dung tóm tắt và thời gian tương đối (ví dụ 5m, 3h, 2d). Thông báo chưa đọc được đánh dấu bằng nền hơi đậm hơn và một chấm tròn nhỏ màu chủ đạo ở cuối dòng.

## Điều kiện tiên quyết

Bạn cần đăng nhập tài khoản CafeStory. Trình duyệt cần bật JavaScript để tải danh sách hoạt động và cho phép mở các trang liên quan khi bạn nhấp vào từng dòng thông báo.

## Các bước thực hiện

### Bước 1: Mở trang Notifications

Nhấp vào biểu tượng chuông trên thanh điều hướng bên trái (hoặc mở trực tiếp đường dẫn `/notifications`). Trang sẽ tải toàn bộ hoạt động và hiển thị số lượng thông báo chưa đọc ở góc trên bên trái.

### Bước 2: Lọc thông báo theo loại

Ngay dưới tiêu đề "Notifications" là dải các thẻ lọc dạng nút bo tròn. Nhấp vào từng thẻ để chỉ hiển thị đúng loại thông báo bạn quan tâm. Các lựa chọn có sẵn gồm:

- **All**: hiển thị toàn bộ hoạt động.
- **Follows**: người khác bắt đầu theo dõi bạn.
- **Comments**: bình luận trên bài viết của bạn.
- **Likes**: lượt thích bài viết của bạn.
- **Shares**: lượt chia sẻ bài viết của bạn.
- **Messages**: tin nhắn mới trong hộp thư.
- **Tags**: bạn được gắn thẻ trong bài viết của người khác.
- **Moderation**: kết quả kiểm duyệt bài đăng.

Nếu dải thẻ dài hơn khung hiển thị, hai nút mũi tên trái/phải sẽ hiện ra để bạn cuộn ngang.

### Bước 3: Đánh dấu đã đọc toàn bộ

Khi có ít nhất một thông báo chưa đọc, nút "Mark all as read" xuất hiện ở góc trên bên phải. Nhấp vào nút này để đưa toàn bộ danh sách về trạng thái đã đọc, các chấm tròn màu và nền đậm sẽ được gỡ bỏ.

### Bước 4: Mở nội dung của một thông báo

Nhấp vào một dòng thông báo bất kỳ. Hệ thống tự động đánh dấu dòng đó là đã đọc và điều hướng bạn tới trang tương ứng:

- Thông báo **Messages** mở hộp thoại chat với đúng cuộc trò chuyện liên quan.
- Thông báo **Likes**, **Shares**, **Comments**, **Tags** mở cửa sổ chi tiết của bài viết được nhắc tới, sẵn sàng để bạn phản hồi.
- Thông báo **Follows** mở trang cá nhân của người vừa theo dõi bạn.

### Bước 5: Xem lý do kiểm duyệt bài đăng

Khi có kết quả kiểm duyệt, bạn sẽ thấy một dòng với biểu tượng khiên ở vị trí ảnh đại diện. Trạng thái có thể là:

- Bài đăng của bạn đã được duyệt.
- Bài đăng của bạn đã bị từ chối.
- Bài đăng của bạn đang chờ admin xem xét.

Nhấp vào dòng đó để mở hộp thoại chi tiết. Hộp thoại hiển thị tiêu đề theo trạng thái ("Bài đăng đã được duyệt", "Bài đăng đã bị từ chối" hoặc "Bài đăng cần admin xem xét") kèm lý do cụ thể do hệ thống hoặc quản trị viên đưa ra. Nhấp "Đã hiểu" để đóng hộp thoại.

## Xử lý lỗi thường gặp

### Danh sách hiển thị "No notifications yet."

Đây là trạng thái bình thường khi bạn chưa có hoạt động nào. Hệ thống gợi ý theo dõi thêm người dùng khác để nhận cập nhật về bài đăng và hoạt động của họ.

### Không tải được thông báo

Nếu trang hiển thị dòng lỗi thay cho danh sách, hãy kiểm tra kết nối mạng và tải lại trang. Nếu vẫn không nhận thông báo mới, thử đăng xuất rồi đăng nhập lại để làm mới phiên.

### Nhấp vào thông báo nhưng không mở được nội dung

Với thông báo dạng **Follows**, nếu tên người dùng của người theo dõi bạn bị thay đổi hoặc tài khoản không còn hoạt động, trang cá nhân có thể không mở được. Với thông báo bài viết, nếu bài viết đã bị gỡ, cửa sổ bình luận sẽ không hiện. Hãy vào trực tiếp trang hồ sơ hoặc bảng tin để kiểm tra.

## Câu hỏi thường gặp

### Vì sao một số thông báo vẫn còn chấm tròn màu dù tôi đã bấm vào?

Trạng thái đã đọc được lưu trên máy chủ. Nếu thao tác đánh dấu bị gián đoạn (mất mạng ngay lúc nhấp), hãy tải lại trang và nhấp lại vào dòng đó, hoặc dùng nút "Mark all as read".

### Tôi có thể tắt một loại thông báo cụ thể không?

Trang Notifications hiện chỉ cho phép lọc hiển thị, không tắt việc nhận thông báo. Nếu bạn không muốn xem một loại nào đó, dùng bộ lọc phía trên để ẩn khỏi khung nhìn hiện tại.

### Vì sao thông báo kiểm duyệt không kèm ảnh đại diện?

Kiểm duyệt do hệ thống hoặc quản trị viên thực hiện, không gắn với một người dùng cụ thể. Vì vậy dòng thông báo dùng biểu tượng khiên thay cho ảnh đại diện: khiên xanh cho bài đã duyệt, khiên hổ phách cho bài bị từ chối hoặc chờ xem xét.
