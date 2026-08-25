# Evidence Hierarchy and Burden of Proof — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05C` |
| Ngày cập nhật | 2026-07-23 |
| Tài liệu liên quan | `claim-fact-evidence.md`, `12-decisions/glossary.md` |

> Tài liệu này thiết lập mô hình đánh giá evidence và burden of proof. Nó chưa định nghĩa threshold bằng số, minimum evidence theo rule hoặc quyền auto-apply.

## 2. Mục tiêu

Evidence hierarchy giúp hệ thống:

- không coi mọi input là đáng tin như nhau;
- không biến số lượng claim thành proof;
- đánh giá evidence theo điều nó thực sự chứng minh;
- tăng burden of proof theo rủi ro action;
- giải thích vì sao evidence bundle đủ hoặc chưa đủ;
- giữ traceability từ action về nguồn dữ liệu.

## 3. Không có bảng xếp hạng tuyệt đối

Evidence strength phụ thuộc claim, rule và field đang cần chứng minh.

Ví dụ:

- database status là nguồn mạnh cho trạng thái được lưu tại một thời điểm;
- database status không chứng minh ý định của user;
- Admin decision là nguồn mạnh cho việc decision đã xảy ra;
- Admin decision không tự chứng minh target hiện tại vẫn vi phạm;
- target snapshot là nguồn trực tiếp cho nội dung đã capture;
- snapshot bị cắt context có thể không đủ cho policy finding.

Vì vậy, tier chỉ là định hướng trust và directness, không thay thế đánh giá claim-specific fitness.

## 4. Các chiều đánh giá evidence

### 4.1. Provenance

Có xác định được nguồn, phương thức thu thập và chuỗi xử lý hay không.

### 4.2. Integrity

Evidence có version, hash, immutable reference hoặc cơ chế phát hiện thay đổi hay không.

### 4.3. Directness

Evidence trực tiếp thể hiện hành vi cần đánh giá hay chỉ là lời kể, tổng hợp hoặc suy luận.

### 4.4. Relevance

Evidence có liên quan đúng policy clause, rule và target hay không.

### 4.5. Freshness

Evidence có còn phản ánh target ở thời điểm recommendation/action hay đã stale.

### 4.6. Context completeness

Evidence có đủ parent thread, surrounding text, metadata, media hoặc temporal context cần thiết hay không.

### 4.7. Independence

Evidence có nguồn độc lập hay nhiều item chỉ sao chép cùng một claim/output.

### 4.8. Verifiability

Admin hoặc hệ thống có thể truy xuất và kiểm tra lại evidence gốc hay không.

### 4.9. Privacy and admissibility

Evidence có được phép sử dụng, lưu giữ và hiển thị cho vai trò hiện tại hay không. Evidence có nội dung liên quan nhưng vi phạm privacy/access control không tự trở thành admissible evidence.

## 5. Evidence hierarchy định hướng

| Tier | Loại dữ liệu | Giá trị chính | Giới hạn |
|---|---|---|---|
| `T1` | Backend snapshot, entity state, system event có ID/version/timestamp | Trực tiếp và có provenance mạnh cho field được lưu | Có thể stale; không chứng minh intent |
| `T2` | First-party content/media được hệ thống capture | Evidence chính về nội dung target | Có thể thiếu context hoặc media interpretation |
| `T3` | Deterministic extraction hoặc verified tool result | Làm evidence dễ sử dụng/kiểm tra hơn | Phải truy về source; tool có error model |
| `T4` | Human-reviewed record hoặc Admin decision | Chứng minh review/decision đã xảy ra | Không tự chứng minh trạng thái hiện tại |
| `T5` | AI observation, inference hoặc recommendation | Derived analysis hỗ trợ review | Không phải primary evidence |
| `T6` | Report count, anomaly, keyword hit, lịch sử signal | Điều hướng và ưu tiên investigation | Không chứng minh violation |
| `T7` | Reporter reason và description | Claim cần kiểm tra | Có thể chủ quan, sai hoặc bị lạm dụng |

