---
title: Viết bài review quán cafe trên web
slug: viet-bai-review
platform: web
category: blog
tags: [blog, review, viet-bai, kiem-duyet]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /reviews/new
---

# Viết bài review quán cafe trên web

## Giới thiệu

CafeStory cho phép bạn viết bài chia sẻ trải nghiệm ở một quán cafe, kèm ảnh, vị trí, tag bạn bè và tag quán. Trên bản web, bài viết được soạn qua một dialog nhiều bước: chọn/crop ảnh, sau đó viết caption và cấu hình bài. Ngay khi bạn bấm gửi, bài đi qua pipeline kiểm duyệt AI của CafeStory (chấm cả chữ và ảnh) trước khi hiển thị công khai.

## Điều kiện tiên quyết

- Bạn phải đăng nhập tài khoản CafeStory.
- Có ảnh muốn đăng (tối đa 10 ảnh cho một bài). Nếu chỉ muốn viết chữ, bài vẫn được nhận nhưng sẽ tự chuyển admin xem xét thủ công.
- Nếu muốn đăng dưới danh nghĩa quán, tài khoản của bạn phải là chủ sở hữu một Cafe Page đang hoạt động.
- Có kết nối internet ổn định (ảnh được tải thẳng lên Cloudinary trước khi tạo bài).

## Các bước thực hiện

### Bước 1: Mở trình soạn bài

Có hai cách vào trình soạn:

- Từ sidebar bên trái, bấm biểu tượng "+" (tạo bài mới). Hệ thống mở dialog CreatePostSetupModal để bạn chọn ảnh trước khi viết.
- Truy cập trực tiếp đường dẫn `/reviews/new` để mở trang composer đầy đủ (bao gồm nhãn "Reviewer", tiêu đề "Draft", và các trường Cafe/Visit type/Spend/Rating/Review).

### Bước 2: Chọn và cắt ảnh

Ở bước ảnh, bạn chọn ảnh từ máy tính. Nếu ảnh không đúng khung mong muốn, dùng công cụ CreatePostImageCropper để cắt trước khi qua bước tiếp theo. Số ảnh tối đa được backend chấp nhận là 10; nếu chọn nhiều hơn, hệ thống sẽ chặn ở bước gửi.

Ở dialog Create Post chính, phần "Selected Photos" hiển thị lưới các ảnh đã chọn kèm nút "×" ở góc trên phải mỗi ảnh để xoá. Nút "Edit photos" ở đầu section đưa bạn quay lại bước chọn ảnh.

### Bước 3: Viết caption và tag bạn bè hoặc quán

Ô caption có placeholder "Share your experience... Type @ to tag a user or cafe page." Gõ nội dung tự do. Khi bạn gõ ký tự `@`, một trình chọn (MentionPicker) mở ra kèm danh sách người bạn đang follow và các cafe page liên quan. Dùng phím mũi tên lên/xuống để chọn, Enter hoặc Tab để chèn mention. Nhấn Escape để đóng picker mà không tag ai.

Mỗi mention được lưu kèm loại (user hay cafe page) và ID, để backend gắn đúng bản ghi khi tạo bài.

### Bước 4: Thêm vị trí (không bắt buộc)

Trong ô "Location", bấm "Add location". Dialog "Select location" mở lên, gồm ba dropdown có tìm kiếm: Province → City → Ward. Ward không bắt buộc cho bài blog (nhãn "Ward (optional)"). Khi đã chọn tối thiểu Province + City, phần "Selected location" hiển thị chuỗi đầy đủ. Bấm "Done" để lưu; ứng dụng gọi API tạo region nếu chưa tồn tại và gán vào bài.

Sau khi thêm, ô Location hiển thị icon ghim + tên vị trí + nút "Change" và "Remove" để đổi hoặc bỏ.

### Bước 5: Chọn đăng dưới danh nghĩa quán (nếu bạn có Cafe Page)

Nếu tài khoản đang sở hữu một Cafe Page, một section "Post as cafe page" xuất hiện với công tắc bật/tắt. Khi bật, phía dưới hiển thị dòng "This post will appear under <Tên quán>." Bài viết sẽ được đăng dưới danh nghĩa quán thay vì cá nhân, và người theo dõi quán sẽ nhìn thấy trên feed cafe page.

### Bước 6: Cấu hình bình luận

