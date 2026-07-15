---
title: Xem và quản lý bài viết của mình trên web
slug: quan-ly-bai-viet-cua-toi
platform: web
category: blog
tags: [blog, profile, archive, quan-ly-bai]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /[username]
  - /[username]/archive
---

# Xem và quản lý bài viết của mình trên web

## Giới thiệu

Trang cá nhân trên CafeStory hiển thị toàn bộ bài viết bạn đã đăng, đã lưu, và đã chia sẻ trong một lưới ảnh 3–4 cột. Ngoài ra có một trang riêng gọi là Archive để xem các bài ở trạng thái ẩn (`HIDDEN`) — chỉ chủ tài khoản mới xem được. Trang này giúp bạn theo dõi lịch sử hoạt động của mình.

Lưu ý: **giao diện web hiện tại chưa có nút sửa caption, xoá bài, ẩn bài hay lưu trữ (archive) chủ động trên chính bài viết của bạn.** Dropdown menu "..." ở góc bài trong feed chỉ dành cho bài của người khác (dùng để Report). Bài của bạn sẽ tự vào Archive nếu bị moderation chuyển sang trạng thái `HIDDEN`.

## Điều kiện tiên quyết

- Bạn phải đăng nhập tài khoản CafeStory.
- Đã có ít nhất một bài đăng, hoặc bài đã lưu / chia sẻ nếu muốn xem tab tương ứng.
- Trang Archive chỉ mở được nếu URL trùng username của tài khoản đang đăng nhập; hệ thống tự redirect về trang cá nhân nếu không phải chủ.

## Các bước thực hiện

### Bước 1: Vào trang cá nhân

Bấm avatar của bạn ở góc trên bên phải hoặc bấm dòng tên tài khoản trong sidebar. Trang mở tại đường dẫn `/<username>` với header hồ sơ (avatar, tên, stats), section quán bạn sở hữu (nếu có), và ba tab lưới bài ở phía dưới.

### Bước 2: Chuyển giữa các tab bài

Ngay dưới header có bộ tab (`Tabs`) với ba mục:

- **Posts** (`posts`) — các bài bạn đã đăng dưới danh nghĩa cá nhân.
- **Shared** (`shared`) — các bài bạn đã chia sẻ.
- **Saved** (`saved`) — các bài bạn đã lưu để đọc lại. Tab này chỉ có bạn thấy, người khác vào trang của bạn không xem được.

Bấm vào tab tương ứng, lưới bên dưới cập nhật danh sách theo tab đang chọn.

### Bước 3: Mở chi tiết một bài

Click vào ô ảnh bất kỳ trong lưới. Ứng dụng mở modal chi tiết bài (post modal) để bạn xem đầy đủ caption, ảnh, và bình luận. Từ modal chi tiết, bạn có thể like, comment, share hoặc save bài như trên feed.

### Bước 4: Vào Archive để xem bài đang ẩn

Từ trang cá nhân, mở đường dẫn `/<username>/archive` (thường có link "Archive" ở menu quản lý hồ sơ, hoặc gõ trực tiếp vào URL). Trang Archive có header:

- Nút mũi tên quay lại trang cá nhân.
- Tiêu đề "Archive" và dòng phụ "Hidden posts — only you can see this."

Bên dưới là lưới ảnh giống trang chính, hiển thị các bài `HIDDEN` (bao gồm bài bị chuyển ẩn sau khi moderation kết luận cần admin xem xét hoặc bị admin đặt trạng thái HIDE). Bài không có ảnh sẽ hiển thị bằng placeholder.

Bấm vào bài trong Archive để mở modal chi tiết như tab Posts.

### Bước 5: Thao tác với bài của người khác

Nếu bạn xem trang cá nhân của người khác, tab Saved không hiển thị. Trên feed hoặc trong modal chi tiết bài của người khác, nút "..." (menu) mở dropdown với một mục duy nhất: **Report** — dùng để báo cáo bài vi phạm. Không có Edit / Delete cho bài của người khác (theo đúng thiết kế).

## Xử lý lỗi thường gặp

### "No hidden posts yet." trong Archive

Bạn chưa có bài nào ở trạng thái `HIDDEN`. Điều này bình thường; bài chỉ vào Archive khi hệ thống hoặc admin chuyển trạng thái. Không cần thao tác thêm.

### Truy cập `/<username>/archive` của người khác bị chuyển hướng

Đúng theo thiết kế. Trang Archive chỉ dành cho chủ tài khoản; ứng dụng tự redirect về `/<username>` nếu bạn không phải chủ trang.

### Không tìm thấy nút sửa hoặc xoá bài của mình

Đây là hạn chế hiện tại của bản web: **chưa có UI edit / delete / hide chủ động trên bài viết của bạn**. Nếu cần xoá hoặc sửa nội dung bài đã đăng, liên hệ CafeStory qua kênh hỗ trợ ngoài (fanpage, email) kèm mã bài để admin xử lý thủ công.

### Bài của tôi biến mất khỏi tab Posts

Có ba khả năng: (1) bài bị moderation chuyển `HIDDEN` — kiểm tra Archive; (2) bài bị moderation kết luận `DENIED` và chuyển `REMOVED` — thấy trong tab Notifications loại Moderation; (3) sự cố hiển thị tạm thời — thử tải lại trang (Ctrl+F5).

### Lưới bài quá tải với nhiều bài

Grid trong tab tự phân trang / lazy load khi cuộn. Nếu cuộn lâu không thấy bài cũ, quay về đầu trang, chọn lại tab để reset danh sách.

## Câu hỏi thường gặp

### Tab nào chỉ có tôi mới xem được?

Tab **Saved** (bài đã lưu) chỉ hiển thị khi bạn xem trang cá nhân của chính mình. Người khác vào trang của bạn không thấy tab này. Tab Posts và Shared thì công khai với mọi người.

### Trang Archive có xoá bài không?

Không. Archive chỉ để **xem lại** bài ở trạng thái `HIDDEN`. Không có nút xoá, khôi phục, hay chuyển trạng thái từ giao diện. Việc khôi phục bài từ HIDDEN về PUBLISHED cần admin can thiệp qua backend.

### Vì sao web không cho tôi sửa hay xoá bài đã đăng?

Phiên bản web hiện tại chỉ tập trung vào flow đăng bài + kiểm duyệt AI. Chức năng edit / delete từ giao diện chưa được triển khai. Nếu cần thao tác, liên hệ CafeStory qua kênh hỗ trợ.

### Menu "..." trên bài của tôi có gì?

Với bài của chính bạn trong feed, menu "..." **không hiện** — dropdown chỉ render khi bài là của người khác (`!isOwnPost`). Nên bạn sẽ không thấy tuỳ chọn nào trên bài của mình từ feed.

### Tab Reviews có tồn tại không?

Trên bản web hiện tại, ba tab được implement là Posts, Shared, Saved. Nếu bạn thấy tab "Reviews" ở đâu đó trong app, đó là section riêng thuộc trang quán (danh sách review cho cafe page) chứ không phải tab trên profile cá nhân.

### Tôi vào Archive nhưng không thấy bài mới bị ẩn hôm nay

Có thể do cache. Bấm nút quay lại rồi vào Archive lần nữa, hoặc tải lại trang. Nếu vẫn không thấy, bài của bạn có thể ở trạng thái `REMOVED` (bị moderation reject) chứ không phải `HIDDEN`, nên không xuất hiện ở Archive.
