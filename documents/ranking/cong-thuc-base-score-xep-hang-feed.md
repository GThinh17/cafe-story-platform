# Công thức BaseScore xếp hạng bài viết trên Feed CafeStory

## 1. Mục đích tài liệu

Tài liệu này mô tả công thức chấm điểm bài viết dùng để xếp hạng Feed của CafeStory theo hướng:

- rõ ràng và có thể tính bằng tay;
- dễ giải thích với giảng viên, mentor và hội đồng;
- các thành phần có cùng thang đo từ `0` đến `1`;
- tổng trọng số bằng `1`;
- phù hợp với giai đoạn đầu của luận văn, khi chưa có đủ dữ liệu để huấn luyện mô hình máy học.

Đây là **mô hình chấm điểm tuyến tính có trọng số do chuyên gia xác lập** (`expert-defined weighted linear scoring model`). Các trọng số là trọng số khởi tạo dựa trên mục tiêu nghiệp vụ của CafeStory, không phải trọng số tối ưu đã được học từ dữ liệu người dùng.

## 2. Phạm vi áp dụng

Công thức được dùng để xếp hạng các bài viết đã trở thành ứng viên hợp lệ của Feed. Trước khi tính điểm, hệ thống phải loại bỏ các bài viết:

- không có trạng thái `PUBLISHED`;
- đã bị ẩn hoặc bị xóa;
- bị AI moderation kết luận `VIOLATION`;
- thuộc người dùng/page đã bị người xem chặn;
- người xem không có quyền truy cập;
- không còn tồn tại hoặc dữ liệu không hợp lệ.

Những điều kiện trên là **bộ lọc bắt buộc** (`hard filter`), không phải thành phần cộng hoặc trừ điểm. Bài viết không hợp lệ sẽ không được đưa vào danh sách xếp hạng.

## 3. Công thức tổng quát

Với mỗi người dùng `u` và bài viết ứng viên `p`, điểm cơ sở được tính như sau:

```text
BaseScore(u, p) =
    0.25 × R(u, p)
  + 0.20 × I(u, p)
  + 0.15 × E(p)
  + 0.15 × F(p)
  + 0.10 × Q(p)
  + 0.05 × L(u, p)
  + 0.05 × D(p, S)
  + 0.05 × U(u, p)
```

Trong đó:

| Ký hiệu | Tên thành phần | Ý nghĩa |
| --- | --- | --- |
| `R` | Relationship | Mức độ quan hệ giữa người xem và tác giả/page |
| `I` | Interest | Mức độ phù hợp với sở thích của người xem |
| `E` | Engagement | Mức độ tương tác của cộng đồng với bài viết |
| `F` | Freshness | Mức độ mới của bài viết |
| `Q` | Quality | Mức độ chất lượng và an toàn của bài viết |
| `L` | Location | Mức độ phù hợp về vị trí địa lý |
| `D` | Diversity | Mức độ đa dạng so với các bài vừa được chọn |
| `U` | Unseen | Bài viết đã được người dùng nhìn thấy gần đây hay chưa |
| `S` | Selected items | Danh sách bài đã được chọn trước bài đang xét |

Tất cả thành phần phải thỏa mãn:

```text
0 ≤ R, I, E, F, Q, L, D, U ≤ 1
```

Tổng trọng số:

```text
0.25 + 0.20 + 0.15 + 0.15 + 0.10 + 0.05 + 0.05 + 0.05 = 1.00
```

Do đó:

```text
0 ≤ BaseScore ≤ 1
```

Hệ thống sắp xếp bài viết theo `BaseScore` giảm dần. Điểm cao hơn thể hiện bài viết phù hợp hơn với người dùng trong ngữ cảnh Feed hiện tại.

> `BaseScore` là điểm xếp hạng tương đối, không phải xác suất người dùng chắc chắn thích hoặc tương tác với bài viết.

## 4. Cơ sở lựa chọn trọng số

