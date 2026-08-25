# Báo cáo đánh giá S2-DONE-FIX-01

## Kết luận

Trạng thái package là `PARTIAL`, không phải `DONE`.

Ba finding ban đầu của audit đã được xử lý:

1. Security harness dùng canonical S2 request và pass.
2. Ba nhánh fail-closed có focused regression test.
3. Historical S2-01 có `summary.json` được reconstruct từ evidence đã approved.

Backend static/focused/full regression đều pass. Tuy nhiên, kiểm thử giao diện
full-path đã phát hiện lỗi thật mà các mock/unit test không thể thấy:

```text
targetSnapshotHash = "sha256:" + 64 hex = 71 ký tự
target_snapshot_hash trong DB = VARCHAR(64)
```

Do đó PostgreSQL từ chối persist mọi AI resolution dùng canonical hash. Lỗi làm
retry happy path và manual fail-safe path cùng không thể hoàn tất trên UI.

## Điều đã chứng minh được

- HMAC/freshness/replay/tamper gate hoạt động.
- Provider outage trả typed `502 AI_PROVIDER_BOUNDARY_FAILED`, retryable và
  không persist recommendation.
- Khi request E2E thất bại, report/target không bị mutation.
- Không tạo auto-apply job ngoài ý muốn.
- Fixture E2E được cleanup về `0`.
- Backend regression `641` test không có failure/error.

## Điều chưa đạt

- Chưa có happy-path E2E `Admin → Backend → n8n/provider → Backend → Admin`
  hoàn tất.
- Chưa hiển thị được persisted manual recommendation của `E2E-S1-04`.
- Chưa được phép sửa database trong Bound hiện tại.

## Sửa đúng được đề xuất

Gate tiếp theo cần rất hẹp:

1. Tạo Flyway migration mới, không sửa migration đã áp dụng, đổi
   `target_snapshot_hash` từ `VARCHAR(64)` thành `VARCHAR(71)`.
2. Đồng bộ entity annotation thành `length = 71`.
3. Thêm migration/schema integration assertion khóa độ dài.
4. Chạy lại focused/full Backend và hai E2E `E2E-S1-13`, `E2E-S1-04`.
5. Chỉ khi cả hai E2E pass mới re-audit và cho phép `APPROVE_S2_DONE`.

Không nên cắt bỏ prefix `sha256:` hoặc truncate hash vì sẽ làm sai canonical
contract và giảm khả năng truy vết thuật toán digest.

## Evidence

- [`test-log.md`](test-log.md)
- [`issue.md`](issue.md)
- [`fix-log.md`](fix-log.md)
- [`coverage.md`](coverage.md)
- [`e2e-provider-recovery/raw/E2E-S1-13-provider-boundary-failure.json`](e2e-provider-recovery/raw/E2E-S1-13-provider-boundary-failure.json)
- [`e2e-manual-failsafe/raw/E2E-S1-04-runtime-result.json`](e2e-manual-failsafe/raw/E2E-S1-04-runtime-result.json)
