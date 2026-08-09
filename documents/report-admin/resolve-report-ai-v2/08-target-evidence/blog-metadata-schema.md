# G0-12M-02 — BLOG Metadata Schema

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Gate | `G0-12M-02` |
| Loại tài liệu | Normative target-schema proposal |
| Trạng thái | `APPROVED` |
| Approval đã ghi nhận | `APPROVE_G0-12M-02` |
| Phụ thuộc | `G0-12M-00`, `G0-12M-01` |
| Target | `BLOG` |
| Ngày cập nhật | `2026-07-26` |

Tài liệu này thiết kế metadata và payload cho BLOG dựa trên source/schema hiện có. Nó chưa sửa Java,
database, n8n hoặc Admin UI.

## 2. Bound

### 2.1. Trong phạm vi

- BLOG target snapshot;
- report-to-BLOG association;
- identity/state/content/author/page/region/media-reference metadata;
- field inclusion/exclusion;
- privacy, hashing và freshness;
- mapping BLOG snapshot sang Common Evidence Envelope;
- gaps giữa source hiện tại và target schema.

### 2.2. Ngoài phạm vi

- COMMENT/USER/CAFE_PAGE schema;
- OCR, vision hoặc fetch media;
- rule-specific burden/sufficiency;
- Evidence Kind Catalog cuối cùng;
- executable JSON Schema/DTO;
- contract version bump;
- source/runtime mutation.

Các nội dung trên lần lượt thuộc `G0-12M-03`–`G0-12M-07` hoặc roadmap sau Sprint 1.

## 3. Source-of-truth đã kiểm tra

| Nguồn | Dữ liệu xác nhận |
|---|---|
| `entity/Blog.java` | ID, author, page, region, content, image URLs, status, flags/counts, timestamps |
| `entity/ContentReport.java` | `targetType`, `blog`, reason/description/status và report association |
| `entity/enums/PostStatus.java` | `DRAFT`, `PUBLISHED`, `HIDDEN`, `REMOVED` |
| `repository/BlogRepository.java` | Author/page graph và `blog_images` URL rows |
| `repository/ContentReportRepository.java` | Report counts và BLOG association queries |
| `cafestory-schema.dbml` | Bảng `blogs`, `blog_images`, `content_reports` |
| `AdminReportAiResolutionServiceImpl.java` | Snapshot/evidence skeleton runtime hiện tại |

### 3.1. Phát hiện chênh lệch tài liệu

BLOG entity/schema hiện **không có `title`**. Vì vậy:

- M02 không tạo `title`;
- không gửi empty/fabricated title;
- nếu tương lai thêm title vào domain, phải version BLOG snapshot schema.

G0-11 `blog-minimum-evidence.md` ghi “title and sanitized content/body” được hiểu lại thành
“sanitized content/body”; phần `title` không áp dụng với source hiện tại.

## 4. Nguyên tắc thiết kế

1. Backend là nguồn target snapshot có thẩm quyền.
2. Snapshot chỉ chứa field có thật hoặc state unavailable rõ ràng.
3. Report claim không nằm trong BLOG evidence.
4. Author/page/region chỉ là internal association reference, không suy ra hành vi hay trách nhiệm.
5. Engagement/ranking signal không chứng minh violation và không gửi model.
6. URL ảnh chỉ là media reference, không phải verified media observation.
7. Hash đại diện exact canonical target payload, không chứa report/retry/capture metadata.
8. Revalidation recompute snapshot hash; không chỉ so sánh `updatedAt`.
9. Content là untrusted data, phải sanitize/bound trước provider boundary.
10. Missing/truncated/unverified metadata không tự tạo `REJECT`.

## 5. BLOG Target Snapshot

### 5.1. Logical schema

