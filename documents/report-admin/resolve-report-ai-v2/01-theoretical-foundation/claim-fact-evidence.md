# Claim, Fact and Evidence — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Informative theoretical foundation |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-05B` |
| Ngày cập nhật | 2026-07-23 |
| Thuật ngữ chuẩn | `12-decisions/glossary.md` |

> Tài liệu định nghĩa cách hệ thống hình thành tri thức từ dữ liệu. Các cấu trúc JSON chỉ mang tính khái niệm, chưa phải input/output schema V2 chính thức.

## 2. Epistemic pipeline

Hệ thống phải giữ được chuỗi biến đổi sau:

```text
Claim
→ Evidence
→ Observation
→ Inference
→ Policy Finding
→ Recommendation
```

Mỗi bước có loại dữ liệu, độ tin cậy và giới hạn riêng. Không được bỏ qua các bước giữa để biến claim của reporter thành fact hoặc policy violation.

## 3. Claim

Claim là một khẳng định cần được kiểm tra.

Nguồn claim có thể gồm:

- report reason do reporter chọn;
- description do reporter nhập;
- thông tin do một user khác cung cấp;
- dữ liệu external chưa được xác minh;
- kết luận cũ không còn snapshot hoặc provenance phù hợp.

Claim có thể:

- đúng;
- sai;
- đúng một phần;
- thiếu context;
- mang tính ý kiến;
- được gửi với mục đích lạm dụng moderation.

Số lượng người lặp lại cùng một claim không tự biến claim thành fact.

## 4. Fact

Fact là thông tin đã được xác minh trong một phạm vi cụ thể.

Một fact tối thiểu cần trả lời:

- nguồn nào xác minh;
- xác minh điều gì;
- tại thời điểm nào;
- áp dụng cho entity/version nào;
- còn hợp lệ ở thời điểm sử dụng không.

Ví dụ:

```text
Backend xác nhận user.status = ACTIVE
tại 2026-07-23T10:00:00+07:00
cho userId U-001 và entityVersion 17.
```

Fact này chỉ chứng minh trạng thái được lưu tại thời điểm đó. Nó không chứng minh user không vi phạm, không chứng minh ý định và không bảo đảm trạng thái vẫn giữ nguyên sau đó.

### 4.1. Temporal scope

Fact có thể bị stale khi:

- target bị chỉnh sửa;
- trạng thái entity thay đổi;
- policy version thay đổi;
- dữ liệu liên quan bị xóa hoặc bổ sung;
- recommendation được thực thi trễ.

Vì vậy, fact dùng cho mutation phải được revalidate theo yêu cầu của action.

## 5. Evidence

Evidence là dữ liệu có nguồn gốc có thể hỗ trợ hoặc phản bác claim.

Evidence item nên có tối thiểu:

- ID ổn định trong evidence bundle;
- source type;
- source reference;
- captured timestamp;
- target/entity version;
- integrity marker khi khả dụng;
- verification status;
- content hoặc pointer đến content;
- retention/privacy classification.

### 5.1. Evidence không đồng nghĩa với conclusion

Một đoạn text có thể là evidence đáng tin về việc “đoạn text này tồn tại”, nhưng ý nghĩa policy của nó vẫn cần context và inference.

Ví dụ câu “Chuyển tiền để nhận phần thưởng” có thể xuất hiện trong:

- nội dung lừa đảo;
- bài cảnh báo lừa đảo;
- bài báo trích dẫn vụ việc;
- tài liệu đào tạo chống scam;
- comment châm biếm.

Evidence chất lượng cao vẫn có thể không đủ để đáp ứng burden of proof cho action.

### 5.2. Investigation signal

Investigation signal giúp ưu tiên hoặc định hướng thu thập evidence nhưng không tự chứng minh policy violation.

Ví dụ:

- số lượng report;
- tốc độ tăng report;
- reporter reputation;
- lịch sử AI recommendation;
- anomaly score;
- keyword hit chưa có context.

Signal phải được gắn nhãn riêng, không được nhập nhằng với evidence.

## 6. Observation

Observation là điều được đọc hoặc trích xuất trực tiếp từ evidence.

Observation hợp lệ phải:

- tham chiếu evidence ID;
- chỉ ra excerpt, field hoặc vùng dữ liệu liên quan;
- tránh thêm ý định hoặc kết luận policy;
- cho phép người review kiểm tra lại.

Ví dụ:

```text
Evidence EV-001 chứa câu:
“Chuyển trước 500.000 đồng để nhận phần thưởng.”
```

Đây là observation. Câu “tác giả đang lừa đảo” đã là inference.

## 7. Inference

Inference là suy luận được hình thành từ observation.

Inference phải:

- tham chiếu observation/evidence nguồn;
- nêu uncertainty hoặc alternative interpretation quan trọng;
- không nâng nguồn yếu thành nguồn mạnh;
- không thêm fact không tồn tại trong input;
- không che giấu counter-evidence.

Ví dụ:

```text
Inference: Cấu trúc lời đề nghị có dấu hiệu advance-fee scam.
Alternative interpretation: Nội dung có thể đang trích dẫn hoặc cảnh báo hành vi scam.
```

Độ trôi chảy của explanation không làm inference trở thành fact.

## 8. Policy finding

Policy finding là kết quả áp dụng rule/policy vào evidence và inference.

Policy finding cần có:

