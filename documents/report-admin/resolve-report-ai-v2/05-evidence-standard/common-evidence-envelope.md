# G0-12M-01 — Common Evidence Envelope

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Gate | `G0-12M-01` |
| Loại tài liệu | Normative contract proposal |
| Trạng thái | `APPROVED` |
| Approval đã ghi nhận | `APPROVE_G0-12M-01` |
| Phạm vi | Envelope dùng chung cho mọi evidence item |
| Ngoài phạm vi | Payload riêng BLOG/COMMENT/USER/CAFE_PAGE, Evidence Kind Catalog, JSON Schema executable |
| Ngày cập nhật | `2026-07-26` |

Tài liệu này chưa sửa Contract V2 hoặc source. Nó khóa cấu trúc logic cần được dùng làm đầu vào cho
`G0-12M-02`–`G0-12M-07`.

## 2. Mục tiêu

Common Evidence Envelope phải giúp Backend, n8n và Admin trả lời được:

1. Evidence này là gì?
2. Evidence nói về target và snapshot nào?
3. Evidence đến từ hệ thống/entity/field nào?
4. Thành phần nào đã thu thập và biến đổi evidence?
5. Evidence được chụp lúc nào và có còn phù hợp không?
6. Payload có toàn vẹn với dữ liệu thực sự gửi đi không?
7. Evidence có khả dụng và có chất lượng ở mức nào?
8. Evidence có chứa dữ liệu nhạy cảm hoặc đã bị redaction không?
9. Evidence được phép dùng để substantiation, context hay chỉ routing?

Envelope không được tự kết luận:

- target vi phạm;
- Rule ID được `SUPPORTED`;
- evidence bundle đã `SUFFICIENT`;
- action nào được phép;
- AI explanation là fact.

## 3. Ranh giới dữ liệu đã chốt từ G0-12M-00

```text
Report Claim
→ Target Metadata / Target Snapshot
→ Evidence Item
→ Observation
→ Finding
→ Recommendation
→ Human Decision
```

### 3.1. Target Metadata không phải Evidence Envelope

Target metadata mô tả target được đánh giá:

```text
targetType
targetId
snapshotVersion
snapshotHash
targetState
target timestamps
target-specific fields
```

Mỗi evidence item chỉ tham chiếu target snapshot qua `subject`; không lặp toàn bộ target metadata.

### 3.2. Evidence không phải Observation

Evidence là dữ liệu/pointer có provenance. Observation là điều được trích xuất trực tiếp từ evidence.

Ví dụ:

```text
Evidence:
  Snapshot nội dung BLOG, payload digest X.

Observation:
  EV-TARGET-CONTENT chứa câu “Chuyển trước 500.000 đồng...”.
```

Vì vậy field free-text `observation` không thuộc Common Evidence Envelope mới.

### 3.3. Evidence không tự “support rule”

Field cũ `supportsRuleIds[]` bị loại khỏi envelope vì nó tạo policy conclusion quá sớm.

Thay thế:

- `collectedForRuleIds[]`: optional routing metadata; không chứng minh support;
- `Finding.supportingEvidenceIds[]`: kết luận evidence hỗ trợ rule;
- `Finding.counterEvidenceIds[]`: kết luận evidence phản bác rule;
- `Finding.missingEvidenceRequirements[]`: yêu cầu còn thiếu.

## 4. Cấu trúc chuẩn đề xuất

