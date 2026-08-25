# Workflow Improvement — Sau G0-06B

## Điều đang hoạt động tốt

- BE giữ quyền validate/persist/apply.
- n8n không trực tiếp mutation CafeStory.
- AI eligibility thống nhất OPEN/REVIEWING.
- Có delayed auto-apply, cancel, active-job uniqueness và stale-target skip.
- Bulk giữ kết quả thành công và báo lỗi theo item.

## Cải tiến theo thứ tự

1. Hoàn thành database read-only audit để biết catalog/data runtime.
2. Chốt policy framework và rule catalog trước khi sửa prompt.
3. Thiết kế canonical evidence/decision contract và version metadata.
4. Chuyển default automation về recommend-only; destructive action luôn Human Review.
5. Tách violation likelihood, harm severity, action risk và assessment confidence.
6. Thêm trust/provenance label cho reporter claim, snapshot và derived AI output.
7. Thiết kế image evidence path hoặc abstain khi ảnh là critical evidence.
8. Thêm webhook authentication, correlation, idempotency và replay protection.
9. Redesign Admin UI theo evidence-first và stage-specific bulk outcome.
10. Chỉ sau đó sửa BE → n8n → FE và chạy focused/unit/E2E/runtime verification.

## Cải tiến quy trình n8n

- Policy/rule không hardcode thành vài câu trong workflow.
- BE cung cấp policy/rule version đã pin; n8n chỉ orchestration.
- Structured output phải chứa evidence references và uncertainty.
- Retry chỉ áp dụng transient provider/network failure.
- Operational failure có error contract riêng, không biến thành content decision.
- Workflow version phải được persist cùng recommendation.
- Static export, imported workflow và published production version phải có checksum/trace.
