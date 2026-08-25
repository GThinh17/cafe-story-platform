# Traceability Matrix — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-09` |
| Approval chat | `APPROVE_G0-09` |
| Trạng thái | `PROPOSED` |
| Matrix version | `TM-2.0.0-proposed.1` |
| Theory baseline | 74 approved decisions |
| Policy baseline | 92 proposed clauses |
| Rule baseline | 24 proposed rules/controls |
| Current-state baseline | 19 audited gaps |

Approval G0-09 chỉ cho phép lập traceability. Nó không phê duyệt business decisions,
không làm policy/rule thành `ACTIVE` và không chứng minh source/runtime đã compliant.

## 2. Trạng thái trace

| Status | Ý nghĩa |
|---|---|
| `COVERED_BY_DESIGN` | Đã có theory/policy/rule artifact ở hồ sơ |
| `PLANNED_NOT_WRITTEN` | Artifact đích có trong cây nhưng nội dung chi tiết chưa được duyệt |
| `DECISION_REQUIRED` | Cần business approval ở G0-10 |
| `IMPLEMENTATION_REQUIRED` | Có requirement nhưng source chưa được sửa/xác nhận compliant |
| `TEST_REQUIRED` | Chưa có test/evaluation theo contract V2 |
| `RUNTIME_NOT_VERIFIED` | Chưa chứng minh runtime/API/UI/n8n sau implementation |
| `FUTURE_SCOPE` | Không block canonical Sprint 1 nếu được business chấp thuận |

## 3. Current-state gap → requirement → delivery

