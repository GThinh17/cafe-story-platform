# Glossary — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Canonical business and system vocabulary |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-04`, `APPROVE_G0-04-DECISIONS` |
| Ngày cập nhật | 2026-07-23 |

> Glossary định nghĩa semantics mục tiêu của V2. Nó không khẳng định source hiện tại đã tuân thủ và không tự cấp quyền thay đổi source code.

## 2. Quy tắc sử dụng thuật ngữ

1. FE, BE, n8n, prompt, tài liệu và test phải dùng cùng một ý nghĩa cho một thuật ngữ.
2. Không được đổi nghĩa của thuật ngữ theo từng target type nếu chưa có qualifier rõ ràng.
3. Thuật ngữ legacy phải được đánh dấu và chỉ tồn tại ở compatibility boundary.
4. Tên field có chữ `probability` chỉ được dùng khi score đã được calibration phù hợp.
5. AI explanation không được dùng thay cho evidence reference.

## 3. Claim, fact và evidence

### 3.1. `Claim`

Khẳng định do reporter hoặc một nguồn chưa được xác minh cung cấp. Claim là đầu vào cần điều tra, không phải fact.

Ví dụ: “Tài khoản này lừa đảo” là claim cho đến khi có evidence hỗ trợ.

### 3.2. `Fact`

Thông tin đã được xác minh theo nguồn và quy trình xác định. Fact không có nghĩa là chân lý tuyệt đối; hồ sơ phải cho biết ai hoặc hệ thống nào đã xác minh, vào thời điểm nào.

### 3.3. `Evidence`

Dữ liệu có nguồn gốc có thể dùng để hỗ trợ hoặc phản bác claim. Evidence phải có reference và provenance đủ để kiểm tra lại.

### 3.4. `Observation`

Điều được đọc hoặc trích xuất trực tiếp từ evidence, chưa chứa kết luận policy.

Ví dụ: “Nội dung có câu ‘chuyển 500.000 đồng để nhận mã’.”

### 3.5. `Inference`

Suy luận từ một hoặc nhiều observation. Inference có thể sai và phải biểu diễn uncertainty.

### 3.6. `Policy Finding`

Kết quả ánh xạ evidence và inference vào một policy clause hoặc rule cụ thể. Policy finding phải truy ngược được về evidence ID và rule version.

### 3.7. `Evidence Reference`

ID hoặc vị trí ổn định giúp truy ngược đến evidence gốc, ví dụ `EV-001` hoặc target snapshot field cụ thể.

### 3.8. `Evidence Provenance`

Nguồn, thời điểm, phương thức thu thập, trạng thái xác minh và chuỗi biến đổi của evidence.

### 3.9. `Counter-evidence`

Evidence chống lại hoặc làm suy yếu kết luận đang được xem xét. Hệ thống phải tìm và hiển thị counter-evidence thay vì chỉ xác nhận claim của reporter.

### 3.10. `Missing Evidence`

Dữ liệu quan trọng còn thiếu để đáp ứng burden of proof. Missing evidence không được tự suy thành “không có vi phạm”.

### 3.11. `Target Snapshot`

Ảnh chụp có version về trạng thái target tại thời điểm đánh giá, bao gồm định danh, timestamp và integrity marker khi khả dụng.

### 3.12. `Evidence Quality`

Độ tin cậy, tính nguyên vẹn, provenance và mức đầy đủ context của từng evidence item hoặc evidence bundle.

Canonical field đề xuất:

```text
evidenceQualityScore: 0..100
```

### 3.13. `Evidence Sufficiency`

Mức evidence hiện có đáp ứng burden of proof cho một decision hoặc action cụ thể. Evidence quality cao không bảo đảm sufficiency cao.

Canonical representation:

```text
evidenceSufficiency: INSUFFICIENT | PARTIAL | SUFFICIENT
burdenOfProofMetFor: [action...]
burdenOfProofNotMetFor: [action...]
```

Ví dụ: một target snapshot nguyên vẹn có thể có quality cao nhưng vẫn không đủ để `SUSPEND_USER` nếu thiếu context hoặc lịch sử vi phạm đã xác nhận.

### 3.14. `AI Explanation`

Phần trình bày AI đã diễn giải evidence và rule như thế nào. Explanation không phải evidence và không được tự tham chiếu chính nó để chứng minh conclusion.

