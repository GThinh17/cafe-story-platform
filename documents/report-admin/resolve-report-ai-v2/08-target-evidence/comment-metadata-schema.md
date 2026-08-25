# G0-12M-03 — COMMENT Metadata Schema

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Gate | `G0-12M-03` |
| Loại tài liệu | Normative target-schema proposal |
| Trạng thái | `APPROVED` |
| Approval đã nhận | `APPROVE_G0-12M-03` |
| Phụ thuộc | `G0-12M-00`, `G0-12M-01`, `G0-12M-02` |
| Target | `COMMENT` |
| Ngày cập nhật | `2026-07-26` |

Tài liệu này thiết kế COMMENT metadata từ entity, migration và DBML hiện có. Nó chưa sửa source,
database, n8n hoặc Admin UI.

## 2. Bound

### 2.1. Trong phạm vi

- COMMENT target snapshot;
- report-to-COMMENT association;
- persisted user actor và optional Cafe Page actor context;
- direct parent BLOG context;
- direct parent COMMENT context khi target là reply;
- target/comment-context content, state, media-reference metadata;
- provider projection, hashing và freshness;
- mapping sang Common Evidence Envelope;
- current-source/schema gap register.

### 2.2. Ngoài phạm vi

- full conversation thread;
- sibling/descendant comments;
- recursive ancestor chain ngoài direct parent;
- behavioral/harassment/spam aggregation;
- COMMENT rule-specific burden/sufficiency;
- OCR/vision/media fetch;
- executable DTO/JSON Schema;
- source/runtime mutation.

## 3. Source-of-truth đã kiểm tra

| Nguồn | Dữ liệu xác nhận |
|---|---|
| `entity/Comment.java` | Comment/blog/user/actor context/parent/content/media/status/timestamps |
| `CommentServiceImpl.java` | Parent phải cùng BLOG; actor được resolve khi tạo |
| `ActorContextResolver.java` | `USER` hoặc `CAFE_PAGE`; page actor cần quyền quản lý lúc tạo |
| `CommentRepository.java` | Query comment/reply và current entity graph |
| `ContentReport.java` | Report-to-COMMENT association |
| `PostStatus.java` | `DRAFT`, `PUBLISHED`, `HIDDEN`, `REMOVED` |
| `V20250621_01__cafe_page_actor_standardization.sql` | Thêm actor context/page columns vào `comments` |
| `cafestory-schema.dbml` | Base `comments`, `comment_images`, `content_reports` |
| `AdminReportAiResolutionServiceImpl.java` | Runtime COMMENT snapshot/evidence skeleton hiện tại |

### 3.1. Schema drift đã phát hiện

Java entity và Flyway migration có:

```text
comments.actor_context_type
comments.actor_cafe_page_id
```

Nhưng `cafestory-schema.dbml` hiện chưa liệt kê hai cột này.

Kết luận M03:

- intended runtime schema có actor context theo migration/entity;
- DBML hiện stale ở phần COMMENT actor;
- không sửa applied migration;
- trước implementation phải read-only verify schema/Flyway history và cập nhật DBML bằng task riêng;
- nếu live database thiếu hai cột, đó là environment/schema blocker, không xóa field khỏi design để che lỗi.

## 4. Các invariant cốt lõi

1. `subject` luôn là COMMENT bị report.
2. Parent BLOG và parent COMMENT chỉ là context evidence.
3. Không gán nội dung vi phạm của parent cho tác giả target COMMENT.
4. `Comment.user` luôn là persisted underlying user actor.
5. `actorContextType=CAFE_PAGE` chỉ hợp lệ khi có `actorCafePage`.
6. Stored page actor context không tự chứng minh quyền quản lý page tại thời điểm review.
7. Context missing/ambiguous không được chuyển thành `REJECT`.
8. Một comment không chứng minh repeated/coordinated behavior.
9. Raw internal UUID không gửi model mặc định.
10. Raw media URL không phải media observation.
11. Snapshot hash bao target và bounded direct context, không bao report/capture/retry metadata.
12. Freshness phải recompute target + context hash.

## 5. COMMENT Target Snapshot

### 5.1. Logical schema

