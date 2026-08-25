# Workflow Improvement — DOD-FIX-04

## Đã cải thiện trong lượt này

- Fault injection dùng đúng container name, luôn recovery trong `finally`.
- Issue phải được ghi từ baseline trước khi sửa production source.
- Failure phase và recovery phase dùng cùng report để chứng minh retry thật.
- Mỗi phase lưu raw sanitized riêng; credential, HMAC và provider key không vào evidence.
- Cleanup query lại exact IDs và fail test nếu còn dữ liệu.

## Khuyến nghị tiếp theo

1. Tách thêm error code cho timeout, provider rate-limit, response-signature/auth failure và
   invalid response schema; không gom tất cả về provider unavailable.
2. Chuẩn hóa `Retry-After` hoặc backoff policy cho retryable errors.
3. Thêm metrics theo `code/stage` và correlation ID, tuyệt đối không label bằng secret/raw prompt.
4. Bổ sung Admin unit/component test infrastructure để kiểm coverage parser và error card.
5. Chuyển fault injection thành reusable fixture để DOD-FIX-05 không lặp logic container/cleanup.
6. Giữ n8n là orchestrator; Backend tiếp tục sở hữu error normalization, validation,
   persistence và mọi state change.
