---
title: Đăng nhập trên ứng dụng di động
slug: dang-nhap-mobile
platform: mobile
category: auth
tags: [dang-nhap, xac-thuc, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Login
---

## Giới thiệu

Màn hình Login là điểm ra vào chính của ứng dụng CafeStory trên React Native. Người dùng đã có tài khoản CafeStory sẽ dùng màn hình này để đăng nhập bằng cặp thông tin định danh (email hoặc username) và mật khẩu. Sau khi đăng nhập thành công, ứng dụng sẽ chuyển vào MainTabs với bottom tab bar (Home, Explore, Create, Notifications, Profile). Nếu tài khoản chưa chọn khu vực, người dùng sẽ được điều hướng qua màn hình Region trước khi vào feed.

Màn hình Login trên mobile hiện tại không có OTP, không có đăng nhập bằng Google/Apple, không có "Quên mật khẩu". Người dùng chỉ có hai lựa chọn: đăng nhập bằng tài khoản đã có hoặc tạo tài khoản mới thông qua link "Create a CafeStory account".

## Điều kiện tiên quyết

Bạn cần có tài khoản CafeStory đã đăng ký từ trước (qua web hoặc mobile). Cần biết chính xác email hoặc username, cùng mật khẩu tương ứng.

Thiết bị cần được kết nối Internet ổn định. Ứng dụng CafeStory mobile phải được cài đặt và mở lên; nếu bạn đang ở màn hình Register hoặc Region, hãy quay lại Login bằng nút "I already have an account" hoặc "Back to sign in".

## Các bước thực hiện

### Bước 1: Mở màn hình Welcome back

Mở ứng dụng CafeStory. Nếu bạn chưa đăng nhập, hệ thống sẽ tự động hiển thị màn hình Login với tiêu đề "Welcome back" và dòng phụ đề mô tả "Sign in to keep collecting cafe stories, saved corners, and reviews."

### Bước 2: Nhập email hoặc username

Chạm vào trường "Email or username" (placeholder "hello@cafestory.com"). Bàn phím sẽ hiện lên. Trường này không tự động viết hoa chữ cái đầu (autoCapitalize=none) nên bạn có thể gõ trực tiếp username in thường hoặc địa chỉ email đầy đủ.

Chú ý loại bỏ khoảng trắng thừa ở đầu hoặc cuối. Hệ thống sẽ tự cắt khoảng trắng khi gửi lên nhưng tốt nhất là nhập chính xác.

### Bước 3: Nhập mật khẩu

Chuyển sang trường "Password" (placeholder "Enter your password"). Mật khẩu được ẩn dưới dạng chấm (secureTextEntry). Gõ mật khẩu chính xác theo phân biệt hoa/thường.

### Bước 4: Chạm nút Sign in

Khi cả hai trường đều đã có nội dung, nút "Sign in" sẽ chuyển từ trạng thái vô hiệu sang trạng thái có thể chạm. Chạm vào nút này để gửi yêu cầu đăng nhập.

Trong lúc chờ phản hồi, nút chuyển sang trạng thái loading và không thể chạm lại. Nếu đăng nhập thành công, ứng dụng chuyển sang MainTabs. Nếu chưa hoàn tất khu vực, hệ thống sẽ đưa bạn sang màn hình Region trước.

### Bước 5: (Tuỳ chọn) Chuyển sang Register

Nếu bạn chưa có tài khoản, hãy chạm vào dòng chữ "Create a CafeStory account" ở dưới nút Sign in. Ứng dụng sẽ điều hướng sang màn hình Register.

## Xử lý lỗi thường gặp

### "Email/username and password are required."

Thông báo này xuất hiện khi một trong hai trường bị bỏ trống hoặc chỉ chứa khoảng trắng. Nhập lại đầy đủ email/username và mật khẩu rồi chạm Sign in.

### "Unable to sign in."

Đây là thông báo chung khi server trả lỗi. Nguyên nhân phổ biến là sai email/username, sai mật khẩu, hoặc mất mạng. Kiểm tra kết nối, kiểm tra bàn phím có gõ nhầm phím Caps Lock hay không, và thử lại.

### Nút Sign in không kích hoạt

Nút "Sign in" bị vô hiệu khi trường identifier hoặc password rỗng. Đảm bảo bạn đã gõ nội dung thực chứ không chỉ khoảng trắng.

### Bàn phím che nút Sign in

Cuộn nhẹ nội dung form lên phía trên hoặc đóng bàn phím bằng cách chạm ra ngoài trường nhập rồi chạm lại nút Sign in.

## Câu hỏi thường gặp

### Tôi quên mật khẩu thì làm sao?

Hiện tại màn hình Login trên mobile chưa có luồng "Quên mật khẩu". Bạn cần đặt lại mật khẩu qua kênh khác (ví dụ web hoặc liên hệ hỗ trợ) rồi quay lại đăng nhập.

### Tôi có thể đăng nhập bằng Google/Apple không?

Chưa. Màn hình Login mobile chỉ hỗ trợ cặp identifier + password. Không có nút đăng nhập mạng xã hội.

### Tại sao đăng nhập xong lại vào Choose your region thay vì Home?

Nếu tài khoản chưa gắn khu vực, ứng dụng bắt buộc chọn tỉnh, thành phố, phường và đường trước khi cho vào feed. Hoàn tất Region là bạn vào Home ngay.

### Tôi có bị đăng xuất khi tắt ứng dụng không?

Không. Phiên đăng nhập được lưu, lần mở tiếp theo sẽ tự vào MainTabs mà không cần nhập lại. Bạn chỉ cần đăng nhập lại khi phiên hết hạn hoặc chủ động đăng xuất.