```json
{
  "targetType": "COMMENT",
  "targetId": "00000000-0000-0000-0000-000000000011",
  "snapshotVersion": "comment-1.0.0",
  "capturedAt": "2026-07-26T02:00:00Z",
  "reportTargetAssociation": {
    "reportId": "00000000-0000-0000-0000-000000000101",
    "targetType": "COMMENT",
    "targetId": "00000000-0000-0000-0000-000000000011",
    "associationStatus": "VERIFIED"
  },
  "identity": {
    "commentId": "00000000-0000-0000-0000-000000000011",
    "blogRef": {
      "type": "BLOG",
      "id": "00000000-0000-0000-0000-000000000001",
      "relationship": "COMMENT_ON_BLOG"
    },
    "underlyingUserRef": {
      "type": "USER",
      "id": "00000000-0000-0000-0000-000000000002",
      "relationship": "PERSISTED_USER_ACTOR"
    },
    "actorContext": {
      "type": "CAFE_PAGE",
      "actorCafePageRef": {
        "type": "CAFE_PAGE",
        "id": "00000000-0000-0000-0000-000000000003",
        "relationship": "DISPLAYED_ACTOR_CONTEXT"
      }
    },
    "parentCommentRef": {
      "type": "COMMENT",
      "id": "00000000-0000-0000-0000-000000000012",
      "relationship": "DIRECT_REPLY_TO"
    }
  },
  "state": {
    "status": "PUBLISHED",
    "createdAt": "2026-07-25T22:30:00Z",
    "updatedAt": "2026-07-25T23:40:00Z"
  },
  "content": {
    "sanitizedText": "Nội dung COMMENT sau sanitization...",
    "sourceLength": 45,
    "capturedLength": 45,
    "truncated": false,
    "truncationLimit": 4000,
    "sanitizerVersion": "comment-content-sanitizer-1.0.0",
    "contentDigest": "sha256:..."
  },
  "context": {
    "scope": "DIRECT_PARENT_AND_BLOG_ONLY",
    "blogContext": {
      "availability": "AVAILABLE",
      "blogRef": "00000000-0000-0000-0000-000000000001",
      "status": "PUBLISHED",
      "sanitizedExcerpt": "Bounded BLOG context...",
      "sourceLength": 5000,
      "capturedLength": 1000,
      "truncated": true,
      "contextDigest": "sha256:..."
    },
    "parentCommentContext": {
      "relationPresent": true,
      "availability": "AVAILABLE",
      "parentCommentRef": "00000000-0000-0000-0000-000000000012",
      "status": "PUBLISHED",
      "parentActorAliasRequired": true,
      "sanitizedExcerpt": "Direct parent COMMENT context...",
      "sourceLength": 120,
      "capturedLength": 120,
      "truncated": false,
      "contextDigest": "sha256:..."
    },
    "recursiveAncestorsIncluded": false,
    "siblingsIncluded": false,
    "descendantsIncluded": false
  },
  "media": {
    "referenceCount": 0,
    "references": []
  },
  "unavailableFields": [],
  "excludedSignals": [
    "commentLikeCount",
    "sameTargetReportCount",
    "priorAiModeration",
    "threadPattern"
  ],
  "snapshotHash": "sha256:..."
}
```

Đây là Backend/audit logical schema. Provider projection dùng aliases và không nhận raw UUID.
Executable JSON Schema thuộc `G0-12M-07`.

### 5.2. Root fields

| Field | Bắt buộc | Nguồn |
|---|---:|---|
| `targetType` | Có | Constant `COMMENT` + report target type |
| `targetId` | Có | `Comment.id` |
| `snapshotVersion` | Có | Backend schema version |
| `capturedAt` | Có | Backend UTC clock |
| `reportTargetAssociation` | Có | `ContentReport.id/targetType/comment.id` |
| `identity` | Có | Comment/blog/user/actor/parent refs |
| `state` | Có | `Comment.status/createdAt/updatedAt` |
| `content` | Có | Sanitized/bounded target COMMENT |
| `context` | Có | Direct BLOG + optional direct parent COMMENT context |
| `media` | Có | `comment_images` reference metadata |
| `unavailableFields` | Có | Structured unavailable markers |
| `excludedSignals` | Có | Audit list; không gửi values |
| `snapshotHash` | Có | Canonical target+context digest |

## 6. Field inclusion matrix

