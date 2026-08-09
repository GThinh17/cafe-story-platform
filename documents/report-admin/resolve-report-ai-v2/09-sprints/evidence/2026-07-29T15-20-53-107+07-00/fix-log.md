# Fix log — DOD-FIX-05

- Đã xác lập baseline trước khi sửa production source.
- Chưa kết luận có `CODE_BUG` cho tới khi executable E2E chạy.
- `FIX05-TEST-001`: sửa browser response listener dùng đúng Playwright `Response`; typecheck pass.
- `FIX05-TEST-002`: bỏ dependency vào helper component không tồn tại trong test; typecheck pass.
- `FIX05-TEST-003`: tách combined grep thành hai command literal trên Windows; cả hai pass.
- `FIX05-TEST-004`: scroll failure alert trước screenshot; visual QA pass.
- `FIX05-TEST-005`: mở rộng no-mutation assertion từ status sang canonical report state; E2E pass.
- `FIX05-TEST-006`: GET lại terminal persisted state sau PATCH để không so timestamp trước/sau
  PostgreSQL microsecond normalization; ghi canonical before/after vào raw evidence, hai item đều
  `reportMutation=false`.
- Production source không cần sửa vì runtime behavior đã đạt `SAF-011`.
