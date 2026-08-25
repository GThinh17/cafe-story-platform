# Fix Log — G0-12A

| Issue | Hành động | File chính | Verify | Trạng thái |
|---|---|---|---|---|
| `G012A-SEC-001` | Thêm JCS/HMAC signer, sáu request headers và fail closed khi thiếu secret | `AdminReportAiWebhookSigner.java`, `AdminReportAiResolutionServiceImpl.java` | Service HTTP test | `FIXED_VERIFIED` |
| `G012A-SEC-002` | Verify secret/header/freshness/nonce/body hash/HMAC trước OpenAI; nonce TTL 300 giây | n8n workflow export | Node executable validator | `FIXED_STATIC_VERIFIED` |
| `G012A-SEC-003` | Ký normalized response và bắt Backend verify trước parse/persist | n8n export, BE service/signer | Signed/unsigned response tests | `FIXED_STATIC_VERIFIED` |
| `G012A-ENV-004` | Không workaround | Không sửa runtime | Docker daemon probe | `OPEN_G0-12D` |
| `G012A-DESIGN-005` | Ghi rõ single-instance/static-data limitation | Detailed Design | Document review | `ACCEPTED_RESIDUAL_RISK` |
| `G012A-CODE-006` | Reject Jackson/JCS invalid hoặc empty JSON | `AdminReportAiWebhookSigner.java` | TC012 rerun | `FIXED_VERIFIED` |
| `G012A-CODE-007` | Tách security verification failure khỏi JSON parse failure | BE service | Unsigned response test | `FIXED_VERIFIED` |
| `G012A-SEC-008` | Thay ad-hoc canonicalizer bằng JCS implementation, thêm Java/JS number vector | `pom.xml`, signer/test, Node validator | Numeric vectors | `FIXED_VERIFIED` |
| `G012A-SEC-009` | Không chép secret vào docs/commit; cảnh báo rotate | Không sửa secret | `.env` tracked check | `USER_ACTION_REQUIRED` |
| `G012A-ENV-010` | Không tạo/ghi secret giả | Không sửa `.env` | Chỉ kiểm tra boolean config | `BLOCKED_G0-12D` |

## Failed-test repair loop

1. Test mới `TC012` fail vì empty JSON chưa bị reject.
2. Issue `G012A-CODE-006` được ghi trước khi sửa.
3. Guard được sửa để fail closed.
4. Focused test chạy lại và pass.

Không có test expectation nào bị nới lỏng để tạo pass giả.