```json
{
  "targetType": "BLOG",
  "targetId": "00000000-0000-0000-0000-000000000001",
  "snapshotVersion": "blog-1.0.0",
  "capturedAt": "2026-07-26T01:00:00Z",
  "reportTargetAssociation": {
    "reportId": "00000000-0000-0000-0000-000000000101",
    "targetType": "BLOG",
    "targetId": "00000000-0000-0000-0000-000000000001",
    "associationStatus": "VERIFIED"
  },
  "identity": {
    "blogId": "00000000-0000-0000-0000-000000000001",
    "authorRef": {
      "type": "USER",
      "id": "00000000-0000-0000-0000-000000000002",
      "relationship": "AUTHOR"
    },
    "pageRef": {
      "type": "CAFE_PAGE",
      "id": "00000000-0000-0000-0000-000000000003",
      "relationship": "PUBLISHED_ON_BEHALF_OF_PAGE"
    },
    "regionRef": {
      "type": "REGION",
      "id": "00000000-0000-0000-0000-000000000004",
      "relationship": "BLOG_REGION",
      "jurisdictionVerified": false
    }
  },
  "state": {
    "status": "PUBLISHED",
    "createdAt": "2026-07-25T22:00:00Z",
    "updatedAt": "2026-07-25T23:30:00Z"
  },
  "content": {
    "sanitizedText": "Nội dung BLOG sau sanitization...",
    "sourceLength": 42,
    "capturedLength": 42,
    "truncated": false,
    "truncationLimit": 4000,
    "sanitizerVersion": "blog-content-sanitizer-1.0.0",
    "contentDigest": "sha256:..."
  },
  "media": {
    "referenceCount": 1,
    "references": [
      {
        "mediaRefId": "MR-sha256-prefix",
        "mediaType": "IMAGE",
        "sourceFieldPath": "blog_images.image_url",
        "referenceDigest": "sha256:...",
        "host": "res.cloudinary.com",
        "rawReferenceIncluded": false,
        "availability": "REFERENCE_AVAILABLE",
        "evaluationStatus": "NOT_EVALUATED"
      }
    ]
  },
  "unavailableFields": [],
  "excludedSignals": [
    "isPinned",
    "allowComment",
    "likeCount",
    "shareCount",
    "commentCount"
  ],
  "snapshotHash": "sha256:..."
}
```

Đây là logical schema để review. JSON Schema executable thuộc `G0-12M-07`.

Schema trên là Backend/audit snapshot. Provider projection không mặc định nhận raw internal UUID;
quy tắc projection nằm tại mục 15.

### 5.2. Root fields

| Field | Bắt buộc | Nguồn |
|---|---:|---|
| `targetType` | Có | Constant `BLOG` + report target type |
| `targetId` | Có | `Blog.id` |
| `snapshotVersion` | Có | Backend snapshot contract |
| `capturedAt` | Có | Backend UTC clock |
| `reportTargetAssociation` | Có | `ContentReport.id/targetType/blog.id` |
| `identity` | Có | `Blog.id/author/pageId/regionId` |
| `state` | Có | `Blog.status/createdAt/updatedAt` |
| `content` | Có | Sanitized/bounded `Blog.content` |
| `media` | Có | `Blog.imageUrls`/`blog_images` references |
| `unavailableFields` | Có | Structured missing/inaccessible markers |
| `excludedSignals` | Có | Audit-only list of intentionally omitted fields |
| `snapshotHash` | Có | SHA-256 canonical evaluation payload |

## 6. Field inclusion matrix

| Source field | Snapshot | Provider evidence | Lý do |
|---|---|---|---|
| `Blog.id` | Include | Include | Target identity |
| `Blog.author.userId` | Include internal ref | Request-scoped alias/relationship | Chứng minh author association, không gửi raw profile/UUID |
| `Blog.pageId` | Include nullable ref | Request-scoped alias/association flag | Chứng minh page association |
| `Blog.regionId` | Include nullable ref | Alias/availability context only | Không tự chứng minh jurisdiction |
| `Blog.content` | Include sanitized/bounded | Include | Nội dung reported target |
| `Blog.imageUrls` | Include sanitized references | Reference metadata only | URL không phải media observation |
| `Blog.status` | Include | Include | Target state |
| `Blog.createdAt` | Include | Include | Temporal scope |
| `Blog.updatedAt` | Include nullable | Include | Freshness hint |
| `Blog.isPinned` | Exclude value | Exclude | Ranking/admin signal, dễ gây bias |
| `Blog.allowComment` | Exclude value | Exclude | Product setting, không chứng minh violation |
| `Blog.likeCount` | Exclude value | Exclude | Cached popularity signal |
| `Blog.shareCount` | Exclude value | Exclude | Cached popularity signal |
| `Blog.commentCount` | Exclude value | Exclude | Cached engagement signal |
| Title | Không tồn tại | Không tạo | Không phát minh field |