```json
{
  "evidenceId": "EV-TARGET-CONTENT",
  "envelopeVersion": "1.0.0-rc.1",
  "evidenceKind": "TARGET_TEXT_CONTENT",
  "subject": {
    "targetType": "BLOG",
    "targetId": "00000000-0000-0000-0000-000000000001",
    "snapshotVersion": "1",
    "snapshotHash": "sha256:..."
  },
  "source": {
    "sourceType": "TARGET_SNAPSHOT",
    "sourceSystem": "CAFE_STORY_BACKEND",
    "sourceEntityType": "BLOG",
    "sourceEntityId": "00000000-0000-0000-0000-000000000001",
    "sourceFieldPath": "content",
    "verificationStatus": "SYSTEM_CAPTURED",
    "authorityScope": "PLATFORM_OWNED_FIELD"
  },
  "capture": {
    "collectorName": "AdminReportEvidenceSnapshotFactory",
    "collectorVersion": "1.0.0",
    "capturedAt": "2026-07-24T01:00:00Z",
    "sourceUpdatedAt": "2026-07-24T00:55:00Z",
    "transformations": [
      {
        "type": "SANITIZE_HTML",
        "version": "1.0",
        "materiality": "NON_MATERIAL"
      },
      {
        "type": "TRUNCATE",
        "version": "1.0",
        "materiality": "MATERIALITY_UNKNOWN"
      }
    ]
  },
  "integrity": {
    "canonicalization": "JCS",
    "digestAlgorithm": "SHA-256",
    "payloadDigest": "sha256:...",
    "sourceDigest": null
  },
  "availability": {
    "status": "AVAILABLE",
    "reasonCode": null
  },
  "quality": {
    "level": "HIGH",
    "reasonCodes": [
      "AUTHORITATIVE_PLATFORM_FIELD",
      "FRESH_AT_CAPTURE"
    ]
  },
  "privacy": {
    "classification": "INTERNAL_MODERATION",
    "containsPersonalData": false,
    "redactionStatus": "APPLIED",
    "retentionClass": "REPORT_EVIDENCE_90D"
  },
  "intendedUse": "RULE_EVALUATION_CANDIDATE",
  "collectedForRuleIds": [
    "RULE-SCAM-ADVANCE-FEE-001"
  ],
  "payload": {
    "representation": "INLINE",
    "mediaType": "application/json",
    "value": {
      "sanitizedText": "..."
    },
    "reference": null,
    "truncated": true
  }
}
```

Đây là logical schema dùng để review. JSON Schema executable và quyết định bump Contract V2 thuộc
`G0-12M-07`.

## 5. Định nghĩa field

### 5.1. Root

| Field | Bắt buộc | Ý nghĩa |
|---|---:|---|
| `evidenceId` | Có | ID duy nhất trong một evidence bundle/correlation |
| `envelopeVersion` | Có | Version của Common Evidence Envelope, độc lập contract version |
| `evidenceKind` | Có | Loại evidence; catalog chính thức chốt tại `G0-12M-05` |
| `subject` | Có | Target snapshot mà evidence áp dụng |
| `source` | Có | Provenance có cấu trúc |
| `capture` | Có | Collector, thời gian và transformation |
| `integrity` | Có | Digest của payload thực sự gửi đi |
| `availability` | Có | Evidence có đọc/thu thập được không |
| `quality` | Có | Chất lượng của riêng evidence item |
| `privacy` | Có | Phân loại dữ liệu, redaction và retention |
| `intendedUse` | Có | Giới hạn cách evidence được phép sử dụng |
| `collectedForRuleIds` | Không | Rule routing context; không phải finding |
| `payload` | Có | Inline value, reference hoặc `NONE` |

### 5.2. `evidenceId`

Quy tắc:

- unique trong một request/evidence bundle;
- ổn định khi retry cùng `idempotencyKey` và cùng snapshot;
- không cần global uniqueness giữa hai snapshot khác nhau;
- không chứa target content hoặc PII;
- Backend tạo, model không được tạo;
- finding chỉ được tham chiếu ID tồn tại trong request.

Ví dụ hợp lệ:

```text
EV-TARGET-IDENTITY
EV-TARGET-CONTENT
EV-PARENT-CONTEXT
EV-TARGET-MEDIA-01
```

### 5.3. `subject`

