# Nhật ký vấn đề — Feed Ranking EXPERT_V1

## ENV-001 — H2 không hỗ trợ PostgreSQL named enum

- Thời điểm ghi nhận: 2026-07-19 16:51:17 (Asia/Ho_Chi_Minh)
- Phân loại: `CONFIG_ENV`
- Phạm vi: kiểm thử tích hợp đếm SQL cho Feed cá nhân hóa.
- Hiện tượng: Hibernate không thể dựng schema H2 có các cột PostgreSQL `NAMED_ENUM` (ví dụ `admin_payout.status`, SQL type 6001).
- Quyết định: không sửa mã nguồn để né lỗi môi trường; bài kiểm thử query-count phải chạy trên PostgreSQL thật.
- Tự động sửa: không.

## TOOLING-001 — Web chưa có công cụ unit coverage

- Phân loại: `CONFIG_ENV`
- Mã chuẩn hóa: `FRONTEND_COVERAGE_TOOLING_MISSING`
- Phạm vi: `2-cafe-story-nextjs-web`.
- Quyết định: chạy typecheck/build và kiểm thử hiệu năng giao diện; không tuyên bố coverage giả.
- Tự động sửa: không.

## TOOLING-002 — Mobile chưa có công cụ unit coverage

- Phân loại: `CONFIG_ENV`
- Mã chuẩn hóa: `FRONTEND_COVERAGE_TOOLING_MISSING`
- Phạm vi: `3-cafe-story-reactnative-mobile`.
- Quyết định: chạy typecheck và instrumentation có điều kiện; không tuyên bố coverage giả.
- Tự động sửa: không.

## TEST-001 — Fixture Mockito chưa đồng bộ chữ ký upsert EXPERT_V1

- Thời điểm ghi nhận: 2026-07-19 17:01:52 (Asia/Ho_Chi_Minh)
- Phân loại: `TEST_BUG`
- Cổng phát hiện: test compile của nhóm `FeedScoreCalculationServiceImplTest,BlogFeedRankingServiceImplTest,SponsoredCafeCandidateServiceImplTest`.
- Hiện tượng: bốn lỗi biên dịch do các lệnh `verify(...)` trong test thiếu tham số component mới của `upsertRecommendationScore`.
- Ảnh hưởng production: không; `mvn -DskipTests compile` đã thành công.
- Quyết định: đồng bộ fixture test với contract repository mới, sau đó chạy lại đúng nhóm test.
- Tự động sửa: có, sửa hẹp trong test.

## TEST-002 — Kỳ vọng số học trong unit test scorer không chính xác

- Thời điểm ghi nhận: 2026-07-19 17:03:17 (Asia/Ho_Chi_Minh)
- Phân loại: `TEST_BUG`
- Cổng phát hiện: focused unit tests, 27 test chạy, 2 fail.
- Nguyên nhân 1: dữ liệu mẫu Engagement tạo `Raw=120`, nhưng test kỳ vọng nhầm `Raw=100`.
- Nguyên nhân 2: test so sánh tuyệt đối `0.95` với biểu diễn IEEE `0.9500000000000001`.
- Quyết định: sửa expected Engagement theo phép tính thật và dùng tolerance cho số thực.
- Tự động sửa: có, chỉ sửa test.

## VALIDATION-001 — TypeScript Mobile tràn call stack

- Thời điểm ghi nhận: 2026-07-19 17:08 (Asia/Ho_Chi_Minh)
- Phân loại tạm thời: đang chẩn đoán.
- Cổng phát hiện: `npm run typecheck` trong mobile.
- Hiện tượng: TypeScript 5.9 dừng với `RangeError: Maximum call stack size exceeded` tại `resolveNameHelper`, không cung cấp file/dòng source.
- Quyết định: chưa sửa source; chạy lại không incremental và kiểm tra Web độc lập để xác định lỗi mới hay giới hạn tooling.
- Tự động sửa: chưa.

## BASELINE-001 — Web typecheck có lỗi ngoài phạm vi Feed

- Thời điểm ghi nhận: 2026-07-19 17:10 (Asia/Ho_Chi_Minh)
- Phân loại: `CONFIG_ENV` (baseline worktree/dependency typing).
- Cổng phát hiện: `npm run typecheck` trong Web.
- Vị trí: các modal region ward/province và các navigator yêu cầu thuộc tính `id`; không thuộc file Feed thay đổi.
- Ảnh hưởng: cổng typecheck toàn Web chưa đạt; selector `data-testid` cần được kiểm tra thêm qua production build.
- Quyết định: không mở rộng scope để sửa các module region/navigation.
- Tự động sửa: không.

## ENV-002 — Chưa có PostgreSQL cho query-count integration

