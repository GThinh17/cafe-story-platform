# G0-12M-07 — Executable Evidence Metadata Contract

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12M-07` |
| Trạng thái | `APPROVED` |
| Approval đã nhận | `APPROVE_G0-12M-07` |
| Contract version | `2.0.0-rc.1` |
| JSON Schema version | `1.0.0-rc.1` |
| Common Evidence Envelope version | `1.0.0-rc.1` |
| Evidence Kind Catalog version | `1.0.0-rc.1` |
| Rule Catalog input | `RC-2.0.0-proposed.1` — chưa `ACTIVE` |
| Runtime authority | `false` |
| Source/runtime mutation | `NONE` |

M07 chuyển logical contract M01–M06 thành artifact có thể chạy. Việc một fixture mô phỏng
`policyStatus=ACTIVE` chỉ dùng để test nhánh tương lai; nó không kích hoạt policy thật.

## 2. Bound

### 2.1. Trong phạm vi

- JSON Schema Draft 2020-12 cho evidence evaluation packet;
- discriminated target snapshot cho BLOG, COMMENT, USER và CAFE_PAGE;
- Common Evidence Envelope với 14 Evidence Kind đã duyệt;
- enum 24 Rule ID và 15 semantic requirement code;
- missing evidence, conflict, rule outcome, scope evaluation và recommendation;
- semantic validator cho invariant không thể diễn tả an toàn chỉ bằng JSON Schema;
- version manifest;
- fixture bases và 16 scenarios;
- validation evidence có thể chạy lại.

### 2.2. Ngoài phạm vi

- sửa DTO/entity/service/controller/backend validator;
- sửa Admin FE;
- import/publish n8n workflow;
- gọi model/provider;
- activate Rule Catalog hoặc policy;
- HMAC, replay protection và secret rotation thuộc `G0-12A`;
- production migration hoặc deploy;
- M08 cross-review/approval.

## 3. Artifact layout

```text
05-evidence-standard/
├─ evidence-metadata-contract-v2.md
└─ contracts/
   └─ v2.0.0-rc.1/
      ├─ evidence-metadata-contract.schema.json
      ├─ contract-manifest.json
      ├─ validate-fixtures.mjs
      ├─ README.md
      └─ fixtures/
         ├─ bases/
         │  ├─ blog-resolve.json
         │  ├─ comment-resolve.json
         │  ├─ user-manual.json
         │  └─ cafe-page-manual.json
         └─ cases/
            └─ 18 scenario overlays
