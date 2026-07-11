---
title: Đăng ký tài khoản và chọn khu vực trên mobile
slug: dang-ky-va-chon-khu-vuc
platform: mobile
category: auth
tags: [dang-ky, khu-vuc, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Register
  - Region
---

## Giới thiệu

Đăng ký tài khoản CafeStory trên mobile gồm hai màn hình liên tiếp: Register (tạo tài khoản) và Region (chọn khu vực). Bạn bắt buộc phải hoàn tất cả hai bước; nếu bỏ ngang, feed Home sẽ không mở được vì hệ thống cần biết bạn thường khám phá quán ở khu vực nào để đề xuất nội dung sát với địa phương.

Màn hình Register có gợi ý username tự động sinh từ họ tên hoặc email, hiển thị dưới dạng suggestion chips. Màn hình Region dùng chip picker ngang cho Province, City, Ward và một ô nhập text cho Street.

## Điều kiện tiên quyết

Bạn cần một email hợp lệ chưa đăng ký tài khoản CafeStory. Cần chọn được một username duy nhất, và một mật khẩu ghi nhớ được. Thiết bị cần kết nối Internet để tải danh sách Province/City/Ward từ server. Bạn nên biết khu vực mình đang sinh sống hoặc thường ghé để chọn đúng.

## Các bước thực hiện

### Bước 1: Mở màn hình Register

Từ màn hình Login, chạm dòng "Create a CafeStory account". Ứng dụng chuyển sang màn hình có tiêu đề "Create account" và mô tả "Start saving cafe notes, reviews, and places worth returning to."

### Bước 2: Nhập Full name

Chạm vào trường "Full name" (placeholder "Gia Thinh") rồi nhập họ tên đầy đủ. Trường này không bắt buộc, nhưng nếu có, hệ thống sẽ dùng để tạo gợi ý username.

### Bước 3: Nhập Email

Chuyển sang trường "Email" (placeholder "hello@cafestory.com"). Trường bật keyboard-type email và không viết hoa chữ đầu. Nhập email chính xác, tránh khoảng trắng.

### Bước 4: Chọn hoặc chỉnh Username

Sau khi Full name hoặc Email có ít nhất 2 ký tự, hệ thống chờ 320ms rồi tự động gọi API gợi ý username. Ba chip gợi ý xuất hiện phía dưới trường "Username". Nếu bạn chưa tự gõ, chip đầu tiên sẽ được tự động điền vào ô Username.

Chạm vào một chip khác để đổi sang lựa chọn đó, hoặc chạm thẳng vào ô Username và gõ tay username tuỳ ý. Khi bạn đã tự chỉnh, hệ thống sẽ không ghi đè bằng gợi ý nữa.

### Bước 5: Nhập Password

Chạm trường "Password" (placeholder "Create a password") và nhập mật khẩu. Mật khẩu được ẩn dưới dạng chấm.

### Bước 6: Chạm Create account

Khi đủ Email, Username và Password, nút "Create account" mở khoá. Chạm vào nút này để tạo tài khoản. Sau khi thành công, ứng dụng tự động điều hướng sang màn hình Region.

### Bước 7: Chọn Province

Màn hình Region có tiêu đề "Choose your region". Dòng chip ngang đầu tiên là danh sách Province. Vuốt ngang để duyệt và chạm chip mong muốn. Chip được chọn sẽ đổi màu và hiện dấu tick.

### Bước 8: Chọn City

Ngay khi chọn Province, dòng chip City xuất hiện. Chạm chip City tương ứng. Nếu Province chỉ có một City, hệ thống sẽ tự chọn giúp.

### Bước 9: Chọn Ward

Sau khi có City, danh sách Ward (phường/xã) hiển thị dạng chip ngang. Vuốt để tìm và chạm để chọn.

### Bước 10: Nhập Street

Trong ô "Street" (placeholder "Nguyen Hue Street"), gõ tên đường bạn muốn gắn với hồ sơ.

### Bước 11: Chạm Continue

Khi đã có đầy đủ Province, City, Ward và Street khác rỗng, nút "Continue" mở khoá. Chạm để lưu khu vực. Ứng dụng gọi API cập nhật vùng, làm mới thông tin user và chuyển vào MainTabs.

## Xử lý lỗi thường gặp

### "Email, username, and password are required."

Bạn bỏ trống một trong ba trường bắt buộc. Kiểm tra lại và điền đủ.

### "Unable to create account."

Có thể email/username đã tồn tại, hoặc mất mạng. Đổi username khác hoặc kiểm tra kết nối rồi thử lại.

### Không thấy gợi ý username

Gợi ý chỉ xuất hiện khi Full name hoặc phần trước dấu @ của Email đạt ít nhất 2 ký tự. Gõ thêm ký tự và chờ khoảng nửa giây.

### "Unable to load provinces/cities/wards"

Danh sách khu vực tải từ server thất bại. Kiểm tra mạng, kéo lùi rồi mở lại màn hình Region. Nếu vẫn lỗi, đóng và mở lại ứng dụng.

### "Province, city, ward, and street are required."

Bạn bấm Continue khi còn thiếu chip lựa chọn hoặc chưa nhập Street. Kiểm tra bốn phần và bổ sung.

### "Unable to save your region."

Server không lưu được. Kiểm tra kết nối, đảm bảo Street không chỉ có khoảng trắng, rồi thử lại.

## Câu hỏi thường gặp

### Tôi có thể bỏ qua bước Region không?

Không. Register xong bắt buộc phải chọn Region mới vào được MainTabs. Nếu bạn thoát ứng dụng giữa chừng, lần mở lại vẫn ở màn hình Region cho tới khi hoàn tất.

### Tôi có thể đổi khu vực sau này không?

Có. Sau khi vào ứng dụng, bạn có thể cập nhật khu vực trong phần thiết lập hồ sơ.

### Tại sao username của tôi bị đổi khi tôi vừa gõ Full name?

Trong lúc bạn chưa tự nhập ô Username, chip gợi ý đầu tiên sẽ tự điền để tiết kiệm thao tác. Ngay khi bạn chạm vào chip khác hoặc gõ tay ô Username, hệ thống dừng ghi đè.

### Nếu server gợi ý username bị lỗi thì sao?

Ứng dụng sẽ tự tạo gợi ý cục bộ dựa trên tên/email của bạn, gồm ba biến thể có thêm hậu tố số ngẫu nhiên.

### Street có bắt buộc chính xác không?

Đây là mô tả đường dùng cho hồ sơ, không cần trùng khớp bản đồ tuyệt đối. Nhưng nếu để trống hoặc chỉ khoảng trắng, nút Continue sẽ không mở khoá.
