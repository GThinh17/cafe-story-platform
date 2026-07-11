---
title: Quy định huy hiệu và công thức reviewer
slug: quy-dinh-huy-hieu-reviewer
platform: both
category: reviewer
tags: [reviewer, badge, multiplier, formula, tier]
version: 1
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Chương trình reviewer của Cafe Story xây trên hai trục: **điểm hoạt động** tính từ tương tác cộng đồng lên bài viết của reviewer, và **huy hiệu** dùng làm mức đánh dấu uy tín. Huy hiệu vừa hiển thị bên cạnh tên reviewer để cộng đồng dễ nhận biết, vừa quyết định hệ số nhân (multiplier) áp lên payout mà reviewer nhận được. Tài liệu này chốt các con số nền tảng: 5 mức huy hiệu bắt buộc, ngưỡng điểm mặc định, hệ số nhân theo mức và điểm hoạt động mặc định cho từng loại tương tác.

Toàn bộ số ở đây có thể được admin cấu hình lại thông qua bảng quản trị (ngưỡng, công thức). Khi chưa có cấu hình riêng, hệ thống dùng giá trị fallback dưới đây để tránh trạng thái không xác định.

## Quy định

**1. Năm mức huy hiệu bắt buộc đủ.**

Hệ thống bắt buộc phải tồn tại đúng 5 mức huy hiệu: **IRON**, **BRONZE**, **SILVER**, **GOLD**, **DIAMOND**. Khi admin cập nhật cấu hình ngưỡng, nếu thiếu bất kỳ mức nào trong danh sách này, thao tác lưu sẽ bị từ chối. Điều này bảo đảm mọi reviewer luôn có thể được phân vào đúng một mức huy hiệu ở bất kỳ tổng điểm nào.

**2. Ngưỡng điểm mặc định (fallback) theo mức huy hiệu.**

Khi admin chưa cấu hình ngưỡng riêng, hệ thống áp dụng dải điểm sau dựa trên tổng điểm reviewer đã tích lũy:

- **IRON**: điểm `< 100`
- **BRONZE**: `100 ≤` điểm `< 300`
- **SILVER**: `300 ≤` điểm `< 700`
- **GOLD**: `700 ≤` điểm `< 1500`
- **DIAMOND**: điểm `≥ 1500`

Reviewer mới đăng ký thường bắt đầu ở IRON và thăng hạng khi tổng điểm vượt ngưỡng. Việc rớt hạng không xảy ra tự động ở mức mã nguồn; điểm chỉ cộng dồn.

**3. Hệ số nhân (multiplier) áp lên payout.**

Số tiền payout cuối cùng bằng payout cơ bản (theo loại tương tác) nhân với hệ số của huy hiệu hiện tại:

- IRON: `×1.00`
- BRONZE: `×1.20`
- SILVER: `×1.50`
- GOLD: `×2.00`
- DIAMOND: `×3.00`

Reviewer DIAMOND nhận gấp 3 lần reviewer IRON cho cùng một tương tác — đây là ưu đãi thiết kế để khuyến khích duy trì chất lượng dài hạn.

**4. Điểm hoạt động mặc định theo loại tương tác.**

Điểm reviewer tích lũy khi bài viết nhận tương tác từ cộng đồng. Giá trị mặc định trong `ReviewerFormula`:

- **Like**: 1 điểm
- **Comment**: 5 điểm
- **Share**: 3 điểm

Comment được đánh giá cao hơn like vì thể hiện mức độ đầu tư nội dung cao hơn. Share cao hơn like nhưng thấp hơn comment vì có thể phản ánh cả tương tác thụ động (chia sẻ nhanh) lẫn chủ động.

**5. Admin có thể chỉnh ngưỡng và công thức bất kỳ lúc nào.**

Admin có quyền thay đổi ngưỡng điểm cho từng mức huy hiệu và điểm hoạt động cho từng loại tương tác thông qua giao diện quản trị. Sau khi lưu, giá trị mới áp dụng cho các đợt tính điểm tiếp theo, không tính lại lịch sử payout đã hoàn tất.

## Ngoại lệ

- **Nếu admin chỉ cấu hình một phần** (ví dụ đặt lại ngưỡng SILVER nhưng để trống các mức khác), hệ thống vẫn từ chối lưu vì yêu cầu đủ 5 mức. Không có chế độ hybrid giữa cấu hình mới và fallback.
- **Reviewer đã hết hạn gói** không tiếp tục tích điểm mới cho tới khi gia hạn, bất kể huy hiệu hiện tại. Multiplier vẫn giữ nguyên khi tính payout cho phần điểm cũ chưa payout xong.
- **Đổi ngưỡng không dịch chuyển huy hiệu ngược.** Nếu admin nâng ngưỡng SILVER lên `500`, những reviewer đã đạt SILVER với 400 điểm không tự động bị hạ xuống BRONZE ngay; hệ thống áp dụng ngưỡng mới cho lần tính lại tiếp theo.
- **Multiplier thay đổi không tính lại payout đã APPROVED hoặc PAID.** Các payout đang PENDING sẽ dùng multiplier mới khi được duyệt.

## Câu hỏi thường gặp

**Tôi có thể tự chọn mức huy hiệu không?**
Không. Huy hiệu được xác định tự động theo tổng điểm và ngưỡng hiện hành. Bạn chỉ có thể tác động gián tiếp bằng cách tạo bài viết chất lượng để thu hút tương tác.

**Ngưỡng thăng hạng có cố định mãi mãi không?**
Không. Đây chỉ là ngưỡng fallback khi admin chưa cấu hình. Admin có thể chỉnh lại bất kỳ lúc nào để phù hợp chiến lược sản phẩm.

**Vì sao comment quan trọng hơn like?**
Vì comment cần công sức viết nội dung, phản ánh mức độ quan tâm sâu hơn và tạo giá trị thảo luận. Do đó `comment = 5` so với `like = 1`.

**Nếu tôi mất huy hiệu DIAMOND, thu nhập cũ có bị tính lại không?**
Không. Payout đã APPROVED hoặc PAID không bị tính lại. Chỉ những đợt tương tác chưa được payout sẽ dùng hệ số huy hiệu tại thời điểm chốt.

**Ngưỡng DIAMOND 1500 điểm có khó không?**
Tuỳ mức độ hoạt động. Ví dụ 300 comment tương ứng 1500 điểm, hoặc 1500 like, hoặc kết hợp. Reviewer chăm sóc bài viết đều đặn thường đạt DIAMOND sau vài tháng.

**Reviewer IRON có được payout không?**
Có, với `×1.00`. IRON không cấm payout, chỉ không có hệ số nhân thưởng.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReviewerBadgeThresholdServiceImpl.java:79-82` — bắt buộc đủ 5 mức IRON/BRONZE/SILVER/GOLD/DIAMOND khi lưu cấu hình.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReviewerBadgeThresholdServiceImpl.java:98-103` — ngưỡng fallback IRON <100, BRONZE <300, SILVER <700, GOLD <1500, DIAMOND ≥1500.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ReviewerFormula.java:33-49` — điểm hoạt động mặc định like=1, comment=5, share=3.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/ReviewerFormula.java:52-65` — hệ số nhân IRON ×1.00, BRONZE ×1.20, SILVER ×1.50, GOLD ×2.00, DIAMOND ×3.00.
