---
title: Đăng nhập vào CafeStory
slug: dang-nhap
platform: web
category: auth
tags: [dang-nhap, login, auth, tai-khoan]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /login
---

# Đăng nhập vào CafeStory trên web

## Giới thiệu

Trang đăng nhập cho phép bạn truy cập vào tài khoản CafeStory bằng email hoặc tên người dùng (username) đã đăng ký, kèm mật khẩu. Sau khi đăng nhập thành công, hệ thống sẽ đưa bạn về trang chủ hoặc trở lại đúng trang bạn đã cố truy cập trước đó.

## Điều kiện tiên quyết

- Bạn đã có tài khoản CafeStory (nếu chưa có, chuyển sang trang Register để tạo).
- Trình duyệt cho phép lưu cookie phiên đăng nhập của cafestory.com.
- Có kết nối internet ổn định để trao đổi dữ liệu với máy chủ.

## Các bước thực hiện

### Bước 1: Mở trang đăng nhập

Truy cập đường dẫn "/login" của CafeStory. Bạn sẽ thấy tiêu đề "Welcome back" cùng khung nhập thông tin đăng nhập.

### Bước 2: Nhập email hoặc tên người dùng

Điền vào ô "Email or username" bằng địa chỉ email hoặc username bạn đã dùng khi đăng ký. Trường này bắt buộc.

### Bước 3: Nhập mật khẩu

Nhập mật khẩu tại ô "Password". Mật khẩu phân biệt chữ hoa/thường. Nếu quên mật khẩu, bấm vào liên kết "Forgot Password?" phía trên bên phải ô mật khẩu.

### Bước 4: Gửi biểu mẫu đăng nhập

Bấm nút "Sign in" ở cuối biểu mẫu. Trong lúc chờ, nút sẽ hiển thị trạng thái "Please wait..." và tạm khoá để tránh gửi trùng.

### Bước 5: Xử lý sau đăng nhập

Khi đăng nhập thành công, hệ thống chuyển bạn về trang chủ. Nếu trước đó bạn đã cố truy cập vào một trang cần đăng nhập, CafeStory sẽ đưa bạn trở lại đúng trang đó.

### Bước 6: Chuyển sang tài khoản khác khi cần

CafeStory hiện chỉ giữ một phiên đăng nhập tại một thời điểm. Để đổi sang tài khoản khác, mở hộp thoại "Account" từ khu vực avatar, bấm nút "Logout" để kết thúc phiên hiện tại, sau đó quay lại trang đăng nhập và nhập thông tin của tài khoản mới.

### Bước 7: Đăng ký nếu chưa có tài khoản

Nếu bạn chưa có tài khoản, cuộn xuống cuối khung đăng nhập và bấm liên kết "Join the club" bên cạnh dòng "Don't have an account?". Bạn sẽ được chuyển đến trang Register.

## Xử lý lỗi thường gặp

### Sai email/username hoặc mật khẩu

Nếu thông tin không khớp, hệ thống hiển thị thông báo lỗi ngay dưới biểu mẫu. Kiểm tra lại chính tả, tình trạng phím Caps Lock và thử nhập lại. Nếu vẫn không được, dùng chức năng khôi phục mật khẩu.

### Trang yêu cầu đăng nhập lại

Khi phiên đăng nhập hết hạn, bạn sẽ thấy thông báo "Please sign in or register to continue." ở đầu trang. Đăng nhập lại để tiếp tục thao tác.

### Nút "Sign in" không phản hồi

Nếu nút vẫn ở trạng thái "Please wait...", kiểm tra kết nối mạng và tải lại trang bằng Ctrl+F5. Nếu vẫn không được, đóng và mở lại trình duyệt.

## Câu hỏi thường gặp

### Tôi có thể đăng nhập bằng Google hoặc Apple không?

Hai nút "Google" và "Apple" hiển thị dưới phần "Or continue with". Tính năng đăng nhập bằng nhà cung cấp bên thứ ba sẽ được kích hoạt theo lộ trình sản phẩm; hiện tại vui lòng dùng email/username và mật khẩu.

### Sau khi đăng nhập tôi có tự động đăng nhập ở lần sau không?

CafeStory lưu phiên đăng nhập bằng cookie an toàn cho đến khi bạn bấm "Logout" hoặc phiên hết hạn. Không cần chọn thêm tuỳ chọn nào để duy trì đăng nhập trong cùng trình duyệt.

### Có thể mở đồng thời nhiều tài khoản trên cùng trình duyệt không?

Không. Mỗi cửa sổ trình duyệt chỉ giữ một phiên. Muốn dùng tài khoản khác cùng lúc, hãy sử dụng cửa sổ ẩn danh hoặc profile trình duyệt riêng.
