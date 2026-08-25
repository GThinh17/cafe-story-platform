# Đánh giá workflow

- Sai sót vòng trước: dừng ở blocker đăng nhập thay vì yêu cầu ngay credential hợp lệ và chạy benchmark authenticated.
- Kiểm tra nên làm sớm hơn: đo cold rebuild tách biệt trước warm Feed; nếu chỉ benchmark Swagger warm sẽ bỏ sót độ trễ 60 giây.
- Evidence còn thiếu: phase timing/query count bên trong rebuild và PostgreSQL statement trace.
- Cải tiến đề xuất cho workflow: với endpoint có lazy rebuild/cache, luôn báo cáo riêng `cold`, `warm` và `forced rebuild`; không gộp thành một con số response time.
