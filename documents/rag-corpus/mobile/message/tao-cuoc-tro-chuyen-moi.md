---
title: Tạo cuộc trò chuyện mới trên ứng dụng di động
slug: tao-cuoc-tro-chuyen-moi
platform: mobile
category: message
tags: [message, new-chat, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - NewChat
---

# Tạo cuộc trò chuyện mới trên ứng dụng di động CafeStory

## Giới thiệu

Màn hình "New message" trên ứng dụng di động CafeStory giúp bạn bắt đầu một cuộc trò chuyện trực tiếp với người khác. Ứng dụng gợi ý danh sách những người bạn đang theo dõi ở phía trên và một khối "Discover people" phía dưới với các tài khoản được đề xuất mà bạn chưa theo dõi. Khi bạn chọn một người, ứng dụng sẽ tạo cuộc trò chuyện trực tiếp giữa hai bên và mở ngay màn hình ChatDetail để bạn nhắn tin.

## Điều kiện tiên quyết

Bạn cần đã đăng nhập ứng dụng CafeStory. Để danh sách gợi ý có nội dung, tài khoản cần có ít nhất vài người đang theo dõi hoặc có gợi ý đề xuất từ hệ thống. Thiết bị cần có kết nối mạng để gọi các API danh sách theo dõi, đề xuất người dùng và tạo cuộc trò chuyện.

## Các bước thực hiện

### Bước 1: Mở màn hình New message

Từ màn hình "Conversations", nhấn nút chỉnh sửa ở góc phải của thanh trên cùng "ConversationTopBar". Ứng dụng chuyển sang màn hình "New message" với thanh trên cùng gồm nút quay lại và tiêu đề "New message". Bên dưới là ô nhập gồm nhãn "To:" ở cạnh trái và ô nhập "Search" ở cạnh phải.

### Bước 2: Nhập từ khoá vào ô tìm kiếm

Nhấn vào ô "Search" và nhập tên hoặc tên tài khoản của người bạn muốn nhắn. Ô tìm kiếm có chế độ auto-capitalize tắt để bạn có thể nhập chính xác username, không bị viết hoa chữ đầu tự động. Ứng dụng lọc song song hai danh sách gợi ý: "Suggestions" phía trên là những người bạn đang theo dõi, "Discover people" phía dưới là danh sách đề xuất từ hệ thống. Bộ lọc so khớp cả tên đầy đủ và tên tài khoản, không phân biệt chữ hoa chữ thường.

### Bước 3: Xem danh sách Suggestions

Ngay dưới ô tìm kiếm là tiêu đề mục "Suggestions". Danh sách này chứa các "NewChatSuggestionRow" cho những người bạn đang follow. Mỗi hàng gồm avatar hình tròn, tên đầy đủ và tên tài khoản. Nếu bạn chưa follow ai, ứng dụng hiển thị màn hình trống với tiêu đề "No suggestions yet" và mô tả "Follow people first to start a direct chat."

### Bước 4: Xem danh sách Discover people

Bên dưới danh sách Suggestions, khi hệ thống có gợi ý mở rộng, ứng dụng vẽ thêm khối "Discover people" ngăn cách bằng một đường kẻ mảnh phía trên. Khối này chứa các tài khoản được đề xuất mà bạn chưa follow và cũng khác với tài khoản của chính bạn. Hàng hiển thị giống mục Suggestions để trải nghiệm nhất quán.

### Bước 5: Chọn một người để bắt đầu trò chuyện

Nhấn hàng của người bạn muốn nhắn. Ứng dụng gửi yêu cầu tạo direct conversation với người đó. Trong lúc chờ máy chủ, toàn bộ các hàng gợi ý bị tạm khoá thao tác để bạn không tạo nhiều cuộc trò chuyện cùng lúc. Khi thành công, ứng dụng thay thế màn hình "New message" bằng "ChatDetail" của cuộc trò chuyện vừa tạo; nếu bạn nhấn quay lại, bạn sẽ trở về màn hình "Conversations" chứ không phải màn hình chọn người nữa.

### Bước 6: Tiếp tục nhắn tin trong ChatDetail

Trong màn hình "ChatDetail" vừa mở, tên và avatar của người nhận đã được điền sẵn từ thông tin của họ. Bạn có thể nhập nội dung vào ChatComposer và gửi tin nhắn đầu tiên như hướng dẫn trong tài liệu "Nhắn tin và mở cuộc trò chuyện trên ứng dụng di động".

## Xử lý lỗi thường gặp

### Dòng cảnh báo màu đỏ "Unable to load suggestions."

Nếu danh sách theo dõi hoặc danh sách đề xuất không tải được, ứng dụng hiển thị dòng cảnh báo màu đỏ ngay phía trên vùng danh sách. Kiểm tra kết nối mạng và quay lại màn hình sau vài giây. Bạn cũng có thể nhấn nút quay lại rồi mở lại màn hình "New message" để thử lại từ đầu.

### Nhấn một người nhưng không mở được cuộc trò chuyện

Nếu tạo cuộc trò chuyện lỗi, ứng dụng hiển thị dòng "Unable to start chat." trong khối cảnh báo và mở khoá lại các hàng gợi ý. Bạn có thể thử nhấn lại người khác hoặc quay ra để kiểm tra mạng.

### Không tìm thấy ai với từ khoá vừa nhập

Khi cả hai danh sách bị lọc rỗng và ô tìm kiếm không rỗng, ứng dụng hiển thị màn hình trống với tiêu đề "No people found" và mô tả "Try another name or username." Kiểm tra chính tả hoặc thử tên tài khoản viết liền không dấu cách.

### Danh sách Suggestions trống dù đã follow nhiều người

Đảm bảo bạn đăng nhập đúng tài khoản. Nếu bạn vừa follow ai đó trên nền web nhưng chưa refresh danh sách trên di động, quay lại "Conversations" rồi mở lại "New message" để nạp lại API.

## Câu hỏi thường gặp

### Tôi có thể tạo nhóm chat mới từ màn hình này không

Màn hình "New message" hiện tại chỉ tạo cuộc trò chuyện trực tiếp giữa hai người dùng. Chức năng tạo nhóm chat sẽ được bổ sung ở bản cập nhật sau.

### Vì sao cùng một người vừa xuất hiện trong Suggestions vừa trong Discover people

Ứng dụng đã lọc để tài khoản bạn đang follow không xuất hiện trong "Discover people". Nếu bạn vẫn thấy trùng, có thể danh sách theo dõi vừa được cập nhật ở nơi khác; quay lại và mở lại màn hình để đồng bộ.

### Nếu tôi đã có cuộc trò chuyện với người này trước đây, ứng dụng có tạo cuộc trò chuyện mới không

API tạo direct conversation trên máy chủ trả về cuộc trò chuyện hiện có nếu đã tồn tại thay vì tạo mới. Vì vậy khi bạn chọn một người mà đã từng nhắn, ứng dụng chỉ mở lại đúng cuộc trò chuyện cũ trong ChatDetail.

### Tôi có thể vào thẳng ChatDetail từ hồ sơ người dùng khác không

Có. Trên "Other user profile" bạn có thể tìm nút nhắn tin để mở nhanh, không cần đi qua màn hình "New message". Màn hình này phù hợp khi bạn không nhớ hồ sơ và muốn duyệt danh sách theo dõi hoặc gợi ý.
