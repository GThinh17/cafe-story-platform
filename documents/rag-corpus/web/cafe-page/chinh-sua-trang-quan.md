---
title: Chỉnh sửa trang quán
slug: chinh-sua-trang-quan
platform: web
category: cafe-page
tags: [chinh-sua-quan, cafe-page, cloudinary, region, settings]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /cafes/edit
---

# Chỉnh sửa trang quán cà phê

## Giới thiệu

Sau khi đã tạo quán, chủ quán có thể cập nhật thông tin bất cứ lúc nào tại `/cafes/edit`. Biểu mẫu này chia thành các khối riêng biệt (Photos, Cafe identity, Open to visitors, Location) và mỗi khối lưu độc lập, giúp bạn cập nhật từng phần mà không cần lưu lại toàn bộ.

## Điều kiện tiên quyết

Bạn phải đăng nhập bằng tài khoản đang sở hữu quán. Nếu tài khoản chưa có quán nào, biểu mẫu chuyển sang chế độ tạo mới (xem tài liệu "Tạo trang quán cà phê").

Ảnh mới phải là định dạng ảnh và tối đa 5MB. Ảnh được tải lên Cloudinary khi bạn lưu.

## Các bước thực hiện

### Bước 1: Mở trang chỉnh sửa từ Settings modal

Trên trang quán của bạn (`/cafes/<mã quán>`), bấm biểu tượng bánh răng (Settings) nằm ngay cạnh tên quán. Nút này chỉ hiện với chủ quán. Hộp thoại "Cafe page settings" mở ra với các lựa chọn:

- "Edit menu" (chưa khả dụng, ghi chú "Cafe menu editing is not available yet.").
- "Edit cafe page" — bấm để chuyển đến `/cafes/edit`.
- "Renew subscription" — mở modal thanh toán gói.
- "Cancel" — đóng hộp thoại.

Cách khác: truy cập trực tiếp `/cafes/edit`. Ở chế độ chỉnh sửa, tiêu đề trang là "Edit cafe page" và mô tả "Update your cafe profile. Each section saves independently.".

### Bước 2: Cập nhật ảnh bìa và ảnh đại diện

Trong khối "Photos":

- Bấm nút "Cover" ở góc dưới khung ảnh bìa để chọn ảnh mới. Bạn sẽ thấy tên tệp bên dưới khung.
- Bấm biểu tượng máy ảnh trên khung avatar tròn, hoặc nút "Change avatar" bên cạnh, để chọn ảnh đại diện mới.

Sau khi chọn ít nhất một ảnh mới, bấm "Save images" ở cuối khối. Nút hiển thị "Saving..." trong lúc tải lên và đổi lại thành "Save images" sau khi xong. Khi thành công, thông báo "Images saved." hiện bên dưới.

Nếu bạn không chọn ảnh mới, nút "Save images" bị vô hiệu hoá.

### Bước 3: Sửa tên quán và mô tả

Khối "Cafe identity" mặc định thu gọn với dòng tóm tắt là tên quán hiện tại. Bấm "Edit" ở bên phải để mở khối:

- "Cafe name": tên quán, bắt buộc.
- "Description": mô tả, có thể để trống.

Bấm "Save changes" để lưu. Nút hiển thị "Saving..." khi đang xử lý. Bấm "Cancel" để huỷ chỉnh sửa và giữ giá trị cũ. Khối sẽ tự đóng lại sau khi lưu thành công.

### Bước 4: Bật/tắt hiển thị công khai

Ở khối "Open to visitors", công tắc bên phải cho biết trạng thái:

- Bật: "Your page is live and visible to the community".
- Tắt: "Your page is hidden — only you can see it".

Bấm công tắc để chuyển giữa hai trạng thái. Trong lúc thao tác, công tắc bị mờ và không nhận click cho tới khi máy chủ xác nhận.

### Bước 5: Cập nhật địa chỉ

Khối "Location" thu gọn với dòng tóm tắt là địa chỉ đầy đủ đang lưu. Bấm "Edit" để mở khối. Bạn có thể thay đổi:

- "Province / city": tỉnh hoặc thành phố trực thuộc trung ương.
- "City / district": quận, huyện hoặc thành phố cấp dưới.
- "Ward": phường/xã.
- "Area" (không bắt buộc): khu vực hoặc mốc dễ nhận biết.
- "Street": số nhà và tên đường chi tiết.

Cần đủ tỉnh, quận/huyện, phường và Street. Bấm "Save location" để lưu. Khi thành công, thông báo "Location saved." xuất hiện và khối tự đóng.

### Bước 6: Kiểm tra kết quả

Sau khi cập nhật, quay lại `/cafes/<mã quán>` để xem thay đổi. Ảnh mới, tên, mô tả và địa chỉ sẽ hiển thị ngay ở khu vực đầu trang quán. Nếu bạn đã bật công tắc "Open to visitors" và gói còn hiệu lực, khách hàng cũng sẽ thấy các cập nhật này.

## Xử lý lỗi thường gặp

### Nút "Save images" mờ, không bấm được

Bạn chưa chọn ảnh mới. Chỉ khi có ít nhất một ảnh (cover hoặc avatar) được chọn, nút mới sáng lên.

### Lỗi "Unable to save images."

Ảnh không tải lên được. Kiểm tra kết nối, dung lượng ảnh (dưới 5MB) và định dạng, rồi thử lại. Trước khi thử lại có thể cần chọn lại tệp.

### Lỗi "Cafe name is required."

Bạn đã xoá tên quán và bấm "Save changes". Nhập lại tên rồi lưu.

### Lỗi "Province, city and ward are required." hoặc "Street address is required."

Thiếu trường bắt buộc trong khối "Location". Chọn hoặc điền đầy đủ trước khi lưu.

### Công tắc "Open to visitors" bật/tắt nhưng trang khách vẫn không thấy

Công tắc chỉ điều chỉnh trạng thái. Nếu gói dịch vụ đã hết hạn, khách vẫn thấy hộp thoại "Cafe page expired" khi truy cập. Gia hạn gói theo tài liệu "Gia hạn gói Cafe Page".

### Không thấy nút bánh răng trên trang quán

Nút chỉ hiện với chủ quán. Đảm bảo bạn đang đăng nhập bằng tài khoản chủ quán và đang xem đúng quán mình sở hữu.

## Câu hỏi thường gặp

### Có thể lưu từng phần riêng lẻ không?

Có. Mỗi khối có nút lưu riêng: "Save images", "Save changes" (tên/mô tả), "Save location". Bạn có thể chỉ cập nhật một khối mà không ảnh hưởng khối khác.

### Đổi địa chỉ có làm mất các bài review cũ không?

Không. Bài review đã có vẫn giữ nguyên, chỉ địa chỉ của quán được cập nhật.

### Tôi đổi tên quán, bài viết cũ có tự cập nhật không?

Tên hiển thị của quán được lấy động từ trang quán, nên các nơi tham chiếu tới quán sẽ tự dùng tên mới.

### Tại sao nút "Edit menu" bị mờ?

Chức năng chỉnh sửa menu chưa mở trong phiên bản hiện tại. Bạn sẽ được thông báo khi tính năng sẵn sàng.

### Bao lâu thì thay đổi hiển thị cho khách?

Ngay sau khi lưu thành công. Trong một số trường hợp cache trình duyệt, khách nên tải lại trang để thấy cập nhật mới nhất.
