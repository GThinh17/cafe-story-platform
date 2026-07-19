---
title: Trò chuyện với Trợ lý CafeStory
slug: chat-voi-tro-ly-ai
platform: web
category: message
tags: [tro-ly-ai, assistant, chatbot, cafestory-ai]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /messages
---

# Trò chuyện với Trợ lý CafeStory trên web

## Giới thiệu

Trợ lý CafeStory là cuộc trò chuyện đặc biệt do hệ thống cung cấp, luôn được ghim (pin) ở đầu danh sách Conversation List trong trang Messages. Cuộc trò chuyện có tên hiển thị "Trợ lý CafeStory", tên người dùng `@cafestory_ai` và huy hiệu nhỏ "AI" bên cạnh tên để bạn dễ phân biệt với các cuộc trò chuyện thông thường.

Trợ lý dùng để trả lời nhanh các câu hỏi về CafeStory: cách sử dụng ứng dụng, thông tin về các quán cafe được đăng, đội ngũ reviewer, gói dịch vụ, chính sách và các tính năng liên quan. Trợ lý trả lời qua kênh HTTP dành riêng, không dùng chung hạ tầng chat thời gian thực với người dùng thật, và có thể đi kèm danh sách nguồn tham khảo giúp bạn kiểm chứng thông tin.

## Điều kiện tiên quyết

Trợ lý hoạt động ngay cả khi bạn chưa đăng nhập, dựa trên bộ tài liệu công khai của CafeStory. Tuy nhiên, để danh sách các cuộc trò chuyện với người dùng khác đầy đủ và để đồng bộ trên nhiều thiết bị, bạn nên đăng nhập trước khi mở trang Messages.

## Các bước thực hiện

### Bước 1: Mở trang Messages

Truy cập `/messages` từ thanh điều hướng. Danh sách cuộc trò chuyện tải xong sẽ đưa Trợ lý CafeStory lên vị trí đầu tiên, kèm dòng xem trước "Hỏi bất kỳ điều gì về CafeStory".

### Bước 2: Chọn Trợ lý CafeStory

Nhấp vào dòng "Trợ lý CafeStory" ở đầu danh sách. Khung chat bên phải mở ra với tin nhắn chào mừng do trợ lý gửi sẵn, giới thiệu ngắn về những chủ đề bạn có thể hỏi (quán cafe, reviewer, gói dịch vụ, cách dùng ứng dụng).

### Bước 3: Gửi câu hỏi

Nhập câu hỏi vào ô "Message..." và nhấn Enter hoặc nút gửi. Câu hỏi của bạn xuất hiện ngay ở bên phải khung chat. Trợ lý sẽ hiển thị một bong bóng bên trái với ba chấm nhấp nháy trong lúc đang soạn câu trả lời.

Bạn có thể chèn emoji bằng nút mặt cười giống như khi nhắn cho người dùng khác. Cuộc trò chuyện với trợ lý không hỗ trợ gửi ảnh: nút chọn ảnh vẫn hiển thị nhưng tin nhắn của trợ lý luôn ở dạng văn bản.

### Bước 4: Đọc câu trả lời và nguồn tham khảo

Khi câu trả lời sẵn sàng, ba chấm nhấp nháy được thay bằng nội dung do trợ lý viết. Nếu trợ lý dùng tài liệu nội bộ để trả lời, các thẻ nguồn (hiển thị tiêu đề bài viết) xuất hiện phía dưới bong bóng để bạn tham khảo thêm.

Trợ lý ghi nhớ tối đa mười lượt trao đổi gần nhất trong cùng phiên để phục vụ các câu hỏi tiếp nối. Bạn có thể hỏi tiếp mà không cần lặp lại toàn bộ bối cảnh.

### Bước 5: Quay lại cuộc trò chuyện khác

Vì trợ lý luôn được ghim ở đầu, bạn có thể chuyển sang cuộc trò chuyện khác ở phía dưới bất cứ lúc nào rồi quay lại mà không mất lịch sử hiển thị trong phiên hiện tại.

## Xử lý lỗi thường gặp

### Trợ lý báo "Unable to reach assistant."

Đây là lỗi kết nối tới dịch vụ trả lời tự động. Kiểm tra kết nối mạng và gửi lại câu hỏi sau ít phút. Bong bóng lỗi hiển thị màu cảnh báo bên dưới nội dung.

### Câu trả lời không liên quan hoặc không đầy đủ

Trợ lý dựa trên tài liệu CafeStory nên các câu hỏi ngoài phạm vi (thời sự, ý kiến cá nhân, thông tin của bên thứ ba) có thể không được trả lời chính xác. Diễn đạt lại câu hỏi ngắn gọn, cụ thể hơn, hoặc chỉ rõ tính năng bạn muốn tìm hiểu để có kết quả tốt hơn.

### Danh sách bên trái chỉ có mỗi Trợ lý CafeStory

Nếu chưa đăng nhập hoặc chưa có cuộc trò chuyện nào với người dùng khác, danh sách sẽ chỉ hiển thị trợ lý. Đăng nhập và bắt đầu nhắn tin với người khác để danh sách được điền đầy đủ.

## Câu hỏi thường gặp

### Trợ lý có lưu lại nội dung trò chuyện của tôi không?

Trợ lý dùng lịch sử tối đa mười lượt gần nhất trong phiên hiện tại để trả lời tiếp nối. Nội dung không xuất hiện trên trang cá nhân hay khung chat của bất kỳ người dùng nào khác.

### Trợ lý có thay thế được nhân viên hỗ trợ CafeStory không?

Trợ lý phù hợp cho các câu hỏi thường gặp và hướng dẫn nhanh. Với các trường hợp cần thao tác trên tài khoản (khóa/mở tài khoản, hoàn tiền, khiếu nại nội dung), bạn nên liên hệ kênh hỗ trợ chính thức thông qua email hoặc biểu mẫu liên hệ trong ứng dụng.

### Trợ lý có gửi được ảnh hoặc file không?

Trợ lý chỉ trả lời bằng văn bản và thẻ nguồn tham khảo. Nếu cần minh họa bằng hình ảnh, hãy tham khảo tài liệu chính thức trong mục Trung tâm trợ giúp.
