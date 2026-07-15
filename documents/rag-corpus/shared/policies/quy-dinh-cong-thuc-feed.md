---
title: Quy định công thức xếp hạng bảng tin
slug: quy-dinh-cong-thuc-feed
platform: both
category: feed
tags: [feed, ranking, algorithm, trending, personalization]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Bảng tin Cafe Story không hiển thị theo thứ tự thời gian mà theo điểm xếp hạng tổng hợp. Điểm này ưu tiên các bài viết liên quan đến người dùng, có tương tác tốt, còn mới và tránh lặp lại. Ngoài ra hệ thống áp phạt các bài đã được người dùng xem gần đây và các bài có báo cáo vi phạm để giữ trải nghiệm sạch.

Tài liệu tóm tắt đầy đủ công thức đang chạy để đội sản phẩm, đội phân tích và đội hỗ trợ có thể tra cứu nhất quán. Mọi thay đổi số học đều phải cập nhật ở đây trước khi đưa lên môi trường sản xuất, để bảng tin luôn có nguồn quy chiếu duy nhất.

## Quy định

**1. Bonus liên quan cá nhân.**

- **Bài của chính người dùng**: `+40`. Bài do bạn viết luôn được ưu tiên đưa lên đầu bảng tin cá nhân.
- **Bài của cafe page bạn đang theo dõi**: `+30`. Ưu tiên các trang cafe mà bạn theo dõi để bạn không bỏ lỡ cập nhật.
- **Bài của người bạn đang theo dõi**: `+25`. Kể cả reviewer, tác giả cá nhân — miễn bạn có mối quan hệ theo dõi.
- **Bài cùng khu vực (cùng tỉnh/thành với người dùng)**: `+15`. Địa lý là yếu tố quan trọng cho cộng đồng cafe, giúp gợi ý các quán và sự kiện gần bạn.
- **Bài của reviewer đang hoạt động**: `+10`. Tăng nhẹ để nội dung của reviewer chất lượng có cơ hội hiển thị.

**2. Điểm tương tác.**

Hệ thống cộng điểm dựa trên các chỉ số tương tác thực tế của bài viết:

- Views × 0.1
- Likes × 2
- Comments × 4
- Replies × 4
- Shares × 6
- Saves × 5

Trọng số cao cho share, save, comment và reply nhằm ưu tiên các tương tác thể hiện ý định giữ lại/chia sẻ hơn là chỉ xem lướt.

**3. Freshness và trending.**

- **Freshness**: `exp(-ageHours / 48) × 10`. Bài mới có điểm cao và giảm theo hàm mũ; sau khoảng 48 giờ, freshness giảm nhanh về gần 0.
- **Trending**: cộng thêm `trendingScore × 0.35`. Điểm trending do luồng xếp hạng bài trending (dành cho admin) tính riêng, feed cá nhân dùng với hệ số 0.35 để tránh áp đảo tính cá nhân hóa.

**4. Điểm phạt.**

- Bài có báo cáo vi phạm: `-10` mỗi lần xét (được cấu hình trong ranking service).
- Bài đã bị người dùng đánh dấu "đã xem": `-25`. Có bộ nhớ recent-seen trong 7 ngày để nhớ bài từng phân phối.
- Repetition penalty: mỗi bài trùng tác giả hoặc trùng cafe page kể từ bài thứ 3 trở đi trong cùng đợt xếp hạng bị trừ `10`, tối đa `-30`. Tránh việc một tác giả/page chiếm quá nhiều slot.
- Bài blog ở trạng thái **VIOLATION** bị loại hoàn toàn khỏi bảng tin (không tính điểm nữa).

**5. Feed cá nhân và feed organic.**

Feed cá nhân là feed chính hiển thị cho người dùng đã đăng nhập, dùng đầy đủ các quy tắc trên. Feed organic là feed đại chúng, dùng khi thiếu tín hiệu cá nhân, tính theo công thức:

`score = likes × 2 + comments × 4 + shares × 5 + exp(-ageHours / 36) × 30 + 5 − reports × 10`

Công thức organic không có bonus quan hệ theo dõi và có freshness mạnh hơn (`exp(-ageHours/36)`), ưu tiên bài mới nổi.

**6. Trending admin (bảng xếp hạng độc lập).**