Counts nếu cần cho investigation trong tương lai phải lấy từ source relation/event phù hợp và nằm trong
`investigationSignals`, không chuyển thành evidence.

## 7. Identity và association

### 7.1. Report association

`reportTargetAssociation` bắt buộc:

```text
reportId
targetType = BLOG
targetId = blogId
associationStatus = VERIFIED
```

Backend phải reject/operational-fail khi:

- report target type không phải BLOG;
- `ContentReport.blog` null;
- report `blog.id` khác target ID;
- target association không load/verify được.

Reporter ID, email, username hoặc reputation không nằm trong BLOG snapshot.

### 7.2. Author reference

BLOG luôn có author theo entity constraint.

```json
{
  "type": "USER",
  "id": "internal-uuid",
  "relationship": "AUTHOR"
}
```

Không đưa vào BLOG evidence:

- username/full name;
- email/phone;
- avatar;
- account/session data;
- behavior/strike/report history.

Author association chỉ chứng minh user là author của BLOG snapshot. Nó không chứng minh actor-level
violation và không cho phép suy ra `SUSPEND_USER`.

Internal UUID là pseudonymous identifier, không được mô tả là “không có dữ liệu cá nhân”. Backend
giữ ID để audit/association; provider projection dùng request-scoped alias.

### 7.3. Cafe Page reference

`pageRef` nullable:

- null: BLOG do user đăng không gắn page;
- non-null: BLOG có `page_id`, nhưng vẫn có real user author;
- page association không tự chứng minh page-level responsibility hoặc `SUSPEND_PAGE`.

Không gửi owner/manager/member PII trong BLOG schema.

### 7.4. Region reference

`regionRef` nullable và chỉ là platform association:

```text
jurisdictionVerified = false
```

`regionId` không tự chứng minh:

- nơi giao dịch thực tế;
- legal jurisdiction;
- địa chỉ của author;
- phạm vi điều chỉnh restricted goods.

Rule cần jurisdiction phải chờ evidence requirement tại M06 hoặc manual review.

## 8. State và temporal scope

Allowed BLOG status:

```text
DRAFT
PUBLISHED
HIDDEN
REMOVED
```

Snapshot phải giữ đúng status tại capture time, không đổi `HIDDEN/REMOVED` thành missing content.

`updatedAt` có thể null. Khi null:

- giữ `updatedAt = null`;
- không sao chép `createdAt` vào field `updatedAt`;
- validator có thể dùng `createdAt` làm freshness hint riêng;
- null không đồng nghĩa với stale.

BLOG entity chưa có row-version field. Vì vậy `updatedAt` không đủ làm concurrency token tuyệt đối.
Pre-persist/reuse phải recompute full snapshot hash từ dữ liệu hiện tại.

## 9. Content payload

### 9.1. Required shape

```json
{
  "sanitizedText": "...",
  "sourceLength": 5200,
  "capturedLength": 4000,
  "truncated": true,
  "truncationLimit": 4000,
  "sanitizerVersion": "blog-content-sanitizer-1.0.0",
  "contentDigest": "sha256:..."
}
```

Rules:

1. `Blog.content` là required/non-blank theo entity, nhưng runtime vẫn validate null/blank.
2. `sanitizedText` là exact text gửi provider.
3. `sourceLength` đo source trước truncate, sau decode cần thiết.
4. `capturedLength` đo exact sanitized text gửi đi.
5. `contentDigest` hash exact `sanitizedText`.
6. `truncated=true` bắt buộc ghi limit và transformation.
7. Truncate không được giả là complete evidence.
8. Prompt instruction nằm trong content không có authority.

Nếu content null/blank/inaccessible:

```text
availability = MISSING/INACCESSIBLE
payload representation = NONE
quality = UNUSABLE
```

M06 quyết định field/rule nào critical. Với Sprint 1 hiện tại, content không khả dụng phải manual.

### 9.2. Không duplicate content

