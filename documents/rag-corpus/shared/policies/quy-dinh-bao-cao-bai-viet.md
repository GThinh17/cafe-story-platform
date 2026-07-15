---
title: Quy định báo cáo bài viết
slug: quy-dinh-bao-cao-bai-viet
platform: both
category: blog
tags: [report, moderation, blog, notifications, feed]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài liệu này mô tả quy trình khi người dùng CafeStory báo cáo một bài blog: cách hệ thống tạo moderation job, cơ chế retry của AI, timeout xử lý, các hành động admin có thể chọn và cách bài viết bị vi phạm được ẩn khỏi feed. Áp dụng cho cả web và mobile.

## Quy định

### 1. Tạo moderation job khi user report

Khi user gửi report, hệ thống tạo một **moderation job** trong database và đẩy vào hàng đợi xử lý. Chi tiết tại `service/serviceImplement/ReportModerationServiceImpl.java` (dòng 49-52).

Mỗi report tạo tối đa một job đang hoạt động cho cùng một blog; các report lặp lại chỉ tăng số lượng, không tạo job mới.

### 2. Cơ chế retry với exponential backoff

Nếu AI moderation gặp lỗi tạm thời (timeout, quota, network), hệ thống retry tối đa **3 lần** với backoff:

- Lần retry 1: sau **30 giây**
- Lần retry 2: sau **2 phút**
- Lần retry 3: sau **10 phút**

Cùng file `ReportModerationServiceImpl.java`. Sau 3 lần thất bại, job được chuyển sang trạng thái lỗi để admin xử lý thủ công.

### 3. Stale-processing timeout

Nếu một job đang ở trạng thái processing quá **10 phút** mà không hoàn tất (worker chết, mất kết nối...), hệ thống xem là stale và cho phép reclaim để xử lý lại.

Chi tiết tại `ReportModerationServiceImpl.java` (dòng 451-458).

Cơ chế này đảm bảo không có job bị "kẹt" vô hạn vì worker crash.

### 4. Các hành động admin sau khi review

Admin có 3 hành động chốt cho mỗi report:

| Hành động | Ý nghĩa                                                             |
|-----------|---------------------------------------------------------------------|
| APPROVE   | Nội dung không vi phạm, giữ nguyên bài                              |
| HIDE      | Ẩn bài khỏi feed công khai (tác giả và moderator vẫn xem được)      |
| REMOVE    | Gỡ bài, đánh dấu `VIOLATION`, loại khỏi mọi feed                    |

Các giá trị này được định nghĩa trong `entity/enums/ModerationResolveAction.java`.

### 5. Thông báo tới người dùng

Tác giả bài viết nhận thông báo về kết quả moderation qua tab **Notifications** với type `BLOG_MODERATION`. Nội dung nêu rõ hành động của admin và (nếu có) lý do.

Người báo cáo cũng có thể nhận thông báo cảm ơn đã báo cáo tuỳ cấu hình.

### 6. Bài bị REMOVED loại khỏi feed

Blog có trạng thái `VIOLATION` (kết quả của hành động `REMOVE`) bị loại khỏi **mọi feed**: feed cá nhân, feed công khai, feed cafe page, kết quả tìm kiếm, feed đề xuất.

Chi tiết tại `BlogFeedRankingServiceImpl.java` (dòng 182, 429-435).

Tác giả vẫn có thể thấy bài trong trang cá nhân với nhãn cảnh báo (tuỳ implement UI); nhưng không xuất hiện với người khác.

## Ngoại lệ

- Blog đã `REMOVED` không được restore tự động; muốn phục hồi cần admin tác động thủ công.
- Report trùng lặp trên cùng blog không tạo job mới, tránh spam moderation queue.
- Nếu AI moderation fail cả 3 lần retry, job phải chờ admin xử lý; không tự động approve/hide.
- Stale-processing 10 phút được reset khi worker hợp lệ nhận lại job.

## Câu hỏi thường gặp

**1. Tôi report một bài, bao lâu có kết quả?**
AI xử lý trong vài giây đến vài phút; nếu cần admin review thủ công, có thể mất lâu hơn tuỳ hàng đợi.

**2. Vì sao báo cáo của tôi bị hệ thống nói "đã tồn tại"?**
Cùng một blog chỉ có một job moderation đang hoạt động. Report lặp được ghi nhận nhưng không tạo job mới.

**3. Bài của tôi bị REMOVE, làm sao khiếu nại?**
Vào tab Notifications, mở thông báo `BLOG_MODERATION` để xem lý do; liên hệ hỗ trợ nếu bạn cho rằng quyết định sai.

**4. Bài đã HIDE có bị xoá vĩnh viễn không?**
Không. HIDE chỉ ẩn khỏi feed công khai; tác giả và admin vẫn thấy. REMOVE mới là mức nghiêm khắc hơn.

**5. Tại sao bài `VIOLATION` biến mất khỏi trang khám phá của tôi?**
Vì blog vi phạm bị loại khỏi mọi feed theo quy định tại `BlogFeedRankingServiceImpl`.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReportModerationServiceImpl.java` (dòng 49-52, 451-458)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/enums/ModerationResolveAction.java`
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java` (dòng 182, 429-435)
