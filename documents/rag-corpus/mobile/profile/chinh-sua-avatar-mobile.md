---
title: Chỉnh sửa avatar trên ứng dụng di động
slug: chinh-sua-avatar-mobile
platform: mobile
category: profile
tags: [avatar, profile, mobile, upload-anh]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Profile
---

# Chỉnh sửa avatar trên ứng dụng di động CafeStory

## Giới thiệu

Trên ứng dụng React Native của CafeStory, người dùng đổi ảnh đại diện qua EditProfileModal mở từ tab Profile. Ứng dụng chỉ dùng thư viện ảnh của thiết bị (expo-image-picker gallery), không mở máy ảnh, cho phép cắt ảnh theo tỉ lệ 1:1 rồi tải lên Cloudinary. URL ảnh mới được đưa vào updateMyProfile để lưu trên máy chủ; hồ sơ được refresh ngay để avatar mới hiển thị khắp ứng dụng.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản CafeStory. Ứng dụng cần quyền truy cập thư viện ảnh (Photo Library trên iOS, Media/Photos trên Android). Nếu chưa cấp, ứng dụng sẽ hỏi khi bạn chạm nút thay ảnh lần đầu. Ảnh chấp nhận là JPG/PNG do máy chọn từ thư viện; ứng dụng đặt tham số quality 0.86 nên ảnh được nén nhẹ trước khi upload.

## Các bước thực hiện

### Bước 1: Mở EditProfileModal

Vào tab Profile ở thanh điều hướng dưới cùng. Trong hàng nút hành động dưới phần mô tả và chips, chạm nút "Edit Profile". Modal chỉnh sửa hồ sơ trượt lên toàn màn hình với các trường tên hiển thị, username, email, số điện thoại, và ô ảnh đại diện lớn ở đầu modal.

### Bước 2: Chạm vào ô avatar để mở thư viện ảnh

Chạm vào ảnh đại diện hoặc biểu tượng máy ảnh ở modal. Ứng dụng gọi ImagePicker.requestMediaLibraryPermissionsAsync để xin quyền lần đầu. Nếu quyền được cấp, ứng dụng gọi ImagePicker.launchImageLibraryAsync với các tuỳ chọn: allowsEditing bật, aspect 1:1, mediaTypes chỉ ảnh, quality 0.86.

Trình chọn ảnh của hệ điều hành mở lên - bạn duyệt qua album, chọn ảnh muốn dùng, sau đó màn hình cắt ảnh xuất hiện. Kéo khung vuông để chọn phần muốn giữ. Chạm "Chọn" hoặc "Done" (tuỳ hệ điều hành) để xác nhận.

### Bước 3: Chờ upload lên Cloudinary

Ngay khi bạn xác nhận ảnh, ứng dụng bật cờ đang upload avatar. Trong modal, khu vực avatar hiển thị trạng thái loading và các nút liên quan bị vô hiệu để tránh thao tác kép. Ứng dụng gọi uploadAvatarToCloudinary với thông tin file: tên (dùng fileName nếu có, nếu không thì tự sinh dạng "avatar-<timestamp>.jpg"), type (mimeType hoặc mặc định image/jpeg), uri của asset vừa chọn.

### Bước 4: Cập nhật hồ sơ

Khi upload xong, ứng dụng nhận uploadedAvatarUrl và gọi updateMyProfile với trường userAvatar là URL mới. Server cập nhật hồ sơ và trả về UserResponse mới. Ứng dụng gán vào state profile, avatar mới hiển thị ngay ở đầu modal và ở ProfileScreen phía dưới.

### Bước 5: Đóng modal

Chạm nút đóng ở góc trái header của modal để quay lại màn hình Profile. Avatar mới đã sẵn sàng hiển thị ở nhiều nơi khác: header các màn hình khác dùng chung nguồn user context. Nếu bạn muốn chỉnh thêm tên hoặc mô tả, tiếp tục trong modal rồi mới bấm nút "Save" của EditProfileModal để lưu các trường còn lại.

## Xử lý lỗi thường gặp

### "Photo access is required to update your avatar."

Bạn đã từ chối quyền thư viện ảnh. Vào Cài đặt hệ thống của thiết bị, tìm mục CafeStory, bật quyền Photos hoặc Media, sau đó quay lại và bấm lại nút thay avatar. Trên một số bản Android, cần thoát và mở lại ứng dụng để quyền mới có hiệu lực.

### "Unable to upload your avatar."

Đây là lỗi upload lên Cloudinary hoặc lưu hồ sơ. Kiểm tra kết nối mạng, thử chọn ảnh có dung lượng nhỏ hơn (dưới 5 MB) hoặc định dạng JPG/PNG chuẩn. Đóng modal, mở lại và thử lần nữa. Nếu vẫn lỗi, đăng xuất và đăng nhập lại.

### Avatar không đổi sau khi upload

Kéo xuống ProfileScreen để refresh, hoặc thoát tab Profile và quay lại. Trong hiếm khi cache ảnh cũ vẫn hiển thị, đóng và mở lại ứng dụng để buộc tải lại URL mới.

### Trình chọn ảnh không mở

Trên một số thiết bị Android cũ, expo-image-picker cần cấp quyền ở cả cấp hệ điều hành và cấp ứng dụng. Kiểm tra Cài đặt > Ứng dụng > CafeStory > Quyền > Ảnh/Media, sau đó chọn "Cho phép mọi lúc" nếu có tuỳ chọn.

## Câu hỏi thường gặp

### Tôi có chụp ảnh trực tiếp từ máy ảnh được không?

Luồng đổi avatar trên di động chỉ mở thư viện ảnh (gallery-only). Nếu muốn dùng ảnh vừa chụp, hãy chụp bằng ứng dụng máy ảnh của điện thoại, sau đó quay lại CafeStory và chọn ảnh đó từ thư viện.

### Tỉ lệ cắt là bao nhiêu?

Ứng dụng ép tỉ lệ 1:1 cho avatar. Bạn thấy khung vuông trong màn hình cắt của hệ điều hành để chọn vùng muốn giữ.

### Ảnh có bị nén không?

Có, ứng dụng đặt quality 0.86 khi mở picker, nghĩa là ảnh sẽ được nén ở mức chất lượng tốt trước khi upload. Điều này giúp tiết kiệm băng thông mà vẫn giữ ảnh nét trong khung avatar tròn nhỏ.

### Avatar mới có tự hiển thị trên web không?

Có. URL avatar được lưu tập trung trên máy chủ nên khi bạn đăng nhập trên web, ảnh mới sẽ hiển thị đồng bộ.