Section "Turn off commenting" là một công tắc. Bật lên nếu bạn không muốn ai bình luận vào bài. Ghi chú bên dưới nhắc: "You can change this later by going to the ... menu at the top of your post."

### Bước 7: Đăng bài

Bấm nút "Post" ở góc phải header của dialog. Nút bị vô hiệu nếu caption trống. Trong lúc gửi, nút chuyển sang trạng thái "Posting" kèm biểu tượng vòng xoay. Trình tự backend:

1. Ảnh được upload song song lên Cloudinary.
2. Mentions trong caption được mã hoá và ID tương ứng được đóng gói.
3. API `createModeratedBlog` được gọi với payload: allowComment (nghịch của "Turn off commenting"), content đã mã hoá mention, imageUrls, isPinned=false, pageId (nếu đăng dưới danh nghĩa quán), regionId (nếu chọn vị trí), taggedUserIds, taggedCafePageIds.
4. Bài được tạo với trạng thái đang kiểm duyệt.

Sau khi tạo xong, toast thông báo hiện lên: "Bài đăng đang được kiểm duyệt bởi AI. Bạn sẽ nhận thông báo khi hoàn tất." Dialog tự đóng và biểu mẫu được reset.

## Xử lý lỗi thường gặp

### "Please enter a caption before posting."

Xuất hiện khi bạn bấm Post mà ô caption trống hoặc chỉ chứa khoảng trắng. Nhập ít nhất một ký tự có nội dung rồi thử lại.

### "Unable to create post. Please try again."

Lỗi tổng quát khi API tạo bài thất bại. Kiểm tra kết nối mạng, số lượng ảnh (không vượt 10), và thử lại. Nếu vẫn lỗi, đăng xuất và đăng nhập lại để làm mới phiên.

### Ảnh upload chậm hoặc treo

Ảnh được upload song song lên Cloudinary. Nếu quá nhiều ảnh dung lượng lớn, quá trình có thể mất vài chục giây. Giữ dialog mở, không tải lại trang. Nếu vẫn treo, xoá bớt ảnh (dung lượng khuyến nghị ≤ 5MB/ảnh) rồi thử lại.

### Không thấy trình chọn mention khi gõ @

MentionPicker chỉ mở khi bạn gõ `@` trong ô caption và có danh sách người bạn đang follow. Nếu bạn chưa follow ai, danh sách trống — bạn có thể gõ tay slug rồi tự khớp sau, nhưng backend sẽ không nhận diện tag đó thành ID.

### Không có section "Post as cafe page"

Section này chỉ hiển thị khi tài khoản của bạn sở hữu một Cafe Page. Nếu bạn chưa tạo Cafe Page hoặc trang đã hết hạn, section này bị ẩn.

## Câu hỏi thường gặp

### Tôi có thể đăng bài không kèm ảnh không?

Được, backend chấp nhận. Nhưng bài không ảnh sẽ tự chuyển admin duyệt thủ công thay vì auto-approve. Bài của bạn có thể mất thêm thời gian để hiển thị.

### Bài của tôi sau khi Post đã lên feed ngay chưa?

Chưa. Bài đi vào trạng thái đang kiểm duyệt AI. Kết quả có ba khả năng: được duyệt (PUBLISHED), chuyển admin xem xét (HIDDEN tạm thời), hoặc bị từ chối (REMOVED). Bạn nhận thông báo trong tab Notifications loại "Moderation" khi có kết quả.

### Tôi có sửa được caption sau khi đã đăng không?

Hiện tại giao diện web chưa có chức năng sửa caption của bài đã đăng. Nếu cần sửa, xoá bài rồi đăng lại (nhưng lưu ý web cũng chưa có nút xoá — cần liên hệ admin nếu buộc phải sửa).

### Tối đa bao nhiêu ảnh cho một bài?

Tối đa 10 ảnh. Backend enforce giới hạn cứng, gửi nhiều hơn sẽ bị chặn.

### Ward có bắt buộc không?

Không. Trong dialog chọn vị trí, ô Ward có nhãn "Ward (optional)". Chỉ Province và City là bắt buộc để lưu vị trí.

### Toast "Bài đăng đang được kiểm duyệt" xuất hiện bao lâu?

Toast hiển thị khoảng 5 giây rồi tự ẩn. Nếu bạn bỏ lỡ, có thể vào tab Notifications loại "Moderation" để theo dõi trạng thái bài.