| Field | Bắt buộc | Quy tắc |
|---|---:|---|
| `targetType` | Có | `BLOG`, `COMMENT`, `USER`, `CAFE_PAGE` |
| `targetId` | Có | Internal UUID; phải khớp report-target association |
| `snapshotVersion` | Có | Version của target snapshot contract |
| `snapshotHash` | Có | Digest của canonical target snapshot |

Mọi evidence trong cùng request phải trỏ đúng reported target snapshot. Với context evidence, ví dụ
parent BLOG của COMMENT, `subject` vẫn là COMMENT bị report; parent entity nằm trong `source` và
payload relation. Quy tắc parent/reply relation sẽ được chốt tại `G0-12M-03`.

### 5.4. `source`

| Field | Bắt buộc | Ý nghĩa |
|---|---:|---|
| `sourceType` | Có | Phân loại nguồn |
| `sourceSystem` | Có | Hệ thống có thẩm quyền cung cấp dữ liệu |
| `sourceEntityType` | Có | Loại entity/document/source record |
| `sourceEntityId` | Có điều kiện | Internal ID/reference khi nguồn có identity |
| `sourceFieldPath` | Có điều kiện | Field/path cụ thể được capture |
| `verificationStatus` | Có | Nguồn được xác minh ở mức nào |
| `authorityScope` | Có | Nguồn chứng minh được điều gì |

Allowed `sourceType` cho Common Evidence Envelope:

```text
PLATFORM_RECORD
TARGET_SNAPSHOT
VERIFIED_MEDIA_OBSERVATION
AUTHORITATIVE_EXTERNAL_REFERENCE
```

Các loại baseline sau không còn là Evidence Envelope:

```text
REPORTER_CLAIM → giữ trong reportClaim
DERIVED_SIGNAL → giữ trong investigationSignals/audit context
```

Forbidden source:

```text
MODEL_MEMORY
AI_RATIONALE
```

`REPORTER_CLAIM`, `DERIVED_SIGNAL`, `MODEL_MEMORY` và `AI_RATIONALE` không được nằm trong
`evidence[]` hợp lệ. Chúng có thể xuất hiện trong validation error/audit taxonomy hoặc object riêng
đúng loại.

`sourceType` không tự bảo đảm relevance, freshness, quality hoặc sufficiency.

Proposed `verificationStatus`:

```text
SYSTEM_CAPTURED
TOOL_VERIFIED
AUTHORITATIVE_REFERENCE_VERIFIED
```

Proposed `authorityScope`:

```text
PLATFORM_OWNED_FIELD
EXTERNAL_AUTHORITY_LIMITED
```

### 5.5. `capture`

| Field | Bắt buộc | Ý nghĩa |
|---|---:|---|
| `collectorName` | Có | Component/adapter thu thập evidence |
| `collectorVersion` | Có | Version logic thu thập |
| `capturedAt` | Có | Thời điểm tạo evidence item, ISO-8601 có timezone |
| `sourceUpdatedAt` | Không | Thời điểm source entity được cập nhật |
| `transformations` | Có | Danh sách transformation; có thể rỗng |

AI model không được là collector của platform evidence.

Mỗi transformation gồm:

```text
type
version
materiality
```

Proposed `materiality`:

```text
NON_MATERIAL
MATERIAL
MATERIALITY_UNKNOWN
```

Nếu transformation có thể làm mất context quan trọng, quality phải giảm hoặc evidence requirement
phải bị đánh dấu missing/critical tại bước sufficiency.

### 5.6. `integrity`

| Field | Bắt buộc | Ý nghĩa |
|---|---:|---|
| `canonicalization` | Có | Cách canonicalize payload |
| `digestAlgorithm` | Có | Sprint 1 dùng `SHA-256` |
| `payloadDigest` | Có điều kiện | Digest của payload chính xác gửi sang n8n/model |
| `sourceDigest` | Không | Digest của source nguyên bản nếu collector có thể cung cấp |

