---
title: Xem danh sách người theo dõi và đang theo dõi
slug: xem-nguoi-theo-doi
platform: web
category: profile
tags: [profile, followers, following, follow-button]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /[username]
---

## Giới thiệu

Mỗi hồ sơ Cafe Story hiển thị ba con số ở phần đầu trang: **posts** (số bài viết), **following** (số người mà chủ hồ sơ đang theo dõi) và **followers** (số người theo dõi chủ hồ sơ). Bấm vào con số **followers** hoặc **following** sẽ mở một hộp thoại danh sách người dùng, cho phép bạn tra cứu tài khoản, xem hồ sơ nhanh và bấm theo dõi hoặc bỏ theo dõi ngay trong danh sách.

Tính năng này dùng chung cho hồ sơ của chính bạn và hồ sơ người khác. Trên hồ sơ của người khác, bạn còn thấy các nút hành động ngay bên cạnh (Follow, Message). Trên hồ sơ của mình, ba con số vẫn bấm được để bạn xem lại cộng đồng của mình.

## Điều kiện tiên quyết

- Bạn đã đăng nhập Cafe Story bằng một tài khoản còn hoạt động (một số hành động như theo dõi bắt buộc phải đăng nhập).
- Bạn đang ở trang cá nhân của một tài khoản tồn tại (`/[username]`), không phải trang lỗi 404 hay trang chưa nạp xong.
- Trình duyệt kết nối được với máy chủ Cafe Story để tải danh sách quan hệ theo dõi và thông tin chi tiết từng người dùng.

## Các bước thực hiện

### Bước 1: Truy cập hồ sơ cần xem

Vào trang cá nhân của người dùng bằng cách bấm vào tên hoặc ảnh đại diện của họ ở bất kỳ đâu (bảng tin, bình luận, kết quả tìm kiếm), hoặc gõ trực tiếp `/[username]` trên thanh địa chỉ.

### Bước 2: Bấm vào con số followers hoặc following

Ở phần đầu hồ sơ, dòng thống kê hiển thị "N posts", "N following", "N followers". Ba con số này là các nút bấm được. Bấm vào **followers** để mở danh sách những người đang theo dõi hồ sơ này, hoặc bấm **following** để mở danh sách những người mà hồ sơ này đang theo dõi. Số **posts** hiện tại không mở danh sách.

### Bước 3: Duyệt danh sách

Hộp thoại **Followers** hoặc **Following** hiện ra với tiêu đề tương ứng. Danh sách được tải lên gồm avatar, tên đăng nhập (`userName`) và họ tên đầy đủ (`userFullName`, nếu có). Trong khi tải, hệ thống hiển thị các thẻ chờ (skeleton) để bạn biết đang lấy dữ liệu.

### Bước 4: Tìm nhanh trong danh sách

Ở đầu hộp thoại có ô **Search**. Gõ một phần tên đăng nhập hoặc họ tên; danh sách sẽ được lọc ngay lập tức không phân biệt hoa thường. Xóa nội dung tìm kiếm để trở lại danh sách đầy đủ.

### Bước 5: Mở hồ sơ hoặc theo dõi từ danh sách

Bấm vào bất kỳ dòng nào để đi đến hồ sơ của người đó (hộp thoại tự đóng khi bạn điều hướng). Ở hồ sơ đích, nếu chưa theo dõi, bạn có thể bấm **Follow** để bắt đầu theo dõi; nếu đã theo dõi, nút chuyển thành trạng thái đang theo dõi và bấm lại sẽ hủy.

### Bước 6: Đóng hộp thoại

Bấm ra ngoài hộp thoại, bấm nút đóng hoặc nhấn phím **Escape** để đóng danh sách và quay lại trang hồ sơ.

## Xử lý lỗi thường gặp

**"Unable to load users."**
Máy chủ không trả được danh sách. Kiểm tra kết nối mạng và thử mở lại hộp thoại. Nếu vẫn lỗi, có thể một số tài khoản đã bị vô hiệu hóa hoặc dịch vụ tạm gián đoạn.

**"No followers yet." hoặc "No following yet."**
Hồ sơ này chưa có người theo dõi hoặc chưa theo dõi ai. Đây không phải lỗi.

**"No users found."**
Từ khóa tìm kiếm không khớp với ai trong danh sách. Hãy xóa bớt ký tự hoặc thử tên khác.

**Nút Follow không phản hồi.**
Bạn có thể chưa đăng nhập hoặc phiên đã hết hạn. Đăng nhập lại và thử lại. Ngoài ra, bạn không thể tự theo dõi chính mình — hệ thống sẽ từ chối yêu cầu này.

**Con số followers/following không cập nhật ngay.**
Số liệu ở phần đầu hồ sơ được nạp lúc mở trang; sau khi bạn theo dõi/bỏ theo dõi, có thể cần làm mới trang để con số cập nhật.

## Câu hỏi thường gặp

**Ai có thể xem danh sách followers và following?**
Bất kỳ người dùng đã đăng nhập nào cũng có thể xem. Cafe Story không có chế độ hồ sơ riêng tư ẩn danh sách này.

**Vì sao tôi thấy có người trong danh sách nhưng không mở được hồ sơ?**
Nếu tài khoản đã bị vô hiệu (soft ban) hoặc xóa, hệ thống có thể không trả về thông tin chi tiết. Dòng đó sẽ không hiển thị hoặc trả về lỗi.

**Tôi bấm vào ô "posts" nhưng không có gì xảy ra, có phải lỗi không?**
Không. Hiện tại chỉ **followers** và **following** mở hộp thoại danh sách. **Posts** chỉ là số liệu hiển thị; bạn xem bài viết trực tiếp ở lưới bài của hồ sơ.

**Ô tìm kiếm có tìm cả trong người tôi chưa từng biết không?**
Ô tìm kiếm lọc trong danh sách đang được nạp (tối đa toàn bộ followers hoặc following của hồ sơ đó). Nó không tìm khắp Cafe Story; cần dùng trang tìm kiếm chung cho mục đích đó.

**Tại sao tôi không thể tự theo dõi chính mình?**
Cafe Story chặn hành vi tự theo dõi ở tầng dịch vụ. Bạn chỉ có thể theo dõi các tài khoản khác.
