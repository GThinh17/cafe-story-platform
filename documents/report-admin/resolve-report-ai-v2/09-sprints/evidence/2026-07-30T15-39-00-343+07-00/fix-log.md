# S2-05 — Fix log

| Issue | Thay đổi | Verify | Trạng thái |
|---|---|---|---|
| `S2-05-ISSUE-001` | Tách benchmark-only per-rule schema khỏi canonical runtime schema | schema/negative/provider-boundary gate `PASS` | `RESOLVED_VERIFIED` |
| `S2-05-ISSUE-002` | Không sửa repo để che lỗi Docs MCP; dùng official web fallback | nguồn chỉ từ `developers.openai.com` | `OPEN_NON_BLOCKING` |
| `S2-05-ISSUE-003` | Giữ quality denominator `0`, cấm accuracy/calibration/model selection | config mutation guards + summary assertion `PASS` | `OPEN_EXPECTED_LIMITATION` |
| `S2-05-ISSUE-004` | Test đúng output property boundary thay vì cấm câu phủ định trong prompt | focused + coverage `PASS` | `RESOLVED_VERIFIED` |
| `S2-05-ISSUE-005` | Thêm provider-compatible Structured Outputs adapter; giữ internal post-validation | static schema compatibility + dry-run `PASS` | `PROVIDER_RETEST_PENDING` |
| `S2-05-ISSUE-006` | Thêm GLOBAL/MODEL/NONE circuit breaker | stop-decision assertions + coverage `PASS` | `RESOLVED_STATIC_VERIFIED` |
| `S2-05-ISSUE-007` | Tách output path của failed run và retest để không ghi đè evidence | dry-run path assertion/inspection | `RESOLVED_STATIC_VERIFIED` |

Không đổi Backend, FE, mobile, database, canonical n8n workflow, policy status,
runtime authority hoặc production deployment.
