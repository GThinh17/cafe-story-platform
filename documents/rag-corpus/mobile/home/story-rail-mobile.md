---
title: Sử dụng Story rail trên mobile
slug: story-rail-mobile
platform: mobile
category: home
tags: [story-rail, home, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Home
---

## Giới thiệu

Story rail là dòng avatar tròn nằm ngang ngay dưới ShareTopBar trong tab Home. Đây là lối tắt để nhanh chóng quay lại người mình theo dõi hoặc cafe page mình quan tâm, và để mở trực tiếp luồng đăng bài mới. Story rail trên mobile là danh sách cuộn ngang (ScrollView horizontal), mỗi ô rộng 72 pixel với avatar 58 pixel bên trong một vòng viền.

Ô đầu tiên luôn là "Your posts" của chính bạn (có huy hiệu dấu cộng và viền primary). Các ô sau là mục bạn theo dõi, tối đa 14 mục, gồm cả người dùng và cafe page.

## Điều kiện tiên quyết

Bạn phải đăng nhập và đã vào MainTabs. Muốn có nhiều mục trên rail, bạn cần theo dõi ít nhất một số người dùng hoặc cafe page. Thiết bị cần kết nối Internet để tải danh sách theo dõi.

## Các bước thực hiện

### Bước 1: Mở tab Home

Chạm tab Home ở bottom tab bar. Story rail nằm ngay dưới ShareTopBar.

### Bước 2: Nhận biết các ô trên rail

- Ô có viền primary và biểu tượng dấu cộng ở góc dưới phải, kèm nhãn "Your posts": đại diện cho chính bạn.
- Các ô tròn khác có viền secondary: người dùng hoặc cafe page bạn theo dõi. Nhãn hiển thị tên người/quán, tối đa một dòng.

### Bước 3: Chạm "Your posts" để tạo bài

Chạm ô đầu tiên. Ứng dụng chuyển sang tab Create. Bạn có thể nhập caption, chọn ảnh và đăng bài.

### Bước 4: Chạm ô cafe page

Nếu ô bạn chạm là một cafe page, ứng dụng mở màn hình chi tiết quán tương ứng.

### Bước 5: Chạm ô người dùng

Nếu ô là một người bạn theo dõi, ứng dụng mở màn hình hồ sơ người dùng đó.

### Bước 6: Vuốt ngang để duyệt

Story rail cuộn ngang. Đặt ngón tay lên rail và vuốt trái/phải để duyệt qua tối đa 14 mục.

### Bước 7: Làm mới danh sách rail

Kéo pull-to-refresh ở đầu feed. Ngoài việc tải lại feed, ứng dụng cũng gọi lại API danh sách theo dõi để cập nhật story rail.

## Xử lý lỗi thường gặp

### Rail chỉ có mỗi "Your posts"

Bạn chưa theo dõi ai. Sang tab Explore để tìm reviewer hoặc cafe page rồi theo dõi. Sau khi trở lại Home và kéo refresh, rail sẽ có thêm mục.

### Ô người/quán không mở gì khi chạm

Có thể mạng gián đoạn khiến ứng dụng chưa tải xong dữ liệu điều hướng. Kéo pull-to-refresh và thử lại.

### Ô mới theo dõi vẫn chưa xuất hiện trên rail

Ứng dụng chỉ làm mới danh sách rail khi bạn kéo pull-to-refresh Home hoặc mở lại tab Home. Hãy kéo refresh để cập nhật.

### Ô hiển thị chữ tắt thay vì ảnh

Nếu người dùng hoặc cafe page chưa có avatar, ứng dụng hiển thị hai chữ cái đầu của tên. Điều này bình thường và không phải lỗi.

## Câu hỏi thường gặp

### Ô "Your posts" có phải là story dạng Instagram không?

Không. Chạm vào ô này chỉ mở tab Create để bạn đăng bài viết. CafeStory mobile hiện chưa có story dạng ephemeral.

### Tại sao tôi chỉ thấy tối đa 14 ô?

Ứng dụng giới hạn 14 mục theo dõi để rail gọn gàng và tải nhanh. Nếu bạn theo dõi nhiều hơn, chỉ 14 mục đầu (theo thứ tự server trả về) được hiển thị.

### Cafe page nào được ưu tiên hiển thị?

Server quyết định thứ tự trong danh sách "following targets" trả về, dựa trên tương tác gần đây và loại đối tượng.

### Vòng viền primary và secondary có ý nghĩa gì?

Viền primary màu đậm dùng riêng cho ô của chính bạn ("Your posts"). Viền secondary màu nhẹ hơn dùng cho các ô còn lại để phân biệt.

### Tôi có thể sắp xếp lại rail theo ý mình không?

Chưa. Thứ tự do server sắp xếp; bạn không tuỳ chỉnh được từ mobile.
