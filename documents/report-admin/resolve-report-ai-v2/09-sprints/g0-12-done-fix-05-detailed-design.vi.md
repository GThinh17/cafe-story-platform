# Detailed Design — G0-12-DONE-FIX-05

## 1. Mục tiêu và phạm vi

FIX-05 đóng `DOD-18 / SAF-011 / E2E-S1-09`: bulk recommendation phải cô lập lỗi từng item.
Một item lỗi không được rollback recommendation hợp lệ của item khác, không được biến failure thành
content decision và không được gây AI-triggered mutation.

Trong phạm vi: Playwright full-path fixture, Admin bulk UI assertion, Backend/runtime state probes,
evidence và cleanup. Ngoài phạm vi: đổi concurrency, retry queue, n8n workflow, production deploy.

## 2. Fault-injection design

1. Tạo hai BLOG và hai report synthetic ở trạng thái `OPEN`.
2. Mở bulk dialog, chọn cả hai khi FE còn xem chúng là eligible.
3. Dùng Admin API chuyển một report sang `RESOLVED` sau selection.
4. Bấm Run AI trên stale selection.
5. Backend đọc trạng thái fresh:
   - terminal item trả `409`;
   - eligible item tiếp tục full path qua n8n/OpenAI và trả `200`.

Cách này inject failure theo đúng contract mà không tắt provider hoặc sửa dữ liệu ngoài fixture.

## 3. FE workflow

`handleBulkAskAi()` xử lý tuần tự từng report trong `try/catch` riêng:

- success tăng `recommended` hoặc `manualReview`;
- failure tăng `failed` và lưu `reportId + reason`;
- loop không throw ra ngoài sau per-item failure;
- UI hiển thị Completed/Recommended/Needs manual review/Failed/Remaining/Total;
- failure alert nói rõ successful recommendations được giữ lại.

FIX-05 không đổi production component vì baseline chứng minh behavior đã đúng.

## 4. BE/n8n workflow

- Terminal report bị Backend reject trước provider bằng HTTP `409`.
- Eligible report đi qua Backend → signed n8n/OpenAI → Backend validator/persistence.
- n8n không được sửa/publish trong package này.
- Successful output vẫn là A0 recommendation-only, không tạo auto-apply job.

## 5. Safety assertions

| Item | Resolution | Report | Target | Auto job |
|---|---|---|---|---|
| Eligible success | `0 -> 1` | giữ `OPEN` | không đổi | `0` |
| Injected terminal failure | `0 -> 0` | giữ `RESOLVED` | không đổi | `0` |

UI actual: `Completed 1`, `Needs manual review 1`, `Failed 1`, `Remaining 0`, `Total 2`.

Report no-mutation không so sánh JSON response thô vì DTO còn chứa field trình bày/dẫn xuất của
reporter. Test chiếu response về canonical state do report sở hữu: identity, target references, reason,
description, status, `createdAt` và `resolvedAt`. Với terminal item, test GET lại persisted report sau
PATCH để PostgreSQL đã chuẩn hóa `resolvedAt` về microsecond trước khi chụp baseline. Raw evidence lưu
cả snapshot trước/sau; hai item đều giữ nguyên canonical state.

## 6. Verification

- Playwright `E2E-S1-09`: `1/1 PASS`;
- Admin typecheck/build: `PASS`;
- full Backend: `625`, failure/error `0`, skipped `1`;
- visual QA: counters và per-item failure detail hiển thị;
- canonical report snapshot trước/sau bằng nhau cho cả success và failed item;
- cleanup report/BLOG `0/0`;
- production changed-file coverage: `N/A` vì không thay production file.

## 7. Trạng thái

- Package: `COMPLETED_VERIFIED_AWAITING_APPROVAL`.
- Approval cần nhận: `APPROVE_G0-12-DONE-FIX-05`.
- Production deployment: `NOT_AUTHORIZED`.
