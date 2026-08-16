# Kịch bản quay video demo CafeStory — Web Client & Web Admin

Tổng thời lượng mục tiêu: **13–15 phút** (bản đầy đủ) hoặc **6 phút** (bản rút gọn cho buổi bảo vệ — xem mục 9).

| Hạng mục | Giá trị |
|---|---|
| Web Client | `http://localhost:3000` |
| Web Admin | `http://localhost:3636` |
| Backend | Spring Boot (`1-cafe-story-backend-javaspring`) |
| AI service | `4-cafe-story-ai-python` |

---

## 0. Chuẩn bị trước khi quay

### 0.1. Môi trường kỹ thuật

- [ ] Bật đủ 4 tiến trình: backend Spring Boot, AI service Python, web client (`npm run dev`), web admin (`npm run dev -p 3636`).
- [ ] Kiểm tra `1-cafe-story-backend-javaspring/src/main/resources/.env` — file này **đè** `application.properties`, sai key ở đây là hỏng cả buổi quay.
- [ ] Chạy thử một vòng toàn bộ luồng trước khi bấm ghi hình (đặc biệt: WebSocket chat, thanh toán, AI assistant).
- [ ] Kiểm tra kết nối mạng ổn định — Stripe/VNPay và AI streaming đều phụ thuộc mạng.

### 0.2. Dữ liệu cần seed sẵn

- [ ] ≥ 20 bài review có ảnh thật, trải đều nhiều quán, nhiều reviewer (feed trống là mất điểm nhất).
- [ ] ≥ 3 cafe page đang hoạt động, có ảnh bìa, có toạ độ bản đồ hợp lệ.
- [ ] ≥ 1 chiến dịch quảng cáo đang chạy để sponsored card xuất hiện trong feed.
- [ ] Reviewer demo đã có: điểm xếp hạng, huy hiệu đã đạt + huy hiệu đang tiến độ dở, lịch sử thu nhập vài tháng.
- [ ] ≥ 3 report đang ở trạng thái chờ xử lý (cho phần Admin + AI assistant).
- [ ] Vài giao dịch thanh toán đã hoàn tất (cho biểu đồ doanh thu Admin không rỗng).
- [ ] Dữ liệu ở nhiều tỉnh/thành khác nhau để bản đồ Việt Nam trong Admin có màu.

### 0.3. Tài khoản cần đăng nhập sẵn (mỗi tài khoản một cửa sổ trình duyệt riêng)

| Cửa sổ | Vai trò | Dùng cho |
|---|---|---|
| 1 | User thường / Reviewer | Phần A, B |
| 2 | Cafe Owner (CAFE_PAGE) | Phần C |
| 3 | Admin | Phần D |
| 4 (tùy chọn) | User thứ hai | Demo chat realtime hai chiều |

### 0.4. Thiết lập ghi hình

- [ ] Độ phân giải **1920×1080**, 30 fps, thu toàn màn hình (không thu cửa sổ lẻ để tránh nhảy khung).
- [ ] Ẩn thanh bookmark, tab thừa, thông báo hệ thống (bật Focus Assist / Do Not Disturb).
- [ ] Zoom trình duyệt **100%**; nếu chữ quá nhỏ khi xem lại thì dùng 110%, giữ nguyên suốt video.
- [ ] Bật chế độ sáng (light mode) làm mặc định — dành 5 giây riêng để demo nút chuyển dark mode.
- [ ] Xoá lịch sử autocomplete của ô tìm kiếm để không lộ dữ liệu test cũ.
- [ ] Bật hiển thị click chuột (mouse-click highlight) nếu phần mềm quay có hỗ trợ.
- [ ] **Che/đổi mọi thông tin nhạy cảm**: email thật, số điện thoại, key API, mã giao dịch thật.

### 0.5. Nguyên tắc thao tác khi quay

- Di chuột **chậm và dứt khoát**, dừng 1 giây trước khi click để người xem kịp nhìn.
- Sau mỗi lần chuyển trang, **dừng 2 giây** cho trang load xong rồi mới nói tiếp.
- Không rê chuột vô định khi đang nói.
- Gõ liệu đã chuẩn bị sẵn trong file text để copy-paste, tránh gõ sai giữa chừng.
- Nếu lỗi: **không dừng máy quay**, im lặng 3 giây rồi làm lại thao tác đó — cắt lúc dựng.

---

## 1. Mở đầu — 0:00 → 0:30

| Mục | Nội dung |
|---|---|
| Hình ảnh | Slide tiêu đề: tên đề tài, tên nhóm, GVHD → fade sang trang chủ web client |
| Thao tác | Không |

