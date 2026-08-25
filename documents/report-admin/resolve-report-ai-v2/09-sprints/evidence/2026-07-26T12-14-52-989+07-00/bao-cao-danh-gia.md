# Báo cáo đánh giá G0-12A

## 1. Trạng thái

`PARTIAL — IMPLEMENTED_SOURCE_VERIFIED_AWAITING_APPROVAL`

Source Backend và n8n export đã được implement, test và review. Gate chưa được tick hoàn thành vì chưa nhận
`APPROVE_G0-12A`; published runtime được thiết kế chuyển sang G0-12D.

## 2. Đã thực hiện

- thêm `AdminReportAiWebhookSigner`;
- dùng JCS/RFC 8785 trước SHA-256/HMAC;
- ký request Backend → n8n bằng sáu `X-CafeStory-*` headers;
- verify signed response trước JSON parse/semantic validation/persistence;
- timestamp freshness `±120 giây`;
- response replay cache TTL `300 giây`;
- n8n verify request trước provider;
- n8n ký response sau normalize;
- n8n request nonce cache theo workflow static data;
- cấu hình `NODE_FUNCTION_ALLOW_BUILTIN=crypto`;
- thêm Java tests và executable Node workflow validator;
- giữ workflow export `active=false`;
- không thay đổi FE, DB hoặc automation authority.

## 3. Đã kiểm chứng

| Kiểm chứng | Kết quả thực tế |
|---|---|
| Backend baseline trước sửa | `12/12 PASS` |
| Backend focused + regression + Spring context sau sửa | `45/45 PASS` |
| Signer line coverage | `88/88 = 100%` |
| Signer branch coverage | `48/56 = 85.71%` |
| n8n valid signed request | `PASS` |
| n8n nonce replay | `REJECTED` |
| n8n stale timestamp | `REJECTED` |
| n8n tampered body | `REJECTED` |
| n8n invalid signature | `REJECTED` |
| n8n signed response | `PASS` |
| Workflow JSON parse | `PASS`, 5 nodes |
| Workflow active flag | `false` |
| Docker Compose static config | `PASS` |
| Git whitespace check | `PASS` |

## 4. Lỗi được highlight

### Critical đã sửa ở source/static

- `G012A-SEC-001`: Backend không ký request.
- `G012A-SEC-002`: n8n không verify request/freshness/replay trước provider.
- `G012A-SEC-003`: Backend tin response không có chữ ký.

### High đã sửa

- `G012A-SEC-008`: canonicalizer tự viết có nguy cơ lệch Java/JavaScript với numeric serialization.

### Lỗi do test bắt được và đã sửa

- `G012A-CODE-006`: empty JSON chưa fail closed.
- `G012A-CODE-007`: security failure bị báo nhầm thành invalid JSON.

### Critical còn cần người dùng xử lý

- `G012A-SEC-009`: provider key thật đã xuất hiện trong output cục bộ; cần thu hồi/rotate trước G0-12D.
- File `docker/.env` đang được Git ignore và không được stage.

### Runtime blocker

- Docker Desktop/daemon chưa chạy.
- `ADMIN_REPORT_AI_HMAC_SECRET` chưa được cấu hình.

## 5. Chưa kiểm chứng

- import/publish workflow thật;
- exact production webhook;
- provider call thật;
- PostgreSQL runtime;
- FE/Admin E2E;
- nonce behavior dưới multi-instance/concurrency cao.

Các mục này không được ghi là pass và được chuyển đúng sang G0-12C/G0-12D/G0-12E.

## 6. Residual design risk

`$getWorkflowStaticData('global')` chỉ phù hợp baseline single-instance:

- chỉ persist ở production execution;
- có cảnh báo không ổn định khi execution tần suất cao;
- không cung cấp atomic distributed uniqueness.

Khi scale, phải thay bằng Redis/PostgreSQL atomic TTL store ở cả n8n request nonce và Backend response nonce.

## 7. Kết luận

G0-12A đã đạt acceptance ở cấp source, unit/integration test và executable n8n export test. Chưa thể gọi là
runtime-ready. Gate đang chờ người dùng review bằng token:

```text
APPROVE_G0-12A
```
