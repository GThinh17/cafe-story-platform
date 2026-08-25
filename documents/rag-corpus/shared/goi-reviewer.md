---
title: Gói Reviewer là gì
slug: goi-reviewer
platform: both
category: reviewer
tags: [reviewer, goi-dich-vu, payout, huy-hieu]
version: 2
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Gói Reviewer cho phép người dùng thường trở thành reviewer — nhóm cộng tác viên chuyên viết bài đánh giá quán cafe và nhận thu nhập theo tương tác thực tế từ cộng đồng. Reviewer là một vai trò có gói dịch vụ, có công thức tính điểm, có 5 mức huy hiệu và có quy trình payout độc lập với các hình thức thanh toán khác.

Người dùng đăng ký gói qua bảng ExtraFee với `feeType = REVIEWER_REGISTRATION`. Mỗi gói có `durationMonths` xác định thời hạn hoạt động. Sau khi thanh toán thành công, tài khoản được gán vai trò REVIEWER, cờ `reviewer_active` được đặt thành `true`, và thời hạn được cộng dồn với thời hạn còn lại (nếu có).

Payout được thực hiện bằng đồng Việt Nam (VND) thông qua Stripe Connect. Reviewer phải hoàn tất onboarding Stripe Connect và đạt trạng thái `payoutsEnabled = true` thì mới có thể nhận tiền. Toàn bộ điểm và số tiền cụ thể được tính dựa trên công thức reviewer (`ReviewerFormula`) và cấu hình huy hiệu, cả hai đều do admin cấu hình.

## Quy định

**1. Đăng ký và gia hạn gói Reviewer.**

Gói Reviewer bán qua bảng ExtraFee với `feeType = REVIEWER_REGISTRATION`. Khi giao dịch chuyển sang PAID, hệ thống:

- Gán role REVIEWER cho tài khoản.
- Đặt `reviewer_active = true`.
- Tính thời hạn theo công thức cộng dồn: `expiresAt = max(now, current_expiresAt) + durationMonths`. Thời hạn còn lại không bị reset về 0 khi gia hạn.

**2. Công thức tính điểm reviewer.**

Điểm reviewer tính dựa trên tương tác của cộng đồng với các bài viết của reviewer đó. Điểm mặc định cho mỗi loại tương tác được lưu trong `ReviewerFormula`:

- Like: 1 điểm.
- Comment: 5 điểm.
- Share: 3 điểm.

Các giá trị này là mặc định trong entity `ReviewerFormula`, admin có thể cấu hình lại thông qua trang quản trị.

**3. Payout mặc định theo loại tương tác (VND).**

Cùng với điểm, hệ thống tính số tiền payout cơ bản theo tương tác:

- Like: 100 đồng.
- Comment: 500 đồng.
- Share: 300 đồng.

Số tiền cuối cùng bằng payout cơ bản nhân với multiplier theo huy hiệu hiện tại của reviewer.

**4. Năm mức huy hiệu bắt buộc đủ.**

Hệ thống bắt buộc phải có đúng 5 mức huy hiệu: IRON, BRONZE, SILVER, GOLD, DIAMOND. Khi admin cập nhật cấu hình ngưỡng huy hiệu, nếu thiếu bất kỳ mức nào trong 5 mức này, hệ thống sẽ từ chối lưu.

Multiplier áp vào payout theo mức huy hiệu:

- IRON: ×1.00
- BRONZE: ×1.20
- SILVER: ×1.50
- GOLD: ×2.00
- DIAMOND: ×3.00

**5. Ngưỡng huy hiệu fallback (khi admin chưa cấu hình).**

Nếu admin chưa cấu hình ngưỡng riêng, hệ thống dùng ngưỡng fallback dựa trên tổng điểm reviewer:

- IRON: điểm < 100
- BRONZE: 100 ≤ điểm < 300
- SILVER: 300 ≤ điểm < 700
- GOLD: 700 ≤ điểm < 1500
- DIAMOND: điểm ≥ 1500

Admin có toàn quyền cấu hình lại các ngưỡng này để phù hợp chiến lược từng thời điểm.

**6. Điều kiện payout: Stripe Connect.**