**Lời thoại:**
> "Xin chào thầy cô. Sau đây nhóm em xin trình bày video demo hệ thống CafeStory — nền tảng mạng xã hội chia sẻ trải nghiệm quán cà phê, kết hợp cơ chế xếp hạng người đánh giá và kênh quảng cáo cho chủ quán. Hệ thống gồm ba phần: web dành cho người dùng, khu vực quản trị dành cho admin, và ứng dụng di động. Video này tập trung vào hai phần web."

**Lưu ý:** giữ đúng 30 giây, không dài dòng phần mở đầu.

---

## 2. PHẦN A — WEB CLIENT: Người dùng phổ thông (0:30 → 5:00)

### A1. Đăng nhập — 0:30 → 0:50

| Mục | Nội dung |
|---|---|
| Màn hình | `/login` |
| Thao tác | Nhập email + mật khẩu (đã chuẩn bị sẵn) → Đăng nhập → chuyển về trang chủ |

**Lời thoại:**
> "Hệ thống có đăng ký, đăng nhập và phân quyền theo vai trò: người dùng, reviewer, chủ quán và quản trị viên. Em đăng nhập bằng tài khoản người dùng thường."

**Lưu ý:** đi thật nhanh — đây là chức năng chuẩn, không phải điểm nhấn. **Không** quay màn hình nhập mật khẩu ở chế độ hiện chữ.

---

### A2. Trang chủ — Bảng tin hỗn hợp — 0:50 → 1:50

| Mục | Nội dung |
|---|---|
| Màn hình | `/` |
| Thao tác | 1) Dừng ở đầu trang, quét chuột qua sidebar trái<br>2) Cuộn chậm qua story rail<br>3) Cuộn tới khi gặp **sponsored cafe card** → dừng lại chỉ vào nhãn "Được tài trợ"<br>4) Chỉ vào panel "Top cafe gần bạn" bên phải<br>5) Cuộn thêm để thấy infinite scroll nạp bài mới |

**Lời thoại:**
> "Đây là bảng tin chính. Bảng tin được sắp xếp bằng thuật toán xếp hạng riêng của hệ thống, kết hợp độ mới của bài viết, mức độ tương tác, uy tín của người viết và mức độ liên quan với sở thích người dùng — chứ không phải sắp xếp theo thời gian đơn thuần."
>
> "Xen giữa các bài viết là các thẻ quán được tài trợ. Điểm quan trọng ở đây là nội dung quảng cáo được gắn nhãn rõ ràng và có kiểu dáng riêng, không giả dạng bài viết tự nhiên của cộng đồng."
>
> "Bên phải là gợi ý các quán gần vị trí người dùng, còn bảng tin bên dưới tự động nạp thêm khi cuộn."

**Lưu ý:** đây là cảnh quan trọng nhất — cuộn **chậm hơn bình thường**, đừng để feed lướt vụt qua.

---

### A3. Tương tác với bài viết — 1:50 → 2:30

| Mục | Nội dung |
|---|---|
| Màn hình | `/` (modal bình luận) |
| Thao tác | 1) Thích một bài → cho thấy số đếm tăng<br>2) Bấm vào ảnh nhiều tấm → lướt carousel<br>3) Mở modal bình luận → gõ một bình luận có `@mention` → gửi<br>4) Bấm lưu bài (save)<br>5) Mở menu "…" → chỉ vào mục Báo cáo bài viết → mở modal báo cáo → chọn lý do → gửi |

**Lời thoại:**
> "Người dùng có thể thích, bình luận, chia sẻ và lưu bài viết. Trong bình luận có hỗ trợ nhắc tên người dùng khác bằng ký hiệu a-còng, và người được nhắc sẽ nhận thông báo."
>
> "Người dùng cũng có thể báo cáo bài viết vi phạm. Báo cáo này sẽ đi vào hàng đợi xử lý của quản trị viên — phần đó em sẽ trình bày ở khu vực admin."

**Lưu ý:** báo cáo bài viết ở đây chính là dữ liệu sẽ được xử lý ở cảnh D5 → nên **báo cáo đúng bài mà lát nữa admin sẽ xử lý**, tạo mạch liên kết giữa hai phần.

---

### A4. Đăng bài review mới — 2:30 → 3:20

| Mục | Nội dung |
|---|---|
| Màn hình | Modal tạo bài viết (nút "+" ở sidebar) |
| Thao tác | 1) Bấm "+"<br>2) Chọn quán cà phê từ danh sách<br>3) Tải lên 2–3 ảnh → thao tác **cắt ảnh** một tấm<br>4) Viết nội dung ngắn có `@mention`<br>5) Chấm điểm/đánh giá quán<br>6) Đăng → quay về feed thấy bài vừa đăng ở đầu |