Quy tắc:

- `payloadDigest` hash canonical payload sau sanitize/redaction/truncate;
- `sourceDigest`, nếu có, hash source trước transformation;
- digest không phải authorization;
- HMAC request/response là security envelope riêng tại `G0-12A`;
- evidence có payload `NONE` vì missing/unreadable có thể không có digest, nhưng phải có reason code.

### 5.7. `availability`

Proposed status:

```text
AVAILABLE
PARTIAL
MISSING
UNREADABLE
INACCESSIBLE
NOT_COLLECTED
STALE
```

| Status | Payload | Ý nghĩa |
|---|---|---|
| `AVAILABLE` | Inline/reference | Đọc được dữ liệu cần thiết |
| `PARTIAL` | Inline/reference | Chỉ đọc được một phần |
| `MISSING` | `NONE` | Field/source không tồn tại |
| `UNREADABLE` | `NONE` hoặc partial ref | Có source nhưng không giải mã/đọc được |
| `INACCESSIBLE` | `NONE` | Không có quyền hoặc source không truy cập được |
| `NOT_COLLECTED` | `NONE` | Collector chưa chạy/không thuộc Sprint hiện tại |
| `STALE` | Inline/reference | Có dữ liệu nhưng không còn khớp target version hiện tại |

`reasonCode` bắt buộc khi status khác `AVAILABLE`.

Không biến deleted/inaccessible/missing thành empty string vì empty content là một observation khác.

### 5.8. `quality`

Level:

```text
HIGH
MEDIUM
LOW
UNUSABLE
```

Quality được đánh giá trên từng evidence item dựa trên:

- authority/provenance;
- freshness;
- completeness;
- transformation/redaction;
- verification status;
- integrity.

`reasonCodes[]` bắt buộc để Admin/validator hiểu vì sao có level đó.

Ví dụ:

```text
AUTHORITATIVE_PLATFORM_FIELD
FRESH_AT_CAPTURE
PARTIAL_CONTEXT
TRUNCATED_AT_MATERIAL_POINT
WEAK_PROVENANCE
UNVERIFIED_SOURCE
```

Quality không phải sufficiency:

```text
HIGH quality content snapshot
+ rule yêu cầu repeated pattern
+ chỉ có một event
→ evidenceSufficiency = INSUFFICIENT
```

`evidenceSufficiency` không được đặt trong evidence item. Nó thuộc finding/recommendation evaluation.

### 5.9. `privacy`

| Field | Bắt buộc | Ý nghĩa |
|---|---:|---|
| `classification` | Có | Mức nhạy cảm của payload |
| `containsPersonalData` | Có | Payload có personal data sau sanitization hay không |
| `redactionStatus` | Có | Tình trạng redaction |
| `retentionClass` | Có | Policy retention áp dụng |

Proposed classification:

```text
PUBLIC_CONTENT
INTERNAL_MODERATION
RESTRICTED
LEGAL_RESTRICTED
```

Proposed redaction status:

```text
NOT_REQUIRED
APPLIED
PARTIAL
BLOCKED
```

Sprint 1:

- không gửi reporter contact;
- không gửi user contact/session/private profile;
- không gửi secret, token hoặc internal credential;
- internal actor/page ID chỉ dùng reference;
- redaction làm mất material context phải được ghi trong transformation và quality reason.

Default retention class cho sanitized Sprint 1 evidence:

```text
REPORT_EVIDENCE_90D
```

Legal hold/deletion runtime chưa thuộc M01.

### 5.10. `intendedUse`

Proposed values:

```text
RULE_EVALUATION_CANDIDATE
CONTEXT_ONLY
```

Rules:

- parent BLOG evidence của COMMENT thường `CONTEXT_ONLY`;
- `RULE_EVALUATION_CANDIDATE` có thể trở thành supporting hoặc counter-evidence tại Finding;
- mọi candidate vẫn cần rule relevance và sufficiency;
- intended use không phải actual finding outcome.

