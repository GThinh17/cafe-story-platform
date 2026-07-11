---
title: Nhắn tin với người dùng khác
slug: chat-voi-nguoi-dung
platform: web
category: message
tags: [tin-nhan, chat, message, realtime, cloudinary]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /messages
---

# Nhắn tin với người dùng khác trên web

## Giới thiệu

Trang Messages là không gian trò chuyện một-một giữa bạn và những người dùng CafeStory khác. Giao diện chia thành hai cột: bên trái là danh sách cuộc trò chuyện (Conversation List) hiển thị tên người tham gia, ảnh đại diện và đoạn nội dung xem trước của tin nhắn mới nhất; bên phải là khung chat (Chat Panel) chứa các bong bóng tin nhắn của cuộc trò chuyện đang mở.

Hệ thống hỗ trợ gửi văn bản, chèn biểu tượng cảm xúc từ bảng emoji nhanh, và gửi kèm một ảnh cho mỗi tin nhắn. Tin nhắn từ đối phương được cập nhật gần như tức thời thông qua kết nối WebSocket theo giao thức STOMP, không cần bạn tải lại trang.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản CafeStory. Để bắt đầu một cuộc trò chuyện mới, bạn cần theo dõi (follow) người đó hoặc mở trang cá nhân của họ. Khi gửi ảnh, ứng dụng chỉ chấp nhận file thuộc kiểu ảnh và tải lên qua Cloudinary trước khi gắn vào tin nhắn.

## Các bước thực hiện

### Bước 1: Mở trang Messages

Nhấp vào biểu tượng tin nhắn trên thanh điều hướng để mở đường dẫn `/messages`. Cột trái sẽ tải danh sách cuộc trò chuyện của bạn cùng những người bạn đang theo dõi (dùng để bắt đầu cuộc trò chuyện mới). Trợ lý CafeStory luôn được ghim ở đầu danh sách, phía dưới là các cuộc trò chuyện với người dùng thật.

### Bước 2: Chọn hoặc mở một cuộc trò chuyện

Nhấp vào một dòng bất kỳ trong Conversation List để mở cuộc trò chuyện tương ứng ở khung bên phải. Nếu bạn chọn một người mà chưa từng nhắn tin trước đó, hệ thống sẽ tự tạo cuộc trò chuyện mới ở trạng thái "Opening..." và chuyển sang trạng thái sẵn sàng khi máy chủ xác nhận.

Bạn cũng có thể mở nhanh cuộc trò chuyện từ trang cá nhân của người dùng bằng nút chat trên profile, hoặc từ một thông báo dạng Messages trong trang Notifications.

### Bước 3: Gửi tin nhắn văn bản

Nhập nội dung vào ô "Message..." ở đáy khung chat và nhấn Enter hoặc nhấp nút gửi (biểu tượng máy bay giấy). Tin nhắn xuất hiện ngay bên phải khung chat ở trạng thái đang gửi (biểu tượng vòng xoay nhỏ), rồi chuyển thành đã gửi khi máy chủ xác nhận. Nếu muốn chèn biểu tượng cảm xúc, nhấp vào nút mặt cười để mở bảng emoji nhanh và chọn.

### Bước 4: Gửi tin nhắn kèm ảnh

Nhấp vào biểu tượng ảnh bên trái ô nhập liệu để mở hộp chọn file. Sau khi chọn ảnh, một khung xem trước hiện phía trên form nhập, gồm ảnh thu nhỏ, tên file và nút X để loại bỏ. Bạn có thể nhập thêm chú thích trong ô "Message..." rồi nhấn gửi. Ảnh sẽ được tải lên Cloudinary trước, sau đó gắn vào tin nhắn và hiển thị trong bong bóng chat của bạn cũng như của đối phương.

### Bước 5: Theo dõi tin nhắn thời gian thực

Khi bạn mở một cuộc trò chuyện, ứng dụng đăng ký nhận sự kiện từ máy chủ qua STOMP. Mọi tin nhắn mới từ đối phương sẽ tự động xuất hiện ở cuối khung chat, danh sách cuộc trò chuyện bên trái cũng cập nhật đoạn xem trước và đưa cuộc trò chuyện đó lên đầu. Bạn không cần thao tác gì thêm.

## Xử lý lỗi thường gặp

### Tin nhắn hiển thị "Unable to send message."

Kiểm tra kết nối mạng và trạng thái đăng nhập. Bạn có thể nhập lại nội dung để gửi lại. Bong bóng tin nhắn lỗi sẽ có biểu tượng cảnh báo màu đỏ và dòng thông báo bên dưới.

### Không tải được lịch sử tin nhắn

Nếu khung chat hiện dòng lỗi kèm nút "Retry", nhấp nút này để thử tải lại danh sách tin nhắn. Nếu vẫn lỗi, tải lại trang hoặc thử mở cuộc trò chuyện khác trước khi quay lại.

### Ảnh không gửi được

Đảm bảo file bạn chọn là ảnh (định dạng ảnh phổ biến như JPG, PNG, WebP). Nếu Cloudinary từ chối tải lên (mạng chậm, dung lượng quá lớn), thử chọn ảnh nhỏ hơn hoặc kiểm tra lại kết nối.

### Nút gửi bị mờ

Nút gửi chỉ sáng lên khi cuộc trò chuyện đã sẵn sàng (không ở trạng thái "creating" hay "error") và bạn đã nhập ít nhất một ký tự văn bản hoặc chọn một ảnh.

## Câu hỏi thường gặp

### Vì sao trong danh sách bên trái có người mà tôi chưa từng nhắn?

Danh sách gồm cả những người bạn đang theo dõi. Chọn tên của họ để tạo cuộc trò chuyện mới; chỉ những cuộc đã có ít nhất một tin nhắn mới được giữ lại lâu dài trong danh sách.

### Tôi có gửi được nhiều ảnh trong một tin nhắn không?

Mỗi tin nhắn hiện chỉ đính kèm được một ảnh. Nếu cần gửi nhiều ảnh, gửi thành nhiều tin nhắn liên tiếp.

### Ứng dụng có thông báo khi có tin nhắn mới không?

Có. Tin nhắn mới sẽ hiển thị trong khung chat nếu bạn đang mở cuộc trò chuyện đó, đồng thời tạo thông báo trong trang Notifications với loại Messages để bạn biết ngay cả khi không mở trang Messages.
