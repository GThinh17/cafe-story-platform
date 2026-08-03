# Workflow Improvement — G0-08

1. Duy trì một machine-readable registry trong implementation phase để Backend, prompt và test
   sinh từ cùng source of truth thay vì copy Markdown thủ công.
2. Validator nên kiểm tra cả Rule ID uniqueness, definition coverage, reason coverage và version pin.
3. G0-09 nên tạo traceability từ mỗi rule tới policy clauses, contract fields và test cases.
4. Trước activation phải có evaluation fixture đại diện đủ rule/reason/target; runtime DB hiện tại
   chỉ dùng 2/22 reasons nên không đủ calibration.
5. Broad reason nên được route nhiều candidate rules, không ép thành một free-form label của model.
