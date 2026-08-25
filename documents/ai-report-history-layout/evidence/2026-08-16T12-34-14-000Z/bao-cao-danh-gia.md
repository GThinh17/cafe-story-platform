# Báo cáo đánh giá bố cục lịch sử AI Report

## Kết quả

- `AI recommendation history` đã được chuyển vào cột nội dung chính, ngay sau nội dung report và khu xử lý BLOG/COMMENT.
- Cột `Latest AI recommendation`/bằng chứng vẫn ở bên phải trên desktop, song song với lịch sử.
- Khoảng trắng do lịch sử từng nằm dưới toàn bộ grid đã được loại bỏ.
- Trên mobile, thứ tự hiển thị là nội dung → lịch sử → bằng chứng; không có horizontal overflow.

## Kiểm chứng

- `npm run i18n:check`: `PASS`.
- `npm run typecheck`: `PASS`.
- `npm run build`: `PASS`.
- `npx playwright test tests/e2e/admin-report-ai.spec.ts --project=chromium --grep "E2E-REPORT-ACTIONS"`: `1/1 PASS`.
- E2E kiểm tra hình học cho BLOG và COMMENT:
  - history nằm trong biên cột nội dung;
  - evidence bắt đầu ở bên phải cột nội dung trên desktop;
  - history và evidence có vùng dọc song song;
  - evidence nằm sau history trên mobile.

## Evidence

- `screenshots/LAYOUT-BLOG-desktop-light.png`
- `screenshots/LAYOUT-COMMENT-desktop-light.png`
- `screenshots/LAYOUT-BLOG-mobile.png`
- `screenshots/LAYOUT-COMMENT-mobile.png`
- `raw/report-actions-policy-summary.json`

Target BLOG/COMMENT được E2E khôi phục trạng thái ban đầu; report test được đóng trong cleanup.
