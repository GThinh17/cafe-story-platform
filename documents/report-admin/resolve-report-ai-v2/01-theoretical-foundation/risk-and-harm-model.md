# Risk and Harm Model — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05E` |
| Ngày cập nhật | 2026-07-23 |
| Tài liệu liên quan | `moderation-model.md`, `uncertainty-and-confidence.md`, `12-decisions/glossary.md` |

> Tài liệu này định nghĩa các chiều risk/harm. Nó chưa chốt công thức, threshold số, action matrix hoặc quyền auto-apply.

## 2. Mục tiêu

Mô hình phải ngăn việc sử dụng một `riskScore` mơ hồ để đồng thời biểu diễn:

- khả năng target vi phạm;
- mức harm nếu vi phạm là thật;
- mức rủi ro khi thực hiện action;
- confidence của model;
- quality của evidence.

Các khái niệm này phải được tách để Admin, Backend và test có thể hiểu chính xác điều mỗi score đang nói tới.

## 3. Ba câu hỏi độc lập

| Khái niệm | Câu hỏi |
|---|---|
| `Violation likelihood` | Target có khả năng vi phạm policy đến đâu? |
| `Harm severity` | Nếu violation là thật thì hậu quả nghiêm trọng đến đâu? |
| `Action risk` | Nếu thực hiện action này thì nguy cơ xử lý sai hoặc gây hậu quả phụ lớn đến đâu? |

Canonical fields định hướng:

```text
violationLikelihoodScore: 0..100
harmSeverityScore: 0..100
actionRiskScore: 0..100, theo từng candidate action
```

Field và range chính thức vẫn phải được domain/AI contract chốt.

## 4. Harm severity là conditional harm

`harmSeverityScore` trả lời:

> Hậu quả có thể nghiêm trọng đến đâu nếu policy finding là đúng?

Nó không trả lời:

> Policy finding có đúng hay không?

Ví dụ một lời đe dọa bạo lực có harm tiềm năng rất cao. Nhưng nếu câu đó nằm trong bài báo hoặc nội dung cảnh báo, violation likelihood có thể thấp.

```text
harmSeverityScore: HIGH
violationLikelihoodScore: LOW hoặc UNCERTAIN
```

Không được dùng harm cao để bypass evidence hoặc kết luận violation.

## 5. Các chiều của content harm

### 5.1. Severity

Mức thiệt hại nếu hành vi xảy ra hoặc tiếp tục xảy ra.

### 5.2. Immediacy

Nguy cơ xảy ra ngay, trong thời gian ngắn hay dài hạn.

### 5.3. Reach

Số lượng người hoặc đối tượng có thể bị ảnh hưởng.

### 5.4. Vulnerability

Mức dễ tổn thương của nhóm bị ảnh hưởng.

### 5.5. Persistence

Harm có tồn tại lâu dài hoặc tiếp tục lan truyền hay không.

### 5.6. Repeatability

Hành vi có thể được lặp lại, nhân rộng hoặc tự động hóa hay không.

### 5.7. Recoverability

Người bị ảnh hưởng có thể phục hồi dữ liệu, tài sản, quyền truy cập hoặc danh tiếng hay không.

### 5.8. Systemic impact

Hành vi có làm suy yếu niềm tin, integrity hoặc an toàn của toàn nền tảng hay không.

Không phải rule nào cũng cần mọi chiều. Policy framework phải chỉ rõ chiều nào phù hợp với từng policy category.

## 6. Action risk theo từng candidate action

Cùng một evidence bundle có thể có action risk khác nhau:

| Candidate action | Action risk định hướng | Lý do |
|---|---:|---|
| `NO_ACTION` | Thấp đối với target | Không mutation, nhưng có thể chứa false-negative cost |
| `KEEP_VISIBLE` | Thấp đối với target | Không enforcement, nhưng content harm có thể tiếp tục nếu decision sai |
| `KEEP_ACTIVE` | Thấp đối với target | Không account restriction |
| `HIDE` | Trung bình | Có tác động nhưng thường có thể rollback |
| `REMOVE` | Cao | Có thể mất nội dung hoặc khó phục hồi |
| `SUSPEND_USER` | Rất cao | Tác động toàn tài khoản và nhiều nội dung |
| `SUSPEND_PAGE` | Rất cao | Tác động chủ sở hữu, follower và hoạt động kinh doanh |

Không được tạo một `actionRiskScore` chung nếu nhiều candidate action đang được cân nhắc.

## 7. Các chiều của action risk

- likelihood của false positive;
- mức ảnh hưởng của action;
- reversibility và rollback fidelity;
- thời gian phát hiện xử lý sai;
- blast radius;
- khả năng appeal;
- evidence sufficiency;
- critical uncertainty;
- target freshness;
- độ rõ của policy/rule;
- collateral consequences;
- quyền hạn của actor thực thi.

### 7.1. Reversibility

Action được gọi là reversible chỉ khi hệ thống có thể phục hồi đầy đủ trạng thái quan trọng, không chỉ đổi một status flag.

Ví dụ cần xem xét:

- nội dung có phục hồi nguyên vẹn không;
- notification/visibility side effect có hoàn tác được không;
- follower hoặc access state có bị mất không;
- appeal có thể khôi phục trong thời gian hợp lý không.

## 8. Expected content harm và enforcement harm

Hệ thống phải cân nhắc hai phía:

