# S2-04 — Workflow retrospective

## Điều hoạt động tốt

- Đối chiếu policy với signed runtime catalog trước khi viết prompt đã phát
  hiện `REL.001` semantic mismatch.
- Candidate-only package tránh làm drift S2-03 và tránh publish ngầm.
- Clause code allowlist giữ prompt text bất biến và làm manifest có ý nghĩa.
- Injection invariance kiểm tra trực tiếp điều quan trọng hơn snapshot string
  đơn lẻ: untrusted mutation không đổi system prompt.

## Điều cần cải tiến

1. Tách rule semantic generator theo `ruleId`, không chỉ suy từ family; family
   mapping hiện quá thô và đã tạo lỗi REL.001.
2. Tạo một remediation gate riêng cho mismatch giữa dossier và Runtime Rule
   Context trước S2-05.
3. Khi candidate được approve, bổ sung build step sinh/import Code Node từ
   reviewed source thay vì copy thủ công.
4. Trước provider benchmark, pin model ID, request shape, token/cost capture và
   sanitized result store.
5. Muốn có changed-file coverage trực tiếp cho assembler, nên tách pure
   assembly core thành module testable và tạo n8n wrapper/generated artifact;
   phải giữ parity hash để không hình thành hai implementation.
6. Không mở rộng sang SPAM/HAR.002 cho đến khi pattern/history evidence adapter
   tồn tại.
