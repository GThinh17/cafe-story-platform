---
title: Theo dõi và đánh giá quán
slug: follow-va-danh-gia-quan
platform: web
category: cafe-page
tags: [follow, like, review, danh-gia, cafe-page]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /cafes/[id]
---

# Theo dõi và đánh giá quán cà phê

## Giới thiệu

Trên trang quán, bạn có ba hành động chính để tương tác: thích quán (Like), theo dõi quán (Follow) và xem/tạo bài đánh giá cho quán. Ba nút này nằm cạnh nhau ở khu vực đầu trang quán, ngay bên phải phần tên quán.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản CafeStory để thích, theo dõi hoặc để lại bài đánh giá cho quán. Nếu chưa đăng nhập, khi bấm vào các nút này hệ thống sẽ yêu cầu bạn đăng nhập trước.

Quán phải đang hoạt động (gói còn hiệu lực). Với quán đã hết gói, trang chỉ hiển thị hộp thoại "Cafe page expired" và không thể thao tác.

## Các bước thực hiện

### Bước 1: Thích quán (Like)

Ở khu vực đầu trang quán, bấm nút hình trái tim ở đầu hàng nút hành động. Trạng thái nút thay đổi ngay lập tức:

- Chưa thích: nút màu chủ đạo, trái tim rỗng, số lượt hiển thị dạng rút gọn (ví dụ 128, 1.2K).
- Đã thích: nút chuyển sang màu phụ, trái tim được tô kín và số lượt tăng thêm 1.

Bấm lần nữa để bỏ thích. Nếu thao tác thất bại (mất mạng, lỗi máy chủ), trạng thái tự phục hồi lại như trước.

### Bước 2: Theo dõi quán (Follow)

Bấm nút "Follow" ở giữa hàng nút. Sau khi theo dõi, nút đổi trạng thái sang "Following" và bạn sẽ nhận cập nhật từ quán trong feed và mục Notifications. Bấm lại nút để bỏ theo dõi. Số người theo dõi được hiển thị trong khối "Vibe & Features" dưới dạng "<số> followers".

### Bước 3: Xem menu quán trước khi review

Trước khi viết bài đánh giá, bạn có thể bấm "View Menu" (nút bên phải cùng, có biểu tượng cuốn sổ) để mở hộp thoại thực đơn. Tham khảo tên món, giá và mô tả để đưa vào bài viết cho chi tiết.

### Bước 4: Xem bài đánh giá gần đây

Cuộn xuống mục "Recent Posts". Mỗi thẻ bài viết đã có sẵn:

- Điểm đánh giá của bài (huy hiệu ở góc phải trên ảnh, giá trị từ 1 đến 5).
- Người viết và ảnh đại diện.
- Đoạn caption ngắn.
- Nút Like, Comment, Share, Bookmark ở dưới cùng.

Bấm nút Comment để mở hộp thoại bình luận của bài. Bấm nút Like (trái tim) để thích bài viết. Bấm ảnh đại diện hoặc tên người viết để mở trang cá nhân của họ.

### Bước 5: Đánh giá quán bằng cách viết bài review

CafeStory không có nút "chấm sao nhanh" ngay trên trang quán; điểm đánh giá được ghi lại thông qua bài review bạn tạo tại `/reviews/new`. Trong biểu mẫu tạo bài, bạn chọn 1 trong 5 mức từ 1 sao đến 5 sao (Rating), gắn tên quán và viết cảm nhận. Sau khi bài được duyệt, điểm của bạn sẽ được tính vào điểm trung bình của quán và hiển thị trong danh sách "Recent Posts".

Quy tắc chi tiết về thang điểm, thời gian duyệt và cách tính điểm trung bình được mô tả trong tài liệu chính sách đánh giá riêng. Trong khuôn khổ trang quán, bạn chỉ cần nhớ:

- Thang điểm: 1 (rất tệ) đến 5 (rất tốt).
- Mỗi bài review phải qua kiểm duyệt bằng AI trước khi công khai.
- Sau khi bài được duyệt, quán và người theo dõi sẽ thấy trong feed.

### Bước 6: Xem quán trên bản đồ

Ở đầu mục "Recent Posts" có tuỳ chọn "Map View" bên phải. Bấm để mở hộp thoại bản đồ và xem vị trí quán dựa trên địa chỉ đã đăng ký.

## Xử lý lỗi thường gặp

### Bấm Like nhưng số không tăng hoặc quay lại như cũ

Kết nối mạng có thể chập chờn. Hệ thống sẽ tự khôi phục trạng thái nếu máy chủ không xác nhận thao tác. Thử lại sau vài giây.

### Nút Follow không phản hồi

Kiểm tra bạn đã đăng nhập chưa. Nếu đã đăng nhập, tải lại trang và thử lại. Nếu vẫn lỗi, đăng xuất rồi đăng nhập lại.

### Không thấy nút Like/Follow/View Menu

Có thể trang quán đang tải (skeleton). Chờ trang hiển thị đầy đủ. Nếu quán đã hết gói, các nút sẽ không xuất hiện — thay vào đó là hộp thoại "Cafe page expired".

### Bài review vừa gửi chưa xuất hiện trong "Recent Posts"

Bài phải qua bước kiểm duyệt AI. Bạn sẽ nhận thông báo trong mục Notifications khi bài được duyệt. Nếu bị từ chối, xem tài liệu "Khiếu nại bài bị từ chối".

## Câu hỏi thường gặp

### Like và Follow khác nhau thế nào?

Like thể hiện bạn thích quán và tăng số lượt thích hiển thị công khai. Follow giúp bạn nhận bài đăng mới từ quán trong feed và thông báo. Bạn có thể chỉ Like mà không Follow, hoặc ngược lại.

### Tôi có thể đánh giá quán mà không cần đăng bài không?

Không. Điểm sao được tính từ bài review công khai. Nếu chỉ muốn ủng hộ quán mà không viết bài, bạn có thể Like hoặc Follow.

### Điểm review của tôi có tính ngay không?

Không tính ngay. Bài của bạn được đưa vào bộ lọc kiểm duyệt AI trước. Sau khi được duyệt, điểm mới cộng vào trung bình của quán.

### Bao nhiêu sao là ổn?

Thang từ 1 đến 5, trong đó 5 là rất hài lòng và 1 là rất không hài lòng. Nội dung chi tiết chính sách chấm điểm được nêu trong tài liệu chính sách đánh giá.

### Tôi có thể chỉnh lại điểm sao sau khi đăng không?

Việc chỉnh sửa bài đã đăng phụ thuộc vào chính sách nội dung hiện hành. Xem tài liệu "Chỉnh sửa bài viết" để biết chi tiết.