```text
Expected content harm
= harm nếu không xử lý một violation thật

Expected enforcement harm
= harm nếu xử lý nhầm hoặc xử lý quá mức
```

Không áp dụng công thức nhân số cứng trong Gate 0 vì:

- score chưa calibration;
- các chiều không độc lập;
- harm có thể phi tuyến;
- action effect khác nhau theo target;
- false positive và false negative có cost khó quy về cùng một đơn vị.

Giai đoạn đầu dùng reasoning có cấu trúc, policy matrix và human review.

## 9. Ma trận định hướng

| Violation likelihood | Harm severity | Action risk | Hướng xử lý định hướng |
|---|---|---|---|
| Cao | Cao | Thấp–trung bình | Recommendation mạnh, vẫn theo Human-in-the-loop policy |
| Cao | Thấp | Cao | Chọn action nhẹ hơn hoặc manual review |
| Thấp/không chắc | Cao | Cao | Urgent manual review, không tự trừng phạt |
| Thấp | Thấp | Thấp | Có thể `REJECT` nếu evidence đủ |
| Không xác định | Bất kỳ | Bất kỳ | `NEEDS_MANUAL_REVIEW + NO_ACTION` |

Ma trận này chưa phải decision/action policy và không chứa threshold.

## 10. Ví dụ

### 10.1. Spam rõ ràng

```text
violationLikelihoodScore: 98
harmSeverityScore: 20
actionRisk(HIDE): 10
actionRisk(SUSPEND_USER): 75
```

Evidence có thể đủ để xử lý nội dung nhưng chưa đủ để hạn chế toàn tài khoản.

### 10.2. Dấu hiệu scam thiếu context

```text
violationLikelihoodScore: 65
harmSeverityScore: 85
evidenceSufficiency: PARTIAL
actionRisk(REMOVE): 70
```

Hướng xử lý:

```text
NEEDS_MANUAL_REVIEW
Urgent escalation
NO_ACTION
```

Harm cao không bypass thiếu evidence.

### 10.3. Nội dung vi phạm nhẹ nhưng account action quá mạnh

```text
Policy finding: SPAM_SUPPORTED
Evidence: SUFFICIENT cho content action
Action risk(HIDE): LOW
Action risk(SUSPEND_USER): HIGH
```

Recommendation phải giới hạn ở action được evidence và policy hỗ trợ, không tự leo thang lên account restriction.

## 11. Reporter-provided severity

Severity do reporter chọn:

- là claim hoặc investigation signal;
- có thể dùng để ưu tiên queue;
- không trực tiếp trở thành `harmSeverityScore`;
- không tự tăng action severity;
- phải được đánh giá lại từ evidence, context và policy finding;
- có thể bị abuse trong coordinated reporting.

## 12. Quan hệ với uncertainty và evidence

Risk/harm assessment phải được đọc cùng:

```text
assessmentConfidenceScore
violationLikelihoodScore
harmSeverityScore
actionRiskScore per action
evidenceQualityScore
evidenceSufficiency
uncertaintyFactors
```

Các field không được nén trở lại thành một generic score trong UI hoặc Backend rule.

## 13. Vai trò Backend và AI

AI có thể đề xuất:

- harm dimensions;
- candidate action risks;
- uncertainty factors;
- explanation và evidence references.

Backend phải là source of truth cho:

- action có được policy cho phép không;
- burden of proof đã đạt chưa;
- candidate action có nằm trong allowlist không;
- target có còn current không;
- action có cần Admin xác nhận không;
- auto-apply eligibility;
- semantic consistency giữa finding, scores và action.

## 14. Legacy riskScore

Generic `riskScore` bị deprecate vì không xác định rõ đang biểu diễn violation, harm hay enforcement risk.

Migration principle:

- không dùng generic `riskScore` trong canonical V2 contract;
- legacy field phải được gắn tên/metadata `legacyRiskScore`;
- không dùng legacy field làm automation gate duy nhất;
- mapping phải có version và audit;
- loại bỏ sau compatibility period được phê duyệt.

## 15. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `R1` | Tách violation likelihood, harm severity và action risk | `APPROVED` |
| `R2` | Harm severity là hậu quả có điều kiện nếu violation là thật; không chứng minh violation | `APPROVED` |
| `R3` | Reporter severity chỉ là claim/signal, không phải harm score chính thức | `APPROVED` |
| `R4` | Action risk phải đánh giá riêng cho từng candidate action | `APPROVED` |
| `R5` | Harm cao nhưng evidence chưa đủ phải urgent human review, không tự trừng phạt | `APPROVED` |
| `R6` | Harm assessment phải xét immediacy, reach, vulnerability và recoverability | `APPROVED` |
| `R7` | Action khó rollback hoặc blast radius lớn phải có action risk và burden cao hơn | `APPROVED` |
| `R8` | Generic `riskScore` không tồn tại trong canonical V2 contract | `APPROVED` |
| `R9` | Chưa dùng công thức hoặc threshold số trước policy/evaluation data | `APPROVED` |
| `R10` | Backend là source of truth cho action eligibility; AI cung cấp recommendation và risk factors | `APPROVED` |

## 16. Giới hạn của tài liệu

Tài liệu chưa chốt:

- numeric thresholds;
- risk aggregation formula;
- policy-specific harm dimensions;
- action allowlist;
- temporary safeguard;
- UI visualization;
- auto-apply policy;
- calibration dataset.

Các nội dung này thuộc policy, rule, system design, automation và verification gates tiếp theo.

