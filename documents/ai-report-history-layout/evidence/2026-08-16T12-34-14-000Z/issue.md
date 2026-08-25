# Issues

## LAYOUT-001 — Recommendation history outside the main column

- Classification: `CODE_BUG`
- Evidence: ảnh người dùng và `screenshots/LAYOUT-BLOG-desktop-light.png` sau sửa.
- Affected behavior: lịch sử từng nằm sau toàn bộ desktop grid nên chiếm toàn chiều ngang và tạo khoảng trắng lớn dưới phần nội dung ngắn hơn.
- Likely owner: admin report detail layout.
- Proposed action: đặt history vào cột nội dung chính và giữ evidence ở cột grid riêng.
- Auto-fix allowed: yes.
- Result: `FIXED`.

## LAYOUT-002 — Frontend changed-file coverage unavailable

- Classification: `CONFIG_ENV`
- Evidence: admin chưa có unit coverage instrumentation.
- Affected behavior: không thể báo line/branch coverage định lượng cho JSX layout.
- Likely owner: frontend test tooling.
- Proposed action: dùng typecheck, production build, Playwright geometry assertions và screenshot evidence.
- Auto-fix allowed: no.
- Marker: `FRONTEND_COVERAGE_TOOLING_MISSING`.
