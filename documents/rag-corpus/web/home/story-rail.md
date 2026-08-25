---
title: Sử dụng dải Story Rail
slug: story-rail
platform: web
category: home
tags: [story-rail, story, home, follow]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /
---

# Sử dụng dải Story Rail trên trang chủ

## Giới thiệu

Story Rail là dải hình tròn nằm ở đầu cột nội dung chính của trang chủ. Dải tổng hợp tất cả người dùng bạn đang theo dõi và các trang quán cà phê bạn đã theo dõi thành các mục có thể cuộn ngang, kèm nút "Explore" ở cuối để khám phá thêm.

## Điều kiện tiên quyết

- Đã đăng nhập tài khoản CafeStory. Story Rail chỉ hiển thị khi bạn có phiên đăng nhập hợp lệ.
- Đã theo dõi ít nhất một người dùng hoặc một trang quán để có mục xuất hiện trên dải. Nếu chưa có, dải sẽ trống, chỉ còn lại nút "Explore".

## Các bước thực hiện

### Bước 1: Định vị dải trên trang chủ

Mở đường dẫn "/" sau khi đăng nhập. Story Rail là dải đầu tiên phía trên danh sách bài viết, mỗi mục có ảnh đại diện hình tròn và tên nhãn phía dưới.

### Bước 2: Cuộn qua các mục theo dõi

Dùng con lăn chuột hoặc thao tác vuốt để cuộn ngang. Ngoài ra, khi dải dài hơn khung hiển thị, hai nút mũi tên tròn xuất hiện ở hai đầu:

- Nút bên trái ("Scroll stories left") cuộn về đầu dải.
- Nút bên phải ("Scroll stories right") cuộn về cuối dải.

Nút bị ẩn khi không còn phần nào để cuộn theo hướng đó.

### Bước 3: Nhận biết loại mục

Story Rail hợp nhất hai nhóm:

- Người dùng bạn đang theo dõi: nhãn hiển thị theo tên người dùng ("userName").
- Trang quán cà phê bạn đã theo dõi: nhãn hiển thị theo tên quán.

Cả hai đều dùng ảnh đại diện tương ứng; nếu tài khoản hoặc trang chưa có ảnh, hệ thống dùng ảnh mặc định của CafeStory.

### Bước 4: Mở mục bạn quan tâm

Bấm vào một mục trên dải:

- Mục người dùng dẫn tới trang cá nhân của người đó.
- Mục trang quán dẫn tới trang chi tiết quán tương ứng.

Có thể mở trong tab mới bằng cách nhấn giữ Ctrl khi bấm chuột trái (hoặc chuột giữa).

### Bước 5: Khám phá thêm bằng nút "Explore"

Ở cuối dải luôn có mục "Explore" với biểu tượng dấu cộng. Bấm vào để chuyển sang trang "/explore" và tìm thêm quán, reviewer hoặc bài viết nổi bật để theo dõi.

## Xử lý lỗi thường gặp

### Dải trống dù bạn đã theo dõi nhiều tài khoản

Có thể phiên đăng nhập vừa được khôi phục và dữ liệu chưa kịp tải. Tải lại trang bằng Ctrl+F5. Nếu vẫn trống, kiểm tra ở trang cá nhân xem danh sách "Following" và các trang quán đang theo dõi còn nguyên hay không.

### Không cuộn ngang được bằng chuột

Trên một số bàn di, cuộn ngang có thể không hoạt động mặc định. Dùng nút mũi tên tròn hai bên để cuộn, hoặc giữ Shift trong khi cuộn dọc để chuyển thành cuộn ngang.

### Bấm vào mục mà không đi đâu

Nếu tài khoản người dùng hoặc trang quán đã bị xoá/ẩn, liên kết vẫn hiển thị nhưng trang đích có thể báo không tìm thấy. Bỏ theo dõi để không thấy mục đó nữa.

## Câu hỏi thường gặp

### Story Rail có hiển thị nội dung dạng "story" (ảnh/video ngắn) không?

Hiện tại các mục trên dải chỉ dẫn tới trang cá nhân hoặc trang quán tương ứng. Định dạng story ngắn hạn có thể được bổ sung theo lộ trình sản phẩm và sẽ có tài liệu riêng khi ra mắt.

### Thứ tự các mục trong dải được sắp thế nào?

Danh sách người dùng bạn theo dõi được hiển thị trước, tiếp đến là các trang quán bạn theo dõi. Trong mỗi nhóm, thứ tự do máy chủ trả về theo dữ liệu theo dõi của bạn.

### Có thể ẩn Story Rail không?

Trên phiên bản hiện tại chưa có tuỳ chọn ẩn dải trực tiếp. Nếu bạn không muốn thấy mục nào, hãy bỏ theo dõi tài khoản hoặc trang quán tương ứng.