Reviewer chỉ có thể nhận payout sau khi đã liên kết tài khoản Stripe Connect và trạng thái tài khoản Stripe của reviewer có `payoutsEnabled = true`. Nếu điều kiện này chưa đạt, yêu cầu payout sẽ bị từ chối ở tầng dịch vụ admin.

Toàn bộ payout sử dụng đơn vị tiền tệ VND, được xác định cứng trong luồng `AdminPayoutServiceImpl`.

**7. Quy trình payout không đảo chiều.**

Trạng thái payout đi theo chuỗi tuyến tính:

`PENDING → APPROVED → PAID`

Ngoài ra có nhánh `CANCELLED` để hủy yêu cầu. Không có thao tác nào cho phép chuyển ngược lại (ví dụ từ APPROVED về PENDING). Điều này đảm bảo lịch sử payout có tính bất biến để phục vụ kế toán và đối soát.

## Ngoại lệ

- Reviewer đã hết hạn (`reviewer_active = false` hoặc quá `expiresAt`) không được tính điểm tương tác mới, không đủ điều kiện tạo yêu cầu payout mới cho tới khi gia hạn.
- Trong trường hợp admin cấu hình lại multiplier hoặc ngưỡng huy hiệu, thay đổi chỉ áp dụng cho các đợt tính điểm và payout sau thời điểm cập nhật; các payout đã ở trạng thái APPROVED hoặc PAID không bị tính lại.
- Nếu reviewer chưa hoàn tất Stripe Connect, họ vẫn tích điểm và lên huy hiệu bình thường, chỉ bị chặn ở bước phát hành tiền.

## Câu hỏi thường gặp

**Gia hạn gói khi chưa hết hạn có bị mất thời gian còn lại không?**
Không. Công thức là `expiresAt = max(now, current) + durationMonths`, phần thời gian còn lại được cộng dồn.

**Điểm reviewer được tính từ đâu?**
Từ tương tác với bài viết của reviewer: like = 1, comment = 5, share = 3 điểm (mặc định).

**Vì sao tôi chưa nhận được tiền dù đã đủ điểm?**
Bạn cần hoàn tất onboarding Stripe Connect và trạng thái Stripe phải là `payoutsEnabled = true`. Nếu chưa đạt, yêu cầu payout sẽ bị từ chối.

**Ngưỡng huy hiệu có cố định không?**
Không. Có bộ ngưỡng fallback mặc định (IRON <100, BRONZE <300, SILVER <700, GOLD <1500, DIAMOND ≥1500), nhưng admin có thể cấu hình lại.

**Tôi có thể nhận payout bằng USD không?**
Không. Toàn bộ payout thực hiện bằng VND theo cấu hình trong `AdminPayoutServiceImpl`.

**Multiplier DIAMOND thực sự là ×3 sao?**
Đúng. Giá trị mặc định là ×3.00, mức cao nhất trong 5 mức huy hiệu.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ExtraFee.java` — cấu trúc gói `feeType`, `durationMonths`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/PaymentServiceImpl.java:430-445` — thanh toán gói REVIEWER_REGISTRATION: gán role, đặt `reviewer_active = true`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/PaymentServiceImpl.java:437-442` — cộng dồn `expiresAt = max(now, current) + durationMonths`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ReviewerFormula.java:33-49` — điểm mặc định (like=1, comment=5, share=3) và payout mặc định (100đ, 500đ, 300đ).
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ReviewerFormula.java:52-65` — multiplier: IRON ×1.00, BRONZE ×1.20, SILVER ×1.50, GOLD ×2.00, DIAMOND ×3.00.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReviewerBadgeThresholdServiceImpl.java:79-82` — bắt buộc đủ 5 huy hiệu IRON/BRONZE/SILVER/GOLD/DIAMOND.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReviewerBadgeThresholdServiceImpl.java:98-103` — ngưỡng fallback: IRON <100, BRONZE <300, SILVER <700, GOLD <1500, DIAMOND ≥1500.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminPayoutServiceImpl.java:215-224` — yêu cầu Stripe Connect với `payoutsEnabled = true`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminPayoutServiceImpl.java:229-238` — currency VND.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminPayoutServiceImpl.java:248-257` — payout flow `PENDING → APPROVED → PAID` (hoặc `CANCELLED`), không đảo chiều.
