# Issue Register — G0-12D

## Bound

- Gate: `G0-12D`.
- Mục tiêu: import/publish đúng workflow n8n V2 và kiểm chứng security/provider boundary ở runtime.
- Trong phạm vi: n8n local, workflow ID `cafestory-admin-report-ai-resolution-v2`, exact production webhook, secret readiness ở mức boolean.
- Ngoài phạm vi: Admin UI/E2E (`G0-12E`), production deploy, production database và thay đổi dữ liệu nghiệp vụ.
- Quy tắc bí mật: không ghi giá trị, độ dài hoặc fingerprint của secret vào evidence.

## Issues

| ID | Phân loại | Mức độ | Evidence | Xử lý | Trạng thái |
|---|---|---:|---|---|---|
| `G012D-RUNTIME-001` | `RUNTIME_DRIFT` | `CRITICAL` | ID canonical V2 đang active nhưng code runtime không có HMAC/body hash/timestamp/nonce và vẫn dùng `confidenceScore`/`riskScore` | Backup toàn bộ 5 workflow; unpublish đúng ID; restart n8n; import canonical export với `active=false` | `FIXED_VERIFIED_SAFE_STATE` |
| `G012D-SECRET-002` | `SECRET_EXPOSURE` | `CRITICAL` | Provider key đang configured nhưng là khóa tồn tại trước sự cố lộ key đã ghi nhận tại G0-12A | Người dùng đã revoke/rotate; key mới chỉ lưu local và OpenAI preflight trả `200` | `RESOLVED_VERIFIED` |
| `G012D-CONFIG-003` | `CONFIG_ENV` | `HIGH` | Container runtime ban đầu trả `ADMIN_REPORT_AI_HMAC_SECRET_CONFIGURED=false` | Sinh secret 256-bit local, cấu hình cùng giá trị cho Backend/n8n, không in hoặc commit | `FIXED_VERIFIED` |
| `G012D-OPS-004` | `OPERATIONS_HARDENING` | `MEDIUM` | `N8N_ENCRYPTION_KEY` không được khai báo tường minh trong container env | Đánh giá và chuẩn hóa secret lifecycle/backup trước production; không tự ý thay key của volume hiện hữu | `OUT_OF_SCOPE_FOLLOW_UP` |
| `G012D-CONFIG-005` | `CONFIG_ENV` | `HIGH` | So sánh boolean cho thấy AI Python và Backend `.env` không khớp key mới trong `docker/.env`; Backend source không có consumer `OPENAI_API_KEY` | Đồng bộ key mới chỉ sang AI Python; xóa key thừa khỏi Backend; giữ FE/mobile không có key | `FIXED_VERIFIED` |
| `G012D-CODE-006` | `CODE_BUG` | `MEDIUM` | Disposable fixture fail trước khi chạm env thật: Windows PowerShell 5.1 không có static `RandomNumberGenerator.Fill` | Dùng instance `RandomNumberGenerator.Create().GetBytes()` và rerun fixture | `FIXED_VERIFIED` |
| `G012D-TEST-007` | `TEST_BUG` | `LOW` | Fixture assertion dùng `$` nhưng không chấp nhận ký tự `\r` của CRLF Windows | Đổi regex assertion thành `\r?$`, không sửa production behavior | `FIXED_VERIFIED` |
| `G012D-CODE-008` | `CODE_BUG` | `MEDIUM` | Chạy helper không truyền path fail trước khi đọc env vì PowerShell 5.1 đánh giá parameter default lúc `$PSScriptRoot` còn rỗng | Resolve default paths trong thân script bằng `$MyInvocation.MyCommand.Path`; rerun fixture và real sync | `FIXED_VERIFIED` |
| `G012D-TEST-009` | `TEST_BUG` | `LOW` | AI Python preflight đầu tiên gọi `OpenAIModel()` thiếu required `model_name`; key load vẫn trả `true` | Đọc constructor và rerun với model name hiện có, không sửa production class | `FIXED_VERIFIED` |
| `G012D-CODE-010` | `CODE_BUG` | `CRITICAL` | Version `102dc461-...`: execution `205` và replay `206` đều `success`, chứng minh `$getWorkflowStaticData()` không persist nonce runtime | Thay bằng atomic nonce files trên persisted n8n volume; version `4e221ccf-...`: execution `215` success, replay `216` error | `FIXED_VERIFIED_RUNTIME` |
| `G012D-CODE-011` | `CODE_BUG` | `MEDIUM` | n8n `2.28.6` trả HTTP `200` body rỗng khi Code node reject dù execution status là `error` | Backend hiện fail closed vì thiếu signed response; đề xuất explicit rejection branch trả sanitized 4xx trước production | `OPEN_NON_BLOCKING_HARDENING` |

## Highlight

Lỗi `G012D-RUNTIME-001` không phải nhận xét mơ hồ: workflow đang publish thực tế là logic cũ và không có security boundary đã thiết kế. Endpoint chỉ được đưa về trạng thái an toàn sau khi unpublish và restart; direct probe sau đó trả `404 Active version not found`.

## Gate outcome

Ba điều kiện publish đã đạt:

1. provider key cũ đã revoke/rotate và key mới được OpenAI xác nhận `200`;
2. `ADMIN_REPORT_AI_HMAC_SECRET` dùng chung Backend/n8n đã configured;
3. canonical published runtime và exact-webhook matrix đã pass.

`G012D-CODE-011` và `G012D-OPS-004` là hardening còn lại, không làm mất fail-closed invariant và không
được hiểu là production-ready.