Các trọng số được xác lập bằng phương pháp chuyên gia, dựa trên đặc trưng nghiệp vụ của CafeStory là nền tảng mạng xã hội chia sẻ bài viết và khám phá quán cà phê.

| Thành phần | Trọng số | Lý do lựa chọn |
| --- | ---: | --- |
| Relationship `R` | `0.25` | CafeStory có quan hệ follow giữa người dùng và page; nội dung từ nguồn có quan hệ nên được ưu tiên cao nhất. |
| Interest `I` | `0.20` | Bài viết phù hợp sở thích giúp Feed mang tính cá nhân hóa thay vì chỉ hiển thị nội dung phổ biến. |
| Engagement `E` | `0.15` | Tương tác cộng đồng phản ánh độ hấp dẫn, nhưng không được áp đảo sở thích cá nhân. |
| Freshness `F` | `0.15` | Feed cần duy trì nội dung mới và tránh để bài cũ có nhiều tương tác chiếm ưu thế lâu dài. |
| Quality `Q` | `0.10` | Chất lượng và độ an toàn cần được xem xét, nhưng vi phạm nghiêm trọng phải được hard filter trước khi xếp hạng. |
| Location `L` | `0.05` | Vị trí có ý nghĩa với việc khám phá quán cà phê, nhưng không phải mọi bài viết đều phụ thuộc vị trí. |
| Diversity `D` | `0.05` | Tránh nhiều bài liên tiếp từ cùng tác giả/page/chủ đề và cải thiện trải nghiệm đọc Feed. |
| Unseen `U` | `0.05` | Tăng cơ hội khám phá bài chưa xem nhưng không loại bỏ hoàn toàn bài cũ có giá trị. |

Relationship và Interest chiếm tổng cộng `45%` vì mục tiêu chính của Feed là cá nhân hóa. Engagement và Freshness chiếm `30%` để cân bằng độ phổ biến và tính thời điểm. Bốn yếu tố còn lại chiếm `25%`, đóng vai trò kiểm soát chất lượng và trải nghiệm danh sách.

Các trọng số này là **baseline của giai đoạn 1**. Chúng có thể được điều chỉnh trong tương lai khi CafeStory thu thập đủ impression, click, thời gian đọc, like, comment, share, save, skip và dismiss.

## 5. Cách tính từng thành phần

### 5.1. Relationship Score — `R(u, p)`

`R` phản ánh mức độ quan hệ giữa người xem `u` với tác giả hoặc page sở hữu bài viết `p`.

Quy tắc giai đoạn 1:

| Điều kiện | `R` |
| --- | ---: |
| Bài của chính người xem; hoặc người xem đang follow nguồn và từng tương tác thường xuyên với nguồn đó | `1.00` |
| Người xem đang follow tác giả hoặc page | `0.80` |
| Không follow nhưng từng like, comment, share hoặc save nội dung của nguồn trong thời gian gần đây | `0.50` |
| Không có quan hệ hoặc tương tác trước đó | `0.00` |

Nếu nhiều điều kiện đồng thời đúng, sử dụng giá trị cao nhất:

```text
R(u, p) = max(relationship conditions)
```

Ví dụ: người dùng đang follow một cafe page thì bài của page đó nhận `R = 0.80`. Nếu người dùng vừa follow vừa thường xuyên tương tác với page, bài có thể nhận `R = 1.00`.

### 5.2. Interest Score — `I(u, p)`

`I` đo mức độ trùng khớp giữa tập sở thích của người dùng và chủ đề của bài viết.

Gọi:

- `UserInterests(u)` là hợp các tag từ những bài người dùng đã like, comment, share hoặc save trong 30 ngày gần nhất;
- `BlogTopics(p)` là tập chủ đề hoặc tag của bài viết.

Phiên bản `EXPERT_V1` không đưa page đã follow trực tiếp vào `I`, vì tín hiệu follow đã được phản ánh trong `R`. Quy tắc này tránh tính lặp cùng một quan hệ ở hai thành phần.

