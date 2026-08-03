# Detailed Design — G0-12-DONE-FIX-06

## 1. Mục tiêu và phạm vi

FIX-06 đóng `DOD-19 / SAF-012 / E2E-S1-10`: report terminal không được Ask AI ở FE và Backend
phải tiếp tục reject nếu client cố bypass.

Trong phạm vi: Playwright FE assertion, Backend bypass probe, state/evidence/cleanup. Ngoài phạm vi:
đổi danh sách terminal state, đổi eligibility policy hoặc deploy production.

## 2. FE guard

`canRequestAi(report)` chỉ trả `true` cho `OPEN` hoặc `REVIEWING`. Nút Ask AI ở report detail dùng
cùng predicate để set `disabled`.

Executable assertion:

1. tạo report synthetic;
2. chuyển report thành `RESOLVED`;
3. mở detail;
4. assert Ask AI disabled;
5. gọi DOM click trên disabled button;
6. assert số POST request tới AI endpoint bằng `0`.

FIX-06 không đổi production component vì baseline chứng minh guard đã hoạt động đúng.

## 3. Backend defense in depth

Test gọi trực tiếp:

```text
POST /api/admin/reports/{terminalReportId}/ai-resolution
```

Backend trả HTTP `409` với thông báo chỉ report `OPEN/REVIEWING` mới được request AI. Provider không
được gọi, resolution không được persist.

## 4. Safety assertions

- report status sau probe: `RESOLVED`;
- canonical persisted report state trước/sau bằng nhau (`reportMutation=false`);
- resolution count `0 -> 0`;
- target snapshot không đổi;
- auto-apply job `0`;
- cleanup report/BLOG `0/0`.

## 5. Verification

- Playwright `E2E-S1-10`: `1/1 PASS`;
- screenshot hiển thị `Resolved` và Ask AI disabled;
- report state được GET lại sau status PATCH để lấy persisted baseline đã qua PostgreSQL microsecond
  normalization; raw evidence lưu canonical before/after;
- Admin typecheck/build: `PASS`;
- full Backend: `625`, failure/error `0`, skipped `1`;
- production changed-file coverage: `N/A` vì không thay production file.

## 6. Trạng thái

- Package: `COMPLETED_VERIFIED_AWAITING_APPROVAL`.
- Approval cần nhận: `APPROVE_G0-12-DONE-FIX-06`.
- Production deployment: `NOT_AUTHORIZED`.