### 5.1. Tier không phải score

Tier không được chuyển máy móc thành điểm, ví dụ `T1 = 100`, `T7 = 10`. Một T1 snapshot không liên quan vẫn có relevance bằng không đối với policy finding đang xét.

### 5.2. Derived evidence

Kết quả OCR, parser hoặc normalization chỉ giữ được trust khi:

- source evidence còn truy xuất được;
- tool/version được ghi lại;
- lỗi extraction có thể kiểm tra;
- output không bị trình bày như primary source.

## 6. Quy tắc cộng gộp evidence

Không được giả định:

```text
N evidence yếu = một evidence mạnh
```

Evidence bundle được củng cố khi các item:

- có nguồn thực sự độc lập;
- cùng hỗ trợ một claim hoặc policy finding;
- có provenance rõ;
- không chỉ lặp lại AI output hoặc reporter claim;
- bổ sung context cho nhau;
- không có contradiction chưa được xử lý.

### 6.1. Correlated evidence

Nhiều report dùng cùng một ảnh, cùng một nội dung copy hoặc cùng được tạo bởi coordinated campaign phải được xem là correlated, không phải N nguồn độc lập.

### 6.2. Circular evidence

AI recommendation cũ không được đưa vào model mới như evidence để tự củng cố kết luận. Nó chỉ có thể là historical analysis để so sánh/audit.

## 7. Burden of proof

Burden of proof là mức evidence tối thiểu cần đạt trước một decision hoặc action. Đây là tiêu chuẩn nội bộ của nền tảng, không phải tuyên bố sử dụng tiêu chuẩn pháp lý.

### 7.1. Các mức định hướng

| Mức | Ý nghĩa | Kết quả/action định hướng |
|---|---|---|
| `BP-0 NOT_MET` | Evidence thiếu, không liên quan hoặc mâu thuẫn nghiêm trọng | `NEEDS_MANUAL_REVIEW + NO_ACTION` |
| `BP-1 REVIEW_THRESHOLD` | Có dấu hiệu hợp lý để ưu tiên điều tra | Manual review hoặc escalation |
| `BP-2 REVERSIBLE_ACTION` | Evidence đủ cho action có khả năng phục hồi | Có thể xem xét `HIDE` |
| `BP-3 DESTRUCTIVE_CONTENT_ACTION` | Evidence mạnh, rule rõ, counter-evidence đã xử lý | Có thể xem xét `REMOVE` |
| `BP-4 ACCOUNT_RESTRICTION` | Evidence rất mạnh và phạm vi đánh giá phù hợp | Có thể xem xét `SUSPEND_USER/PAGE` |

Mức burden chưa cấp quyền auto-apply. Human-in-the-loop, action matrix và automation policy vẫn áp dụng độc lập.

### 7.2. Burden theo finding và action

Burden không nên được đánh giá một lần cho toàn report. Một evidence bundle có thể:

- đủ cho một spam finding;
- chưa đủ cho fraud finding;
- đủ cho `HIDE`;
- chưa đủ cho `REMOVE`;
- hoàn toàn chưa đủ cho `SUSPEND_USER`.

Canonical representation định hướng:

```text
evidenceSufficiency: INSUFFICIENT | PARTIAL | SUFFICIENT
burdenOfProofMetFor: [action...]
burdenOfProofNotMetFor: [action...]
```

## 8. Điều kiện evidence sufficiency

Một finding/action chỉ được xem là sufficient khi:

- minimum evidence của rule được đáp ứng;
- evidence reference hợp lệ và truy xuất được;
- provenance/integrity đáp ứng yêu cầu;
- không thiếu critical context;
- counter-evidence đã được xem xét;
- target snapshot còn current hoặc đã revalidate;
- policy/rule version được hỗ trợ;
- evidence đủ direct và relevant;
- burden of proof phù hợp action;
- conclusion không phụ thuộc duy nhất vào AI explanation, AI output cũ hoặc reporter claim.