Contract mục tiêu chỉ có một canonical BLOG content payload. Không duy trì song song:

```text
top-level contentText
+ targetSnapshot.observableFields.contentText
+ EV-TARGET-CONTENT.observation
```

Compatibility adapter có thể tạm đọc field cũ, nhưng hash/provider input phải chọn một canonical source.

## 10. Media references

### 10.1. Source limitation

`blog_images` hiện chỉ có:

```text
blog_id
image_url
```

Không có stable media row ID, mime type, byte digest, width/height hoặc verified content observation.

### 10.2. Canonical reference

Mỗi URL source được chuyển thành reference metadata:

```json
{
  "mediaRefId": "MR-sha256-prefix",
  "mediaType": "IMAGE",
  "sourceFieldPath": "blog_images.image_url",
  "referenceDigest": "sha256:...",
  "host": "res.cloudinary.com",
  "rawReferenceIncluded": false,
  "availability": "REFERENCE_AVAILABLE",
  "evaluationStatus": "NOT_EVALUATED"
}
```

Rules:

- normalize reference trước digest theo một implementation được test;
- strip/redact query/fragment có thể chứa credential hoặc tracking data;
- `mediaRefId` derive từ normalized reference digest, không dùng list index đơn lẻ;
- duplicate reference được dedupe hoặc có occurrence suffix deterministic;
- sort canonical references trước snapshot hash vì DB chưa có media order column;
- raw URL không gửi model mặc định khi chưa có verified media collector;
- host/reference tồn tại chỉ chứng minh reference, không chứng minh nội dung ảnh;
- image-specific report + `NOT_EVALUATED` phải emit missing evidence state.

OCR/vision/fetch result, nếu có trong tương lai, phải là evidence item riêng có collector/version/digest.

## 11. Snapshot hash

### 11.1. Canonical hash payload

`snapshotHash` hash object target-semantic ổn định:

```json
{
  "snapshotVersion": "blog-1.0.0",
  "identity": {},
  "state": {},
  "content": {},
  "media": {},
  "unavailableFields": []
}
```

Không hash:

- `capturedAt`;
- `reportTargetAssociation`/`reportId`;
- correlation ID;
- request ID;
- transient retry metadata;
- explanation/model output;
- excluded signal values.

Lý do:

- retry cùng target snapshot phải tạo cùng hash;
- hai report khác nhau trên cùng target state có thể dùng cùng target hash, nhưng idempotency key vẫn
  khác vì Backend đã bao `reportId`;
- report association được Backend validate riêng và được whole-request digest/HMAC bảo vệ, không trộn
  identity của report vào identity của target.

### 11.2. Hash semantics

- canonicalization dùng stable field ordering;
- null và missing khác nhau;
- media references được canonical-sort;
- hash bao phủ exact sanitized target content/reference metadata gửi evaluation;
- changed content/status/author/page/region/media ref phải đổi hash;
- hash không phải authentication; HMAC thuộc `G0-12A`.

## 12. BLOG evidence slots

M02 xác định slot target-level; Evidence Kind Catalog chính thức chốt tại M05.

| Provisional slot | Source | Intended use |
|---|---|---|
| `EV-BLOG-IDENTITY` | `blogs.id` + report association | Rule evaluation candidate |
| `EV-BLOG-STATE` | status/timestamps | Rule evaluation/context |
| `EV-BLOG-CONTENT` | sanitized content snapshot | Rule evaluation candidate |
| `EV-BLOG-AUTHOR-ASSOCIATION` | internal author ID | Context only |
| `EV-BLOG-PAGE-ASSOCIATION` | nullable page ID | Context only |
| `EV-BLOG-REGION-ASSOCIATION` | nullable region ID | Context only; not jurisdiction proof |
| `EV-BLOG-MEDIA-REF-{id}` | sanitized media reference | Context/reference only |

Không tạo:

```text
EV-BLOG-POPULARITY
EV-BLOG-REPORT-COUNT
EV-BLOG-PRIOR-AI-CONCLUSION
```

Các dữ liệu này là signal/history, không phải BLOG violation evidence.

## 13. Mapping vào Common Evidence Envelope

Ví dụ `EV-BLOG-CONTENT`:

