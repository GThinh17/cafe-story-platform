---
title: Chỉnh sửa bio trên ứng dụng di động
slug: chinh-sua-bio-mobile
platform: mobile
category: profile
tags: [bio, profile, mobile, mo-ta]
version: 1
updated_at: 2026-07-10
owner: team-frontend
related_ui:
  - Profile
---

# Chỉnh sửa bio trên ứng dụng di động CafeStory

## Giới thiệu

Bio (mô tả ngắn) là dòng giới thiệu bản thân hiển thị trên hồ sơ CafeStory. Trên ứng dụng di động, bio được sửa qua BioEditorModal - một modal toàn màn hình có bàn phím tự né (KeyboardAvoidingView), tự động focus vào ô nhập và giới hạn 150 ký tự. Không cần rời màn hình Profile; toàn bộ thao tác nằm gọn trong một modal.

## Điều kiện tiên quyết

Bạn cần đăng nhập tài khoản CafeStory và mở tab Profile. Ứng dụng cần kết nối mạng để lưu bio lên máy chủ qua updateMyProfile. Bio hiển thị công khai trên hồ sơ nên tránh viết thông tin nhạy cảm.

## Các bước thực hiện

### Bước 1: Mở BioEditorModal từ Profile

Vào tab Profile. Nếu bạn đã có bio, dòng bio hiển thị inline dưới phần tên và stats, kế bên biểu tượng bút chì nhỏ. Chạm vào dòng này để mở modal chỉnh sửa. Nếu bạn chưa có bio, khối gợi ý "Add a short description about you here" hiện ở cùng vị trí với biểu tượng bút chì; chạm vào đó cũng mở cùng modal.

Modal trượt lên toàn màn hình. Thanh top bar có nút "X" ở góc trái, tiêu đề "Bio" ở giữa và nút dấu tick màu xanh ở góc phải. Bên dưới là khối input được viền dày với nhãn "Bio" và bộ đếm ký tự dạng "N/150".

### Bước 2: Nhập nội dung bio

Ô input tự động focus khi modal mở, bàn phím thiết bị hiện lên và KeyboardAvoidingView đẩy nội dung modal lên để ô nhập không bị che. Ô có chế độ multiline, tự viết hoa đầu câu (autoCapitalize="sentences") và giới hạn tối đa 150 ký tự. Con trỏ nhấp nháy với màu tertiary của CafeStory.

Bộ đếm ở góc phải trên ô input cập nhật theo từng ký tự để bạn biết còn bao nhiêu ký tự trước khi chạm giới hạn. Khi đạt 150, hệ điều hành sẽ chặn nhập thêm.

Nếu bạn từng có bio cũ, nội dung cũ được đổ sẵn khi modal mở (đã trim khoảng trắng đầu và cuối). Bạn có thể sửa lại hoặc xoá sạch rồi viết mới.

### Bước 3: Lưu bio

Chạm dấu tick màu xanh ở góc phải header. Ứng dụng gọi updateMyProfile với trường userDescription là nội dung mới đã được trim. Trong lúc lưu, các nút "X" và tick bị vô hiệu (mờ đi) để tránh thao tác kép.

Khi thành công, modal tự đóng và ProfileScreen cập nhật lại dòng bio inline ngay lập tức. Nếu bạn xoá bio về rỗng và lưu, khối gợi ý "Add a short description about you here" sẽ quay lại.

### Bước 4: Đóng modal mà không lưu

Chạm "X" ở góc trái header để đóng modal mà không lưu. Nếu đang trong quá trình lưu, nút "X" bị vô hiệu; chờ tác vụ hoàn tất rồi mới đóng được.

Bạn cũng có thể vuốt xuống hoặc bấm nút back của Android; hệ thống gọi onRequestClose và modal đóng nếu không đang lưu.

## Xử lý lỗi thường gặp

### Thông báo "Unable to update your bio."

Đây là lỗi máy chủ khi lưu bio. Kiểm tra kết nối mạng, chạm dấu tick lần nữa. Nếu vẫn báo lỗi, đóng modal, mở lại và thử lần nữa. Trường hợp hiếm, đăng xuất và đăng nhập lại.

### Bàn phím che ô nhập

Modal đã dùng KeyboardAvoidingView với behavior "padding" trên iOS. Nếu trên Android bàn phím vẫn che, cuộn nhẹ hoặc gõ Enter để đưa dòng đang gõ lên phía trên. Kiểm tra bạn có bật chế độ chia màn hình - trường hợp này có thể làm bàn phím chiếm chỗ khác thường.

### Không nhập thêm được ký tự

Bio giới hạn 150 ký tự. Kiểm tra bộ đếm ở góc phải trên ô input; nếu đã đạt 150, xoá bớt để nhập thêm.

### Bio hiển thị bị xuống dòng lạ

Ô nhập là multiline nên các dấu xuống dòng bạn thêm sẽ được lưu. Nếu không muốn xuống dòng, gõ liền hoặc dùng dấu chấm để phân tách.

## Câu hỏi thường gặp

### Bio dài tối đa bao nhiêu ký tự?

150 ký tự. Bộ đếm hiện đầy đủ ở góc phải trên ô nhập.

### Tôi có thể dùng emoji trong bio không?

Có. Ô nhập chấp nhận mọi ký tự Unicode. Lưu ý mỗi emoji có thể chiếm nhiều đơn vị ký tự trong đếm.

### Bio có được hiển thị công khai không?

Có. Bio nằm trên hồ sơ và người khác thấy khi mở hồ sơ của bạn (OtherUserProfile). Đừng viết thông tin nhạy cảm.

### Sửa bio có ảnh hưởng đến username hay tên hiển thị không?

Không. Bio chỉ cập nhật trường userDescription. Username và tên hiển thị được sửa trong EditProfileModal (nút "Edit Profile").

### Có lịch sử bio để khôi phục không?

Ứng dụng không lưu lịch sử bio. Trước khi lưu, nếu cần giữ bio cũ, hãy copy nội dung ra ghi chú riêng.
