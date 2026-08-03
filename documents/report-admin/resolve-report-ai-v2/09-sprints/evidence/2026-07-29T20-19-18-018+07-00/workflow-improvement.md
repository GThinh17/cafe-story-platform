# Workflow improvement — S2-REBASE-01

1. Mọi Sprint AI nên kiểm tra runtime rule metadata trước khi tối ưu prompt.
2. Tách security/contract test khỏi model-quality evaluation để không gọi chung là “AI test”.
3. Chỉ mở provider benchmark sau khi dataset, rubric và immutable prompt candidate tồn tại.
4. Mỗi package Sprint 2 cần approval riêng; Detailed Design phải đi trước code.
