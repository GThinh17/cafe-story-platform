---
title: Cài đặt tài khoản và đăng xuất
slug: cai-dat-tai-khoan
platform: web
category: profile
tags: [profile, settings, logout, account-switch, theme]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /[username]
---

## Giới thiệu

Hộp thoại cài đặt tài khoản mở ra từ nút ba chấm ở phần đầu trang cá nhân. Đây là nơi bạn truy cập nhanh vào các thao tác quản lý tài khoản mà không cần rời trang: chỉnh sửa hồ sơ, mở bảng điều khiển reviewer (nếu là reviewer), mở trang cafe page (nếu bạn là chủ trang cafe), và đăng xuất khỏi phiên hiện tại.

Ngoài ra, ở phần chuyển tài khoản, Cafe Story hiển thị thông tin phiên đăng nhập hiện tại (tên tài khoản, họ tên hiển thị, email) và cho phép bạn kết thúc phiên để quay về màn hình đăng nhập, sẵn sàng đăng nhập bằng tài khoản khác. Việc chuyển đổi giao diện sáng/tối được quản lý bằng công tắc chủ đề trong thanh điều hướng và tự lưu theo tùy chọn hệ thống của trình duyệt.

## Điều kiện tiên quyết

- Bạn đã đăng nhập vào Cafe Story.
- Bạn đang mở trang cá nhân của chính mình (`/[username]`). Nút ba chấm mở cài đặt chỉ hiển thị khi bạn xem hồ sơ của mình.
- Nếu muốn thấy mục **Reviewer dashboard**, tài khoản của bạn phải có vai trò reviewer đang hoạt động.
- Nếu muốn thấy mục **Cafe page**, tài khoản phải có vai trò chủ trang cafe hoặc đã có trang cafe.

## Các bước thực hiện

### Bước 1: Mở hộp thoại cài đặt

Vào trang cá nhân của bạn (`/[username]`). Ở phần đầu hồ sơ, cạnh tên đăng nhập và huy hiệu reviewer, bấm nút biểu tượng ba chấm (`MoreHorizontal`). Hộp thoại **Profile settings** hiện ra ở giữa màn hình.

### Bước 2: Chỉnh sửa hồ sơ

Trong hộp thoại, chọn **Edit profile** để đi đến trang chỉnh sửa hồ sơ (`/[username]/edit`). Hộp thoại tự đóng khi bạn chuyển trang. Xem hướng dẫn chi tiết ở tài liệu "Chỉnh sửa thông tin cá nhân".

### Bước 3: Mở Reviewer dashboard (nếu có)

Nếu tài khoản bạn có vai trò reviewer, hộp thoại sẽ hiển thị thêm mục **Reviewer dashboard**. Bấm để vào trang bảng điều khiển reviewer, nơi xem điểm, tương tác và trạng thái payout.

### Bước 4: Mở trang Cafe page (nếu có)

Nếu bạn là chủ trang cafe, sẽ có mục **Cafe page**. Bấm để đi đến trang chi tiết cafe của bạn hoặc trang chỉnh sửa cafe khi chưa có trang chi tiết.

### Bước 5: Đổi giao diện sáng/tối

Đóng hộp thoại (bấm **Cancel** hoặc nhấn Escape). Sử dụng công tắc chủ đề ở thanh điều hướng để chuyển giữa chế độ sáng và tối. Tùy chọn được lưu trong trình duyệt và áp dụng cho mọi trang Cafe Story trong phiên đăng nhập của bạn.

### Bước 6: Đăng xuất

Mở lại hộp thoại **Profile settings** và bấm **Log out**. Nút hiển thị **"Logging out..."** trong khi hệ thống hủy phiên. Sau khi kết thúc, bạn được chuyển tới `/login` và mọi thông tin phiên trên trình duyệt được xóa.

### Bước 7: Chuyển sang tài khoản khác

Sau khi đăng xuất, tại màn hình đăng nhập, bạn nhập tên tài khoản và mật khẩu của tài khoản khác. Cafe Story chưa hỗ trợ "chuyển tài khoản không đăng xuất" trực tiếp trên web — mỗi phiên gắn với một tài khoản, đăng xuất là cách chuẩn để chuyển tài khoản.

## Xử lý lỗi thường gặp

**Không thấy nút ba chấm ở trang cá nhân.**
Bạn đang xem hồ sơ của người khác. Nút cài đặt chỉ hiện trên hồ sơ của chính bạn. Vào trang cá nhân của mình rồi thử lại.

**Đăng xuất bị kẹt ở "Logging out..."**
Kết nối mạng bị gián đoạn khi hệ thống gọi API đăng xuất. Dù vậy, sau khi tiến trình kết thúc, phiên trên trình duyệt của bạn vẫn được xóa cục bộ và bạn được đưa về `/login`. Nếu vẫn không thoát, hãy làm mới trang.

**Sau khi đăng xuất tôi vẫn thấy tên mình ở góc.**
Làm mới trang bằng `Ctrl` + `F5`. Trạng thái phiên đã được xóa nhưng vài trang có thể cần tải lại để cập nhật UI.

**Không thấy mục Reviewer dashboard dù tôi đã đăng ký reviewer.**
Vai trò reviewer chỉ hiện khi tài khoản có role chứa từ "reviewer" và còn hoạt động. Nếu gói đã hết hạn (`reviewer_active = false`), mục này có thể bị ẩn cho đến khi bạn gia hạn.

**Không thấy mục Cafe page dù tôi có cafe.**
Trang cafe cần đúng vai trò (`cafe`, `page_owner`, hoặc `cafe_page`) hoặc đã có đường dẫn trang cafe tồn tại. Nếu vẫn thiếu, thử đăng xuất và đăng nhập lại để phiên nạp lại các vai trò mới nhất.

## Câu hỏi thường gặp

**Đăng xuất trên web có làm mất phiên đăng nhập trên di động không?**
Không. Mỗi thiết bị giữ phiên riêng. Đăng xuất trên trình duyệt chỉ hủy phiên của trình duyệt đó.

**Tôi có thể duy trì đăng nhập nhiều tài khoản trên cùng trình duyệt không?**
Trên web hiện tại, mỗi trình duyệt chỉ duy trì một phiên đăng nhập. Nếu cần nhiều tài khoản song song, hãy dùng cửa sổ ẩn danh hoặc trình duyệt khác cho tài khoản thứ hai.

**Chọn giao diện tối lưu ở đâu?**
Cafe Story lưu tùy chọn chủ đề trong trình duyệt. Nếu đổi máy hoặc xóa dữ liệu trình duyệt, tùy chọn sẽ trở về mặc định theo hệ điều hành.

**Sau khi đăng xuất, các bài viết đã lưu có mất không?**
Không. Bài viết đã lưu, đánh giá và tương tác gắn với tài khoản trên máy chủ, không phải phiên cục bộ. Khi bạn đăng nhập lại, mọi thứ vẫn còn.

**Tôi bấm Log out nhưng chuyển đến trang khác thay vì `/login`?**
Sau đăng xuất, hệ thống điều hướng về `/login`. Nếu chưa thấy, hãy đợi thêm vài giây hoặc gõ trực tiếp `/login` vào thanh địa chỉ.