**Lời thoại:**
> "Chức năng cốt lõi của người dùng là viết bài đánh giá một quán cà phê. Bài viết được gắn với một quán cụ thể, cho phép tải nhiều ảnh kèm công cụ cắt ảnh ngay trên trình duyệt, và hỗ trợ nhắc tên người dùng khác."
>
> "Sau khi đăng, bài viết được đưa vào hàng đợi kiểm duyệt tự động trước khi hiển thị rộng rãi."

**Lưu ý:** chuẩn bị sẵn 3 ảnh đẹp trong thư mục dễ tìm, đặt tên không lộ đường dẫn cá nhân. Nội dung bài viết soạn sẵn để copy-paste.

---

### A5. Khám phá & tìm kiếm — 3:20 → 4:00

| Mục | Nội dung |
|---|---|
| Màn hình | `/explore` |
| Thao tác | 1) Vào Explore → lưới bài viết<br>2) Gõ từ khoá vào ô tìm kiếm<br>3) Chuyển lần lượt 3 tab: Bài viết → Quán → Reviewer<br>4) Ở tab Reviewer, chỉ vào **hạng và huy hiệu** hiển thị trên thẻ reviewer |

**Lời thoại:**
> "Trang Khám phá cho phép tìm kiếm theo ba nhóm kết quả: bài viết, quán cà phê và người đánh giá. Ở nhóm người đánh giá, hệ thống hiển thị luôn hạng và huy hiệu của họ, giúp người xem biết được mức độ uy tín của người viết."

---

### A6. Trang quán cà phê — 4:00 → 4:35

| Mục | Nội dung |
|---|---|
| Màn hình | `/cafes/[id]` |
| Thao tác | 1) Bấm vào một quán từ Explore<br>2) Cuộn xem thông tin quán, ảnh, mô tả<br>3) Mở **modal bản đồ** → đóng lại<br>4) Thực hiện đánh giá sao cho quán<br>5) Cuộn xuống khối bài đánh giá gần đây<br>6) Chỉ vào khối chiến dịch quảng cáo của quán (nếu có) |

**Lời thoại:**
> "Mỗi quán có một trang riêng gồm thông tin, hình ảnh, vị trí trên bản đồ, điểm đánh giá trung bình và các bài viết gần đây về quán đó. Người dùng có thể chấm điểm trực tiếp tại đây."

---

### A7. Nhắn tin thời gian thực — 4:35 → 5:05

| Mục | Nội dung |
|---|---|
| Màn hình | `/messages` — **cần 2 cửa sổ đặt cạnh nhau** |
| Thao tác | 1) Chia đôi màn hình: trái = user A, phải = user B<br>2) User A gửi tin → **quay cận cảnh tin nhắn hiện ngay bên phải mà không tải lại trang**<br>3) User B trả lời<br>4) Chỉ vào chỉ báo đã đọc / danh sách hội thoại |

**Lời thoại:**
> "Hệ thống có nhắn tin thời gian thực giữa các người dùng, xây dựng trên WebSocket. Ở đây em mở hai tài khoản song song để minh hoạ: tin nhắn xuất hiện ngay lập tức ở phía người nhận mà không cần tải lại trang."

**Lưu ý:** **cảnh dễ hỏng nhất.** Kiểm tra WebSocket đã kết nối trước khi bấm ghi (mở tab Network, lọc `ws`, thấy trạng thái 101). Nếu tin nhắn không hiện ngay, tải lại **cả hai** cửa sổ rồi quay lại.

---

### A8. Thông báo & Hồ sơ cá nhân — 5:05 → 5:35

| Mục | Nội dung |
|---|---|
| Màn hình | Panel thông báo, `/[username]`, `/[username]/edit`, `/[username]/archive` |
| Thao tác | 1) Bấm chuông → panel thông báo trượt ra<br>2) Lọc theo loại thông báo → bấm một thông báo → nhảy đúng tới bài viết liên quan<br>3) Vào trang cá nhân → lưới bài viết → tab đã lưu / lưu trữ<br>4) Mở chỉnh sửa hồ sơ → đổi ảnh đại diện hoặc tiểu sử → lưu<br>5) Mở cài đặt → **đổi ngôn ngữ Việt/Anh** → **chuyển dark mode** |

