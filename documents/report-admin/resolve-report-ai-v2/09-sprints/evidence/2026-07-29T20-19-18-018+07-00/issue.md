# Issue register — S2-REBASE-01

| ID | Phân loại | Mức | Kết luận |
|---|---|---|---|
| `S2RB-001` | DESIGN_RUNTIME_GAP | P0 | Rule burden chưa materialize ở runtime; route `S2-01` |
| `S2RB-002` | DESIGN_RUNTIME_GAP | P1 | Prompt generic; route `S2-04` sau contract/validator |
| `S2RB-003` | CONTRACT_HARDENING | P0 | Schema chưa bounded đầy đủ; route `S2-02` |
| `S2RB-004` | SEMANTIC_HARDENING | P0 | Validator chưa enforce toàn bộ per-rule burden; route `S2-02` |
| `S2RB-005` | EVIDENCE_GAP | P0 | Chưa có model-quality dataset; route `S2-03` |
| `S2RB-006` | PREREQUISITE_MISSING | P2 | Benchmark provider chưa measurable; `S2-05 CONDITIONAL` |
| `S2RB-007` | OUT_OF_SCOPE_DEPENDENCY | Deferred | Authoritative adapters cần workstream riêng |
| `S2RB-008` | RESEARCH_NOT_AUTHORITY | Deferred | Calibration chưa có dataset/approval |
| `S2RB-009` | OPERATIONS_OUT_OF_SCOPE | Open elsewhere | Production readiness còn 5 mục |

Không issue nào cần sửa code trong token rebase. Một lệnh đọc DTO dùng tên file giả định không tồn tại;
đây là lỗi đường dẫn khi khảo sát, không phải lỗi source. Discovery sau đó xác nhận request dùng
`AdminReportAiResolutionRequestDTO`.