| Trace ID | Gap | Theory | Policy | Rule/control | Delivery artifact dự kiến | Verification dự kiến | Current status |
|---|---|---|---|---|---|---|---|
| `TR-GAP-001` | `CG-001` | `E1`–`E8`, `H1`–`H8`, `U3`, `XA1`–`XA4` | `CP-001`–`CP-004`, `RD-001`–`RD-007` | `CSR.EVD.001`–`CSR.EVD.004`; mọi violation rule | `05-evidence-standard/*`, `06-ai-contract/input-schema-v2.md`, `output-schema-v2.md` | semantic/contract/safety tests | COVERED_BY_DESIGN → IMPLEMENTATION_REQUIRED |
| `TR-GAP-002` | `CG-002` | `R1`–`R10`, `U1`–`U9`, `H9` | `SF-001`–`SF-010`, `RD-008`, `CP-015` | Mọi rule; score không cấp authority | `04-domain-contract/score-contract.md`, `06-ai-contract/score-normalization.md` | calibration + semantic tests | COVERED_BY_DESIGN → DECISION_REQUIRED |
| `TR-GAP-003` | `CG-003` | `HI1`–`HI9`, `AR1`–`AR10`, `M2`–`M4` | `CP-006`–`CP-008`, `EA-001`–`EA-012` | 24/24 ở `A0` | `04-domain-contract/action-contract.md`, `07-system-design/auto-apply-design.md` | safety/action matrix tests | COVERED_BY_DESIGN → DECISION_REQUIRED |
| `TR-GAP-004` | `CG-004` | `E1`, `R3`, `H3` | `PT-001`–`PT-008` | `CSR.ROUTE.001`–`CSR.ROUTE.003`, `CSR.REL.001`; mapping 22 reasons | `04-domain-contract/terminology.md`, approved intake-catalog migration | reason mapping/compatibility tests | COVERED_BY_DESIGN → DECISION_REQUIRED |
| `TR-GAP-005` | `CG-005` | `E5`, `XA3`, `XA6` | `CP-002`, `CP-012`, `PP-001`–`PP-010`, `PV-001`–`PV-010` | 24 stable Rule IDs | catalog manifest + semantic validator | unknown/free-form rule rejection tests | COVERED_BY_DESIGN → IMPLEMENTATION_REQUIRED |
| `TR-GAP-006` | `CG-006` | `E1`, `E4`, `E6`, `H3`–`H5`, `XA1` | `CP-001`, `CP-003`, `PT-006` | Routing + evidence controls | `05-evidence-standard/evidence-source-types.md` | trust-level/adversarial tests | COVERED_BY_DESIGN → IMPLEMENTATION_REQUIRED |
| `TR-GAP-007` | `CG-007` | `E2`, `H8`, `U3`, `HI4` | `CP-004`, `RD-003`, `EA-004`, `PX-009` | `CSR.ROUTE.003`, `CSR.EVD.003` | `08-target-evidence/*`, `06-ai-contract/fallback-rules.md` | unreadable-media abstention tests | COVERED_BY_DESIGN → IMPLEMENTATION_REQUIRED |
| `TR-GAP-008` | `CG-008` | `AR6`, `AR9`, `XA6`, `XA9`, `XA11` | `CP-010`, `CP-012`, `EA-009`, `PV-001`–`PV-007` | Catalog/rule version trên mọi finding | input/output schemas, audit/persistence/idempotency design | version/hash/correlation tests | COVERED_BY_DESIGN → IMPLEMENTATION_REQUIRED |
| `TR-GAP-009` | `CG-009` | `XA9`, authority boundary `HI1`–`HI2` | `CP-017`, `EA-001`, `PP-004` | Không thuộc content rule | `07-system-design/security-design.md`, `n8n-design.md` | signature/freshness/replay tests | PLANNED_NOT_WRITTEN |
| `TR-GAP-010` | `CG-010` | `HI7`–`HI8`, `AR5`, `AR10` | `CP-007`, `CP-016`, `EA-007`–`EA-010`, `PV-005` | Mọi punitive rule | auto-apply, rollback, audit, state-transition design | stale/rollback/appeal tests | DECISION_REQUIRED |
| `TR-GAP-011` | `CG-011` | `HI6`, `XA4`–`XA5`, `U5` | `CP-009`, `RD-010`–`RD-011` | Findings và Evidence Controls | `07-system-design/frontend-design.md` | Admin UI E2E/accessibility tests | PLANNED_NOT_WRITTEN |
| `TR-GAP-012` | `CG-012` | `U8`, `AR11`–`AR12` | `CP-013`, `RD-004`, `RD-014`, `PX-009` | Không biến operational error thành rule result | `07-system-design/error-contract.md`, `06-ai-contract/fallback-rules.md` | retry/error taxonomy tests | PLANNED_NOT_WRITTEN |
| `TR-GAP-013` | `CG-013` | `XA7`–`XA8` | `CP-010`–`CP-011`, `PV-004`, `PV-010` | Evidence references, không raw secret | evidence retention, audit, persistence design | redaction/access/retention tests | DECISION_REQUIRED |
| `TR-GAP-014` | `CG-014` | `U9`, `R10`, `HI2` | `RD-001`–`RD-008`, `EA-001`, `EA-012` | Registry + compatibility rules | domain contracts + persistence design | BE validator/DB constraint tests | PLANNED_NOT_WRITTEN |
| `TR-GAP-015` | `CG-015` | `M5`, `U6`, `H9`, `R9` | `CP-014`–`CP-015`, `SF-005`, `SF-010` | 24/24 cần representative fixtures | `10-verification/evaluation-dataset-design.md`, quality metrics | per-rule/target/error dataset | TEST_REQUIRED |
| `TR-GAP-016` | `CG-016` | Terminology decision `D5` | `EA-001`, decision/action compatibility | `KEEP_VISIBLE`; legacy adapter only | action/terminology/semantic-invariant docs | adapter/API/UI copy tests | IMPLEMENTATION_REQUIRED |
| `TR-GAP-017` | `CG-017` | `AR7`, `AR12` | `EA-011`, `RD-013`–`RD-014` | Không thuộc content rule | action/error contract + frontend design | per-item/per-stage bulk E2E | IMPLEMENTATION_REQUIRED |
| `TR-GAP-018` | `CG-018` | `AR9`, `XA6` | `PV-001`–`PV-009` | Active catalog version phải pin | rollout/readiness/monitoring docs | published webhook + FE→BE→n8n→provider E2E | RUNTIME_NOT_VERIFIED |
| `TR-GAP-019` | `CG-019` | `XA7`, history principle | `PV-004`, `PV-010` | Không backfill finding giả | approved data cleanup/migration plan | metadata + rollback verification | FUTURE_SCOPE |