**Lời thoại:**
> "Thông báo được phân loại theo lượt thích, bình luận, chia sẻ, nhắc tên và tin nhắn, và bấm vào sẽ điều hướng đúng tới nội dung liên quan. Trang cá nhân hiển thị toàn bộ bài viết, bài đã lưu và bài đã lưu trữ."
>
> "Giao diện hỗ trợ song ngữ Việt — Anh và có chế độ sáng, tối."

**Lưu ý:** phần đổi ngôn ngữ + dark mode nên gộp lại làm **một đoạn 8 giây gọn gàng**, đừng bấm qua bấm lại nhiều lần.

---

## 3. PHẦN B — REVIEWER DASHBOARD (5:35 → 8:15)

> Đây là phần **đóng góp riêng** của đề tài — quay kỹ, nói chậm hơn phần A.

### B1. Tổng quan bảng điều khiển — 5:35 → 6:15

| Mục | Nội dung |
|---|---|
| Màn hình | `/reviewer-dashboard` |
| Thao tác | 1) Vào dashboard<br>2) Dừng ở hàng thẻ thống kê → đọc lướt từng chỉ số<br>3) Chỉ vào thẻ phân khúc reviewer (segment)<br>4) Chỉ vào biểu đồ hiệu suất<br>5) Cuộn xuống panel hoạt động gần đây |

**Lời thoại:**
> "Người dùng viết bài đánh giá thường xuyên sẽ trở thành reviewer và có một bảng điều khiển riêng. Màn hình tổng quan tóm tắt các chỉ số: số bài viết, lượt tương tác nhận được, điểm xếp hạng, phân khúc hiện tại và thu nhập tích luỹ."

---

### B2. Xếp hạng & bảng xếp hạng — 6:15 → 6:55

| Mục | Nội dung |
|---|---|
| Màn hình | `/reviewer-dashboard/ranking` |
| Thao tác | 1) Vào trang xếp hạng<br>2) Chỉ vào **hạng hiện tại và điểm số**<br>3) Chỉ vào phần **diễn giải cách tính điểm** (nếu giao diện có hiển thị thành phần điểm)<br>4) Cuộn xuống bảng xếp hạng chung → chỉ vào vị trí của mình |

**Lời thoại:**
> "Điểm xếp hạng của reviewer không phải là con số tuỳ ý, mà được tính theo công thức có trọng số gồm nhiều thành phần: số lượng và chất lượng bài viết, mức độ tương tác thực nhận, độ đều đặn khi hoạt động và các yếu tố phạt khi vi phạm."
>
> "Bộ trọng số của công thức này không nằm cứng trong mã nguồn mà được quản trị viên cấu hình được — em sẽ minh hoạ ở phần admin."

**Lưu ý:** đây là câu **liên kết mạnh nhất** giữa client và admin. Nhớ nói rõ, vì lát nữa cảnh D6 sẽ chứng minh lại.

---

### B3. Huy hiệu — 6:55 → 7:20

| Mục | Nội dung |
|---|---|
| Màn hình | `/reviewer-dashboard/badges` |
| Thao tác | 1) Vào trang huy hiệu<br>2) Chỉ vào các huy hiệu **đã đạt**<br>3) Chỉ vào một huy hiệu **đang dở** → thanh tiến độ và điều kiện còn thiếu |

**Lời thoại:**
> "Song song với xếp hạng là hệ thống huy hiệu. Mỗi huy hiệu gắn với một điều kiện cụ thể và hiển thị tiến độ hiện tại, giúp reviewer biết chính xác cần làm gì để đạt được — đây là cơ chế khuyến khích người dùng đóng góp nội dung đều đặn."

---

### B4. Hiệu suất — 7:20 → 7:40

| Mục | Nội dung |
|---|---|
| Màn hình | `/reviewer-dashboard/performance` |
| Thao tác | Vào trang → chỉ vào biểu đồ theo thời gian → rê chuột lên một điểm dữ liệu để hiện tooltip |

**Lời thoại:**
> "Trang hiệu suất thống kê biến động các chỉ số theo thời gian, giúp reviewer theo dõi hiệu quả nội dung của mình theo từng giai đoạn."

---

### B5. Thu nhập & rút tiền — 7:40 → 8:15

| Mục | Nội dung |
|---|---|
| Màn hình | `/reviewer-dashboard/earnings` |
| Thao tác | 1) Vào trang thu nhập → bảng thu nhập theo kỳ<br>2) Chỉ vào trạng thái kết nối tài khoản nhận tiền (Stripe Connect)<br>3) Bấm nút kết nối → **màn hình onboarding của Stripe** → quay lại trang trả về<br>4) Chỉ vào panel yêu cầu rút tiền và lịch sử chi trả |

