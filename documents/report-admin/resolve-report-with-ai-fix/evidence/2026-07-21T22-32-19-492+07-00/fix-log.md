# Fix log

## Frontend Admin

- Chỉ cho Ask AI với report `OPEN` hoặc `REVIEWING`.
- Disable checkbox bulk của report terminal; selected/filtered bulk đều lọc report terminal.
- Đổi “AI resolve all” thành “Generate AI recommendations”.
- Hiển thị lỗi Ask AI ngay trong dialog.
- Ghi nhận lỗi bulk theo từng `reportId`, giữ kết quả thành công một phần.
- Cập nhật E2E để preflight n8n thật, không cộng điểm giả và retry registration race.

## Backend

- Đổi URL mặc định từ `/webhook-test` sang `/webhook`.
- Tăng timeout từ 15.000 ms lên 40.000 ms.
- Từ chối Ask AI cho report terminal hoặc target không tồn tại.
- Validate score hữu hạn trong `0..100`, explanation và modelName không rỗng.
- Enforce ma trận decision/action theo target type.
- Revalidate target ID, status và thời điểm cập nhật trước auto-apply.
- Stale/changed/missing target được đánh dấu `SKIPPED` với lý do.
- Bổ sung test cho HTTP client, parse lỗi, semantic matrix, stale target, scheduling và action.

## n8n

- Backup workflow trước khi thay đổi runtime.
- Không xóa workflow cũ/archived.
- Publish workflow V2 `cafestory-admin-report-ai-resolution-v2`.
- Bổ sung retry OpenAI: tối đa 3 lần, chờ 2 giây.
- Chuẩn hóa score dạng `0.85` thành `85`.
- Fallback tổ hợp decision/action sai về manual review + none.

## Tự sửa sau kiểm chứng

- E2E lần đầu sau sửa phát hiện bulk chọn 12 report gồm nhiều report terminal, tạo `Success 3 / Failed 9`.
- Đã sửa selection/filter và chạy E2E lần hai; RAI-25/27 chuyển sang `PASSED`.
- Typecheck chạy trong lúc dev hot-reload phát hiện `.next/dev/types` bị append dở; đã loại đúng artifact generated và chạy typecheck/build tĩnh pass.

## Không tự sửa

- Không tạo COMMENT giả trực tiếp trong database vì thiếu owner/session và có nguy cơ làm bẩn dữ liệu.
- Không thay đổi migration dù DB runtime mới hơn source.
- Không thêm HMAC/idempotency/snapshot schema vì cần thiết kế contract và migration riêng.