| Source field | Backend snapshot | Model projection | Lý do |
|---|---|---|---|
| `Comment.id` | Raw internal ref | Alias `TARGET-COMMENT-1` | Target identity |
| `Comment.blog.id` | Raw internal ref | Alias `BLOG-CONTEXT-1` | Parent BLOG relation |
| `Comment.user.userId` | Raw internal ref | Alias `ACTOR-USER-1` | Persisted underlying actor |
| `actorContextType` | Include | Include | USER hay CAFE_PAGE context |
| `actorCafePage.id` | Nullable internal ref | Alias/association flag | Displayed page actor context |
| `parentComment.id` | Nullable internal ref | Alias/relationship | Direct reply relation |
| `Comment.content` | Sanitized/bounded | Include | Reported target content |
| `Comment.imageUrls` | Sanitized refs | Reference metadata only | URL không phải media observation |
| `Comment.status` | Include | Include | Target state |
| `Comment.createdAt` | Include | Include | Temporal scope |
| `Comment.updatedAt` | Nullable | Include | Freshness hint |
| User/page display name/avatar | Exclude | Exclude | Không cần cho rule; privacy/bias |
| Like/report/thread counts | Exclude | Exclude | Investigation signals, không phải violation evidence |

## 7. Report association

Required:

```text
reportId
targetType = COMMENT
targetId = commentId
associationStatus = VERIFIED
```

Operational reject khi:

- report target type khác COMMENT;
- `ContentReport.comment` null;
- target ID mismatch;
- COMMENT không load được;
- COMMENT không còn association tới BLOG.

Reporter identity/contact/reputation không nằm trong COMMENT snapshot.

## 8. Actor model

### 8.1. Underlying user

`Comment.user` luôn required theo entity.

Nó chứng minh:

```text
User ID là persisted actor record của COMMENT
```

Nó không chứng minh:

- user-level abuse pattern;
- user hiện còn active;
- user có intent;
- user nên bị suspend.

### 8.2. Actor context

Invariant:

```text
actorContextType = USER
→ actorCafePageRef = null

actorContextType = CAFE_PAGE
→ actorCafePageRef != null
```

Nếu stored data vi phạm invariant:

```text
ACTOR_CONTEXT_INCONSISTENT
→ UNASSESSABLE
→ NEEDS_MANUAL_REVIEW + NO_ACTION
```

`CAFE_PAGE` context chứng minh comment được lưu dưới page actor context. Nó không tự chứng minh:

- page owner trực tiếp viết;
- membership/quyền hiện tại;
- page-wide policy violation;
- `SUSPEND_PAGE` là phù hợp.

### 8.3. Provider projection

Backend giữ raw internal UUID cho audit. Model chỉ nhận request-scoped aliases:

```text
TARGET-COMMENT-1
ACTOR-USER-1
ACTOR-PAGE-1
BLOG-CONTEXT-1
PARENT-COMMENT-1
```

Aliases không ổn định giữa request và không dùng để cross-request profiling.

## 9. Target COMMENT content

Shape:

```json
{
  "sanitizedText": "...",
  "sourceLength": 4500,
  "capturedLength": 4000,
  "truncated": true,
  "truncationLimit": 4000,
  "sanitizerVersion": "comment-content-sanitizer-1.0.0",
  "contentDigest": "sha256:..."
}
```

Rules:

1. `sanitizedText` là exact target text provider đọc.
2. Không duplicate content ở top-level/request/evidence free-text.
3. Hash exact sanitized text.
4. Null/blank/inaccessible không chuyển thành empty content.
5. Truncation phải machine-readable.
6. Emoji/short text không được model tự bổ sung nghĩa ngoài context.
7. Quoted/reported speech ambiguity phải được giữ cho sufficiency/manual behavior.
8. Unstructured content mặc định `containsPersonalData=true` sau provider projection, trừ khi
   deterministic redaction chứng minh ngược lại.

## 10. BLOG context

### 10.1. Vì sao cần

COMMENT có thể là:

- trả lời nội dung BLOG;
- phản bác/cảnh báo/trích dẫn BLOG;
- dùng đại từ hoặc ngữ cảnh chỉ hiểu khi đọc BLOG;
- bản thân trung tính nhưng parent BLOG vi phạm, hoặc ngược lại.

### 10.2. Context shape

```json
{
  "availability": "AVAILABLE",
  "blogAlias": "BLOG-CONTEXT-1",
  "status": "PUBLISHED",
  "sanitizedExcerpt": "...",
  "sourceLength": 5000,
  "capturedLength": 1000,
  "truncated": true,
  "excerptLimit": 1000,
  "contextDigest": "sha256:..."
}
```

M03 đề xuất một canonical limit `1000` characters cho BLOG context fixture/schema. Exact limit được
đóng trong executable schema M07.

Rules:

