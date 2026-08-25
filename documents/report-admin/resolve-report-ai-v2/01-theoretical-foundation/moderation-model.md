# Moderation Model — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05A` |
| Ngày cập nhật | 2026-07-23 |
| Thuật ngữ chuẩn | `12-decisions/glossary.md` |

> Tài liệu này mô tả mô hình ra quyết định. Nó không tự định nghĩa hành vi nào là vi phạm và không cấp quyền auto-apply cho bất kỳ action nào.

## 2. Bài toán moderation

Resolve Report with AI không phải bài toán phân loại nhị phân đơn giản. Hệ thống phải ra recommendation khi:

- claim của reporter có thể sai, thiếu hoặc bị lạm dụng;
- target snapshot có thể thiếu context hoặc đã thay đổi;
- evidence có nhiều mức quality và sufficiency;
- false positive và false negative gây hậu quả khác nhau;
- action có mức độ hoàn tác khác nhau;
- policy và rule có thể thay đổi theo version;
- model luôn có uncertainty và giới hạn nhận thức.

Mục tiêu không phải tối đa hóa một con số accuracy tổng. Mục tiêu là giảm expected harm trong khi bảo vệ tính nhất quán, khả năng giải thích và quyền review của con người.

## 3. Đơn vị quyết định

Hệ thống phải tách ba lớp:

```text
Policy finding
→ Report decision
→ Target action
```

- `Policy finding`: evidence hỗ trợ rule nào và ở mức nào.
- `Report decision`: `RESOLVE`, `REJECT` hoặc `NEEDS_MANUAL_REVIEW`.
- `Target action`: `NO_ACTION`, `KEEP_VISIBLE`, `KEEP_ACTIVE`, `HIDE`, `REMOVE`, `SUSPEND_USER` hoặc `SUSPEND_PAGE`.

Một report decision không tự động cấp quyền thực hiện target action. Action còn phụ thuộc burden of proof, action risk, quyền hạn và revalidation.

## 4. Không gian kết quả

### 4.1. Confusion matrix cơ bản

| Thực tế theo policy và evidence đầy đủ | Hệ thống xử lý vi phạm | Hệ thống không xử lý vi phạm |
|---|---|---|
| Target có vi phạm | True positive | False negative |
| Target không vi phạm | False positive | True negative |

### 4.2. Abstention

`NEEDS_MANUAL_REVIEW` là abstention có kiểm soát khi hệ thống không đủ cơ sở để đưa ra kết luận đáng tin cậy.

Abstention:

- không tự động là false positive;
- không tự động là false negative;
- phải được đo bằng manual-review rate và review outcome;
- không được dùng để che giấu lỗi contract hoặc lỗi hạ tầng;
- là kết quả hợp lệ khi evidence thiếu, mâu thuẫn hoặc không truy xuất được.

## 5. False positive

False positive xảy ra khi target không vi phạm nhưng hệ thống kết luận hoặc thực thi như có vi phạm.

Ví dụ:

- bài viết cảnh báo lừa đảo bị hiểu nhầm là lời mời lừa đảo;
- nội dung trích dẫn hate speech để phản biện bị xử lý như hate speech trực tiếp;
- user bị suspend chỉ vì nhận nhiều report phối hợp;
- comment bị remove khi thiếu context của parent thread.

Hậu quả có thể gồm:

- xử lý nhầm người hoặc nội dung hợp lệ;
- mất dữ liệu và quyền tiếp cận;
- mất niềm tin vào moderation;
- phát sinh appeal, rollback và chi phí vận hành;
- tạo chilling effect khiến user ngại đăng nội dung hợp lệ.

False-positive cost tăng mạnh theo độ khó hoàn tác và phạm vi tác động của action.

## 6. False negative

False negative xảy ra khi target thật sự vi phạm nhưng hệ thống không nhận ra hoặc không xử lý phù hợp.

Ví dụ:

- nội dung lừa đảo rõ ràng được giữ hiển thị;
- hành vi quấy rối lặp lại bị xem là một câu nói đơn lẻ;
- cafe page giả mạo tiếp tục hoạt động;
- report nguy hiểm bị `REJECT` chỉ vì pipeline thiếu evidence collector.

Hậu quả có thể gồm:

- tiếp tục gây hại cho user và cộng đồng;
- lan rộng nội dung hoặc hành vi vi phạm;
- giảm niềm tin vào khả năng bảo vệ của nền tảng;
- tăng chi phí xử lý sự cố về sau.

Với harm cao nhưng evidence chưa đủ, giải pháp an toàn mặc định là urgent human escalation, không phải tự động trừng phạt.

## 7. Cost-sensitive moderation

Không tồn tại một ưu tiên global luôn đúng giữa false positive và false negative. Chi phí phải được đánh giá theo:

- policy category;
- harm severity;
- target type;
- phạm vi người bị ảnh hưởng;
- reversibility của action;
- thời gian phát hiện và rollback;
- khả năng thu thập thêm evidence;
- quyền appeal của đối tượng bị xử lý.

