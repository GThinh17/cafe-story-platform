---
title: Chỉnh sửa trang quán trên ứng dụng di động
slug: chinh-sua-trang-quan-mobile
platform: mobile
category: cafe-page
tags: [cafe-page, chinh-sua, mobile, upload-anh]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - CafeDetail
---

# Chỉnh sửa trang quán trên ứng dụng di động

## Giới thiệu

Chủ quán (Owner) hoặc quản lý (Manager) có thể cập nhật thông tin trang quán trực tiếp từ ứng dụng di động: đổi ảnh bìa, ảnh đại diện, tên, mô tả, địa chỉ và khu vực (province, city, ward). Toàn bộ thao tác thực hiện trong EditCafePageModal - một modal trượt lên toàn màn hình có bàn phím tự né (KeyboardAvoidingView) để phần nhập liệu không bị che khi gõ.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản là chủ quán hoặc có quyền quản lý trang. Nút "Edit Page" chỉ hiển thị ở hàng nút hành động của CafePageHeader khi bạn có quyền. Ứng dụng cần quyền truy cập thư viện ảnh của thiết bị để chọn ảnh bìa và ảnh đại diện. Nếu bạn từ chối quyền, thao tác thay ảnh sẽ báo lỗi và bạn cần cấp lại trong phần cài đặt hệ thống.

## Các bước thực hiện

### Bước 1: Mở EditCafePageModal

Vào màn hình trang quán của mình, chạm nút "Edit Page" trong hàng ba nút dưới CafePageHeader. Modal chỉnh sửa trượt lên từ dưới. Thanh header của modal có nút mũi tên quay lại ở bên trái, tiêu đề "Edit page" ở giữa và nút "Save" màu xanh ở bên phải. Khi mới mở, giá trị các trường được đổ sẵn từ thông tin quán hiện tại: tên, địa chỉ, mô tả và trạng thái region (nếu có).

### Bước 2: Thay ảnh bìa

Phía trên cùng của nội dung modal là khối chọn ảnh bìa với tỉ lệ ngang. Nếu quán đã có ảnh, ảnh hiển thị full khối và có lớp phủ tối "Edit cover" với biểu tượng máy ảnh ở góc dưới bên phải. Nếu chưa có, khối hiển thị biểu tượng khung ảnh và dòng "Add cover".

Chạm vào khối bìa để mở thư viện ảnh của thiết bị (chỉ gallery, không dùng camera). Ứng dụng yêu cầu quyền truy cập thư viện lần đầu; sau đó bạn có thể cắt ảnh theo tỉ lệ 16:9 rồi xác nhận. Ảnh được tải lên Cloudinary và bạn sẽ thấy vòng xoay "loading" thay cho biểu tượng máy ảnh trong lúc chờ. Khi upload xong, ảnh bìa mới hiện ngay trong khối chọn.

### Bước 3: Thay ảnh đại diện quán

Ngay bên dưới ảnh bìa là hàng ảnh đại diện tròn (avatar). Bên trái là ô hình vuông có biểu tượng máy ảnh, chạm vào đó để mở thư viện ảnh. Ảnh đại diện được cắt theo tỉ lệ 1:1. Sau khi bạn chọn ảnh và xác nhận, ứng dụng upload lên Cloudinary và cập nhật ngay ô Avatar tròn nằm bên phải. Trong lúc chờ upload, biểu tượng máy ảnh được thay bằng vòng xoay.

### Bước 4: Sửa các trường thông tin cơ bản

Kéo xuống phần "fields" trong modal:

- "Cafe name": tên hiển thị của quán, bắt buộc phải có. Nếu để trống và bấm Save, modal hiện lỗi "Cafe name is required.".
- "Address": địa chỉ dạng đường phố hoặc mô tả. Trường này có thể bỏ trống nếu bạn không đổi region, nhưng bắt buộc khi bạn chọn cả ba mức province, city, ward.
- "Description": mô tả tối đa 300 ký tự, có đếm ký tự ở góc phải dưới. Ô mô tả cao 116 điểm và tự cuộn khi nội dung dài.

### Bước 5: Cập nhật khu vực (Location)

Phần Location cho phép chọn tuần tự Province -> City -> Ward. Mỗi lựa chọn hiển thị dạng hàng nút cuộn ngang (horizontal ScrollView) với biểu tượng vị trí ở đầu và tên khu vực. Lựa chọn đang chọn có nền hồng nhạt và biểu tượng dấu tick.