## 4. Theory coverage

Mỗi approved theory ID xuất hiện ít nhất một lần trong bảng sau. `Mapped policy`
chỉ thể hiện design linkage, không chứng minh implementation.

| Trace ID | Theory IDs | Mapped policy | Rule/delivery effect | Status |
|---|---|---|---|---|
| `TR-TH-001` | `M1`, `M2`, `M3`, `M4`, `M5` | `CP-005`, `CP-006`, `CP-014`, `RD-003`, `EA-003` | Per-rule/action evaluation; manual escalation | COVERED_BY_DESIGN |
| `TR-TH-002` | `E1`, `E2`, `E3`, `E4`, `E5`, `E6`, `E7`, `E8` | `CP-001`–`CP-004`, `RD-001`, `RD-002`, `RD-010`, `PV-004` | Finding cites evidence/rule; historical AI output not evidence | COVERED_BY_DESIGN |
| `TR-TH-003` | `H1`, `H2`, `H3`, `H4`, `H5`, `H6`, `H7`, `H8`, `H9` | `CP-001`–`CP-006`, `CP-015`, `SF-010` | Rule-specific burden, counter/missing evidence, no premature threshold | COVERED_BY_DESIGN |
| `TR-TH-004` | `U1`, `U2`, `U3`, `U4`, `U5`, `U6`, `U7`, `U8`, `U9` | `SF-003`–`SF-006`, `RD-003`, `RD-004`, `RD-007`, `RD-008`, `EA-005` | Score/uncertainty semantic contract | COVERED_BY_DESIGN |
| `TR-TH-005` | `R1`, `R2`, `R3`, `R4`, `R5`, `R6`, `R7`, `R8`, `R9`, `R10` | `SF-001`–`SF-010`, `CP-005`–`CP-007`, `EA-006` | Tách likelihood/harm/action risk; legacy risk deprecated | COVERED_BY_DESIGN |
| `TR-TH-006` | `HI1`, `HI2`, `HI3`, `HI4`, `HI5`, `HI6`, `HI7`, `HI8`, `HI9` | `CP-007`–`CP-010`, `CP-016`, `EA-001`–`EA-012`, `RD-009`–`RD-011` | Human authority, UI evidence, override/revalidation | COVERED_BY_DESIGN; DECISION_REQUIRED |
| `TR-TH-007` | `AR1`, `AR2`, `AR3`, `AR4`, `AR5`, `AR6`, `AR7`, `AR8`, `AR9`, `AR10`, `AR11`, `AR12` | `CP-008`, `CP-012`–`CP-013`, `EA-002`–`EA-011`, `RD-013`–`RD-014`, `PV-005` | A0, allowlist, per-item outcome, retry/version/rollback | COVERED_BY_DESIGN |
| `TR-TH-008` | `XA1`, `XA2`, `XA3`, `XA4`, `XA5`, `XA6`, `XA7`, `XA8`, `XA9`, `XA10`, `XA11`, `XA12` | `CP-002`–`CP-003`, `CP-009`–`CP-012`, `RD-010`–`RD-012`, `PV-001`–`PV-010` | Structured rationale, immutable audit/version, no chain-of-thought | COVERED_BY_DESIGN |

Theory coverage: `74/74`.

## 5. Policy clause → downstream package

