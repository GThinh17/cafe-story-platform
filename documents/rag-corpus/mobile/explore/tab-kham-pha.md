---
title: Chuyển tab trên màn hình Explore mobile
slug: tab-kham-pha
platform: mobile
category: explore
tags: [explore, tabs, trending, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Explore
---

## Giới thiệu

Explore trên mobile có bốn tab con nằm ngay dưới ô tìm kiếm: all, cafes, reviewers và trending. Ba tab đầu sử dụng thành phần ExploreRecommendationList để đề xuất tài khoản/quán bạn có thể theo dõi; tab trending dùng BlogFeedCard để cuộn qua bài viết đang thịnh hành trong 24 giờ gần nhất.

Mọi tab đều hỗ trợ pull-to-refresh. Dữ liệu tab được nạp lười (chỉ khi bạn mở tab lần đầu) và cache lại đến khi bạn thoát Explore hoặc chạm refresh.

## Điều kiện tiên quyết

Bạn phải đăng nhập, đã hoàn tất Region và đang ở tab Explore. Kết nối Internet ổn định. Ô tìm kiếm phía trên phải rỗng (không ở chế độ tìm kiếm), vì khi có query, ExploreTabs sẽ bị ẩn.

## Các bước thực hiện

### Bước 1: Mở tab Explore

Chạm biểu tượng Explore trên bottom tab bar. Ô tìm kiếm ở trên, dòng ExploreTabs ở dưới ô tìm kiếm.

### Bước 2: Xem tab all (mặc định)

Tab all được chọn mặc định. Tiêu đề "Recommended for you" hiện phía trên danh sách. Nội dung là gợi ý hỗn hợp cafe page và reviewer. Chạm mục để mở chi tiết.

### Bước 3: Chuyển sang tab cafes

Chạm chip "cafes" trên dòng ExploreTabs. Tiêu đề đổi thành "Suggested cafes". Danh sách chỉ gồm cafe page đề xuất, tối đa 20 mục. Chạm mục để mở màn hình chi tiết quán.

### Bước 4: Chuyển sang tab reviewers

Chạm chip "reviewers". Tiêu đề đổi thành "Explore reviewers". Danh sách là các reviewer được gợi ý, tối đa 20 mục. Chạm mục để mở hồ sơ reviewer.

### Bước 5: Chuyển sang tab trending

Chạm chip "trending". Tiêu đề đổi thành "Trending posts". Ứng dụng nạp tối đa 12 bài blog thịnh hành trong cửa sổ 24 giờ và hiển thị bằng BlogFeedCard giống Home. Bạn có thể thích, lưu, mở chi tiết như bình thường.

### Bước 6: Pull-to-refresh tab đang mở

Từ đầu nội dung, kéo xuống rồi thả tay. Vòng loading xuất hiện. Ứng dụng gọi lại API cho đúng tab đang chọn: recommendation nếu là all/cafes/reviewers, trending blogs nếu là trending.

### Bước 7: Chuyển lại tab đã xem trước

Chạm chip của tab bạn đã xem trước đó. Vì dữ liệu tab đó đã cache, ứng dụng hiển thị ngay mà không tải lại. Nếu muốn cập nhật, kéo pull-to-refresh.

## Xử lý lỗi thường gặp

### "No recommendations yet" ở tab all/cafes/reviewers

Chưa có dữ liệu gợi ý. Có thể tài khoản mới hoặc server đang tính toán lại. Kéo pull-to-refresh sau vài phút.

### "No trending posts yet" ở tab trending

Không có bài nào đủ điều kiện trending trong 24 giờ. Quay lại sau, hoặc chuyển sang tab all để khám phá gợi ý người/quán.

### Thông báo lỗi hiển thị nguyên văn từ server ở đầu danh sách

API gợi ý hoặc trending trả lỗi. Kéo pull-to-refresh trên chính tab đó. Nếu vẫn lỗi, chuyển sang tab khác rồi quay lại.

### Skeleton hiện lâu

Ứng dụng đang tải trang. Chờ vài giây; nếu quá lâu, kiểm tra kết nối và pull-to-refresh.

### Tab không đổi khi chạm

Đảm bảo ô tìm kiếm phía trên không có query. Khi có nội dung trong ô tìm kiếm, dòng ExploreTabs bị ẩn để nhường chỗ cho kết quả tìm.

## Câu hỏi thường gặp

### Tab all có phải là union của cafes và reviewers không?

Tab all gọi API Mixed Recommendations rồi lọc lấy các mục có targetType là CAFE_PAGE hoặc REVIEWER. Nó khác cafes/reviewers ở chỗ hỗn hợp cả hai và có thể theo tỷ lệ khác.

### Trending dùng cửa sổ thời gian nào?

Ứng dụng gọi API trending với window HOUR_24, tức chỉ tính hoạt động trong 24 giờ gần nhất.

### Vì sao đổi tab lại nhanh, còn lần đầu thì thấy skeleton?

Lần đầu mở tab, dữ liệu chưa cache nên phải gọi API. Sau khi có kết quả, kết quả được lưu trong state và hiển thị lại tức thì khi bạn quay lại tab đó.

### Tôi có thể theo dõi ngay trong danh sách gợi ý không?

Có. Chạm mục để mở màn hình chi tiết người/quán và bấm Follow ở đó.

### Kéo refresh ở tab trending có gọi lại cả recommendation không?

Không. Pull-to-refresh chỉ tải lại đúng dữ liệu của tab đang mở.