### 5.11. `collectedForRuleIds`

Field này chỉ cho biết Backend thu thập evidence để đánh giá candidate rule nào.

Nó không được dùng để:

- nói evidence đang support rule;
- tăng likelihood;
- thay thế finding;
- bỏ qua counter-evidence;
- cấp action authority.

Rule IDs phải do Backend allowlist cung cấp và phải tồn tại trong `policyContext.candidateRules`.

### 5.12. `payload`

Common payload wrapper:

| Field | Bắt buộc | Ý nghĩa |
|---|---:|---|
| `representation` | Có | `INLINE`, `REFERENCE`, `NONE` |
| `mediaType` | Có điều kiện | Kiểu dữ liệu payload |
| `value` | Có điều kiện | Sanitized inline value |
| `reference` | Có điều kiện | Internal/external reference đã kiểm soát |
| `truncated` | Có | Payload có bị giới hạn không |

Invariant:

```text
INLINE    → value != null, reference = null
REFERENCE → value = null, reference != null
NONE      → value = null, reference = null
```

Target/evidence-kind-specific shape của `value` và `reference` thuộc `G0-12M-02`–`G0-12M-05`.

URL ảnh đơn thuần là `REFERENCE`; nó không phải `VERIFIED_MEDIA_OBSERVATION`.

## 6. Validation invariants

Backend là authority cuối và phải enforce:

1. Envelope không có unknown root property.
2. Mọi required object/field tồn tại và đúng type.
3. `evidenceId` unique trong evidence bundle.
4. `subject.targetType/id/snapshotHash` khớp request target snapshot.
5. `collectedForRuleIds` là subset của candidate Rule IDs.
6. `MODEL_MEMORY` và `AI_RATIONALE` không được nhận làm evidence.
7. Source type và intended use phải hợp lệ.
8. `REPORTER_CLAIM`/`DERIVED_SIGNAL` không được nằm trong `evidence[]`; chúng dùng object riêng.
9. Availability và payload representation phải tương thích.
10. `AVAILABLE/PARTIAL` phải có payload digest khi payload thực sự được gửi.
11. Missing/unreadable/inaccessible/not-collected phải có reason code.
12. `capturedAt` có timezone và không nằm bất hợp lý trong tương lai.
13. Snapshot changed sau capture làm evidence stale; không reuse idempotency result.
14. Transformation/redaction material phải ảnh hưởng quality hoặc missing requirement.
15. Không có secret hoặc forbidden PII trong source reference/payload.
16. Finding chỉ tham chiếu Evidence ID tồn tại.
17. Quality không được dùng thay evidence sufficiency.
18. Evidence item immutable sau khi correlation ID được phát hành.

n8n kiểm tra schema ở boundary nhưng không thay thế Backend semantic validation.

## 7. Current skeleton → Common Envelope

| Current field | Common Envelope | Quyết định |
|---|---|---|
| `evidenceId` | `evidenceId` | Giữ, thêm uniqueness/stability rule |
| `sourceType` | `source.sourceType` | Chuyển thành structured provenance |
| `observation` | Top-level `observations[]` tương lai | Loại khỏi evidence item |
| `provenance` string | `source` + `capture` | Không dùng free-form provenance |
| `capturedAt` | `capture.capturedAt` | Giữ semantics, bắt buộc timezone |
| `quality` string | `quality.level/reasonCodes` | Thêm lý do có cấu trúc |
| `availability` string | `availability.status/reasonCode` | Thêm semantics có cấu trúc |
| `supportsRuleIds` | `collectedForRuleIds` hoặc Finding refs | Không để evidence tự kết luận support |
| `EV-REASON-ROUTE` | `reportClaim` + `policyContext` | Claim/routing không còn giả làm evidence |
| `EV-DERIVED-MODERATION` | `investigationSignals`/audit context | Derived signal không còn giả làm evidence |
| Không có | `subject` | Thêm target snapshot reference |
| Không có | `integrity` | Thêm payload digest |
| Không có | `privacy` | Thêm classification/redaction/retention |
| Không có | `intendedUse` | Tách substantive/context/routing/signal |
| Không có | `payload` wrapper | Phân biệt inline/reference/none |

