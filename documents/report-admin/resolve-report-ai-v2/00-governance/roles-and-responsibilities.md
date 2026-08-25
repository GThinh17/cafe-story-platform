# Roles and Responsibilities

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-10A` |
| Trạng thái | `APPROVED_DECISION` |
| Recommended model | `A1_ROLE_BASED_OWNERSHIP` |
| Approval chat | `APPROVE_G0-10A` |

| Role | Responsible for | Không có quyền mặc định |
|---|---|---|
| Product/Policy Owner | Policy semantics, taxonomy, rule lifecycle | Production activation, source mutation |
| Business Approver | Risk acceptance, action authority, automation scope | Bypass evidence/security/revalidation |
| Engineering Owner | Contract, implementation, migration design, tests | Tự phê duyệt business semantics |
| Security/Privacy Reviewer | HMAC/replay, PII, retention, high-impact review | Chọn content decision thay Admin |
| Operations/Release Operator | Activation, monitoring, rollback execution | Sửa artifact đã ký/checksum |
| Admin Moderator | Review evidence, final decision/override trong authority | Bypass policy-invalid action |
| AI/n8n/model | Recommendation và orchestration | Business approval hoặc mutation authority |

Một người có thể kiêm role trong đồ án, nhưng mỗi audit action phải ghi role đang thực hiện.
Tên người cụ thể được phép để `TBD_BEFORE_ACTIVATION`; quorum chuyển G0-10C.
