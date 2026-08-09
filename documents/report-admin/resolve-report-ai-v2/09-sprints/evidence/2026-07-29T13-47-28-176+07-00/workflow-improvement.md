# Workflow improvement — DOD-FIX-02

## Điều đã hoạt động tốt

- Viết executable test CT-010/SAF-010 trước source fix đã biến nhận xét “snapshot có thể stale”
  thành hai lỗi có thể tái hiện.
- Kiểm tra freshness ở cả hai thời điểm nhạy cảm: trước cache return và sau provider call.
- Semantic fail-closed rõ ràng: stale không bị diễn giải thành REJECT; nó trở thành
  `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- Coverage gate phát hiện hai nhánh compatibility/error chưa được test và buộc bổ sung trước khi
  kết luận.

## Cải tiến nên đưa vào workflow chung

1. Mọi idempotency test phải có cả “same snapshot reuse” và “changed snapshot non-reuse”.
2. Mọi provider workflow dài phải có before/after snapshot check ở owner của persistence.
3. Stale result phải có machine-readable blocked reason thống nhất để FE/audit phân biệt với
   provider failure và evidence insufficiency.
4. DOD-FIX-03 trở đi nên giữ pattern: test đỏ → issue classification → narrow fix → focused →
   coverage → full regression.

## Cải tiến để sau

- Distributed lock/optimistic version hoặc DB unique conflict test cho multi-instance.
- Metric đếm `TARGET_SNAPSHOT_CHANGED_DURING_EVALUATION`.
- UI hiển thị riêng trạng thái stale và nút chạy lại trên snapshot mới.

Các mục này không được tự động triển khai trong DOD-FIX-02.
