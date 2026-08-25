# Sexual and Sensitive Content Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Family | `PF-SEXUAL` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.SEX.001` — Nudity or explicit sexual activity

- **Criteria:** evidence trực tiếp cho thấy nudity hoặc explicit sexual activity thuộc phạm vi
  policy đã phê duyệt; reason/URL ảnh không đủ để kết luận.
- **Required evidence:** visual/text observation có provenance, media accessibility và context.
- **Counter-evidence/exceptions:** y khoa, giáo dục, nghệ thuật, breastfeeding, documentary hoặc
  context khác nếu policy cho phép.
- **Critical missing:** model không có vision/OCR nhưng image là evidence trọng yếu; age/context
  cần thiết nhưng không xác định được.
- **Candidate action:** content-level human review; high-impact action không tự động.

## `CSR.SEX.002` — Sexualized or sensitive content

- **Criteria:** sexualized solicitation, explicit description hoặc sensitive material thuộc phạm vi
  rule; không dùng cảm giác “không phù hợp” làm tiêu chí.
- **Required evidence:** text/media observation và context intent/audience khi liên quan.
- **Counter-evidence/exceptions:** education, health, support hoặc non-explicit contextual discussion.
- **Critical missing:** thiếu content, audience/context hoặc media unreadable.
- **Candidate action:** content-level candidate; exact age-sensitive policy để G0-10/future policy.

Hai rule dùng version `1.0.0-proposed.1`, automation `A0`, effective date `TBD_ACTIVATION`.