```

Case fixture dùng base + deterministic JSON Pointer operations. Validator materialize instance trước
khi chạy schema và semantic checks. Cách này tránh copy hàng nghìn dòng Common Envelope nhưng vẫn
tạo được exact JSON instance có thể kiểm tra lại.

## 4. Version model

### 4.1. Các version độc lập

| Version | Ý nghĩa | Bump khi |
|---|---|---|
| `contractVersion` | Whole evaluation packet/wire contract | Thay root contract hoặc semantics tương thích |
| `schemaVersion` | Exact executable JSON Schema | Thay structural validation |
| `envelopeVersion` | Common Evidence Envelope item | Thay envelope field/invariant |
| `evidenceKindCatalogVersion` | Closed kind catalog | Thêm/xóa/đổi lifecycle kind |
| `ruleCatalogVersion` | Rule criteria/action metadata | Thay rule catalog |

Không suy một version từ version khác. Request/result/audit phải pin đủ version.

### 4.2. Version hiện tại

`2.0.0-rc.1` là release candidate của contract V2, không phải production-active version. Tại M07
approval, lifecycle vẫn `PROPOSED`; sau `APPROVE_G0-12M-08`, lifecycle đã chuyển `APPROVED`.

```text
lifecycle = APPROVED
runtimeAuthority = false
```

M08 approval duyệt design contract nhưng không activation. Activation vẫn cần active policy/catalog
và runtime implementation/verification.

### 4.3. Compatibility

- Unknown root property: reject.
- Unknown Rule ID/Evidence Kind/semantic requirement: reject.
- Version không đúng exact supported version: reject.
- Không tự fallback về prompt text hoặc model memory.
- Skeleton hiện tại không tự được xem là V2-compatible.
- Adapter/migration runtime chỉ được làm tại implementation gate sau.

## 5. Contract root

| Field | Bắt buộc | Authority |
|---|---:|---|
| `contractVersion` | Có | Backend |
| `schemaVersion` | Có | Backend |
| `correlationId` | Có | Backend |
| `policyContext` | Có | Backend/policy registry |
| `targetSnapshot` | Có | Backend |
| `providerExecution` | Có | Backend orchestration guard |
| `evidence[]` | Có | Approved collectors |
| `ruleEvaluations[]` | Có | Candidate evaluation, Backend revalidate |
| `missingEvidence[]` | Có | Backend requirement evaluator |
| `conflicts[]` | Có | Backend/approved review logic |
| `scopeEvaluation` | Có | Backend |
| `recommendation` | Có | Provider candidate + Backend validation |

Không có `riskScore`. `assessmentConfidence` là field bắt buộc nhưng nullable; khi có số thì chỉ là
assessment metadata từ 0 đến 1 và không cấp authority.

## 6. Target snapshot discriminator

### 6.1. BLOG

- `snapshotVersion` phải theo `blog-x.y.z`;
- bắt buộc state, sanitized/bounded content và media references;
- BLOG được provider-eligible khi các policy/version gate hợp lệ;
- exact content evidence dùng `TARGET_TEXT_CONTENT`.

### 6.2. COMMENT

- `snapshotVersion` phải theo `comment-x.y.z`;
- bắt buộc target content, BLOG context slot và direct-parent context slot;
- context scope cố định `DIRECT_PARENT_AND_BLOG_ONLY`;
- context item vẫn `CONTEXT_ONLY`;
- missing material parent context dẫn tới manual review.

### 6.3. USER

- `snapshotVersion` phải theo `user-manual-x.y.z`;
- freshness chỉ `LIMITED`;
- giữ boolean `accountStatus`, không phát minh lifecycle enum;
- provider eligibility/projection đều `false`;
- `TARGET_DEEP_CONTEXT` luôn missing-only.

### 6.4. CAFE_PAGE

- `snapshotVersion` phải theo `cafe-page-manual-x.y.z`;
- giữ state vector, ownership context, public context và local media references;
- owner/member PII bị cấm;
- provider eligibility/projection đều `false`;
- `TARGET_DEEP_CONTEXT` luôn missing-only.

## 7. JSON Schema responsibilities

Schema enforce:

1. exact contract/schema/catalog version;
2. closed 24 Rule IDs;
3. closed 14 Evidence Kinds;
4. closed 15 semantic requirement codes;
5. four target-specific snapshot shapes;
6. Common Envelope required fields;
7. payload representation shape;
8. availability/quality/privacy enum;
9. missing evidence record shape;
10. rule outcome/conflict/recommendation shape;
11. no unknown root/envelope property;
12. `assessmentConfidence` required-but-nullable, khi có số chỉ từ 0 đến 1;
13. known lifecycle gồm `DRAFT`, `PROPOSED`, `APPROVED`, `ACTIVE`, `DEPRECATED`, `RETIRED`.

Schema không được quảng cáo là đủ cho cross-record, set/cardinality và decision aggregation logic.

## 8. Semantic validator responsibilities

`validate-fixtures.mjs` enforce:

- unique Evidence ID/Missing ID/Rule Evaluation;
- exact Rule Evaluation coverage với candidate rules;
- report-target association và evidence subject binding;
- target structural Evidence Kind cardinality;
- Evidence Kind × target compatibility;
- Evidence Kind × source compatibility;
- intended-use ceiling;
- reserved kind không được `AVAILABLE/PARTIAL`;
- availability/payload/digest/reason compatibility;
- `collectedForRuleIds` phải là subset candidate rules;
- supporting/counter/finding references phải tồn tại;
- context-only evidence không được dùng làm proof;
- missing semantic/kind record phải đúng shape;
- inactive policy/catalog không tạo content decision;
- USER/PAGE manual clamp;
- critical missing/conflict/material unassessable bắt buộc manual/no-action;
- RESOLVE cần substantiated rule và finding;
- REJECT cần complete scope, `CSR.EVD.004`, cleared material rules, không missing/conflict,
  `KEEP_VISIBLE`;
- confidence không override các guard trên.
- nested forbidden PII/secret/authority-score field bị fail closed.

## 9. Decision precedence executable

```text
schema invalid
→ fail closed

