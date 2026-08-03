# S2-05 — Workflow retrospective

## Điều đã hoạt động đúng

1. Dry-run khóa exact models/cases/repeats/cost trước external call.
2. Secret chỉ được kiểm tra presence/length; không xuất value.
3. Synthetic/sanitized data và `store=false` giữ data boundary.
4. Internal strict schema tách khỏi canonical runtime schema, tránh tạo final
   decision/action authority.
5. Quality denominator `0` chặn việc gọi benchmark là accuracy/model winner.
6. Raw error/body không được persist.

## Điều cần cải thiện ngay

1. Provider-compatible schema phải được validate như một artifact riêng trước
   execute; Ajv Draft 2020-12 pass không chứng minh Responses subset pass.
2. Thêm một schema-probe request trước full matrix. Chỉ sau probe pass mới mở
   35 request còn lại.
3. Global circuit breaker phải là gate bắt buộc cho auth/schema/config.
4. Failed run và retest phải dùng output path khác nhau.
5. Cần capture safe provider error details đủ để diagnose keyword/path mà không
   lưu raw message hoặc secret.

## Cải tiến sau

1. Tạo authoritative human-labeled HAR dataset và reviewer agreement để quality
   denominator lớn hơn `0`.
2. Tách cold/warm latency và ghi percentile chỉ cho successful inference.
3. Dùng snapshot model ID sau khi candidate được chọn, không activate alias chỉ
   từ một benchmark.
4. Thêm pricing refresh gate; pricing pin ngày `2026-07-30` không mặc định đúng
   cho lần chạy sau.
5. Chỉ nối benchmark candidate vào n8n staging sau model-change approval, không
   thay runtime workflow trực tiếp từ S2-05.

## Retest workflow khuyến nghị

```text
static schema subset gate
→ one-call provider schema probe
→ global stop check
→ bounded full matrix
→ post-validation
→ safety eligibility
→ stability/latency/cost
→ human quality review
→ model-change decision
```

