# Safety, Violence and Self-harm Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Family | `PF-SAFETY` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.SAF.001` — Credible violence threat

- **Criteria:** target truyền đạt ý định, chỉ dẫn hoặc lời đe dọa gây tổn hại thể chất
  đủ cụ thể để cần đánh giá nguy cơ; câu trích dẫn và đối tượng bị đe dọa phải xác định được.
- **Required evidence:** snapshot chứa phát ngôn/hình ảnh; context đủ để phân biệt đe dọa
  với trích dẫn, tường thuật, châm biếm hoặc cảnh báo; provenance và timestamp.
- **Counter-evidence/exceptions:** tin tức, giáo dục, phản bác, hư cấu hoặc self-defense
  discussion không cổ vũ gây hại.
- **Critical missing:** không đọc được nội dung; thiếu context thay đổi nghĩa; chỉ có reporter claim.
- **Candidate action:** BLOG/COMMENT có thể `HIDE` hoặc `REMOVE`; actor-level action chỉ là
  candidate cho human review khi có evidence độc lập về actor.

## `CSR.SAF.002` — Violent or exploitative content

- **Criteria:** nội dung cổ vũ, ca ngợi, hướng dẫn hoặc khai thác bạo lực/tổn hại thể chất;
  không bao gồm chỉ sự hiện diện của từ khóa bạo lực.
- **Required evidence:** observation cụ thể từ content/media và context trình bày.
- **Counter-evidence/exceptions:** tài liệu, báo chí, giáo dục, tưởng niệm hoặc phản đối bạo lực.
- **Critical missing:** media là evidence trọng yếu nhưng pipeline chưa đọc/kiểm chứng được media.
- **Candidate action:** content-level action; mọi high-impact action cần human.

## `CSR.SAF.003` — Self-harm encouragement or facilitation

- **Criteria:** khuyến khích, hướng dẫn, ép buộc hoặc cổ vũ self-harm/abnormal eating;
  nội dung hỗ trợ hồi phục không phải vi phạm theo rule này.
- **Required evidence:** observation trực tiếp và context xác định hướng tác động của nội dung.
- **Counter-evidence/exceptions:** tìm kiếm trợ giúp, chia sẻ phục hồi, prevention hoặc clinical education.
- **Critical missing:** không phân biệt được hỗ trợ với cổ vũ; thiếu surrounding context.
- **Candidate action:** content-level action và urgent human review; catalog không tự cấp emergency mutation.

Tất cả ba rule có `ruleVersion=1.0.0-proposed.1`, `automationEligibility=A0`,
`effectiveFrom=TBD_ACTIVATION`.
