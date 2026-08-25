# Workflow improvement — DOD-FIX-01

1. Prompt policy phải có semantic enforcement kép ở n8n và Backend; chỉ viết trong
   system prompt là chưa đủ.
2. Test fixture nên giữ oracle/blocked reason cạnh payload để traceability không phụ
   thuộc diễn giải thủ công.
3. Các reference collection mới phải được kiểm tra tập trung; tránh chỉ validate
   `evidenceIds` rồi bỏ sót `missingEvidenceIds`/summary.
4. Deterministic harness nên chạy trước provider E2E để bắt regression nhanh, không tốn
   quota và không cần secret thật.
5. Gate publish runtime phải tách khỏi gate source test để không vô tình mở rộng quyền
   triển khai.

