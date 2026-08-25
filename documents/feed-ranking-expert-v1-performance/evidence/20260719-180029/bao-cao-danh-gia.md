# Báo cáo sửa và kiểm thử cold rebuild Feed

## Kết luận

Bản sửa đã loại bỏ việc `GET` Feed chờ rebuild đồng bộ khoảng 60 giây. Rebuild được chuyển sang nền theo mô hình stale-while-revalidate, ghi score dùng JDBC batch, và personalized Feed cache đã đọc/ghi ổn định. Trạng thái tổng thể là `PARTIAL_PASS`: lỗi 62 giây đã sửa, personalized Feed và Web Home HTTP đạt ngưỡng; Mixed Feed còn cao hơn ngưỡng 500 ms và chưa có số đo màn hình Mobile trên thiết bị Android thật.

Các phép đo chạy local/LAN ngày 19/07/2026, backend kết nối PostgreSQL Supabase từ xa. Benchmark kiểm soát tắt scheduled jobs bằng cấu hình runtime `app.scheduling.enabled=false`; hành vi mặc định của ứng dụng không bị thay đổi. Không lưu credential, token, cookie hoặc PII.

## Bảng kết quả

| Tầng đo | Metric | Số lượt | Min (ms) | Avg (ms) | p50 (ms) | p75 (ms) | p95 (ms) | Max (ms) | Ngưỡng | Kết luận |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Rebuild trước sửa | `POST /api/blogs/feed/rebuild`, 434 bài | 1 | 58,202.00 | 58,202.00 | 58,202.00 | 58,202.00 | 58,202.00 | 58,202.00 | Không chặn GET | Fail |
| Rebuild sau batch | `POST /api/blogs/feed/rebuild`, 434 bài | 1 | 5,635.75 | 5,635.75 | 5,635.75 | 5,635.75 | 5,635.75 | 5,635.75 | Tác vụ quản trị/nền | Cải thiện 90.32% |
| Cold/stale GET | `GET /api/blogs/feed`, DAY_7 | 1 | 2,516.87 | 2,516.87 | 2,516.87 | 2,516.87 | 2,516.87 | 2,516.87 | Không chờ rebuild 5.6 s | Pass hành vi, chưa đạt latency |
| Personalized Feed warm | `GET /api/blogs/feed`, size 20 | 50 | 234.77 | 244.58 | 241.75 | 245.92 | 267.95 | 315.46 | p95 ≤ 500 ms, lỗi 0% | Pass |
| Mixed Feed thật | `GET /api/feed`, size 20 | 10 | 467.26 | 554.14 | 586.91 | 589.40 | 590.88 | 590.88 | p95 ≤ 500 ms, lỗi 0% | Fail nhẹ 90.88 ms |
| Web Home HTTP | `GET http://localhost:3000/` có đăng nhập | 10 | 121.22 | 141.19 | 133.97 | 139.40 | 224.36 | 224.36 | Không còn log 62 s | Pass |
| Mobile Home Android | API/render/media trên thiết bị thật | 0 | — | — | — | — | — | — | 10 lượt sau 2 warm-up | Chưa đo: không có device session |

Web Home ở đây là thời gian phản hồi HTTP của Next dev server, không phải FCP/LCP. Vì chưa điều khiển trình duyệt và chưa có Android device trong session, không được trình bày số này như LCP hoặc SLA production.

## Query count và cache

- Personalized size 20 khi payload cache cũ/không đọc được: `10` SQL statements.
- Personalized size 20 sau cache fix: `1` SQL statement cho mỗi cache-hit.
- Số query không tăng theo từng Feed item ở mẫu runtime; các truy vấn follow đã được batch-load.
- Integration tự động size 5 so với size 20 chưa có kết quả: lần đầu sai cách truyền credential; lần hai kết nối được PostgreSQL nhưng timeout sau `183 s` trong bước rebuild fixture. Vì vậy không tuyên bố cổng integration này đã pass.

## Kiểm thử mã nguồn

- Focused backend: `24/24` pass.
- Regression backend: `571` test, `0` fail, `0` error, `1` skip.
- Web: `npm run typecheck` pass.
- Mobile: `npm run typecheck` dừng do TypeScript compiler tràn call stack, không có diagnostic chỉ vào source Feed.

## Coverage các class trọng tâm

| Class | Line | Branch | Cổng yêu cầu | Kết luận |
|---|---:|---:|---:|---|
| `BlogRecommendationScoreBatchWriterImpl` | 100% | 100% | 100% / ≥85% | Pass |
| `FeedRebuildExecutorConfig` | 100% | 100% | 100% / ≥85% | Pass |
| `FeedScoreCalculationServiceImpl` | 100% | 94.34% | 100% / ≥85% | Pass |
| `SponsoredCafeCandidateServiceImpl` | 99.32% | 89.74% | 100% / ≥85% | Fail line |
| `BlogFeedRankingServiceImpl` | 86.38% | 57.19% | 100% / ≥85% | Fail |

Không tuyên bố coverage gate tổng thể đã đạt. Class ranking hiện hữu lớn chứa nhiều nhánh ngoài phần sửa; cần một đợt test riêng nếu muốn đạt đúng cổng luận văn đã đặt.

## Trạng thái API Web và Mobile

- Web Home gọi `getMixedFeed()` → `GET /api/feed`.
- Mobile Home gọi `getMixedFeed()` → `GET /api/feed`; request dùng API client chung có Bearer token và có instrumentation hiệu năng khi bật flag.
- Không có client nào gọi `POST /api/blogs/feed/rebuild`.
- Đây là chủ ý đúng: rebuild là tác vụ nội bộ/backend hoặc endpoint quản trị/Swagger. Cho Web/Mobile tự gọi rebuild sẽ tạo trùng job, dễ bị abuse và tái tạo lỗi latency.

## Vấn đề còn mở

1. Mixed Feed p95 `590.88 ms`, vượt ngưỡng 500 ms dù personalized cache-hit đã nhanh. Phần còn lại chủ yếu thuộc candidate quảng cáo/ghi impression và round-trip PostgreSQL từ xa; chưa tái kiến trúc trong phạm vi sửa 62 giây.
2. Scheduled rebuild toàn bộ user vẫn là job nặng. GET không chờ job nữa, nhưng job có thể tranh tài nguyên DB khi chạy; cần benchmark tải riêng trước khi production.
3. Chưa đo Mobile first-feed-render/media trên thiết bị Android thật.
4. Query-count integration và coverage gate chưa đạt đầy đủ.
