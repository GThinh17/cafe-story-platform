# Báo cáo đánh giá — G0-12D

## 1. Trạng thái

`DONE — COMPLETED_RUNTIME_APPROVED`

Các acceptance bắt buộc của G0-12D đã có evidence runtime. Người dùng đã review và chốt gate bằng
`APPROVE_G0-12D`; checklist G0-12D đã được tick hoàn thành.

## 2. Bound

- Trong phạm vi: secret wiring local, n8n canonical workflow, exact published webhook, OpenAI provider boundary,
  Backend signer/service regression.
- Không tác động: Admin UI/E2E (`G0-12E`), production deploy, production database, report/target thật và Admin
  Assistant workflow.
- Dữ liệu provider test: synthetic, không có PII và không liên kết record CafeStory.
- Secret: không in giá trị, độ dài hoặc fingerprint vào log/evidence.

## 3. Đã thực hiện

- backup toàn bộ `5` workflow trước mutation;
- vô hiệu hóa runtime legacy không HMAC và import canonical V2;
- dùng key mới trong `docker/.env` cho n8n và AI Python;
- xóa `OPENAI_API_KEY` thừa khỏi Backend; FE/mobile tiếp tục không giữ key;
- sinh shared HMAC local cho Backend/n8n;
- xác nhận key mới qua OpenAI API preflight `200`;
- publish exact workflow ID/version;
- phát hiện `$getWorkflowStaticData()` không persist nonce ở runtime;
- thay replay cache bằng atomic hashed nonce files trên persisted n8n volume, TTL 300 giây;
- import/publish version đã sửa và chạy exact-webhook matrix;
- verify provider Contract V2 response và HMAC response signature;
- chạy focused Backend signer/service regression.

## 4. Đã kiểm chứng

| Tiêu chí | Phương pháp | Kết quả thực tế | Trạng thái |
|---|---|---|---|
| n8n liveness | `GET /healthz` sau recreate/restart | `200 {"status":"ok"}` | `PASS` |
| Secret không lộ | Boolean equality + secret scan | Không output giá trị; raw secret hit `0` | `PASS` |
| OpenAI key mới | `GET https://api.openai.com/v1/models` từ n8n container | `200` | `PASS` |
| AI Python | Load settings + init OpenAI client | configured `true`, init `PASS` | `PASS` |
| Shared HMAC | So sánh local n8n/Backend và container env | configured/match `true` | `PASS` |
| Canonical publish | n8n published export | version `4e221ccf-...`, `active=true` | `PASS` |
| Runtime/source parity | SHA-256 node parameters | `5/5` node match | `PASS` |
| Valid signed request | Exact production webhook | provider response `200`, Contract V2 signed | `PASS` |
| Recommendation semantics | Synthetic evidence packet | `NEEDS_MANUAL_REVIEW + NO_ACTION`; không numeric score | `PASS` |
| Response authenticity | Recompute JCS body hash + HMAC | body hash/signature match | `PASS` |
| Replay | Execution audit | `215 success`; same nonce `216 error` | `PASS` |
| Stale/future | Execution audit | `217/218 error` trước provider | `PASS` |
| Invalid signature | Execution audit | `219 error` trước provider | `PASS` |
| Tampered body | Execution audit | `220 error` trước provider | `PASS` |
| Missing headers | Execution audit | `221 error` trước provider | `PASS` |
| Nonce persistence | Count trước/sau n8n restart | `4 → 4` | `PASS` |
| Static nonce behavior | replay/expiry/atomic claim | Tất cả `PASS` | `PASS` |
| Backend regression | Maven focused suites | `37/37`, failure/error `0` | `PASS` |

## 5. Lỗi quan trọng đã phát hiện và sửa

### Runtime drift

Workflow cùng ID V2 từng publish logic cũ, thiếu HMAC/freshness/replay và còn `confidenceScore`/`riskScore`.
Đã backup, unpublish, import/publish canonical và verify parity.

### Replay protection chỉ pass static nhưng fail runtime

Version `102dc461-...` cho cùng nonce tạo execution `205` và `206`, cả hai `success`. Điều này chứng minh
`$getWorkflowStaticData()` không đạt assumption persistence tại runtime đang dùng.

Bản sửa:

- hash nonce bằng SHA-256 trước khi làm filename;
- atomic claim bằng `openSync(..., "wx")`;
- permission `0600`, directory `0700`;
- TTL `300` giây và cleanup file hết hạn;
- lưu trên volume `/home/node/.n8n`;
- version mới `4e221ccf-...` có execution valid `215 success`, replay `216 error`.

## 6. Chưa kiểm chứng hoặc chưa đạt

- Admin UI/account test và no-mutation E2E thuộc `G0-12E`;
- production deployment chưa được phép;
- multi-instance/distributed replay protection chưa được phép suy ra từ file store single-instance.

## 7. Residual không block G0-12D

- n8n `2.28.6` trả transport `200` body rỗng khi validator throw. Execution status/error vẫn đúng và Backend
  fail closed do thiếu signed response. Nên thêm explicit sanitized 4xx rejection branch trước production.
- `N8N_ENCRYPTION_KEY` chưa được khai báo tường minh; không tự thay key của volume hiện hữu nếu chưa có
  credential backup/restore drill.
- File nonce phù hợp local/single-instance baseline; khi scale ngang phải dùng Redis/PostgreSQL atomic TTL store.

## 8. Kết luận gate

```text
G0-12D STATUS: COMPLETED_RUNTIME_APPROVED
APPROVAL RECEIVED: APPROVE_G0-12D
NEXT GATE: IMPLEMENT_G0_12E
PRODUCTION DEPLOYMENT: NOT AUTHORIZED
```
