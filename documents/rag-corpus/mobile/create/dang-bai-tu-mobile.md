---
title: Đăng bài từ tab Create trên mobile
slug: dang-bai-tu-mobile
platform: mobile
category: create
tags: [dang-bai, create, mobile, image-picker]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Create
---

## Giới thiệu

Tab Create nằm giữa bottom tab bar của CafeStory mobile và là lối đăng bài chính. Khi bạn mở tab Create, bottom tab bar bị ẩn đi để bạn tập trung vào luồng soạn bài; header chuyển thành CreatePostHeader với nhãn "New Post" hoặc "Post Settings" tuỳ bước.

Luồng chia làm hai bước: compose và settings. Ở bước compose, bạn viết caption, chọn ảnh từ thư viện, chọn hashtag chip, tag người và chọn location. Ở bước settings, bạn chọn visibility, bật/tắt cho phép bình luận và bật/tắt pin lên hồ sơ trước khi nhấn Post.

Tab Create có thể được mở kèm cafePageId để đăng bài dưới danh nghĩa một cafe page (nhận diện qua avatar và tên quán hiển thị ở header composer).

## Điều kiện tiên quyết

Bạn phải đăng nhập và đã hoàn tất Region (feed cần regionId để đăng bài). Cần quyền truy cập thư viện ảnh (Photos) khi lần đầu chọn ảnh. Kết nối Internet để tải ảnh lên Cloudinary và gọi API createBlog.

## Các bước thực hiện

### Bước 1: Mở tab Create

Chạm biểu tượng Create ở giữa bottom tab bar. Ứng dụng ẩn tab bar, hiển thị CreatePostHeader với tiêu đề "New Post" và bước compose.

### Bước 2: Viết caption

Chạm vào ô caption và gõ nội dung bạn muốn chia sẻ. Caption là bắt buộc; nút "Next" chỉ mở khi caption không rỗng (sau khi trim).

### Bước 3: Thêm ảnh từ thư viện

Chạm nút thêm ảnh trên CreatePostBottomBar hoặc trong khu vực media của bước compose. Lần đầu, hệ thống hỏi quyền truy cập thư viện ảnh; chấp nhận để tiếp tục. Ứng dụng mở thư viện ảnh của thiết bị (expo-image-picker), cho phép chọn tối đa 10 ảnh cùng lúc, không có tính năng cắt trong picker, chỉ nhận Images (không nhận video).

Sau khi chọn xong, ảnh xuất hiện trong khu vực media. Bạn có thể chạm "Remove" trong CreatePostComposeStep để xoá toàn bộ ảnh đã chọn nếu muốn thay.

### Bước 4: Chọn hashtag chip

Trong bước compose, ứng dụng hiển thị các chip hashtag đề xuất. Chạm để bật/tắt, tag đã bật sẽ vào danh sách tags của bài.

### Bước 5: (Tuỳ chọn) Tag người và chọn location

Chạm "Tag people" để mở modal chọn người theo dõi. Chạm nút chọn location để mở modal chọn Province/City/Ward. Xem chi tiết trong tài liệu riêng về tag người và chọn quán.

### Bước 6: Chạm Next

Khi caption đã có, nút "Next" ở CreatePostBottomBar mở khoá. Chạm để chuyển sang bước settings. Header đổi thành "Post Settings" và nút quay lại (mũi tên) thay thế nút hủy.

### Bước 7: Chọn visibility

Trong bước settings, chọn ai xem được bài (PUBLIC hoặc lựa chọn khác) qua CreatePostSettingsStep.

### Bước 8: Bật/tắt cho phép bình luận

Chuyển toggle "Allow comments" theo ý muốn. Bật cho phép bình luận (mặc định); tắt nếu bạn không muốn nhận bình luận.

### Bước 9: Bật/tắt pin lên hồ sơ

Chuyển toggle "Pin to profile" nếu muốn bài này được ghim lên đầu hồ sơ cá nhân của bạn.

### Bước 10: Chạm Post

Chạm nút "Post" ở CreatePostBottomBar. Ứng dụng lần lượt tải các ảnh cục bộ lên Cloudinary (bỏ qua ảnh đã là URL từ xa), sau đó gọi API createBlog với caption, danh sách ảnh, hashtag, người tag, regionId, cafePageId (nếu có), allowComment và isPinned.

Sau khi thành công, ứng dụng reset draft và điều hướng về tab Home; bạn sẽ thấy bài mới sau khi pull-to-refresh Home.

## Xử lý lỗi thường gặp

### "Photo access is required to add images."

Bạn từ chối quyền thư viện ảnh. Vào Cài đặt hệ thống của điện thoại, cấp quyền Photos cho CafeStory, rồi quay lại và chạm nút thêm ảnh.

### "Write a caption before continuing."

Caption rỗng khi bạn bấm Next. Nhập nội dung rồi thử lại.

### "Write a caption before posting."

Trong bước settings mà caption bị xoá về rỗng. Ứng dụng đưa bạn về bước compose kèm thông báo này; nhập lại caption.

### "Add your profile location before posting."

Draft không có regionId (rất hiếm khi bạn đã hoàn tất Region). Chạm nút chọn location để chọn khu vực đăng bài, hoặc kiểm tra lại phần cài đặt khu vực hồ sơ.

### "Unable to create post. Please try again."

Có thể do tải ảnh Cloudinary lỗi hoặc API createBlog trả lỗi. Kiểm tra kết nối, giảm số ảnh, và thử lại.

### Quá 10 ảnh không được thêm

Bộ chọn giới hạn 10 ảnh mỗi lần và ứng dụng cũng slice(0, 10) khi merge. Xoá bớt ảnh trong media trước khi thêm mới.

## Câu hỏi thường gặp

### Tôi có thể chụp ảnh trực tiếp từ camera không?

Không. Trên mobile, ứng dụng chỉ mở thư viện ảnh (launchImageLibraryAsync). Muốn dùng ảnh mới chụp, hãy chụp bằng ứng dụng Camera hệ thống trước, ảnh sẽ nằm trong thư viện và bạn chọn từ đó.

### Tôi có thể đăng bài không kèm ảnh không?

Có. Caption là bắt buộc, nhưng ảnh thì không. Bạn có thể chỉ viết text và đăng.

### Đăng bài dưới danh nghĩa cafe page thì làm sao?

Tab Create có thể được mở kèm tham số cafePageId (ví dụ từ trang chi tiết quán bạn quản trị). Khi đó, khu vực postingIdentity trong CreatePostComposeStep sẽ hiển thị avatar và tên quán thay vì tài khoản cá nhân của bạn. Đăng xong, bài thuộc về cafe page đó.

### Ảnh đã có URL từ xa có bị tải lại lên Cloudinary không?

Không. Ứng dụng dùng isRemoteImageUrl để bỏ qua ảnh có URL http/https, chỉ upload các ảnh có URI cục bộ.

### Tôi thoát ra rồi vào lại thì bản nháp có còn không?

Draft chỉ tồn tại trong bộ nhớ của phiên tab Create hiện tại. Nếu bạn chạm nút cancel trên header (khi ở bước compose), draft được reset và ứng dụng chuyển về Home.

### Pin to profile khác gì với đăng bình thường?

Bật pin sẽ ghim bài lên đầu hồ sơ cá nhân của bạn. Người khác vào hồ sơ sẽ thấy bài này trước tiên.
