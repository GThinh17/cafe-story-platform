# G0-12M-04 — USER/CAFE_PAGE Minimal Manual-only Metadata Schema

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12M-04` |
| Trạng thái | `APPROVED` |
| Approval đã nhận | `APPROVE_G0-12M-04` |
| Phụ thuộc | `G0-12M-00`–`G0-12M-03`, `BD-011` |
| Contract | `Admin Report AI Contract 2.0` |
| Automation mode | `A0_RECOMMEND_ONLY` |
| Target | `USER`, `CAFE_PAGE` |
| Provider eligibility | `NEVER_IN_SPRINT_1` |
| Source/runtime mutation | `NONE` |

Tài liệu này chuẩn hóa metadata tối thiểu để Backend tạo một kết quả local manual-only có thể
audit. Nó không biến USER hoặc CAFE_PAGE thành target được AI đánh giá sâu.

## 2. Bound

### 2.1. Trong phạm vi

- report-to-USER và report-to-CAFE_PAGE association;
- target identity và trạng thái có thật trong source;
- public profile/page fields tối thiểu để Admin định hướng;
- owner association tối thiểu của CAFE_PAGE;
- privacy classification và field exclusion;
- local manual-only routing, decision origin và zero-provider invariant;
- snapshot hash, freshness và unavailable-field semantics;
- provisional evidence slots để M05 chuẩn hóa.

### 2.2. Ngoài phạm vi

- suy luận repeated/coordinated abuse;
- gom BLOG/COMMENT thành actor-level violation;
- user behavior history, role history, device, session, IP hoặc login risk;
- merchant/KYC/license/ownership verification;
- page-member investigation, quorum hoặc suspension workflow;
- jurisdiction conclusion từ address/region tự khai;
- rule-specific evidence sufficiency;
- provider prompt/projection cho USER/CAFE_PAGE;
- source code, database migration, n8n hoặc UI implementation.

## 3. Source-of-truth đã kiểm tra

| Source | Fact được dùng |
|---|---|
| `User.java` | UUID, public profile fields, cached counters, `accountStatus`, privacy flag; không có timestamps/version |
| `CafePage.java` | owner, region, public page fields, `status`, `pageActive`, expiry, counters, timestamps |
| `PageMember.java` | membership có role/status riêng và lifecycle riêng |
| `ContentReport.java` | target USER dùng `reportedUser`; target CAFE_PAGE dùng `cafePage` |
| `ContentReportServiceImpl.java` | cấm self-report/own-page report; enforce duplicate active-report guard |
| `AdminReportAiResolutionServiceImpl.java` | USER/PAGE hiện hard-clamp local, không gọi webhook |
| `AdminUserServiceImpl.java` | authoritative user enforcement state hiện là `accountStatus` boolean |
| `AdminCafePageServiceImpl.java` | admin status update đồng bộ `status` và `pageActive` |
| `PaymentServiceImpl.java` | subscription có thể đổi `pageActive`/expiry độc lập với lifecycle status |
| `cafestory-schema.dbml` | users, cafe_pages, page_members và content_reports associations |
| `g0-10k-target-depth.md` | USER/PAGE chỉ common snapshot + manual escalation trong Sprint 1 |

### 3.1. Source/schema gaps phải giữ rõ

1. `User` không có `createdAt`, `updatedAt` hoặc row version.
2. `users` DBML chưa có `hide_cafe_page_on_profile` dù entity có field này.
3. `accountStatus` là boolean; không có enum `ACTIVE/INACTIVE/BANNED` ở entity hiện tại.
4. CAFE_PAGE có `status`, `pageActive`, `pageExpiresAt`; một field không biểu diễn đủ state.
5. Page membership thay đổi không bắt buộc làm đổi `CafePage.updatedAt`.
6. Current manual guard vẫn dựng generic webhook request, content text và media list trước khi skip.
7. Current response không có `providerCalled` hoặc `decisionOrigin`.
8. `modelName=backend-policy-guard` có thể bị UI/người đọc hiểu nhầm là kết quả từ AI model.
9. Current USER snapshot có timestamps `null` nhưng chưa có unavailable reason.
10. Current CAFE_PAGE target status chỉ dùng `PageStatus`, bỏ `pageActive` và expiry.
11. Current generic execution constraints vẫn liệt kê content actions cho mọi target.
12. Current generic evidence dùng free-text maps thay vì typed Common Evidence Envelope.

Các gap trên là implementation input. M04 không tự sửa source hoặc DBML.

## 4. Invariant chung

1. USER/CAFE_PAGE luôn route `LOCAL_MANUAL_ONLY` trong Sprint 1.
2. Không serialize provider projection và không gọi n8n/provider.
3. Kết quả luôn là:
   `NEEDS_MANUAL_REVIEW + NO_ACTION`.
4. `findings=[]`; local guard không tạo policy finding.
5. Public profile/page text chỉ là `CONTEXT_ONLY`, không phải bằng chứng vi phạm.
6. Reporter reason/description vẫn là `REPORTER_CLAIM`, không nằm trong target evidence.
7. Một BLOG/COMMENT đơn lẻ không đủ tạo USER/PAGE-level finding.
8. Raw contact/authentication data không đi vào snapshot.
9. Null, missing, source-field-absent và not-collected phải phân biệt.
10. Hash không thay thế HMAC và không chứng minh sự thật ngoài source record.

## 5. Common local manual-only result

```json
{
  "contractVersion": "2.0",
  "decisionOrigin": "BACKEND_POLICY_GUARD",
  "route": "LOCAL_MANUAL_ONLY",
  "providerEligibility": "NOT_ELIGIBLE_IN_SPRINT_1",
  "providerCalled": false,
  "recommendationState": "NEEDS_MANUAL_REVIEW",
  "reportDecision": "NEEDS_MANUAL_REVIEW",
  "candidateTargetAction": "NO_ACTION",
  "allowedCandidateActions": ["NO_ACTION"],
  "findings": [],
  "blockedReasons": ["TARGET_DEEP_POLICY_NOT_IN_SPRINT1"],
  "evidenceSufficiency": "UNASSESSABLE",
  "violationLikelihood": "UNKNOWN",
  "harmSeverity": "UNKNOWN",
  "explanationKind": "DETERMINISTIC_SYSTEM_MESSAGE"
}
```

### 5.1. Semantics

- Đây là policy routing result, không phải AI recommendation.
- `explanationKind` phải giúp UI ghi đúng “Hệ thống chuyển review thủ công”.
- `providerCalled=false` là field audit bắt buộc, không chỉ suy ra từ model name.
- `evidenceSufficiency=UNASSESSABLE` không có nghĩa mọi evidence item có chất lượng thấp.
- Không dùng `REJECT`, vì thiếu target-deep evidence không chứng minh report sai.
- Không dùng `RESOLVE`, vì chưa có target-level finding đủ burden.
- Không schedule delayed auto-apply và không đóng report.

## 6. Report-to-target association

### 6.1. USER

```json
{
  "reportId": "00000000-0000-0000-0000-000000000101",
  "targetType": "USER",
  "selectedTargetField": "reportedUser",
  "selectedTargetColumn": "reported_user_id",
  "associatedTargetRef": "00000000-0000-0000-0000-000000000201",
  "associationValidated": true
}
```

### 6.2. CAFE_PAGE

```json
{
  "reportId": "00000000-0000-0000-0000-000000000102",
  "targetType": "CAFE_PAGE",
  "selectedTargetField": "cafePage",
  "selectedTargetColumn": "cafe_page_id",
  "associatedTargetRef": "00000000-0000-0000-0000-000000000202",
  "associationValidated": true
}
```

Validation bắt buộc:

- đúng một target association phù hợp `targetType`;
- associated target ID bằng snapshot target ID;
- report còn `OPEN` hoặc `REVIEWING`;
- target còn đọc được;
- reporter identity không được copy vào target snapshot;
- self-report guard là intake invariant, không phải evidence report đúng/sai.

Association được verify và bảo vệ ở whole-request boundary, nhưng không nằm trong target hash.

## 7. USER minimal snapshot

### 7.1. Logical schema

```json
{
  "targetType": "USER",
  "snapshotVersion": "user-manual-1.0.0",
  "capturedAt": "2026-07-26T10:00:00Z",
  "identity": {
    "userRef": "00000000-0000-0000-0000-000000000201",
    "referenceKind": "INTERNAL_UUID"
  },
  "state": {
    "accountStatus": true,
    "sourceRepresentation": "BOOLEAN",
    "createdAt": null,
    "updatedAt": null,
    "versionSignal": "UNAVAILABLE"
  },
  "publicProfileContext": {
    "userName": {
      "availability": "AVAILABLE",
      "value": "bounded-user-name",
      "classification": "PUBLIC_PROFILE_CONTEXT",
      "contextOnly": true
    },
    "displayName": {
      "availability": "AVAILABLE",
      "value": "bounded-display-name",
      "classification": "PUBLIC_PROFILE_CONTEXT",
      "contextOnly": true
    },
    "description": {
      "availability": "AVAILABLE",
      "value": "bounded-sanitized-description",
      "truncated": false,
      "digest": "sha256:example",
      "classification": "PUBLIC_PROFILE_CONTEXT",
      "contextOnly": true
    },
    "avatar": {
      "availability": "REFERENCE_ONLY",
      "referenceDigest": "sha256:example",
      "fetched": false,
      "evaluated": false,
      "contextOnly": true
    }
  },
  "privacy": {
    "providerEligible": false,
    "providerProjectionCreated": false,
    "excludedFields": [
      "userPassword",
      "userEmail",
      "userPhone",
      "roles",
      "refreshTokens",
      "sessions",
      "exactRegion"
    ],
    "relatedCafePageContextCollected": false
  },
  "freshness": {
    "basis": "INCLUDED_FIELD_REHASH",
    "sourceTimestampAvailable": false,
    "strength": "LIMITED"
  },
  "unavailableFields": [
    {
      "field": "state.createdAt",
      "reason": "SOURCE_FIELD_ABSENT"
    },
    {
      "field": "state.updatedAt",
      "reason": "SOURCE_FIELD_ABSENT"
    },
    {
      "field": "state.versionSignal",
      "reason": "SOURCE_FIELD_ABSENT"
    }
  ],
  "snapshotHash": "sha256:canonical-target-payload"
}
```

### 7.2. USER field inclusion

| Source field | Snapshot | Mục đích/ghi chú |
|---|---|---|
| `userId` | Include | Target identity |
| `accountStatus` | Include | Enforcement state đúng theo source boolean |
| `userName` | Include, bounded | Admin orientation; context only |
| `userFullName` | Include, bounded | Admin orientation; context only |
| `userDescription` | Include, sanitized/bounded/digested | Untrusted public profile context |
| `userAvatar` | Reference metadata only | Không fetch/evaluate |
| `hideCafePageOnProfile` | Không đưa vào moderation payload | Privacy control; không được coi là concealment |
| `userLike`, `userFollower` | Exclude | Cached popularity signal, không phải violation evidence |
| `region` | Exclude khỏi minimal snapshot | Không cần cho manual route; exact location nhạy cảm |
| `userEmail`, `userPhone` | Exclude | Direct contact PII |
| `userPassword` | Prohibited | Authentication secret |
| roles/refresh/session | Prohibited trong M04 | Authorization/security context ngoài phạm vi |

### 7.3. USER state semantics

Không normalize:

```text
accountStatus=true  → ACTIVE
accountStatus=false → BANNED
```

vì source chỉ chứng minh boolean enabled/disabled, không chứng minh lý do hoặc lifecycle state.

Canonical representation:

```text
sourceRepresentation=BOOLEAN
accountStatus=<true|false>
normalizedLifecycleStatus=UNAVAILABLE
```

`accountStatus=false` cũng không phải evidence report hiện tại đúng; nó chỉ là state tại thời điểm
capture.

### 7.4. USER freshness limitation

Vì entity không có timestamp/version:

1. reload report association và User row;
2. rebuild đúng included field set;
3. canonicalize và recompute hash;
4. so hash trước persist/reuse;
5. nếu khác, tạo snapshot mới hoặc giữ manual;
6. luôn ghi `freshness.strength=LIMITED`.

Field rehash phát hiện thay đổi trên field được include nhưng không chứng minh row không đổi ở field bị
exclude. Không được ghi `FRESH_STRONG` cho USER.

## 8. CAFE_PAGE minimal snapshot

### 8.1. Logical schema

```json
{
  "targetType": "CAFE_PAGE",
  "snapshotVersion": "cafe-page-manual-1.0.0",
  "capturedAt": "2026-07-26T10:00:00Z",
  "identity": {
    "pageRef": "00000000-0000-0000-0000-000000000202",
    "referenceKind": "INTERNAL_UUID"
  },
  "state": {
    "lifecycleStatus": "ACTIVE",
    "subscriptionActive": true,
    "subscriptionExpiresAt": "2026-12-31T23:59:59",
    "createdAt": "2026-01-01T08:00:00",
    "updatedAt": "2026-07-26T09:30:00",
    "stateConsistency": "CONSISTENT"
  },
  "ownershipContext": {
    "primaryOwnerRef": "00000000-0000-0000-0000-000000000203",
    "ownerAssociationAvailability": "AVAILABLE",
    "ownerPiiIncluded": false,
    "privilegedMemberDetails": {
      "availability": "NOT_COLLECTED",
      "reason": "TARGET_DEEP_CONTEXT_OUT_OF_SCOPE"
    }
  },
  "publicPageContext": {
    "name": {
      "availability": "AVAILABLE",
      "value": "bounded-page-name",
      "contextOnly": true
    },
    "description": {
      "availability": "AVAILABLE",
      "value": "bounded-sanitized-description",
      "truncated": false,
      "digest": "sha256:example",
      "contextOnly": true
    },
    "declaredAddress": {
      "availability": "AVAILABLE",
      "value": "bounded-sanitized-address",
      "classification": "PAGE_DECLARED_LOCATION",
      "verified": false,
      "jurisdictionProof": false,
      "contextOnly": true
    },
    "regionRef": {
      "availability": "AVAILABLE",
      "reference": "00000000-0000-0000-0000-000000000204",
      "jurisdictionProof": false,
      "contextOnly": true
    }
  },
  "media": {
    "avatar": {
      "availability": "REFERENCE_ONLY",
      "referenceDigest": "sha256:example-avatar",
      "fetched": false,
      "evaluated": false
    },
    "cover": {
      "availability": "REFERENCE_ONLY",
      "referenceDigest": "sha256:example-cover",
      "fetched": false,
      "evaluated": false
    }
  },
  "privacy": {
    "providerEligible": false,
    "providerProjectionCreated": false,
    "ownerContactPiiIncluded": false,
    "memberPiiIncluded": false
  },
  "freshness": {
    "basis": "UPDATED_AT_AND_INCLUDED_FIELD_REHASH",
    "sourceTimestampAvailable": true,
    "strength": "BOUNDED"
  },
  "unavailableFields": [],
  "snapshotHash": "sha256:canonical-target-payload"
}
```

### 8.2. CAFE_PAGE field inclusion

| Source field/association | Snapshot | Mục đích/ghi chú |
|---|---|---|
| `id` | Include | Target identity |
| `status` | Include | Lifecycle/moderation status |
| `pageActive` | Include riêng | Subscription/availability signal; không thay `status` |
| `pageExpiresAt` | Include riêng | Subscription temporal signal |
| `createdAt`, `updatedAt` | Include | Temporal/freshness |
| `owner.userId` | Include as restricted reference | Ownership association only |
| owner profile/contact | Exclude | Không cần và có PII |
| page members | Not collected | Deep ownership/management investigation deferred |
| `name` | Include, bounded | Public page orientation; context only |
| `description` | Include, sanitized/bounded | Untrusted context only |
| `address` | Include, bounded | Declared context; không phải verified location/license |
| `region.id` | Include as reference | Context only; không phải jurisdiction proof |
| `avatarUrl`, `coverUrl` | Reference digest only | Không fetch/evaluate |
| `likeCount`, `followerCount` | Exclude | Popularity/cached counters không chứng minh violation |
| `maxMembers` | Exclude | Subscription capacity, không cần cho manual routing |

### 8.3. State vector, không synthetic status

Không nén:

```text
status + pageActive + pageExpiresAt → ACTIVE/SUSPENDED
```

Phải giữ ba field độc lập. `stateConsistency` chỉ là deterministic validation:

| Trường hợp | State consistency | Hành vi |
|---|---|---|
| `status=ACTIVE`, `pageActive=true`, expiry hợp lệ/null theo contract | `CONSISTENT` | Manual-only bình thường |
| `status=SUSPENDED`, `pageActive=true` | `INCONSISTENT` | Thêm blocked reason, không tự sửa |
| `pageActive=true`, expiry đã qua | `INCONSISTENT_OR_STALE_SUBSCRIPTION` | Manual review |
| một field không đọc được | `UNASSESSABLE` | Explicit unavailable |

`stateConsistency` không phải finding vi phạm nội dung và không cấp quyền sửa page.

### 8.4. Ownership semantics

- `primaryOwnerRef` chứng minh association trong platform tại thời điểm capture.
- Nó không chứng minh merchant identity, legal ownership hoặc trách nhiệm cho mọi content.
- Không copy owner email/phone/name vào target evidence.
- Không thu thập toàn bộ member refs trong M04.
- Thay đổi PageMember không được suy từ `CafePage.updatedAt`; deep ownership freshness để Sprint 4.

### 8.5. Address và region

Address/region là dữ liệu platform hoặc dữ liệu page tự khai:

- dùng để Admin định hướng;
- không dùng làm license/jurisdiction proof;
- không tự suy merchant legitimacy;
- không gửi provider;
- phải sanitize và bounded trước khi đưa vào restricted Admin context.

## 9. Provider and privacy boundary

### 9.1. Provider projection

```json
{
  "targetType": "USER",
  "providerProjection": null,
  "projectionStatus": "NOT_CREATED_POLICY_GUARD",
  "providerCalled": false
}
```

CAFE_PAGE dùng cùng invariant.

Implementation tương lai phải branch trước khi tạo provider DTO:

```text
authorize + load + validate report association
→ detect USER/CAFE_PAGE
→ build restricted local snapshot
→ persist deterministic manual-only result
→ return
```

Không dùng:

```text
build generic webhook request with profile/address/media
→ later decide not to call webhook
```

### 9.2. Privacy zones

| Zone | USER | CAFE_PAGE |
|---|---|---|
| Restricted Backend snapshot | Minimal public context + state | Minimal public page + state + owner ref |
| Admin API projection | Chỉ field cần cho manual navigation | Chỉ field cần cho manual navigation |
| Logs/metrics | IDs hashed/correlation only | IDs hashed/correlation only |
| n8n/provider | Không có payload | Không có payload |
| Raw audit export | RBAC + explicit operational need | RBAC + explicit operational need |

## 10. Snapshot hash

### 10.1. USER hash payload

```json
{
  "snapshotVersion": "user-manual-1.0.0",
  "identity": {},
  "state": {},
  "publicProfileContext": {},
  "privacy": {
    "providerEligible": false,
    "relatedCafePageContextCollected": false
  },
  "unavailableFields": []
}
```

### 10.2. CAFE_PAGE hash payload

```json
{
  "snapshotVersion": "cafe-page-manual-1.0.0",
  "identity": {},
  "state": {},
  "ownershipContext": {},
  "publicPageContext": {},
  "media": {},
  "privacy": {
    "providerEligible": false
  },
  "unavailableFields": []
}
```

Không hash:

- `capturedAt`;
- report ID/association;
- reporter claim/description;
- correlation/retry metadata;
- deterministic explanation;
- model/provider fields;
- excluded contact/auth/session data.

Hash thay đổi khi included target state/profile/ownership/media reference thay đổi. Report association
được validate riêng; idempotency key vẫn bao `reportId`.

## 11. Freshness và stale behavior

### 11.1. USER

```text
re-read association + User
→ recompute included-field hash
→ same hash: may persist/reuse local manual result with LIMITED freshness
→ changed hash: new snapshot/idempotency result
```

### 11.2. CAFE_PAGE

```text
re-read association + CafePage + owner reference
→ compare updatedAt
→ recompute included-field hash
→ validate state vector
→ changed/inconsistent: manual-only + explicit blocked reason
```

Suggested blocked reasons:

- `TARGET_DEEP_POLICY_NOT_IN_SPRINT1`;
- `USER_VERSION_SIGNAL_UNAVAILABLE`;
- `TARGET_SNAPSHOT_CHANGED`;
- `PAGE_STATE_INCONSISTENT`;
- `PAGE_OWNER_ASSOCIATION_UNAVAILABLE`;
- `TARGET_UNAVAILABLE`.

Các reason này mô tả routing/operational state, không phải policy finding.

## 12. Provisional evidence slots

M04 chỉ định nghĩa slot. M05 chốt Evidence Kind Catalog và payload kind.

### 12.1. USER

| Slot | Source | Use |
|---|---|---|
| `EV-USER-IDENTITY` | `users.user_id` + association | Target binding |
| `EV-USER-STATE` | `account_status` | Admin context only |
| `EV-USER-PUBLIC-PROFILE` | bounded public profile | Orientation/context only |
| `EV-USER-DEEP-REVIEW` | unavailable marker | Giải thích manual-only burden chưa có |

### 12.2. CAFE_PAGE

| Slot | Source | Use |
|---|---|---|
| `EV-PAGE-IDENTITY` | `cafe_pages.id` + association | Target binding |
| `EV-PAGE-STATE` | status/active/expiry/timestamps | Admin context/state validation |
| `EV-PAGE-OWNER-ASSOCIATION` | owner FK | Restricted context only |
| `EV-PAGE-PUBLIC-PROFILE` | bounded page fields | Orientation/context only |
| `EV-PAGE-MEDIA-REFERENCE` | sanitized reference metadata | Reference existence only |
| `EV-PAGE-DEEP-REVIEW` | unavailable marker | Giải thích manual-only burden chưa có |

`EV-*-DEEP-REVIEW` không được giả lập thành payload rỗng hoặc quality thấp. Nó phải có availability
phù hợp như `NOT_COLLECTED_OUT_OF_SCOPE`.

## 13. Common Evidence Envelope example

```json
{
  "evidenceId": "EV-USER-STATE",
  "envelopeVersion": "1.0.0-rc.1",
  "evidenceKind": "TARGET_STATE",
  "subject": {
    "targetType": "USER",
    "targetAlias": "target-user-1",
    "snapshotVersion": "user-manual-1.0.0",
    "snapshotHash": "sha256:canonical-target-payload"
  },
  "source": {
    "sourceType": "PLATFORM_RECORD",
    "sourceSystem": "CAFE_STORY_BACKEND",
    "sourceEntityType": "USER",
    "sourceEntityAlias": "target-user-1",
    "sourceFieldPath": "accountStatus",
    "verificationStatus": "SYSTEM_CAPTURED",
    "authorityScope": "PLATFORM_OWNED_FIELD"
  },
  "capture": {
    "collectorName": "BackendPolicyGuard",
    "collectorVersion": "2.0.0",
    "capturedAt": "2026-07-26T10:00:00Z",
    "sourceUpdatedAt": null,
    "transformations": []
  },
  "integrity": {
    "canonicalization": "JCS",
    "digestAlgorithm": "SHA-256",
    "payloadDigest": "sha256:example",
    "sourceDigest": null
  },
  "availability": {
    "status": "AVAILABLE",
    "reasonCode": null
  },
  "quality": {
    "level": "HIGH",
    "reasonCodes": ["DIRECT_PLATFORM_RECORD"]
  },
  "privacy": {
    "classification": "RESTRICTED",
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
      "accountStatus": true,
      "sourceRepresentation": "BOOLEAN",
      "versionSignal": "UNAVAILABLE"
    },
    "reference": null,
    "truncated": false
  }
}
```

Evidence item quality `HIGH` ở đây chỉ nói account state đến trực tiếp từ platform record. Bundle vẫn
`UNASSESSABLE` cho actor-level violation vì target-deep evidence chưa được thiết kế.

## 14. Current implementation gaps

| ID | Hiện trạng | M04 yêu cầu |
|---|---|---|
| `M04-GAP-01` | Manual target vẫn dựng webhook request | Branch trước provider DTO |
| `M04-GAP-02` | Không có `providerCalled` | Persist/return explicit false |
| `M04-GAP-03` | Không có `decisionOrigin` | `BACKEND_POLICY_GUARD` |
| `M04-GAP-04` | `modelName` dễ gây hiểu nhầm | UI/audit phân biệt deterministic system result |
| `M04-GAP-05` | USER timestamps null không reason | Typed unavailable fields + limited freshness |
| `M04-GAP-06` | USER state có thể bị normalize quá mức | Giữ boolean source representation |
| `M04-GAP-07` | PAGE chỉ capture `status` | Capture state vector |
| `M04-GAP-08` | Generic allowed actions có content actions | USER/PAGE chỉ `NO_ACTION` |
| `M04-GAP-09` | Generic profile text/media fields | Typed minimal restricted snapshot |
| `M04-GAP-10` | Evidence là free-text map | Common Evidence Envelope |
| `M04-GAP-11` | Overall LOW dễ trộn quality/sufficiency | Per-item quality, bundle unassessable |
| `M04-GAP-12` | DBML thiếu user privacy flag | Verify live schema rồi repair DBML riêng |

## 15. Nội dung để gate sau

| Nội dung | Gate |
|---|---|
| Evidence Kind và payload schema chính thức | `G0-12M-05` |
| Rule-specific required/optional/critical evidence | `G0-12M-06` |
| Executable JSON Schema, fixtures, version bump | `G0-12M-07` |
| Cross-target contract review | `G0-12M-08` |
| USER behavior/account-history evidence | Sprint 4 gate riêng |
| CAFE_PAGE merchant/ownership/member/jurisdiction evidence | Sprint 4 gate riêng |
| Suspension/restore recommendation và quorum | Sprint 4/business approval |

## 16. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M04-D01` | USER/PAGE dùng Common Evidence Envelope nhưng có minimal payload riêng | `APPROVED` |
| `M04-D02` | Route local trước khi tạo provider DTO; không có provider projection | `APPROVED` |
| `M04-D03` | Canonical result luôn manual/no-action/no-finding | `APPROVED` |
| `M04-D04` | Ghi explicit `decisionOrigin` và `providerCalled=false` | `APPROVED` |
| `M04-D05` | Report association phải bind đúng FK/target và được revalidate | `APPROVED` |
| `M04-D06` | USER giữ `accountStatus` boolean, không phát minh lifecycle enum | `APPROVED` |
| `M04-D07` | USER freshness chỉ `LIMITED` bằng included-field rehash | `APPROVED` |
| `M04-D08` | Email/phone/password/roles/session/exact region bị loại | `APPROVED` |
| `M04-D09` | Public user profile chỉ context, không substantiation | `APPROVED` |
| `M04-D10` | User privacy flag không phải guilt signal và không mở rộng page context | `APPROVED` |
| `M04-D11` | PAGE giữ riêng status/pageActive/expiry; inconsistency chỉ route manual | `APPROVED` |
| `M04-D12` | PAGE owner chỉ restricted ref; member detail không collect trong M04 | `APPROVED` |
| `M04-D13` | Address/region tự khai không phải merchant/jurisdiction proof | `APPROVED` |
| `M04-D14` | Avatar/cover chỉ reference, không media evidence | `APPROVED` |
| `M04-D15` | Hash target-semantic payload; report association validate riêng | `APPROVED` |
| `M04-D16` | Không cross-target attribution hoặc single-content actor suspension | `APPROVED` |
| `M04-D17` | Per-item quality không tự thành bundle sufficiency | `APPROVED` |
| `M04-D18` | DBML drift chỉ repair sau live verification; không sửa migration cũ | `APPROVED` |

## 17. Acceptance criteria

- [x] Schema dựa trên entity/service/repository/DBML hiện có.
- [x] USER và CAFE_PAGE vẫn manual-only.
- [x] Không provider projection/call.
- [x] Report association rõ cho hai target.
- [x] USER boolean state và timestamp limitation rõ.
- [x] CAFE_PAGE state vector rõ.
- [x] PII/auth/role/session exclusion rõ.
- [x] Public context không bị biến thành violation evidence.
- [x] Hash/freshness limitation rõ.
- [x] Quality và sufficiency không bị trộn.
- [x] Deep target evidence/Sprint 4 không bị lấn phạm vi.
- [x] Người dùng review và approve `M04-D01`–`M04-D18` bằng `APPROVE_G0-12M-04`.

## 18. Trạng thái gate

```text
Deliverable tồn tại: YES
Source/schema discovery: COMPLETED
Static content review: PASS
User approval: APPROVE_G0-12M-04
Source/runtime mutation: NONE

G0-12M-04 STATUS: COMPLETED
NEXT REQUIRED ACTION: IMPLEMENT_G0_12M_05
```
