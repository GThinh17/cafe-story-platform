# G0-12M-06 — Rule-to-Evidence Requirement Matrix

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12M-06` |
| Trạng thái | `APPROVED` |
| Approval đã nhận | `APPROVE_G0-12M-06` |
| Matrix version | `1.0.0-rc.1` |
| Rule Catalog input | `RC-2.0.0-proposed.1` — `PROPOSED`, chưa `ACTIVE` |
| Evidence Kind Catalog | `1.0.0-rc.1` — M05 approved |
| Target depth | BLOG/COMMENT content-first; USER/CAFE_PAGE manual-only |
| Automation | `A0_RECOMMEND_ONLY` |
| Source/runtime mutation | `NONE` |

Ma trận này là thiết kế burden/evidence. Nó không tự kích hoạt Rule Catalog hoặc cấp production
authority.

## 2. Bound

### 2.1. Trong phạm vi

- requirement level theo Rule ID và target depth;
- Evidence Kind bắt buộc, conditional, context và missing-only;
- observation/policy-context requirement nằm ngoài Evidence Kind;
- counter-evidence/exception burden;
- missing/conflict behavior;
- per-rule current evaluation ceiling;
- aggregation rule cho `RESOLVE`, `REJECT`, `NEEDS_MANUAL_REVIEW`;
- evidence gap cần collector/catalog extension tương lai.

### 2.2. Ngoài phạm vi

- thay đổi criteria/title/target list của 24 Rule ID;
- tự activate Rule Catalog, protected list, restricted registry hoặc policy version;
- exact phrase/keyword/score threshold;
- verified media, web search, legal, identity hoặc pattern collector implementation;
- USER/CAFE_PAGE deep policy và suspension burden;
- JSON Schema executable/fixtures;
- action execution/quorum implementation;
- sửa BE, FE, n8n, database hoặc prompt.

## 3. Điều kiện tiên quyết trước mọi rule evaluation

### 3.1. Version gate

Trước runtime:

```text
policyVersion.status = ACTIVE
ruleCatalogVersion.status = ACTIVE
evidenceKindCatalogVersion accepted
ruleVersion.status = ACTIVE
targetSnapshot/schema versions accepted
```

Nếu một version chỉ `PROPOSED`, missing hoặc không khớp:

```text
POLICY_VERSION_NOT_ACTIVE_OR_INVALID
→ policy/operational validation failure
→ không tạo RESOLVE/REJECT
→ không fallback sang prompt text/model memory
```

Hiện `RC-2.0.0-proposed.1` chưa `ACTIVE`; M06 không thay đổi trạng thái đó.

### 3.2. Structural base

Mọi target cần:

| Code | Evidence Kind | Level | Failure behavior |
|---|---|---|---|
| `BASE-01` | `TARGET_IDENTITY` | Structural required | Invalid/unavailable target |
| `BASE-02` | `REPORT_TARGET_ASSOCIATION` | Structural required | Stop evaluation; association invalid |
| `BASE-03` | `TARGET_STATE` | Structural required | Manual hoặc operational failure tùy availability |
| `BASE-04` | Snapshot hash/version/freshness | Structural required | Stale/manual; không reuse |
| `BASE-05` | Rule/catalog/schema version context | Structural required | Policy validation failure |

Structural base không substantiation violation.

M05 structural bundle theo từng target vẫn phải được build đầy đủ. Bảng trên chỉ tách các prerequisite
chung để rule evaluation bắt đầu; nó không loại các context slot bắt buộc của target schema.

## 4. Requirement notation

| Code | Tên | Ý nghĩa |
|---|---|---|
| `S` | Structural | Phải có để bind/validate evaluation |
| `R` | Required | Cần để đánh giá rule trong evaluated branch |
| `C` | Conditionally critical | Chỉ critical khi trigger/modality/context tương ứng tồn tại |
| `O` | Optional/context | Có thể giúp interpretation/counter-evidence |
| `X` | Critical capability gap | Hiện không có kind/collector khả dụng; branch phải manual |
| `N` | Not applicable | Không dùng |

Có Evidence Kind `AVAILABLE` chưa đủ. Rule còn cần observation/criteria được trích xuất có
provenance và counter-evidence assessment.

## 5. Requirement không phải Evidence Kind

M06 cần các requirement semantic sau. Chúng không được giả làm Evidence Kind.

| Requirement code | Loại | Ý nghĩa |
|---|---|---|
| `SEM-THREAT-CREDIBILITY` | Observation/context | Threat target, intent, specificity và credibility |
| `SEM-TARGETED-SUBJECT` | Observation/context | Người/nhóm bị nhắm đến phải xác định được |
| `SEM-PROTECTED-CHARACTERISTIC` | Policy + observation | Explicit because-of relation với protected code |
| `SEM-AGE-CONSENT` | External/human context | Age/consent khi material |
| `SEM-PATTERN-HISTORY` | Deferred evidence | Repetition, unwanted contact, coordinated behavior |
| `SEM-IDENTITY-COMPARATOR` | Deferred evidence | Authentic identity/reference để đánh giá impersonation |
| `SEM-AUTHORITATIVE-FACT` | Deferred evidence | Counter-source có authority/freshness cho factual claim |
| `SEM-TRANSACTION-INTENT` | Observation/context | Offer/request/payment/contact/facilitation intent |
| `SEM-CLAIMANT-AUTHORITY` | Legal/human evidence | Người khiếu nại có authority đối với IP/privacy claim |
| `SEM-LICENSE-PERMISSION` | Legal/human evidence | License, permission hoặc authorized use |
| `SEM-PRIVACY-AUTHORIZATION` | Legal/human evidence | Consent/public availability/authorized disclosure |
| `SEM-JURISDICTION` | Authority/human evidence | Jurisdiction áp dụng và source/version |
| `SEM-RESTRICTED-REGISTRY` | Policy context | Active restricted-goods registry/category |
| `SEM-EXCEPTION-CONTEXT` | Counter/context | News, education, satire, quotation, medical, artistic... |
| `SEM-COMPLETE-EVALUATION-SCOPE` | Control | Mọi material candidate rule đã được đánh giá đầy đủ |

Nếu semantic requirement critical chưa có approved evidence kind/collector:

```text
missingEvidenceRequirement
→ critical=true
→ evidenceSufficiency=UNASSESSABLE hoặc INSUFFICIENT
→ NEEDS_MANUAL_REVIEW + NO_ACTION
```

## 6. Requirement profiles

| Profile | Nội dung | Default ceiling |
|---|---|---|
| `RP-TEXT-DIRECT` | Direct BLOG/COMMENT text + required observation | Conditional content recommendation |
| `RP-TEXT-CONTEXT` | Text + bounded context/counter-exception review | Conditional; missing material context → manual |
| `RP-MEDIA-CONDITIONAL` | Media reference + verified observation khi media material | Media branch manual ở capability hiện tại |
| `RP-PATTERN-DEFERRED` | Repetition/contact/coordination history | Manual-only |
| `RP-AUTHENTICITY-DEFERRED` | Identity comparator/authority | Manual-only |
| `RP-PLATFORM-FACT` | Material objectively verifiable claim + authoritative counter-record | Narrow conditional; external branch manual |
| `RP-LEGAL-PRIVACY` | Claimant/consent/license/jurisdiction | Human/legal manual-only |
| `RP-COMMERCE` | Transaction intent + active registry + jurisdiction/license branch | Manual ở current activation state |
| `RP-NON-VIOLATION-SIGNAL` | Quality/preference context | Không tạo violation finding |
| `RP-ROUTING` | Chọn rule/manual lane | Không substantiation |
| `RP-EVIDENCE-CONTROL` | Missing/conflict/unreadable/non-substantiation | Control decision only |

## 7. Target override

### 7.1. BLOG/COMMENT

- Có thể dùng `RP-TEXT-*` content-first.
- `TARGET_TEXT_CONTENT` là direct evidence candidate.
- Parent/actor/page/region evidence mặc định context-only.
- Media-dependent branch bị block khi `VERIFIED_MEDIA_OBSERVATION` missing-only.
- HIDE/REMOVE chỉ là candidate theo action contract và vẫn A0/human.

### 7.2. USER/CAFE_PAGE

Áp dụng cho mọi violation/non-violation rule:

```text
TARGET_DEEP_CONTEXT = NOT_COLLECTED
providerCalled = false
finding = none
decision = NEEDS_MANUAL_REVIEW
action = NO_ACTION
blockedReason = TARGET_DEEP_POLICY_NOT_IN_SPRINT1
```

Target applicability trong Rule Catalog không override M04/K1 manual-only guard.

### 7.3. Context-only ceiling của các kind còn lại

| Evidence Kind | M06 ceiling |
|---|---|
| `TARGET_ACTOR_ASSOCIATION` | Actor binding/context; không chứng minh intent hoặc actor-level violation |
| `TARGET_PAGE_ASSOCIATION` | Page relation/context; không chuyển content finding thành page finding |
| `TARGET_REGION_ASSOCIATION` | Location association context; không jurisdiction proof |
| `PARENT_BLOG_CONTEXT` | Interpret COMMENT target; không attribution parent violation |
| `PARENT_COMMENT_CONTEXT` | Interpret direct reply; không full-thread/pattern proof |
| `TARGET_PUBLIC_PROFILE_CONTEXT` | USER/PAGE local Admin orientation only |
| `PAGE_OWNER_ASSOCIATION` | Platform owner FK only; không legal/merchant ownership |
| `TARGET_MEDIA_REFERENCE` | Chứng minh reference tồn tại; không chứng minh media content |
| `TARGET_DEEP_CONTEXT` | Missing-only trong Sprint 1 |

## 8. Rule-to-Evidence Requirement Matrix — 24/24 Rule IDs

Ký hiệu target:

- `B`: BLOG
- `C`: COMMENT
- `U`: USER
- `P`: CAFE_PAGE
- `U/P-M`: USER/PAGE luôn manual theo target override.

| Rule ID | Type | Target route | Profile | Required evidence/observation | Critical conditional/missing | Current ceiling |
|---|---|---|---|---|---|---|
| `CSR.SAF.001` | VIOLATION | B/C; U/P-M | `RP-TEXT-CONTEXT` + media conditional | `TARGET_TEXT_CONTENT` R; `SEM-TARGETED-SUBJECT` R; `SEM-THREAT-CREDIBILITY` R | Parent context C khi quotation/reply/credibility phụ thuộc context; verified media C nếu threat nằm trong media; unresolved fiction/reporting exception C | B/C conditional finding; any critical gap → manual |
| `CSR.SAF.002` | VIOLATION | B/C | `RP-TEXT-DIRECT` + `RP-MEDIA-CONDITIONAL` | Direct text R cho text branch; verified media R cho visual branch | Media reference without verified observation X; documentary/news/educational context C | Text branch conditional; visual branch manual hiện tại |
| `CSR.SAF.003` | VIOLATION | B/C; U-M | `RP-TEXT-CONTEXT` + media conditional | Direct encouragement/facilitation observation R từ text hoặc verified media | Meaning/quotation/help-seeking context C; media C khi material | Text branch conditional; ambiguous/media branch manual |
| `CSR.HATE.001` | VIOLATION | B/C; U/P-M | `RP-TEXT-CONTEXT` | Text R; `SEM-TARGETED-SUBJECT` R; `SEM-PROTECTED-CHARACTERISTIC` R; protected-list version required in policy context | Reporter-only identity, inferred identity, quotation/reclaimed/education context → manual if unresolved | Conditional B/C finding; no identity inference |
| `CSR.HAR.001` | VIOLATION | B/C; U-M | `RP-TEXT-CONTEXT` | Text R; explicit targeted person/group observation R | Parent context C khi target/meaning không explicit; quotation/satire/reporting counter-context C | Conditional only when targeting is observable |
| `CSR.HAR.002` | VIOLATION | B/C; U-M | Direct bullying branch + `RP-PATTERN-DEFERRED` | Text R; direct target observation R for one-item bullying branch | Unwanted/repeated-contact branch requires `SEM-PATTERN-HISTORY` X; subtype ambiguity C | Direct explicit bullying branch conditional; unwanted/pattern branch manual |
| `CSR.SEX.001` | VIOLATION | B/C | `RP-TEXT-CONTEXT` + `RP-MEDIA-CONDITIONAL` | Text R for explicit textual activity; verified media R for nudity/visual branch | Media X hiện tại; `SEM-AGE-CONSENT` C khi material; medical/education/art/news exception C | Text branch conditional; visual/age/consent ambiguity manual |
| `CSR.SEX.002` | VIOLATION | B/C | `RP-TEXT-CONTEXT` + `RP-MEDIA-CONDITIONAL` | Text R for solicitation/sensitive text; verified media R for visual branch | Media X; age/consent C; contextual exceptions C | Text branch conditional; sensitive visual branch manual |
| `CSR.INT.001` | VIOLATION | B/C; U/P-M | `RP-AUTHENTICITY-DEFERRED` | Text/profile representation plus actor/page association context | `SEM-IDENTITY-COMPARATOR` X; ownership/authenticity dispute X | Manual-only until identity authority/comparator exists |
| `CSR.INT.002` | VIOLATION | B/C; U/P-M | `RP-PLATFORM-FACT` | Exact material claim in text R; authoritative platform counter-record R for narrow platform-fact branch | `SEM-AUTHORITATIVE-FACT` X cho external claims; opinion/prediction/satire/dispute C | Narrow platform-fact branch conditional only; external/broad claims manual |
| `CSR.INT.003` | VIOLATION | B/C; U/P-M | `RP-TEXT-CONTEXT` | Text R; `SEM-TRANSACTION-INTENT` R; material deception/payment/contact observation R | Context/correction/legitimate offer C; off-platform transaction proof C nếu content không tự đủ | Explicit content-level solicitation branch conditional; otherwise manual |
| `CSR.SPAM.001` | VIOLATION | B/C; U/P-M | `RP-PATTERN-DEFERRED` | Text/profile context O; repetition/manipulation pattern required | `SEM-PATTERN-HISTORY` X; report count/previous AI are non-evidence | Manual-only for violation finding in Sprint 1 |
| `CSR.PRIV.001` | VIOLATION | B/C; U/P-M | `RP-LEGAL-PRIVACY` | Text or verified media identifies data disclosure candidate; data category observation required | `SEM-PRIVACY-AUTHORIZATION` X; claimant relationship/consent/public-interest C; sensitive media X | Human Privacy lane; urgent containment routing only |
| `CSR.IP.001` | VIOLATION | B/C; P-M | `RP-LEGAL-PRIVACY` | Target content/reference O for triage | `SEM-CLAIMANT-AUTHORITY` X; `SEM-LICENSE-PERMISSION` X; ownership/exception/jurisdiction X | Human/legal IP lane only |
| `CSR.IP.002` | VIOLATION | B/C; U/P-M | `RP-LEGAL-PRIVACY` | Target content/reference O for triage | Right type, claimant authority, permission, exception and jurisdiction X | Human/legal IP lane only |
| `CSR.COM.001` | VIOLATION | B/C; U/P-M | `RP-COMMERCE` | Text/verified media R; `SEM-TRANSACTION-INTENT` R; active category registry required | Registry currently proposed; media X if visual; regulated category needs jurisdiction/license/age X | Manual at current activation; future prohibited-baseline branch may be conditional |
| `CSR.REL.001` | NON_VIOLATION_SIGNAL | B/C; P-M | `RP-NON-VIOLATION-SIGNAL` | Text R; COMMENT parent BLOG context R để xác định topic relation | Missing parent context C; target PAGE remains manual | Signal only; không punitive finding |
| `CSR.ROUTE.001` | NON_VIOLATION_SIGNAL | All | `RP-ROUTING` | Versioned intake mapping + structural base; reason remains report claim | Material description suggesting another rule requires re-route/manual | Routing/non-violation signal only |
| `CSR.ROUTE.002` | ROUTING_CONTROL | All | `RP-ROUTING` | Required report description + structural base | Description absent/ambiguous or routes multiple rules → manual | Manual triage; không finding |
| `CSR.ROUTE.003` | ROUTING_CONTROL | All | `RP-MEDIA-CONDITIONAL` | Media reference R; verified media observation required before image rule selection | `VERIFIED_MEDIA_OBSERVATION` X hiện tại; USER/PAGE manual | `NEEDS_MANUAL_REVIEW`; URL/reason không substantiation |
| `CSR.EVD.001` | EVIDENCE_CONTROL | All | `RP-EVIDENCE-CONTROL` | At least one critical requirement record marked missing | Missing requirement ID/kind/trigger must be explicit | `NEEDS_MANUAL_REVIEW + NO_ACTION` |
| `CSR.EVD.002` | EVIDENCE_CONTROL | All | `RP-EVIDENCE-CONTROL` | Material evidence items/findings conflict with traceable refs | Conflict unresolved after allowed context/counter review | `NEEDS_MANUAL_REVIEW + NO_ACTION` |
| `CSR.EVD.003` | EVIDENCE_CONTROL | All | `RP-EVIDENCE-CONTROL` | Required item has unreadable/inaccessible/unsupported state | No safe verified collector/fallback | Manual or operational failure; never reject |
| `CSR.EVD.004` | EVIDENCE_CONTROL | B/C only for Sprint 1 AI | `RP-EVIDENCE-CONTROL` | `SEM-COMPLETE-EVALUATION-SCOPE`; all material candidate rules evaluated; required evidence available; counter-evidence assessed; none substantiated | Any critical missing/conflict/unassessable candidate blocks this control | May permit `REJECT + KEEP_VISIBLE` recommendation; never auto-close |

## 9. Per-family critical burden

### 9.1. Safety

| Requirement | Behavior |
|---|---|
| Direct threat/encouragement text available | Evaluate exact observation |
| Meaning depends on reply/quotation | Parent context becomes critical |
| Claim is visual | Verified media becomes critical |
| Credibility/target unresolved | Manual |
| Potential imminent harm but evidence incomplete | Urgent human review; no final violation inference |

### 9.2. Hate

Required:

- direct target text;
- observable attack/degradation/threat;
- target person/group;
- explicit relation to approved protected characteristic;
- pinned protected-list version;
- relevant counter-context.

Forbidden:

- infer characteristic from name, appearance, location or stereotype;
- treat every insult as Hate;
- use reporter label as protected identity fact.

### 9.3. Harassment/bullying

Direct one-item harassment can be evaluated only when target and attack are observable in the
snapshot/context. Unwanted-contact, repetition or coordinated behavior requires pattern evidence not
present in Sprint 1.

### 9.4. Sexual/sensitive

Modality branches:

```text
text-only rule branch
→ may evaluate TARGET_TEXT_CONTENT

