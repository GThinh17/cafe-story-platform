# S2-03 — Workflow retrospective

## Điều đã hoạt động

- Chạy baseline trước khi thêm dataset giúp phân biệt regression có sẵn với lỗi
  do S2-03.
- Dataset class tách hard invariant, provisional semantic và disagreement ngăn
  việc tạo ground truth giả.
- Fingerprint làm dependency drift fail rõ ràng.
- Harness tái sử dụng S2-02/M07/ADV làm executable oracle thay vì chỉ ghi mô tả.
- Issue được ghi trước fix và retest đúng phạm vi.

## Điều cần cải tiến ngay

1. Chuẩn hóa command runner cross-platform dùng chung, tránh mỗi harness tự xử
   lý `.cmd`.
2. Cho mọi evidence run sinh machine-readable summary tự động thay vì copy số
   liệu bằng tay.
3. Thêm CI job chạy `--metadata-only` trên mọi thay đổi dataset và full hard
   gate khi contract/catalog/prompt thay đổi.
4. Tạo schema riêng cho `manifest.json` và `rubric.json`; hiện hai file được
   custom-validate trong harness nhưng chưa có JSON Schema độc lập.
5. Quy định review ownership cho provisional/disagreement trước S2-04.

## Deferred

- Provider benchmark, latency/cost và semantic quality thuộc S2-05 conditional.
- Numeric calibration chỉ được mở khi có reviewer-approved ground truth đủ lớn.
- Deep target/media/external evidence adapter không thuộc S2 core hiện tại.
