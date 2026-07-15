---
title: Xem lý do bài bị từ chối trên web
slug: xem-ly-do-bai-bi-tu-choi
platform: web
category: blog
tags: [blog, moderation, notification, kiem-duyet]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /notifications
---

# Xem lý do bài bị từ chối trên web

## Giới thiệu

Khi bài viết của bạn đi qua pipeline kiểm duyệt AI, kết quả được gửi về tài khoản qua trung tâm thông báo với loại "Moderation" (nhóm nội bộ `BLOG_MODERATION`). Có ba trạng thái khả dĩ: bài được duyệt, bài cần admin xem xét thêm, hoặc bài bị từ chối. Với mỗi trạng thái, bạn có thể mở một dialog chi tiết để đọc thông điệp cụ thể mà hệ thống trả về.

Lưu ý quan trọng: **giao diện web hiện tại chưa có nút khiếu nại trực tiếp trong app.** Dialog chỉ có nút "Đã hiểu" để xác nhận đã đọc và đóng dialog. Nếu bạn cho rằng bài bị từ chối nhầm, xem phần "Khiếu nại" phía dưới để biết cách phản hồi qua kênh ngoài.

## Điều kiện tiên quyết

- Bạn phải đăng nhập tài khoản CafeStory và đã có bài viết được kiểm duyệt.
- Bài của bạn đã có kết quả kiểm duyệt (không còn ở trạng thái đang xử lý ban đầu).
- Bạn nhận được thông báo trong tab Notifications loại "Moderation".

## Các bước thực hiện

### Bước 1: Mở trang Notifications

Trên thanh sidebar bên trái hoặc thanh điều hướng, bấm vào biểu tượng chuông thông báo để mở trang `/notifications`.

### Bước 2: Lọc tab "Moderation"

Trên trang Notifications, các thông báo được nhóm theo loại. Bấm tab "Moderation" (nội bộ là `BLOG_MODERATION`) để chỉ hiển thị các thông báo liên quan đến kiểm duyệt bài viết. Các loại khác gồm Follow, Like, Comment, Share, Message, Tag.

### Bước 3: Đọc trạng thái ngắn trên mỗi dòng

Mỗi thông báo Moderation hiển thị một trong ba tin nhắn ngắn tuỳ trạng thái:

- "Bài đăng của bạn đã được duyệt." — bài đã lên feed công khai (`APPROVED`).
- "Bài đăng của bạn đang chờ admin xem xét." — AI không chắc chắn, chuyển thủ công cho admin (`SEND_ADMIN`).
- "Bài đăng của bạn đã bị từ chối." — bài vi phạm chính sách nội dung (`DENIED`).

### Bước 4: Bấm vào dòng để mở dialog chi tiết

Click vào dòng thông báo. Dialog "Moderation reason" (`ModerationReasonDialog`) mở lên với:

- **Tiêu đề** tương ứng với trạng thái:
  - `APPROVED` → "Bài đăng đã được duyệt"
  - `SEND_ADMIN` → "Bài đăng cần admin xem xét"
  - `DENIED` → "Bài đăng đã bị từ chối"
- **Nội dung** là lý do cụ thể do hệ thống trả về. Nếu backend không kèm lý do chi tiết, dialog dùng câu mặc định:
  - `APPROVED` → "Bài của bạn đã được đăng công khai."
  - `SEND_ADMIN` → "Bài của bạn cần được admin duyệt thêm trước khi công khai."
  - `DENIED` → "Bài của bạn không phù hợp với chính sách nội dung."

### Bước 5: Đóng dialog

Bấm nút "Đã hiểu" ở góc phải dialog. Dialog đóng lại và thông báo tương ứng được đánh dấu đã đọc.

Có thể đóng dialog bằng cách bấm ra ngoài overlay hoặc phím Escape; kết quả tương đương.

## Khiếu nại

Web app hiện tại **không có nút "Khiếu nại" hay "Appeal" ngay trong dialog moderation**. Nếu bạn tin bài bị từ chối nhầm hoặc muốn yêu cầu xem xét lại:

