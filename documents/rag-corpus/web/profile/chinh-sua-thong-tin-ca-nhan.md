---
title: Chỉnh sửa thông tin cá nhân
slug: chinh-sua-thong-tin-ca-nhan
platform: web
category: profile
tags: [profile, edit-profile, region, avatar, ho-so]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - /[username]/edit
---

## Giới thiệu

Trang chỉnh sửa thông tin cá nhân cho phép bạn cập nhật những thông tin hiển thị công khai trên hồ sơ Cafe Story: ảnh đại diện, họ và tên đầy đủ, số điện thoại và địa chỉ khu vực (tỉnh/thành, quận/huyện, phường/xã, khu vực và đường phố). Trang này được thiết kế theo mô hình tách nhỏ từng biểu mẫu để bạn có thể lưu độc lập từng phần mà không phải điền lại toàn bộ.

Toàn bộ thông tin đều được đồng bộ với hồ sơ hiển thị của bạn trên Cafe Story và ảnh hưởng đến cách người khác nhìn thấy bạn ở trang cá nhân, phần bình luận, danh sách người theo dõi và trong tính năng gợi ý theo khu vực của bảng tin. Địa chỉ khu vực đặc biệt quan trọng vì hệ thống dùng nó để nâng điểm ưu tiên cho những bài viết liên quan đến cùng tỉnh/thành với bạn khi xếp hạng bảng tin.

## Điều kiện tiên quyết

- Bạn phải đăng nhập bằng tài khoản Cafe Story còn hoạt động.
- Bạn chỉ có thể chỉnh sửa hồ sơ của chính mình. Nếu URL không khớp với `userName` hiện tại thì hệ thống sẽ tự động chuyển hướng bạn về đường dẫn chỉnh sửa đúng.
- Ảnh đại diện phải là tệp hình ảnh (`image/*`) và có dung lượng tối đa 5 MB.
- Trình duyệt cần cho phép Cafe Story truy cập dữ liệu địa giới hành chính Việt Nam (province, city, ward) để danh sách chọn khu vực hiển thị đầy đủ.

## Các bước thực hiện

### Bước 1: Mở trang chỉnh sửa hồ sơ

Bạn có ba cách vào trang chỉnh sửa:

- Vào trang cá nhân của mình rồi bấm nút **Edit Profile** ở phần đầu hồ sơ.
- Mở menu ba chấm (biểu tượng dấu chấm ngang) trên trang cá nhân và chọn **Edit profile**.
- Truy cập trực tiếp đường dẫn `/[username]/edit` với `[username]` là tên người dùng của bạn.

Sau khi trang được nạp, hệ thống sẽ tự động điền các giá trị hiện tại của bạn vào tất cả các trường.

### Bước 2: Cập nhật ảnh đại diện

Bấm vào biểu tượng máy ảnh ở góc dưới bên phải khung ảnh, hoặc bấm nút **Change Avatar**. Chọn một hình ảnh trên thiết bị (dưới 5 MB). Ảnh xem trước sẽ hiển thị ngay lập tức. Bấm **Save avatar** để xác nhận. Hệ thống sẽ tải ảnh lên Cloudinary, cập nhật URL vào hồ sơ và làm mới trang để mọi nơi hiển thị ảnh mới.

### Bước 3: Cập nhật họ tên và số điện thoại

Ở khối **Basic info**, sửa **Full name** thành họ tên đầy đủ mà bạn muốn hiển thị. Trường **Phone** chỉ nhận ký tự số (các ký tự khác sẽ bị bỏ qua khi bạn gõ). Bấm **Save basic info** để lưu. Hệ thống gọi API cập nhật thông tin cá nhân và làm mới dữ liệu hồ sơ.

### Bước 4: Cập nhật khu vực (province / city / ward)

Chọn **Province / city** ở danh sách thả xuống đầu tiên. Sau khi có tỉnh/thành, danh sách **City / district** sẽ được nạp; chọn tiếp quận/huyện. Cuối cùng chọn **Ward** ở danh sách thứ ba. Tỉnh và phường là bắt buộc để lưu địa chỉ.

Nhập thêm **Area** (khu vực nhỏ, khu phố hoặc điểm mốc — không bắt buộc) và **Street** (số nhà, tên đường, địa chỉ chi tiết — không bắt buộc). Bấm **Save address**. Hệ thống lưu địa chỉ mới và hiển thị thông báo thành công.

### Bước 5: Kiểm tra kết quả

Quay lại trang cá nhân `/[username]` để xem ảnh đại diện, họ tên và khu vực đã cập nhật đúng. Nếu bạn có bài viết trong bảng tin, thứ tự hiển thị bài đề xuất có thể thay đổi do khu vực mới ảnh hưởng đến điểm ưu tiên bảng tin.

## Xử lý lỗi thường gặp

**"Please choose an image file."**
Bạn đã chọn một tệp không phải hình ảnh. Hãy chọn tệp có định dạng ảnh (JPG, PNG, WEBP...).

**"Avatar image must be 5MB or smaller."**
Ảnh vượt quá 5 MB. Hãy nén ảnh hoặc chọn ảnh nhỏ hơn trước khi lưu.

**"Phone must be a valid number."**
Số điện thoại không phải một số hợp lệ. Chỉ nhập chữ số, không nhập dấu cách hoặc ký tự đặc biệt.

**"Province, city and ward are required."**
Bạn phải chọn cả ba mức tỉnh, quận/huyện và phường/xã trước khi lưu khối địa chỉ.

**"Unable to load Vietnam address data."**
Không tải được dữ liệu địa giới hành chính. Kiểm tra kết nối mạng và thử lại. Danh sách khu vực dựa vào dịch vụ region của Cafe Story.

**"Redirecting..."**
Bạn đang truy cập URL chỉnh sửa của một người khác. Hệ thống sẽ tự động đưa bạn về trang chỉnh sửa của chính mình.

## Câu hỏi thường gặp

**Tôi có thể đổi tên đăng nhập (`userName`) ở đây không?**
Không. Trang này chỉ cho phép sửa họ tên hiển thị (`userFullName`). Tên đăng nhập (`userName`) là định danh duy nhất và không được đổi qua trang chỉnh sửa hồ sơ.

**Số điện thoại có bắt buộc không?**
Không bắt buộc. Bạn có thể để trống. Nếu đã có số cũ, xóa trắng sẽ giữ lại giá trị hiện tại trên máy chủ; muốn xóa số điện thoại vui lòng liên hệ hỗ trợ.

**Khu vực có công khai không?**
Có. Khu vực ở dạng "Tỉnh / Quận / Phường" hiển thị công khai trên hồ sơ. Trường **Area** và **Street** hiện tại không hiển thị chi tiết ra ngoài, chỉ dùng cho các gợi ý theo khu vực.

**Tại sao khi đổi khu vực, bảng tin của tôi lại khác đi?**
Vì công thức xếp hạng bảng tin cộng thêm điểm ưu tiên khi bài viết cùng tỉnh/thành với người dùng. Đổi khu vực có thể làm thay đổi thứ tự hiển thị bài đề xuất.

**Ảnh cũ có bị xóa khỏi máy chủ không?**
Ảnh cũ vẫn tồn tại trên Cloudinary vì có thể được tham chiếu ở các bài viết hoặc bình luận trước đó. Hồ sơ chỉ trỏ đến URL ảnh mới nhất.
