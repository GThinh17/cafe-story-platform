---
title: Quy định tài khoản, mật khẩu và phiên đăng nhập
slug: quy-dinh-tai-khoan-mat-khau
platform: both
category: auth
tags: [auth, password, username, token, session, follow]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài khoản Cafe Story là gốc của mọi thao tác trên nền tảng: đăng bài, đánh giá, theo dõi, thanh toán, đăng ký reviewer. Vì vậy quy định về định danh và bảo mật tài khoản phải rõ ràng và nhất quán giữa web và mobile. Tài liệu này tổng hợp toàn bộ quy định liên quan đến tên đăng nhập, mật khẩu, trạng thái tài khoản, token phiên và một số ràng buộc quan hệ như tự theo dõi bản thân.

Cafe Story không dùng xác minh OTP hay xác minh email ở phiên bản hiện tại. Bảo mật đăng nhập dựa trên cặp email/username + mật khẩu và cơ chế access token / refresh token do backend cấp. Việc siết chặt các quy tắc này ở tầng mã nguồn giúp giảm rủi ro tài khoản trùng, tài khoản mạo danh và tên đăng nhập gây nhầm lẫn.

## Quy định

**1. Mật khẩu từ 6 đến 255 ký tự.**

Trường `password` khi đăng ký phải có độ dài từ 6 đến 255 ký tự. Không có yêu cầu bắt buộc về ký tự đặc biệt, chữ hoa hay số ở tầng mã nguồn hiện tại — người dùng nên chọn mật khẩu mạnh theo khuyến nghị chung.

**2. Tên đăng nhập tuân theo biểu thức chính quy nghiêm ngặt.**

`userName` phải khớp regex `^[a-z0-9](?!.*[._]{2})[a-z0-9._]{3,8}[a-z0-9]$`, đồng nghĩa:

- Chỉ chứa chữ cái thường `a-z`, chữ số `0-9`, dấu chấm `.` và dấu gạch dưới `_`.
- Bắt đầu và kết thúc bằng chữ hoặc số, không được bắt đầu hoặc kết thúc bằng dấu chấm/gạch dưới.
- Không được có hai ký tự `.` hoặc `_` liền nhau (không có `..`, `__`, `._`, `_.`).
- Độ dài tổng cộng từ 5 đến 10 ký tự.

Quy định này áp dụng cho cả bước đăng ký ban đầu và bước đổi tên đăng nhập (nếu có).

**3. Danh sách tên đăng nhập bị cấm (reserved).**

Không được đăng ký hoặc đổi tên đăng nhập trùng với các từ khóa hệ thống: `admin`, `root`, `login`, `register`, `api`, `me`, `support`, `cafestory`. Các từ này được kiểm tra không phân biệt hoa/thường và được kiểm tra sau khi đã chuẩn hóa xuống chữ thường.

**4. Email và tên đăng nhập là duy nhất.**

Mỗi email chỉ được gắn với một tài khoản; mỗi `userName` cũng phải duy nhất. Ràng buộc này được thực thi ở tầng cơ sở dữ liệu (unique index), do đó thao tác đăng ký với email hoặc tên đăng nhập đã tồn tại sẽ trả lỗi ngay lập tức.

**5. Không có OTP và xác minh email ở phiên bản hiện tại.**

Sau khi đăng ký thành công, tài khoản có thể đăng nhập ngay. Không cần nhập mã OTP qua email/SMS và không có bước xác minh email. Chính sách này có thể thay đổi trong tương lai; nếu thay đổi sẽ được cập nhật ở đây.

**6. Thời gian sống của access token và refresh token.**

- **Access token**: khoảng 25 giờ (chính xác `90000` giây, tương đương ~1500 phút).
- **Refresh token**: 7 ngày (`604800` giây).

Access token đi kèm mỗi yêu cầu HTTP để xác thực; hết hạn thì client dùng refresh token để lấy access token mới. Khi refresh token hết hạn, người dùng phải đăng nhập lại.

**7. Tài khoản bị vô hiệu hoá (soft ban) không thể sử dụng.**