Có thể sử dụng độ tương đồng Jaccard:

```text
I(u, p) =
    |UserInterests(u) ∩ BlogTopics(p)|
    ----------------------------------
    |UserInterests(u) ∪ BlogTopics(p)|
```

Ví dụ:

```text
UserInterests = {specialty-coffee, study-space, quiet-cafe}
BlogTopics    = {specialty-coffee, quiet-cafe, district-1}

Số phần tử giao = 2
Số phần tử hợp  = 4
I = 2 / 4 = 0.50
```

Trường hợp chưa có đủ dữ liệu sở thích hoặc bài viết chưa được gắn chủ đề, dùng giá trị trung lập:

```text
I(u, p) = 0.50
```

Giá trị trung lập giúp người dùng mới và bài viết mới không bị giảm điểm chỉ vì hệ thống chưa thu thập đủ dữ liệu.

### 5.3. Engagement Score — `E(p)`

Trước tiên tính điểm tương tác thô:

```text
RawEngagement(p) =
    views    × 0.1
  + likes    × 2
  + comments × 4
  + replies  × 4
  + shares   × 6
  + saves    × 5
```

Trong implementation `EXPERT_V1`, các lượt tương tác trên được batch-load trong cửa sổ 30 ngày gần nhất. Không truy vấn riêng theo từng candidate.

Giải thích mức điểm:

- view có trọng số thấp vì dễ phát sinh khi người dùng chỉ lướt qua;
- like là tín hiệu tích cực nhưng mức cam kết thấp;
- comment và reply thể hiện mức tham gia cao hơn;
- save cho thấy nội dung có giá trị sử dụng lại;
- share là hành động lan truyền nên có trọng số cao nhất.

Không dùng trực tiếp `RawEngagement` trong `BaseScore`, vì giá trị này không có giới hạn và bài viral có thể lấn át toàn bộ yếu tố cá nhân hóa.

Chuẩn hóa bằng hàm bão hòa:

```text
E(p) = RawEngagement(p) / (RawEngagement(p) + K_E)
```

Trong giai đoạn 1, chọn:

```text
K_E = 100
```

Ý nghĩa của `K_E`:

- `RawEngagement = 0` thì `E = 0`;
- `RawEngagement = 100` thì `E = 0.50`;
- `RawEngagement = 300` thì `E = 0.75`;
- khi tương tác tăng rất lớn, `E` tiến dần đến `1` nhưng không vượt quá `1`.

Hàm bão hòa tạo hiệu ứng lợi ích giảm dần: tăng từ 0 lên 100 điểm tương tác có ý nghĩa lớn hơn tăng từ 1.000 lên 1.100 điểm. Nhờ đó bài phổ biến vẫn được ưu tiên nhưng không chiếm toàn bộ Feed.

### 5.4. Freshness Score — `F(p)`

`F` giảm dần theo số giờ kể từ thời điểm bài được đăng:

```text
F(p) = exp(-AgeHours(p) / 48)
```

Trong đó:

```text
AgeHours(p) = số giờ từ createdAt của bài đến thời điểm tính điểm
```

Một số giá trị minh họa:

| Tuổi bài viết | `F` xấp xỉ |
| --- | ---: |
| Vừa đăng | `1.000` |
| 12 giờ | `0.779` |
| 24 giờ | `0.607` |
| 48 giờ | `0.368` |
| 96 giờ | `0.135` |

Mốc `48 giờ` được chọn vì Feed vẫn cần cho bài viết cũ khoảng một đến hai ngày cơ hội xuất hiện nếu có quan hệ, sở thích hoặc tương tác tốt.

### 5.5. Quality Score — `Q(p)`

Vi phạm nghiêm trọng không được xử lý bằng cách trừ điểm. Bài có moderation decision `VIOLATION` phải bị loại trước khi tính `BaseScore`.