- source là `Comment.blog`;
- không lấy arbitrary BLOG từ request;
- BLOG status/content digest được giữ trong context;
- excerpt chỉ là `CONTEXT_ONLY`;
- không đưa BLOG author/page/engagement fields vào COMMENT context;
- parent BLOG violation không được gán cho target COMMENT;
- truncation tại material point phải được biểu diễn, không giả complete context.

Nếu BLOG context missing/inaccessible:

```text
availability = MISSING/INACCESSIBLE
context payload = NONE
```

M06 quyết định rule nào critical; khi meaning phụ thuộc BLOG context thì phải manual, không `REJECT`.

## 11. Direct parent COMMENT context

### 11.1. Root COMMENT

Khi `Comment.parentComment == null`:

```text
identity.parentCommentRef = null
context.parentCommentContext = null
```

Đây là explicit root-comment state, không phải missing evidence.

### 11.2. Reply COMMENT

Khi parent tồn tại:

- parent phải thuộc cùng BLOG;
- chỉ capture direct parent;
- capture status, bounded sanitized excerpt và context digest;
- capture parent actor dưới request-scoped alias nếu cần phân biệt same/different actor;
- parent content là `CONTEXT_ONLY`.

Proposed direct-parent excerpt limit: `1000` characters, đóng tại M07.

### 11.3. Parent relation lỗi

```text
parentCommentRef != null
+ parent cannot load / belongs to other BLOG / relation conflicts
→ PARENT_CONTEXT_UNAVAILABLE_OR_INVALID
→ UNASSESSABLE
→ NEEDS_MANUAL_REVIEW + NO_ACTION
```

Không biến trường hợp này thành root COMMENT và không gọi parent content là empty string.

### 11.4. Không full-thread trong Sprint 1

M03 không thu thập:

- grandparent ngoài direct parent;
- siblings;
- replies/descendants;
- thread-wide frequency;
- user/page behavior timeline.

Nếu meaning cần các phần trên, emit missing requirement cho M06/manual review.

## 12. Không attribution sai

Finding cho target COMMENT chỉ được dựa trên observation về target COMMENT, có thể dùng parent context
để giải nghĩa.

Forbidden:

```text
Parent BLOG chứa scam
→ target COMMENT author vi phạm scam
```

```text
Parent COMMENT xúc phạm
→ reply “không đúng” cũng là harassment
```

Allowed:

```text
Target COMMENT chứa statement X
+ parent context giải thích pronoun/quote
→ finding về target COMMENT
```

Observation phải phân biệt:

```text
TARGET_COMMENT_TEXT
PARENT_COMMENT_CONTEXT
PARENT_BLOG_CONTEXT
```

## 13. Media references

`comment_images` hiện chỉ có `comment_id + image_url`; không có stable media ID, byte digest hoặc
verified observation.

Mỗi reference dùng cùng pattern M02:

```json
{
  "mediaRefId": "MR-sha256-prefix",
  "mediaType": "IMAGE",
  "sourceFieldPath": "comment_images.image_url",
  "referenceDigest": "sha256:...",
  "host": "res.cloudinary.com",
  "rawReferenceIncluded": false,
  "availability": "REFERENCE_AVAILABLE",
  "evaluationStatus": "NOT_EVALUATED"
}
```

Rules:

- normalize, redact query/fragment, digest và canonical-sort;
- provider không nhận raw URL mặc định;
- URL/reference không chứng minh media content;
- image-specific claim + `NOT_EVALUATED` emit missing evidence;
- OCR/vision result tương lai là evidence item riêng.

## 14. Snapshot hash

Canonical target/context hash payload:

```json
{
  "snapshotVersion": "comment-1.0.0",
  "identity": {},
  "state": {},
  "content": {},
  "context": {},
  "media": {},
  "unavailableFields": []
}
```

Không hash:

- report ID/association;
- capturedAt;
- request/correlation/retry metadata;
- raw UUID provider aliases;
- model output/explanation;
- excluded signal values.

Hash thay đổi khi:

- target content/status/actor/parent/blog/media thay đổi;
- bounded BLOG/parent COMMENT context thay đổi;
- context availability/truncation thay đổi.

Report association được Backend validate riêng và whole-request HMAC bảo vệ.

## 15. Freshness

Trước persist/reuse:

1. reload report-to-COMMENT association;
2. reload COMMENT, actor context, BLOG và direct parent;
3. verify parent thuộc cùng BLOG;
4. rebuild target content/context/media payload;
5. recompute snapshot hash;
6. compare request/result hash.