Nếu `accountStatus = false`, mọi luồng yêu cầu qua middleware xác thực đều bị chặn với mã **HTTP 403 FORBIDDEN**. Người dùng không thể đăng bài, đánh giá, theo dõi, thanh toán hoặc bất kỳ hành động ghi nào. Trạng thái này do admin đặt trong công cụ quản trị.

**8. Không được theo dõi chính mình.**

Ở tầng dịch vụ theo dõi (`UserFollowServiceImpl`), yêu cầu `followerUserId == followingUserId` bị từ chối. Đây là ràng buộc kỹ thuật để tránh vòng tự tham chiếu làm rối bộ đếm followers/following và làm sai các gợi ý theo mối quan hệ trên bảng tin.

## Ngoại lệ

- **Tài khoản admin** vẫn tuân theo cùng quy tắc mật khẩu và regex tên đăng nhập, nhưng có bộ vai trò khác. Admin không có ưu đãi bỏ qua kiểm tra tên cấm hay unique.
- **Đăng nhập bằng nhà cung cấp bên ngoài (nếu có trong tương lai)** có thể không phải nhập mật khẩu Cafe Story; tuy nhiên tại phiên bản hiện tại, đăng nhập luôn dùng cặp email/username + mật khẩu do người dùng đặt.
- **Người dùng bị soft ban vẫn giữ dữ liệu.** Bài viết, đánh giá và mối quan hệ vẫn tồn tại nhưng không thể tạo mới hay chỉnh sửa. Nếu được kích hoạt lại, các thao tác trở lại bình thường.
- **Tự theo dõi thông qua tài khoản phụ.** Hệ thống không có cơ chế phát hiện tài khoản phụ; ràng buộc chỉ chặn ID trùng nhau ở cùng một tài khoản.

## Câu hỏi thường gặp

**Vì sao tên đăng nhập `admin` không được chấp nhận?**
Vì `admin` nằm trong danh sách tên cấm để tránh nhầm lẫn và giả mạo. Danh sách còn bao gồm `root`, `login`, `register`, `api`, `me`, `support`, `cafestory`.

**Tôi muốn dùng tên `John.Doe` được không?**
Không, vì tên phải là chữ thường. Ngoài ra, độ dài của tổ hợp trên có thể vượt 10 ký tự. Hãy dùng `john.doe` nếu đủ điều kiện (5–10 ký tự và không có ký tự đặc biệt liền nhau).

**Mật khẩu 4 ký tự có được không?**
Không. Tối thiểu 6 ký tự. Khuyến nghị chọn mật khẩu dài, kết hợp chữ, số và ký tự đặc biệt.

**Tôi phải nhập OTP khi đăng ký không?**
Không. Phiên bản hiện tại không có OTP hay xác minh email khi đăng ký.

**Bao lâu tôi phải đăng nhập lại?**
Khi refresh token hết hạn (7 ngày). Trong lúc còn phiên, mỗi 25 giờ access token sẽ được gia hạn tự động qua refresh token.

**Tại sao tôi không thể tự bấm follow chính mình?**
Vì hệ thống chặn ràng buộc kỹ thuật để tránh sai số liệu followers/following và các suy diễn quan hệ trong bảng tin.

**Tôi có thể đổi email hay tên đăng nhập không?**
Đổi email cần liên hệ hỗ trợ vì email là định danh duy nhất. Đổi tên đăng nhập (nếu tính năng bật) vẫn phải tuân theo cùng regex và danh sách tên cấm.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/requestDTO/RegisterRequest.java:19-20` — ràng buộc độ dài mật khẩu 6–255.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AuthServiceImpl.java:39-53` — regex tên đăng nhập, độ dài 5–10 và kiểm tra reserved.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AuthServiceImpl.java:281-285` — danh sách reserved `admin, root, login, register, api, me, support, cafestory`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/User.java:46-49` — ràng buộc unique cho email và userName.
- `1-cafe-story-backend-javaspring/src/main/resources/application.properties:63-64` — thời gian sống access token 90000 giây và refresh token 604800 giây.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/validation/UserValidator.java:26-33` — chặn tài khoản có `accountStatus = false` với 403 FORBIDDEN.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/UserFollowServiceImpl.java:65` — chặn tự theo dõi trong luồng follow.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/UserFollowServiceImpl.java:218` — chặn tự theo dõi trong luồng liên quan.
