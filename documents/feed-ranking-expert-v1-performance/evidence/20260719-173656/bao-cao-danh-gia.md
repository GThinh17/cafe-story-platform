# Báo cáo đánh giá response time Feed EXPERT_V1

- Môi trường: local Web `localhost:3000`, backend `localhost:8080`, database theo cấu hình backend đang chạy.
- Tài khoản: authenticated; credential, cookie và token không được lưu.
- Đây là baseline local, không phải SLA production.

## Kết luận

Log `GET / 200 in 62s (... application-code: 60s)` đã được tái hiện ở đúng cold-path. Endpoint rebuild mất `58.202 ms` cho `434` bài. Khi Home gặp score chưa có hoặc sai `formulaVersion`, nó chờ rebuild đồng bộ nên thời gian gần bằng log 60 giây.

Swagger có vẻ nhanh hơn vì thường được gọi sau khi score/cache đã ấm và chỉ đo một endpoint. Tuy vậy warm Feed hiện vẫn không đạt ngưỡng 500 ms.

Một warm `/api/blogs/feed` riêng có Redis `+1 hit / +0 miss` nhưng vẫn mất `2.020 ms`. Như vậy cold rebuild không lặp lại, song cache-hit path cũng cần được profile tiếp.

## Bảng đo

| Tầng đo | Metric | Số lượt đo | Min | Avg | P50 | P75 | P95 | Max | Ngưỡng | Kết luận |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Backend cold | Rebuild `HOUR_24`, 434 bài | 1 | 58.202 ms | 58.202 ms | 58.202 ms | 58.202 ms | 58.202 ms | 58.202 ms | Không được chặn Home | **KHÔNG ĐẠT** |
| Backend warm | `/api/blogs/feed?size=20` | 50 | 1.549,21 ms | 1.772,52 ms | 1.674,64 ms | 1.766,84 ms | 2.684,11 ms | 3.067,51 ms | p95 ≤500 ms | **KHÔNG ĐẠT** |
| Backend mixed warm | `/api/feed?size=20` | 10 | 1.960,06 ms | 2.126,27 ms | 2.063,79 ms | 2.152,47 ms | 2.486,46 ms | 2.486,46 ms | p95 ≤500 ms | **KHÔNG ĐẠT** |
| Next SSR warm | Authenticated `GET /` | 10 | 2.914,93 ms | 3.097,63 ms | 3.028,32 ms | 3.131,04 ms | 3.689,78 ms | 3.689,78 ms | p95 ≤3.000 ms | **KHÔNG ĐẠT** |

Tất cả benchmark warm có error rate `0%`. Warm-up: `2` lượt cho từng nhóm, không tính vào bảng.

## Nguyên nhân kỹ thuật

1. `getPersonalizedFeed()` kiểm tra stale và gọi rebuild ngay trong request đọc.
2. Rebuild lấy toàn bộ `434` published blog, tính score và diversity.
3. `upsertRecommendationScores()` gọi repository upsert tuần tự một lần cho mỗi score, tạo write N+1 tối thiểu `434` statement.
4. Chỉ sau khi toàn bộ rebuild hoàn tất, mixed Feed mới trả về cho Next.
5. Home còn chờ top cafes và các panel phụ; ở tài khoản này các danh sách follow/chat rỗng, do đó chúng không giải thích cold delay 58–62 giây.

## Hướng sửa khuyến nghị

Ưu tiên theo tác động:

1. Không rebuild đồng bộ trong `GET`: trả snapshot `EXPERT_V1` gần nhất hoặc organic fallback ngay, đồng thời enqueue rebuild nền.
2. Thay `434` native upsert tuần tự bằng JDBC batch/multi-row upsert trong transaction.
3. Giới hạn candidate của rebuild theo kế hoạch riêng thay vì toàn bộ published blog.
4. Profile query warm path `/api/blogs/feed` và `/api/feed`; chưa đủ bằng chứng để đoán query nào chiếm p95.
5. Tách top cafes/Story Rail/Message Dock khỏi critical SSR path hoặc dùng streaming/Suspense sau khi xác minh theo tài liệu Next.js tại repo.

Chưa sửa source trong vòng này vì yêu cầu hiện tại là kiểm thử và bắt lỗi; thay đổi rebuild bất đồng bộ là thay đổi kiến trúc và cần được duyệt.