```json
{
  "evidenceId": "EV-BLOG-CONTENT",
  "envelopeVersion": "1.0.0-rc.1",
  "evidenceKind": "TARGET_TEXT_CONTENT",
  "subject": {
    "targetType": "BLOG",
    "targetId": "00000000-0000-0000-0000-000000000001",
    "snapshotVersion": "blog-1.0.0",
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
    "capturedAt": "2026-07-26T01:00:00Z",
    "sourceUpdatedAt": "2026-07-25T23:30:00Z",
    "transformations": [
      {
        "type": "SANITIZE_CONTENT",
        "version": "1.0.0",
        "materiality": "NON_MATERIAL"
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
    "containsPersonalData": true,
    "redactionStatus": "APPLIED",
    "retentionClass": "REPORT_EVIDENCE_90D"
  },
  "intendedUse": "RULE_EVALUATION_CANDIDATE",
  "collectedForRuleIds": [],
  "payload": {
    "representation": "INLINE",
    "mediaType": "application/json",
    "value": {
      "sanitizedText": "Nội dung BLOG sau sanitization...",
      "sourceLength": 42,
      "capturedLength": 42,
      "truncated": false,
      "truncationLimit": 4000,
      "sanitizerVersion": "blog-content-sanitizer-1.0.0",
      "contentDigest": "sha256:..."
    },
    "reference": null,
    "truncated": false
  }
}
```

Observation/finding không nằm trong object này.

## 14. Freshness và stale behavior

Trước khi persist/reuse recommendation, Backend phải:

1. reload report association;
2. reload BLOG identity/state/content/media references;
3. rebuild canonical BLOG snapshot;
4. recompute snapshot hash;
5. compare với request/result snapshot hash.

Nếu khác:

```text
STALE_TARGET_SNAPSHOT
→ không reuse old result cho snapshot mới
→ không persist finding như current
→ rebuild/new request hoặc NEEDS_MANUAL_REVIEW
```

Chỉ so `updatedAt` là không đủ vì media collection/association có thể thay đổi và BLOG chưa có row version.

## 15. Privacy và prompt-injection boundary

- BLOG content/media references là untrusted.
- Không interpolate content vào system policy.
- Không gửi reporter identity/contact.
- Không gửi author profile/contact/session.
- Không gửi page owner/manager/member data.
- Không gửi engagement counts hoặc ranking state.
- Không log raw content/URL trong error preview.
- Admin UI chỉ hiển thị sanitized evidence; technical digest/reference nằm trong collapsed audit detail.
- Unstructured BLOG content mặc định `containsPersonalData=true` trừ khi deterministic
  sanitizer/classifier chứng minh payload sau redaction không còn personal data.

### 15.1. Backend snapshot và provider projection

| Field | Backend/audit | Model input |
|---|---|---|
| `reportId` | Raw internal UUID | Omit |
| `blogId/targetId` | Raw internal UUID | Request-scoped alias `TARGET-1` |
| `authorRef.id` | Raw internal UUID | Alias `AUTHOR-1` hoặc relationship-only |
| `pageRef.id` | Raw internal UUID khi có | Alias `PAGE-1` hoặc `pageAssociated=true` |
| `regionRef.id` | Raw internal UUID khi có | Alias/availability only; không suy ra jurisdiction |
| content/status/timestamps | Sanitized/bounded | Include khi rule cần |
| raw media URL | Internal source only | Omit trước verified media collector |
| digest/version/availability | Include | Include khi cần traceability |

Alias:

- chỉ ổn định trong một correlation/evidence bundle;
- không cho model liên kết actor/page qua nhiều request;
- không thể đảo ngược về UUID từ provider payload;
- Backend giữ mapping nội bộ, không n8n/model tự tạo authority từ alias.

Hiện n8n gửi toàn bộ request body vào model; đây là implementation gap cần sửa sau M08.

## 16. Current source gaps

