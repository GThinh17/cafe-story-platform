# Đánh giá và cải tiến workflow

## Điều đã làm tốt

- Ghi issue trước khi sửa fixture/test, giữ đúng chu trình code → test → evidence → phân loại → retest.
- Phân biệt lỗi source với lỗi môi trường: không sửa domain model để H2 giả PostgreSQL enum, không tạo số performance giả và không dùng guest thay personalized Feed.
- Dùng version `EXPERT_V1` xuyên suốt DB, cache và stale detection giúp rollback/đối chiếu rõ hơn.
- Hai N+1 đã có test khóa hành vi “không gọi per-item”, giảm nguy cơ tái phát.

## Điểm cần cải thiện

1. Chuẩn bị profile PostgreSQL test riêng có seed tối thiểu và credential không nhạy cảm trước khi bắt đầu audit hiệu năng.
2. Cung cấp một test account cố định hoặc script cấp token ngắn hạn để benchmark authenticated có thể lặp lại.
3. Thêm script thu thập thống nhất cho HTTP, SQL count, Web Vitals và `[PERF]` mobile rồi xuất cùng schema CSV/JSON.
4. Tách phần ranking legacy trong `BlogFeedRankingServiceImpl` thành các collaborator nhỏ hơn. Service hiện khoảng lớn và coverage branch thấp; tiếp tục ép 100% bằng test thuần Mockito sẽ tốn nhiều công nhưng không tỷ lệ thuận với độ tin cậy.
5. Bổ sung unit coverage tooling cho Web/Mobile để bỏ phân loại `FRONTEND_COVERAGE_TOOLING_MISSING` ở các vòng sau.

## Đề xuất vòng evidence kế tiếp

- Khởi động PostgreSQL thật, chạy migration, seed user/blog/tag/interaction/report/impression có kiểm soát.
- Chạy query-count trước để phát hiện N+1; chỉ khi đạt mới chạy 50 HTTP request.
- Dùng cùng account và cùng dataset cho 10 Web reload và 10 Mobile refresh.
- Gắn metadata máy, network LAN, phiên bản runtime vào raw results; tiếp tục ghi rõ đây là baseline local/LAN, không phải SLA production.