Nếu target hoặc material context đổi:

```text
STALE_COMMENT_OR_CONTEXT_SNAPSHOT
→ không reuse recommendation cũ cho snapshot mới
→ rebuild/new request hoặc manual
```

Chỉ so `Comment.updatedAt` không đủ vì BLOG/parent/media có lifecycle riêng.

## 16. COMMENT evidence slots

M03 xác định provisional slots; catalog chính thức chốt tại M05.

| Slot | Source | Intended use |
|---|---|---|
| `EV-COMMENT-IDENTITY` | Comment ID + report association | Rule evaluation candidate |
| `EV-COMMENT-STATE` | Status/timestamps | Rule evaluation/context |
| `EV-COMMENT-CONTENT` | Target sanitized content | Rule evaluation candidate |
| `EV-COMMENT-ACTOR-ASSOCIATION` | User + actor context | Context only |
| `EV-COMMENT-BLOG-CONTEXT` | Parent BLOG bounded context | Context only |
| `EV-COMMENT-PARENT-CONTEXT` | Direct parent COMMENT | Context only; reply only |
| `EV-COMMENT-MEDIA-REF-{id}` | Sanitized media reference | Context/reference only |

Không tạo:

```text
EV-COMMENT-THREAD-PATTERN
EV-COMMENT-POPULARITY
EV-COMMENT-REPORT-COUNT
EV-COMMENT-PRIOR-AI-CONCLUSION
```

## 17. Common Evidence Envelope example

Ví dụ BLOG context evidence của target COMMENT:

```json
{
  "evidenceId": "EV-COMMENT-BLOG-CONTEXT",
  "envelopeVersion": "1.0.0-rc.1",
  "evidenceKind": "PARENT_BLOG_CONTEXT",
  "subject": {
    "targetType": "COMMENT",
    "targetId": "00000000-0000-0000-0000-000000000011",
    "snapshotVersion": "comment-1.0.0",
    "snapshotHash": "sha256:..."
  },
  "source": {
    "sourceType": "PLATFORM_RECORD",
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
    "capturedAt": "2026-07-26T02:00:00Z",
    "sourceUpdatedAt": "2026-07-25T23:30:00Z",
    "transformations": [
      {
        "type": "SANITIZE_AND_BOUND_CONTEXT",
        "version": "1.0.0",
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
    "level": "MEDIUM",
    "reasonCodes": [
      "AUTHORITATIVE_PLATFORM_FIELD",
      "BOUNDED_CONTEXT"
    ]
  },
  "privacy": {
    "classification": "INTERNAL_MODERATION",
    "containsPersonalData": true,
    "redactionStatus": "APPLIED",
    "retentionClass": "REPORT_EVIDENCE_90D"
  },
  "intendedUse": "CONTEXT_ONLY",
  "collectedForRuleIds": [],
  "payload": {
    "representation": "INLINE",
    "mediaType": "application/json",
    "value": {
      "relationship": "PARENT_BLOG_CONTEXT_FOR_COMMENT",
      "sanitizedExcerpt": "Bounded BLOG context...",
      "sourceLength": 5000,
      "capturedLength": 1000,
      "truncated": true,
      "contextDigest": "sha256:..."
    },
    "reference": null,
    "truncated": true
  }
}
```

`subject` vẫn là target COMMENT; BLOG nằm trong `source`.

## 18. Provider projection

| Internal field | Provider value |
|---|---|
| report ID | Omit |
| comment ID | `TARGET-COMMENT-1` |
| underlying user ID | `ACTOR-USER-1` |
| actor Cafe Page ID | `ACTOR-PAGE-1` hoặc association flag |
| BLOG ID | `BLOG-CONTEXT-1` |
| parent COMMENT ID | `PARENT-COMMENT-1` |
| display name/avatar/contact | Omit |
| target/context content | Sanitized/bounded |
| raw image URL | Omit |
| availability/digests/versions | Include khi cần traceability |

Aliases chỉ ổn định trong evidence bundle. n8n hiện gửi nguyên request body cho model nên projection
là implementation gap sau M08.

## 19. Current source gaps