## 4. Moderation decision và target action

### 4.1. `Report`

Hồ sơ chứa claim của reporter, định danh target và metadata liên quan.

### 4.2. `Target`

Đối tượng bị report. Phạm vi hiện tại gồm `BLOG`, `COMMENT`, `USER` và `CAFE_PAGE`.

### 4.3. `Recommendation`

Đề xuất có cấu trúc do AI pipeline tạo ra. Recommendation không phải quyết định cuối cùng và không tự mang quyền mutation.

### 4.4. `Report Decision`

Kết luận nghiệp vụ đối với report, tách biệt hoàn toàn với action trên target.

### 4.5. `RESOLVE`

Evidence đáp ứng burden of proof rằng target vi phạm policy. Trong giai đoạn hiện tại, đây vẫn là enum compatibility được giữ để tránh mở rộng migration trong Sprint 1.

### 4.6. `REJECT`

Evidence đủ để kết luận claim không được hỗ trợ, report không hợp lệ hoặc target không vi phạm policy. Không được dùng `REJECT` chỉ vì thiếu evidence.

### 4.7. `NEEDS_MANUAL_REVIEW`

Evidence thiếu, mâu thuẫn, không truy xuất được hoặc uncertainty vượt ngưỡng cho phép. Đây là decision bắt buộc khi AI không đủ cơ sở để `RESOLVE` hoặc `REJECT`.

Sau human review, Admin có thể đóng hồ sơ với closure reason `INSUFFICIENT_EVIDENCE` và không thực hiện target action.

### 4.8. `Target Action`

Hành động đề xuất hoặc thực hiện lên target. Target action không được suy ra chỉ từ report decision mà phải qua policy/action matrix và burden of proof.

### 4.9. `NO_ACTION`

Không thực hiện mutation lên target. Dùng khi manual review, lỗi validation hoặc không có action phù hợp.

### 4.10. `KEEP_VISIBLE`

Giữ `BLOG` hoặc `COMMENT` hiển thị. Đây là canonical V2 action thay cho legacy action `APPROVE`.

### 4.11. `KEEP_ACTIVE`

Giữ `USER` hoặc `CAFE_PAGE` hoạt động.

### 4.12. `HIDE`

Ẩn target theo cơ chế có thể phục hồi. Đây là reversible enforcement action nhưng vẫn cần evidence và policy support.

### 4.13. `REMOVE`

Loại bỏ nội dung do vi phạm đã đáp ứng burden of proof cao hơn `HIDE`.

### 4.14. `SUSPEND_USER`

Vô hiệu hóa user theo policy, quyền hạn và burden of proof được duyệt.

### 4.15. `SUSPEND_PAGE`

Đình chỉ cafe page theo policy, quyền hạn và burden of proof được duyệt.

### 4.16. Legacy `APPROVE`

Action cũ dành cho content. Tên này bị deprecate vì dễ nhầm với business approval.

Compatibility mapping:

```text
V2 KEEP_VISIBLE → legacy APPROVE
```

UI phải hiển thị “Giữ nội dung hiển thị”, không hiển thị “Approve”.

## 5. Score, severity và risk

### 5.1. `Assessment Confidence Score`

Mức AI tự tin rằng recommendation của nó đáng tin dựa trên input hiện có. Confidence không phải accuracy, evidence quality hoặc khả năng target vi phạm.

```text
assessmentConfidenceScore: 0..100
```

### 5.2. `Violation Likelihood Score`

Mức evidence đang nghiêng về khả năng target vi phạm policy.

```text
violationLikelihoodScore: 0..100
```

Tên `violationProbability` được bảo lưu cho tương lai và chỉ được dùng sau khi score đã được calibration bằng dataset phù hợp.

### 5.3. `Harm Severity Score`

Mức thiệt hại tiềm năng nếu vi phạm là thật. Severity của harm không chứng minh rằng vi phạm đã xảy ra.

```text
harmSeverityScore: 0..100
```

### 5.4. `Action Risk Score`

Rủi ro gây false positive, hậu quả không tương xứng hoặc thiệt hại khó hoàn tác khi áp dụng action.

```text
actionRiskScore: 0..100
```

### 5.5. `Uncertainty`

