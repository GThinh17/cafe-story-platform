# Fix log — DOD-FIX-06

- Đã xác lập baseline trước khi sửa production source.
- Chưa kết luận có `CODE_BUG` cho tới khi executable E2E chạy.
- E2E baseline pass ngay trên code hiện tại; không sửa production source.
- Đã chứng minh FE disabled, UI request count `0`, Backend bypass HTTP `409`.
- `FIX06-TEST-001`: thay status-only assertion bằng canonical persisted report-state comparison.
- `FIX06-TEST-002`: GET lại persisted state sau status PATCH để loại precision mismatch của
  `resolvedAt`; focused E2E pass và `reportMutation=false`.