| Trace ID | Policy clauses | Rule/control relation | Downstream artifacts | Required verification | Status |
|---|---|---|---|---|---|
| `TR-POL-001` | `CP-001`, `CP-002`, `CP-003`, `CP-004`, `CP-005`, `CP-006`, `CP-007`, `CP-008`, `CP-009`, `CP-010`, `CP-011`, `CP-012`, `CP-013`, `CP-014`, `CP-015`, `CP-016`, `CP-017`, `CP-018` | All rules plus routing/evidence controls | evidence, AI contract, BE/FE/n8n/security/audit designs | contract, safety, adversarial, E2E | PLANNED_NOT_WRITTEN |
| `TR-POL-002` | `PT-001`, `PT-002`, `PT-003`, `PT-004`, `PT-005`, `PT-006`, `PT-007`, `PT-008` | 22-reason mapping; `CSR.ROUTE.001`–`003`, `CSR.REL.001` | terminology, catalog manifest, migration | reason/rule/target compatibility | COVERED_BY_DESIGN → IMPLEMENTATION_REQUIRED |
| `TR-POL-003` | `SF-001`, `SF-002`, `SF-003`, `SF-004`, `SF-005`, `SF-006`, `SF-007`, `SF-008`, `SF-009`, `SF-010` | Score context on every finding/action | score contract/normalization/evaluation | semantic + calibration | DECISION_REQUIRED |
| `TR-POL-004` | `RD-001`, `RD-002`, `RD-003`, `RD-004`, `RD-005`, `RD-006`, `RD-007`, `RD-008`, `RD-009`, `RD-010`, `RD-011`, `RD-012`, `RD-013`, `RD-014` | Violation and Evidence Control outcome | decision/action/error/state contracts | decision matrix + bulk tests | PLANNED_NOT_WRITTEN |
| `TR-POL-005` | `EA-001`, `EA-002`, `EA-003`, `EA-004`, `EA-005`, `EA-006`, `EA-007`, `EA-008`, `EA-009`, `EA-010`, `EA-011`, `EA-012` | All rules are A0 in current catalog | action, auto-apply, idempotency, rollback design | authority/revalidation/idempotency tests | DECISION_REQUIRED |
| `TR-POL-006` | `PP-001`, `PP-002`, `PP-003`, `PP-004`, `PP-005`, `PP-006`, `PP-007`, `PP-008`, `PP-009`, `PP-010` | Multi-rule evaluation and version precedence | semantic validator + release manifest | conflict/invalid-version tests | PLANNED_NOT_WRITTEN |
| `TR-POL-007` | `PX-001`, `PX-002`, `PX-003`, `PX-004`, `PX-005`, `PX-006`, `PX-007`, `PX-008`, `PX-009`, `PX-010` | Rule exception candidate, human authority | exception/override/audit design | scope/expiry/authority tests | DECISION_REQUIRED |
| `TR-POL-008` | `PV-001`, `PV-002`, `PV-003`, `PV-004`, `PV-005`, `PV-006`, `PV-007`, `PV-008`, `PV-009`, `PV-010` | Catalog `RC-2.0.0-proposed.1`; legacy marker | versioning, persistence, rollout/rollback | checksum/history/revalidation tests | PLANNED_NOT_WRITTEN |

Policy coverage: `92/92`.

## 6. Rule/control → evidence and tests