## 8. Ví dụ missing evidence hợp lệ

```json
{
  "evidenceId": "EV-TARGET-MEDIA-01",
  "envelopeVersion": "1.0.0-rc.1",
  "evidenceKind": "VERIFIED_MEDIA_OBSERVATION",
  "subject": {
    "targetType": "BLOG",
    "targetId": "00000000-0000-0000-0000-000000000001",
    "snapshotVersion": "1",
    "snapshotHash": "sha256:..."
  },
  "source": {
    "sourceType": "TARGET_SNAPSHOT",
    "sourceSystem": "CAFE_STORY_BACKEND",
    "sourceEntityType": "BLOG_MEDIA_REFERENCE",
    "sourceEntityId": "media-01",
    "sourceFieldPath": "images[0]",
    "verificationStatus": "SYSTEM_CAPTURED",
    "authorityScope": "PLATFORM_OWNED_FIELD"
  },
  "capture": {
    "collectorName": "AdminReportEvidenceSnapshotFactory",
    "collectorVersion": "1.0.0",
    "capturedAt": "2026-07-24T01:00:00Z",
    "sourceUpdatedAt": null,
    "transformations": []
  },
  "integrity": {
    "canonicalization": "JCS",
    "digestAlgorithm": "SHA-256",
    "payloadDigest": null,
    "sourceDigest": null
  },
  "availability": {
    "status": "NOT_COLLECTED",
    "reasonCode": "VERIFIED_MEDIA_COLLECTOR_NOT_AVAILABLE"
  },
  "quality": {
    "level": "UNUSABLE",
    "reasonCodes": [
      "MEDIA_NOT_FETCHED_OR_EVALUATED"
    ]
  },
  "privacy": {
    "classification": "INTERNAL_MODERATION",
    "containsPersonalData": false,
    "redactionStatus": "NOT_REQUIRED",
    "retentionClass": "REPORT_EVIDENCE_90D"
  },
  "intendedUse": "RULE_EVALUATION_CANDIDATE",
  "collectedForRuleIds": [
    "RULE-INAPPROPRIATE-IMAGE-001"
  ],
  "payload": {
    "representation": "NONE",
    "mediaType": null,
    "value": null,
    "reference": null,
    "truncated": false
  }
}
```

Kết quả ở bước finding:

```text
critical media requirement missing
→ evidenceSufficiency = INSUFFICIENT
→ NEEDS_MANUAL_REVIEW + NO_ACTION
```

Không tạo `REJECT`.

## 9. Anti-patterns bị cấm

### 9.1. Reporter claim laundering

```json
{
  "sourceType": "PLATFORM_RECORD",
  "observation": "Reporter says this is a scam"
}
```

Sai vì đổi nhãn claim thành platform evidence.

### 9.2. AI rationale làm evidence

```json
{
  "evidenceId": "EV-AI-EXPLANATION",
  "sourceType": "AI_RATIONALE"
}
```

Sai vì explanation là derived text.

### 9.3. Evidence tự support rule

```json
{
  "supportsRuleIds": ["RULE-SCAM-001"]
}
```

Sai vì support/counter là kết quả của Finding.

### 9.4. Empty content thay missing

```json
{
  "availability": "AVAILABLE",
  "payload": ""
}
```

Sai nếu source thực tế inaccessible/deleted/not collected.

### 9.5. High quality tự thành sufficient

```text
quality = HIGH
→ evidenceSufficiency = SUFFICIENT
```

Sai vì sufficiency phụ thuộc rule burden và toàn evidence bundle.