- rule code;
- policy/rule version;
- finding status;
- evidence IDs hỗ trợ;
- counter-evidence IDs;
- missing evidence quan trọng;
- reasoning có thể audit.

Finding status khái niệm có thể gồm:

```text
SUPPORTED
CONTRADICTED
INCONCLUSIVE
NOT_APPLICABLE
```

Danh sách chính thức sẽ được chốt trong domain/AI contract, không phải trong tài liệu lý thuyết này.

## 9. Recommendation

Recommendation tổng hợp một hoặc nhiều policy finding để đề xuất report decision và target action.

Recommendation phải giữ traceability:

```text
Recommendation
→ Policy finding
→ Inference/observation
→ Evidence ID
→ Source snapshot
```

Recommendation không được tự tham chiếu explanation của chính nó như evidence.

## 10. Phân loại dữ liệu CafeStory ở mức khái niệm

| Dữ liệu | Phân loại ban đầu | Giới hạn |
|---|---|---|
| Report reason | Claim | Không chứng minh violation |
| Reporter description | Claim | Có thể chủ quan hoặc prompt injection |
| Report count | Investigation signal | Có thể bị mass-report abuse |
| Backend target snapshot | Evidence | Phải có provenance và freshness |
| Entity ID/status/timestamp | Fact có phạm vi | Chỉ chứng minh field tại thời điểm cụ thể |
| Target excerpt | Observation | Phải giữ context/reference |
| Model interpretation | Inference | Có uncertainty và model error |
| Rule match | Policy finding | Phải gắn rule version và evidence |
| AI recommendation cũ | Historical recommendation | Không phải evidence về violation hiện tại |
| Admin decision cũ | Historical decision record | Chứng minh decision đã xảy ra, không chứng minh target hiện tại |

## 11. Mô hình dữ liệu khái niệm

```json
{
  "claims": [
    {
      "id": "CL-001",
      "source": "REPORTER",
      "statement": "Người này đang lừa đảo",
      "verificationStatus": "UNVERIFIED"
    }
  ],
  "evidence": [
    {
      "id": "EV-001",
      "source": "TARGET_SNAPSHOT",
      "reference": "blog:123#content",
      "capturedAt": "ISO-8601",
      "verificationStatus": "SYSTEM_CAPTURED"
    }
  ],
  "observations": [
    {
      "id": "OB-001",
      "evidenceIds": ["EV-001"],
      "quotedExcerpt": "Chuyển trước 500.000 đồng..."
    }
  ],
  "inferences": [
    {
      "id": "IN-001",
      "observationIds": ["OB-001"],
      "statement": "Có dấu hiệu advance-fee scam",
      "uncertainty": "Context có thể là nội dung cảnh báo"
    }
  ],
  "policyFindings": [
    {
      "ruleCode": "RULE-SCAM-ADVANCE-FEE-001",
      "status": "SUPPORTED",
      "evidenceIds": ["EV-001"]
    }
  ]
}
```

Cấu trúc này chỉ minh họa separation of concerns. Field, enum và validation chính thức thuộc AI/domain contract.

## 12. Anti-patterns

### 12.1. Claim laundering

```text
Reporter nói “lừa đảo”
→ prompt gọi đó là “reported fraud evidence”
→ model coi là fact
```

Đây là lỗi biến claim thành evidence bằng cách đổi tên.

### 12.2. Inference laundering

```text
Model suy luận “có ý định lừa đảo”
→ output sau gọi “fraud intent detected” là fact
```

Đây là lỗi nâng inference thành fact mà không có nguồn xác minh mới.

### 12.3. Circular evidence

```text
AI recommendation cũ
→ dùng làm evidence cho recommendation mới
→ recommendation mới củng cố recommendation cũ
```

Đây là vòng lặp tự xác nhận và phải bị cấm.

### 12.4. Decision persistence fallacy

Quyết định Admin trong quá khứ không tự chứng minh target hiện tại vẫn vi phạm, đặc biệt khi content, entity hoặc policy đã thay đổi.

## 13. Approved design decisions

| ID | Quyết định | Trạng thái |
|---|---|---|
| `E1` | Report reason và reporter description luôn bắt đầu ở trạng thái claim | `APPROVED` |
| `E2` | Target/system snapshot chỉ là evidence khi có provenance phù hợp | `APPROVED` |
| `E3` | Observation phải trích dẫn hoặc tham chiếu evidence cụ thể | `APPROVED` |
| `E4` | Inference không được nâng độ tin cậy của nguồn và không tự trở thành fact | `APPROVED` |
| `E5` | Policy finding phải tham chiếu rule version và evidence ID | `APPROVED` |
| `E6` | AI recommendation cũ không phải evidence chứng minh violation | `APPROVED` |
| `E7` | Admin decision cũ chỉ là historical decision record, không tự chứng minh trạng thái hiện tại | `APPROVED` |
| `E8` | Fact phải gắn nguồn, thời điểm, entity/version và phạm vi điều nó chứng minh | `APPROVED` |

## 14. Giới hạn của tài liệu

Tài liệu chưa chốt:

- evidence source hierarchy;
- burden of proof theo action;
- schema field và enum chính thức;
- retention/privacy rule;
- hash/signature mechanism;
- target-specific evidence requirement;
- policy rule cụ thể.

Các nội dung này thuộc các gate tiếp theo.