Với các bài còn hợp lệ, giai đoạn 1 sử dụng số report đang hoạt động:

```text
Q(p) = 1 / (1 + 0.25 × ActiveReports(p))
```

Ví dụ:

| Active reports | `Q` |
| ---: | ---: |
| `0` | `1.000` |
| `1` | `0.800` |
| `2` | `0.667` |
| `4` | `0.500` |
| `8` | `0.333` |

Công thức làm giảm dần điểm chất lượng nhưng không tạo điểm âm. Trọng số `Q = 0.10` cũng giới hạn ảnh hưởng tối đa của thành phần này trong `BaseScore`.

Giới hạn của giai đoạn 1 là report thô có thể bất lợi cho bài có nhiều lượt xem. Khi có đủ impression, nên thay bằng tỷ lệ report có làm mượt:

```text
ReportRate = ActiveReports / max(Impressions, MinimumImpressions)
```

### 5.6. Location Score — `L(u, p)`

`L` phản ánh mức độ phù hợp địa lý giữa người dùng và bài viết/page:

| Điều kiện | `L` |
| --- | ---: |
| Cùng thành phố | `1.00` |
| Có đủ dữ liệu và khác thành phố | `0.00` |
| Người dùng hoặc bài viết chưa có dữ liệu vị trí | `0.50` |

Sử dụng thành phố thay vì so sánh trực tiếp `regionId`, vì nhiều khu vực khác nhau có thể thuộc cùng một thành phố.

Giá trị `0.50` trong trường hợp thiếu dữ liệu là giá trị trung lập, tránh làm giảm điểm người dùng hoặc bài viết mới.

### 5.7. Diversity Score — `D(p, S)`

`D` là thành phần phụ thuộc ngữ cảnh danh sách. Một bài không tự thân bị xem là lặp; nó bị xem là lặp khi tác giả hoặc page của nó đã xuất hiện nhiều lần trong các vị trí ngay trước đó.

Quy tắc giai đoạn 1, xét hai bài gần nhất trong danh sách `S`:

| Điều kiện | `D` |
| --- | ---: |
| Tác giả/page chưa xuất hiện trong hai bài gần nhất | `1.00` |
| Tác giả/page xuất hiện một lần trong hai bài gần nhất | `0.50` |
| Hai bài gần nhất đều đến từ cùng tác giả/page | `0.00` |

Do `D` phụ thuộc các bài đã chọn, Feed được xây dựng tuần tự:

```text
1. Tính các thành phần không phụ thuộc danh sách.
2. Chọn bài có BaseScore cao nhất.
3. Cập nhật D cho các ứng viên còn lại.
4. Chọn bài tiếp theo.
5. Lặp lại đến khi đủ số lượng cần trả về.
```

Ở phiên bản nâng cao, diversity có thể được tách thành một bước reranking riêng theo tác giả, page, chủ đề và loại nội dung.

### 5.8. Unseen Score — `U(u, p)`

`U` ưu tiên bài chưa được hiển thị gần đây cho người dùng:

| Điều kiện | `U` |
| --- | ---: |
| Chưa có impression trong 7 ngày gần nhất | `1.00` |
| Đã có impression trong 7 ngày gần nhất | `0.00` |
| Không xác định được người dùng, ví dụ khách chưa đăng nhập | `0.50` |

Không hard filter bài đã xem vì bài đó vẫn có thể đáng hiển thị lại khi có tương tác mới hoặc rất phù hợp với người dùng. `U` chỉ đóng góp tối đa `0.05`, nên đây là ưu tiên nhẹ.

## 6. Ví dụ tính điểm hoàn chỉnh

Giả sử một bài viết có các giá trị:

```text
R = 0.80   (người dùng đang follow page)
I = 0.60   (chủ đề tương đối phù hợp)
E = 0.60   (điểm tương tác đã chuẩn hóa)
F = 0.779  (bài được đăng cách đây 12 giờ)
Q = 1.00   (không có active report)
L = 1.00   (cùng thành phố)
D = 0.50   (nguồn đã xuất hiện một lần trong hai bài gần nhất)
U = 1.00   (chưa hiển thị trong 7 ngày gần nhất)
```

Thay vào công thức:

```text
BaseScore =
    0.25 × 0.80
  + 0.20 × 0.60
  + 0.15 × 0.60
  + 0.15 × 0.779
  + 0.10 × 1.00
  + 0.05 × 1.00
  + 0.05 × 0.50
  + 0.05 × 1.00

BaseScore =
    0.200
  + 0.120
  + 0.090
  + 0.117
  + 0.100
  + 0.050
  + 0.025
  + 0.050

BaseScore ≈ 0.752
```

Bài viết nhận khoảng `0.752/1.000`. Hệ thống dùng giá trị này để so sánh với các ứng viên khác và sắp xếp giảm dần.

Nếu hai bài có cùng `BaseScore`, sử dụng quy tắc ổn định:

```text
BaseScore giảm dần
-> createdAt giảm dần
-> blogId tăng dần hoặc giảm dần theo một quy ước cố định
```

Quy tắc cuối cùng theo `blogId` giúp kết quả ổn định khi điểm và thời gian bằng nhau.

## 7. Vì sao công thức chỉ sử dụng phép cộng?

Tất cả thành phần trong `BaseScore` đều được định nghĩa theo hướng:

```text
Giá trị càng cao = bài viết càng phù hợp
```

Ví dụ:

- `E` cao nghĩa là tương tác tốt;
- `F` cao nghĩa là bài còn mới;
- `Q` cao nghĩa là ít rủi ro chất lượng;
- `U` cao nghĩa là bài chưa được xem gần đây.

Vì vậy các thành phần phải được **cộng** với nhau. Nếu viết:

```text
0.25R - 0.20I - 0.15E - 0.15F ...
```

thì bài càng đúng sở thích, càng nhiều tương tác và càng mới lại càng bị hạ điểm. Công thức đó trái với ý nghĩa các biến.

Tín hiệu tiêu cực được biểu diễn bằng giá trị thành phần thấp, ví dụ bài có report thì `Q` giảm; bài đã xem thì `U = 0`. Vi phạm nghiêm trọng được hard filter trước khi tính điểm.

## 8. Vì sao tổng trọng số bằng 1?

Tổng trọng số bằng `1` không phải điều kiện bắt buộc để sắp xếp. Nhân toàn bộ trọng số với cùng một hằng số vẫn giữ nguyên thứ tự bài viết.

Tuy nhiên, chuẩn hóa tổng trọng số bằng `1` mang lại các lợi ích:

- bảo đảm `BaseScore` nằm trong `[0,1]` khi các thành phần cũng nằm trong `[0,1]`;
- thể hiện rõ tỷ lệ đóng góp tối đa của từng thành phần;
- dễ giải thích và kiểm tra công thức;
- thuận tiện khi điều chỉnh trọng số;
- phù hợp với việc trình bày mô hình rule-based trong luận văn.

Điều kiện quan trọng hơn tổng trọng số bằng `1` là tất cả thành phần phải được chuẩn hóa về cùng thang đo. Nếu `E` có thể bằng hàng nghìn trong khi `R` chỉ từ `0` đến `1`, thì dù tổng các hệ số bằng `1`, Engagement vẫn có thể lấn át toàn bộ công thức.

## 9. Trạng thái triển khai trong hệ thống

Công thức trong tài liệu đã được triển khai với mã phiên bản `EXPERT_V1` trong cơ chế rebuild Feed cá nhân hóa. Các row cũ được giữ lại và mang `formula_version = LEGACY_V1`; row mới lưu đủ tám component, `feed_score`, `rank_position`, `reason` và `formula_version = EXPERT_V1`.

