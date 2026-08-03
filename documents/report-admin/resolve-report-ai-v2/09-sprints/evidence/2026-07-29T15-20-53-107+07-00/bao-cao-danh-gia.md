# Báo cáo đánh giá — DOD-FIX-05

## Kết luận

`SAF-011 / E2E-S1-09` đã có executable full-path evidence. Bulk UI giữ isolation theo từng item:
một report chuyển terminal sau selection trả HTTP `409`, report còn lại vẫn gọi n8n/OpenAI và trả
HTTP `200`. Không phát hiện production code bug; remediation là bổ sung test/evidence còn thiếu.

## Kết quả thực tế

- Total `2`.
- Completed `1`.
- Needs manual review `1`.
- Failed `1`.
- Remaining `0`.
- Successful item: resolution `0 -> 1`, canonical report state giữ nguyên ở `OPEN`, target không
  mutation, auto job `0`.
- Failed item: resolution `0 -> 0`, canonical report state giữ nguyên ở `RESOLVED`, target không
  mutation, auto job `0`.
- Cleanup: report/BLOG còn `0/0`.

## Verification

- Playwright `E2E-S1-09`: `1/1 PASS` sau visual-evidence rerun.
- Admin typecheck: `PASS`.
- Admin production build: `PASS`.
- Full Backend regression: `625`, failure/error `0`, skipped `1`.
- Production coverage: `N/A`, không thay production file trong FIX-05.
- Frontend unit coverage: chưa có tooling; đã ghi `FIX05-CONFIG-001`.

## Lỗi đã highlight

1. `FIX05-TEST-001`: dùng sai Playwright response type.
2. `FIX05-TEST-002`: gọi helper component không tồn tại trong test.
3. `FIX05-TEST-003`: grep regex bị Windows shell diễn giải thành pipe.
4. `FIX05-TEST-004`: screenshot chưa đưa failure-detail vào viewport.
5. `FIX05-TEST-005`: no-mutation assertion ban đầu chỉ kiểm status.
6. `FIX05-TEST-006`: lấy PATCH response trước PostgreSQL microsecond normalization làm baseline;
   đã GET lại persisted state rồi mới lưu/so canonical before/after.
7. `FIX05-CONFIG-002`: terminal wrapper từ chối compound Docker command; đã tách lệnh và
   readiness probe pass.

Các test bug đều đã verify; config limitation đã được xử lý trong verification workflow. Open issue
trong phạm vi: `0`.
