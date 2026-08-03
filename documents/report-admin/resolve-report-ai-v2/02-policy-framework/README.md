# Policy Framework — Resolve Report with AI V2

## Trạng thái

- Gate: `G0-07`
- Trạng thái: `PROPOSED`
- Draft version: `PF-2.0.0-proposed.1`
- Approval chat: `APPROVE_G0-07` cho phép soạn thảo, không phải business adoption
- Implementation: chưa được phép

## Thứ tự đọc

1. `community-policy-principles.md`
2. `policy-taxonomy.md`
3. `severity-framework.md`
4. `report-decision-framework.md`
5. `enforcement-action-framework.md`
6. `policy-precedence.md`
7. `policy-exceptions.md`
8. `policy-versioning.md`
9. `../00-governance/policy-change-process.md`

## Những gì framework đã đề xuất

- authority boundary AI–n8n–Backend–Admin;
- claim/fact/evidence separation;
- taxonomy layer và policy family;
- score/severity/action-risk semantics;
- decision và action framework;
- `A0 RECOMMEND_ONLY` baseline;
- destructive action bắt buộc Human Review;
- precedence/exception handling;
- version manifest/change process;
- audit, security, privacy và failure separation.

## Những gì framework chưa chốt

- rule criteria chi tiết;
- mapping đầy đủ 22 report reasons;
- numeric thresholds;
- auto-apply allowlist;
- role matrix/two-person approval;
- retention/SLA;
- target-specific deep rules;
- implementation schema/API/UI/n8n.

## Gate conclusion

Framework đủ làm input cho G0-08 Rule Catalog draft.

Nó vẫn ở trạng thái `PROPOSED` và không được dùng làm production policy trước G0-10 approval/activation process.
