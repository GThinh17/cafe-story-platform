---
title: Tương tác với bài viết trong feed
slug: tuong-tac-bai-viet
platform: web
category: home
tags: [feed, like, comment, share, report]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /
---

# Tương tác với bài viết trong feed

## Giới thiệu

Mỗi bài viết trong newsfeed cho phép bạn thả tim, bình luận, chia sẻ, lưu lại và báo cáo nếu nội dung vi phạm. Các hành động được đặt trực tiếp trên thẻ bài viết ở feed và cũng có mặt trong hộp thoại chi tiết khi bạn mở bình luận.

## Điều kiện tiên quyết

- Đã đăng nhập tài khoản CafeStory để lưu trạng thái thích/lưu và gửi bình luận.
- Bài viết đang ở trạng thái hiển thị công khai trong feed của bạn.

## Các bước thực hiện

### Bước 1: Thả tim ("Like") bài viết

Trên thẻ bài viết, bấm nút "Like" (biểu tượng trái tim) ở hàng hành động dưới ảnh. Khi thả tim thành công, biểu tượng chuyển sang trạng thái được tô màu và số bên cạnh tăng thêm một. Bấm lần nữa để bỏ thả tim.

### Bước 2: Mở khung bình luận ("Comment")

Bấm nút "Comment" (biểu tượng khung hội thoại). CafeStory mở hộp thoại chi tiết hai cột: bên trái là ảnh/video, bên phải là chú thích gốc và luồng bình luận.

### Bước 3: Gửi bình luận mới

Trong hộp thoại chi tiết, cuộn xuống thanh nhập ở chân trang:

1. Bấm vào ô nhập với gợi ý "Add a comment..." và soạn nội dung.
2. Bấm nút "Post" hoặc nhấn Enter để gửi.
3. Bình luận xuất hiện ngay trong luồng với trạng thái tạm thời "sending"; sau khi máy chủ xác nhận, trạng thái chuyển thành "sent" và số bình luận trên thẻ được cập nhật.

Nếu tác giả tắt bình luận cho bài viết đó, ô nhập hiển thị "Comment restricted" và bị khoá.

### Bước 4: Trả lời một bình luận

Bấm nút trả lời ở bình luận bạn muốn phản hồi. Ô nhập tự động đổi placeholder sang dạng "Reply to <tên người dùng>..." và bình luận mới sẽ được đính kèm dưới bình luận gốc.

### Bước 5: Thích một bình luận

Bấm biểu tượng trái tim cạnh bình luận để thích/bỏ thích. Số lượt thích của bình luận được cập nhật ngay tại giao diện.

### Bước 6: Chia sẻ ("Share") bài viết

Bấm nút "Share" (biểu tượng mũi tên xoay vòng) trên thẻ bài viết để chia sẻ. Số lượt chia sẻ hiển thị bên cạnh nút. Bài viết do chính bạn tạo sẽ không hiện nút "Share".

### Bước 7: Lưu bài viết ("Save")

Bấm biểu tượng đánh dấu (bookmark) ở góc phải hàng hành động. Khi bài đã được lưu, biểu tượng đổi màu và trạng thái aria chuyển sang "Unsave post"; bấm lần nữa để bỏ lưu. Danh sách bài đã lưu có thể xem lại trong trang cá nhân.

### Bước 8: Báo cáo ("Report") bài vi phạm

1. Bấm biểu tượng ba chấm ("More options") ở góc phải đầu thẻ bài viết.
2. Chọn mục "Report" trong menu thả xuống. Nút này chỉ hiển thị với bài của người khác, không hiện trên bài của bạn.
3. Trong hộp thoại "Report", chọn một lý do trong danh sách (ví dụ: "False information", "Scam, fraud, or spam", "Bullying or unwanted contact").
4. Điền thêm chi tiết vào ô "Details". Với một số lý do (như "Intellectual property"), ô chi tiết là bắt buộc và được đánh dấu "(required)"; các lý do còn lại đánh dấu "(optional)".
5. Bấm "Report" để gửi. Khi thành công, hộp thoại chuyển sang màn hình "Thanks for your report" cùng nút "Close" để đóng.

Muốn quay lại danh sách lý do khi đang ở màn hình chi tiết, bấm "Back" hoặc biểu tượng mũi tên trái ở đầu hộp thoại.

## Xử lý lỗi thường gặp

### Bình luận không gửi được

Nếu máy chủ từ chối bình luận, mục vừa gửi sẽ chuyển sang trạng thái lỗi và số bình luận trở về giá trị trước đó. Kiểm tra mạng rồi thử soạn lại. Nếu chủ bài viết đã tắt bình luận, ô nhập sẽ báo "Comment restricted"; bạn không thể gửi thêm.

### Không mở được danh sách lý do khi báo cáo

Nếu hộp thoại báo cáo hiển thị "Unable to load report reasons", có thể máy chủ chưa cấu hình lý do. Đóng hộp thoại, tải lại trang và thử lại sau ít phút.

### Đã bấm "Like" nhưng số không tăng

Có thể mạng bị chậm hoặc phiên đăng nhập vừa hết hạn. Tải lại trang; nếu vẫn không cập nhật, đăng nhập lại và thử lại.

## Câu hỏi thường gặp

### Người khác có biết tôi đã báo cáo bài viết của họ không?

Không. Trong hộp thoại báo cáo, CafeStory ghi rõ "Your report is anonymous." Người bị báo cáo không nhận được thông tin về danh tính người báo cáo.

### Tôi có thể chỉnh sửa hoặc xoá bình luận đã gửi không?

Bài này chỉ mô tả các thao tác cơ bản trên feed. Việc chỉnh sửa/xoá bình luận được hướng dẫn ở tài liệu quản lý bình luận riêng.

### Lưu bài viết có gửi thông báo tới tác giả không?

Không. Lưu bài là hành động cá nhân, chỉ hiển thị trong danh sách bài đã lưu của bạn.