| Trace ID | Rule/control | Policy anchor | Evidence profile | Planned test focus | Status |
|---|---|---|---|---|---|
| `TR-RULE-001` | `CSR.SAF.001` | `CP-005`, `RD-005`, `EA-003` | threat quote/context/target/provenance | threat vs quotation/satire; high-harm escalation | TEST_REQUIRED |
| `TR-RULE-002` | `CSR.SAF.002` | `CP-002`, `RD-005`, `PX-007` | violent/exploitative observation + context | promotion vs news/education; unreadable media | TEST_REQUIRED |
| `TR-RULE-003` | `CSR.SAF.003` | `CP-005`, `RD-003`, `PX-008` | self-harm direction/context | encouragement vs recovery/support | TEST_REQUIRED |
| `TR-RULE-004` | `CSR.HATE.001` | `CP-002`, `RD-005`, `PX-007` | target/protected-class/attack/context | attack vs mention/counter-speech | DECISION_REQUIRED |
| `TR-RULE-005` | `CSR.HAR.001` | `CP-002`, `RD-005`, `EA-006` | targeted abuse + subject/context | criticism vs targeted harassment | TEST_REQUIRED |
| `TR-RULE-006` | `CSR.HAR.002` | `CP-002`, `RD-003`, `EA-006` | interaction history/boundary | pattern absent; wanted vs unwanted contact | TEST_REQUIRED |
| `TR-RULE-007` | `CSR.SEX.001` | `CP-002`, `RD-003`, `PX-007` | verified visual/text context | explicit vs medical/art/education; no vision | DECISION_REQUIRED |
| `TR-RULE-008` | `CSR.SEX.002` | `CP-002`, `RD-005`, `PX-007` | sexualized/sensitive observation | explicit context and ambiguity | DECISION_REQUIRED |
| `TR-RULE-009` | `CSR.INT.001` | `CP-002`, `RD-005`, `EA-006` | claimed/reference identity + material deception | parody/fan/shared name | TEST_REQUIRED |
| `TR-RULE-010` | `CSR.INT.002` | `CP-002`, `RD-006`, `SF-005` | exact material claim + reliable counter-source | opinion/satire/disputed fact | DECISION_REQUIRED |
| `TR-RULE-011` | `CSR.INT.003` | `CP-002`, `RD-005`, `EA-006` | offer/request/deception/target link | legitimate trade vs scam; content vs actor burden | TEST_REQUIRED |
| `TR-RULE-012` | `CSR.SPAM.001` | `CP-002`, `RD-005`, `PT-007` | repetition/unsolicited/manipulation pattern | one item vs pattern; technical retry duplicate | DECISION_REQUIRED |
| `TR-RULE-013` | `CSR.PRIV.001` | `CP-011`, `RD-005`, `PX-003` | sanitized data/context/consent | consent/public-interest/redaction | DECISION_REQUIRED |
| `TR-RULE-014` | `CSR.IP.001` | `CP-002`, `RD-003`, `PX-007` | work/content/claimant authority/comparison | ownership/license/exception | DECISION_REQUIRED |
| `TR-RULE-015` | `CSR.IP.002` | `CP-002`, `RD-003`, `PX-007` | right/reference/use/authority | authorized/nominative/legal dispute | DECISION_REQUIRED |
| `TR-RULE-016` | `CSR.COM.001` | `CP-002`, `RD-003`, `PP-001` | approved list/version/item/transaction intent | mention vs offer; list/jurisdiction | DECISION_REQUIRED |
| `TR-RULE-017` | `CSR.REL.001` | `PT-003`, `PT-004`, `RD-006` | community context/relevance signal | must not create punitive finding by default | TEST_REQUIRED |
| `TR-RULE-018` | `CSR.ROUTE.001` | `PT-004`, `CP-001` | preference claim only | dislike never becomes violation | TEST_REQUIRED |
| `TR-RULE-019` | `CSR.ROUTE.002` | `PT-005`, `RD-003` | description used only to select candidates | other/blank/unknown route | TEST_REQUIRED |
| `TR-RULE-020` | `CSR.ROUTE.003` | `PT-002`, `RD-003`, `CP-018` | verified visual observation or unreadable marker | URL-only/prompt-injection/media failure | TEST_REQUIRED |
| `TR-RULE-021` | `CSR.EVD.001` | `CP-004`, `RD-003`, `EA-004` | explicit critical-missing list | manual + no action; never reject | TEST_REQUIRED |
| `TR-RULE-022` | `CSR.EVD.002` | `CP-004`, `PP-008`, `RD-003` | conflicting evidence references | conflict blocks automation | TEST_REQUIRED |
| `TR-RULE-023` | `CSR.EVD.003` | `CP-004`, `PX-009`, `RD-003` | unreadable/unsupported evidence marker | abstain without hallucinating media | TEST_REQUIRED |
| `TR-RULE-024` | `CSR.EVD.004` | `RD-006`, `CP-004`, `H8` | evaluated scope + sufficient negative basis | valid reject vs missing-evidence reject | TEST_REQUIRED |