| ID | Gap hiện tại | Tác động |
|---|---|---|
| `M02-GAP-01` | Snapshot generic `Map<String,Object>` | Không enforce BLOG schema |
| `M02-GAP-02` | Hash chỉ bao generic `observableFields` | Chưa có canonical BLOG identity/author/page/region/unavailable schema |
| `M02-GAP-03` | `contentText` bị duplicate ở nhiều field | Có thể hash/evaluate khác payload hiển thị |
| `M02-GAP-04` | Evidence content truncate 4000 nhưng target snapshot dùng full content | Hai representations không nhất quán |
| `M02-GAP-05` | Media dùng raw URL list | Chưa structured privacy/integrity/availability |
| `M02-GAP-06` | Evidence state/provenance là free-text | Không machine-validate |
| `M02-GAP-07` | Author/page/region chưa nằm trong snapshot runtime | Context/association audit chưa đủ |
| `M02-GAP-08` | Freshness chủ yếu dựa timestamp/hash cũ | Chưa có dedicated snapshot factory/revalidation |
| `M02-GAP-09` | G0-11 từng yêu cầu title không tồn tại | Documentation drift |
| `M02-GAP-10` | n8n hiện gửi nguyên request JSON cho model | Raw internal IDs chưa có provider projection |

Các gap này là implementation input cho sau `G0-12M-08`; M02 không tự sửa source.

## 17. Nội dung để M06 quyết định

M02 không kết luận:

- một BLOG đủ chứng minh spam pattern hay không;
- region association đủ chứng minh jurisdiction hay không;
- content truncation critical cho rule nào;
- author/page association support hay counter rule;
- media reference bắt buộc cho reason nào;
- burden để `RESOLVE` hoặc `REJECT`.

M06 sẽ map:

```text
targetType + ruleId
→ required/optional/critical evidence slots
→ missing behavior
→ sufficiency result
```

## 18. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M02-D01` | BLOG dùng target snapshot schema riêng nhưng evidence giữ Common Envelope | `APPROVED` |
| `M02-D02` | Chỉ dùng field có thật; không tạo `title` | `APPROVED` |
| `M02-D03` | Author là internal association reference; không gửi profile/contact và model chỉ nhận alias | `APPROVED` |
| `M02-D04` | Page/region là nullable context refs, không chứng minh page liability/jurisdiction | `APPROVED` |
| `M02-D05` | Loại isPinned/allowComment/engagement counts khỏi provider evidence | `APPROVED` |
| `M02-D06` | Content có sanitized text, length, truncation và digest thống nhất | `APPROVED` |
| `M02-D07` | Không duplicate canonical content ở nhiều field | `APPROVED` |
| `M02-D08` | Media chỉ là sanitized reference; raw URL không gửi model mặc định | `APPROVED` |
| `M02-D09` | Media reference ID/digest/order phải deterministic | `APPROVED` |
| `M02-D10` | Target snapshot hash loại reportId/capturedAt/retry metadata nhưng bao semantic target payload | `APPROVED` |
| `M02-D11` | Freshness recompute full snapshot hash, không chỉ dựa updatedAt | `APPROVED` |
| `M02-D12` | Report association bắt buộc; reporter identity nằm ngoài BLOG snapshot | `APPROVED` |
| `M02-D13` | Rule-specific sufficiency được giữ cho M06 | `APPROVED` |
| `M02-D14` | Provider projection dùng request-scoped alias, không gửi raw internal UUID mặc định | `APPROVED` |

## 19. Acceptance criteria

- [x] Field schema dựa trên `Blog`, `ContentReport` và DBML thật.
- [x] Không phát minh `title`.
- [x] Có inclusion/exclusion matrix.
- [x] Author/page/region dùng internal refs cho audit và request-scoped alias cho model.
- [x] Content có truncation/digest semantics rõ.
- [x] Media URL được phân loại là reference, không phải observation.
- [x] Snapshot hash và freshness semantics rõ.
- [x] Có mapping vào Common Evidence Envelope.
- [x] Có current-source gap register.
- [x] Không lấn sang rule-specific sufficiency hoặc target khác.
- [x] Người dùng review và approve `M02-D01`–`M02-D14`.

## 20. Trạng thái gate

```text
Deliverable tồn tại: YES
Source/schema discovery: COMPLETED
Static content review: PASS
User approval: APPROVE_G0-12M-02
Source/runtime mutation: NONE

G0-12M-02 STATUS: COMPLETED
NEXT STEP: PREPARE_G0-12M-03_FOR_REVIEW
```
