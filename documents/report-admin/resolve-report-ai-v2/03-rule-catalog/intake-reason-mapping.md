# Intake Reason → Candidate Rule Mapping

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Approval chat | `APPROVE_G0-08` |
| Trạng thái | `PROPOSED` |
| Runtime inventory | 22 active reasons từ read-only audit G0-06C |
| Catalog | `RC-2.0.0-proposed.1` |

Mapping này dùng để chọn candidate rule cần đánh giá. Nó không phải finding, không chứng minh
violation và không map trực tiếp sang action.

## 2. Mapping đầy đủ 22 reasons

| Intake reason runtime | Candidate rule/control | Ý nghĩa route | Disposition đề xuất |
|---|---|---|---|
| `DISLIKE_CONTENT` | `CSR.ROUTE.001` | Preference signal, không tạo violation | Giữ intake hoặc chuyển product feedback sau impact review |
| `SPAM` | `CSR.SPAM.001` | Đánh giá repetition/unsolicited/manipulation | Giữ; reconcile overlap |
| `BULLYING_OR_UNWANTED_CONTACT` | `CSR.HAR.002` | Đánh giá bullying/unwanted contact | Giữ |
| `SCAM_OR_FRAUD` | `CSR.INT.003` | Đánh giá scam/fraud | Candidate canonical scam reason |
| `HARASSMENT` | `CSR.HAR.001`, `CSR.HAR.002` | Broad reason, cần phân subtype từ evidence | Giữ hoặc split sau usage review |
| `SELF_HARM_OR_ABNORMAL_EATING` | `CSR.SAF.003` | Đánh giá encouragement/facilitation | Giữ |
| `HATE_SPEECH` | `CSR.HATE.001` | Đánh giá protected-class attack | Giữ; cần approved protected-class list |
| `VIOLENCE_HATE_OR_EXPLOITATION` | `CSR.SAF.002`, `CSR.HATE.001` | Broad mixed reason; route hai family | Candidate deprecate/split, chưa xóa |
| `VIOLENCE_THREAT` | `CSR.SAF.001` | Đánh giá credible threat | Giữ |
| `NUDITY_OR_SEXUAL_ACTIVITY` | `CSR.SEX.001` | Đánh giá nudity/explicit activity | Giữ |
| `SEXUAL_CONTENT` | `CSR.SEX.001`, `CSR.SEX.002` | Broad sexual/sensitive route | Giữ hoặc split sau review |
| `PRIVACY_VIOLATION` | `CSR.PRIV.001` | Đánh giá disclosure/misuse personal data | Giữ; cần legal scope |
| `SCAM_FRAUD_OR_SPAM` | `CSR.INT.003`, `CSR.SPAM.001` | Mixed reason; không tự biết scam hay spam | Candidate deprecate/split, chưa xóa |
| `COPYRIGHT_VIOLATION` | `CSR.IP.001` | Copyright-specific route | Giữ |
| `FALSE_INFORMATION` | `CSR.INT.002` | Materially misleading claim | Giữ; scope misinformation cần chốt |
| `INTELLECTUAL_PROPERTY` | `CSR.IP.001`, `CSR.IP.002` | Broad IP route | Giữ hoặc yêu cầu subtype |
| `IMPERSONATION` | `CSR.INT.001` | Identity/authenticity route | Giữ |
| `FAKE_OR_MISLEADING` | `CSR.INT.001`, `CSR.INT.002` | Identity hoặc deceptive representation | Giữ hoặc split |
| `INAPPROPRIATE_IMAGE` | `CSR.ROUTE.003` → rule theo observation | Image URL/reason không phải violation | Giữ routing-only; bắt buộc vision/manual handling |
| `RESTRICTED_GOODS` | `CSR.COM.001` | Restricted-commerce route | Giữ; cần approved restricted list |
| `OFF_TOPIC_OR_IRRELEVANT` | `CSR.REL.001` | Quality/relevance signal | Non-violation mặc định |
| `OTHER` | `CSR.ROUTE.002` | Description-based routing/manual triage | Giữ, bắt buộc description |

Kết quả coverage: `22/22` runtime reasons có route; không reason nào được map thẳng tới action.

## 3. Rule selection algorithm

```text
1. Đọc intake reason như claim routing hint.
2. Chọn candidate rule từ mapping đã pin theo version.
3. Kiểm tra target applicability.
4. Thu thập observation/evidence cho từng candidate rule.
5. Nếu critical evidence thiếu/unreadable → Evidence Control.
6. Đánh giá từng rule độc lập và ghi evidence references.
7. Tổng hợp findings/counter-evidence/uncertainty.
8. Tạo decision recommendation; candidate action xử lý riêng.
```

AI không được:

- thêm Rule ID ngoài catalog;
- kết luận vi phạm chỉ vì reason severity cao;
- dùng số report như fact;
- dùng recommendation/explanation cũ làm evidence độc lập;
- tự merge reason hoặc thay đổi catalog version.

## 4. Source/DB drift cần xử lý sau

- Source initializer có 9 codes trong khi runtime DB có 22.
- `FALSE_INFORMATION` và `RESTRICTED_GOODS` có field drift.
- Tất cả 22 runtime reasons đang có target `ALL`, không chứng minh chúng thực sự áp dụng mọi target.
- Label encoding/style không nhất quán.
- `SCAM_OR_FRAUD` overlap `SCAM_FRAUD_OR_SPAM`; nhiều broad reason khác cũng overlap.

G0-08 chỉ ghi disposition đề xuất. Migration/reconcile phải:

1. inventory usage và client compatibility;
2. chốt versioned intake catalog;
3. tạo alias/deprecation plan;
4. không rewrite reason snapshot lịch sử;
5. có business approval trước mutation.

## 5. Legacy AI rule codes

14 free-form code quan sát trong 23 recommendation cũ không được tự map thành canonical Rule ID.
Các giá trị như `BLOG_SCAM`, `BLOG_VIOLATION`, `CAFE_PAGE_POLICY`, `USER_SUSPENSION`
phải được lưu dưới `legacyRuleCode` cho audit.

Nếu cần migration analytics, mapping legacy phải dựa vào raw context/re-evaluation có kiểm soát;
không suy từ tên code và không backfill policy finding giả.

## 6. Open decisions chuyển G0-10

- reason nào active/deprecated/aliased;
- owner và approval authority của intake catalog;
- protected characteristic list;
- misinformation scope;
- restricted-goods list/jurisdiction;
- IP/privacy legal process;
- target applicability cuối cùng;
- effective date và migration window.