visual rule branch
→ requires VERIFIED_MEDIA_OBSERVATION
→ current collector missing
→ manual
```

Age, consent và exception context become critical when they change rule applicability.

### 9.5. Integrity/scam/spam

- Impersonation requires identity comparator/authority.
- Misinformation requires exact claim + authority-scoped counter-source.
- Scam can evaluate explicit content-level solicitation/deception, but must identify transaction
  intent rather than keywords.
- Spam/manipulation requires pattern history; report count alone is not evidence.

### 9.6. Privacy/IP

AI may:

- triage;
- identify sanitized target/reference;
- enumerate missing requirements;
- flag urgent risk for human review.

AI must not:

- decide consent/ownership/legality;
- treat claimant declaration as substantiation;
- copy sensitive evidence into explanation;
- recommend USER/PAGE suspension.

### 9.7. Restricted commerce

Required branches:

```text
active registry category
target content/media observation
transaction intent
platform policy scope
jurisdiction/license/age when category is regulated
exception/counter-context
```

Current registry remains proposed and verified media/external authority capability is incomplete;
therefore runtime ceiling remains manual.

## 10. COMMENT context escalation

Parent evidence changes from `O` to `C` when:

- target is a reply and meaning references direct parent;
- quotation/sarcasm/negation cannot be resolved from target alone;
- targeted subject appears only in BLOG/direct parent;
- transaction/threat context is incomplete without parent;
- potential exception appears in parent context.

Rules:

1. `PARENT_BLOG_CONTEXT` và `PARENT_COMMENT_CONTEXT` vẫn `CONTEXT_ONLY`.
2. Finding observation phải mô tả target COMMENT, không parent content.
3. Parent violation không được attribution sang target author.
4. Full thread/pattern không được suy từ direct parent.
5. Context missing khi material → manual, không reject.

## 11. Missing Evidence Requirement record

```json
{
  "missingEvidenceId": "ME-CSR-SEX-001-VERIFIED-MEDIA-01",
  "requiredByRuleId": "CSR.SEX.001",
  "requirementType": "EVIDENCE_KIND",
  "requiredEvidenceKind": "VERIFIED_MEDIA_OBSERVATION",
  "semanticRequirementCode": null,
  "triggerCondition": "REPORT_OR_TARGET_HAS_MATERIAL_IMAGE",
  "critical": true,
  "availability": "NOT_COLLECTED",
  "reasonCode": "VERIFIED_MEDIA_COLLECTOR_NOT_AVAILABLE",
  "recommendedNextStep": "HUMAN_REVIEW_MEDIA",
  "decisionEffect": "NEEDS_MANUAL_REVIEW"
}
```

Semantic requirement chưa có catalog kind:

```json
{
  "missingEvidenceId": "ME-CSR-IP-001-CLAIMANT-AUTHORITY",
  "requiredByRuleId": "CSR.IP.001",
  "requirementType": "SEMANTIC_REQUIREMENT",
  "requiredEvidenceKind": null,
  "semanticRequirementCode": "SEM-CLAIMANT-AUTHORITY",
  "triggerCondition": "COPYRIGHT_CLAIM_SELECTED",
  "critical": true,
  "availability": "NOT_COLLECTED",
  "reasonCode": "SECURE_LEGAL_INTAKE_NOT_IMPLEMENTED",
  "recommendedNextStep": "HUMAN_LEGAL_REVIEW",
  "decisionEffect": "NEEDS_MANUAL_REVIEW"
}
```

Missing record không phải evidence item và không được model tự xóa.

## 12. Counter-evidence requirements

Counter-evidence dùng existing Evidence IDs hoặc future authority records; không có separate
“counter evidence kind”.

| Family | Counter/exception cần xét |
|---|---|
| Safety | fiction, quotation, reporting, disavowal, help-seeking, prevention |
| Hate/Harassment | quotation, condemnation, education, self-reference, reclaimed expression |
| Sexual | medical, education, breastfeeding, childbirth, news, art, age/consent uncertainty |
| Integrity | correction, disclaimer, opinion, prediction, satire, current authoritative state |
| Scam/Spam | legitimate offer/context, consent, isolated item vs pattern, corrected content |
| Privacy | consent, authorization, public availability, public interest, data category |
| IP | claimant authority, ownership, license, permission, exception, counter-notice |
| Commerce | non-transaction context, news/education, active license, jurisdiction, age restriction |
| Relevance | parent topic/page scope |

Rules:

- unresolved material counter-evidence → `CSR.EVD.002`;
- absent critical counter-check → manual;
- model disagreement is not counter-evidence;
- context label alone does not prove exception.

## 13. Per-rule outcome

Allowed internal assessment:

```text
SUBSTANTIATED
NOT_SUBSTANTIATED
UNASSESSABLE
CONFLICTED
NOT_APPLICABLE
POLICY_INVALID
```

| Outcome | Điều kiện |
|---|---|
| `SUBSTANTIATED` | Required evidence/observations met; no critical missing/conflict; exception handled |
| `NOT_SUBSTANTIATED` | Complete material scope; required evidence available; criteria not met |
| `UNASSESSABLE` | Critical required evidence/context unavailable |
| `CONFLICTED` | Material evidence/counter-evidence conflict unresolved |
| `NOT_APPLICABLE` | Active rule scope excludes target/content branch |
| `POLICY_INVALID` | Version/status/schema invalid; no content decision |

Không dùng `NOT_SUBSTANTIATED` khi thực chất là missing evidence.

## 14. Decision aggregation

```text
validate operational + version gates
→ apply USER/PAGE manual override
→ resolve candidate rules from versioned intake mapping
→ evaluate structural base
→ evaluate each rule requirements/observations/counter-evidence
→ aggregate
```

### 14.1. `RESOLVE`

Chỉ khi:

- ít nhất một violation rule `SUBSTANTIATED`;
- không có material candidate rule đang `UNASSESSABLE/CONFLICTED`;
- finding cite rule/version và Evidence IDs;
- target BLOG/COMMENT;
- candidate action hợp action contract và active rule metadata;
- A0 giữ human authority.

### 14.2. `REJECT`

Chỉ khi:

- không violation rule nào substantiated;
- mọi material candidate rule đã `NOT_SUBSTANTIATED` hoặc `NOT_APPLICABLE`;
- `SEM-COMPLETE-EVALUATION-SCOPE` đạt;
- không critical missing/unreadable/conflict;
- `CSR.EVD.004` control condition đạt;
- target BLOG/COMMENT trong Sprint 1;
- recommendation đi với `KEEP_VISIBLE`;
- không auto-close report.

### 14.3. `NEEDS_MANUAL_REVIEW`

Khi:

- bất kỳ critical requirement missing;
- material conflict;
- media/pattern/legal/authority capability chưa có;
- policy/exception applicability mơ hồ nhưng request vẫn operationally valid;
- USER/CAFE_PAGE;
- high-harm urgent case cần human.

Action luôn `NO_ACTION`.

### 14.4. Operational/policy failure

Provider timeout, schema invalid, inactive version hoặc stale security envelope không được đổi thành
content decision.

## 15. Candidate action boundary

- M06 xác định rule finding burden, không dùng score để chọn action.
- BLOG/COMMENT `RESOLVE` chỉ có thể đi với `HIDE` hoặc `REMOVE` candidate theo existing contract.
- Exact HIDE-vs-REMOVE selection phải đến từ active rule action metadata và action-specific burden.
- Nếu active rule record chưa có action-specific burden, output phải manual/no-action thay vì model
  tự nâng severity.
- `REMOVE` execution cần C1 distinct second approver và revalidation.
- USER/PAGE action candidate luôn `NO_ACTION` trong Sprint 1.
- Không action nào auto-execute theo A0.

## 16. Current capability/readiness gaps

| Gap | Affected rules | Current effect |
|---|---|---|
| Rule Catalog vẫn `PROPOSED` | All | Chưa được dùng như active runtime authority |
| Verified media kind missing-only | SAF.002, SEX.001, SEX.002, ROUTE.003 và media branches | Manual |
| Pattern/history kind/collector chưa có | HAR.002 unwanted-contact, SPAM.001, USER/PAGE deep | Manual |
| Identity comparator chưa có | INT.001 | Manual |
| Authoritative fact adapter/kind chưa có | INT.002 external claims | Manual |
| Secure claimant/legal evidence chưa có | PRIV.001, IP.001, IP.002 | Human/legal manual |
| Restricted registry chưa active; jurisdiction/license adapter chưa có | COM.001 | Manual |
| Exception records chưa versioned/active theo rule | Multiple families | Ambiguous exception → manual |
| Rule records chưa có đầy đủ exact criteria/action burden | All violation rules | Không tự chọn action severity |
| Backend hiện generic evidence maps, chưa enforce matrix | All | Current runtime chưa compliant M06 |
| No representative evaluation/calibration dataset | All | Không numeric threshold authority |

## 17. Required future semantic catalog extensions

M06 xác nhận nhu cầu nhưng không tự thêm vào approved M05 catalog:

| Candidate semantic kind | Rules | Status |
|---|---|---|
| `BEHAVIOR_PATTERN_RECORD` | HAR.002, SPAM.001, USER/PAGE deep | `DEFERRED_NOT_CATALOGED` |
| `IDENTITY_AUTHENTICITY_RECORD` | INT.001 | `DEFERRED_NOT_CATALOGED` |
| `AUTHORITATIVE_FACT_RECORD` | INT.002 | `DEFERRED_NOT_CATALOGED` |
| `CLAIMANT_AUTHORITY_RECORD` | IP/Privacy | `DEFERRED_NOT_CATALOGED` |
| `LICENSE_PERMISSION_RECORD` | IP/Commerce | `DEFERRED_NOT_CATALOGED` |
| `PRIVACY_AUTHORIZATION_RECORD` | PRIV.001 | `DEFERRED_NOT_CATALOGED` |
| `JURISDICTION_AUTHORITY_RECORD` | COM/IP/Privacy | `DEFERRED_NOT_CATALOGED` |

Không đưa các kind này vào executable schema M07 như `AVAILABLE` nếu chưa có payload semantics,
collector, privacy và lifecycle approval. M07 chỉ cần hỗ trợ missing semantic requirement record.

## 18. Implementation impact map

### Backend

- versioned requirement registry theo Rule ID;
- conditional requirement evaluator;
- missing requirement records;
- per-rule outcomes;
- complete-scope checker cho `REJECT`;
- USER/PAGE manual clamp trước provider;
- catalog/version/activity validation;
- no score-based authority.

### n8n/provider

- nhận candidate rules và allowed requirement metadata từ Backend;
- không tự thêm/bỏ critical requirement;
- chỉ tham chiếu Evidence IDs;
- không tạo evidence;
- không biến missing thành not-substantiated;
- response bị Backend semantic revalidation.

### Admin FE

- hiển thị required/available/missing/counter theo rule;
- phân biệt `UNASSESSABLE` và `NOT_SUBSTANTIATED`;
- hiển thị manual lane/next step;
- không trình bày media URL, reporter claim hoặc score như proof;
- không hiển thị inactive/proposed rule như policy đang có hiệu lực.

M06 không thực hiện các thay đổi này.

## 19. M07 handoff

M07 phải tạo fixtures cho tối thiểu:

1. text rule đủ evidence;
2. COMMENT cần parent context nhưng missing;
3. image reason chỉ có URL;
4. verified media kind cố gửi `AVAILABLE` khi reserved;
5. USER/PAGE manual clamp;
6. external fact requirement missing;
7. privacy/IP claimant authority missing;
8. material conflict;
9. complete non-substantiation đủ điều kiện `REJECT`;
10. incomplete scope bị cấm `REJECT`;
11. inactive Rule Catalog;
12. unknown requirement/Rule/Evidence Kind;
13. high confidence nhưng critical missing;
14. substantiated rule nhưng another material rule unassessable;
15. score không đổi decision authority.

## 20. Decision package cần review

| ID | Đề xuất | Trạng thái |
|---|---|---|
| `M06-D01` | Matrix phải cover đủ 24 Rule ID | `APPROVED` |
| `M06-D02` | Structural base bắt buộc nhưng không substantiation violation | `APPROVED` |
| `M06-D03` | Evidence Kind availability không thay observation/criteria | `APPROVED` |
| `M06-D04` | BLOG/COMMENT dùng content-first conditional evaluation | `APPROVED` |
| `M06-D05` | USER/CAFE_PAGE luôn manual/no-provider/no-finding | `APPROVED` |
| `M06-D06` | Parent context chỉ critical khi material cho interpretation | `APPROVED` |
| `M06-D07` | Media URL/reference không đủ; verified media missing → manual | `APPROVED` |
| `M06-D08` | Pattern-dependent rules manual tới khi có pattern evidence | `APPROVED` |
| `M06-D09` | Impersonation manual tới khi có identity comparator/authority | `APPROVED` |
| `M06-D10` | Misinformation chỉ conditional ở narrow authoritative platform-fact branch | `APPROVED` |
| `M06-D11` | Explicit content-level scam branch có thể conditional; pattern/transaction ambiguity manual | `APPROVED` |
| `M06-D12` | Privacy/IP luôn human/legal lane trong Sprint 1 | `APPROVED` |
| `M06-D13` | Restricted commerce manual tới khi registry/authority requirements active | `APPROVED` |
| `M06-D14` | Hate yêu cầu pinned protected list và explicit because-of observation | `APPROVED` |
| `M06-D15` | Sexual rule không suy age và không đánh giá visual nếu media chưa verified | `APPROVED` |
| `M06-D16` | Counter-evidence/exception phải được xét theo từng rule | `APPROVED` |
| `M06-D17` | Critical missing/unreadable/conflict luôn manual, không reject | `APPROVED` |
| `M06-D18` | `REJECT` chỉ khi complete evaluated scope và EVD.004 đạt | `APPROVED` |
| `M06-D19` | Một substantiated rule không bù material candidate rule còn unassessable | `APPROVED` |
| `M06-D20` | Score/confidence không override requirement hoặc decision ceiling | `APPROVED` |
| `M06-D21` | A0 giữ mọi target mutation và report closure ở human authority | `APPROVED` |
| `M06-D22` | Exact action severity cần active rule action burden; thiếu thì manual | `APPROVED` |
| `M06-D23` | Proposed/inactive policy version không được dùng làm runtime authority | `APPROVED` |
| `M06-D24` | M07 encode matrix/negative fixtures nhưng không tự activate deferred kinds | `APPROVED` |

## 21. Acceptance criteria

- [x] Cover `24/24` Rule IDs.
- [x] Tách structural evidence, Evidence Kind, observation và semantic requirement.
- [x] Có requirement notation/profile.
- [x] Có target override BLOG/COMMENT vs USER/PAGE.
- [x] Có per-rule required/critical/current ceiling.
- [x] Có COMMENT context escalation.
- [x] Có missing requirement schema.
- [x] Có counter-evidence matrix.
- [x] Có aggregation cho RESOLVE/REJECT/manual/error.
- [x] Có capability/readiness gaps.
- [x] Có M07 fixture handoff.
- [x] Người dùng review và approve `M06-D01`–`M06-D24` bằng `APPROVE_G0-12M-06`.

## 22. Trạng thái gate

```text
Deliverable tồn tại: YES
Rule coverage: 24/24
Static content review: PASS
User approval: APPROVE_G0-12M-06
Source/runtime mutation: NONE

G0-12M-06 STATUS: COMPLETED
NEXT REQUIRED ACTION: IMPLEMENT_G0_12M_07
```