| ID | Gap hiện tại | Tác động |
|---|---|---|
| `M03-GAP-01` | Generic `Map<String,Object>` snapshot | Không enforce COMMENT schema |
| `M03-GAP-02` | Runtime chỉ thêm parent BLOG excerpt | Bỏ qua direct parent COMMENT context |
| `M03-GAP-03` | Parent BLOG excerpt 500 ở snapshot nhưng 1000 ở evidence | Context không canonical |
| `M03-GAP-04` | Target content duplicated ở top-level/snapshot/evidence string | Hash/provider payload có thể lệch |
| `M03-GAP-05` | Actor context/page không nằm trong runtime snapshot | Không audit displayed actor |
| `M03-GAP-06` | DBML thiếu actor context columns đã có trong migration/entity | Schema documentation drift |
| `M03-GAP-07` | CommentRepository graph không fetch BLOG/parent | Dedicated snapshot query/projection chưa có |
| `M03-GAP-08` | Raw media URL list | Không structured privacy/integrity |
| `M03-GAP-09` | Freshness không recompute parent BLOG/comment context | Có thể reuse context stale |
| `M03-GAP-10` | n8n gửi nguyên request JSON | Raw internal IDs chưa có provider projection |

## 20. Nội dung để M06 quyết định

M03 không chốt:

- rule nào bắt buộc BLOG/parent COMMENT context;
- truncation nào là material theo rule;
- direct parent alias có cần cho harassment/hate rule;
- when missing context is critical;
- sufficiency để `RESOLVE` hoặc `REJECT`;
- thread pattern requirement.

M06 map:

```text
COMMENT + ruleId
→ target/context evidence requirements
→ critical/missing/conflicted behavior
→ evidence sufficiency
```

## 21. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M03-D01` | COMMENT có target schema riêng nhưng dùng Common Evidence Envelope | `APPROVED` |
| `M03-D02` | Target COMMENT luôn là subject; BLOG/parent COMMENT chỉ context | `APPROVED` |
| `M03-D03` | Không attribution vi phạm từ context sang target COMMENT author | `APPROVED` |
| `M03-D04` | Report-to-COMMENT association bắt buộc; reporter identity nằm ngoài snapshot | `APPROVED` |
| `M03-D05` | Persisted user actor và actor context phải được biểu diễn riêng | `APPROVED` |
| `M03-D06` | Actor context inconsistent phải manual; page context không cấp page-level finding | `APPROVED` |
| `M03-D07` | Provider dùng request-scoped aliases, không raw internal UUID | `APPROVED` |
| `M03-D08` | Target content dùng một canonical sanitized/bounded/digested payload | `APPROVED` |
| `M03-D09` | BLOG context luôn bounded và CONTEXT_ONLY | `APPROVED` |
| `M03-D10` | Root comment có parent null rõ; reply chỉ capture direct parent | `APPROVED` |
| `M03-D11` | Full thread/siblings/descendants/pattern deferred khỏi Sprint 1 | `APPROVED` |
| `M03-D12` | Media chỉ là sanitized reference trước verified collector | `APPROVED` |
| `M03-D13` | Snapshot hash bao direct context nhưng loại report/capture/retry metadata | `APPROVED` |
| `M03-D14` | Freshness recompute target + BLOG + direct parent context | `APPROVED` |
| `M03-D15` | DBML actor-column drift phải được verify/repair tài liệu, không sửa migration cũ | `APPROVED` |
| `M03-D16` | Rule-specific context burden/sufficiency để M06 | `APPROVED` |
| `M03-D17` | Missing/ambiguous context không được tạo `REJECT` | `APPROVED` |

## 22. Acceptance criteria

- [x] Schema dựa trên Comment entity, service, migration và DBML.
- [x] Reported COMMENT và parent contexts được tách rõ.
- [x] User actor/Cafe Page actor context có invariant.
- [x] Root/reply semantics rõ.
- [x] Không attribution parent violation sang target author.
- [x] Content/media/context có privacy và digest semantics.
- [x] Snapshot hash/freshness bao direct context.
- [x] Provider projection không dùng raw internal UUID.
- [x] Schema drift và current gaps được ghi nhận.
- [x] Không lấn full thread hoặc rule sufficiency.
- [x] Người dùng review và approve `M03-D01`–`M03-D17` bằng `APPROVE_G0-12M-03`.

## 23. Trạng thái gate

```text
Deliverable tồn tại: YES
Source/schema discovery: COMPLETED
Static content review: PASS
User approval: APPROVE_G0-12M-03
Source/runtime mutation: NONE

G0-12M-03 STATUS: COMPLETED
NEXT REQUIRED ACTION: IMPLEMENT_G0_12M_04
```
