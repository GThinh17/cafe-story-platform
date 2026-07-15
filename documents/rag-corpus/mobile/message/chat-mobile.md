---
title: Nhắn tin và mở cuộc trò chuyện trên ứng dụng di động
slug: chat-mobile
platform: mobile
category: message
tags: [message, chat, conversation, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Conversations + ChatDetail
---

# Nhắn tin và mở cuộc trò chuyện trên ứng dụng di động CafeStory

## Giới thiệu

Tính năng nhắn tin của ứng dụng di động CafeStory gồm hai màn hình chính: "Conversations" liệt kê tất cả các cuộc trò chuyện của bạn, và "ChatDetail" là nơi bạn đọc lịch sử tin nhắn và gửi tin mới. Danh sách cuộc trò chuyện hỗ trợ đồng thời chat trực tiếp giữa hai người dùng, chat nhóm và hộp thư của trang cafe khi bạn đang trong ngữ cảnh quản lý một cafe page. Màn hình chi tiết dùng bố cục danh sách đảo ngược (inverted) để tin nhắn mới nhất luôn ở cạnh khung soạn thảo phía dưới.

## Điều kiện tiên quyết

Bạn cần đã đăng nhập ứng dụng di động CafeStory với tài khoản hợp lệ. Thiết bị cần có kết nối mạng để tải danh sách và gửi tin. Nếu bạn muốn chat trong vai trò trang cafe, tài khoản của bạn phải được liên kết với một cafe page và cuộc trò chuyện phải cho phép reply với danh nghĩa trang.

## Các bước thực hiện

### Bước 1: Mở màn hình Conversations

Nhấn biểu tượng tin nhắn trên thanh tab dưới cùng của ứng dụng, hoặc truy cập màn hình "Conversations" từ menu trong hồ sơ. Phía trên cùng của màn hình là thanh "ConversationTopBar" gồm nút quay lại phía trái, tiêu đề "Messages" ở giữa và nút chỉnh sửa để tạo cuộc trò chuyện mới ở phía phải. Nếu bạn đang ở ngữ cảnh cafe page, tiêu đề chuyển thành "Tên cafe messages" và nút tạo mới không hiển thị.

### Bước 2: Tìm cuộc trò chuyện bằng thanh MessageSearch

Ngay bên dưới thanh trên cùng là ô tìm kiếm "MessageSearch". Nhập tên người, tên trang cafe hoặc trích đoạn tin nhắn để lọc danh sách. Ứng dụng lọc theo tên hiển thị, tên tài khoản và nội dung tin nhắn gần nhất mà không phân biệt chữ hoa chữ thường. Nếu không có kết quả, phần trung tâm hiển thị dòng "No conversations found" cùng gợi ý "Try another name or message."

### Bước 3: Xem dải người dùng đang online

Khi bạn không ở ngữ cảnh cafe page, ứng dụng hiển thị "OnlineUserRail" phía trên tiêu đề "Conversations". Đây là dải các avatar hình tròn của những người bạn theo dõi đang hoạt động, cuộn ngang được. Nhấn một avatar để xem hồ sơ hoặc bắt đầu cuộc trò chuyện tuỳ theo cấu hình.

### Bước 4: Đọc từng hàng ConversationRow

Danh sách chính là "FlatList" các "ConversationRow". Mỗi hàng gồm avatar hình tròn phía trái, tên hiển thị và trích đoạn tin nhắn gần nhất ở giữa, thời gian rút gọn ("5m", "3h", "2d") ở phía phải. Với chat trực tiếp giữa hai người, avatar và tên hiển thị là của đối phương. Với chat nhóm, avatar là ảnh nhóm và tên là tên nhóm. Với cuộc trò chuyện cafe page bình thường, hàng hiển thị avatar và tên trang. Trong ngữ cảnh cafe page inbox, hàng hiển thị thông tin của khách hàng đang nhắn cho trang thay vì thông tin trang.

### Bước 5: Kéo xuống để làm mới danh sách

FlatList hỗ trợ pull-to-refresh. Kéo tay từ đỉnh danh sách xuống dưới, vòng xoay hiện lên và ứng dụng gọi lại API để lấy các cuộc trò chuyện mới nhất. Nếu bạn đang ở tab thường, danh sách được nạp từ endpoint chung; nếu đang trong ngữ cảnh cafe page, danh sách được nạp riêng cho trang đó.

### Bước 6: Mở màn hình ChatDetail

Nhấn một hàng bất kỳ để chuyển sang màn hình "ChatDetail". Ở phía trên cùng của ChatDetail là "ChatDetailHeader" với nút quay lại, avatar tròn của đối phương hoặc trang, tên hiển thị và tên tài khoản. Nhấn avatar hoặc tên trong header để xem hồ sơ người dùng khác hoặc trang cafe.

### Bước 7: Đọc lịch sử tin nhắn

Bên dưới header là "FlatList" chứa các "ChatMessageBubble". Danh sách được sắp xếp sao cho tin nhắn mới nhất nằm sát khung soạn thảo phía dưới; các bong bóng cũ hơn nằm phía trên. Bong bóng tin nhắn của bạn hiển thị lệch phải theo màu chủ đạo, bong bóng của đối phương hiển thị lệch trái với nền trung tính. Ứng dụng hiển thị tự động nhãn "[image]" nếu tin nhắn chỉ chứa ảnh và "[sticker]" nếu tin nhắn là sticker. Trong khi tải, phần đầu danh sách hiển thị "Loading messages..."

### Bước 8: Nhập và gửi tin nhắn qua ChatComposer

Phía cuối màn hình là "ChatComposer" chứa ô nhập văn bản và nút gửi. Toàn bộ nội dung nằm trong "KeyboardAvoidingView"; trên iOS, hành vi được thiết lập là "padding" nên khi bàn phím bật lên khung soạn thảo tự đẩy lên trên bàn phím và không che nội dung. Nhập nội dung vào ô, nhấn nút gửi để đăng tin. Ứng dụng cắt khoảng trắng đầu cuối, không gửi tin rỗng và tạm khoá nút trong lúc chờ máy chủ. Khi máy chủ trả về thành công, tin nhắn được thêm vào cuối danh sách, ô nhập được xoá và bàn phím tự động đóng.

### Bước 9: Trả lời với danh nghĩa cafe page

Nếu bạn đang ở cuộc trò chuyện với khách hàng của trang cafe mà bạn quản lý, dòng mô tả trong phần giới thiệu hiển thị "Replying as this cafe page to this customer." Khi bạn gửi tin, ứng dụng tự đính kèm ngữ cảnh gửi là "CAFE_PAGE" cùng mã trang, để tin nhắn xuất hiện dưới tên trang thay vì tên cá nhân.

## Xử lý lỗi thường gặp

### Danh sách cuộc trò chuyện hiện "Conversations unavailable"

Đây là màn hình trống báo lỗi khi API danh sách trả về lỗi. Kiểm tra kết nối mạng và kéo xuống refresh. Nếu vẫn không tải được, hãy đăng xuất rồi đăng nhập lại.

### Gửi tin nhắn nhưng nhận dòng "Unable to send message."

Dòng cảnh báo này hiển thị ngay phía trên danh sách khi máy chủ từ chối yêu cầu gửi. Kiểm tra mạng và thử gửi lại. Nội dung bạn nhập vẫn còn trong ô soạn thảo cho tới khi máy chủ xác nhận thành công.

### Không tìm thấy cuộc trò chuyện dù đã có trong danh sách

Ô tìm kiếm chỉ lọc trên các cuộc trò chuyện đã tải xuống. Nếu bạn nghi ngờ có cuộc trò chuyện mới chưa tải, xoá nội dung tìm kiếm và kéo xuống refresh trước khi tìm lại.

### Bàn phím che khung nhập trên iOS cũ

KeyboardAvoidingView đã bật "padding" trên iOS. Nếu vẫn thấy che, hãy đóng bàn phím rồi mở lại, hoặc xoay ngang thiết bị rồi trở về dọc để buộc bố cục vẽ lại.

## Câu hỏi thường gặp

### Tôi có thể gửi ảnh hoặc sticker ngay trên di động không

Phiên bản hiện tại của ChatComposer chỉ hỗ trợ gửi văn bản. Tin nhắn có ảnh hoặc sticker do người khác gửi vẫn hiển thị được với nhãn "[image]" hoặc "[sticker]" trong bong bóng.

### Vì sao trong danh sách có một số hàng ghi "No messages yet"

Khi một cuộc trò chuyện mới được tạo mà chưa có tin nhắn nào, ứng dụng hiển thị "No messages yet" ở phần trích đoạn để bạn biết đây là cuộc trò chuyện trống.

### Làm sao mở nhanh cuộc trò chuyện từ thông báo

Từ màn hình "Notifications", nhấn thông báo có nội dung tin nhắn để đi thẳng vào ChatDetail của cuộc trò chuyện tương ứng.

### Tôi có thể xoá cuộc trò chuyện trực tiếp từ danh sách không

Phiên bản hiện tại chưa hỗ trợ vuốt để xoá trong ConversationRow. Bạn có thể ẩn hoặc xoá cuộc trò chuyện thông qua các thao tác trong hồ sơ và cài đặt tài khoản của phiên bản tiếp theo.
