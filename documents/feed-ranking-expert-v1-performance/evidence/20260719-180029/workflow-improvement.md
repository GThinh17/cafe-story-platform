# Đánh giá workflow

## Điều workflow đã giúp phát hiện

- Đo endpoint thật đã tách được ba lỗi khác nhau: rebuild đồng bộ, write N+1 và Redis deserialize failure.
- Chạy PostgreSQL thật bắt được SQL cast sai mà unit mock không thể phát hiện.
- Benchmark khi scheduler đang chạy cho số liệu nhiễu; dùng cờ runtime có sẵn để tạo môi trường kiểm soát đã giúp kết luận đúng.
- Phân loại lỗi trước khi sửa ngăn việc thay client hoặc DB credential không cần thiết.

## Cải thiện đề xuất

1. Thêm integration Redis thật cho round-trip `personalizedFeedRankings`; ConcurrentMap không mô phỏng serialization.
2. Query-count test không nên tự rebuild toàn bộ candidate trong cùng test. Seed một snapshot nhỏ `EXPERT_V1`, rồi chỉ đo read path size 5/20.
3. Tách benchmark scheduled rebuild toàn user thành kịch bản tải riêng, có giới hạn số user và log progress.
4. Mobile typecheck cần cô lập file/type gây compiler recursion hoặc nâng TypeScript có kiểm soát; không dùng lỗi compiler làm bằng chứng contract sai.
5. Báo cáo Web cần bổ sung browser automation production build để có FCP/LCP/card-first-render, vì HTTP timing không thay thế Core Web Vitals.
