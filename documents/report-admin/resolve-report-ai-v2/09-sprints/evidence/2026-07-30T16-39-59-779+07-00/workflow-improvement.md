# S2-DONE-FIX-01 — Workflow improvement

- Security fixture nên dùng shared canonical request builder để tránh contract drift.
- Evaluation harness không nên khóa exact số test khi package chỉ yêu cầu suite
  không regression.
- Coverage parser PowerShell nên gom rows trước khi pipe format.
- Giữ một remediation package và một functional verification theo Lean Roadmap.
- Contract test phải kiểm tra cả Java/domain format và physical DB column length;
  unit test hiện đã khóa hash 71 ký tự nhưng chưa phát hiện schema chỉ có 64.
- Happy-path runtime test cần chạy trước khi tuyên bố sprint done; lỗi này chỉ xuất
  hiện tại persistence boundary, không xuất hiện trong repository mock.
