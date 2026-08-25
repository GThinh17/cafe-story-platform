# Fix Log — G0-12B

| Bước | Thay đổi | Kết quả |
|---|---|---|
| 1 | Đo baseline trên 5 class logic Sprint 1 | Chỉ signer đạt gate; 4 class còn lại thiếu coverage |
| 2 | Bổ sung test cho A0 auto-apply: null input, null batch, detached legacy job và mapping nullable | Auto-apply đạt `100% line / 100% branch` |
| 3 | Tạo `AdminReportAiSemanticValidatorTest` cho categorical semantics, evidence reference, version pin và action matrix | Policy catalog đạt `100% / 100%`; semantic validator đạt `100% / 98.75%` |
| 4 | Loại enum case `NEEDS_MANUAL_REVIEW` không thể chạy sau guard cùng giá trị | Không đổi hành vi; decision/action matrix pass |
| 5 | Bổ sung service tests cho webhook failure, COMMENT context, moderation evidence, missing target, legacy/null mapping và serialization failure | Resolution service đạt `100% / 87.85%` |
| 6 | Sửa lỗi compile của assertion AssertJ trong test mới | Focused suite compile và pass |
| 7 | Chạy focused suite rồi full backend regression | `56/56 PASS`; full `617` tests, `0` failure, `0` error, `1` skip |

Không thay đổi controller, repository, migration, n8n workflow hoặc Admin UI trong G0-12B.

