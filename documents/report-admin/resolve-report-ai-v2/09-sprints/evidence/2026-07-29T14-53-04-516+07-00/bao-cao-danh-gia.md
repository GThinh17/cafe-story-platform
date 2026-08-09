# Báo cáo đánh giá — DOD-FIX-04

## Kết luận

Luồng provider/n8n unavailable trước bản sửa fail-closed ở mức persistence nhưng contract lỗi
quá chung và UI không cho Admin đủ thông tin vận hành. Sau bản sửa, lỗi được phân loại là
operational failure, có support reference và retry rõ ràng; tuyệt đối không được biến thành
`REJECT`, `RESOLVE` hoặc recommendation giả.

## Bằng chứng trước sửa

- Backend trả HTTP `502` nhưng thiếu `code`, `correlationId`, `retryable`, `stage`.
- Admin chỉ hiển thị message chung.
- Resolution count vẫn `0 -> 0`, nên safety persistence đã fail-closed từ baseline.

## Bằng chứng sau sửa

- Failure contract:
  `AI_PROVIDER_BOUNDARY_FAILED`, canonical safe message, request correlation ID,
  `retryable=true`, `stage=N8N_PROVIDER`.
- UI hiển thị error code, stage, support reference và trạng thái retry.
- Sau khi n8n phục hồi, Admin retry thành công và nhận Contract V2 HTTP `200`.
- Report/target không mutation; auto-apply job `0`.
- Synthetic fixture được cleanup về `0/0`.

## Lỗi đã highlight

1. `FIX04-TEST-001` — cleanup fixture sai thứ tự dependency.
2. `FIX04-CODE-001` — Backend thiếu structured operational-error contract.
3. `FIX04-CODE-002` — Admin làm mất/không hiển thị operational metadata.
4. `FIX04-CONFIG-001` — Admin chưa có unit coverage script; đã dùng typecheck/build/E2E,
   nhưng không tuyên bố frontend unit coverage.

## Giới hạn

- Chỉ fault-inject n8n container unavailable; chưa bao phủ mọi failure mode của provider.
- Không publish workflow, không deploy production.
- Không triển khai DOD-FIX-05/06.
