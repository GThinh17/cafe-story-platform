# Detailed Design — G0-12-DONE-FIX-04

## 1. Mục tiêu và ranh giới

`DOD-FIX-04` hiện thực hóa `E2E-S1-13`: nếu Backend không gọi được n8n/provider thì đây là lỗi
vận hành, không phải kết luận về nội dung. Hệ thống phải:

- trả HTTP `502` với error contract có kiểu;
- không persist recommendation/resolution giả;
- không sửa report hoặc target;
- không tạo auto-apply job;
- cho Admin thấy support reference và khả năng retry;
- retry thành công sau khi dependency phục hồi.

Trong phạm vi: Backend error normalization, Admin error parsing/rendering, Playwright fault
injection và evidence. Ngoài phạm vi: DOD-FIX-05/06, thay prompt, publish n8n, production data,
secret và production deploy.

## 2. Lỗi baseline

### 2.1 Backend thiếu error contract vận hành

Khi n8n dừng, Backend cũ trả `502` qua envelope chung:

```text
message = Admin report AI resolution service unavailable
data    = null
```

Body thiếu `code`, `correlationId`, `retryable`, `stage`. Tuy nhiên resolution count vẫn `0 -> 0`;
vì vậy lỗi là observability/contract, không phải fake persistence.

### 2.2 Admin làm mất ngữ cảnh vận hành

UI cũ chỉ render message chung. Admin không biết lỗi thuộc boundary nào, không có support reference
để đối chiếu log và không biết retry có hợp lệ hay không.

### 2.3 E2E cleanup chưa chịu được background analytics

Attempt đầu gặp FK từ `blog_events`. Harness được sửa để xóa dependency theo đúng thứ tự và luôn
khởi động lại n8n trong `finally`.

## 3. Thiết kế Backend

### 3.1 Typed exception tại provider boundary

`AdminReportAiProviderBoundaryException` mang:

- canonical code `AI_PROVIDER_BOUNDARY_FAILED`;
- safe message `AI recommendation service is unavailable.`;
- stage `N8N_PROVIDER`;
- request correlation ID;
- cause nội bộ để log/diagnose, không serialize ra client.

`AdminReportAiResolutionServiceImpl.callWebhook()` tạo exception này khi transport fail hoặc n8n
trả non-2xx. Không có resolution nào được persist vì exception xảy ra trước bước validate/persist.

### 3.2 Exception advice và response DTO

`AdminReportAiExceptionAdvice` bắt chính xác typed exception và trả HTTP `502` bằng
`AdminReportAiOperationalErrorResponseDTO`:

```json
{
  "statusCode": 502,
  "status": "Fail",
  "message": "AI recommendation service is unavailable.",
  "data": null,
  "code": "AI_PROVIDER_BOUNDARY_FAILED",
  "correlationId": "request-uuid",
  "retryable": true,
  "stage": "N8N_PROVIDER"
}
```

DTO kế thừa envelope hiện hành để `GlobalResponseAdvice` không double-wrap. Raw body, chữ ký,
provider key và HMAC không được trả ra client.

## 4. Thiết kế Admin UI

`ApiError` giữ bốn trường operational từ response: `code`, `correlationId`, `retryable`, `stage`.
Ask AI dialog giữ state riêng cho operational error, không trộn với recommendation content.

Error card hiển thị:

1. safe message;
2. error code;
3. stage;
4. support reference;
5. retry available/unavailable;
6. hướng dẫn kiểm tra service rồi bấm Ask AI lại.

Nút Ask AI chỉ disable khi request đang chạy. Sau failure, nó được enable để Admin chủ động retry.

## 5. Luồng BE → n8n/provider → BE → FE

### 5.1 Failure phase

1. Admin mở report synthetic và bấm Ask AI.
2. Test đã dừng đúng local container `cafestory-n8n`.
3. Backend tạo correlation ID, gọi webhook và gặp transport failure.
4. Backend trả structured `502`; không vào validate/persist.
5. Admin render operational error và support reference.
6. Query DB xác nhận resolution count `0 -> 0`.

### 5.2 Recovery phase

1. Test khởi động lại đúng container `cafestory-n8n`.
2. Chờ health và exact webhook sẵn sàng.
3. Admin bấm Ask AI trên cùng report.
4. n8n/OpenAI trả signed Contract V2.
5. Backend verify, semantic validate và persist đúng một recommendation A0.
6. UI nhận HTTP `200`; DB xác nhận resolution `1`, auto job `0`.

### 5.3 Safety và cleanup

Snapshot trước/sau chứng minh report và BLOG không đổi. Cuối test xóa dependency, report và BLOG
theo exact IDs; số dòng còn lại `0/0`.

## 6. Traceability và kiểm chứng

| Requirement | Test/evidence | Kết quả |
|---|---|---|
| Provider non-2xx có typed exception | `TC013` | PASS |
| Advice không double-wrap và đủ field | `AdminReportAiExceptionAdviceTest` | PASS |
| Operational error trên UI | screenshot `E2E-S1-13` | PASS |
| Không fake resolution khi lỗi | failure raw | `0 -> 0` |
| Retry sau recovery | recovery raw | HTTP `200`, resolution `1` |
| A0 no mutation/no auto job | recovery raw | false/false/`0` |
| Test hygiene | cleanup raw | `0/0` |

Verification tổng:

- smallest focused Backend `2/2 PASS`;
- Admin Report AI focused `64/64 PASS`;
- full Backend `625`, failure/error `0`, skipped `1`;
- Admin typecheck/build `PASS`;
- Playwright `E2E-S1-13` `1/1 PASS`;
- 4 changed Backend classes line `100%`; service branch `87.75%`.

## 7. Trạng thái

- Implementation/test/evidence: `COMPLETED_VERIFIED_APPROVED`.
- User approval đã nhận: `APPROVE_G0-12-DONE-FIX-04`.
- n8n chỉ bị stop/start để fault injection; workflow không bị sửa/publish.
- Production deployment: `NOT_AUTHORIZED`.
- `G0-12-DONE` chưa đóng; còn DOD-FIX-05 và DOD-FIX-06.
