# Danh sách lỗi — Admin Resolve Report with AI

## Phạm vi

- Nhánh: `n8n/Ai-agent/fix-bug-report-admin`.
- Phạm vi sửa: Backend Admin Resolve AI, n8n tương ứng, Admin UI và E2E.
- Ngoài phạm vi: mobile, web người dùng và luồng moderation lúc tạo report.

## Lỗi đã phát hiện và kết quả xử lý

| ID | Mức độ | Phân loại | Lỗi | Kết quả |
|---|---|---|---|---|
| RAI-FIX-001 | P0 | External service | Production webhook n8n trả 404. | Đã backup workflow cũ, publish V2 và kiểm chứng production webhook HTTP 200. |
| RAI-FIX-002 | P1 | Code bug | BE cho Ask AI report terminal và chấp nhận decision/action mâu thuẫn. | Đã thêm status guard, score/audit validation và semantic matrix. |
| RAI-FIX-003 | P1 | Code bug | Auto-apply có thể dùng recommendation cũ khi target đã đổi. | Đã revalidate target ID, status và `updatedAt`; stale job chuyển `SKIPPED`. |
| RAI-FIX-004 | P1 | Config bug | Timeout BE 15 giây ngắn hơn budget OpenAI. | Đã đổi thành 40 giây và dùng production webhook mặc định. |
| RAI-FIX-005 | P2 | UX bug | Lỗi Ask AI nằm sau dialog. | Đã hiển thị alert trong dialog, giữ dialog để retry. |
| RAI-FIX-006 | P2 | UX/functional bug | Tên bulk gây hiểu nhầm; không có lỗi theo item. | Đã đổi thành “Generate AI recommendations”, giữ danh sách failure theo report. |
| RAI-FIX-007 | P1 | Test bug | E2E từng tính pass cho 502 hoặc không có job. | Đã hard-fail preflight, validate result thật và đánh dấu thiếu job là `BLOCKED`. |
| RAI-FIX-008 | P2 | Test data | Không có COMMENT fixture an toàn. | Chưa xử lý; RAI-02/08 được đánh dấu `BLOCKED`, không cộng điểm. |
| RAI-FIX-009 | P2 | Config env | DB runtime có Flyway version mới hơn checkout. | Không sửa source để che môi trường; ghi nhận ngoài phạm vi. |
| RAI-FIX-010 | P1 | n8n contract bug | OpenAI có lúc trả score `0.85` thay vì `85`. | Đã chuẩn hóa thang điểm và kiểm chứng runtime score `0..100`. |
| RAI-FIX-011 | P0 | Readiness bug | `/healthz` trả 200 trước khi webhook được đăng ký. | E2E production-webhook preflight có retry; không dùng healthz làm bằng chứng duy nhất. |
| RAI-FIX-012 | P1 | Code bug | Bulk chọn cả report `RESOLVED/REJECTED`, tạo `Failed 9`. | Đã lọc/disable report terminal ở selected và filtered mode; E2E sau sửa `Failed 0`. |
| RAI-FIX-013 | P2 | Generated artifact | Next dev hot-reload làm hỏng `.next/dev/types` khi typecheck chạy đồng thời. | Đã loại artifact sinh tự động bị hỏng, chạy typecheck/build tĩnh pass; source không bị sửa để che lỗi. |

## Vấn đề còn mở

| ID | Ưu tiên | Vấn đề | Điều kiện đóng |
|---|---|---|---|
| OPEN-001 | P1 | Webhook n8n chưa có HMAC/shared secret và replay protection. | Request sai chữ ký bị từ chối; có test hợp lệ/sai/hết hạn/replay. |
| OPEN-002 | P1 | Chưa có idempotency key cho retry. | Retry cùng request không tạo recommendation/job trùng. |
| OPEN-003 | P1 | Snapshot target chưa đầy đủ, đặc biệt USER không có version/updatedAt dùng cho kiểm tra stale. | Job lưu target version/hash và worker đối chiếu trước apply. |
| OPEN-004 | P2 | Thiếu COMMENT fixture E2E. | RAI-02 và RAI-08 pass trên runtime rồi cleanup target/report. |
| OPEN-005 | P2 | URL ảnh chỉ được gửi như text, chưa có vision thật. | Có image-fetch policy an toàn và model vision nếu nghiệp vụ yêu cầu. |
| OPEN-006 | P2 | Frontend chưa có unit/coverage tooling cho component này. | Có component tests và changed-file coverage gate phù hợp. |

## Kết luận issue

Không còn lỗi P0/P1 đã biết trong luồng runtime được kiểm chứng cho BLOG, USER, CAFE_PAGE, bulk và auto-apply. Trạng thái tổng thể vẫn `PARTIAL` vì COMMENT runtime chưa có fixture và các hạng mục production hardening còn mở.
