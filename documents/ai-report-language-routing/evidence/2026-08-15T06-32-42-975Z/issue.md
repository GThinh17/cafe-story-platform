# Issues

## AIRL-001

- Classification: `CONFIG_ENV`
- Evidence: `node docker/tests/validate-admin-report-ai-runtime.mjs`
- Affected behavior: Chưa kiểm chứng runtime n8n/OpenAI thật cho response English/Vietnamese.
- Likely owner: local n8n runtime/configuration.
- Observed: `connect ECONNREFUSED 127.0.0.1:5678`.
- Proposed action: Khởi động/import/publish workflow n8n với credential test phù hợp rồi chạy lại runtime suite.
- Auto-fix allowed: no.
## AIRL-002

- Classification: `TEST_BUG`
- Evidence: `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs`
- Affected behavior: Broad evaluation gate dừng trước khi chạy hard gates.
- Likely owner: evaluation fixture manifest.
- Observed: `dataset.schema.json` SHA-256 actual `de055b8c...e806b`, expected `b62084d...29cd`.
- Proposed action: Rà soát thay đổi fixture rồi cập nhật manifest trong một task riêng nếu checksum mới là hợp lệ.
- Auto-fix allowed: no, vì file fixture/manifest không thuộc thay đổi hiện tại và lỗi đã tồn tại ngoài phạm vi.

## AIRL-003

- Classification: `CONFIG_ENV`
- Evidence: Không có coverage command/instrumentation cho n8n Code node JavaScript.
- Affected behavior: Chưa đo được 100% line và 85% branch coverage cho production file thay đổi.
- Likely owner: test tooling.
- Proposed action: Tách language resolver thành module thuần có coverage harness trong một task kiểm thử riêng.
- Auto-fix allowed: no.