Rule/control coverage: `24/24`.

## 7. Delivery package map

| Package | Inputs traced | Output status | Activation condition |
|---|---|---|---|
| `04-domain-contract/*` | score, decision, action, state semantics | `PLANNED_NOT_WRITTEN` | G0-10 decisions + Sprint 1 DD |
| `05-evidence-standard/*` | E/H/XA theory; evidence controls | `PLANNED_NOT_WRITTEN` | minimum evidence decision |
| `06-ai-contract/*` | policy/rule/version/uncertainty/security | `PLANNED_NOT_WRITTEN` | schema/prompt design approval |
| `07-system-design/*` | BE/FE/n8n/persistence/security/operations | `PLANNED_NOT_WRITTEN` | G0-11 |
| `08-target-evidence/*` | rule applicability and evidence profiles | `PLANNED_NOT_WRITTEN` | target-depth decision |
| `09-sprints/sprint-01-safety-contract.md` | P0/P1 canonical safety scope | `PLANNED_NOT_WRITTEN` | G0-10 approved decisions |
| `10-verification/*` | all clauses/rules/gaps | `PLANNED_NOT_WRITTEN` | contract and implementation artifacts |
| `11-operations/*` | rollout, rollback, monitoring, incident | `PLANNED_NOT_WRITTEN` | implementation candidate ready |
| `12-decisions/*` | business/legal/owner choices | `RESOLVED_G0-10_12_OF_12` | Detailed Design must consume selected options |

## 8. Decision queue cho G0-10

| Decision ID | Nội dung | Affected trace |
|---|---|---|
| `BD-001` | Policy/catalog owner và approval authority | Versioning, all rules |
| `BD-002` | Exact auto-apply allowlist; có cho `HIDE` hay giữ A0 | `CG-003`, `EA-*`, all rules |
| `BD-003` | Two-person approval, override và role matrix | `CG-010`, `HI9`, `PX-*` |
| `BD-004` | Threshold/calibration acceptance | `CG-002`, `CG-015`, `SF-*` |
| `BD-005` | Protected characteristics | `CSR.HATE.001` |
| `BD-006` | Sexual/sensitive exceptions và age-sensitive scope | `CSR.SEX.001`, `CSR.SEX.002` |
| `BD-007` | Misinformation scope và authoritative source model | `CSR.INT.002` |
| `BD-008` | Restricted-goods list và jurisdiction | `CSR.COM.001` |
| `BD-009` | IP/privacy legal workflow | `CSR.IP.*`, `CSR.PRIV.001` |
| `BD-010` | Intake reason active/deprecated/alias disposition | `CG-004`, `PT-*` |
| `BD-011` | Target-specific depth trong Sprint 1 | `08-target-evidence/*`, 24 rules |
| `BD-012` | Retention, appeal/rollback SLA và operations owner | `CG-010`, `CG-013`, `CP-011`, `CP-016` |

Queue này là snapshot được tạo tại G0-09. Kết quả G0-10 ngày `2026-07-23`: `12/12 APPROVED`;
selected options và approval mode nằm trong `12-decisions/business-decision-review.md`.

## 9. Coverage conclusion

```text
CURRENT_GAPS             19/19 TRACED
THEORY_DECISIONS         74/74 TRACED
POLICY_CLAUSES           92/92 TRACED
RULES_AND_CONTROLS       24/24 TRACED
IMPLEMENTED_REQUIREMENTS  0 CLAIMED
RUNTIME_VERIFIED          0 CLAIMED
```

G0-09 đã hoàn thành với các count trên. G0-10 sau đó đã chốt 12/12 business decisions;
bước tiếp theo là G0-11 Sprint 1 Detailed Design, chưa được tự động authorize.
