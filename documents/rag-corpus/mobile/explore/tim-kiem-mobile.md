---
title: Tìm kiếm trên tab Explore mobile
slug: tim-kiem-mobile
platform: mobile
category: explore
tags: [tim-kiem, explore, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Explore
---

## Giới thiệu

Tab Explore có một ô tìm kiếm đặt phía trên cùng, đóng vai trò điểm ra vào để tra cứu người dùng, cafe page và bài viết trong CafeStory. Ô tìm kiếm dùng cơ chế debounce 320ms và yêu cầu tối thiểu 2 ký tự trước khi gọi API. Kết quả được nhóm rõ ràng thành "Users and reviewers", "Cafe pages" và "Posts" để bạn dễ chọn.

Trong lúc bạn nhập, dòng chip tab Explore (all / cafes / reviewers / trending) sẽ tạm biến mất; toàn bộ vùng nội dung nhường chỗ cho khu vực tìm kiếm. Khi bạn xoá query, tab Explore quay lại.

## Điều kiện tiên quyết

Bạn phải đăng nhập và ở trong MainTabs. Thiết bị cần kết nối Internet để gọi API tìm kiếm. Nên có ý tưởng về từ khoá muốn tìm (tên quán, tên người, một cụm trong bài viết).

## Các bước thực hiện

### Bước 1: Mở tab Explore

Chạm biểu tượng Explore ở bottom tab bar. Ô tìm kiếm hiện ngay trên cùng với biểu tượng kính lúp bên trái và placeholder "Search cafes, reviewers, or posts".

### Bước 2: Chạm vào ô tìm kiếm

Chạm vào ô. Bàn phím hiện lên. Ô không viết hoa chữ đầu và không autocorrect nên bạn có thể gõ tên riêng hoặc handle một cách chính xác.

### Bước 3: Gõ ít nhất 2 ký tự

Khi bạn mới gõ 1 ký tự, khu vực nội dung hiển thị EmptyState với tiêu đề "Keep typing" và mô tả "Type at least 2 characters to search users, cafe pages, and posts." Tiếp tục gõ đến khi đạt tối thiểu 2 ký tự.

### Bước 4: Chờ debounce 320ms

Sau khi bạn tạm dừng gõ 320 mili-giây, ứng dụng gửi yêu cầu tìm kiếm. Trong lúc chờ, tiêu đề "Searching" hiện kèm ActivityIndicator và một dải skeleton hàng danh sách bên dưới.

### Bước 5: Duyệt kết quả nhóm

Khi có kết quả, khu vực "Search results" hiển thị:

- "Users and reviewers": tên đầy đủ, subtitle là @username hoặc thành phố, nhãn "User".
- "Cafe pages": tên quán, subtitle là thành phố hoặc địa chỉ, nhãn "Cafe".
- "Posts": tiêu đề là đoạn nội dung bài, subtitle là tên tác giả hoặc tên quán, nhãn "Post".

Mỗi mục đều có avatar bên trái và một dòng nhãn nhỏ màu primary bên phải để cho biết loại đối tượng.

### Bước 6: Chạm vào mục để mở

- Chạm mục người dùng: mở màn hình hồ sơ người dùng.
- Chạm mục cafe page: mở màn hình chi tiết quán.
- Chạm mục bài viết: mở màn hình chi tiết bài blog.

### Bước 7: Xoá query để quay lại Explore

Xoá hết nội dung trong ô tìm kiếm. Ứng dụng ẩn khu vực search, hiện lại dòng chip ExploreTabs và nội dung tab đang chọn.

## Xử lý lỗi thường gặp

### "Keep typing" vẫn hiện dù đã gõ nhiều ký tự

Kiểm tra xem có ký tự nào là khoảng trắng không. Ứng dụng cắt khoảng trắng trước khi đếm; nếu bạn gõ 3 dấu cách và 1 chữ, ký tự hiệu lực chỉ là 1.

### "No results found"

Không có kết quả trùng khớp. Thử từ khoá khác, đổi cách viết (không dấu, viết tắt), hoặc dùng từ khoá tiếng Anh.

### Thông báo lỗi hiện nguyên văn từ server

Có thể server gặp sự cố. Xoá query và gõ lại, hoặc kéo pull-to-refresh trên trang.

### Kết quả cũ vẫn hiện sau khi tôi xoá query

Kết quả sẽ biến mất khi query xuống dưới 2 ký tự. Nếu vẫn còn, hãy chạm ngoài ô tìm kiếm rồi chạm lại và xoá sạch.

### Nút Search trên bàn phím không làm gì

Nút "search" trên bàn phím chỉ ẩn bàn phím; tìm kiếm được kích hoạt tự động theo debounce khi bạn dừng gõ.

## Câu hỏi thường gặp

### Tại sao lại là 320ms debounce?

Đây là khoảng chờ giữa lần bạn nhả phím và lần API được gọi, giúp tránh gọi nhiều lần khi bạn đang gõ dở. Con số này khớp với debounce của gợi ý username khi đăng ký, để trải nghiệm nhất quán.

### Tôi tìm bằng @username được không?

Bạn có thể gõ username không kèm @. Kết quả nhóm "Users and reviewers" sẽ so khớp và hiển thị subtitle bằng @username.

### Tại sao bài viết hiển thị tiêu đề là nội dung?

Bài viết CafeStory không có trường tiêu đề riêng, nên ứng dụng lấy trực tiếp phần content (đã trim) làm tiêu đề hiển thị. Nếu content trống thì dùng displayName hoặc "Blog post" thay thế.

### Tìm kiếm có ưu tiên khu vực của tôi không?

Kết quả trả về là kết quả chung. Ưu tiên khu vực áp dụng ở feed và Explore recommendations chứ không phải ô tìm kiếm.

### Kết quả tìm có phân trang không?

Chưa. Ô tìm kiếm hiện trả về một lượt kết quả duy nhất, không có infinite scroll. Muốn thu hẹp, hãy gõ từ khoá cụ thể hơn.
