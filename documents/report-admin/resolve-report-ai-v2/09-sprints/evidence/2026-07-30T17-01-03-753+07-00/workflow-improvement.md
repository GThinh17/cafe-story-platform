# S2-DONE-FIX-02-DB-HASH-LENGTH — Workflow improvement

- Schema contract test phải kiểm tra đồng thời canonical value, entity annotation
  và migration SQL; chỉ unit-test service là chưa đủ.
- Migration gate phải có PostgreSQL `information_schema` probe và JPA `validate`,
  không suy ra từ H2.
- E2E persistence nên chạy trước done-audit để phát hiện mismatch ở physical DB
  boundary sớm hơn.
- Trên PowerShell/runner, tách container start và readiness probes để command dễ
  chẩn đoán, không bị lỗi quoting/policy.
- Local integration profile nên tắt rõ background cache worker hoặc cung cấp
  Redis test riêng để giảm warning không liên quan.
- E2E Admin AI nên có profile tắt Report Moderation worker; đồng thời workflow
  Report Moderation cần gate riêng cho lazy entity collection khi tạo webhook DTO.
