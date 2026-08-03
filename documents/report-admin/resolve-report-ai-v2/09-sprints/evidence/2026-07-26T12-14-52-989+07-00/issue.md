# Issue Register — G0-12A

| ID | Phân loại | Mức độ | Trạng thái ban đầu | Bằng chứng trước sửa | Hướng xử lý |
|---|---|---|---|---|---|
| `G012A-SEC-001` | `CODE_BUG` | `CRITICAL` | `OPEN` | `AdminReportAiResolutionServiceImpl.callWebhook()` gửi JSON trực tiếp, không có `X-CafeStory-Timestamp`, nonce, body hash hoặc HMAC | Thêm signer phía Backend và fail closed khi thiếu secret/chữ ký response |
| `G012A-SEC-002` | `CODE_BUG` | `CRITICAL` | `OPEN` | Workflow export nhận request theo schema nhưng chưa xác thực integrity/freshness/replay trước node OpenAI | Verify HMAC + timestamp ±120 giây + nonce TTL 5 phút trước provider |
| `G012A-SEC-003` | `CODE_BUG` | `CRITICAL` | `OPEN` | Workflow trả JSON không ký; Backend tin response nếu parse/semantic validation pass | Ký response và bắt Backend xác thực contract/correlation/body/signature/freshness/nonce |
| `G012A-ENV-004` | `ENVIRONMENT` | `HIGH` | `OPEN` | Docker Desktop chưa chạy; không thể import/activate và probe exact published webhook trong lượt G0-12A | Giữ workflow `active=false`; chuyển runtime verification sang `G0-12D` |
| `G012A-DESIGN-005` | `DESIGN_LIMITATION` | `HIGH` | `OPEN` | n8n workflow static data chỉ phù hợp single-instance, lưu khi production execution thành công và có thể không tin cậy ở tần suất cao | Khóa rõ single-instance boundary; defer distributed nonce store tới hardening/scale gate |
| `G012A-CODE-006` | `CODE_BUG` | `MEDIUM` | `TEST_DISCOVERED` | `AdminReportAiWebhookSignerTest.TC012` fail: Jackson trả `MissingNode` cho chuỗi JSON rỗng nên guard `source == null` chưa fail-closed | Reject cả `null` và `MissingNode`, chạy lại focused test |
| `G012A-CODE-007` | `CODE_BUG` | `MEDIUM` | `REVIEW_DISCOVERED` | Security verification exception đang đi chung catch với JSON parse và bị báo sai là “response is not valid JSON” | Tách security failure khỏi parse failure, vẫn trả fail-closed `502` |
| `G012A-SEC-008` | `CODE_BUG` | `HIGH` | `REVIEW_DISCOVERED` | Canonicalizer sort-key tự viết của Java không bảo đảm ECMAScript number serialization giống n8n (`1.0`, exponent), có thể false-reject chữ ký hợp lệ | Dùng implementation JCS RFC 8785 phía Java và thêm numeric interoperability test |
| `G012A-SEC-009` | `SECRET_EXPOSURE` | `CRITICAL` | `USER_ACTION_REQUIRED` | `docker/.env` có provider key thật và key đã xuất hiện trong output kiểm tra cục bộ; file được Git ignore và không được stage | Người dùng thu hồi/rotate key trước G0-12D; tuyệt đối không chép key vào evidence/commit |
| `G012A-ENV-010` | `ENVIRONMENT` | `HIGH` | `BLOCKED_RUNTIME` | Kiểm tra tên biến cho thấy `ADMIN_REPORT_AI_HMAC_SECRET_CONFIGURED=false` | Tạo secret ngẫu nhiên đủ mạnh, cấu hình cùng một giá trị cho Backend và n8n trước runtime test; không lưu vào Git |

## Quy tắc xử lý

- Ba lỗi `SEC-001`–`SEC-003` nằm trong scope G0-12A và được phép sửa.
- `ENV-004` không được che bằng source workaround hoặc tuyên bố runtime pass.
- `DESIGN-005` không được mô tả là distributed replay protection; phải giữ residual risk rõ trong handoff.
- Không ghi secret thật vào source, test output hoặc evidence.
