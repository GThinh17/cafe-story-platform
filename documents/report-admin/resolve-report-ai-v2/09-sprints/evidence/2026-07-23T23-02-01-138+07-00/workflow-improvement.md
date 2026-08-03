# Workflow Improvement — G0-11

- Sau khi viết main design, luôn scan toàn cây để phát hiện file khung còn lại.
- Detailed Design phải có cả runtime sequence và implementation order; không trộn hai thứ.
- Mỗi rule/decision cần đường xuống DTO/service/UI/test, không chỉ một câu policy.
- Khi Sprint mới hấp thụ scope cũ, phải rebase các sprint sau thay vì để kế hoạch overlap.
- G0-12 nên review theo bốn nhóm ngắn: scope, safety invariants, file plan, verification gate.
