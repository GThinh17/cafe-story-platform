# Issue register — DOD-FIX-01

## FIX01-TEST-001

- Classification: `TEST_BUG`
- Evidence: baseline command `node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs`
- Affected behavior: ADV-005 dừng ở assertion kiểm tra payload đã đi vào vùng user-data.
- Actual: JSON serializer escape dấu nháy trong payload, trong khi assertion tìm nguyên văn chuỗi chưa encode.
- Likely owner: test.
- Proposed action: so sánh với `JSON.stringify(payload).slice(1, -1)` để kiểm tra đúng representation trong JSON packet.
- Auto-fix allowed: yes.
- Status: `FIXED`.

## FIX01-CODE-001

- Classification: `CODE_BUG`
- Evidence: lần chạy baseline thứ hai dừng tại `ADV-010: attack not clamped`; actual `RESOLVE`, expected `NEEDS_MANUAL_REVIEW`.
- Affected behavior: `EV-DERIVED-MODERATION` có ID hợp lệ nên có thể bị dùng làm bằng chứng dương duy nhất để đề xuất `RESOLVE + HIDE`, dù sourceType là `DERIVED_SIGNAL`.
- Likely owner: n8n normalizer và Backend semantic boundary.
- Proposed action: nếu finding `SUPPORTED` không có ít nhất một evidence dương độc lập, khả dụng, không phải `REPORTER_CLAIM`/`DERIVED_SIGNAL`, clamp về manual với blocked reason rõ ràng.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.

## FIX01-CODE-002

- Classification: `CODE_BUG`
- Evidence: source review của node `Validate And Normalize Recommendation V2` và `AdminReportAiSemanticValidator` cho thấy chỉ kiểm tra `evidenceIds`/`counterEvidenceIds`; bỏ sót `missingEvidenceIds` và các reference trong `evidenceSummary`.
- Affected behavior: ADV-012 có thể đưa Evidence ID không được Backend cấp vào các trường bị bỏ sót mà không bị semantic fallback.
- Likely owner: n8n normalizer và Backend semantic boundary.
- Proposed action: kiểm tra allowlist cho toàn bộ evidence-reference fields trong findings và evidenceSummary.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.

## Residual / ngoài phạm vi

- Published n8n runtime chưa được cập nhật; source export mới chỉ được kiểm chứng bằng
  executable harness. Classification: `CONFIG_ENV`, auto-fix: no trong gate này.
- Full Backend suite có `1 skipped` là PostgreSQL integration test cần môi trường riêng;
  focused behavior của FIX-01 không phụ thuộc test đó.
- `DOD-FIX-02`–`DOD-FIX-06` chưa triển khai.

## FIX01-TEST-002

- Classification: `TEST_BUG`
- Evidence: lệnh đọc `target/site/jacoco/jacoco.xml` chạy từ repo root trả `path does not exist`.
- Affected behavior: chưa lấy được số coverage; không phải coverage fail.
- Likely owner: validation command.
- Proposed action: đọc XML dưới module `1-cafe-story-backend-javaspring/target/site/jacoco/`.
- Auto-fix allowed: yes.
- Status: `FIXED`.
