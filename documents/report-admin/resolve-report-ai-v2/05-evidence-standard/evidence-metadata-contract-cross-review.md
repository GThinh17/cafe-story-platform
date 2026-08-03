# G0-12M-08 — Evidence Metadata Contract Cross-review

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12M-08` |
| Trạng thái | `APPROVED` |
| Approval đã nhận | `APPROVE_G0-12M-08` |
| Contract được review | `2.0.0-rc.1` |
| Phạm vi | `G0-12M-00`–`G0-12M-07` |
| Cross-review checks | `16/16 PASS` |
| Fixture regression | `18/18 PASS` |
| Runtime authority | `false` |
| Source/runtime mutation | `NONE` |

## 2. Kết luận trước

```text
DESIGN CONTRACT: READY_FOR_APPROVAL
RUNTIME IMPLEMENTATION: NOT_IMPLEMENTED
RUNTIME AUTHORITY: FALSE
PRODUCTION DEPLOYMENT: NOT_AUTHORIZED
```

M01–M07 đủ coherent sau bốn correction của M08. Kết luận này không có nghĩa feature runtime đã dùng
typed evidence contract, n8n đã publish hoặc Admin UI đã chạy E2E.

## 3. Bound

### 3.1. Trong phạm vi

- review consistency M00–M07;
- đối chiếu terminology, version, Rule ID, Evidence Kind và semantic requirements;
- đối chiếu target snapshots BLOG/COMMENT/USER/CAFE_PAGE;
- review privacy/PII/secret boundary;
- review JSON Schema và semantic validator;
- review missing/conflict/decision ceiling;
- review fixtures positive/negative;
- review authority split Backend → n8n/provider → Backend → FE;
- sửa hẹp metadata contract artifacts khi cross-review chứng minh inconsistency;
- tạo implementation handoff cho `G0-12A`.

### 3.2. Ngoài phạm vi

- port schema/validator vào Java;
- sửa prompt/n8n;
- sửa Admin UI;
- gọi provider;
- apply migration;
- HMAC/timestamp/nonce;
- runtime/E2E;
- activate Rule Catalog/policy/contract;
- production deploy.

## 4. Review chain M00–M07

| Gate | Deliverable | Cross-review assertion | Kết quả |
|---|---|---|---|
| M00 | Semantic boundary | Claim/metadata/evidence/observation/finding/recommendation tách riêng | `PASS` |
| M01 | Common Envelope | Canonical envelope `1.0.0-rc.1`, provenance/availability/privacy/payload rõ | `PASS_AFTER_FIX` |
| M02 | BLOG schema | Content-first, bounded media reference, snapshot/hash/association rõ | `PASS_AFTER_FIX` |
| M03 | COMMENT schema | Target attribution đúng, direct parent + BLOG context bounded | `PASS_AFTER_FIX` |
| M04 | USER/PAGE manual-only | Provider clamp, PII exclusion, deep context missing-only | `PASS_AFTER_FIX` |
| M05 | Evidence Kind Catalog | Closed 14 kinds, target/source/lifecycle compatibility | `PASS` |
| M06 | Rule-to-Evidence Matrix | 24 rules, 15 semantic requirements, decision ceilings | `PASS` |
| M07 | Executable contract | Schema + semantic validator + manifest + fixtures | `PASS_AFTER_FIX` |

## 5. Findings và disposition

### 5.1. `M08-CONTRACT-01` — Legacy example drift

Severity: `HIGH`

Phát hiện:

- M01/M02 dùng `CONTENT_SNAPSHOT`;
- M01 dùng `MEDIA_OBSERVATION`;
- M03 dùng `PARENT_CONTEXT_SNAPSHOT`;
- examples dùng envelope `1.0.0`;
- M04 dùng field shape trước canonical M07.

Sửa:

- chuyển sang `TARGET_TEXT_CONTENT`, `VERIFIED_MEDIA_OBSERVATION`,
  `PARENT_BLOG_CONTEXT`;
- pin `envelopeVersion=1.0.0-rc.1`;
- canonicalize M04 envelope example;
- giữ provenance alias, không dùng raw provider-facing identity.

Kết quả: `FIXED`.

### 5.2. `M08-CONTRACT-02` — Privacy/schema gap

Severity: `CRITICAL`

Phát hiện:

- USER/PAGE context objects còn mở;
- generic payload có thể lồng field email/phone/token/session/secret;
- `riskScore` có thể bị giấu trong nested payload.

Sửa:

- close allowlisted USER/PAGE context properties;
- thêm recursive forbidden-field semantic guard;
- thêm `M08-FX-17`;
- fixture chứa `userEmail` nested bị chặn bằng `FORBIDDEN_FIELD_PRESENT`.

Kết quả: `FIXED`.

Lưu ý: runtime vẫn cần secure DTO, redaction và logging review. Validator design không tự bảo vệ một
runtime chưa triển khai.

### 5.3. `M08-CONTRACT-03` — Lifecycle enum incomplete

Severity: `MEDIUM`

Phát hiện: schema thiếu `DRAFT` và `RETIRED` dù policy lifecycle đã định nghĩa.

Sửa:

- lifecycle enum đầy đủ sáu states;
- chỉ `ACTIVE` có runtime authority;
- thêm `M08-FX-18` chứng minh `RETIRED` là known-but-inactive và phải policy-stop.

Kết quả: `FIXED`.

### 5.4. `M08-CONTRACT-04` — Confidence optionality mismatch

Severity: `MEDIUM`

Phát hiện: design nói optional nhưng schema required nullable.

Sửa:

- chốt `assessmentConfidence` bắt buộc nhưng nullable;
- khi có số chỉ nằm trong `[0,1]`;
- không phải violation probability;
- không override missing/conflict/manual target/decision ceiling.

Kết quả: `FIXED`.

## 6. Catalog traceability

| Catalog | Source | Schema | Kết quả |
|---|---:|---:|---|
| Rule IDs | `24` | `24` exact set | `PASS` |
| Evidence Kinds | `14` | `14` exact set | `PASS` |
| Semantic requirements | `15` | `15` exact set | `PASS` |
| Targets | `4` | `4` | `PASS` |
| Policy lifecycle states | `6` | `6` exact set | `PASS` |

Unknown Rule ID, Evidence Kind hoặc semantic requirement fail closed tại schema boundary.

## 7. Target review

### BLOG

- exact target snapshot/version/hash binding;
- content evidence candidate chỉ là target text;
- page/actor/region mặc định context-only;
- URL/media reference không phải media finding;
- verified media vẫn reserved missing-only.

Kết quả: `PASS_WITH_RUNTIME_GAPS`.

### COMMENT

- subject luôn là reported COMMENT;
- parent BLOG/direct parent chỉ context;
- thiếu material context → manual;
- không attribution parent violation sang target author;
- không suy full-thread/pattern.

Kết quả: `PASS_WITH_RUNTIME_GAPS`.

### USER

- local manual-only;
- provider không được gọi;
- state giữ boolean source semantics;
- profile allowlist đóng;
- deep context missing-only;
- confidence cao không đổi authority.

Kết quả: `PASS_WITH_RUNTIME_GAPS`.

### CAFE_PAGE

- local manual-only;
- owner association không phải legal ownership proof;
- declared address/region không phải jurisdiction proof;
- owner/member PII bị cấm;
- deep context missing-only.

Kết quả: `PASS_WITH_RUNTIME_GAPS`.

## 8. Evidence and decision review

### Evidence item

- Evidence ID unique;
- subject bind đúng snapshot;
- source × kind × target compatible;
- availability/payload/digest/reason compatible;
- context-only không được dùng làm proof;
- reserved kind không được AVAILABLE/PARTIAL;
- nested forbidden fields bị chặn.

### Per-rule evaluation

- candidate rules và evaluation set phải exact;
- supporting/counter/missing refs phải tồn tại;
- critical missing không được biến thành `NOT_SUBSTANTIATED`;
- conflict chưa giải quyết không được tạo mutation candidate.

### Aggregation

| Decision | Executable guard |
|---|---|
| `RESOLVE` | BLOG/COMMENT, có substantiated rule/finding, không manual blocker |
| `REJECT` | Complete scope, EVD.004, no substantiated/missing/conflict, `KEEP_VISIBLE` |
| `NEEDS_MANUAL_REVIEW` | Critical missing/conflict/unassessable/USER/PAGE, `NO_ACTION` |
| `null` | Policy/schema/operational stop, không phải content decision |

Automation vẫn `A0_RECOMMEND_ONLY`.

## 9. Privacy review

Forbidden nested field names gồm:

```text
authorization, cookie, password, email, phone, roles,
accessToken, refreshToken, token, session, apiKey, secret,
OPENAI_API_KEY, riskScore, violationProbability
```

Đây là fail-closed guard bổ sung. Runtime implementation còn phải:

- dùng request-scoped aliases;
- không log raw payload/credential;
- redact before persistence/provider;
- validate provider projection riêng;
- có secure legal intake cho IP/privacy;
- có HMAC/replay protection tại G0-12A.

## 10. Fixture review

| Nhóm | Số |
|---|---:|
| M06 minimum scenarios | `15` |
| M07 approval baseline | `16` |
| M08 hardening additions | `2` |
| Current total | `18` |
| Pass | `18` |
| Fail | `0` |

Negative fixture pass nghĩa là invalid instance bị chặn đúng expected error, không phải instance được
chấp nhận.

## 11. Cross-layer readiness

| Layer | Design readiness | Runtime readiness | Gap |
|---|---|---|---|
| Backend | `READY_FOR_IMPLEMENTATION` | `NOT_IMPLEMENTED` | Typed DTO/schema/semantic validator/version registry |
| n8n | `READY_FOR_REBASE` | `NOT_IMPLEMENTED` | Provider projection/response contract/publish |
| Provider/prompt | `READY_FOR_REBASE` | `NOT_IMPLEMENTED` | Evidence references, no evidence creation, prompt injection boundary |
| Admin FE | `READY_FOR_REBASE` | `NOT_IMPLEMENTED` | Typed rendering, missing/conflict/policy-invalid states |
| Security | `DESIGNED_PARTIAL` | `NOT_IMPLEMENTED` | HMAC/timestamp/nonce at G0-12A |
| Operations | `DESIGNED_PARTIAL` | `NOT_VERIFIED` | migration/runtime/monitoring/rollback |

## 12. Approval effect

Approval `APPROVE_G0-12M-08` đã được nhận, vì vậy:

1. M08 chuyển `COMPLETED`.
2. G0-12M checklist hoàn thành.
3. Manifest lifecycle chuyển `PROPOSED → APPROVED`.
4. `runtimeAuthority` vẫn `false`.
5. Không activate Rule Catalog/policy.
6. Không publish n8n.
7. Không production deploy.
8. Bước kế tiếp theo roadmap là `IMPLEMENT_G0_12A`.

## 13. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M08-D01` | M01–M07 coherent sau bốn correction M08 | `APPROVED` |
| `M08-D02` | Khi approve M08, manifest chuyển APPROVED nhưng runtimeAuthority=false | `APPROVED` |
| `M08-D03` | Executable M07 schema/manifest/validator là canonical implementation reference | `APPROVED` |
| `M08-D04` | Legacy examples phải dùng canonical M05/M07 kinds/version/shape | `APPROVED` |
| `M08-D05` | USER/PAGE context objects phải closed allowlist | `APPROVED` |
| `M08-D06` | Recursive forbidden-field guard là mandatory defense-in-depth | `APPROVED` |
| `M08-D07` | Lifecycle gồm đủ 6 states; chỉ ACTIVE có authority | `APPROVED` |
| `M08-D08` | `assessmentConfidence` required nullable và không cấp authority | `APPROVED` |
| `M08-D09` | Provenance giữ entity type/alias/field path có cấu trúc | `APPROVED` |
| `M08-D10` | Schema + semantic validator đều bắt buộc | `APPROVED` |
| `M08-D11` | Current regression baseline là 18 fixtures | `APPROVED` |
| `M08-D12` | Backend là final validation/mutation authority | `APPROVED` |
| `M08-D13` | n8n/provider không tạo evidence hoặc decision authority | `APPROVED` |
| `M08-D14` | FE phải tách evidence/missing/conflict/policy-invalid/confidence | `APPROVED` |
| `M08-D15` | Rule Catalog/policy chưa ACTIVE vẫn là runtime blocker | `APPROVED` |
| `M08-D16` | Approval M08 đóng design contract, không chứng minh runtime/E2E | `APPROVED` |
| `M08-D17` | G0-12A là bước kế tiếp theo roadmap | `APPROVED` |
| `M08-D18` | Production deploy tiếp tục không được phép | `APPROVED` |

