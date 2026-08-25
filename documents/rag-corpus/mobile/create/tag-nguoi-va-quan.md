---
title: Tag người và chọn địa điểm khi đăng bài mobile
slug: tag-nguoi-va-quan
platform: mobile
category: create
tags: [tag-nguoi, location, create, mobile]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Create
---

## Giới thiệu

Khi soạn bài trong tab Create, bạn có thể mở hai modal riêng biệt để bổ sung ngữ cảnh cho bài viết: modal "Tag people" để đánh dấu người theo dõi vào bài, và modal "Select location" để chọn khu vực đăng bài (Province, City, Ward). Cả hai modal đều mở toàn màn hình theo kiểu slide, có nút mũi tên quay lại ở góc trái và nút "Done" ở góc phải.

Modal "Tag people" chỉ liệt kê người bạn đang theo dõi, phù hợp cho việc gắn thẻ bạn bè hoặc reviewer. Modal "Select location" gọi API vùng để bạn chọn khu vực gắn với bài, sau đó tạo bản ghi region riêng cho blog và trả lại regionId cho draft.

## Điều kiện tiên quyết

Bạn phải đang ở bước compose của tab Create. Kết nối Internet để tải danh sách theo dõi (cho modal people) hoặc tải cây Province/City/Ward (cho modal location). Muốn tag ai, bạn cần theo dõi họ từ trước.

## Các bước thực hiện

### Bước 1: Mở modal Tag people

Trong bước compose, chạm mục "Tag people". Modal mở slide toàn màn hình, header có tiêu đề "Tag people".

### Bước 2: Chờ danh sách following tải xong

Ứng dụng gọi API lấy danh sách người bạn theo dõi, sau đó lấy hồ sơ chi tiết của từng người. Trong lúc chờ, hiện LoadingState "Loading following...".

### Bước 3: Chọn người muốn tag

Mỗi hàng gồm avatar, tên đầy đủ, @username và một vòng tròn tick bên phải. Chạm hàng để bật/tắt. Vòng tròn tick chuyển thành nền primary khi được chọn.

### Bước 4: Chạm Done trong modal Tag people

Chạm "Done" ở góc phải header. Ứng dụng lưu danh sách taggedUserIds vào draft và đóng modal, trở lại bước compose. Người được tag sẽ hiển thị dưới caption trong composer.

### Bước 5: Đóng modal Tag people mà không lưu

Chạm mũi tên quay lại ở góc trái header hoặc dùng cử chỉ back của hệ thống. Modal đóng và giữ nguyên danh sách tag trước đó.

### Bước 6: Mở modal Select location

Trong bước compose, chạm mục chọn location. Modal "Select location" mở, có dòng mô tả nhắc "Pick a post location. Street is not required for blog posts."

### Bước 7: Chọn Province

Dòng chip ngang đầu tiên là danh sách Province. Vuốt ngang, chạm chip mong muốn. Chip được chọn đổi màu và hiện dấu tick.

### Bước 8: Chọn City

Sau khi chọn Province, dòng chip City xuất hiện. Nếu chỉ có một City, ứng dụng tự chọn giúp; nếu nhiều, chạm chip mong muốn.

### Bước 9: (Tuỳ chọn) Chọn Ward

Dòng chip "Ward optional" xuất hiện sau City. Chạm để chọn nếu bạn muốn cụ thể xuống phường/xã. Nếu bỏ qua, location vẫn hợp lệ với Province + City.

### Bước 10: Kiểm tra selected location

Khối "Selected location" hiện tên ghép Ward, City, Province (bỏ qua Ward nếu chưa chọn). Kiểm tra nội dung trước khi lưu.

### Bước 11: Chạm Done trong modal Select location

Chạm "Done" ở góc phải header. Ứng dụng gọi API tạo region với purpose BLOG_LOCATION, nhận regionId, sau đó ghi vào draft.location và đóng modal. Trong lúc lưu, nút "Done" chuyển thành spinner và các thao tác đóng bị vô hiệu.

## Xử lý lỗi thường gặp

### Modal Tag people báo "No following users yet"

Bạn chưa theo dõi ai nên không có ai để tag. Đóng modal, sang tab Explore để theo dõi người dùng/reviewer trước.

### Modal Tag people báo "Unable to load following users."

Kéo mạng gián đoạn hoặc API lỗi. Chạm mũi tên đóng modal rồi mở lại. Nếu vẫn lỗi, thử lại sau vài phút.

### Modal Tag people báo "Unable to find your account"

Có thể phiên đăng nhập gặp trục trặc, không lấy được currentUserId. Đăng xuất và đăng nhập lại, sau đó thử tag lại.

### Modal Select location báo "Unable to load provinces/cities/wards."

Danh sách vùng tải thất bại. Chờ vài giây rồi kéo lùi mở lại modal. Kiểm tra kết nối.

### "Province and city are required." khi bấm Done

Bạn chưa chọn đủ Province và City. Vuốt lên và chọn thêm chip còn thiếu.

### "Unable to save this location."

API createRegion lỗi. Kiểm tra kết nối và thử lại. Nếu vẫn lỗi, chọn tổ hợp Province/City khác gần đó.

### Nút Done không có phản ứng

Trong lúc lưu, ứng dụng vô hiệu nút Done và hiển thị spinner. Chờ vài giây; nếu treo lâu, đóng modal và mở lại.

## Câu hỏi thường gặp

### Tôi có thể tag người tôi chưa theo dõi không?

Không. Modal Tag people chỉ liệt kê những người bạn đang theo dõi. Muốn tag ai, hãy theo dõi họ từ tab Explore hoặc từ hồ sơ trước.

### Location cho bài khác gì với khu vực hồ sơ tôi đã chọn khi đăng ký?

Khi bạn mở tab Create lần đầu, ứng dụng dùng khu vực hồ sơ (regionCity + regionProvince) làm mặc định cho draft. Nếu muốn đăng bài từ một địa điểm khác (ví dụ đi du lịch), mở modal Select location để chọn khác. Việc này không ảnh hưởng đến khu vực hồ sơ.

### Ward có bắt buộc không?

Không. Trong modal Select location, dòng chip Ward có nhãn "Ward optional" và bạn có thể bỏ qua.

### Vì sao chọn location xong lại thấy tên ghép rất dài?

Ứng dụng ghép Ward, City, Province thành một chuỗi để bạn xem tổng quan. Khi hiển thị trong composer, tên có thể được rút gọn theo bố cục.

### Tôi có thể chỉnh danh sách tag đã chọn trước đó không?

Có. Mở lại modal Tag people; các người đã chọn từ lần trước sẽ được đánh dấu sẵn. Bạn có thể bỏ chọn hoặc thêm mới rồi bấm Done.

### Modal đóng bằng cử chỉ back có ghi nhớ lựa chọn không?

Đóng bằng mũi tên hoặc cử chỉ back sẽ không áp dụng thay đổi hiện tại của modal; draft giữ nguyên trạng thái trước đó. Chỉ khi bạn chạm "Done" thì lựa chọn mới được lưu vào draft.