1. Chuẩn bị mã bài (thấy ở URL bài viết hoặc mã trong thông báo) và ảnh chụp màn hình lý do trong dialog.
2. Liên hệ CafeStory qua kênh hỗ trợ chính thức bên ngoài ứng dụng (fanpage Facebook, email hỗ trợ, hoặc form liên hệ nếu có).
3. Cung cấp mô tả ngắn vì sao bạn nghĩ bài không vi phạm.

Đội CafeStory sẽ xem xét lại thủ công ở tầng admin — nội bộ có luồng xử lý báo cáo với 3 hành động: `APPROVE` (khôi phục), `HIDE` (ẩn tạm), `REMOVE` (giữ trạng thái vi phạm).

## Xử lý lỗi thường gặp

### Không thấy tab "Moderation" trong Notifications

Nếu bạn chưa từng đăng bài nào cần kiểm duyệt, tab có thể không có thông báo nào và bạn dễ bỏ qua. Cuộn ngang danh sách tab, "Moderation" nằm cùng hàng với các tab khác (Follow, Like, Comment...).

### Dialog không hiện lý do cụ thể

Khi backend không kèm chuỗi lý do, dialog dùng câu mặc định theo trạng thái. Điều này không có nghĩa bài của bạn "không có lý do rõ ràng" — hệ thống có ghi log nội bộ nhưng chưa expose ra dialog cho user. Nếu cần biết chính xác vì sao, liên hệ hỗ trợ.

### Bài đã bị từ chối nhưng vẫn thấy trong archive

Đúng theo thiết kế. Trang Archive (`/[username]/archive`) hiển thị các bài trạng thái `HIDDEN` để chủ tài khoản xem lại. Bài `REMOVED` (vi phạm) không xuất hiện trong feed nhưng có thể vẫn xuất hiện ở archive tuỳ phiên bản. Không có nút khôi phục trong giao diện.

### Không nhận được thông báo moderation

Kiểm tra: đã đăng bài chưa, bài có đang ở trạng thái đợi kiểm duyệt kéo dài không, tài khoản có bị đánh dấu inactive không. Nếu bài ở trạng thái đợi quá lâu (nhiều giờ), khả năng cao dịch vụ AI moderation tạm thời gián đoạn — bài sẽ tự chuyển admin thủ công.

## Câu hỏi thường gặp

### Ba trạng thái APPROVED / SEND_ADMIN / DENIED khác gì nhau?

- `APPROVED`: AI xác nhận bài an toàn, tự động lên feed.
- `SEND_ADMIN`: AI không chắc chắn (ví dụ ảnh confidence thấp, không có ảnh, hoặc service tạm thời gián đoạn) và chuyển bài cho admin duyệt thủ công. Trạng thái blog thành `HIDDEN` tạm.
- `DENIED`: bài vi phạm chính sách. Trạng thái blog thành `REMOVED` và bị loại khỏi mọi feed.

### Vì sao dialog không có nút Khiếu nại?

Phiên bản web hiện tại chưa hỗ trợ luồng khiếu nại trực tiếp trong app. Đội sản phẩm có thể bổ sung ở phiên bản sau; hiện tại vui lòng dùng kênh hỗ trợ ngoài.

### Bấm "Đã hiểu" có ảnh hưởng đến bài viết không?

Không. Nút "Đã hiểu" chỉ đóng dialog và đánh dấu thông báo đã đọc. Trạng thái bài không thay đổi khi bạn bấm nút này.

### Tôi có xem lại được lý do sau khi đã đóng dialog không?

Có. Trong tab Notifications, bấm lại vào dòng thông báo Moderation cũ — dialog sẽ mở lại với cùng nội dung.

### Tại sao bài mới của tôi bị SEND_ADMIN thay vì APPROVED?

Ba khả năng chính: (1) bài không có ảnh nên AI không tự tin quyết định, (2) ảnh có confidence phân loại dưới ngưỡng, hoặc (3) dịch vụ AI đang gián đoạn nên fallback qua admin. Trong mọi trường hợp, bài sẽ được admin duyệt lại thủ công.
