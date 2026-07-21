# Nhật ký vấn đề — sửa cold rebuild Feed

## CODE_BUG-001 — GET Feed chặn chờ rebuild đồng bộ

- Phân loại: `CODE_BUG`.
- Evidence trước sửa: `../20260719-173656/raw/response-time-summary.csv`.
- Hiện tượng: cold rebuild `434` bài mất `58.202 ms`, làm `GET /` mất khoảng `62 s`.
- Hành vi sai: request đọc tự thực hiện toàn bộ rebuild khi score thiếu hoặc sai formula version.
- Hướng sửa: stale-while-revalidate; trả snapshot cũ hoặc organic fallback ngay và chỉ schedule một background rebuild cho cùng user/window/region.
- Auto-fix: có; người dùng đã phê duyệt sửa và test lại.

## CODE_BUG-002 — Persist score tạo write N+1

- Phân loại: `CODE_BUG`.
- Evidence mã nguồn trước sửa: `upsertRecommendationScores()` gọi repository một lần cho từng score.
- Ảnh hưởng: `434` score tạo tối thiểu `434` native upsert tuần tự.
- Hướng sửa: dùng JDBC batch upsert trong cùng transaction.
- Auto-fix: có.

## CONTRACT-001 — Trạng thái wiring rebuild Web/Mobile cần xác minh

- Phân loại: `TEST_BUG` ở mức giả định/verification gap, không phải lỗi client đã xác nhận.
- Cần đối chiếu backend controller, endpoint constants, service và Home callers.
- Nguyên tắc kiểm tra: Web/Mobile chỉ nên gọi `GET /api/feed`; rebuild là trách nhiệm nội bộ backend. Không thêm `POST /api/blogs/feed/rebuild` vào client nếu không có use case quản trị rõ ràng.

## CODE_BUG-003 — Spring context không chọn được executor cho rebuild

- Phân loại: `CODE_BUG`.
- Cổng phát hiện: `mvn '-Dtest=CafestoryApplicationTests' test`.
- Hiện tượng: `NoUniqueBeanDefinitionException`; WebSocket đã tạo ba `TaskExecutor`, còn backend không có executor chuyên dụng/qualifier cho Feed rebuild.
- Ảnh hưởng: DevTools restart thất bại và cổng 8080 không lên.
- Hướng sửa: tạo bounded executor riêng `feedRecommendationRebuildExecutor` và inject bằng qualifier.
- Auto-fix: có.

## CODE_BUG-004 — Batch upsert ép kiểu vào PostgreSQL enum không tồn tại

- Phân loại: `CODE_BUG`.
- Cổng phát hiện: gọi thật `POST /api/blogs/feed/rebuild?windowType=HOUR_24` trên PostgreSQL local.
- Hiện tượng: HTTP `500` sau `4.864 s`; PostgreSQL báo `type "trend_window_type" does not exist`.
- Nguyên nhân: cột `window_type` của schema hiện tại lưu chuỗi theo `@Enumerated(EnumType.STRING)`, nhưng SQL batch mới lại ép tham số vào một PostgreSQL named enum không có trong schema.
- Hướng sửa: truyền trực tiếp tên enum Java dưới dạng chuỗi, đồng nhất với mapping JPA hiện tại.
- Auto-fix: có; sửa hẹp một biểu thức SQL và kiểm thử lại trên PostgreSQL thật.

## CODE_BUG-005 — Redis personalized Feed cache không deserialize được

- Phân loại: `CODE_BUG`.
- Cổng phát hiện: benchmark warm 50 request và đối chiếu log cache/SQL trên backend thật.
- Hiện tượng: warm p95 `1.975 s`; mỗi request vẫn tạo `10` SQL statements dù key Redis tồn tại.
- Log gốc: `Cache get failed for cache=personalizedFeedRankings ... Unexpected token (START_OBJECT)`.
- Nguyên nhân: cache serializer dùng polymorphic typing không đọc ổn định `List<BlogFeedResponse>` đã ghi trực tiếp.
- Hướng sửa: lưu payload personalized Feed dưới dạng JSON string do mapper có kiểu đích rõ ràng quản lý; Redis serializer chỉ xử lý một `String`.
- Auto-fix: có; thêm regression test gọi Feed hai lần và bắt buộc lần hai không đọc score repository.