**Lời thoại:**
> "Reviewer có thu nhập từ hệ thống dựa trên đóng góp nội dung. Thu nhập được ghi nhận theo kỳ và được chi trả qua Stripe Connect: reviewer kết nối tài khoản nhận tiền một lần, sau đó theo dõi các kỳ chi trả và trạng thái từng khoản ngay trên bảng điều khiển."

**Lưu ý:**
- Dùng tài khoản **Stripe test mode**. Che số tài khoản, mã định danh Stripe khi dựng.
- Nếu luồng onboarding Stripe chậm, quay **riêng** đoạn này rồi tua nhanh 4× khi dựng, có chú thích "tua nhanh".

---

## 4. PHẦN C — WEB CLIENT: Chủ quán cà phê (8:15 → 10:00)

> Chuyển sang cửa sổ trình duyệt số 2 (tài khoản chủ quán). Khi dựng, chèn một **thẻ tiêu đề 2 giây**: "Vai trò: Chủ quán".

### C1. Nâng cấp gói & thanh toán — 8:15 → 9:00

| Mục | Nội dung |
|---|---|
| Màn hình | Modal bảng giá → cổng thanh toán → `/payments/vnpay/return` hoặc `/payments/stripe/success` |
| Thao tác | 1) Mở modal bảng giá từ sidebar<br>2) So sánh các gói → chọn một gói<br>3) Chọn phương thức thanh toán → **VNPay**<br>4) Sang cổng VNPay sandbox → thanh toán bằng thẻ test<br>5) Quay về trang kết quả → trạng thái thành công<br>6) Chỉ nhanh rằng hệ thống cũng hỗ trợ Stripe |

**Lời thoại:**
> "Chủ quán muốn mở trang quán và chạy quảng cáo thì cần đăng ký gói dịch vụ. Hệ thống tích hợp hai cổng thanh toán: VNPay cho thị trường trong nước và Stripe cho thẻ quốc tế. Ở đây em dùng môi trường thử nghiệm của VNPay."
>
> "Sau khi thanh toán, hệ thống nhận kết quả trả về, xác thực chữ ký giao dịch rồi mới kích hoạt quyền cho tài khoản."

**Lưu ý:** **tuyệt đối dùng sandbox và thẻ test.** Khi dựng, che toàn bộ mã giao dịch và thông tin thẻ, kể cả thẻ test.

---

### C2. Tạo & quản lý trang quán — 9:00 → 9:25

| Mục | Nội dung |
|---|---|
| Màn hình | `/cafes/edit` |
| Thao tác | 1) Vào trang chỉnh sửa quán<br>2) Sửa tên/mô tả/giờ mở cửa<br>3) Đổi ảnh bìa<br>4) Chọn vị trí trên bản đồ<br>5) Lưu → mở trang quán công khai xem kết quả |

**Lời thoại:**
> "Chủ quán tự quản lý thông tin trang quán của mình: mô tả, hình ảnh, giờ mở cửa và vị trí trên bản đồ. Thay đổi được phản ánh ngay trên trang quán công khai."

---

### C3. Tạo chiến dịch quảng cáo — 9:25 → 10:00

| Mục | Nội dung |
|---|---|
| Màn hình | `/cafes/campaigns/new` → `/ads` |
| Thao tác | 1) Vào tạo chiến dịch mới<br>2) Điền: tên chiến dịch, ngân sách, thời gian chạy, nội dung hiển thị<br>3) Tạo → sang bảng điều khiển quảng cáo `/ads`<br>4) Chỉ vào các chỉ số hiệu quả: lượt hiển thị, lượt bấm<br>5) **Chuyển về cửa sổ user thường, tải lại trang chủ → chỉ vào sponsored card của chính chiến dịch vừa tạo** |

**Lời thoại:**
> "Chủ quán tạo chiến dịch quảng cáo với ngân sách và thời gian chạy cụ thể. Sau khi chiến dịch được kích hoạt, thẻ quán được tài trợ sẽ xuất hiện xen kẽ trong bảng tin của người dùng, có gắn nhãn rõ ràng, và chủ quán theo dõi được lượt hiển thị cùng lượt bấm ngay trên bảng điều khiển quảng cáo."

**Lưu ý:** thao tác cuối (quay về feed nhìn thấy quảng cáo vừa tạo) là **cảnh chốt của phần C** — nó chứng minh vòng lặp khép kín. Đừng bỏ.

---

## 5. PHẦN D — WEB ADMIN (10:00 → 14:00)

