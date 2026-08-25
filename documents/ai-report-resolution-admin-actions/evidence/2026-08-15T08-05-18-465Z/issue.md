# AI Report Resolution issues

## ISSUE-001 — Duplicate Target ID locator

- Classification: `TEST_BUG`
- Symptom: Playwright strict mode thấy Target ID tại nhiều vị trí hợp lệ trong dialog.
- Fix: scope assertion vào exact first match; áp dụng tương tự cho technical identifiers bị lặp.
- Runtime impact: không có.

## ISSUE-002 — Policy API envelope parsing

- Classification: `TEST_BUG`
- Symptom: test đọc `recommendationOnly` ở envelope root thay vì standard API `data`.
- Fix: parse response qua helper `parseResponse<AiPolicy>`.
- Runtime impact: không có; Admin API client đã unwrap đúng.

## ISSUE-003 — Policy Sheet below report dialog overlay

- Classification: `PRODUCT_BUG`
- Symptom: Sheet hiển thị nhưng overlay của report dialog (`z-[70]`) nằm trên Sheet mặc định (`z-50`) và chặn pointer events.
- Fix: thêm `overlayClassName` tùy chọn cho `SheetContent`; riêng AI Policy Sheet dùng overlay `z-[80]`, content `z-[81]`.
- Verification: tab technical details tương tác được bằng focus + Enter trong focused E2E; light/dark screenshots được tạo.

Tất cả target dùng cho E2E được khôi phục trạng thái ban đầu trong `finally`; report test được đóng `REJECTED`.