Công thức MVP trước đây dùng điểm cộng/trừ trực tiếp. Bảng sau thể hiện thay đổi đã triển khai:

| Nội dung | `LEGACY_V1` | `EXPERT_V1` đã triển khai |
| --- | --- | --- |
| Thang điểm | Không giới hạn | `[0,1]` |
| Relationship | Bonus cố định | Chuẩn hóa thành `R` |
| Engagement | Cộng số lượng tương tác thô | Hàm bão hòa `E` |
| Trending | Có thể lặp engagement/freshness | Không dùng thành phần trending riêng |
| Report | Penalty trực tiếp và có nguy cơ tính lặp | Thể hiện qua `Q` hoặc hard filter |
| Seen | Trừ điểm cố định | Chuẩn hóa thành `U` |
| Diversity | Repetition penalty | Điểm động `D` theo danh sách |
| Khả năng giải thích | Khó so sánh tỷ lệ đóng góp | Trọng số thể hiện tỷ lệ rõ ràng |

Không có `TrendingScore`, reviewer bonus hoặc activity thô cộng riêng trong `EXPERT_V1`, vì các tín hiệu đó có thể lặp với Engagement và Freshness. AI moderation `VIOLATION` được hard filter trước khi chấm điểm; `Q` chỉ đếm report trạng thái `OPEN` hoặc `REVIEWING`.

Quy trình xếp hạng thực tế gồm hai bước:

1. Tính phần tĩnh `0.25R + 0.20I + 0.15E + 0.15F + 0.10Q + 0.05L + 0.05U` cho toàn bộ candidate hợp lệ.
2. Chọn bài tuần tự, mỗi vòng cộng `0.05D` dựa trên hai nguồn vừa chọn. Nếu bằng điểm, ưu tiên `createdAt` mới hơn rồi dùng `blogId` làm tie-break cố định.

Cache key cá nhân hóa chứa `EXPERT_V1`. Nếu score gần nhất sai phiên bản, hệ thống xem cache là stale và rebuild một lần, không đọc lại Redis key của công thức cũ.

## 10. Cách giải thích ngắn gọn trước giảng viên

Có thể trình bày như sau:

> Thuật toán Feed của CafeStory sử dụng mô hình chấm điểm tuyến tính có trọng số do chuyên gia xác lập. Mỗi bài viết được đánh giá theo tám yếu tố gồm quan hệ xã hội, sở thích, tương tác, độ mới, chất lượng, vị trí, đa dạng và trạng thái đã xem. Tất cả yếu tố được chuẩn hóa về khoảng từ 0 đến 1. Các trọng số được lựa chọn theo mục tiêu nghiệp vụ của CafeStory và có tổng bằng 1, nhờ đó điểm cuối cũng nằm trong khoảng từ 0 đến 1 và có thể giải thích rõ tỷ lệ đóng góp. Relationship và Interest được ưu tiên cao nhất vì mục tiêu chính là cá nhân hóa; Engagement và Freshness cân bằng độ phổ biến với tính mới; các yếu tố còn lại kiểm soát chất lượng và trải nghiệm Feed. Đây là bộ trọng số baseline của giai đoạn đầu, chưa phải trọng số tối ưu được học từ dữ liệu.

## 11. Các câu hỏi phản biện thường gặp

### Vì sao Relationship có trọng số cao nhất?

CafeStory là nền tảng xã hội có follow user và follow page. Nếu không ưu tiên quan hệ, Feed dễ trở thành bảng xếp hạng bài phổ biến chung và mất tính cá nhân hóa.

### Vì sao Engagement không có trọng số cao nhất?

Engagement cao chỉ chứng minh bài phổ biến với cộng đồng, không chứng minh bài phù hợp với từng người. Đặt Engagement quá cao sẽ tạo hiệu ứng bài giàu tương tác tiếp tục được hiển thị và nhận thêm tương tác, khiến bài mới khó có cơ hội.

### Vì sao Freshness và Engagement cùng bằng `0.15`?

