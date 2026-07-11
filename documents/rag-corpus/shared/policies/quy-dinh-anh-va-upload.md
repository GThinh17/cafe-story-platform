---
title: Quy định ảnh và upload
slug: quy-dinh-anh-va-upload
platform: both
category: upload
tags: [upload, image, cloudinary, moderation, blog, avatar]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài liệu này mô tả quy định về upload ảnh trên CafeStory: giới hạn dung lượng, số lượng ảnh cho từng loại đối tượng (blog, avatar, cover), quy trình AI moderation, và khác biệt giữa web và mobile trong việc chọn ảnh. Toàn bộ ảnh lưu trữ trên Cloudinary; backend chỉ chuyển tiếp và ghi nhận URL.

## Quy định

### 1. Giới hạn dung lượng multipart

Cấu hình Spring:

- `spring.servlet.multipart.max-file-size = 5MB`
- `spring.servlet.multipart.max-request-size = 5MB`

Chi tiết tại `resources/application.properties` (dòng 30-31).

Điều này có nghĩa: mỗi file upload tối đa **5MB** và tổng dung lượng của cả request cũng tối đa **5MB**. Client nên nén hoặc resize ảnh trước khi upload, đặc biệt trên mobile.

### 2. Giới hạn số ảnh trên blog

Mỗi bài blog cho phép tối đa **10 ảnh**. Kiểm tra ở DTO tạo blog: `dto/requestDTO/BlogCreateDTO.java` (dòng 22).

Vượt quá 10 ảnh, request bị từ chối ở tầng validation. Ảnh thứ 11 trở đi cần được xoá bớt trước khi submit.

### 3. AI moderation cho ảnh blog

Khi blog được gửi kiểm duyệt AI:

- Hệ thống chỉ **xét 10 ảnh đầu tiên** trong bài để moderation.
- Việc **gắn tag tự động** dùng dữ liệu từ **3 ảnh đầu tiên**.

Chi tiết tại `AiBlogModerationServiceImpl.java` (dòng 119-120) và `moderation_rules.json` (dòng 20-21).

Do đó, nếu ảnh nhạy cảm nằm ở vị trí thứ 11 trở đi, AI moderation ban đầu sẽ không phát hiện; các báo cáo từ người dùng vẫn có thể kích hoạt kiểm duyệt lại toàn bộ nội dung.

### 4. Upload trực tiếp lên Cloudinary

Các loại ảnh sau được upload thẳng lên Cloudinary (backend chỉ nhận URL kết quả và lưu vào database):

- Avatar người dùng
- Ảnh cover (blog, profile, cafe page)
- Ảnh nội dung blog

Việc này giảm tải cho backend và tận dụng CDN của Cloudinary cho tốc độ load ảnh.

### 5. Nguồn ảnh trên mobile

App mobile (React Native + Expo) chỉ sử dụng **thư viện ảnh (gallery)** thông qua `expo-image-picker`. **Không có code truy cập camera trực tiếp** trong dự án hiện tại.

App xin permission runtime khi user lần đầu chọn ảnh; nếu user từ chối, tính năng upload không khả dụng cho tới khi quyền được cấp qua Cài đặt.

### 6. Định dạng ảnh chấp nhận

Định dạng được xử lý theo hỗ trợ mặc định của Cloudinary. Phổ biến và khuyến nghị:

- JPG / JPEG
- PNG

Các định dạng khác Cloudinary hỗ trợ (WebP, GIF, HEIC...) về mặt kỹ thuật có thể upload, nhưng khả năng hiển thị nhất quán trên toàn bộ client chưa được test đầy đủ - khuyến nghị dùng JPG/PNG cho ổn định.

## Ngoại lệ

- Ảnh vượt 5MB bị Spring reject trước cả tầng service - client tự chịu trách nhiệm nén trước.
- AI moderation chỉ xử lý 10 ảnh đầu; ảnh vi phạm ở vị trí sau vẫn có thể được người dùng report và admin xử lý thủ công.
- Nếu Cloudinary trả lỗi (network, credential), upload thất bại và backend không tạo record ảnh; client cần retry.
- Mobile không có luồng chụp ảnh trực tiếp; user phải chụp ngoài app rồi chọn từ gallery.

## Câu hỏi thường gặp

**1. Tôi upload ảnh 6MB bị lỗi là sao?**
Giới hạn cứng 5MB/file. Vui lòng nén hoặc resize xuống dưới 5MB.

**2. Tôi cần đăng 15 ảnh cho một bài blog, phải làm sao?**
Giới hạn 10 ảnh/bài. Bạn có thể chia thành nhiều bài hoặc chọn 10 ảnh tiêu biểu nhất.

**3. Tôi thấy AI chỉ tag đúng vài chủ đề đầu, ảnh sau bị bỏ qua?**
Đúng vậy. AI tagging chỉ dùng 3 ảnh đầu để suy luận tag chủ đề bài viết.

**4. Vì sao app mobile không có nút chụp ảnh?**
Phiên bản hiện tại chỉ hỗ trợ chọn từ thư viện. User có thể chụp ngoài app rồi mở lại để chọn.

**5. Ảnh HEIC từ iPhone có upload được không?**
Về mặt kỹ thuật Cloudinary hỗ trợ HEIC, nhưng khuyến nghị chuyển sang JPG để đảm bảo hiển thị nhất quán.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/resources/application.properties` (dòng 30-31)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/requestDTO/BlogCreateDTO.java` (dòng 22)
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AiBlogModerationServiceImpl.java` (dòng 119-120)
- `1-cafe-story-backend-javaspring/src/main/resources/moderation_rules.json` (dòng 20-21)