policy/rule catalog inactive
→ POLICY_INVALID
→ provider not called
→ reportDecision = null

USER/CAFE_PAGE
→ LOCAL_MANUAL_ONLY
→ NEEDS_MANUAL_REVIEW + NO_ACTION

critical missing | material conflict | material UNASSESSABLE
→ NEEDS_MANUAL_REVIEW + NO_ACTION

substantiated violation + no manual blocker
→ RESOLVE candidate

complete non-substantiation + EVD.004 + no blocker
→ REJECT + KEEP_VISIBLE candidate
```

Mọi kết quả vẫn ở automation mode `A0_RECOMMEND_ONLY`.

## 10. Fixture coverage

| ID | Scenario | Expected |
|---|---|---|
| `M07-FX-01` | Text rule đủ evidence | Valid RESOLVE candidate |
| `M07-FX-02` | COMMENT thiếu material parent context | Valid manual |
| `M07-FX-03` | Chỉ có image reference | Valid manual |
| `M07-FX-04` | Reserved verified media bị gửi AVAILABLE | Semantic reject |
| `M07-FX-05` | USER manual clamp | Valid manual |
| `M07-FX-06` | Authoritative fact missing | Valid manual |
| `M07-FX-07` | IP claimant authority missing | Valid legal/manual |
| `M07-FX-08` | Material conflict | Valid manual |
| `M07-FX-09` | Complete non-substantiation | Valid REJECT candidate |
| `M07-FX-10` | Incomplete scope nhưng REJECT | Semantic reject |
| `M07-FX-11` | Inactive Rule Catalog | Valid policy stop |
| `M07-FX-12` | Unknown rule/kind/requirement | Schema reject |
| `M07-FX-13` | Confidence cao + critical missing | Valid manual |
| `M07-FX-14` | Một rule substantiated, một rule unassessable | Valid manual |
| `M07-FX-15` | Confidence 0.999 trên USER | Valid manual |
| `M07-FX-16` | CAFE_PAGE manual clamp | Valid manual |
| `M08-FX-17` | Forbidden nested PII/secret/score key | Semantic reject |
| `M08-FX-18` | RETIRED policy/catalog | Valid policy stop |

M06 yêu cầu tối thiểu 15 scenarios. M07 có 16 tại thời điểm được approve; M08 cross-review bổ sung
hai hardening scenarios, đưa regression suite hiện tại lên 18.

## 11. Validator execution

```powershell
node "documents/report-admin/resolve-report-ai-v2/05-evidence-standard/contracts/v2.0.0-rc.1/validate-fixtures.mjs"
```

Expected:

```text
actualScenarioCount = 18
passed = 18
failed = 0
allPassed = true
```

Validator dùng Ajv `8.20.0` đã có trong workspace, Draft 2020-12 và strict mode. Không thêm package.

## 12. Runtime implementation boundary

### Backend

- là owner của version pinning, target snapshot, evidence collection và final semantic validation;
- không deserialize thẳng thành current generic maps rồi bỏ unknown field;
- phải reject unknown version/kind/rule;
- phải tái kiểm tra association/freshness trước mutation;
- phải giữ A0 human authority.

### n8n/provider

- chỉ nhận provider projection do Backend tạo;
- không tạo Evidence ID, không tự sửa missing evidence;
- không đổi Evidence Kind lifecycle;
- không cấp `RESOLVE/REJECT` authority;
- response phải bị Backend schema + semantic validator kiểm tra lại.

### Admin FE

- render available/missing/conflict/per-rule outcome riêng;
- không trình bày context-only item như proof;
- không trình bày confidence như violation probability;
- hiển thị policy-invalid khác content manual review;
- USER/PAGE phải hiển thị local manual lane.

M07 không thực hiện các thay đổi runtime trên.

## 13. Known gaps

1. Rule Catalog thật vẫn `PROPOSED`, chưa `ACTIVE`.
2. JSON Schema/validator chưa được port vào Backend.
3. n8n chưa consume contract này.
4. Admin FE chưa render typed evidence contract.
5. HMAC/replay boundary chưa thuộc M07.
6. Ajv được reuse từ web workspace; CI/runtime owner phải chọn dependency chính thức khi code.
7. Fixtures là deterministic design fixtures, không phải production E2E evidence.

## 14. M08 handoff

M08 phải review:

- consistency M01–M07;
- schema fields so với detailed target docs;
- semantic validator coverage so với M06;
- privacy/PII boundary;
- version/activation semantics;
- fixture negative-path completeness;
- implementation readiness BE → n8n → BE → FE;
- quyết định approve/revise metadata contract trước source implementation tiếp theo.

## 15. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M07-D01` | Dùng JSON Schema Draft 2020-12 strict | `APPROVED` |
| `M07-D02` | Contract version là `2.0.0-rc.1` | `APPROVED` |
| `M07-D03` | Schema/envelope/catalog/rule versions độc lập và phải pin | `APPROVED` |
| `M07-D04` | Contract lifecycle vẫn PROPOSED, runtimeAuthority=false | `APPROVED` |
| `M07-D05` | Unknown property/rule/kind/requirement fail closed | `APPROVED` |
| `M07-D06` | JSON Schema và semantic validator là hai lớp bắt buộc | `APPROVED` |
| `M07-D07` | Four target snapshots dùng discriminator và exact version pattern | `APPROVED` |
| `M07-D08` | Structural bundle cardinality do semantic validator enforce | `APPROVED` |
| `M07-D09` | Reserved Evidence Kind không được AVAILABLE/PARTIAL | `APPROVED` |
| `M07-D10` | Inactive policy/catalog không tạo content decision | `APPROVED` |
| `M07-D11` | USER/PAGE clamp trước provider | `APPROVED` |
| `M07-D12` | Context-only evidence không được làm supporting proof/finding | `APPROVED` |
| `M07-D13` | Critical missing/conflict/unassessable luôn manual/no-action | `APPROVED` |
| `M07-D14` | REJECT cần complete scope và `CSR.EVD.004` | `APPROVED` |
| `M07-D15` | Confidence không đổi authority | `APPROVED` |
| `M07-D16` | Fixture overlays là format test canonical của M07 | `APPROVED` |
| `M07-D17` | Tối thiểu 15 scenarios; M07 approval baseline có 16, M08 được phép bổ sung hardening fixture | `APPROVED` |
| `M07-D18` | M07 approval không activate/deploy contract | `APPROVED` |

## 16. Acceptance criteria

- [x] JSON Schema parse và strict compile.
- [x] Contract/version manifest tồn tại.
- [x] Cover 24 Rule IDs.
- [x] Cover 14 Evidence Kinds.
- [x] Cover 15 semantic requirement codes.
- [x] Cover BLOG/COMMENT/USER/CAFE_PAGE.
- [x] Có schema + semantic validation split.
- [x] Có tối thiểu 15 fixture scenarios.
- [x] Negative fixtures fail với expected error.
- [x] Fixture suite hiện tại chạy `18/18 PASS` sau M08 hardening.
- [x] Có evidence report/issue/fix/summary/improvement.
- [x] Không sửa source/runtime.
- [x] Người dùng review và approve `M07-D01`–`M07-D18` bằng `APPROVE_G0-12M-07`.

## 17. Trạng thái gate

```text
Deliverable tồn tại: YES
Schema strict compile: PASS
Fixtures: 18/18 PASS
Negative-path assertions: PASS
Static content review: PASS
User approval: APPROVE_G0-12M-07
Source/runtime mutation: NONE

G0-12M-07 STATUS: COMPLETED
NEXT REQUIRED ACTION: IMPLEMENT_G0_12M_08
```
