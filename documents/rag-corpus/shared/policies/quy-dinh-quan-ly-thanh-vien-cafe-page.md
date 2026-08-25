---
title: Quy định quản lý thành viên cafe page
slug: quy-dinh-quan-ly-thanh-vien-cafe-page
platform: both
category: cafe-page
tags: [cafe-page, members, roles, subscription, extra-fee, status]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài liệu này mô tả quy định về vai trò thành viên trong một **cafe page**, giới hạn số lượng thành viên mặc định, cách gia hạn thời hạn hoạt động của trang và các ràng buộc về quyền sở hữu. Áp dụng cho toàn bộ chủ quán và nhân sự quản lý trên CafeStory (web và mobile).

## Quy định

### 1. Danh sách vai trò

Cafe page có 3 vai trò thành viên (lưu dưới dạng string `roleName` trong bảng `page_member`):

- `OWNER`: chủ sở hữu, quyền cao nhất.
- `CO_OWNER`: đồng chủ, có quyền quản lý và đăng bài dưới danh nghĩa quán.
- `MEMBER`: thành viên thường, không có quyền quản lý.

Các giá trị được khai báo dưới dạng hằng trong `entity/PageMember.java` (`ROLE_OWNER`, `ROLE_CO_OWNER`, `ROLE_MEMBER`). Không có vai trò `STAFF` riêng - nhân sự quản lý được gán `CO_OWNER`.

### 2. Quyền đăng bài và quản lý

Chỉ **OWNER** và **CO_OWNER** ở trạng thái `ACTIVE` mới được:

- Đăng bài dưới danh nghĩa quán.
- Chỉnh sửa thông tin cafe page.
- Duyệt / mời / gỡ thành viên.

Kiểm tra tại `validation/CafePageValidator.java` (dòng 40-68) qua các phương thức `validateUserCanCreateBlogOnPage` và `validateUserCanManagePage`. Người dùng không thoả điều kiện nhận `HTTP 403 FORBIDDEN`.

### 3. Giới hạn số thành viên mặc định

Cafe page mới tạo có `maxMembers = 2` (bao gồm chính chủ và tối đa 1 người nữa).

- Mặc định định nghĩa tại `entity/CafePage.java` (dòng 74-76).
- Giá trị mặc định khi tính extra fee ở `service/serviceImplement/ExtraFeeServiceImpl.java` (dòng 20).

Chủ quán muốn mở rộng số lượng thành viên phải thanh toán phụ phí theo mỗi thành viên tăng thêm; công thức xử lý ở `ExtraFeeServiceImpl`.

### 4. Ràng buộc validate maxMembers

`maxMembers` phải ≥ **1**. Giá trị 0 hoặc âm bị từ chối tại tầng validation trong `ExtraFeeServiceImpl.java` (dòng 85-88).

Trong thực tế con số thấp nhất có ý nghĩa là 1 (chỉ chủ quán); dưới 1 là dữ liệu không hợp lệ.

### 5. Gia hạn cộng dồn thời hạn cafe page

Khi chủ quán mua gia hạn, trường `pageExpiresAt` được cập nhật theo công thức:

```
pageExpiresAt = max(now, current_pageExpiresAt) + durationMonths
```

Ý nghĩa: nếu trang **vẫn còn thời hạn**, thời gian mua thêm sẽ cộng dồn từ ngày hết hạn hiện tại. Nếu **đã hết hạn**, thời gian mới tính từ hôm nay. Người dùng không bị "mất" phần thời gian còn lại khi gia hạn sớm.

Chi tiết tại `service/serviceImplement/PaymentServiceImpl.java` (dòng 447-485).

### 6. Ràng buộc "1 tài khoản chỉ sở hữu 1 cafe page"

Một `userId` chỉ được sở hữu **duy nhất một** cafe page ở trạng thái tồn tại. Nếu user đã sở hữu 1 trang và cố tạo trang thứ 2, server trả **HTTP 409 CONFLICT**.

Kiểm tra tại `validation/CafePageValidator.java` (dòng 34-38) qua `validateUserCanCreateCafePage`.

Muốn tạo trang khác, chủ quán phải chuyển quyền sở hữu hoặc xoá trang hiện tại.

### 7. Trạng thái cafe page

Cafe page có 3 trạng thái (`entity/enums/PageStatus.java`):

- `DRAFT`: bản nháp, chỉ chủ thấy.
- `ACTIVE`: đang hoạt động công khai.
- `SUSPENDED`: bị tạm ngưng (do vi phạm hoặc theo yêu cầu quản trị).

Trạng thái `SUSPENDED` không cho phép đăng bài mới; nội dung cũ có thể bị ẩn tuỳ chính sách vận hành.

## Ngoại lệ

- Chủ quán ban đầu (`OWNER`) không đếm vào giới hạn "chỉ 1 CO_OWNER khi maxMembers=2" - ràng buộc 2 là tổng số thành viên bao gồm cả chủ.
- Trang `SUSPENDED` vẫn giữ `pageExpiresAt`; nếu được khôi phục, thời hạn còn lại được tôn trọng.
- Ràng buộc 409 CONFLICT chỉ tính trang do user đang sở hữu; user vẫn có thể được mời làm CO_OWNER/MEMBER ở trang khác.
- Vai trò `STAFF` không tồn tại trong code hiện tại; nếu tài liệu nghiệp vụ nhắc "staff", ánh xạ về `CO_OWNER` khi cần quyền quản lý.

## Câu hỏi thường gặp

**1. Tôi muốn thêm nhân viên vào cafe page thì gán vai trò nào?**
Nếu người đó cần quyền đăng bài và quản lý, gán `CO_OWNER`. Còn lại dùng `MEMBER`.

**2. Vì sao tôi bị 409 khi tạo cafe page thứ hai?**
Ràng buộc "1 tài khoản chỉ sở hữu 1 cafe page". Bạn cần chuyển quyền hoặc xoá trang hiện tại.

**3. Tôi gia hạn sớm 2 tháng trước khi hết hạn có bị "mất" 2 tháng còn lại không?**
Không. Công thức `max(now, current) + durationMonths` cộng dồn từ ngày hết hạn hiện tại.

**4. Cafe page của tôi mặc định cho phép bao nhiêu thành viên?**
Mặc định `maxMembers = 2`. Muốn nhiều hơn, cần mua phụ phí mở rộng.

**5. Trang bị SUSPENDED có mất thời hạn còn lại không?**
Không mất. `pageExpiresAt` được giữ nguyên; khi khôi phục, phần thời hạn còn lại được sử dụng tiếp.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/PageMember.java` (hằng `ROLE_OWNER`, `ROLE_CO_OWNER`, `ROLE_MEMBER`)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/validation/CafePageValidator.java` (dòng 34-38, 40-68)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/CafePage.java` (dòng 74-76)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ExtraFeeServiceImpl.java` (dòng 20, 85-88)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/PaymentServiceImpl.java` (dòng 447-485)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/enums/PageStatus.java`