- Thời điểm ghi nhận: 2026-07-19 17:12 (Asia/Ho_Chi_Minh)
- Phân loại: `CONFIG_ENV`.
- Bằng chứng: không có container/listener PostgreSQL cổng 5432; shell không có `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Quyết định: thêm test gate `FEED_QUERY_COUNT_TEST=true`, bắt buộc xác minh database product là PostgreSQL; không chạy giả bằng H2.
- Số query phiên này: chưa đo được.
- Tự động sửa: không.

## BUILD-001 — Maven clean testCompile mất class production ngoài phạm vi

- Thời điểm ghi nhận: 2026-07-19 17:15 (Asia/Ho_Chi_Minh)
- Phân loại tạm thời: `CONFIG_ENV` / build artifact state.
- Cổng phát hiện: Maven focused regression sau `clean`.
- Hiện tượng: main compile báo thành công 535 source, nhưng testCompile ngay sau đó không truy cập được hàng loạt class production ngoài phạm vi như `User`, `Role`, DTO và security classes.
- Đối chiếu: cùng nhóm focused tests đã chạy xanh 31/31 trước `clean`.
- Quyết định: chưa sửa source; kiểm tra file class thực tế và chạy lại compile/test để phân biệt lỗi môi trường với lỗi mã.
- Tự động sửa: chưa.

## COVERAGE-001 — Changed-file coverage chưa đạt hard gate

- Thời điểm ghi nhận: 2026-07-19 17:14 (Asia/Ho_Chi_Minh)
- Phân loại: `TEST_BUG` (thiếu coverage fixture).
- Scorer mới: `100%` line, `94.34%` branch — đạt.
- Ranking service: `87.98%` line, `56.15%` branch — chưa đạt.
- Sponsored candidate service: `99.32%` line, `89.74%` branch — thiếu 1 line.
- Entity recommendation score: `0%` line/branch — chưa có lifecycle unit test.
- Hard gate: mọi production Java file thay đổi phải `100%` line và `>=85%` branch.
- Quyết định: bổ sung test theo missed line/branch, không thêm `/* istanbul ignore */` hay loại file khỏi JaCoCo.
- Tự động sửa: có, chỉ mở rộng test.

## WEB-001 — Next production build bị Turbopack panic trên Windows

- Thời điểm ghi nhận: 2026-07-19 17:17 (Asia/Ho_Chi_Minh)
- Phân loại: `CONFIG_ENV`.
- Cổng phát hiện: `npm run build` (Next.js 16.2.6, Turbopack).
- Hiện tượng: `TurbopackInternalError: failed to receive message`, kết nối nội bộ bị đóng cưỡng bức (`os error 10054`).
- Liên hệ source Feed: không có file/dòng source trong panic.
- Quyết định: kiểm tra tùy chọn CLI và thử production build bằng webpack nếu được Next 16 hỗ trợ; không sửa source.
- Tự động sửa: không.

## TESTDATA-001 — Không có phiên đăng nhập/tài khoản test hợp lệ để benchmark authenticated

- Thời điểm ghi nhận: 2026-07-19 17:19 (Asia/Ho_Chi_Minh)
- Phân loại: `TEST_DATA`.
- Phạm vi: 50 request authenticated `/api/blogs/feed` và 10 lượt Web Home.
- Bằng chứng: production Home chuyển đến `/login`; tài khoản mẫu trong tài liệu trả `Invalid username/email or password`; chỉ có in-app browser và không có tab/session đăng nhập sẵn.
- Quyết định: không đoán credential, không tạo user rác, không dùng số guest thay cho personalized.
- Kết quả latency: chưa đo được trong phiên này.
- Tự động sửa: không.

## ENV-003 — Chưa có thiết bị Android/Expo kết nối

- Thời điểm ghi nhận: 2026-07-19 17:19 (Asia/Ho_Chi_Minh)
- Phân loại: `CONFIG_ENV`.
- Phạm vi: 2 warm-up + 10 pull-to-refresh trên điện thoại Android thật.
- Bằng chứng: không có `adb`/thiết bị kết nối và chưa có phiên Expo trên LAN.
- Quyết định: giữ instrumentation chỉ bật bằng `EXPO_PUBLIC_PERFORMANCE_LOGGING=true`; không thay source để giả lập số liệu thiết bị thật.
- Kết quả Mobile: chưa đo được trong phiên này.
- Tự động sửa: không.

## Trạng thái chốt các issue

| Mã | Trạng thái cuối | Kết quả |
|---|---|---|
| `TEST-001` | Đã sửa và retest | PASS trong regression backend |
| `TEST-002` | Đã sửa và retest | PASS trong regression backend |
| `VALIDATION-001` | Không sửa source | TypeScript 5.9 tràn stack; kiểm tra graph thay đổi không phát hiện lỗi instrumentation mới |
| `BASELINE-001` | Giữ nguyên ngoài scope | Production build webpack vẫn PASS |
| `BUILD-001` | Tự hết sau rebuild/retest | Regression `568` test không lỗi |
| `COVERAGE-001` | Còn mở | Hard gate chưa đạt; số cuối ở báo cáo coverage |
| `WEB-001` | Đã có đường xác minh thay thế | Webpack production build PASS; Turbopack Windows vẫn là lỗi môi trường |
| `ENV-001`, `ENV-002`, `ENV-003` | Còn chặn evidence | Cần PostgreSQL và Android/Expo thật |
| `TESTDATA-001` | Còn chặn evidence | Cần tài khoản/token test hợp lệ |
| `TOOLING-001`, `TOOLING-002` | Còn mở | `FRONTEND_COVERAGE_TOOLING_MISSING` |