### 8.1. Critical missing evidence

Critical missing evidence làm burden không đạt bất kể confidence tự báo cáo của model cao đến đâu.

Ví dụ:

- thiếu parent comment làm thay đổi ý nghĩa;
- ảnh là nội dung chính nhưng pipeline không đọc được ảnh;
- target đã chỉnh sửa sau snapshot;
- URL external là trọng tâm nhưng chưa xác minh được;
- account suspension dựa vào lịch sử vi phạm nhưng lịch sử chưa được lấy.

## 9. Ví dụ đánh giá bundle

```text
EV-001: Backend snapshot có câu yêu cầu chuyển tiền — T1/T2
EV-002: 20 report cùng reason SCAM — T6/T7
EV-003: AI recommendation cũ nói SCAM — T5
```

Nếu EV-001 thiếu đoạn trước và sau:

- provenance và integrity của snapshot có thể cao;
- context completeness thấp;
- EV-002 chỉ tăng ưu tiên investigation;
- EV-003 không bổ sung primary evidence;
- evidence sufficiency có thể là `PARTIAL`;
- burden cho `REMOVE` hoặc `SUSPEND_USER` chưa đạt.

## 10. REJECT và burden of proof

`REJECT` không có nghĩa “không tìm thấy evidence nên mặc định không vi phạm”. Trước khi `REJECT`, hệ thống phải có đủ cơ sở trong phạm vi evidence có thể quan sát để kết luận:

- claim không được evidence hỗ trợ; hoặc
- evidence trực tiếp cho thấy context không vi phạm; hoặc
- report không áp dụng đúng policy/target; hoặc
- claim bị contradiction đáng tin cậy phản bác.

Nếu critical evidence không thể truy xuất, decision phải là `NEEDS_MANUAL_REVIEW`, không phải `REJECT`.

## 11. Quan hệ với auto-apply

Evidence sufficiency là điều kiện cần nhưng chưa đủ cho auto-apply. Auto-apply còn cần:

- action nằm trong allowlist;
- action risk đáp ứng policy;
- target được revalidate;
- model/prompt/policy version được phê duyệt;
- không có blocker hoặc safety exception;
- idempotency và audit hoạt động;
- Human-in-the-loop policy cho phép.

Tài liệu này không phê duyệt bất kỳ action auto-apply nào.

## 12. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `H1` | Evidence strength phải đánh giá theo claim/rule cụ thể, không có bảng xếp hạng tuyệt đối | `APPROVED` |
| `H2` | Backend snapshot và system fact có provenance là nguồn mạnh nhất cho field chúng trực tiếp chứng minh | `APPROVED` |
| `H3` | Reporter claim, report count và anomaly chỉ là investigation signal nếu chưa có evidence độc lập | `APPROVED` |
| `H4` | AI output là derived analysis, không phải primary evidence | `APPROVED` |
| `H5` | Nhiều evidence yếu không tự động trở thành evidence sufficient | `APPROVED` |
| `H6` | Burden of proof tăng theo mức khó hoàn tác và phạm vi ảnh hưởng của action | `APPROVED` |
| `H7` | Evidence sufficiency phải đánh giá theo từng policy finding và action | `APPROVED` |
| `H8` | Counter-evidence và critical missing evidence phải được xét trước khi kết luận sufficient | `APPROVED` |
| `H9` | Chưa chốt threshold bằng số trước policy framework và evaluation data | `APPROVED` |

## 13. Giới hạn của tài liệu

Tài liệu chưa chốt:

- minimum evidence theo từng rule;
- score weights;
- numeric thresholds;
- temporary safeguard;
- auto-apply allowlist;
- external verification tool;
- target-specific evidence collection.

Các nội dung này thuộc policy, rule, evidence standard, automation và target-evidence gates tiếp theo.