## 14. Acceptance criteria

- [x] Review M00–M07.
- [x] Canonicalize legacy examples.
- [x] Rule ID `24/24`.
- [x] Evidence Kind `14/14`.
- [x] Semantic requirements `15/15`.
- [x] Lifecycle `6/6`.
- [x] Target `4/4`.
- [x] Close USER/PAGE context schema.
- [x] Add forbidden-field semantic guard.
- [x] Add privacy and retired-lifecycle negative/guard fixtures.
- [x] Fixture regression `18/18`.
- [x] Cross-review checks `16/16`.
- [x] Runtime authority vẫn false.
- [x] Không sửa source/runtime.
- [x] Người dùng review và approve `M08-D01`–`M08-D18` bằng `APPROVE_G0-12M-08`.

## 15. Trạng thái gate

```text
M00-M07 cross-review: PASS
Issues found: 4
Issues fixed: 4
Open issues in M08 scope: 0
Cross-review checks: 16/16 PASS
Fixture regression: 18/18 PASS
User approval: APPROVE_G0-12M-08
Contract lifecycle: APPROVED
Runtime authority: FALSE
Source/runtime mutation: NONE

G0-12M-08 STATUS: COMPLETED
NEXT REQUIRED ACTION: IMPLEMENT_G0_12A
```
