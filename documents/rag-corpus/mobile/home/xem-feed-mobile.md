---
title: Xem feed Home trên ứng dụng di động
slug: xem-feed-mobile
platform: mobile
category: home
tags: [feed, home, story-rail, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Home
---

## Giới thiệu

Home là tab đầu tiên trên bottom tab bar của CafeStory mobile. Màn hình này hiển thị thanh chia sẻ ShareTopBar, StoryRail ngang, và một feed hỗn hợp gồm bài viết BlogFeedCard xen kẽ với thẻ quảng cáo SponsoredCafeCard. Feed hỗ trợ kéo xuống để làm mới (pull-to-refresh) và tự động tải thêm khi bạn cuộn gần cuối (infinite scroll).

Trên ShareTopBar, biểu tượng Send ở góc phải mở màn hình Conversations. Chạm lại tab Home khi bạn đang ở Home sẽ cuộn feed về đầu.

## Điều kiện tiên quyết

Bạn phải đăng nhập và đã hoàn tất chọn khu vực. Thiết bị cần kết nối Internet. Ứng dụng phải đang ở trong MainTabs.

## Các bước thực hiện

### Bước 1: Mở tab Home

Từ bottom tab bar, chạm biểu tượng Home. Ứng dụng sẽ hiển thị màn hình Home với ShareTopBar phía trên và feed phía dưới.

### Bước 2: Duyệt StoryRail

Dòng đầu tiên bên dưới ShareTopBar là StoryRail. Ô đầu tiên là "Your posts" với dấu cộng và viền primary, đại diện cho chính bạn. Các ô tiếp theo là những người bạn theo dõi và các cafe page bạn theo dõi. Vuốt ngang để duyệt.

Chạm ô "Your posts" để mở tab Create. Chạm ô cafe page để mở màn hình chi tiết quán. Chạm ô người dùng để mở hồ sơ của họ.

### Bước 3: Xem bài trong feed

Cuộn xuống để đọc bài viết. Feed gồm hai loại thẻ:

- BlogFeedCard: bài viết từ người dùng hoặc cafe page bạn có thể tương tác (thích, lưu, bình luận, xem chi tiết).
- SponsoredCafeCard: thẻ quảng cáo quán cà phê được tài trợ. Chạm vào để mở trang chi tiết quán tương ứng.

Vị trí và thứ tự các thẻ do server quyết định qua Mixed Feed API.

### Bước 4: Kéo xuống để làm mới

Ở đầu feed, kéo xuống rồi thả tay. Vòng loading (RefreshControl) xuất hiện. Ứng dụng tải lại trang đầu tiên của feed và cập nhật cả StoryRail (danh sách người/quán bạn theo dõi).

### Bước 5: Cuộn để tải thêm

Khi bạn cuộn gần cuối feed, ứng dụng tự động phát hiện qua sự kiện onScroll và tải trang tiếp theo. Trong lúc chờ, khối skeleton hai thẻ giả sẽ hiện ở cuối feed. Bạn không cần chạm nút "Load more".

### Bước 6: Mở Conversations

Chạm biểu tượng Send ở góc phải ShareTopBar để mở màn hình Conversations, xem danh sách hội thoại của bạn.

### Bước 7: Cuộn nhanh về đầu

Nếu đã cuộn xuống sâu, chạm lại biểu tượng Home ở bottom tab bar. Feed sẽ animate cuộn về đầu.

## Xử lý lỗi thường gặp

### "Feed unavailable" kèm dòng "Unable to load feed. Pull down to try again."

Trang đầu tiên tải thất bại (thường do mất mạng). Kéo xuống để refresh; nếu vẫn lỗi, kiểm tra kết nối và thử lại.

### "No posts yet"

Không có bài nào trong feed. Có thể bạn ở khu vực mới hoặc chưa theo dõi ai. Hãy thử tab Explore để theo dõi thêm reviewer và cafe page.

### "Unable to load more posts."

Xảy ra khi tải trang kế tiếp thất bại trong lúc cuộn. Cuộn ngược lên chút rồi cuộn xuống lại, hoặc kéo pull-to-refresh để bắt đầu lại từ đầu.

### Feed không cập nhật sau khi tôi đăng bài mới

Kéo pull-to-refresh ở đầu feed để buộc tải lại. Bài mới sẽ xuất hiện nếu server đã ghi nhận.

### Thẻ SponsoredCafeCard không mở được quán

Đảm bảo thẻ có nút mở quán rõ ràng. Nếu chạm không phản hồi, kéo refresh để tải lại thông tin quảng cáo.

## Câu hỏi thường gặp

### Tại sao đôi lúc feed lại kèm quán cà phê tài trợ?

Feed là mixed feed do server dựng. Quán tài trợ được chèn vào các vị trí cụ thể để bạn khám phá quán mới bên cạnh bài viết.

### Ứng dụng có ghi nhận việc tôi đã thấy bài không?

Có. Mỗi khi trang feed mới tải về, ứng dụng gửi impression các blog trong trang đó để server điều chỉnh gợi ý. Việc này chạy ngầm và không ảnh hưởng đến trải nghiệm.

### StoryRail có giới hạn bao nhiêu người?

Ngoài ô "Your posts", ứng dụng hiển thị tối đa 14 mục theo dõi (kết hợp cả người và cafe page).

### Tôi có thể tắt tự động tải thêm không?

Không. Infinite scroll luôn bật để trải nghiệm mượt hơn, bạn chỉ cần cuộn theo tốc độ thoải mái.

### Chạm tab Home khi đang ở Home có tác dụng gì?

Ứng dụng sẽ animate cuộn về đầu feed. Đây là mẹo nhanh khi bạn đang ở giữa danh sách.
