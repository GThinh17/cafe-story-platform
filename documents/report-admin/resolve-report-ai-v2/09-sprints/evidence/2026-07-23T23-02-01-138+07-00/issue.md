# Issue Register — G0-11

| ID | Classification | Finding | Resolution |
|---|---|---|---|
| `G011-DOC-001` | `DOC_GAP` | Main DD đã chi tiết nhưng 04/05/06 còn skeleton, có nguy cơ lặp lại hồ sơ chỉ có khung | Viết đầy đủ 25/25 domain/evidence/AI contract docs |
| `G011-PLAN-001` | `SCOPE_REBASE` | Sprint 2/3 cũ overlap safety contract, prompt/schema, audit/security/UI của Sprint 1 | Ghi `DEFERRED_REBASE_REQUIRED` và remaining scope |
| `G011-TEST-001` | `TEST_BUG` | Validator đếm mọi token `SAF-`, gồm cả câu tham chiếu ngoài table, nên báo 17 thay vì 15 case | Đổi sang regex chỉ đếm dòng table ID; xác nhận 15/15 |

## Residual risks

| ID | Classification | Route |
|---|---|---|
| `G011-RUN-001` | `RUNTIME_NOT_VERIFIED` | Sau implementation/G0-12 |
| `G011-ENV-001` | `ENVIRONMENT_ASSUMPTION` | Verify n8n crypto/single-instance nonce storage |
| `G011-DATA-001` | `TEST_DATA` | Safe BLOG/COMMENT fixtures trước full E2E |
| `G011-LEGAL-001` | `LEGAL_PRIVACY_REVIEW` | Retention/jurisdiction before activation |

Không sửa source hoặc hạ acceptance criteria để che residual risk.
