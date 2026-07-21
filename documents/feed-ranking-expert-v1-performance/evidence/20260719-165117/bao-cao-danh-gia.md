# Báo cáo đánh giá Feed Ranking EXPERT_V1

- Feature slug: `feed-ranking-expert-v1-performance`
- Mốc evidence: `20260719-165117`
- Phiên bản công thức: `EXPERT_V1`
- Trạng thái: **đã triển khai; kiểm thử chức năng đạt; kiểm thử hiệu năng thật chưa hoàn tất do thiếu môi trường và dữ liệu đăng nhập**.
- Phạm vi số liệu: baseline local/LAN trên môi trường thử nghiệm; tuyệt đối không được xem là SLA production.

## 1. Kết quả triển khai

Công thức được triển khai đúng như đã chốt:

`Score = 0.25R + 0.20I + 0.15E + 0.15F + 0.10Q + 0.05L + 0.05D + 0.05U`

- Tám trọng số cộng đúng bằng `1,00`; mọi component được giới hạn trong `[0,1]`.
- `I` lấy Jaccard giữa tags bài và tags từ like/comment/share/save thật trong 30 ngày; chỉ fallback `0,5` khi thiếu một trong hai tập.
- `D` được tính động khi chọn tuần tự; tie-break theo `feedScore`, `createdAt`, `blogId`.
- AI `VIOLATION` bị hard filter; report `OPEN/REVIEWING` làm giảm `Q`.
- Score cũ được phân biệt bằng `LEGACY_V1`; cache và row mới dùng `EXPERT_V1`.
- Follow state và ad-frequency đã chuyển sang batch-load; các test xác nhận không gọi repository theo từng item.
- Contract `GET /api/feed`, rebuild endpoint, cursor và feed session không đổi.

## 2. Bảng kết quả định lượng

`N/A` nghĩa là không có phép đo hợp lệ; không thay thế bằng số mô phỏng.

| Tầng đo | Metric | Số lượt | Min | Avg | P50 | P75 | P95 | Max | Ngưỡng | Kết luận | Evidence |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|---|---|
| Backend | Regression Maven | 568 | N/A | N/A | N/A | N/A | N/A | N/A | 0 failure, 0 error | **ĐẠT** — 1 test PostgreSQL skip có chủ đích | `raw/backend-validation.txt` |
| Backend | Coverage scorer | 1 report | N/A | N/A | N/A | N/A | N/A | N/A | 100% line, ≥85% branch | **ĐẠT** — 100% / 94,34% | `coverage/changed-java-coverage.csv` |
| Backend | Coverage ranking service | 1 report | N/A | N/A | N/A | N/A | N/A | N/A | 100% line, ≥85% branch | **KHÔNG ĐẠT** — 87,98% / 56,15% | `coverage/changed-java-coverage.csv` |
| Backend | Coverage sponsored service | 1 report | N/A | N/A | N/A | N/A | N/A | N/A | 100% line, ≥85% branch | **KHÔNG ĐẠT** — 99,32% / 89,74% | `coverage/changed-java-coverage.csv` |
| Backend | Coverage entity score | 1 report | N/A | N/A | N/A | N/A | N/A | N/A | 100% line, ≥85% branch | **ĐẠT** — 100% / 94,44% | `coverage/changed-java-coverage.csv` |
| Backend/PostgreSQL | SQL statements size=5/20 | 0 | N/A | N/A | N/A | N/A | N/A | N/A | q20 ≤ q5+2; cached q20 ≤12 | **CHƯA ĐO** (`CONFIG_ENV`) | `raw/performance-environment.txt` |
| HTTP authenticated | `/api/blogs/feed?size=20` | 0/50 | N/A | N/A | N/A | N/A | N/A | N/A | p95 ≤500 ms; lỗi 0% | **CHƯA ĐO** (`TEST_DATA`) | `raw/performance-environment.txt` |
| Mixed Feed Web | `/api/feed` | 0/10 | N/A | N/A | N/A | N/A | N/A | N/A | chỉ baseline | **CHƯA ĐO** (`TEST_DATA`) | `screenshots/web-login-auth-required.png` |
| Web production | Build webpack | 1 | N/A | N/A | N/A | N/A | N/A | N/A | build thành công | **ĐẠT** — 22 route | `raw/web-validation.txt` |
| Web Home | TTFB/FCP/LCP/DOM/card đầu | 0/10 | N/A | N/A | N/A | N/A | N/A | N/A | LCP p75 ≤2,5 s; card p95 ≤3,0 s | **CHƯA ĐO** (`TEST_DATA`) | `screenshots/web-login-auth-required.png` |
| Mobile Android thật | API Feed | 0/10 | N/A | N/A | N/A | N/A | N/A | N/A | p95 ≤500 ms | **CHƯA ĐO** (`CONFIG_ENV`) | `raw/mobile-validation.txt` |
| Mobile Android thật | First-feed-render | 0/10 | N/A | N/A | N/A | N/A | N/A | N/A | p95 ≤3,0 s | **CHƯA ĐO** (`CONFIG_ENV`) | `raw/mobile-validation.txt` |
| Mobile Android thật | First-media-load | 0/10 | N/A | N/A | N/A | N/A | N/A | N/A | chỉ báo cáo | **CHƯA ĐO** (`CONFIG_ENV`) | `raw/mobile-validation.txt` |

## 3. Kiểm tra N+1

Ở mức unit test, hai đường đã xác định được khóa bằng hành vi:

- Mapping một page Feed batch-load follow user/page; method `exists...` per-item không được phép gọi.
- Chọn quảng cáo batch-load impression count cho toàn bộ campaign; method count per-campaign không được phép gọi.

Tuy vậy, chưa thể kết luận định lượng rằng toàn bộ đường đọc personalized Feed đạt `q20 ≤ q5+2` và `≤12` SQL statements. Test integration đã có nhưng cố ý chỉ chạy khi `FEED_QUERY_COUNT_TEST=true` trên PostgreSQL thật; phiên này không có PostgreSQL/database credentials.

## 4. Web và Mobile

- Web có selector ổn định `data-testid="feed-post-card"`. Production build bằng webpack đạt; Turbopack trên Windows panic do lỗi kênh nội bộ.
- Web Home chuyển sang màn hình đăng nhập và không có tài khoản test hợp lệ, nên không đo LCP/card đầu bằng một phiên giả.
- Mobile có instrumentation chỉ bật bằng `EXPO_PUBLIC_PERFORMANCE_LOGGING=true`, tách API, first render, refresh và first media.
- Không có Android thật/ADB/Expo LAN trong phiên; do đó không có số mobile hợp lệ.
- Web và Mobile chưa có unit coverage tooling; ghi nhận `FRONTEND_COVERAGE_TOOLING_MISSING`, không tuyên bố coverage giả.

## 5. Đánh giá khách quan

Phần thuật toán và regression đã đủ chắc để review logic luận văn: công thức rõ, tổng trọng số bằng 1, component có miền giá trị xác định và không cộng lặp Trending/reviewer/activity. Tuy nhiên, chưa đủ bằng chứng để tuyên bố đạt mục tiêu hiệu năng hoặc “đã hết N+1” ở cấp SQL runtime. Hai kết luận đó chỉ được chốt sau khi chạy lại evidence trên PostgreSQL, tài khoản seed hợp lệ và Android thật.