### 7.1. Burden of proof theo action

| Action | False-positive cost tương đối | Reversibility | Burden of proof định hướng |
|---|---:|---:|---:|
| `NO_ACTION` | Thấp đối với target, nhưng có thể chứa false-negative cost | Toàn phần | Dùng khi inconclusive |
| `KEEP_VISIBLE` | Thấp đối với target | Toàn phần | Cần đủ cơ sở cho `REJECT` |
| `KEEP_ACTIVE` | Thấp đối với target | Toàn phần | Cần đủ cơ sở cho `REJECT` |
| `HIDE` | Trung bình | Cao | Trung bình đến cao |
| `REMOVE` | Cao | Phụ thuộc hệ thống | Cao |
| `SUSPEND_USER` | Rất cao | Phụ thuộc hệ thống | Rất cao và mặc định cần Admin |
| `SUSPEND_PAGE` | Rất cao | Phụ thuộc hệ thống | Rất cao và mặc định cần Admin |

Bảng này là định hướng lý thuyết. Ngưỡng chính thức phải được policy framework phê duyệt, không được tự suy ra từ bảng.

## 8. Decision flow

```text
Nhận report và target snapshot
→ Phân biệt claim với evidence
→ Kiểm tra provenance, quality và missing evidence
→ Tìm observation và counter-evidence
→ Ánh xạ policy finding
→ Đánh giá uncertainty
→ Evidence không đủ hoặc mâu thuẫn?
   ├─ Có → NEEDS_MANUAL_REVIEW + NO_ACTION
   └─ Không
      → Evidence hỗ trợ không vi phạm?
         ├─ Có → REJECT + KEEP_VISIBLE/KEEP_ACTIVE
         └─ Không
            → Evidence hỗ trợ vi phạm
            → RESOLVE
            → Kiểm tra burden of proof theo action
            → Human review hoặc approved safe automation
```

Decision flow phải fail-safe khi:

- target không tồn tại;
- snapshot không còn current;
- evidence reference không hợp lệ;
- score và decision mâu thuẫn;
- policy/rule version không được hỗ trợ;
- model output sai schema hoặc semantic contract.

## 9. Vai trò của report count

Số lượng report có thể dùng để:

- ưu tiên thứ tự điều tra;
- phát hiện xu hướng hoặc incident;
- quyết định cần thu thập thêm evidence;
- phát hiện coordinated behavior sau khi có signal bổ sung.

Số lượng report không được:

- tự chứng minh target vi phạm;
- thay thế target evidence;
- tự tăng violation likelihood đến mức đủ `RESOLVE`;
- tự cấp quyền `REMOVE` hoặc `SUSPEND`;
- bỏ qua khả năng coordinated mass-report abuse.

## 10. Urgent escalation

Khi harm tiềm năng cao nhưng evidence chưa đủ:

1. không tự `REJECT`;
2. không tự thực hiện destructive action;
3. đánh dấu ưu tiên review cao;
4. nêu rõ missing evidence và lý do khẩn cấp;
5. chuyển người có quyền xem xét trong thời gian phù hợp.

Tên trạng thái, SLA và quyền temporary safeguard chưa được tài liệu này phê duyệt; chúng phải được quyết định trong policy framework và business decision gate.

## 11. Evaluation model

### 11.1. Metrics bắt buộc

- precision và recall theo từng rule;
- false-positive rate;
- false-negative rate;
- manual-review/abstention rate;
- Admin agreement và override rate;
- evidence citation accuracy;
- policy mapping accuracy;
- confidence calibration;
- distribution theo target type, action và policy version.

### 11.2. Metrics không đủ khi đứng một mình

- accuracy tổng;
- số report đã xử lý;
- số recommendation có confidence cao;
- tỷ lệ auto-apply;
- latency trung bình.

Các metric này có thể che giấu lỗi hiếm nhưng có hậu quả cao.

## 12. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `M1` | Không đặt một ưu tiên global giữa false positive và false negative; chi phí phụ thuộc rule và action | `APPROVED` |
| `M2` | Với destructive action, ưu tiên mạnh việc giảm false positive | `APPROVED` |
| `M3` | Với harm cao nhưng evidence chưa đủ, dùng urgent human escalation thay vì tự động trừng phạt | `APPROVED` |
| `M4` | `NEEDS_MANUAL_REVIEW` là kết quả hợp lệ, không phải mặc định là lỗi AI | `APPROVED` |
| `M5` | Đánh giá chất lượng theo rule/action và error type, không chỉ accuracy tổng | `APPROVED` |

## 13. Giới hạn của tài liệu

Tài liệu chưa chốt:

- hành vi cụ thể nào thuộc từng policy category;
- ngưỡng score hoặc burden of proof bằng số;
- action nào được auto-apply;
- SLA urgent escalation;
- target-specific evidence collector;
- temporary safeguard và appeal workflow.

Các nội dung này phải được xử lý ở policy, rule, evidence, automation và business decision gates tương ứng.

