---
title: Quy định đánh giá sao cho blog và cafe page
slug: quy-dinh-danh-gia-sao
platform: both
category: rating
tags: [rating, sao, blog, cafe-page, review]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Đánh giá sao là công cụ định lượng chính để cộng đồng Cafe Story bày tỏ mức độ hài lòng với một bài viết (blog) hoặc một trang cafe (cafe page). Điểm đánh giá dùng làm dữ liệu tổng hợp cho các bảng xếp hạng, gợi ý bài viết và uy tín reviewer. Vì đây là dữ liệu ảnh hưởng trực tiếp đến hiển thị công khai, hệ thống áp đặt các quy tắc chặt chẽ để tránh gian lận và bảo đảm mỗi người dùng chỉ đóng góp một tiếng nói cho mỗi đối tượng.

Tài liệu này gộp cả quy định cho **blog rating** (đánh giá bài viết) và **cafe page rating** (đánh giá trang cafe) vì hai luồng chia sẻ cùng thang điểm, cùng nguyên tắc một-người-một-phiếu và cùng yêu cầu tài khoản đang hoạt động.

## Quy định

**1. Thang điểm bắt buộc là số nguyên từ 1 đến 5.**

Điểm đánh giá (`rating`) luôn là số nguyên và nằm trong khoảng `1 ≤ rating ≤ 5`. Không chấp nhận điểm 0, điểm âm, điểm lớn hơn 5, hoặc điểm nửa vời (ví dụ 4.5). Ràng buộc này được kiểm tra ở cả tầng DTO và tầng entity, do đó bất kỳ giá trị nằm ngoài phạm vi đều bị từ chối trước khi lưu vào cơ sở dữ liệu.

**2. Một người dùng chỉ có một phiếu đánh giá cho mỗi đối tượng.**

Với blog, cặp `(user_id, blog_id)` là khóa duy nhất. Với cafe page, cặp `(user_id, cafe_page_id)` là khóa duy nhất. Khi cùng một người dùng gửi đánh giá lần thứ hai cho cùng blog hoặc cafe page:

- Hệ thống sẽ **cập nhật** phiếu cũ, không tạo phiếu mới.
- Giá trị `rating` mới thay thế giá trị `rating` cũ; các trường ghi chú (nếu có) cũng được cập nhật.

Điều này bảo đảm không ai có thể inflate điểm đánh giá của một đối tượng bằng cách gửi nhiều phiếu.

**3. Chỉ được đánh giá blog ở trạng thái PUBLISHED.**

Blog phải có trạng thái `PUBLISHED` (đã xuất bản) tại thời điểm đánh giá. Nếu blog đang ở trạng thái khác (`DRAFT`, `PENDING`, `HIDDEN`, `VIOLATION`, ...), hệ thống trả về **HTTP 409 CONFLICT** kèm thông báo nêu rõ trạng thái hiện tại. Quy tắc này tránh việc đánh giá tùy tiện những bài chưa được chủ bài viết đưa ra công khai hoặc đã bị gỡ.

**4. Tài khoản người dùng phải đang hoạt động.**

Chỉ tài khoản có `accountStatus = true` (đang hoạt động) mới được phép gửi đánh giá. Tài khoản bị vô hiệu hoá (soft ban) hoặc chưa được kích hoạt sẽ nhận lỗi từ chối ở tầng dịch vụ và không thể ghi phiếu.

**5. Điểm được tính trung bình và không đảo ngược ẩn.**

Điểm trung bình hiển thị của blog hoặc cafe page là trung bình cộng của tất cả phiếu hiện có ở đối tượng đó, làm tròn theo quy tắc hiển thị của giao diện. Khi một phiếu bị chỉnh sửa (cùng người, cùng đối tượng), giá trị mới thay giá trị cũ và điểm trung bình được tính lại tức thì.

## Ngoại lệ

- **Chủ đối tượng vẫn có thể đánh giá.** Hệ thống hiện tại không cấm chủ blog hoặc chủ cafe page tự đánh giá chính mình. Chính sách sản phẩm có thể siết chặt về sau, nhưng ở cấp mã nguồn chưa có ràng buộc này.
- **Blog bị VIOLATION không thể nhận đánh giá.** Ngoài quy tắc chung yêu cầu PUBLISHED, blog bị đánh dấu vi phạm còn bị loại khỏi bảng tin và mọi thao tác tương tác mới, kể cả đánh giá.
- **Reviewer bị ngừng hoạt động** (hết hạn gói hoặc `reviewer_active = false`) vẫn có thể đánh giá với tư cách người dùng thường, không nhận điểm reviewer từ tương tác trên đánh giá đó.
- **Cập nhật phiếu không tạo thông báo mới.** Vì bản chất là update, chủ đối tượng không nhận thông báo lặp lại khi cùng một người sửa điểm.

## Câu hỏi thường gặp

**Tôi có thể đánh giá 0 sao vì rất tệ không?**
Không. Điểm nhỏ nhất là 1 sao. Nếu muốn phản ánh trải nghiệm tiêu cực, hãy chấm 1 sao và viết bình luận kèm theo.

**Vì sao tôi bấm gửi phiếu lần hai nhưng số phiếu không tăng?**
Vì mỗi người chỉ được một phiếu cho một đối tượng. Lần gửi thứ hai chỉ cập nhật giá trị của phiếu cũ chứ không tạo phiếu mới.

**Tôi bị lỗi 409 khi đánh giá một bài viết, nghĩa là gì?**
Bài viết không ở trạng thái PUBLISHED. Có thể bài đã bị đưa về nháp, ẩn, hoặc bị đánh dấu vi phạm. Bạn không thể đánh giá cho đến khi bài trở lại trạng thái công khai.

**Người bị soft ban có thể đánh giá không?**
Không. Tài khoản có `accountStatus = false` bị chặn ở tầng dịch vụ và mọi yêu cầu ghi đánh giá đều bị từ chối.

**Đánh giá có ảnh hưởng đến điểm reviewer không?**
Đánh giá không phải là loại tương tác trực tiếp trong công thức reviewer (like/comment/share). Tuy nhiên, điểm trung bình cao có thể thúc đẩy bảng tin phân phối bài của reviewer rộng hơn, gián tiếp tạo thêm like/comment/share.

**Chủ bài viết có thể xoá phiếu của tôi không?**
Không. Chủ bài viết không có công cụ xoá phiếu của người khác. Việc kiểm duyệt là trách nhiệm của admin và chỉ áp dụng khi phát hiện gian lận hoặc vi phạm chính sách.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/requestDTO/BlogRatingRequestDTO.java:12-13` — ràng buộc `rating` từ 1 đến 5 ở tầng request cho blog.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/requestDTO/CafePageRatingRequestDTO.java:12-13` — ràng buộc `rating` từ 1 đến 5 ở tầng request cho cafe page.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/BlogRating.java:46-49` — ràng buộc `rating` 1–5 ở tầng entity BlogRating.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/BlogRating.java:27` — khóa duy nhất `(user_id, blog_id)` cho blog rating.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/CafePageRating.java:27` — khóa duy nhất `(user_id, cafe_page_id)` cho cafe page rating.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogRatingServiceImpl.java:55-60` — logic cập nhật phiếu cũ khi cùng người rate lại cùng blog.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogRatingServiceImpl.java:111-116` — chỉ cho phép đánh giá blog PUBLISHED (409 CONFLICT nếu khác).
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/BlogRatingServiceImpl.java:52-53` — yêu cầu người dùng đang hoạt động (`accountStatus = true`).
