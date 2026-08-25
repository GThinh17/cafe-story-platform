# S2-REBASE-01 — Tái xác định phạm vi Sprint 2 từ evidence Sprint 1

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `S2-REBASE-01` |
| Token thực hiện | `IMPLEMENT_S2_REBASE_01` |
| Trạng thái | `COMPLETED_REBASE_APPROVED` |
| Approval đã nhận | `APPROVE_S2_REBASE_01` |
| Nhánh khảo sát | `n8n/Ai-agent/fix-bug-report-admin` |
| Source HEAD | `9c155ff46570b0e1d3a151c4a64fa8e2dc5fd6cf` |
| Nguồn sự thật | Source/worktree hiện tại, Sprint 1 DD, re-audit và evidence package |
| Automation mode | Giữ nguyên `A0_RECOMMEND_ONLY` |
| Target runtime | Giữ nguyên BLOG/COMMENT; USER/CAFE_PAGE manual-only |
| Source/runtime thay đổi | Không |
| Production authority | `NOT_AUTHORIZED` |

Tài liệu này thay skeleton Sprint 2 bằng một scope có thứ tự phụ thuộc rõ ràng. Đây là bước rebase và
thiết kế governance, chưa phải approval sửa code.

## 2. Bound

### 2.1. Mục tiêu

1. Xác định Sprint 2 thực sự còn thiếu gì sau khi Sprint 1 đã đóng technical DoD.
2. Tách phần có thể thiết kế/code ngay khỏi phần cần dataset, owner, policy hoặc hạ tầng tương lai.
3. Sắp xếp thứ tự sao cho prompt không đi trước rule/evidence contract runtime.
4. Định nghĩa gate review riêng cho từng package, tránh triển khai cả Sprint bằng một token rộng.

### 2.2. Trong phạm vi

- runtime rule context giữa Backend và n8n/provider;
- strict output schema và Backend semantic validator;
- evaluation dataset/harness phục vụ regression chất lượng;
- rule-family prompt specialization ở phạm vi pilot;
- điều kiện để benchmark provider/model và cost/latency;
- traceability, acceptance criteria và approval sequence.

### 2.3. Ngoài phạm vi

- sửa FE, BE, database, n8n workflow hoặc provider configuration;
- thay model đang dùng hoặc gọi provider để benchmark;
- mở automation cao hơn A0;
- deep evidence cho USER/CAFE_PAGE;
- media OCR/vision;
- authoritative-source adapter thật;
- numeric threshold/calibration authority;
- xử lý năm production-readiness item còn mở;
- production deployment.

## 3. Evidence Sprint 1 dùng để rebase

| Evidence | Sự thật được phép dùng |
|---|---|
| `g0-12-done-reaudit.vi.md` | Technical DoD đạt `21/21`; production vẫn `9/14 NOT_READY` |
| Re-audit evidence `2026-07-29T16-32-07-654+07-00` | Backend `625`, Admin build/typecheck pass, ADV `12/12`, source fingerprint 15 file |
| `AdminReportAiPolicyCatalog.java` | Runtime candidate rule hiện chỉ có `ruleId` và một `ruleVersion` chung |
| `AdminReportAiSemanticValidator.java` | Đã có fail-closed, categorical guard, evidence-reference và independent-evidence guard |
| Canonical n8n workflow | Prompt hiện là policy chung tám dòng; strict schema được dựng trong code node |
| `rule-to-evidence-requirement-matrix.md` | Hồ sơ đã định nghĩa per-rule requirement, exception, current ceiling và action burden |
| `evaluation-dataset-design.md` | Dataset hiện chỉ đủ safety/semantic regression nhỏ, không đủ calibration/automation |
| ADV-001–ADV-012 | Chứng minh prompt boundary và normalization guard; provider không được gọi |

## 4. Những khoảng trống đã xác thực

### `S2RB-001` — Rule contract trong hồ sơ chưa được materialize đầy đủ ở runtime

Runtime `candidateRules` chỉ mang:

```text
ruleId
ruleVersion
```

Trong khi hồ sơ M06 đã yêu cầu thêm tối thiểu:

```text
rule type
target applicability
required/conditional/missing evidence
semantic observation requirements
counter-evidence/exception requirements
current evaluation ceiling
allowed candidate action ceiling
```

Hệ quả: provider nhận Rule ID nhưng chưa nhận đủ burden để đánh giá rule một cách deterministic.
Đây là khoảng trống nền tảng, phải xử lý trước prompt specialization.

### `S2RB-002` — Prompt runtime vẫn là prompt tổng quát

Prompt hiện bảo vệ tốt các invariant chung nhưng chưa có:

- criteria theo rule family;
- required/counter/missing evidence theo rule;
- exception handling theo family;
- target-specific context instruction cho BLOG/COMMENT;
- instruction về complete evaluation của mọi material candidate rule.

Prompt specialization là cần thiết, nhưng chỉ được làm sau khi Backend truyền rule context có type và
version rõ ràng. Không cho n8n tự sở hữu một policy catalog thứ hai.

### `S2RB-003` — Runtime schema strict nhưng chưa bounded đầy đủ

Schema có `additionalProperties=false` và enum, nhưng các string/array chính chưa có `maxLength`,
`maxItems`, pattern hoặc uniqueness tương ứng. Điều này không khớp hoàn toàn với thiết kế
`bounded arrays/strings` và làm tăng rủi ro output quá lớn, trùng reference hoặc khó kiểm soát chi phí.

### `S2RB-004` — Backend validator chưa thực thi toàn bộ burden theo rule

Validator hiện đã kiểm tra Rule ID, Evidence ID, independent evidence, categorical values, version
toàn request và decision/action clamp. Tuy nhiên chưa chứng minh:

- `finding.ruleVersion` khớp version candidate rule;
- mọi material candidate rule đã được đánh giá;
- required/conditional evidence của từng rule đã được thỏa;
- counter-evidence/exception material đã được xét;
- `SUPPORTED` chỉ xuất hiện khi current evaluation ceiling cho phép;
- exact action phù hợp action-specific burden của active rule record.

### `S2RB-005` — Chưa có evaluation dataset cho chất lượng model

ADV `12/12` là executable security/contract suite và không gọi provider. Các E2E Sprint 1 chứng minh
workflow/safety/no-mutation, không phải ground-truth model-quality dataset. Vì vậy chưa được tuyên bố:

- model accuracy;
- per-rule precision/recall;
- calibration;
- model A tốt hơn model B;
- prompt mới tốt hơn prompt cũ trên population đại diện.

### `S2RB-006` — Model/cost/latency benchmark chưa có prerequisite

Benchmark chỉ có ý nghĩa sau khi có:

1. immutable prompt/schema/rule-context candidates;
2. versioned evaluation dataset;
3. deterministic scoring rubric;
4. quality floor và safety hard gates;
5. cùng runtime conditions và cost accounting rule.

Do đó đây là package conditional, không phải việc đầu Sprint 2.

### `S2RB-007` — External authoritative source là capability riêng

Misinformation, identity, IP, ownership và một số commerce claim cần source authority, freshness,
jurisdiction, privacy/legal review và ownership. Không nên nhét adapter thật vào Sprint 2 core chỉ để
prompt có thêm dữ liệu.

### `S2RB-008` — Calibration vẫn là research-only

Hiện không có representative labeled dataset, observed outcome và acceptance metric theo
rule/target/action. Calibration không được dùng để đổi categorical score thành probability hoặc
automation threshold.

## 5. Phạm vi Sprint 2 sau rebase

### 5.1. Core bắt buộc

| Thứ tự | Package | Mục tiêu | Kết quả mong đợi |
|---|---|---|---|
| 1 | `S2-DD-01` | Detailed Design toàn Sprint 2 | Chốt contract, component ownership, migration/versioning, tests và rollback trước code |
| 2 | `S2-01` | Runtime Rule Context Contract | Backend là nguồn duy nhất phát rule metadata versioned; n8n chỉ tiêu thụ |
| 3 | `S2-02` | Schema + Semantic Invariant Hardening | Bounded strict schema; ruleVersion/completeness/burden/ceiling fail-closed |
| 4 | `S2-03` | Evaluation Dataset & Harness V1 | Dataset synthetic/sanitized, oracle/rubric versioned, báo cáo theo slice |
| 5 | `S2-04` | Rule-family Prompt Pilot | Prompt assembler từ approved rule context; pilot trên text-only BLOG/COMMENT |
| 6 | `S2-DONE-AUDIT` | Re-audit Sprint 2 | Traceability, regression, no-mutation, quality/safety report và residual risk |

### 5.2. Conditional

| Package | Điều kiện mở | Nếu thiếu điều kiện |
|---|---|---|
| `S2-05` Provider/model + cost/latency benchmark | `S2-03` dataset/harness approved và `S2-04` prompt candidates immutable | Giữ `DEFERRED_NOT_MEASURABLE` |

Benchmark không được chọn model chỉ theo giá hoặc latency. Mọi candidate phải qua safety hard gate
trước khi so sánh quality, cost và latency.

### 5.3. Deferred khỏi Sprint 2 core