### 9.6. Image URL làm media finding

```text
image URL exists
→ image violation SUPPORTED
```

Sai vì URL chỉ chứng minh reference tồn tại, không chứng minh nội dung ảnh.

## 10. Ảnh hưởng dự kiến khi được approve

### Backend

- thay `List<Map<String, Object>>` bằng typed DTO/envelope;
- tách `Evidence`, `Observation` và `Finding`;
- tạo structured provenance, availability, quality, privacy và digest;
- validate invariants trước provider call và sau provider response.

### n8n

- validate strict nested evidence schema;
- không chỉ kiểm tra `evidence.length > 0`;
- không cho model tạo Evidence ID hoặc biến rationale thành evidence;
- truyền envelope như untrusted structured data.

### Admin FE

- có thể hiển thị source, availability, quality reasons, capture/freshness và privacy-safe locator;
- không hiển thị technical payload/PII mặc định;
- vẫn ưu tiên finding/missing evidence hơn raw metadata.

Những ảnh hưởng trên chỉ là implementation map; M01 không cho phép sửa các layer này.

## 11. Nội dung cố ý để gate sau

| Nội dung | Gate |
|---|---|
| BLOG payload/metadata fields | `G0-12M-02` |
| COMMENT parent/reply subject/context | `G0-12M-03` |
| USER/CAFE_PAGE manual-only metadata | `G0-12M-04` |
| Evidence Kind Catalog và payload kind | `G0-12M-05` |
| Rule-specific required/optional/critical evidence | `G0-12M-06` |
| Executable JSON Schema, fixtures và contract bump | `G0-12M-07` |
| Final cross-layer review | `G0-12M-08` |

## 12. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M01-D01` | Dùng một Common Evidence Envelope cho mọi target | `APPROVED` |
| `M01-D02` | Target-specific metadata nằm trong target snapshot/payload, không fork toàn envelope | `APPROVED` |
| `M01-D03` | Loại free-text `observation` khỏi evidence item | `APPROVED` |
| `M01-D04` | Loại `supportsRuleIds`; support/counter chỉ nằm trong Finding | `APPROVED` |
| `M01-D05` | Quality là per-item; sufficiency là per-rule/finding/bundle | `APPROVED` |
| `M01-D06` | Provenance, capture, integrity và privacy phải có cấu trúc | `APPROVED` |
| `M01-D07` | Missing/unreadable/not-collected là evidence state rõ, không chuyển thành empty content | `APPROVED` |
| `M01-D08` | Reporter claim và derived signal nằm ngoài `evidence[]`, dùng object riêng đúng loại | `APPROVED` |
| `M01-D09` | Payload wrapper phân biệt INLINE/REFERENCE/NONE | `APPROVED` |
| `M01-D10` | Contract version chỉ được chốt/bump tại M07 sau fixtures | `APPROVED` |

## 13. Acceptance criteria của G0-12M-01

- [x] Common envelope có subject/source/capture/integrity/availability/quality/privacy/payload.
- [x] Evidence được tách khỏi Observation và Finding.
- [x] Quality được tách khỏi sufficiency.
- [x] Reporter claim, derived signal và AI rationale có boundary rõ.
- [x] Missing/unavailable state không bị chuyển thành empty content.
- [x] Có migration mapping từ skeleton hiện tại.
- [x] Có positive/missing example và anti-patterns.
- [x] Có impact map BE/n8n/FE.
- [x] Target-specific schema và executable JSON Schema được giữ ngoài phạm vi.
- [x] Người dùng review và approve decision package `M01-D01`–`M01-D10`.

## 14. Trạng thái gate

```text
Deliverable tồn tại: YES
Static content review: PASS
User approval: APPROVE_G0-12M-01
Source/runtime mutation: NONE

G0-12M-01 STATUS: COMPLETED
NEXT STEP: PREPARE_G0-12M-02_FOR_REVIEW
```
