# Integrity, Authenticity and Misleading Representation Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Family | `PF-INTEGRITY` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.INT.001` — Impersonation

- **Criteria:** target materially represents itself as another identifiable person/entity in a way
  likely to mislead; similarity alone không đủ.
- **Required evidence:** target profile/page/content snapshot, claimed identity, evidence của entity
  được impersonate và material representation.
- **Counter-evidence/exceptions:** parody/fan/commentary được disclosure rõ; shared/similar name.
- **Critical missing:** không xác minh được identity/reference entity hoặc intent/material deception.
- **Candidate action:** content action hoặc actor/page action qua human; không tự suspend.

## `CSR.INT.002` — Materially deceptive representation

- **Criteria:** claim về sự thật có tính material và có evidence đáng tin cho thấy sai/misleading;
  opinion, dự đoán hoặc khác biệt quan điểm không tự là violation.
- **Required evidence:** exact claim, context, nguồn phản chứng có provenance và tính liên quan.
- **Counter-evidence/exceptions:** satire, uncertainty disclosure, correction hoặc dispute chưa đủ fact.
- **Critical missing:** không có nguồn kiểm chứng; chỉ dựa report count/AI output cũ.
- **Candidate action:** manual review; exact misinformation scope cần business policy.

Hai rule dùng version `1.0.0-proposed.1`, automation `A0`, effective date `TBD_ACTIVATION`.
