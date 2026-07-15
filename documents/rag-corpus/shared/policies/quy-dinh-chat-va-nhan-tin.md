---
title: Quy định chat và nhắn tin
slug: quy-dinh-chat-va-nhan-tin
platform: both
category: chat
tags: [chat, messaging, pagination, ai-assistant, notifications]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài liệu này mô tả quy định về tính năng chat trực tiếp giữa người dùng CafeStory (web và mobile), giới hạn phân trang tin nhắn, các ràng buộc chống spam cơ bản và cách trợ lý AI xuất hiện trong danh sách hội thoại. Mục tiêu là đảm bảo hiệu năng khi tải lịch sử chat và giữ trải nghiệm nhắn tin nhất quán giữa hai nền tảng.

## Quy định

### 1. Giới hạn phân trang tin nhắn

Mỗi request lấy tin nhắn (history) trả về tối đa **100 messages**. Nếu client gửi `pageSize` lớn hơn 100, server sẽ ép về 100.

Chi tiết tại `service/serviceImplement/ChatServiceImpl.java` (dòng 49).

Client cần cuộn (scroll) và gọi tiếp trang cũ hơn nếu muốn xem lịch sử dài. Việc cap 100 giúp bảo vệ database khỏi query nặng và giảm băng thông cho client mobile.

### 2. Không thể tự chat với chính mình

Nếu người dùng cố tạo hội thoại với chính `userId` của mình, server từ chối. Kiểm tra ở `ChatServiceImpl.java` (dòng 88-89).

Điều này ngăn các tình huống edge case ảnh hưởng danh sách hội thoại và tránh spam self-message.

### 3. Rate limit

Hiện tại **không có rate limit hard-coded** cho việc gửi tin nhắn. Nền tảng dựa vào các cơ chế bảo vệ ở tầng hạ tầng (WAF, giới hạn theo IP) thay vì chặn ở mã service.

Quy định người dùng vẫn cấm spam, quấy rối; vi phạm được xử lý qua luồng report/moderation chung.

### 4. Trợ lý AI ghim đầu danh sách hội thoại (chỉ web)

Trên web, danh sách hội thoại (`ConversationList`) luôn có mục ảo **"Trợ lý CafeStory"** được ghim ở đầu. Đây là entry cho phép người dùng nhắn với trợ lý AI của nền tảng, không phải một người dùng thật.

Trên mobile, entry này chưa được hiển thị trong danh sách hội thoại. Mobile app tập trung vào chat 1-1 giữa người dùng.

### 5. Notifications

- **Web**: nhận thông báo qua kênh chung của trang; không có push notification riêng cho chat.
- **Mobile**: **không có in-app push registration** cho tin nhắn. App đọc thông báo mới từ server khi user mở tab Notifications hoặc kéo refresh.

Do đó, người nhận tin nhắn trên mobile có thể có độ trễ nhận thông báo cho tới khi mở app.

## Ngoại lệ

- Trợ lý CafeStory không tuân theo quy định "không tự chat với chính mình" vì đây là AI, không phải user thật.
- Cap 100 messages/request không áp dụng cho các job nội bộ đọc dữ liệu (ví dụ backup, thống kê).
- Việc không có rate limit hard-coded không đồng nghĩa cho phép spam - vi phạm chuẩn cộng đồng vẫn bị xử lý.

## Câu hỏi thường gặp

**1. Vì sao tôi chỉ thấy 100 tin nhắn cũ nhất trong một lần tải?**
Đó là giới hạn phân trang. Cuộn lên đầu danh sách để hệ thống tải trang cũ hơn.

**2. Có thể tự nhắn cho chính mình để ghi chú không?**
Không. Hệ thống chặn hội thoại self. Bạn nên dùng tính năng khác (bài viết nháp, ghi chú riêng nếu có).

**3. Vì sao trên mobile tôi không nhận được thông báo tin nhắn mới?**
Mobile hiện chưa đăng ký in-app push cho chat. Bạn cần mở app và vào tab Notifications để cập nhật.

**4. "Trợ lý CafeStory" ở đầu danh sách chat là ai?**
Đó là mục ảo dẫn tới AI assistant của nền tảng (chỉ web). Không phải người dùng thật.

**5. Có giới hạn số tin nhắn tôi gửi mỗi phút không?**
Không có giới hạn cứng ở mã service, nhưng hành vi spam vẫn có thể bị xử lý theo quy định cộng đồng.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ChatServiceImpl.java` (dòng 49, 88-89)