Mức không chắc chắn do dữ liệu thiếu, context mơ hồ, evidence mâu thuẫn hoặc giới hạn model/tool.

### 5.6. Legacy `riskScore`

Field mơ hồ từng có thể bị hiểu là violation risk, harm severity hoặc execution risk. Field này bị deprecate trong V2.

Quy tắc migration:

- chỉ giữ tạm dưới semantics `legacyRiskScore`;
- không dùng làm input duy nhất cho auto-apply;
- không xuất hiện như canonical field trong contract V2;
- compatibility adapter phải ghi rõ nguồn và cách mapping.

## 6. Policy và quyền quyết định

### 6.1. `Policy`

Quy định nghiệp vụ đã được người có thẩm quyền phê duyệt, có owner, version và thời điểm hiệu lực.

### 6.2. `Rule`

Điều kiện có cấu trúc dùng để áp dụng một phần policy vào evidence và target cụ thể.

### 6.3. `Rule Code`

Mã ổn định, có version và nằm trong catalog. AI không được tự phát minh rule code.

### 6.4. `Severity`

Mức nghiêm trọng của hành vi nếu vi phạm là thật. Severity không phải mức giận dữ của reporter và không thay thế violation likelihood.

### 6.5. `Burden of Proof`

Mức evidence tối thiểu phải đáp ứng trước một decision hoặc action. Action càng khó hoàn tác thì burden of proof càng cao.

### 6.6. `Human-in-the-loop`

Cơ chế con người review, xác nhận, từ chối hoặc override recommendation trước những action được policy chỉ định.

### 6.7. `Manual Review`

Trạng thái yêu cầu Admin xem xét trước khi có decision/action cuối cùng.

### 6.8. `Auto-apply`

Cơ chế tự thực thi action sau khi recommendation đáp ứng policy gate và target được revalidate ngay trước mutation.

### 6.9. `Override`

Admin chọn decision/action khác recommendation và cung cấp lý do theo policy audit.

### 6.10. `Rollback`

Khôi phục trạng thái trước action khi loại action và dữ liệu hệ thống cho phép.

### 6.11. `Appeal`

Quy trình đối tượng bị xử lý yêu cầu xem xét lại quyết định moderation.

## 7. Contract, version và audit

### 7.1. `Semantic Validation`

Kiểm tra tính hợp lý giữa evidence, score, decision, rule và target action; khác với schema validation chỉ kiểm tra cấu trúc dữ liệu.

### 7.2. `Policy Version`

Phiên bản policy được dùng tại thời điểm recommendation.

### 7.3. `Prompt Version`

Phiên bản instruction và prompt template gửi model.

### 7.4. `Schema Version`

Phiên bản input/output contract.

### 7.5. `Model Version`

Model và provider thực tế tạo recommendation.

### 7.6. `Workflow Version`

Phiên bản orchestration n8n đã xử lý request.

### 7.7. `Audit Trail`

Chuỗi dữ liệu cho phép tái dựng input snapshot, evidence, recommendation, validation, Admin decision và mutation thực tế.

### 7.8. `Idempotency Key`

Khóa giúp retry cùng logical request không tạo recommendation hoặc action trùng lặp.

### 7.9. `Source of Truth`

Thành phần có thẩm quyền cuối cho một loại dữ liệu hoặc validation. Backend là source of truth cho business validation; n8n/model không được tự mutate CafeStory data.

## 8. Decision record của G0-04

| ID | Quyết định | Trạng thái |
|---|---|---|
| `D1` | Thiếu evidence bắt buộc dùng `NEEDS_MANUAL_REVIEW`, không tự `REJECT` | `APPROVED` |
| `D2` | Tách `assessmentConfidenceScore` và `violationLikelihoodScore` | `APPROVED` |
| `D3` | Tách `evidenceQualityScore` và `evidenceSufficiency` | `APPROVED` |
| `D4` | Deprecate generic `riskScore`; chỉ giữ `legacyRiskScore` trong migration | `APPROVED` |
| `D5` | Canonical V2 dùng `KEEP_VISIBLE`; legacy adapter ánh xạ sang `APPROVE` | `APPROVED` |
| `D6` | AI explanation không phải evidence | `APPROVED` |
| `D7` | Chưa đổi enum report decision trong Sprint 1; giữ compatibility nhưng siết semantics | `APPROVED` |

