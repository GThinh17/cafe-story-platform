# Báo cáo đánh giá

## Trạng thái hiện tại

`DONE` — code, focused/regression tests, backend, n8n/OpenAI và authenticated Mobile/Admin UI runtime đã được kiểm chứng. Test data đã dọn và các process/port do task khởi động đã tắt.

## Đã kiểm chứng

- Mobile: i18n check, typecheck, Expo Web bundle và static report-comment regression đều pass; fresh locale hiển thị VI và chuyển EN/VI trên viewport 390×844.
- Admin: i18n check, typecheck, production build, i18n E2E 5/5 và translation E2E 1/1 pass.
- Backend: focused controller/service/validation tests pass; full Maven 1.383 pass, 0 fail, 0 error, 1 skip.
- Coverage Java mới: controller 100% line; service 100% line và 97,37% branch.
- n8n: exact contract, HMAC/replay/tamper, adversarial prompt và identifier preservation pass.
- Runtime n8n/OpenAI: English→Vietnamese và Vietnamese→English pass với signed strict response.
- Runtime backend→n8n→OpenAI: response `TRANSLATED`; URL, enum và diagnostic identifier được giữ nguyên.
- Workflow `cafestory-admin-content-translation-v1` được import và publish tách biệt, active, health n8n OK.
- Authenticated Mobile 390×844: login, Feed, Search, comment load/post/reply, report BLOG/COMMENT và chuyển EN/VI pass.
- Profile Mobile 390×844 được kiểm tra lại sau bản vá cuối: nhãn thống kê không chạm nhau; fallback trang quán và empty state hiển thị tiếng Việt (`mobile-profile-vietnamese-390x844-final.png`).
- Caption và comment giữ nguyên văn khi UI đổi locale; UI chrome trọng yếu hiển thị VI sau khi khôi phục preference.
- Authenticated Admin: mở report COMMENT, target type/status đúng, dịch theo nút, safe error/retry, chuyển bản gốc/bản dịch và giữ UUID/diagnostic number pass.
- Hai report test đã chuyển `REJECTED`; blog/comment target vẫn `PUBLISHED`; comment/reply test đã xóa.
- Không có listener còn lại trên 8080, 8081, 3636, 5678 hoặc 6379.

## Chưa có tooling

`FRONTEND_COVERAGE_TOOLING_MISSING`: Mobile và Admin chưa có changed-file unit coverage instrumentation. Không suy diễn coverage từ build/E2E.

## Ghi chú tác dụng phụ

Lần khởi động backend đầu tiên đã kích hoạt scheduler có sẵn và có thể ghi derived `blog_daily_metrics` trong development database. Backend đã được khởi động lại với scheduler/data initializer tắt; không tự ý xóa dữ liệu vì không có ownership trail an toàn.
