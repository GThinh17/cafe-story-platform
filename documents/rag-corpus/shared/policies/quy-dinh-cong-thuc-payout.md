---
title: Quy định công thức payout cho reviewer
slug: quy-dinh-cong-thuc-payout
platform: both
category: reviewer
tags: [payout, reviewer, formula, stripe-connect, badge, multiplier]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Tài liệu này mô tả công thức tính payout (thu nhập) mà nền tảng CafeStory chi trả cho reviewer dựa trên các tương tác (like, comment, share) mà bài đánh giá của họ nhận được. Payout được điều chỉnh theo cấp bậc huy hiệu (badge) của reviewer và được xử lý qua Stripe Connect.

Chính sách áp dụng cho toàn bộ reviewer trên cả web và mobile. Mục tiêu của công thức là (1) minh bạch trong cách tính tiền, (2) khuyến khích reviewer nâng cấp badge bằng chất lượng nội dung, và (3) đảm bảo dòng tiền chảy qua kênh thanh toán hợp lệ.

## Quy định

### 1. Đơn giá và hệ số hiện hành

Các con số cụ thể — đơn giá mỗi like/comment/share, hệ số nhân theo từng badge (IRON, BRONZE, SILVER, GOLD, DIAMOND), và ngưỡng điểm tối thiểu để đạt mỗi huy hiệu — được **sinh động từ cấu hình admin** và tra cứu ở tài liệu "Công thức payout hiện hành" trong hệ thống RAG. Mỗi khi admin cập nhật formula qua trang quản trị (`PUT /api/admin/formulas/{id}/activate` hoặc `PUT /api/admin/formulas/{id}/thresholds`), bản snapshot mới sẽ được tự động embed lại nên chatbot luôn trả về số đang áp dụng, không phụ thuộc vào các số ví dụ ở đây.

Cấu trúc entity: `entity/ReviewerFormula.java` (đơn giá + hệ số nhân) và `entity/ReviewerBadgeThreshold.java` (ngưỡng điểm mỗi badge). Endpoint snapshot đọc cho RAG: `GET /api/internal/rag/snapshots/formula`.

### 2. Công thức payout tổng quát

```
payout = ( sum(likes) × đơn_giá_like
         + sum(comments) × đơn_giá_comment
         + sum(shares) × đơn_giá_share ) × hệ_số_badge_hiện_tại
```

Trong đó `sum(...)` là tổng số tương tác trên toàn bộ review của reviewer trong kỳ tính payout; `đơn_giá_*` và `hệ_số_badge_hiện_tại` lấy từ active formula tại thời điểm chốt payout (xem điểm 6). Số dư cuối cùng được làm tròn xuống theo đơn vị VND.

### 3. Vòng đời payout (state machine)

Mỗi khoản payout đi qua các trạng thái sau, không được đảo chiều:

```
PENDING → APPROVED → PAID
     └────────────→ CANCELLED
```

- `PENDING`: yêu cầu vừa được tạo, chờ admin duyệt.
- `APPROVED`: admin đã duyệt, chờ hệ thống gọi Stripe.
- `PAID`: đã chuyển tiền thành công qua Stripe Connect.
- `CANCELLED`: bị huỷ (do reviewer, admin, hoặc lỗi không khắc phục được).

Logic chuyển trạng thái nằm ở `service/serviceImplement/AdminPayoutServiceImpl.java` (dòng 248-257). Trạng thái đã ở `PAID` hoặc `CANCELLED` không thể quay lại các trạng thái trước.

### 4. Yêu cầu Stripe Connect

Reviewer chỉ nhận được payout khi tài khoản Stripe Connect của họ có `payoutsEnabled = true`. Nếu chưa bật, khoản payout sẽ bị chặn ở bước gọi Stripe và reviewer nhận thông báo yêu cầu hoàn tất onboarding Stripe. Kiểm tra được thực hiện ở `AdminPayoutServiceImpl.java` (dòng 215-224).

### 5. Đơn vị tiền tệ và idempotency

- Currency: VND cho mọi payout.
- Idempotency key gửi lên Stripe có định dạng `payout-<id>`, trong đó `<id>` là mã payout nội bộ. Nhờ đó, nếu request bị retry, Stripe sẽ không tạo giao dịch trùng.

Chi tiết ở `AdminPayoutServiceImpl.java` (dòng 229-238).

### 6. Quyền chỉnh sửa công thức

Admin có quyền cập nhật đơn giá cơ bản, hệ số nhân theo badge, và ngưỡng điểm mỗi badge. Mọi thay đổi được ghi log và chỉ áp dụng cho kỳ tính payout tiếp theo (payout đã tạo giữ nguyên formula cũ — xem điểm 6 phần "chốt tại thời điểm tính payout").

## Ngoại lệ

- Payout ở trạng thái `CANCELLED` không tạo giao dịch Stripe, không tính vào doanh thu reviewer.
- Nếu reviewer chưa hoàn tất Stripe Connect, hệ thống giữ payout ở `APPROVED` cho tới khi `payoutsEnabled = true`.
- Trường hợp lỗi mạng lúc gọi Stripe, hệ thống retry với cùng idempotency key `payout-<id>`; sẽ không phát sinh giao dịch trùng lặp.
- Multiplier badge được chốt tại thời điểm tính payout; badge thay đổi sau đó không tính hồi tố.

## Câu hỏi thường gặp

**1. Vì sao payout của tôi vẫn là PENDING?**
Payout mới tạo cần admin duyệt trước khi chuyển sang `APPROVED`. Vui lòng chờ hoặc liên hệ bộ phận vận hành.

**2. Tôi đã lên badge GOLD nhưng payout kỳ trước vẫn dùng hệ số của badge cũ?**
Multiplier được chốt tại thời điểm tính payout. Kỳ trước bạn đang là SILVER nên hệ thống dùng hệ số của SILVER. Kỳ sau đủ điều kiện GOLD → hệ số GOLD mới được áp dụng. Con số cụ thể của từng badge xem trong "Công thức payout hiện hành".

**3. Có thể rút VND về tài khoản ngân hàng Việt Nam không?**
Payout đi qua Stripe Connect. Reviewer cần cấu hình tài khoản nhận theo hướng dẫn Stripe; nền tảng chỉ chịu trách nhiệm gửi lệnh chi trả.

**4. Nếu admin đổi công thức, payout của tôi có bị tính lại không?**
Không. Payout đã tạo giữ nguyên công thức tại thời điểm tính; thay đổi chỉ áp dụng cho kỳ sau.

**5. Idempotency key `payout-<id>` để làm gì?**
Ngăn Stripe tạo giao dịch trùng khi request bị retry (do lỗi mạng, timeout...). Cùng một payout luôn có cùng key.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ReviewerFormula.java` — đơn giá + hệ số nhân
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ReviewerBadgeThreshold.java` — ngưỡng điểm mỗi badge
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/controller/RagSnapshotController.java` — endpoint `/api/internal/rag/snapshots/formula` phục vụ RAG
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AdminPayoutServiceImpl.java` — logic Stripe, state machine, idempotency
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/RagReindexClient.java` — trigger reindex khi admin đổi formula
