# G0-10E — Protected Characteristics for Hateful Conduct

## 1. Decision

| Thuộc tính | Giá trị |
|---|---|
| Decision ID | `BD-005` |
| Trạng thái | `APPROVED` |
| Approval chat | `APPROVE_G0-10E` |
| Rule | `CSR.HATE.001` |
| Affected | Hate finding, rule routing, evidence, UI explanation, evaluation |
| Nature | Internal community policy; không phải kết luận pháp lý |

## 2. Vấn đề

`CSR.HATE.001` chỉ có thể đánh giá nhất quán nếu “protected characteristic” được định nghĩa.
Nếu để model tự hiểu:

- cùng nội dung có thể bị phân loại khác nhau giữa lần chạy;
- mọi insult có thể bị gắn Hate thay vì Harassment;
- model có thể suy đoán identity từ tên, ảnh hoặc stereotype;
- không thể tạo dataset/test coverage ổn định.

## 3. Các phương án

### E1 — Versioned enumerated baseline + harassment fallback — Khuyến nghị

Danh mục baseline:

| Code | Protected characteristic |
|---|---|
| `PC_RACE` | Race |
| `PC_ETHNICITY` | Ethnicity |
| `PC_NATIONALITY` | Nationality |
| `PC_NATIONAL_ORIGIN` | National origin |
| `PC_RELIGION_BELIEF` | Religion or belief |
| `PC_SEX_GENDER` | Sex or gender |
| `PC_GENDER_IDENTITY_EXPRESSION` | Gender identity or expression |
| `PC_SEXUAL_ORIENTATION` | Sexual orientation |
| `PC_DISABILITY` | Physical, sensory, intellectual or mental disability |
| `PC_SERIOUS_DISEASE` | Serious disease or health condition |
| `PC_AGE` | Age group |
| `PC_CASTE` | Caste or analogous inherited social status |
| `PC_IMMIGRATION_REFUGEE_STATUS` | Immigration or refugee status |

Policy behavior:

- Hate finding cần chứng minh attack nhắm vào người/nhóm **because of** ít nhất một code trên.
- Hỗ trợ intersectional finding có nhiều characteristic.
- AI không được suy identity từ appearance, name, location hoặc stereotype.
- Nếu characteristic chỉ do reporter claim mà không hiện trong target/context, phải ghi uncertainty.
- Insult dựa nghề nghiệp, thu nhập, địa phương, sở thích, fandom hoặc quan điểm chính trị
  không mặc định là Hate; vẫn route `CSR.HAR.001`, `CSR.HAR.002` hoặc Safety nếu phù hợp.
- Threat vì protected characteristic có thể tạo cả Hate và Safety finding.
- Add/remove characteristic tạo catalog/policy version mới và cần impact review.

### E2 — Mọi identity/group đều là protected characteristic

Đơn giản nhưng làm Hate rule quá rộng, chồng lên Harassment và khó đánh giá consistency.
Không khuyến nghị.

### E3 — Chưa kích hoạt Hate rule cho đến khi có legal list

Giảm rủi ro policy nhưng bỏ trống một nhóm harm quan trọng. Không cần thiết vì đây có thể là
community-policy baseline, với legal review bổ sung khi activation.

## 4. Evidence và exception guard

Finding phải có:

```text
protectedCharacteristicCodes[]
targetedPersonOrGroup
attackObservation
evidenceReferences[]
context
counterEvidence[]
uncertaintyReasons[]
```

Không tự xem là attack khi nội dung:

- trích dẫn để phản bác hoặc báo cáo;
- giáo dục/nghiên cứu/trung lập;
- self-reference hoặc reclaimed expression trong context phù hợp;
- mention một characteristic mà không dehumanize, exclude, threaten hoặc degrade.

Exception vẫn cần evidence/context; model không tự approve exception.

## 5. Attack scope

`CSR.HATE.001` có thể bao gồm:

- dehumanization;
- inferior/superior claim;
- exclusion/segregation advocacy;
- targeted degrading generalization;
- call for discrimination or harm;
- slur khi context thể hiện attack.

Exact phrase list không phải source of truth. Keyword/slur chỉ là investigation signal.

## 6. Khuyến nghị

Chọn `E1`.

Điểm quan trọng là fallback: một cuộc công kích không thuộc Hate list không đồng nghĩa “được phép”.
Nó tiếp tục được đánh giá theo Harassment, Threat, Privacy hoặc rule liên quan.

## 7. Acceptance record đã duyệt

```text
decisionId: BD-005
selectedOption: E1_VERSIONED_PROTECTED_LIST_WITH_FALLBACK
protectedCharacteristicListVersion: PCL-1.0.0-proposed.1
identityInferenceFromAppearanceOrNameAllowed: false
nonProtectedAttackFallback: HARASSMENT_OR_RELEVANT_RULE
multiCharacteristicFindingAllowed: true
listChangeRequiresVersionAndImpactReview: true
```

## 8. Kết luận

Phương án `E1_VERSIONED_PROTECTED_LIST_WITH_FALLBACK` và 13 characteristic codes
đã được duyệt. Danh mục vẫn phải đi qua activation/version gate trước khi dùng runtime.
