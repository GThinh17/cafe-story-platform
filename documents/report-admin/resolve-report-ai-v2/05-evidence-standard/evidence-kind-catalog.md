# G0-12M-05 — Evidence Kind Catalog

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12M-05` |
| Trạng thái | `APPROVED` |
| Approval đã nhận | `APPROVE_G0-12M-05` |
| Catalog version | `1.0.0-rc.1` |
| Phụ thuộc | `G0-12M-00`–`G0-12M-04` |
| Áp dụng | `BLOG`, `COMMENT`, `USER`, `CAFE_PAGE` |
| Source/runtime mutation | `NONE` |

Catalog này khóa semantic type của evidence item. JSON Schema executable, fixtures và quyết định
contract/version cuối cùng thuộc `G0-12M-07`.

## 2. Bound

### 2.1. Trong phạm vi

- canonical Evidence Kind codes;
- ý nghĩa và target applicability của từng kind;
- allowed source type, payload representation và default intended use;
- structural cardinality theo target;
- mapping provisional evidence slots M02–M04 sang kind chuẩn;
- lifecycle cho kind chưa có verified collector;
- validation rules giữa kind/source/payload/availability/privacy.

### 2.2. Ngoài phạm vi

- rule nào bắt buộc/optional/critical kind nào;
- evidence bundle đã sufficient cho Rule ID nào;
- Finding support/counter/missing conclusion;
- executable JSON Schema và code-generated DTO;
- media/OCR/vision collector implementation;
- authoritative external-source taxonomy theo từng policy family;
- USER/CAFE_PAGE deep evidence implementation;
- thay đổi BE, FE, n8n, database hoặc provider prompt.

## 3. Ba khái niệm không được trộn

| Khái niệm | Câu hỏi trả lời | Ví dụ |
|---|---|---|
| `evidenceKind` | Evidence item mô tả loại dữ liệu gì? | `TARGET_TEXT_CONTENT` |
| `source.sourceType` | Dữ liệu đến từ loại nguồn nào? | `TARGET_SNAPSHOT` |
| `evidenceId` | Instance nào trong bundle hiện tại? | `EV-BLOG-CONTENT` |

Sai:

```text
evidenceKind = PLATFORM_RECORD
```

vì `PLATFORM_RECORD` là source type.

Sai:

```text
evidenceKind = EV-BLOG-CONTENT
```

vì đây là instance ID.

Đúng:

```json
{
  "evidenceId": "EV-BLOG-CONTENT",
  "evidenceKind": "TARGET_TEXT_CONTENT",
  "source": {
    "sourceType": "TARGET_SNAPSHOT"
  }
}
```

## 4. Catalog lifecycle

| Status | Ý nghĩa |
|---|---|
| `ACTIVE` | Backend được phép tạo `AVAILABLE/PARTIAL` item nếu collector/source hợp lệ |
| `RESERVED_MISSING_ONLY` | Chỉ được biểu diễn missing/not-collected; chưa được nhận payload khả dụng |
| `DEFERRED_NOT_CATALOGED` | Chưa phải catalog value; cần gate/version mới |

`RESERVED_MISSING_ONLY` giúp biểu diễn critical missing evidence mà không giả vờ collector đã tồn tại.

## 5. Canonical catalog

| ID | Evidence Kind | Category | Targets | Lifecycle | Default intended use |
|---|---|---|---|---|---|
| `EK-01` | `TARGET_IDENTITY` | Binding | All | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-02` | `REPORT_TARGET_ASSOCIATION` | Binding | All | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-03` | `TARGET_STATE` | Target metadata evidence | All | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-04` | `TARGET_TEXT_CONTENT` | Direct target content | BLOG, COMMENT | `ACTIVE` | `RULE_EVALUATION_CANDIDATE` |
| `EK-05` | `TARGET_MEDIA_REFERENCE` | Reference | All | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-06` | `VERIFIED_MEDIA_OBSERVATION` | Verified direct observation | BLOG, COMMENT | `RESERVED_MISSING_ONLY` | `RULE_EVALUATION_CANDIDATE` |
| `EK-07` | `TARGET_ACTOR_ASSOCIATION` | Relationship | BLOG, COMMENT | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-08` | `TARGET_PAGE_ASSOCIATION` | Relationship | BLOG, COMMENT | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-09` | `TARGET_REGION_ASSOCIATION` | Relationship | BLOG, CAFE_PAGE | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-10` | `PARENT_BLOG_CONTEXT` | Bounded context | COMMENT | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-11` | `PARENT_COMMENT_CONTEXT` | Bounded context | COMMENT | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-12` | `TARGET_PUBLIC_PROFILE_CONTEXT` | Bounded context | USER, CAFE_PAGE | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-13` | `PAGE_OWNER_ASSOCIATION` | Relationship | CAFE_PAGE | `ACTIVE` | `CONTEXT_ONLY` |
| `EK-14` | `TARGET_DEEP_CONTEXT` | Deferred deep context | USER, CAFE_PAGE | `RESERVED_MISSING_ONLY` | `CONTEXT_ONLY` |

`All` nghĩa là bốn target hiện tại, không tự mở rộng sang target type tương lai.

## 6. Binding and target kinds

### 6.1. `TARGET_IDENTITY`

Chứng minh record nào là subject target trong CafeStory.

Payload contract:

```json
{
  "targetType": "BLOG",
  "targetRef": "target-blog-1",
  "referenceKind": "REQUEST_SCOPED_ALIAS"
}
```

Rules:

- đúng một item trên bundle;
- source entity ID phải khớp target snapshot;
- raw UUID chỉ được giữ ở restricted Backend view;
- identity không chứng minh target vi phạm;
- không gộp report association vào cùng item.

Allowed source:

```text
PLATFORM_RECORD
TARGET_SNAPSHOT
```

### 6.2. `REPORT_TARGET_ASSOCIATION`

Chứng minh report đang bind đúng target FK.

```json
{
  "reportAlias": "report-1",
  "targetType": "COMMENT",
  "associationField": "comment_id",
  "associatedTargetAlias": "target-comment-1",
  "associationValidated": true
}
```

Rules:

- đúng một item trên bundle;
- chỉ `PLATFORM_RECORD`;
- `associationValidated=false` không được tiếp tục evaluation;
- reporter reason/description không nằm trong payload;
- là control/binding evidence, không substantiation policy violation;
- report association không nằm trong target snapshot hash, nhưng nằm trong whole-request integrity.

### 6.3. `TARGET_STATE`

Mô tả lifecycle/state fields đúng theo source, không phát minh normalization.

Target-specific payload:

| Target | Required semantic fields |
|---|---|
| BLOG | `status`, `createdAt`, `updatedAt` |
| COMMENT | `status`, `createdAt`, `updatedAt` |
| USER | `accountStatus`, `sourceRepresentation=BOOLEAN`, unavailable version/timestamps |
| CAFE_PAGE | `lifecycleStatus`, `subscriptionActive`, `subscriptionExpiresAt`, timestamps |

Rules:

- source state có thể quality cao nhưng vẫn chỉ context;
- state hiện tại không chứng minh report đúng/sai;
- USER không được đổi boolean thành fabricated lifecycle enum;
- PAGE không được nén ba state signals thành một synthetic status;
- stale/inconsistent state phải được biểu diễn bằng availability/quality/blocked reason.

## 7. Direct content and media kinds

### 7.1. `TARGET_TEXT_CONTENT`

Chỉ dùng cho exact reported BLOG hoặc COMMENT text.

```json
{
  "sanitizedText": "bounded target text",
  "sourceLength": 1200,
  "capturedLength": 1200,
  "truncated": false,
  "contentDigest": "sha256:example",
  "sanitizerVersion": "1.0.0"
}
```

Rules:

- only BLOG/COMMENT;
- subject content, không phải parent context;
- `INLINE` khi available/partial;
- transformations phải có sanitize/truncate materiality;
- text là untrusted data, không phải instruction;
- USER/PAGE profile text không được dùng kind này trong Sprint 1.

Allowed source:

```text
TARGET_SNAPSHOT
PLATFORM_RECORD
```

### 7.2. `TARGET_MEDIA_REFERENCE`

Chỉ chứng minh target có media reference tại thời điểm capture.

```json
{
  "mediaAlias": "media-1",
  "referenceDigest": "sha256:example",
  "mediaTypeHint": "image/unknown",
  "fetched": false,
  "evaluated": false
}
```

Rules:

- raw URL không gửi provider mặc định;
- strip query/fragment và canonical-sort theo target schema;
- `REFERENCE` hoặc structured `INLINE` metadata, không chứa media bytes;
- không được tạo observation về nội dung ảnh;
- không substantiation image violation;
- USER/PAGE media vẫn local/context-only.

### 7.3. `VERIFIED_MEDIA_OBSERVATION`

Payload tương lai của trusted media collector:

```json
{
  "mediaAlias": "media-1",
  "inputMediaDigest": "sha256:example",
  "collectorName": "RESERVED",
  "collectorVersion": "RESERVED",
  "structuredObservations": []
}
```

Sprint 1 hiện tại:

```text
lifecycle = RESERVED_MISSING_ONLY
availability = NOT_COLLECTED
reasonCode = VERIFIED_MEDIA_COLLECTOR_NOT_AVAILABLE
payload.representation = NONE
```

Không được:

- đổi media URL thành verified observation;
- dùng model đang ra quyết định làm evidence collector;
- chấp nhận `AVAILABLE` trước capability approval và collector allowlist;
- dùng free-text model narrative làm structured observation.

## 8. Relationship and context kinds

### 8.1. `TARGET_ACTOR_ASSOCIATION`

Mô tả persisted user actor và actor context của BLOG/COMMENT.

Payload tối thiểu:

```json
{
  "underlyingUserAlias": "actor-user-1",
  "actorContextType": "USER",
  "actorCafePageAlias": null,
  "relationIntegrity": "CONSISTENT"
}
```

Nó không chứng minh actor có ý định vi phạm hoặc cấp actor-level action.

### 8.2. `TARGET_PAGE_ASSOCIATION`

Mô tả BLOG/COMMENT có quan hệ với Cafe Page nào.

```json
{
  "associationPresent": true,
  "pageAlias": "page-1",
  "relationship": "PUBLISHED_FOR_PAGE"
}
```

Nullable relation phải dùng `associationPresent=false`, không bỏ field mơ hồ.

### 8.3. `TARGET_REGION_ASSOCIATION`

Mô tả platform region reference của BLOG hoặc CAFE_PAGE.

Rules:

- không phải location/jurisdiction proof;
- không bao exact private user location;
- default `CONTEXT_ONLY`;
- chỉ dùng restricted alias hoặc bounded coarse field đã được duyệt.

### 8.4. `PARENT_BLOG_CONTEXT`

BLOG bounded context của target COMMENT.

```json
{
  "relationship": "PARENT_BLOG_OF_TARGET_COMMENT",
  "blogAlias": "context-blog-1",
  "status": "PUBLISHED",
  "sanitizedExcerpt": "bounded context",
  "contentDigest": "sha256:example",
  "truncated": true
}
```

Subject vẫn là COMMENT. Nội dung BLOG không được attribution sang target COMMENT author.

### 8.5. `PARENT_COMMENT_CONTEXT`

Direct parent COMMENT của reply target.

```json
{
  "relationship": "DIRECT_PARENT_COMMENT",
  "parentCommentAlias": "context-comment-1",
  "sanitizedExcerpt": "bounded direct parent",
  "contentDigest": "sha256:example",
  "truncated": false
}
```

Rules:

- root COMMENT vẫn có một slot với missing/nullable relation semantics được M07 encode;
- chỉ direct parent;
- không full thread, siblings, descendants hoặc behavior pattern;
- context không tự substantiation target violation.

### 8.6. `TARGET_PUBLIC_PROFILE_CONTEXT`

Public USER hoặc CAFE_PAGE fields dùng cho local Admin orientation.

Rules:

- USER/PAGE manual-only;
- bounded/sanitized;
- email, phone, password, roles, sessions và owner/member PII bị cấm;
- PAGE address/region là declared context, không merchant/jurisdiction proof;
- `CONTEXT_ONLY`, không provider projection.

### 8.7. `PAGE_OWNER_ASSOCIATION`

Chỉ chứng minh primary owner FK association của CAFE_PAGE.

```json
{
  "ownerAlias": "owner-user-1",
  "associationAvailability": "AVAILABLE",
  "ownerPiiIncluded": false
}
```

Nó không chứng minh legal ownership, merchant identity hoặc trách nhiệm cho mọi page content.

### 8.8. `TARGET_DEEP_CONTEXT`

Placeholder missing slot cho USER/PAGE deep burden.

Sprint 1:

```text
lifecycle = RESERVED_MISSING_ONLY
availability = NOT_COLLECTED
reasonCode = TARGET_DEEP_POLICY_NOT_IN_SPRINT1
payload.representation = NONE
```

Không được tạo một generic available blob dưới kind này. Khi Sprint 4 thiết kế behavior history,
merchant identity, ownership hoặc jurisdiction evidence, phải tạo semantic kinds cụ thể bằng catalog
extension.

## 9. Source-type compatibility

| Evidence Kind | PLATFORM_RECORD | TARGET_SNAPSHOT | VERIFIED_MEDIA_OBSERVATION | AUTHORITATIVE_EXTERNAL_REFERENCE |
|---|---:|---:|---:|---:|
| `TARGET_IDENTITY` | Yes | Yes | No | No |
| `REPORT_TARGET_ASSOCIATION` | Yes | No | No | No |
| `TARGET_STATE` | Yes | Yes | No | No |
| `TARGET_TEXT_CONTENT` | Yes | Yes | No | No |
| `TARGET_MEDIA_REFERENCE` | Yes | Yes | No | No |
| `VERIFIED_MEDIA_OBSERVATION` | No | No | Reserved | No |
| `TARGET_ACTOR_ASSOCIATION` | Yes | Yes | No | No |
| `TARGET_PAGE_ASSOCIATION` | Yes | Yes | No | No |
| `TARGET_REGION_ASSOCIATION` | Yes | Yes | No | No |
| `PARENT_BLOG_CONTEXT` | Yes | Yes | No | No |
| `PARENT_COMMENT_CONTEXT` | Yes | Yes | No | No |
| `TARGET_PUBLIC_PROFILE_CONTEXT` | Yes | Yes | No | No |
| `PAGE_OWNER_ASSOCIATION` | Yes | Yes | No | No |
| `TARGET_DEEP_CONTEXT` | Missing-only | No | No | No |

`AUTHORITATIVE_EXTERNAL_REFERENCE` vẫn là allowed source type ở Common Envelope, nhưng M05 chưa có
semantic kind tương ứng. Không tạo kind generic tên `AUTHORITATIVE_EXTERNAL_RECORD` chỉ để chứa mọi
thứ. M06 phải xác định nhu cầu semantic cụ thể; catalog extension sau đó mới thêm kind như claimant
authority, license/permission, jurisdiction record hoặc factual reference.

## 10. Payload and availability compatibility

| Lifecycle/status | Allowed representation | Payload rule |
|---|---|---|
| `ACTIVE + AVAILABLE` | `INLINE` hoặc `REFERENCE` theo kind | value/reference và digest hợp lệ |
| `ACTIVE + PARTIAL` | `INLINE` hoặc `REFERENCE` | reason code + quality degradation |
| `MISSING` | `NONE` | reason code bắt buộc |
| `UNREADABLE` | `NONE`, hoặc partial reference khi còn locator an toàn | reason code bắt buộc |
| `INACCESSIBLE` | `NONE` | không leak locator/PII |
| `NOT_COLLECTED` | `NONE` | collector/capability reason bắt buộc |
| `STALE` | payload cũ có thể giữ restricted | không gửi evaluation/reuse result |
| `RESERVED_MISSING_ONLY` | `NONE` | chỉ missing/not-collected/inaccessible |

Cross-field validation:

```text
AVAILABLE/PARTIAL + INLINE    → value != null, reference = null
AVAILABLE/PARTIAL + REFERENCE → value = null, reference != null
NONE                          → value = null, reference = null
RESERVED_MISSING_ONLY         → representation = NONE
```

## 11. Structural bundle theo target

Đây là structural presence, không phải rule sufficiency.

### 11.1. BLOG

| Kind | Cardinality |
|---|---:|
| `TARGET_IDENTITY` | 1 |
| `REPORT_TARGET_ASSOCIATION` | 1 |
| `TARGET_STATE` | 1 |
| `TARGET_TEXT_CONTENT` | 1 |
| `TARGET_ACTOR_ASSOCIATION` | 1 |
| `TARGET_PAGE_ASSOCIATION` | 1 |
| `TARGET_REGION_ASSOCIATION` | 1 |
| `TARGET_MEDIA_REFERENCE` | 0..N |
| `VERIFIED_MEDIA_OBSERVATION` | 0..N missing-only khi rule/media cần |

### 11.2. COMMENT

| Kind | Cardinality |
|---|---:|
| `TARGET_IDENTITY` | 1 |
| `REPORT_TARGET_ASSOCIATION` | 1 |
| `TARGET_STATE` | 1 |
| `TARGET_TEXT_CONTENT` | 1 |
| `TARGET_ACTOR_ASSOCIATION` | 1 |
| `TARGET_PAGE_ASSOCIATION` | 1 |
| `PARENT_BLOG_CONTEXT` | 1 |
| `PARENT_COMMENT_CONTEXT` | 1 slot; root/reply semantics rõ |
| `TARGET_MEDIA_REFERENCE` | 0..N |
| `VERIFIED_MEDIA_OBSERVATION` | 0..N missing-only khi rule/media cần |

### 11.3. USER

| Kind | Cardinality |
|---|---:|
| `TARGET_IDENTITY` | 1 |
| `REPORT_TARGET_ASSOCIATION` | 1 |
| `TARGET_STATE` | 1 |
| `TARGET_PUBLIC_PROFILE_CONTEXT` | 1 |
| `TARGET_DEEP_CONTEXT` | 1 missing-only |

### 11.4. CAFE_PAGE

| Kind | Cardinality |
|---|---:|
| `TARGET_IDENTITY` | 1 |
| `REPORT_TARGET_ASSOCIATION` | 1 |
| `TARGET_STATE` | 1 |
| `TARGET_PUBLIC_PROFILE_CONTEXT` | 1 |
| `PAGE_OWNER_ASSOCIATION` | 1 |
| `TARGET_REGION_ASSOCIATION` | 1 |
| `TARGET_MEDIA_REFERENCE` | 0..N |
| `TARGET_DEEP_CONTEXT` | 1 missing-only |

Structural `1` nghĩa là slot phải có, nhưng availability có thể là missing/unavailable với reason.
Nó không có nghĩa evidence đủ cho bất kỳ rule/action nào.

## 12. Mapping provisional slots M02–M04

### 12.1. BLOG

| Provisional evidence ID | Canonical kind | Normalization |
|---|---|---|
| `EV-BLOG-IDENTITY` | `TARGET_IDENTITY` | Tách report association ra item riêng |
| new `EV-REPORT-TARGET-ASSOCIATION` | `REPORT_TARGET_ASSOCIATION` | Binding control |
| `EV-BLOG-STATE` | `TARGET_STATE` | Context only |
| `EV-BLOG-CONTENT` | `TARGET_TEXT_CONTENT` | Thay alias `CONTENT_SNAPSHOT` |
| `EV-BLOG-AUTHOR-ASSOCIATION` | `TARGET_ACTOR_ASSOCIATION` | Context only |
| `EV-BLOG-PAGE-ASSOCIATION` | `TARGET_PAGE_ASSOCIATION` | Nullable relation explicit |
| `EV-BLOG-REGION-ASSOCIATION` | `TARGET_REGION_ASSOCIATION` | Không jurisdiction proof |
| `EV-BLOG-MEDIA-REF-{id}` | `TARGET_MEDIA_REFERENCE` | Không media observation |

### 12.2. COMMENT

| Provisional evidence ID | Canonical kind | Normalization |
|---|---|---|
| `EV-COMMENT-IDENTITY` | `TARGET_IDENTITY` | Tách report association |
| new `EV-REPORT-TARGET-ASSOCIATION` | `REPORT_TARGET_ASSOCIATION` | Binding control |
| `EV-COMMENT-STATE` | `TARGET_STATE` | Context only |
| `EV-COMMENT-CONTENT` | `TARGET_TEXT_CONTENT` | Exact target content |
| `EV-COMMENT-ACTOR-ASSOCIATION` | `TARGET_ACTOR_ASSOCIATION` | User + page actor context |
| `EV-COMMENT-BLOG-CONTEXT` | `PARENT_BLOG_CONTEXT` | Thay ambiguous `PARENT_CONTEXT_SNAPSHOT` |
| `EV-COMMENT-PARENT-CONTEXT` | `PARENT_COMMENT_CONTEXT` | Direct parent only |
| `EV-COMMENT-MEDIA-REF-{id}` | `TARGET_MEDIA_REFERENCE` | Reference only |

### 12.3. USER

| Provisional evidence ID | Canonical kind | Normalization |
|---|---|---|
| `EV-USER-IDENTITY` | `TARGET_IDENTITY` | Tách report association |
| new `EV-REPORT-TARGET-ASSOCIATION` | `REPORT_TARGET_ASSOCIATION` | Binding control |
| `EV-USER-STATE` | `TARGET_STATE` | Boolean source semantics |
| `EV-USER-PUBLIC-PROFILE` | `TARGET_PUBLIC_PROFILE_CONTEXT` | Context only/no provider |
| `EV-USER-DEEP-REVIEW` | `TARGET_DEEP_CONTEXT` | Missing-only, payload NONE |

### 12.4. CAFE_PAGE

| Provisional evidence ID | Canonical kind | Normalization |
|---|---|---|
| `EV-PAGE-IDENTITY` | `TARGET_IDENTITY` | Tách report association |
| new `EV-REPORT-TARGET-ASSOCIATION` | `REPORT_TARGET_ASSOCIATION` | Binding control |
| `EV-PAGE-STATE` | `TARGET_STATE` | State vector |
| `EV-PAGE-OWNER-ASSOCIATION` | `PAGE_OWNER_ASSOCIATION` | Restricted ref/no owner PII |
| `EV-PAGE-PUBLIC-PROFILE` | `TARGET_PUBLIC_PROFILE_CONTEXT` | Context only |
| `EV-PAGE-MEDIA-REFERENCE` | `TARGET_MEDIA_REFERENCE` | Reference only |
| new `EV-PAGE-REGION-ASSOCIATION` | `TARGET_REGION_ASSOCIATION` | Declared context only |
| `EV-PAGE-DEEP-REVIEW` | `TARGET_DEEP_CONTEXT` | Missing-only |

## 13. Current generic skeleton normalization

| Current generic ID/value | Kết quả |
|---|---|
| `EV-TARGET-IDENTITY` | Tách `TARGET_IDENTITY` và `REPORT_TARGET_ASSOCIATION` |
| `EV-TARGET-CONTENT` cho BLOG/COMMENT | `TARGET_TEXT_CONTENT` |
| `EV-TARGET-CONTENT` cho USER/PAGE | Dừng dùng; chuyển `TARGET_PUBLIC_PROFILE_CONTEXT` |
| `EV-TARGET-STATE` | `TARGET_STATE` |
| `EV-PARENT-CONTEXT` | Tách `PARENT_BLOG_CONTEXT`/`PARENT_COMMENT_CONTEXT` |
| `EV-TARGET-MEDIA` | `TARGET_MEDIA_REFERENCE`; không phải verified observation |
| `EV-REASON-ROUTE` | Loại khỏi `evidence[]`; dùng `reportClaim/policyContext` |
| `EV-DERIVED-MODERATION` | Loại khỏi `evidence[]`; dùng `investigationSignals/audit` |
| `CONTENT_SNAPSHOT` | Deprecated alias → `TARGET_TEXT_CONTENT` |
| `PARENT_CONTEXT_SNAPSHOT` | Bị cấm vì mơ hồ |
| `MEDIA_OBSERVATION` | Bị cấm vì trộn reference và verified observation |

## 14. Không phải Evidence Kind

Các value sau bị cấm trong `evidenceKind`:

```text
REPORTER_CLAIM
DERIVED_SIGNAL
MODEL_MEMORY
AI_RATIONALE
POLICY_RULE
MODEL_EXPLANATION
CONFIDENCE_SCORE
RISK_SCORE
REPORT_COUNT
POPULARITY
PRIOR_AI_CONCLUSION
```

Routing:

| Data | Object đúng |
|---|---|
| reason/description | `reportClaim` |
| same-target report count | `investigationSignals` |
| prior AI moderation | `investigationSignals`/audit history |
| policy/rule versions | `policyContext` |
| model explanation | recommendation/finding rationale |
| confidence/likelihood | evaluation output, không evidence |

## 15. Kind-level substantiation boundary

| Kind group | Có thể trở thành supporting/counter evidence? |
|---|---|
| `TARGET_TEXT_CONTENT` | Có, nếu M06 rule mapping và sufficiency cho phép |
| `VERIFIED_MEDIA_OBSERVATION` | Có trong tương lai, khi collector được approve |
| Identity/association/state | Mặc định không; dùng binding/context/applicability |
| Parent context | Mặc định không attribution; M06 chỉ có thể dùng để interpret target content |
| Public profile/owner/region/page associations | Context only trong Sprint 1 |
| Media reference | Chỉ chứng minh reference tồn tại |
| `TARGET_DEEP_CONTEXT` | Không khi missing-only |

M05 không tự gắn `supportingEvidenceIds` hoặc `counterEvidenceIds`. Finding mới làm việc đó sau khi
kiểm tra relevance, counter-evidence và burden ở M06.

## 16. Validation invariants

Backend/M07 schema phải enforce:

1. `evidenceKind` nằm trong closed catalog version.
2. Kind hợp lệ cho `subject.targetType`.
3. Kind/source type nằm trong compatibility matrix.
4. Kind/lifecycle/availability/payload representation tương thích.
5. Payload shape đúng kind.
6. Một evidence item chỉ có một kind.
7. `evidenceId` unique nhưng không quyết định semantics.
8. Structural singleton kind không xuất hiện hai lần.
9. Missing singleton vẫn có item `NONE` + reason, không biến thành empty data.
10. Reserved kind không nhận `AVAILABLE/PARTIAL`.
11. Default `CONTEXT_ONLY` không được provider nâng thành substantiation.
12. Media reference không được đổi thành verified media observation.
13. Parent context không được attribution sang target author.
14. USER/PAGE kinds không tạo provider projection trong Sprint 1.
15. Claims/signals/rationale không được đổi nhãn thành evidence kind.
16. Unknown/deprecated alias fail closed, không tự đoán mapping ở runtime.
17. Catalog version được pin cùng request/result.
18. Structural completeness không đồng nghĩa rule sufficiency.

## 17. Current documentation gaps

1. `evidence-model.md` vẫn mô tả skeleton cũ có free-text `observation` và `supportsRuleIds`.
2. `evidence-source-types.md` trộn allowed evidence sources với non-evidence context types.
3. M01/M02 dùng `CONTENT_SNAPSHOT`; M05 canonicalize thành `TARGET_TEXT_CONTENT`.
4. M01 missing-media example dùng `MEDIA_OBSERVATION`; M05 tách reference và verified observation.
5. M03 dùng `PARENT_CONTEXT_SNAPSHOT`; M05 tách BLOG và COMMENT context.
6. M04 Common Envelope example dùng abbreviated field aliases khác M01.
7. Current Backend evidence maps chưa có `evidenceKind`.

M05 cập nhật hai tài liệu skeleton đầu để chỉ rõ normative source. M07 chịu trách nhiệm thống nhất
executable field names và fixtures; M08 review toàn bộ cross-document drift.

## 18. Impact map khi implementation được authorize

### Backend

- enum/closed value set cho Evidence Kind;
- typed payload DTO per kind;
- kind/source/target/availability validator;
- split report association khỏi identity;
- reject aliases/unknown kinds;
- route claim/signal khỏi evidence list.

### n8n

- chỉ forward accepted canonical kind;
- không rename/invent kind;
- không biến media reference thành observation;
- model không được tạo evidence item.

### Admin FE

- label kind theo business language;
- phân biệt direct content, context, reference và missing;
- không hiển thị context-only như proof;
- không hiển thị raw restricted locator/PII.

M05 không thực hiện các thay đổi này.

## 19. Nội dung để M06/M07 quyết định

### M06

- required/optional/critical kind theo Rule ID/target;
- khi nào parent context đủ để interpret target;
- counter-evidence requirements;
- media/external authority/jurisdiction/pattern burden;
- missing critical behavior.

### M07

- executable JSON Schema `$defs` cho 14 payload kinds;
- exact Common Envelope field names;
- positive/negative fixtures;
- cardinality validator;
- alias rejection tests;
- catalog/envelope/contract version pinning.

## 20. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M05-D01` | Dùng closed catalog 14 canonical Evidence Kinds | `APPROVED` |
| `M05-D02` | Tách evidenceKind/sourceType/evidenceId | `APPROVED` |
| `M05-D03` | Một evidence item chỉ có một semantic kind | `APPROVED` |
| `M05-D04` | Tách target identity khỏi report-target association | `APPROVED` |
| `M05-D05` | Binding/state kinds mặc định context-only | `APPROVED` |
| `M05-D06` | `TARGET_TEXT_CONTENT` chỉ dùng cho BLOG/COMMENT | `APPROVED` |
| `M05-D07` | Tách media reference khỏi verified media observation | `APPROVED` |
| `M05-D08` | Verified media kind chỉ missing-only trước verified collector | `APPROVED` |
| `M05-D09` | Tách parent BLOG và direct parent COMMENT kinds | `APPROVED` |
| `M05-D10` | Parent context không attribution sang target author | `APPROVED` |
| `M05-D11` | USER/PAGE public profile chỉ context-only/no-provider | `APPROVED` |
| `M05-D12` | Deep target context là missing-only placeholder, không generic available blob | `APPROVED` |
| `M05-D13` | Không tạo generic authoritative-external kind khi chưa có semantic requirement | `APPROVED` |
| `M05-D14` | Reporter claim/derived signal/model output không phải Evidence Kind | `APPROVED` |
| `M05-D15` | Source-kind compatibility là validation bắt buộc | `APPROVED` |
| `M05-D16` | Availability/payload/lifecycle compatibility là validation bắt buộc | `APPROVED` |
| `M05-D17` | Structural cardinality không đồng nghĩa rule sufficiency | `APPROVED` |
| `M05-D18` | Deprecated/unknown alias fail closed | `APPROVED` |
| `M05-D19` | Catalog version pin cùng request/result | `APPROVED` |
| `M05-D20` | Executable shape/version final chốt tại M07, cross-review tại M08 | `APPROVED` |

## 21. Acceptance criteria

- [x] Có closed canonical catalog.
- [x] Kind/source/ID được tách rõ.
- [x] Target applicability và lifecycle rõ.
- [x] Source compatibility matrix tồn tại.
- [x] Payload/availability compatibility rõ.
- [x] Structural bundle cho bốn target tồn tại.
- [x] Provisional slots M02–M04 được map đầy đủ.
- [x] Media reference và verified observation được tách.
- [x] Claim/signal/rationale bị loại khỏi Evidence Kind.
- [x] Context-only không bị trình bày như proof.
- [x] M06/M07 scope không bị lấn.
- [x] Người dùng review và approve `M05-D01`–`M05-D20` bằng `APPROVE_G0-12M-05`.

## 22. Trạng thái gate

```text
Deliverable tồn tại: YES
M02-M04 slot mapping: COMPLETED
Static content review: PASS
User approval: APPROVE_G0-12M-05
Source/runtime mutation: NONE

G0-12M-05 STATUS: COMPLETED
NEXT REQUIRED ACTION: IMPLEMENT_G0_12M_06
```