> Chuyển sang cửa sổ số 3. Chèn thẻ tiêu đề 2 giây: "Khu vực quản trị".

### D1. Đăng nhập & Tổng quan — 10:00 → 10:40

| Mục | Nội dung |
|---|---|
| Màn hình | `localhost:3636/login` → `/` |
| Thao tác | 1) Đăng nhập admin (nhanh)<br>2) Quét chuột qua sidebar: nhóm Content / Operations / Finance<br>3) Dừng ở hàng thẻ thống kê<br>4) Chỉ vào biểu đồ doanh thu<br>5) Chỉ vào biểu đồ vòng trạng thái<br>6) Chỉ vào **bản đồ Việt Nam** phân bố dữ liệu theo tỉnh thành |

**Lời thoại:**
> "Khu vực quản trị là một ứng dụng web tách riêng, chỉ tài khoản có vai trò quản trị mới truy cập được. Màn hình tổng quan gồm các chỉ số vận hành, biểu đồ doanh thu theo thời gian, phân bố trạng thái nội dung và bản đồ phân bố dữ liệu theo tỉnh thành."
>
> "Menu bên trái chia thành ba nhóm: quản lý nội dung, vận hành và tài chính."

**Lưu ý:** đảm bảo bản đồ có màu (cần dữ liệu ở nhiều tỉnh) — bản đồ trắng trơn trông rất tệ.

---

### D2. Quản lý nội dung — 10:40 → 11:15

| Mục | Nội dung |
|---|---|
| Màn hình | `/blogs`, `/cafes`, `/comments` |
| Thao tác | 1) Vào Blogs → lọc theo trạng thái → tìm kiếm → mở chi tiết một bài (dialog) → đổi trạng thái bài viết<br>2) Sang Cafes → xem danh sách trang quán và trạng thái gói<br>3) Sang Comments → ẩn một bình luận vi phạm |

**Lời thoại:**
> "Quản trị viên quản lý toàn bộ nội dung: bài viết, trang quán và bình luận. Mỗi bảng đều có tìm kiếm, lọc theo trạng thái, phân trang và xem chi tiết. Quản trị viên có thể thay đổi trạng thái hiển thị của bài viết hoặc ẩn bình luận vi phạm."

---

### D3. Kiểm duyệt — 11:15 → 11:45

| Mục | Nội dung |
|---|---|
| Màn hình | `/moderation` |
| Thao tác | 1) Vào hàng đợi kiểm duyệt<br>2) Mở một mục → xem **kết quả phân loại tự động và lý do**<br>3) Duyệt một mục / từ chối một mục kèm lý do<br>4) **Chuyển nhanh về cửa sổ user → mở thông báo → cho thấy người dùng nhận được lý do bị từ chối** |

**Lời thoại:**
> "Nội dung mới được đưa qua bước kiểm duyệt. Hệ thống tự động phân tích và đề xuất trước, quản trị viên là người ra quyết định cuối cùng. Khi một bài bị từ chối, người viết nhận được thông báo kèm lý do cụ thể chứ không bị ẩn nội dung một cách im lặng."

**Lưu ý:** cảnh đối chiếu sang phía người dùng rất thuyết phục — chuẩn bị sẵn cửa sổ để chỉ cần Alt+Tab.

---

### D4. Xử lý báo cáo với trợ lý AI — 11:45 → 12:40 ⭐

> **Đây là điểm nhấn lớn nhất của video. Quay kỹ, nói chậm, không cắt vội.**

| Mục | Nội dung |
|---|---|
| Màn hình | `/reports` + ngăn trợ lý AI |
| Thao tác | 1) Vào Reports → danh sách báo cáo chờ xử lý (bao gồm báo cáo đã tạo ở cảnh A3)<br>2) Mở ngăn trợ lý AI<br>3) Bấm một gợi ý có sẵn, ví dụ *"Tóm tắt moderation queue hiện tại"*<br>4) **Để yên cho câu trả lời chạy chữ theo thời gian thực — không tua**<br>5) Gõ tiếp câu hỏi về đúng báo cáo ở cảnh A3<br>6) Khi trợ lý đề xuất **hành động nháp**, dừng lại chỉ vào nội dung đề xuất<br>7) Bấm xác nhận thực thi<br>8) Đóng ngăn → tải lại danh sách → **cho thấy báo cáo đã đổi trạng thái** |

