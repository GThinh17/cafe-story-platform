# S2-04 — Fix log

| Issue | Fix | Retest | Status |
|---|---|---|---|
| `S2-04-ISSUE-001` | Không prompt-override; loại REL.001 khỏi pilot và dùng HAR.001 cho hai branch | Focused `PASS`; S2-03 regression `PASS` | `OPEN_ROUTED` |
| `S2-04-ISSUE-002` | Dùng marker injection tường minh thay vì ký tự đầu | Mutation `4/4 PASS` | `RESOLVED_VERIFIED` |
| `S2-04-ISSUE-003` | Mutation đúng branch BLOG đang thực thi | Negative guards `5/5 PASS` | `RESOLVED_VERIFIED` |
| `S2-04-ISSUE-004` | Đổi output field về canonical `evidenceIds`; thêm provider-schema parity guard | Focused + parity `PASS` | `RESOLVED_VERIFIED` |

Không sửa Backend production source, S2-03 dataset/manifest hoặc canonical
published workflow trong S2-04.

Chưa có fix tại thời điểm Bound.
