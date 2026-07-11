---
title: Đăng ký tài khoản CafeStory
slug: dang-ky
platform: web
category: auth
tags: [dang-ky, register, auth, dia-chi, username]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /register
---

# Đăng ký tài khoản CafeStory trên web

## Giới thiệu

Trang đăng ký giúp bạn tạo tài khoản mới trên CafeStory. Biểu mẫu bao gồm thông tin cá nhân cơ bản, gợi ý username tự động dựa trên họ tên đầy đủ, và phần địa chỉ Việt Nam theo cấp tỉnh/thành - quận/huyện - phường/xã. Sau khi đăng ký thành công, hệ thống tự đăng nhập và đưa bạn về trang chủ.

## Điều kiện tiên quyết

- Địa chỉ email hợp lệ và chưa được đăng ký trên CafeStory.
- Họ tên đầy đủ (Full name) để hệ thống gợi ý username.
- Có kết nối internet để tải danh mục địa chỉ Việt Nam (tỉnh, huyện, xã).

## Các bước thực hiện

### Bước 1: Mở trang đăng ký

Truy cập đường dẫn "/register" hoặc bấm liên kết "Join the club" từ trang đăng nhập. Bạn sẽ thấy tiêu đề "Create your account".

### Bước 2: Nhập họ tên đầy đủ

Nhập họ tên vào ô "Full name". Sau khoảng nửa giây, CafeStory sẽ gọi máy chủ để đề xuất một vài username phù hợp. Trong lúc chờ, dòng "Finding usernames..." sẽ hiện dưới ô nhập.

### Bước 3: Chọn hoặc để hệ thống gán username

Khi danh sách gợi ý xuất hiện, bấm chọn một username. Ô "Username" nằm ngay dưới sẽ tự điền theo lựa chọn của bạn và không cho phép sửa trực tiếp; muốn đổi username khác, hãy chỉnh lại họ tên hoặc chọn một gợi ý khác.

### Bước 4: Nhập email và mật khẩu

Điền địa chỉ email vào ô "Email address" và tạo mật khẩu tại ô "Password". Mật khẩu cần đủ mạnh (bao gồm chữ, số và ký tự đặc biệt) để đảm bảo bảo mật; ràng buộc chi tiết được máy chủ kiểm tra khi bạn gửi biểu mẫu.

### Bước 5: Chọn địa chỉ (tuỳ chọn)

Phần địa chỉ dành cho tài khoản tại Việt Nam. Bạn có thể để trống tất cả, nhưng nếu đã nhập bất kỳ trường nào thì phải chọn đủ tỉnh và phường/xã.

1. Bấm "Province / city" và tìm tỉnh/thành phố của bạn trong danh sách.
2. Sau khi chọn tỉnh, mục "City / district" mở khoá — chọn quận/huyện tương ứng.
3. Cuối cùng, chọn "Ward" (phường/xã).
4. Có thể nhập thêm "Area" (khu vực, mốc dân dụng) và "Street" (số nhà, tên đường) để chi tiết hơn.

### Bước 6: Chấp nhận điều khoản

Đánh dấu ô "I agree to the Terms of Service and Privacy Policy." Đây là mục bắt buộc.

### Bước 7: Gửi đăng ký

Bấm nút "Create account". Hệ thống sẽ tạo tài khoản, đăng nhập tự động và, nếu bạn đã điền địa chỉ, lưu địa chỉ đó vào hồ sơ. Sau đó bạn được chuyển về trang chủ.

## Xử lý lỗi thường gặp

### Email hoặc username đã tồn tại

Nếu email hoặc username trùng với người dùng khác, thông báo lỗi hiển thị dưới biểu mẫu. Đổi sang email khác hoặc chọn một username gợi ý khác rồi thử lại.

### Không tải được danh mục địa chỉ

Nếu bạn thấy thông báo "Unable to load Vietnam address data.", kiểm tra mạng và tải lại trang. Nếu muốn tạo tài khoản ngay, hãy để trống hết phần địa chỉ và cập nhật sau khi đăng nhập.

### Đăng ký thành công nhưng địa chỉ chưa lưu

Có thể xảy ra khi máy chủ tạo tài khoản thành công nhưng bước lưu địa chỉ bị lỗi. CafeStory sẽ giữ tài khoản của bạn và hiển thị lời nhắn yêu cầu cập nhật địa chỉ sau khi đăng nhập.

### Mật khẩu bị từ chối

Nếu máy chủ trả về lỗi liên quan tới mật khẩu, hãy đặt lại mật khẩu dài hơn, kết hợp chữ hoa/thường, số và ký tự đặc biệt.

## Câu hỏi thường gặp

### Tôi có bắt buộc phải nhập địa chỉ khi đăng ký không?

Không. Toàn bộ khối địa chỉ là tuỳ chọn. Nếu bỏ trống, bạn vẫn tạo được tài khoản và có thể bổ sung địa chỉ ở trang cài đặt cá nhân sau này.

### Vì sao tôi không tự nhập username theo ý mình?

Ô "Username" chỉ chấp nhận giá trị từ danh sách gợi ý để đảm bảo username hợp lệ và không trùng lặp. Muốn có username khác, thay đổi họ tên rồi chọn lại từ danh sách gợi ý mới.

### Sau khi đăng ký tôi được đưa đến đâu?

Hệ thống tự đăng nhập bằng tài khoản mới rồi chuyển bạn về trang chủ. Nếu bạn đăng ký từ đường dẫn có tham số "next", CafeStory sẽ đưa bạn về trang đó thay vì trang chủ.