Chọn Province trước, danh sách City sẽ tự tải sau khi chọn xong. Sau khi chọn City, danh sách Ward tải tiếp. Nếu bạn chưa muốn đổi khu vực, chỉ cần bỏ qua phần này; ứng dụng sẽ giữ nguyên regionId cũ. Nếu chọn đủ ba mức, ứng dụng sẽ gọi createRegion để tạo bản ghi khu vực mới và gán vào trang trước khi lưu.

### Bước 6: Lưu thay đổi

Chạm "Save" ở góc phải header modal. Nút chỉ bật khi tên quán khác rỗng, không đang lưu và không đang upload ảnh. Trong lúc lưu, ô "Save" chuyển thành vòng xoay. Khi thành công, modal tự đóng và màn hình trang quán làm mới với dữ liệu mới. Nếu có lỗi (mạng, quyền hoặc validation phía máy chủ), thông báo lỗi hiển thị ở cuối modal và modal không đóng để bạn chỉnh lại.

### Bước 7: Đóng modal mà không lưu

Chạm mũi tên quay lại ở góc trái header modal. Nếu đang trong quá trình lưu hoặc đang upload ảnh, nút quay lại bị vô hiệu để tránh mất dữ liệu. Chờ tác vụ hoàn tất rồi mới đóng được.

## Xử lý lỗi thường gặp

### Ứng dụng báo cần cấp quyền ảnh

Thông báo "Photo access is required to update the cafe cover." hoặc "Photo access is required to update the cafe avatar." xuất hiện khi thư viện ảnh chưa được cấp quyền. Vào Cài đặt hệ thống của thiết bị, tìm mục ứng dụng CafeStory, bật quyền Photos/Media, sau đó quay lại và bấm lại nút chọn ảnh.

### Không upload được ảnh lên Cloudinary

Thông báo "Unable to upload cafe avatar." hoặc "Unable to upload cafe cover." báo hiệu upload thất bại. Kiểm tra kết nối mạng, thử ảnh có dung lượng nhỏ hơn hoặc định dạng phổ biến (JPG/PNG), sau đó bấm lại. Nếu vẫn lỗi, đóng modal, mở lại và thử lần nữa.

### Lỗi "Address is required when updating cafe location."

Khi bạn chọn đủ ba mức Province, City, Ward mà để trống ô Address, hệ thống không cho lưu vì cần địa chỉ đường phố để tạo region mới. Nhập địa chỉ vào ô "Address" rồi bấm Save.

### Danh sách city hoặc ward trống

Nếu sau khi chọn Province mà mục City hiện "No cities available for this province." hoặc chọn City mà Ward hiện "No wards available for this city.", đó là do dữ liệu khu vực chưa được đồng bộ. Chọn một Province khác gần đúng, hoặc để nguyên region cũ và chỉ cập nhật các trường còn lại.

## Câu hỏi thường gặp

### Tôi có thể chụp ảnh trực tiếp thay vì chọn từ thư viện không?

Hiện tại luồng chỉnh sửa trang quán trên di động chỉ mở thư viện ảnh (expo-image-picker gallery). Nếu muốn dùng ảnh vừa chụp, hãy chụp bằng ứng dụng máy ảnh, sau đó quay lại CafeStory và chọn ảnh đó từ thư viện.

### Có giới hạn số ký tự cho mô tả không?

Có, mô tả tối đa 300 ký tự. Bộ đếm ở góc dưới bên phải luôn hiển thị số ký tự hiện tại trên 300.

### Nếu tôi chỉ đổi ảnh mà không sửa gì khác thì có cần bấm Save không?

Ảnh mới được tải lên và cập nhật ngay khi upload xong, nhưng bạn vẫn nên bấm Save để đồng bộ toàn bộ trạng thái trang quán về máy chủ và làm mới màn hình chi tiết.

### Người quản lý (Manager) có được sửa toàn bộ không?

Nút "Edit Page" hiển thị cho cả chủ quán và quản lý. Ứng dụng cho phép quản lý mở modal và sửa các trường cơ bản. Một số ràng buộc quyền cụ thể được máy chủ kiểm tra khi lưu.
