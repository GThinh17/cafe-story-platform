---
title: Xem followers và following trên ứng dụng di động
slug: xem-followers-mobile
platform: mobile
category: profile
tags: [followers, following, profile, mobile, tim-kiem]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - ProfileFollows
---

# Xem followers và following trên ứng dụng di động CafeStory

## Giới thiệu

Màn hình ProfileFollows tổng hợp hai danh sách quan hệ theo dõi của một hồ sơ: những người đang theo dõi hồ sơ (Followers) và những đối tượng mà hồ sơ đang theo dõi (Following). Trên ứng dụng di động, following bao gồm cả tài khoản người dùng và trang quán (cafe page), được trình bày dạng danh sách phẳng. Có ô tìm kiếm inline ở đầu danh sách, hai tab đếm số ở đầu màn hình và pull-to-refresh để tải lại.

## Điều kiện tiên quyết

Bạn cần đăng nhập tài khoản CafeStory và có thể truy cập hồ sơ nào đó (của bạn hoặc của người khác đủ điều kiện xem). Ứng dụng cần kết nối mạng để tải danh sách followers và following. Số lượng lớn có thể mất vài giây để tải xong lần đầu.

## Các bước thực hiện

### Bước 1: Mở màn hình ProfileFollows

Từ ProfileScreen (tab Profile), phần stats hiện ba con số: posts, followers, following. Chạm vào số "followers" để mở màn hình với tab "Followers" chọn sẵn. Chạm vào số "following" để mở với tab "Following" chọn sẵn. Ứng dụng chuyển sang màn hình mới với tiêu đề ở giữa header là username của hồ sơ đang xem.

Bạn cũng có thể mở từ hồ sơ người khác (OtherUserProfile) theo cùng cách: chạm vào số followers hoặc following.

### Bước 2: Đọc header và tabs

Thanh top có mũi tên quay lại ở góc trái, tiêu đề là tên hiển thị của hồ sơ, và biểu tượng "UserPlus" ở góc phải (nút tìm người - dự phòng cho phiên bản sau). Ngay dưới header là dải hai tab: "Followers: N" và "Following: N", với N là tổng số ở mỗi danh sách. Tab đang chọn có gạch chân đen dày và chữ đậm.

### Bước 3: Tìm kiếm inline

Ngay dưới tabs là ô search với biểu tượng kính lúp bên trái và TextInput bên phải, placeholder "Search". Nhập tên hoặc username muốn tìm; ứng dụng lọc danh sách hiện tại theo tab đang chọn, so khớp không phân biệt hoa thường trên username, tên đầy đủ (userFullName), tên hiển thị của cafe page (displayName), pageName, username của cafe page và thành phố. Ô search có autoCapitalize="none" để tránh viết hoa tự động khi bạn nhập username.

Không có nút "Tìm" - ứng dụng lọc trực tiếp mỗi khi bạn gõ. Xoá ô để thấy lại toàn bộ danh sách.

### Bước 4: Đọc hàng người dùng (User row)

Với danh sách Followers, toàn bộ hàng là người dùng. Với Following, hàng có thể là user hoặc cafe page. Hàng user hiển thị avatar tròn bên trái, username và tên đầy đủ ở giữa, và các nút hành động bên phải (Follow, Message) tuỳ theo trạng thái quan hệ.

Chạm vào phần avatar hoặc tên để mở hồ sơ của người đó. Nếu người đó chính là bạn (userId trùng), ứng dụng chuyển tới tab Profile chính chủ. Ngược lại, ứng dụng mở OtherUserProfile với userId và userName tương ứng.

Chạm nút Message để tạo hội thoại 1-1 với người đó qua createDirectConversation, sau đó chuyển sang màn hình ChatDetail. Chạm Follow để gọi followUser; số follower của user đó tăng lên 1 lạc quan và trạng thái isFollowing cập nhật thành true.

### Bước 5: Đọc hàng cafe page

Trong tab Following, các cafe page mà hồ sơ đang theo dõi hiển thị dạng FollowingCafePageRow. Bên trái là avatar quán, ở giữa là tên hiển thị (displayName hoặc pageName) và thành phố (city). Bên phải có badge "Page" với biểu tượng cửa hàng (Store) trên nền hồng nhạt để phân biệt với hàng user.

Chạm vào avatar hoặc phần tên để mở màn hình CafeDetail của trang quán đó. Không có nút Follow ở đây vì đây là danh sách những gì hồ sơ đang theo dõi.

### Bước 6: Kéo xuống để làm mới

FlatList hỗ trợ RefreshControl - kéo xuống từ đỉnh để tải lại cả followers và following. Vòng xoay xuất hiện ở đầu danh sách trong lúc chờ. Sau khi tải xong, danh sách đếm số ở tabs cũng cập nhật theo.

### Bước 7: Xử lý danh sách trống

Nếu chưa có dữ liệu, FlatList hiển thị EmptyState với tiêu đề "No followers yet" hoặc "No following yet" tuỳ tab. Nếu bạn đang tìm kiếm và không có kết quả, khối trống chuyển thành "No people found" với gợi ý "Try another name or username.". Khi màn hình đang tải lần đầu, danh sách hiển thị ListRowSkeletonList thay cho EmptyState để báo hiệu đang chờ dữ liệu.

## Xử lý lỗi thường gặp

### "Unable to load follows."

Lỗi khi gọi getFollowersByUserId hoặc getFollowingTargetsByUserId. Kiểm tra kết nối mạng, kéo xuống để làm mới. Nếu vẫn lỗi, quay lại và mở lại màn hình.

### Không tạo được hội thoại khi chạm Message

Thông báo "Unable to start this conversation." hiện dưới ô search. Có thể do người đó đã chặn bạn hoặc có hạn chế phía máy chủ. Thử lại sau, hoặc mở hồ sơ và dùng nút Message ở đó.

### Nút Follow không phản hồi

Nút bị vô hiệu khi có một yêu cầu Follow khác đang chạy hoặc khi đối tượng là chính bạn. Chờ một giây rồi thử lại. Nếu vẫn không được, kéo xuống làm mới danh sách.

### Danh sách quá dài, tìm không thấy

Dùng ô search inline để lọc. Ô tìm kiếm khớp cả username, tên đầy đủ và thành phố của cafe page, nên gõ một phần từ khoá đủ để thu hẹp.

## Câu hỏi thường gặp

### Following của tôi có tách người và cafe page không?

Trên di động, danh sách Following là một danh sách phẳng hỗn hợp: hàng người và hàng cafe page. Cafe page có badge "Page" bên phải để phân biệt.

### Tôi có thể unfollow từ đây không?

Bản hiện tại có nút Follow trên hàng user để theo dõi. Muốn unfollow, mở hồ sơ hoặc trang quán tương ứng rồi thao tác ở đó.

### Có phân trang không?

Danh sách được tải một lần bằng hai API followers và following, sau đó lọc client-side theo ô search. Với hồ sơ có số lượng cực lớn, ứng dụng có thể mất vài giây tải; sẽ hỗ trợ phân trang trong phiên bản sau.

### Tôi có thấy được followers của người khác không?

Có, nếu ứng dụng cho phép xem hồ sơ của họ. Bạn có thể mở màn hình ProfileFollows từ số liệu trên OtherUserProfile.

### Số hiển thị ở tab và ở Profile có khớp không?

Số trên tab được tính từ danh sách đã tải về của phiên hiện tại. Con số trên Profile lấy từ trường userFollower và followingCount. Sai lệch nhỏ có thể xảy ra ngay sau khi ai đó vừa follow hoặc unfollow, kéo xuống làm mới để đồng bộ.