Hai thành phần tạo thế cân bằng: Engagement hỗ trợ nội dung đã chứng minh được giá trị, còn Freshness tạo cơ hội cho nội dung mới. Không thành phần nào được phép áp đảo thành phần còn lại.

### Vì sao Location chỉ bằng `0.05` trong ứng dụng về cà phê?

Không phải mọi bài trên CafeStory đều là đề xuất quán cần ở gần người dùng. Location là tín hiệu hữu ích nhưng chỉ nên tạo ưu tiên nhẹ; Relationship và Interest vẫn phản ánh nhu cầu cá nhân tốt hơn.

### Các trọng số có phải tối ưu không?

Không. Đây là trọng số baseline được xác lập bằng phương pháp chuyên gia cho giai đoạn đầu của luận văn. Mục tiêu là tạo mô hình hợp lý, minh bạch và có thể kiểm chứng. Khi có đủ dữ liệu, trọng số có thể được hiệu chỉnh dựa trên chỉ số sử dụng thực tế.

### Tại sao không dùng machine learning?

Mô hình máy học cần lượng dữ liệu tương tác đủ lớn, dữ liệu nhãn đáng tin cậy và quy trình đánh giá. Trong giai đoạn hiện tại, mô hình rule-based giúp hệ thống dễ triển khai, dễ kiểm thử, dễ giải thích và phù hợp phạm vi luận văn.

### Tổng trọng số bằng 1 có chứng minh công thức đúng không?

Không. Tổng bằng `1` chỉ giúp chuẩn hóa và giải thích. Tính phù hợp của từng trọng số đến từ giả định nghiệp vụ; độ tối ưu chỉ có thể được đánh giá sau khi có dữ liệu thực nghiệm.

## 12. Giới hạn và hướng phát triển

Giới hạn của mô hình giai đoạn 1:

- trọng số được xác lập bằng đánh giá chuyên gia;
- chủ đề và sở thích phụ thuộc chất lượng tagging;
- report thô có thể chưa phản ánh chính xác chất lượng;
- diversity mới xét nguồn bài, chưa xét sâu nội dung;
- người dùng mới cần giá trị trung lập và nguồn nội dung phổ biến để cold start;
- chưa tối ưu riêng theo từng nhóm người dùng.

Khi có đủ dữ liệu, có thể:

- điều chỉnh trọng số dựa trên CTR, save rate, comment rate, dwell time và skip rate;
- thay Quality theo tỷ lệ report trên impression;
- học Relationship từ tần suất tương tác giữa user và tác giả/page;
- xây dựng Interest bằng topic, embedding hoặc lịch sử hành vi;
- chuyển Diversity thành bước reranking riêng;
- sử dụng learning-to-rank để học hàm chấm điểm từ dữ liệu.

Các cải tiến tương lai không làm thay đổi giá trị của mô hình giai đoạn 1. Mô hình hiện tại vẫn đóng vai trò baseline minh bạch để so sánh với các phiên bản nâng cao.

## 13. Kết luận

Công thức được chốt cho giai đoạn 1:

```text
BaseScore =
    0.25R
  + 0.20I
  + 0.15E
  + 0.15F
  + 0.10Q
  + 0.05L
  + 0.05D
  + 0.05U
```

Đây là công thức rule-based có các đặc điểm:

- tám thành phần có ý nghĩa nghiệp vụ rõ ràng;
- tất cả thành phần được chuẩn hóa về `[0,1]`;
- tổng trọng số bằng `1`;
- không cộng lặp Trending với Engagement và Freshness;
- tín hiệu xấu được xử lý bằng hard filter hoặc giảm `Q`/`U`;
- dễ tính toán, kiểm thử và giải thích trong phạm vi luận văn.

Các trọng số là baseline do chuyên gia xác lập và cần được mô tả đúng bản chất này khi trình bày, thay vì khẳng định là trọng số tối ưu.