**Lời thoại:**
> "Điểm nổi bật của khu vực quản trị là trợ lý ảo hỗ trợ xử lý báo cáo vi phạm. Trợ lý này biết ngữ cảnh trang mà quản trị viên đang mở, truy vấn được dữ liệu báo cáo và kiểm duyệt thực tế trong hệ thống, và trả lời theo thời gian thực bằng cả tiếng Việt lẫn tiếng Anh."
>
> "Quan trọng hơn, trợ lý không tự ý thay đổi dữ liệu. Nó chỉ **soạn sẵn một hành động ở dạng nháp** kèm giải thích, và hành động đó chỉ được thực hiện khi quản trị viên bấm xác nhận. Con người vẫn là người chịu trách nhiệm cuối cùng."
>
> *(sau khi bấm xác nhận)*
> "Sau khi xác nhận, trạng thái báo cáo được cập nhật ngay trong danh sách."

**Lưu ý:**
- Kiểm tra AI service đang chạy và **hỏi thử trước** khi bấm ghi hình.
- Câu hỏi soạn sẵn, copy-paste, tránh gõ sai giữa lúc quay.
- Nếu câu trả lời quá dài, khi dựng có thể tăng tốc 2× **phần giữa** nhưng phải giữ nguyên tốc độ thật ở đoạn đầu (để thấy hiệu ứng streaming) và đoạn hành động nháp.
- Nếu AI trả lời sai/lạc đề: quay lại, **đừng cố cứu bằng lời thoại**.

---

### D5. Xếp hạng & công thức — 12:40 → 13:15 ⭐

| Mục | Nội dung |
|---|---|
| Màn hình | `/ranking`, `/ranking/formulas`, `/formulas` |
| Thao tác | 1) Vào Ranking → bảng xếp hạng reviewer toàn hệ thống<br>2) Vào công thức xếp hạng → **chỉ vào từng trọng số**<br>3) **Sửa một trọng số** → lưu<br>4) Chuyển sang cửa sổ reviewer → tải lại trang xếp hạng → cho thấy điểm/thứ hạng thay đổi |

**Lời thoại:**
> "Như đã nói ở phần reviewer, công thức tính điểm xếp hạng được cấu hình động. Quản trị viên xem và chỉnh trực tiếp trọng số của từng thành phần tại đây, không cần sửa mã nguồn hay triển khai lại hệ thống."
>
> "Em thay đổi một trọng số và lưu lại. Quay sang bảng điều khiển của reviewer, điểm và thứ hạng đã được tính lại theo công thức mới."

**Lưu ý:**
- Đây là cảnh **chứng minh luận điểm** đã nêu ở B2 — giá trị demo rất cao, đáng để đầu tư quay lại nhiều lần cho mượt.
- Đổi trọng số ở mức đủ lớn để kết quả thay đổi thấy rõ; **thử trước** để biết đổi bao nhiêu thì thứ hạng đảo.
- Sau khi quay xong nhớ **trả trọng số về giá trị cũ**.

---

### D6. Người dùng & khu vực — 13:15 → 13:35

| Mục | Nội dung |
|---|---|
| Màn hình | `/users`, `/regions` |
| Thao tác | 1) Users → tìm kiếm → lọc theo vai trò → mở chi tiết → thao tác khoá/mở khoá tài khoản<br>2) Regions → danh sách tỉnh/thành, thêm hoặc sửa một khu vực |

**Lời thoại:**
> "Quản trị viên quản lý tài khoản người dùng theo vai trò, có thể khoá tài khoản vi phạm, và quản lý danh mục khu vực địa lý dùng cho việc gợi ý quán theo vị trí."

---

### D7. Tài chính — 13:35 → 14:10

| Mục | Nội dung |
|---|---|
| Màn hình | `/payments`, `/extra-fees`, `/payout`, `/payout/income`, `/payout/monthly`, `/payout/formulas` |
| Thao tác | 1) Payments → lịch sử giao dịch → **tìm đúng giao dịch VNPay ở cảnh C1**<br>2) Extra fees → cấu hình phụ phí<br>3) Payout → tổng quan chi trả<br>4) Payout / Income → thu nhập reviewer<br>5) Payout / Monthly → kỳ chi trả theo tháng<br>6) Payout / Formulas → công thức chia thu nhập → chỉ vào tham số |

**Lời thoại:**
> "Nhóm chức năng tài chính gồm: lịch sử giao dịch thanh toán của chủ quán — đây chính là giao dịch em vừa thực hiện ở phần trước; cấu hình phụ phí; và toàn bộ quy trình chi trả cho reviewer gồm thu nhập ghi nhận, kỳ chi trả theo tháng và công thức phân chia thu nhập."
>
> "Tương tự công thức xếp hạng, công thức chia thu nhập cũng được cấu hình từ giao diện quản trị."

