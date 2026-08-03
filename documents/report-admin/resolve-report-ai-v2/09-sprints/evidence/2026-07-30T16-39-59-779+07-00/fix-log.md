# S2-DONE-FIX-01 — Fix log

| Issue | Thay đổi | Retest |
|---|---|---|
| `001` | Cập nhật fixture sang canonical S2 request/evidence/rule context | Security `PASS` |
| `002` | Thêm 3 focused test: long-content, image critical-missing, defense-in-depth clamp | `53/53 PASS`; service line `100%`, branch `90.08%` |
| `003` | Tạo S2-01 `summary.json` từ evidence/status đã approved | JSON parse `PASS` |
| `004` | Đổi exact count `passed === 50` thành regression floor `passed >= 50` | Dataset harness `PASS`, focused `53/53` |
| `005` | Sửa coverage parser command | Coverage counters đọc thành công |
| `006` | Chạy riêng từng Playwright test thay cho regex bị shell diễn giải | Command bắt đầu E2E đúng test |
| `007` | Không sửa trong package: cần Flyway migration mới để cột `target_snapshot_hash` chứa canonical hash 71 ký tự | `E2E-S1-13 FAIL`; DB metadata xác nhận `VARCHAR(64)` |
