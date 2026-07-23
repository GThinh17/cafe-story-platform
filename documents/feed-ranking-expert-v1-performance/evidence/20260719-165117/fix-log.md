# Nhật ký sửa lỗi

## FIX-001 — Đồng bộ fixture upsert EXPERT_V1

- Liên kết issue: `TEST-001`.
- Phạm vi: `BlogFeedRankingServiceImplTest`.
- Cách sửa: bổ sung matcher/captor cho đủ component và cột legacy trong chữ ký upsert mới; không thay đổi mã production.
- Retest: đạt; toàn bộ regression backend có `568` test, `0` failure, `0` error, `1` skipped do thiếu PostgreSQL.

## FIX-002 — Sửa kỳ vọng số học scorer

- Liên kết issue: `TEST-002`.
- Phạm vi: `FeedScoreCalculationServiceImplTest`.
- Cách sửa: expected Engagement thành `120/(120+100)`; kiểm tra tổng trọng số với tolerance `1e-12`.
- Retest: đạt trong regression backend.

## FIX-003 — Bổ sung changed-file coverage có giá trị

- Liên kết issue: `COVERAGE-001`.
- Phạm vi: scorer, ranking, sponsored candidate và entity lifecycle.
- Cách sửa: bổ sung test cho công thức, fallback, hard filter, version stale, diversity, batch follow, batch ad-frequency và lifecycle entity; không loại file khỏi JaCoCo.
- Retest: scorer `100%` line / `94,34%` branch; entity `100%` / `94,44%`; sponsored service `99,32%` / `89,74%`; ranking service `87,98%` / `56,15%`.
- Kết luận: hard gate toàn bộ changed-file chưa đạt; giữ trạng thái fail minh bạch, không tiếp tục mở rộng test giả tạo chỉ để đạt phần trăm.

## FIX-004 — Dùng webpack để xác minh production build Web

- Liên kết issue: `WEB-001`.
- Cách xử lý: sau khi Turbopack panic do kết nối nội bộ Windows, chạy `npx next build --webpack`.
- Retest: đạt; compile, TypeScript trong pipeline build và tạo `22` route thành công.
- Ghi chú: đây là đường build production được Next hỗ trợ, không sửa source để che lỗi môi trường Turbopack.

## FIX-005 — Xác minh lại Maven sau lỗi artifact tạm thời

- Liên kết issue: `BUILD-001`.
- Cách xử lý: kiểm tra class production đã được tạo, chạy lại focused tests rồi chạy regression rộng.
- Retest: đạt `568/568` test thực thi không lỗi; test PostgreSQL bị skip có chủ đích.
- Ghi chú: không có thay đổi production cho lỗi artifact tạm thời.
