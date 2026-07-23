# Nhật ký vấn đề hiệu năng Feed EXPERT_V1

## PERFORMANCE-002 — Request Home chờ rebuild đồng bộ gần 60 giây

- Phân loại: `PERFORMANCE`.
- Tài khoản kiểm thử: đã xác thực; email, cookie, token và mật khẩu không được lưu trong evidence.
- Hiện tượng người dùng: `GET / 200 in 62s`, trong đó `application-code: 60s`.
- Phép đo tái hiện: `POST /api/blogs/feed/rebuild?windowType=HOUR_24` trả `200` sau `58.202 ms`, xử lý `434` bài.
- So sánh ban đầu: `/api/feed?size=20` sau khi score/cache đã có trả trong khoảng `1.759–2.662 ms`; authenticated Home warm trả khoảng `2.932 ms`.
- Nguyên nhân đã xác nhận ở cấp luồng: `getPersonalizedFeed()` gọi `rebuildRecommendationCacheLocked()` ngay trong request khi chưa có score hoặc formula version stale. Home phải chờ hoàn tất rebuild trước khi render.
- Điểm nghẽn có bằng chứng mã nguồn: `upsertRecommendationScores()` duyệt `scores.forEach(...)` và gọi một native upsert cho từng score. Lần đo này có `434` score, tức tối thiểu `434` lệnh upsert tuần tự trong cùng rebuild. Tổng thời gian tương đương khoảng `134 ms/score`, bao gồm tính toán và round-trip DB.
- Giới hạn kết luận: chưa có SQL trace chia thời gian theo từng phase, nên không khẳng định toàn bộ `58.202 ms` đều nằm ở upsert; tuy nhiên vòng ghi tuần tự là write N+1 rõ ràng và là nghi phạm chi phối.
- Ảnh hưởng: cold request sau migration/version change có thể mất gần một phút; Swagger gọi ở warm path không phản ánh vấn đề này.
- Chủ sở hữu khả dĩ: kiến trúc/performance của backend ranking.
- Đề xuất: benchmark warm path đầy đủ; sau đó tách rebuild khỏi read request hoặc giới hạn candidate/persist theo batch trong một thay đổi được phê duyệt riêng.
- Auto-fix: **không**. Theo workflow, issue `PERFORMANCE` không được tự ý tái kiến trúc khi chưa có phê duyệt.

## PERFORMANCE-003 — Warm Feed vẫn vượt ngưỡng 500 ms

- Phân loại: `PERFORMANCE`.
- `/api/blogs/feed`, 2 warm-up + 50 request tuần tự: min `1.549,21 ms`, avg `1.772,52 ms`, p50 `1.674,64 ms`, p95 `2.684,11 ms`, max `3.067,51 ms`, error rate `0%`.
- Một warm request kiểm tra riêng mất `2.020 ms`; Redis tăng `1` cache hit và `0` cache miss. Đây là cache-hit path, không phải rebuild lặp lại.
- Redis lúc kiểm tra có `3` key; payload string lớn nhất khoảng `27.736` byte. Chưa đủ bằng chứng để quy toàn bộ 2 giây cho payload; cần profiler runtime.
- `/api/feed`, 2 warm-up + 10 request tuần tự: min `1.960,06 ms`, avg `2.126,27 ms`, p50 `2.063,79 ms`, p95 `2.486,46 ms`, max `2.486,46 ms`, error rate `0%`.
- Ngưỡng kế hoạch: p95 `≤500 ms`; kết luận: **không đạt**.
- Auto-fix: không; cần SQL/query profiler trước khi tối ưu thêm.

## PERFORMANCE-004 — Warm Next Home vượt ngưỡng 3 giây ở p95

- Phân loại: `PERFORMANCE`.
- 2 warm-up + 10 request SSR authenticated: min `2.914,93 ms`, avg `3.097,63 ms`, p50 `3.028,32 ms`, p95 `3.689,78 ms`, max `3.689,78 ms`, error rate `0%`.
- Ngưỡng card đầu tiên p95 `≤3.000 ms`; phép đo này là HTML SSR response, chưa phải browser first-card-render, nhưng bản thân response đã vượt ngưỡng.
- Home chạy song song mixed Feed, top cafes, Story Rail và Message Dock; với tài khoản test, following/cafe/chat đều rỗng nên không có per-item enrichment đáng kể.
- Auto-fix: không; cần tách critical data khỏi ancillary server fetch hoặc streaming sau khi được duyệt.

## TESTDATA-002 — Identifier ban đầu bị sai chính tả domain

- Phân loại: `TEST_DATA`.
- `gmial.com` trả `401`; địa chỉ `gmail.com` được người dùng xác nhận lại và đăng nhập thành công.
- Auto-fix source: không.
