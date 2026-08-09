# Workflow Improvement

- Sequential approval phù hợp khi decision còn tranh luận, nhưng tạo session quá dài khi user đã
  chấp nhận toàn bộ hướng khuyến nghị.
- Cho phép explicit batch delegation với ba guard: ghi selected option từng decision, lưu approval
  mode và không mở rộng sang source/activation.
- Lần sau nên trình một bảng 12 decision trước, chỉ tách sub-gate cho mục user phản đối hoặc yêu cầu
  thảo luận sâu.
- G0-11 nên triển khai theo package FE → BE → n8n → verification thay vì tiếp tục chia nhỏ mỗi câu.
