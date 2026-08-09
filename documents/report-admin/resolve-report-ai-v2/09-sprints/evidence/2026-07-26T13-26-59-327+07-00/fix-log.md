# G0-12C — Fix log

## G012C-TEST-001 — FIXED

- Không sửa source hoặc migration.
- Đổi decision của DB probe từ giá trị không hợp lệ `NO_VIOLATION` sang giá trị canonical `REJECT`.
- Chạy lại bốn negative probes.
- Kết quả: cả bốn probe pass, transaction rollback, row count cuối bằng `0`.

## G012C-ENV-002 — NOT FIXED / OUTSIDE GATE

- Backend vẫn khởi động và kiểm tra G0-12C pass.
- Không thay đổi scheduler/cache source để che warning môi trường.
- Đề xuất tạo runtime integration profile riêng ở phase cải tiến.

## G012C-DATA-003 — ACCEPTED RESIDUAL RISK

- Không đưa dữ liệu production vào DB test.
- Không tạo legacy fixture tùy tiện.
- G0-12C chỉ kết luận trên synthetic pre-V2 disposable snapshot.

## Cleanup

- Backend đã dừng.
- Container `cafestory-g0-12c-postgres` đã dừng nhưng chưa xóa để người dùng có thể inspect.
- Không có AI resolution hoặc auto-apply job test còn lại.
- n8n hiện hữu không bị publish, sửa hoặc dừng.