## CONFIG_ENV-001 — Mobile TypeScript compiler tràn call stack

- Phân loại: `CONFIG_ENV`/tooling; chưa có bằng chứng là lỗi contract Feed.
- Cổng phát hiện: `npm run typecheck` tại ứng dụng React Native.
- Hiện tượng: TypeScript dừng trong `node_modules/typescript/lib/_tsc.js` với `RangeError: Maximum call stack size exceeded`, không phát ra diagnostic có đường dẫn source.
- Xử lý trong phạm vi này: lưu lỗi đúng phân loại, không sửa source Mobile để che lỗi compiler; wiring API được xác minh tĩnh bằng endpoint, service và caller Home.

## CONFIG_ENV-002 — Query-count integration chưa nhận đúng credential PostgreSQL

- Phân loại: `CONFIG_ENV`.
- Cổng phát hiện: bật `FEED_QUERY_COUNT_TEST=true` và override datasource để chạy riêng `FeedQueryCountPostgresIntegrationTest`.
- Hiện tượng: test context trả `password authentication failed`; backend runtime dùng cùng file cấu hình vẫn kết nối PostgreSQL và phục vụ request bình thường.
- Xử lý: không thay credential/DB; giữ integration ở trạng thái chưa xác nhận. Runtime instrumentation ghi nhận size 20 cache-miss `10` SQL và cache-hit `1` SQL, nhưng chưa thay thế được phép so sánh tự động size 5/20.

## PERFORMANCE-001 — Query-count integration timeout ở bước dựng snapshot

- Phân loại: `PERFORMANCE`/`TEST_DATA`.
- Cổng phát hiện: lần chạy lại integration bằng environment datasource override đã kết nối PostgreSQL thành công.
- Hiện tượng: test không hoàn tất trong `183 s`; command bị timeout trong bước `rebuildRecommendationCache(...)` trước khi xuất assertion size 5/20.
- Trạng thái: mở. Không tăng timeout vô hạn và không chỉnh ngưỡng; đề xuất seed snapshot nhỏ để test chỉ đo read path.

## PERFORMANCE-002 — Mixed Feed còn vượt ngưỡng 500 ms

- Phân loại: `PERFORMANCE`.
- Evidence: 10 lượt `GET /api/feed?size=20`, p95 `590.88 ms`, lỗi `0%`.
- Personalized Feed cache-hit đã đạt p95 `267.95 ms`; phần chênh còn lại thuộc đường candidate quảng cáo/ghi served impression và round-trip DB.
- Trạng thái: mở theo đúng stop rule; không tự ý tái kiến trúc sponsored Feed trong đợt sửa cold rebuild.

## CONFIG_ENV-003 — Next.js dev route types bị sinh hỏng trước khi commit

- Phân loại: `CONFIG_ENV`/generated cache.
- Cổng phát hiện: `npm run typecheck` ngày 20/07/2026.
- Hiện tượng: `.next/dev/types/routes.d.ts` bị nối trùng từ dòng 78 và bắt đầu bằng `efault function`; đây là file build cache, không thuộc source hoặc Git.
- Xử lý: chạy `next typegen`, xóa đúng hai generated file hỏng `routes.d.ts` và `validator.ts` trong `.next/dev/types`, sau đó chạy lại typecheck.
- Kết quả: `npm run typecheck` pass; không sửa source/`tsconfig` để che lỗi.

## Tổng trạng thái

- `CODE_BUG-001`: resolved.
- `CODE_BUG-002`: resolved.
- `CODE_BUG-003`: resolved.
- `CODE_BUG-004`: resolved và đã test PostgreSQL thật.
- `CODE_BUG-005`: resolved và đã test Redis/runtime thật.
- `CONTRACT-001`: resolved; Web/Mobile gọi `GET /api/feed`, không gọi rebuild trực tiếp.
- `CONFIG_ENV-001`: open.
- `CONFIG_ENV-002`: superseded bởi lần chạy environment override; credential đã kết nối được.
- `PERFORMANCE-001`: open.
- `PERFORMANCE-002`: open.
- `CONFIG_ENV-003`: resolved bằng cleanup generated cache.
