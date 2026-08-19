# Đánh giá AI Report response language routing

## Kết quả

- AI Report Resolution suy luận ngôn ngữ từ `reportClaim.description` trước, sau đó mới dùng `reasonLabel`, cuối cùng fallback English.
- `explanation` và mọi `finding.rationale` được yêu cầu trả về bằng English hoặc Vietnamese theo kết quả suy luận.
- Enum, label code, Rule ID, Evidence ID, reason code và technical identifier không bị dịch.
- Request contract giữa Backend và n8n không thay đổi; report claim vẫn là dữ liệu không đáng tin và không được đưa nguyên văn vào provider input.

## Kiểm chứng

| Lệnh | Kỳ vọng | Thực tế | Trạng thái |
| --- | --- | --- | --- |
| `node docker/tests/validate-admin-report-ai-s2-contracts.mjs` | Schema, parity và EN/VI routing pass | 7/7 boundary; EN, VI và description precedence đều pass | PASS |
| `node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs` | Không nới authority/prompt boundary | 12/12 pass; provider không được gọi | PASS |
| `node docker/tests/validate-admin-report-ai-security.mjs` | HMAC, replay, tamper và response signing pass | Tất cả assertion pass; workflow hiện inactive | PASS |
| `node docker/tests/validate-admin-report-ai-runtime.mjs` | Gọi được n8n local | `ECONNREFUSED 127.0.0.1:5678` | NOT VERIFIED |
| `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs` | Broad offline gate pass | Dừng do checksum fixture đã drift ngoài phạm vi | NOT VERIFIED |
| `python .agents/skills/cafestory-engineering-workflows/scripts/check_project.py .` | Project structure hợp lệ | Warnings rỗng | PASS |
| `node docker/tests/sync-admin-report-ai-workflow-code.mjs` | Canonical workflow đồng bộ source Code node | `ADMIN_REPORT_AI_WORKFLOW_CODE_SYNC=PASS` | PASS |
| `git diff --check -- docker` | Không whitespace error | Không có lỗi; chỉ cảnh báo CRLF | PASS |

## Coverage và rủi ro còn lại

- Repository chưa có coverage instrumentation cho JavaScript chạy trong n8n Code node, nên chưa chứng minh được gate 100% line / 85% branch cho file production thay đổi.
- Chưa gọi provider OpenAI thật và chưa kiểm chứng nội dung tiếng Anh/Việt qua workflow runtime vì n8n local không chạy.
- Logic chỉ dịch phần diễn giải tự do. Dữ liệu audit và mã kỹ thuật vẫn giữ nguyên theo yêu cầu an toàn.

## Cleanup

- Không tạo report, moderation action, AI resolution hoặc test data trong backend/database.
- Không có secret, token hoặc raw credential trong evidence này.