**Lưu ý:** che số tiền thật nếu là dữ liệu nhạy cảm; dữ liệu demo nên là số tròn, dễ đọc.

---

## 6. Kết — 14:10 → 14:40

| Mục | Nội dung |
|---|---|
| Hình ảnh | Cắt nhanh 4–5 màn hình tiêu biểu (feed → reviewer dashboard → sponsored card → admin AI assistant → công thức xếp hạng), mỗi cảnh 2 giây → slide kết |

**Lời thoại:**
> "Tóm lại, CafeStory là một hệ thống hoàn chỉnh gồm: bảng tin hỗn hợp có xếp hạng nội dung, cơ chế xếp hạng và huy hiệu cho người đánh giá kèm chi trả thu nhập, kênh quảng cáo có gắn nhãn minh bạch cho chủ quán, và khu vực quản trị với trợ lý ảo hỗ trợ kiểm duyệt cùng các công thức nghiệp vụ cấu hình động."
>
> "Cảm ơn thầy cô đã theo dõi."

---

## 7. Danh sách cảnh dễ hỏng — quay dự phòng

Quay sẵn **bản dự phòng** cho các cảnh sau, vì chúng phụ thuộc yếu tố bên ngoài:

| Cảnh | Rủi ro | Phương án dự phòng |
|---|---|---|
| A7 — Chat realtime | WebSocket rớt kết nối | Quay lại 2–3 lần, chọn bản mượt nhất |
| C1 — Thanh toán VNPay | Sandbox chậm/lỗi | Quay riêng, tua nhanh khi dựng; dự phòng bằng luồng Stripe |
| B5 — Stripe Connect | Onboarding nhiều bước, chậm | Quay riêng, tua nhanh 4× có chú thích |
| D4 — Trợ lý AI | Trả lời sai/chậm/lỗi service | Quay 3 lần với 3 câu hỏi khác nhau, chọn bản tốt nhất |
| D5 — Sửa công thức xếp hạng | Đổi trọng số nhưng kết quả không đổi rõ | Thử trước, chốt sẵn con số tạo khác biệt lớn |

---

## 8. Checklist hậu kỳ (dựng phim)

- [ ] Cắt toàn bộ đoạn chờ load > 3 giây.
- [ ] Chèn **thẻ tiêu đề 2 giây** mỗi khi đổi vai trò: Người dùng → Reviewer → Chủ quán → Quản trị viên.
- [ ] Chèn phụ đề (tiếng Việt) — bắt buộc nếu thu tiếng bằng micro laptop.
- [ ] Zoom/highlight ở các cảnh chữ nhỏ: thẻ thống kê, bảng công thức, bảng giao dịch.
- [ ] Làm mờ: email thật, mã giao dịch, ID Stripe, số điện thoại, đường dẫn thư mục cá nhân.
- [ ] Nhạc nền nhẹ, âm lượng **≤ 10%** so với giọng nói — hoặc bỏ hẳn.
- [ ] Xuất **1080p, MP4 (H.264)**.
- [ ] Xem lại toàn bộ một lượt với âm lượng bình thường trước khi nộp.

---

## 9. Bản rút gọn 6 phút (dùng khi bảo vệ trực tiếp)

Nếu chỉ có 6 phút, cắt xuống còn các cảnh sau — giữ nguyên thứ tự:

| Thời lượng | Cảnh | Lý do giữ |
|---|---|---|
| 0:20 | Mở đầu | Bắt buộc |
| 1:00 | A2 — Bảng tin hỗn hợp + sponsored card | Chức năng lõi + điểm nhấn quảng cáo |
| 0:40 | A4 — Đăng bài review | Chức năng lõi |
| 0:30 | A7 — Chat realtime | Chứng minh năng lực kỹ thuật |
| 1:00 | B1 + B2 + B3 — Dashboard, xếp hạng, huy hiệu | Đóng góp riêng của đề tài |
| 0:40 | C3 — Tạo chiến dịch → thấy quảng cáo trong feed | Vòng lặp khép kín |
| 1:00 | D4 — Trợ lý AI xử lý báo cáo | Điểm nhấn lớn nhất |
| 0:40 | D5 — Sửa công thức → điểm reviewer đổi | Chứng minh cấu hình động |
| 0:20 | Kết | Bắt buộc |

**Cắt bỏ:** A1 (đăng nhập), A5, A6, A8, B4, B5, C1, C2, D1, D2, D3, D6, D7 — nhắc bằng một câu duy nhất trong phần kết là đủ.
