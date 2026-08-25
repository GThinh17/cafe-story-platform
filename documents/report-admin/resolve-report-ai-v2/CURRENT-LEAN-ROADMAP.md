# Lean Roadmap — Resolve Report with AI V2

## Mục tiêu đóng chức năng

Chứng minh chức năng Admin Resolve Report with AI chạy đúng trên luồng thực tế,
fail-safe khi AI/evidence không đủ và không tự thay đổi report/target.

## Trạng thái hiện tại

- Core Sprint 1 đã hoàn thành.
- S2-01–S2-04 đã approved.
- Findings S2-DONE-AUDIT đã được review.
- `S2-DONE-FIX-01` đã sửa và verify các finding thuộc test/evidence.
- `S2-DONE-FIX-02-DB-HASH-LENGTH` đã sửa và verify mismatch
  `targetSnapshotHash 71 ký tự > VARCHAR(64)`.
- Flyway current: `20260730.01`; DB/entity length: `71`.
- Lean Functional Gate đã pass.
- Final approval đã nhận: `APPROVE_S2_DONE`.
- Trạng thái core: `COMPLETED_VERIFIED_APPROVED`.
- Không còn hành động bắt buộc.

## Ba bước closure đã hoàn thành

### 1. Sửa một lần

- Cập nhật security harness fixture theo canonical request hiện tại.
- Bổ sung focused test cho các nhánh fail-closed đang thiếu.
- Tạo `summary.json` lịch sử nếu thực hiện được ngay; thiếu file này không còn
  là blocker chức năng.

Không chia thành micro-gate cho từng lỗi.

### 2. Test chức năng

Chỉ cần các gate sau:

- [x] Security HMAC/freshness/replay harness `PASS`.
- [x] Backend focused happy path và fail-closed `PASS`.
- [x] Backend full regression không có failure/error.
- [x] Một Admin E2E happy path:
  `Admin → Backend → n8n/provider → Backend → Admin`.
- [x] Một Admin E2E fail-safe: evidence thiếu hoặc provider lỗi phải về manual,
  không persist AI recommendation không hợp lệ.
- [x] Report và target không bị AI tự động mutation.

Kết quả cuối gate:

- Security, Backend focused và Backend full regression: `PASS`.
- Provider-boundary failure: `PASS` (`502` có typed error, persist `0`).
- Provider recovery và persisted recommendation: `PASS`.
- Critical-evidence manual/no-action: `PASS`.
- No-mutation, auto-job `0` và cleanup synthetic data: `PASS`.

Coverage được báo cáo để tham khảo; không chặn đóng chức năng nếu production
source không đổi và các hành vi bắt buộc ở trên đã pass.

### 3. Đóng chức năng

Nếu toàn bộ functional gate pass:

```text
APPROVE_S2_DONE
```

Token đã được nhận. Resolve Report with AI V2 core được coi là hoàn thành.

## Không chặn việc đóng core

- Provider/model benchmark mở rộng S2-05.
- Numeric calibration.
- Sprint 3 advanced audit/operations.
- Sprint 4 USER/CAFE_PAGE deep evidence, OCR/vision.
- Mở rộng tài liệu lý thuyết hoặc thêm micro approval gate.

Các mục này được giữ ở backlog và chỉ mở lại khi người dùng yêu cầu.
