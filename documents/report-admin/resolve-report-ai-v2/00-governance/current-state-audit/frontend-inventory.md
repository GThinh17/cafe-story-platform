# Frontend Inventory — Admin Reports

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-06B` |
| Approval | `APPROVE_G0-06B` |
| Trạng thái | `AUDITED_STATIC` |
| Ứng dụng | `5-cafe-story-nextjs-admin` |
| Ngày audit | 2026-07-23 |
| Chế độ | Static source audit; chưa đăng nhập/chạy UI trong gate này |

## 2. Contract và hành vi hiện tại

### API client

Admin FE có endpoint cho:

- tạo recommendation;
- lấy AI history;
- lấy auto-apply history;
- cancel auto-apply job.

Evidence: `src/lib/api/endpoints.ts:46-59`; `src/lib/api/admin.ts:443-479`.

Type FE mirror contract BE hiện tại gồm decision/action, confidence, risk, labels, rule, explanation, model, raw response và auto-apply job (`src/types/admin.ts:258-320`).

### Eligibility và bulk

- Chỉ report `OPEN`/`REVIEWING` được xem là eligible (`admin-reports-page.tsx:109-115`).
- Chế độ all-filtered fetch toàn bộ page rồi filter eligibility (`:300-320`).
- Chế độ selected chỉ lấy report eligible đã chọn.
- Bulk gọi tuần tự từng API recommendation, giữ thành công và ghi lỗi riêng theo report (`:322-380`).
- Auto-apply bulk yêu cầu checkbox xác nhận (`:779-829,943-949`).
- UI nói rõ recommendation đơn thuần không resolve report hoặc đổi target (`:730-733`).

### Recommendation view

Chi tiết hiện hiển thị:

- report decision và target action;
- Confidence;
- Risk;
- rule code;
- explanation;
- labels;
- model name và created time.

Evidence: `admin-reports-page.tsx:1216-1263`.

UI chưa có:

- evidence used;
- counter-evidence;
- missing evidence;
- evidence quality/sufficiency;
- violation likelihood/harm/action risk;
- critical uncertainty;
- policy/rule/schema/prompt/workflow version;
- snapshot/provenance;
- lý do automation được phép/bị block dưới dạng có cấu trúc.

## 3. Finding register

| ID | Mức | Class | Finding và evidence | Liên hệ nền tảng đã duyệt |
|---|---|---|---|---|
| `FE-001` | Thông tin | `CURRENT` | FE dùng đúng các endpoint BE cho recommendation/history/auto-job/cancel. `endpoints.ts:46-59`; `admin.ts:443-479` | Contract wiring hiện hữu |
| `FE-002` | Thông tin | `CURRENT` | Mọi path bulk/selection chỉ đưa `OPEN`/`REVIEWING` vào Ask AI. `admin-reports-page.tsx:109-115,300-320,844-876` | Phù hợp backend eligibility |
| `FE-003` | Thông tin | `CURRENT` | UI phân biệt tạo recommendation với resolve/mutation và yêu cầu xác nhận riêng khi bật bulk auto-apply. `admin-reports-page.tsx:730-733,779-829,943-949` | Phù hợp `HI1`, `AR1` |
| `FE-004` | Trung bình | `CURRENT` | Bulk là client-side sequential loop, có failure theo report và giữ success. `admin-reports-page.tsx:322-380,918-930` | Phù hợp một phần `AR7`, `AR12` |
| `FE-005` | Trung bình | `CONFLICT` | `Success` đếm recommendation API thành công, trong khi `Skipped` đếm auto-apply warning; một item có thể đồng thời nằm ở cả hai nhưng UI không nói rõ scope. `admin-reports-page.tsx:361-366,680-685,889-897` | Dễ gây hiểu sai outcome; cần semantics per-stage |
| `FE-006` | Cao | `CONFLICT` | Copy “Only high-confidence recommendations are scheduled” khiến confidence trông như safety criterion chính, trong khi chưa có evidence sufficiency/critical uncertainty. `admin-reports-page.tsx:791-797` | Trái tinh thần `U3`, `U7`, `AR8` |
| `FE-007` | Cao | `MISSING` | UI không hiển thị evidence, counter-evidence hoặc missing evidence. `admin-reports-page.tsx:1216-1263` | Chưa đạt `HI6`, `XA3`, `XA4` |
| `FE-008` | Cao | `LEGACY` | UI dùng nhãn generic `Risk`, không nói đây là violation likelihood, harm hay action risk. `admin-reports-page.tsx:1222-1233,1395-1396` | Trái `R1`, `R8` |
| `FE-009` | Trung bình | `MISSING` | UI không cho biết score chưa calibrated, đối tượng mà confidence mô tả hoặc critical uncertainty. `admin-reports-page.tsx:1222-1233` | Thiếu `U1`, `U2`, `U5`, `U6` |
| `FE-010` | Cao | `MISSING` | Không hiển thị policy/rule/schema/model/prompt/workflow version hoặc target snapshot. `admin-reports-page.tsx:1236-1262`; types `admin.ts:280-320` | Thiếu `AR9`, `XA6`, `XA9` |
| `FE-011` | Trung bình | `LEGACY` | Client nhận `rawResponse` dù UI không hiển thị; mở rộng exposure không cần thiết cho view hiện tại. `src/types/admin.ts:280-296`; BE response tương ứng | Cần xem lại theo `XA8` |
| `FE-012` | Thông tin | `CURRENT` | Playwright source định nghĩa 30 scenario, gồm 4 target, contract, auto-apply/cancel, bulk, security, performance và cleanup. `tests/e2e/admin-report-ai.spec.ts:172-201,719-1251` | Test harness hiện hữu |
| `FE-013` | Trung bình | `UNKNOWN` | Gate này không chạy UI/E2E; source test tồn tại không chứng minh checkout/runtime hiện tại pass. Prior run chỉ là historical evidence. | Cần runtime gate riêng, không suy diễn |

## 4. Nhận xét UI

UI hiện khá tốt ở operational workflow: eligibility, bulk progress, lỗi từng item, confirm auto-apply, countdown/cancel và history. Phần yếu nằm ở decision support:

- Admin thấy “kết luận + score + explanation”, nhưng không thấy căn cứ;
- “Risk” và “Confidence” thiếu định nghĩa;
- explanation có thể tạo automation bias vì không gắn evidence;
- auto-apply copy không phản ánh đầy đủ safety gate;
- outcome bulk trộn recommendation result với scheduling result.

Những thay đổi UI chỉ được thiết kế/implement sau policy framework, rule catalog và canonical contract; chưa sửa trong `G0-06B`.