| Hạng mục | Route khuyến nghị | Lý do |
|---|---|---|
| External authoritative-source adapters | Sprint 4 hoặc workstream riêng | Cần owner, legal/privacy, source trust/freshness và target evidence design |
| Numeric calibration/threshold | Future research gate | Chưa có representative labeled outcomes; không cấp automation authority |
| USER/CAFE_PAGE deep evidence | Sprint 4 | Quyết định K1 đang giữ manual-only |
| Media OCR/vision | Sprint 4 | Chưa có verified-media pipeline |
| Multi-instance replay store, n8n error transport | Sprint 3/Operations | Là security/operations, không phải prompt-quality core |
| Năm production-readiness item còn mở | Production readiness track | Không dùng Sprint 2 để che thiếu owner/inventory/external DB/drill/monitoring |

## 6. Nguyên tắc thiết kế bắt buộc

1. Backend tiếp tục là policy/evidence/action authority cuối.
2. n8n không giữ catalog rule độc lập và không tự mở rộng candidate rules.
3. Prompt không được biến rule `proposed` thành policy `active`.
4. Schema validity không thay semantic validation.
5. AI explanation không phải evidence; finding chỉ tham chiếu Evidence ID được cấp.
6. Thiếu/mâu thuẫn/không đủ critical evidence vẫn clamp
   `NEEDS_MANUAL_REVIEW + NO_ACTION`.
7. A0 và no-mutation invariant không đổi trong toàn Sprint 2.
8. Prompt/model/schema thay đổi phải versioned; không sửa alias âm thầm.
9. Evaluation report phải tách theo target/rule/evidence/attack/context slice.
10. Không dùng aggregate pass rate để che failure ở safety-critical slice.

## 7. Thứ tự gate và token

| Gate | Hành động | Approval sau review |
|---|---|---|
| `S2-REBASE-01` | Phạm vi và sequencing hiện tại | `APPROVE_S2_REBASE_01` |
| `S2-DD-01` | Viết Detailed Design source-backed, chưa code | `APPROVE_S2_DD_01` |
| `S2-01` | Runtime Rule Context Contract | `APPROVE_S2_01` |
| `S2-02` | Schema + semantic hardening | `APPROVE_S2_02` |
| `S2-03` | Dataset + evaluation harness | `APPROVE_S2_03` |
| `S2-04` | Rule-family prompt pilot | `APPROVE_S2_04` |
| `S2-05` | Conditional provider benchmark | `APPROVE_S2_05` |
| `S2-DONE-AUDIT` | Audit đóng Sprint 2 | `APPROVE_S2_DONE` |

Sau khi `APPROVE_S2_REBASE_01`, token được khuyến nghị tiếp theo là:

```text
IMPLEMENT_S2_DD_01
```

Token này chỉ cho phép viết Detailed Design, chưa sửa source.

## 8. Acceptance criteria của rebase

- [x] Đối chiếu source/workflow hiện tại thay vì chỉ lặp lại skeleton Sprint 2.
- [x] Phân biệt core, conditional và deferred.
- [x] Chỉ ra dependency rule context → validator/schema → dataset → prompt → benchmark.
- [x] Giữ A0, no-mutation và manual-only target boundary.
- [x] Không biến calibration/model comparison thành claim chưa có evidence.
- [x] Có gate review riêng cho từng package.
- [x] Không sửa source/runtime trong bước rebase.
- [x] Người dùng review và chốt bằng `APPROVE_S2_REBASE_01`.

## 9. Các decision được chốt nếu approval

`APPROVE_S2_REBASE_01` đồng nghĩa chốt:

1. Sprint 2 core gồm `S2-DD-01`, `S2-01`–`S2-04` và audit;
2. `S2-05` chỉ mở khi dataset/harness và prompt candidate đã approved;
3. authoritative adapters, numeric calibration và deep target evidence không thuộc Sprint 2 core;
4. A0/no-mutation/USER-CAFE_PAGE manual-only không đổi;
5. phải duyệt Detailed Design trước khi sửa code Sprint 2.

## 10. Kết luận

Sprint 2 không nên bắt đầu bằng “viết prompt hay hơn”. Khoảng trống lớn nhất hiện tại là rule/evidence
burden trong hồ sơ chưa được materialize đầy đủ vào runtime. Vì vậy thứ tự đúng là:

```text
Detailed Design
→ Runtime Rule Context
→ Schema/Semantic Validator
→ Evaluation Dataset/Harness
→ Rule-family Prompt Pilot
→ Conditional Provider Benchmark
→ Sprint 2 Audit
```

Trạng thái hiện tại: `COMPLETED_REBASE_APPROVED`.
