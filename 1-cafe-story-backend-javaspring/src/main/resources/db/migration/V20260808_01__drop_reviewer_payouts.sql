-- Xoá bảng reviewer_payouts — hệ payout song song đã chết.
--
-- Bối cảnh: dự án tồn tại hai đường payout không nối với nhau.
--   Đường đang chạy: reviewer_income (job 3h hằng ngày) -> admin_payout
--                    (job 4h ngày 1 hằng tháng).
--   Đường chết:      reviewer_payouts, chỉ được ghi bởi
--                    ReviewerServiceImpl.generateMonthlyPayouts, và hàm đó chỉ
--                    gọi được qua POST /api/reviewers/payouts/generate (admin,
--                    bấm tay). Không scheduler nào gọi nó.
--
-- Hệ quả: trang thu nhập của reviewer đọc reviewer_payouts nên luôn rỗng.
-- Nay endpoint đó đọc admin_payout, entity/repository/DTO tương ứng đã bị gỡ
-- khỏi code, nên bảng này không còn ai tham chiếu.
--
-- V20260807_01 vá FK sai của chính bảng này. Giữ nguyên file đó, không sửa:
-- Flyway đã ghi checksum. Migration này chạy sau và dọn hẳn.

DROP TABLE IF EXISTS reviewer_payouts;
