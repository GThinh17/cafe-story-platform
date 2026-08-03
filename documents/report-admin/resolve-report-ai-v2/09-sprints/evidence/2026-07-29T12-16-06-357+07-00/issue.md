# Issue register G0-12-DONE Audit

| ID | Mức | Phân loại | Mô tả | Trạng thái |
|---|---|---|---|---|
| `G012DONE-TEST-001` | P0 | TEST_GAP | ADV-001–ADV-012 chỉ là designed matrix, chưa executable | `OPEN` |
| `G012DONE-TEST-002` | P0 | TEST_GAP | Chưa có changed-snapshot stale non-reuse test | `OPEN` |
| `G012DONE-E2E-003` | P0 | TEST_DATA/E2E | Chưa có COMMENT missing-critical-context full E2E | `OPEN` |
| `G012DONE-E2E-004` | P0 | E2E | Chưa chạy provider-unavailable full flow | `OPEN` |
| `G012DONE-E2E-005` | P0 | E2E | Bulk chưa inject partial failure để chứng minh success/no-mutation | `OPEN` |
| `G012DONE-FE-006` | P1 | TEST_GAP | Terminal report mới có BE guard, thiếu FE assertion | `OPEN` |
| `G012DONE-OPS-007` | P0 production | OPERATIONS | Production checklist còn 5/14 open | `OPEN_PRODUCTION_GATE` |
| `G012DONE-N8N-008` | P1 | RESIDUAL | Negative n8n execution trả HTTP 200 body rỗng | `DEFERRED_HARDENING` |
| `G012DONE-SEC-009` | P1 | RESIDUAL | Replay store chưa chứng minh multi-instance | `DEFERRED_HARDENING` |