Bảng trending cho admin và các trang khám phá dùng công thức:

`score = views × 0.2 + likes × 2 + comments × 4 + shares × 6 + saves × 5 + freshness − reports × 10`

Trong đó freshness cũng là `exp(-ageHours / 48)`. Bảng này không có bonus quan hệ hay repetition penalty.

**7. Kích thước trang.**

Feed mặc định trả về `20` bài mỗi trang, tối đa `50` bài. Client không được yêu cầu quá 50 để bảo vệ hiệu năng và trải nghiệm cuộn.

## Ngoại lệ

- **Bài của chính người dùng** vẫn nhận `+40` dù có tương tác thấp hay bị báo cáo, miễn không ở trạng thái VIOLATION. Điều này để chủ tài khoản luôn thấy bài của mình khi lướt.
- **Blog VIOLATION** bị loại bỏ ngay bước lọc trước khi tính điểm — không phải chỉ bị trừ điểm.
- **Trending score** áp hệ số 0.35 chứ không cộng thẳng, để feed cá nhân không biến thành bảng trending toàn hệ thống.
- **Recent-seen 7 ngày**: sau 7 ngày, cùng một bài có thể được đề xuất lại cho cùng người dùng và không còn bị phạt `-25`.
- **Repetition penalty tối đa `-30`**: dù có nhiều hơn 3 bài trùng tác giả/page trong đợt, mức phạt cộng dồn không vượt 30 điểm.

## Câu hỏi thường gặp

**Vì sao bài mới đăng của tôi lại lên đầu bảng tin của mình?**
Vì bài của chính bạn được cộng `+40`, cộng với freshness cao, dễ vượt các bài khác.

**Tôi theo dõi một cafe page nhưng bài của họ vẫn chìm, tại sao?**
Có thể bài bị phạt do bạn đã xem (`-25`), do repetition (nếu cùng page xuất hiện nhiều lần), hoặc do freshness thấp (bài cũ). Bonus `+30` không đủ bù nếu tổng phạt lớn.

**Trending khác feed cá nhân thế nào?**
Trending không có bonus quan hệ; feed cá nhân có bonus cho tác giả bạn theo dõi, cafe page bạn theo dõi và khu vực bạn ở.

**Bài bị báo cáo có ẩn khỏi bảng tin không?**
Không tự động. Bài chỉ bị phạt `-10` mỗi lần xét cho tới khi bị đánh dấu **VIOLATION**, lúc đó mới bị loại hoàn toàn.

**Vì sao tôi thấy cùng một bài xuất hiện lại sau nhiều ngày?**
Recent-seen chỉ nhớ trong 7 ngày. Sau khoảng thời gian này, hệ thống có thể đề xuất lại nếu bài vẫn liên quan.

**Tôi có thể lấy 100 bài mỗi trang không?**
Không. Kích thước tối đa là 50; yêu cầu lớn hơn sẽ bị giới hạn lại.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:72-73` — page size mặc định 20 và tối đa 50.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:76` — bonus `+40` cho bài của chính người dùng.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:77` — bonus `+10` cho bài của reviewer đang hoạt động.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:78` — phạt `-25` cho bài đã xem gần đây.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:182` — loại blog VIOLATION khỏi feed.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:412` — bộ nhớ recent-seen 7 ngày.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:429-435` — bảo vệ bổ sung với blog VIOLATION.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:635` — hệ số trending 0.35.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:643` — phạt `-10` mỗi report.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:644` — phạt `-25` cho bài seen.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:807` — bonus `+30` cho cafe page đang theo dõi.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:824-826` — bonus `+25` cho tác giả đang theo dõi.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:836-846` — công thức engagement (views×0.1, likes×2, comments×4, replies×4, shares×6, saves×5).
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:848-861` — repetition penalty +10 mỗi lần trùng từ bài thứ 3, tối đa -30.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:871` — bonus `+15` cho bài cùng khu vực.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:896` — freshness `exp(-ageHours/48) × 10` cho feed cá nhân.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogFeedRankingServiceImpl.java:938-957` — công thức feed organic.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogRankingServiceImpl.java:67-75` — công thức trending cho admin (views×0.2, likes×2, comments×4, shares×6, saves×5, freshness exp(-ageHours/48), reports×-10).
