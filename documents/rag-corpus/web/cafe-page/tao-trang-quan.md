---
title: Tạo trang quán cà phê
slug: tao-trang-quan
platform: web
category: cafe-page
tags: [tao-quan, cafe-page, thanh-toan, cloudinary, region]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /cafes/edit
---

# Tạo trang quán cà phê trên web

## Giới thiệu

Trang tạo quán trên web dành cho chủ quán muốn đưa quán của mình lên CafeStory. Trong biểu mẫu, bạn tải ảnh bìa, ảnh đại diện, điền tên và mô tả quán, chọn địa chỉ theo hệ thống hành chính Việt Nam (tỉnh, thành phố/quận, phường và tên đường). Sau khi tạo, hệ thống sẽ chuyển bạn đến trang chi tiết quán vừa tạo.

## Điều kiện tiên quyết

Bạn phải đăng nhập tài khoản CafeStory. Mỗi tài khoản chỉ được sở hữu một trang quán tại một thời điểm; nếu bạn đã có quán, khi mở `/cafes/edit` hệ thống sẽ chuyển sang chế độ chỉnh sửa quán hiện có thay vì tạo mới.

Ảnh dùng cho quán phải là định dạng ảnh (image/*) và mỗi tệp không quá 5MB. Ảnh được tải lên Cloudinary khi bạn bấm nút tạo/lưu.

Sau khi tạo quán, để công khai với cộng đồng bạn cần mua gói "Cafe Owner Plan". Chi tiết thanh toán được nêu trong tài liệu "Gia hạn gói Cafe Page".

## Các bước thực hiện

### Bước 1: Mở trang tạo quán

Đăng nhập tài khoản, sau đó truy cập đường dẫn `/cafes/edit`. Nếu tài khoản chưa có quán nào, tiêu đề trang hiển thị "Create cafe page". Các phần "Cafe identity" và "Location" được mở sẵn để bạn điền.

### Bước 2: Tải ảnh bìa và ảnh đại diện

Trong khối "Photos":

- Ở phần "Cover image", bấm nút "Cover" ở góc dưới ảnh khung 16:7 để chọn ảnh bìa từ máy tính. Dòng gợi ý "Choose a wide image from your device." sẽ thay bằng tên tệp sau khi chọn.
- Ở phần "Avatar", bấm biểu tượng máy ảnh trên khung tròn hoặc nút "Change avatar" bên cạnh để chọn ảnh đại diện.

Ảnh được xem trước ngay tại chỗ. Nếu tệp không phải ảnh hoặc quá 5MB, hệ thống hiện lỗi "Please choose an image file." hoặc "Cafe images must be 5MB or smaller.".

### Bước 3: Điền tên quán và mô tả

Trong khối "Cafe identity":

- "Cafe name": bắt buộc. Nhập tên hiển thị cho quán.
- "Description": không bắt buộc. Mô tả ngắn về quán, không khí, món đặc trưng.

Nếu để trống tên quán khi tạo, hệ thống hiện lỗi "Cafe name is required." và tự mở lại khối này.

### Bước 4: Chọn địa chỉ quán

Trong khối "Location":

- "Province / city": chọn tỉnh hoặc thành phố trực thuộc trung ương từ danh sách tìm kiếm.
- "City / district": chọn quận, huyện hoặc thành phố cấp dưới sau khi đã chọn tỉnh.
- "Ward": chọn phường/xã sau khi đã chọn quận/huyện.
- "Area" (không bắt buộc): tên khu vực hoặc mốc dễ nhận biết.
- "Street": nhập số nhà, tên đường và địa chỉ chi tiết.

Cả ba trường tỉnh, quận/huyện và phường đều bắt buộc, cùng với "Street". Nếu thiếu, hệ thống hiện lỗi "Province, city, ward and street are required." và mở lại khối "Location".

### Bước 5: Tạo trang quán

Sau khi điền đủ ảnh, tên và địa chỉ, bấm nút "Create cafe page" ở cuối trang. Trong quá trình xử lý, nút hiển thị "Creating...". Hệ thống:

1. Tải ảnh bìa và ảnh đại diện (nếu bạn đã chọn) lên Cloudinary.
2. Ghép địa chỉ từ Street, Ward, Area, City/Province thành chuỗi hoàn chỉnh.
3. Gọi API tạo quán.
4. Chuyển bạn đến trang `/cafes/<mã quán vừa tạo>`.

### Bước 6: Kích hoạt hiển thị và mua gói

Ngay sau khi tạo, trang quán mặc định ở trạng thái ẩn (chỉ bạn thấy). Vào lại `/cafes/edit`, bạn sẽ thấy khối "Open to visitors" với công tắc chuyển đổi. Bật công tắc để đưa quán lên công khai.

Để duy trì trạng thái công khai, bạn cần mua gói "Cafe Owner Plan". Mở modal thanh toán bằng cách bấm biểu tượng bánh răng cạnh tên quán trên trang quán, chọn "Renew subscription" và làm theo tài liệu "Gia hạn gói Cafe Page".

## Xử lý lỗi thường gặp

### Lỗi "Cafe name is required."

Bạn chưa nhập tên quán. Mở lại khối "Cafe identity" và điền tên trước khi bấm "Create cafe page".

### Lỗi "Province, city, ward and street are required."

Thiếu một trong các trường bắt buộc trong khối "Location". Chọn lại đầy đủ tỉnh, quận/huyện, phường và điền số nhà/tên đường.

### Lỗi "Cafe images must be 5MB or smaller."

Ảnh bạn chọn vượt quá 5MB. Nén lại ảnh (ví dụ bằng công cụ nén ảnh trực tuyến) rồi chọn lại.

### Lỗi "Please choose an image file."

Tệp bạn chọn không phải ảnh. Chỉ chọn các định dạng ảnh phổ biến (JPG, JPEG, PNG, WebP).

### Lỗi "Unable to load Vietnam address data." hoặc tương tự cho city/ward

Không tải được danh mục địa chỉ. Thử tải lại trang. Nếu vẫn lỗi, kiểm tra lại kết nối mạng.

### Lỗi "Unable to create cafe page."

Máy chủ từ chối tạo quán. Có thể tài khoản đã có quán (mỗi tài khoản chỉ 1 quán) hoặc phiên đăng nhập đã hết hạn. Đăng xuất và đăng nhập lại, sau đó thử lại.

### Sau khi tạo, quán vẫn không hiện công khai

Sau khi tạo, quán ở trạng thái nháp. Vào `/cafes/edit`, bật công tắc "Open to visitors", và đảm bảo gói dịch vụ còn hiệu lực.

## Câu hỏi thường gặp

### Tôi có thể tạo nhiều quán bằng một tài khoản không?

Không. Mỗi tài khoản chỉ sở hữu một trang quán tại một thời điểm. Nếu bạn có nhiều chi nhánh, cần trao đổi với đội hỗ trợ.

### Tôi phải trả phí ngay khi tạo không?

Bước tạo miễn phí. Chi phí chỉ phát sinh khi bạn mua gói "Cafe Owner Plan" để công khai trang cho cộng đồng.

### Có thể bỏ trống ảnh bìa hoặc ảnh đại diện không?

Có. Nếu bỏ trống, hệ thống dùng ảnh mặc định. Bạn có thể quay lại chỉnh sửa sau.

### Tôi chọn địa chỉ sai thì làm sao?

Vào `/cafes/edit`, mở lại khối "Location" và cập nhật, sau đó bấm "Save location".

### Có thể xoá trang quán không?

Chức năng xoá trang quán không nằm trong biểu mẫu tạo/sửa. Bạn có thể tắt công tắc "Open to visitors" để ẩn quán, hoặc liên hệ đội hỗ trợ nếu muốn xoá hoàn toàn.
