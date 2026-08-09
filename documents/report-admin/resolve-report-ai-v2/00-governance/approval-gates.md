# Approval Gates

## Trạng thái tổng

| Gate | Nội dung | Trạng thái |
|---|---|---|
| `G0-00`–`G0-09` | Charter, theory, audit, policy, rules, traceability | `COMPLETED` |
| `G0-10` | Business decision review | `COMPLETED_12_OF_12` |
| `G0-11` | Sprint 1 Detailed Design | `COMPLETED_REVIEW_READY` |
| `G0-12` | Final pre-code gate | `APPROVED_IMPLEMENTATION_STARTED` |
| `G0-12M` | Evidence Metadata Contract | `COMPLETED_M08_APPROVED` |
| `G0-12A` | HMAC, freshness và replay security boundary | `COMPLETED_SOURCE_STATIC_APPROVED` |
| `G0-12B` | Changed-file coverage gate | `COMPLETED_COVERAGE_APPROVED` |
| `G0-12C` | PostgreSQL/Flyway và Backend runtime gate | `COMPLETED_RUNTIME_APPROVED` |

## G0-10 sub-gates

| Gate | Decision | Trạng thái | Approval token |
|---|---|---|---|
| `G0-10A` | Policy/catalog ownership | `COMPLETED_A1` | `APPROVE_G0-10A` |
| `G0-10B` | Automation allowlist | `COMPLETED_B1` | `APPROVE_G0-10B` |
| `G0-10C` | Role/override/two-person | `COMPLETED_C1` | `APPROVE_G0-10C` |
| `G0-10D` | Threshold/calibration | `COMPLETED_D1` | `APPROVE_G0-10D` |
| `G0-10E` | Protected characteristics | `COMPLETED_E1` | `APPROVE_G0-10E` |
| `G0-10F` | Sexual/sensitive scope | `COMPLETED_F1` | `APPROVE_G0-10F` |
| `G0-10G` | Misinformation scope | `COMPLETED_G1` | `APPROVE_G0-10G` |
| `G0-10H` | Restricted goods | `COMPLETED_H1` | Batch delegation `2026-07-23` |
| `G0-10I` | IP/privacy workflow | `COMPLETED_I1` | Batch delegation `2026-07-23` |
| `G0-10J` | Intake reason disposition | `COMPLETED_J1` | Batch delegation `2026-07-23` |
| `G0-10K` | Target-specific depth | `COMPLETED_K1` | Batch delegation `2026-07-23` |
| `G0-10L` | Retention/SLA/operations | `COMPLETED_L1` | Batch delegation `2026-07-23` |

Approval riêng A–G và batch delegation H–L chỉ chốt business decisions trong G0-10.
Không suy ra quyền sửa source, activate policy hoặc bắt đầu G0-11.

## G0-11 record

| Thuộc tính | Giá trị |
|---|---|
| Approval token | `APPROVE_G0-11` |
| Deliverable chính | `09-sprints/sprint-01-safety-contract.md` |
| Scope | FE → BE → n8n → BE → FE Detailed Design, evidence-first, A0 |
| Gap routing | `19/19` |
| G0-10 realization | `12/12` |
| Source/runtime mutation | `0` |

G0-11 hoàn thành tài liệu review-ready. `APPROVE_G0-12` đã cho phép bắt đầu implementation theo
đúng Sprint 1 scope nhưng không cho phép production deployment. Tiến độ chi tiết và toàn bộ roadmap
được quản lý tại `../MASTER-ROADMAP-CHECKLIST.md`.

## G0-12M record

| Gate | Nội dung | Trạng thái | Approval token |
|---|---|---|---|
| `G0-12M-00` | Ranh giới Claim/Metadata/Evidence/Observation/Finding/Recommendation | `COMPLETED` | `APPROVE_G0-12M-00` |
| `G0-12M-01` | Common Evidence Envelope | `COMPLETED` | `APPROVE_G0-12M-01` |
| `G0-12M-02` | BLOG metadata schema | `COMPLETED` | `APPROVE_G0-12M-02` |
| `G0-12M-03` | COMMENT metadata schema | `COMPLETED` | `APPROVE_G0-12M-03` |
| `G0-12M-04` | USER/CAFE_PAGE minimal manual-only metadata | `COMPLETED` | `APPROVE_G0-12M-04` |
| `G0-12M-05` | Evidence Kind Catalog | `COMPLETED` | `APPROVE_G0-12M-05` |
| `G0-12M-06` | Rule-to-Evidence Requirement Matrix | `COMPLETED` | `APPROVE_G0-12M-06` |
| `G0-12M-07` | JSON Schema, fixtures và contract version | `COMPLETED` | `APPROVE_G0-12M-07` |
| `G0-12M-08` | Evidence Metadata Contract cross-review | `COMPLETED` | `APPROVE_G0-12M-08` |

`G0-12M-08` đã đóng Evidence Metadata Contract và chuyển sang chuỗi implementation G0-12A–G0-12F.
G0-12A–G0-12F đều đã nhận approval riêng; bước tiếp theo là audit `G0-12-DONE`.

## G0-12A record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | HMAC request/response, timestamp freshness và nonce replay protection |
| Trạng thái | `COMPLETED_SOURCE_STATIC_APPROVED` |
| Approval token đã nhận | `APPROVE_G0-12A` |
| Ngày approval | `2026-07-26` |
| Detailed Design | `../09-sprints/g0-12a-security-boundary-detailed-design.vi.md` |
| Backend verification | `45/45 PASS`; Spring context `PASS` |
| New signer coverage | `100% line / 85.71% branch` |
| n8n export verification | valid/replay/stale/tamper/invalid signature/signed response `PASS`; `active=false` |
| Source commit | `bcc7932`; không chứa documents/secret |
| Runtime verification | `NOT_RUN`; Docker daemon unavailable và HMAC secret chưa cấu hình |

Approval G0-12A chỉ đóng source/static security boundary. Nó không cho phép bỏ qua G0-12B–G0-12F,
không publish workflow và không cấp production authority.

## G0-12B record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Focused/changed-file coverage cho 5 class logic Sprint 1 |
| Trạng thái | `COMPLETED_COVERAGE_APPROVED` |
| Approval token đã nhận | `APPROVE_G0-12B` |
| Ngày phê duyệt | `2026-07-26` |
| Detailed Design | `../09-sprints/g0-12b-coverage-gate-detailed-design.vi.md` |
| Focused verification | `56/56 PASS` |
| Full Backend regression | `617` tests; `0` failure; `0` error; `1` PostgreSQL integration skip |
| Coverage gate | 5/5 class đạt `100%` line và `>=85%` branch |
| Source/test commit | `f608528`; không chứa documents |
| Runtime verification | `NOT_IN_SCOPE`; chuyển G0-12C–G0-12E |

Approval G0-12B chỉ đóng coverage gate. Nó không xác nhận PostgreSQL/Flyway runtime, published n8n/provider,
Admin UI/E2E hoặc production deployment.

## G0-12C record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Apply migration V2 và verify Backend runtime trên PostgreSQL disposable |
| Trạng thái | `COMPLETED_RUNTIME_APPROVED` |
| Approval token đã nhận | `APPROVE_G0-12C` |
| Ngày phê duyệt | `2026-07-26` |
| Ngày triển khai | `2026-07-26` |
| Detailed Design | `../09-sprints/g0-12c-postgresql-backend-runtime-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-26T13-26-59-327+07-00/` |
| Flyway | baseline `20260719.01`; migration `20260723.01` success; restart up-to-date |
| Schema contract | 17/17 cột; 8/8 CHECK; 2/2 unique index valid |
| Backend runtime | JPA `validate` pass; `/v3/api-docs=200`; admin unauthenticated `401` |
| Safety probe | constraint/unique probes pass trong transaction rollback; resolution/job rows `0/0` |
| Regression | focused `63/63`; full `617/617`, `1` unrelated conditional skip |
| Source/migration changes | Không có |
| Runtime cleanup | Backend dừng; container PostgreSQL disposable dừng nhưng giữ lại để review |

Approval G0-12C chỉ đóng migration và Backend runtime trên PostgreSQL disposable. Nó không xác nhận tương thích
với dữ liệu production, không publish n8n/provider, không xác nhận Admin UI/E2E và không cấp production authority.

## G0-12D record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Published n8n/provider runtime, HMAC và replay protection |
| Trạng thái | `COMPLETED_RUNTIME_APPROVED` |
| Approval token đã nhận | `APPROVE_G0-12D` |
| Ngày phê duyệt | `2026-07-29` |
| Detailed Design | `../09-sprints/g0-12d-n8n-provider-runtime-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T09-53-00-590+07-00/` |
| Source/test commit | `7168af7`; không chứa documents, `.env` hoặc secret |
| Production authority | `NOT_AUTHORIZED` |

## G0-12E record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Admin UI E2E, A0 warning và target no-mutation |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Approval token đã nhận | `APPROVE_G0-12E` |
| Ngày phê duyệt | `2026-07-29` |
| Detailed Design | `../09-sprints/g0-12e-admin-ui-e2e-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T11-10-13-870+07-00/` |
| Source/test commit | `9c155ff`; không chứa documents/evidence |
| Effective acceptance | `30/30 PASS`; BLOG/COMMENT/USER/CAFE_PAGE không mutation |
| Production authority | `NOT_AUTHORIZED` |

## G0-12F record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Detailed Design as-built tiếng Việt và traceability G0-12A–E |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Approval token đã nhận | `APPROVE_G0-12F` |
| Ngày phê duyệt | `2026-07-29` |
| Detailed Design | `../09-sprints/g0-12-implementation-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T11-56-02-316+07-00/` |
| Source/runtime changes | Không có |
| Production authority | `NOT_AUTHORIZED` |

Approval G0-12F đóng gate tài liệu as-built. Nó không tự đóng Sprint 1 và không cấp production authority.
Bước kế tiếp đã thực hiện là `IMPLEMENT_G0_12_DONE_AUDIT`.

## G0-12-DONE Audit record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Audit Definition of Done Sprint 1 |
| Trạng thái | `AUDITED_REMEDIATION_REQUIRED` |
| Audit token đã nhận | `IMPLEMENT_G0_12_DONE_AUDIT` |
| Approval token đã nhận | `APPROVE_G0-12-DONE-AUDIT` |
| Audit result | `15 PASS/PASS_WITH_SCOPE`, `1 PARTIAL`, `5 BLOCKED` |
| Production readiness | `9/14 PASS`, `5/14 OPEN`; `NOT_AUTHORIZED` |
| Deliverable | `../09-sprints/g0-12-done-audit.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T12-16-06-357+07-00/` |
| G0-12-DONE | `NOT_ACHIEVED` |

`APPROVE_G0-12-DONE-AUDIT` chỉ phê duyệt kết quả audit và danh sách remediation, không đóng Sprint 1.
Bước `IMPLEMENT_G0_12_DONE_FIX_01` đã được thực hiện và kiểm chứng:

- ADV-001–ADV-012: `12/12 PASS`;
- focused Backend: `60/60 PASS`;
- full Backend: `621`, fail/error `0`;
- trạng thái: `COMPLETED_VERIFIED_APPROVED`.

Approval `APPROVE_G0-12-DONE-FIX-01` đã được nhận. Approval này chỉ chốt
`DOD-FIX-01` và không đóng `G0-12-DONE`.

## DOD-FIX-02 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Changed-snapshot stale non-reuse cho cache và provider window |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_G0_12_DONE_FIX_02` |
| Approval token đã nhận | `APPROVE_G0-12-DONE-FIX-02` |
| Contract/safety | `CT-010`, `SAF-010` |
| Backend service tests | `25/25 PASS` |
| Admin Report AI focused | `63/63 PASS` |
| Full Backend | `624`, fail/error `0`, skipped `1` |
| Coverage | `AdminReportAiResolutionServiceImpl`: line `100%`, branch `87.76%` |
| Detailed Design | `../09-sprints/g0-12-done-fix-02-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T13-47-28-176+07-00/` |
| Production authority | `NOT_AUTHORIZED` |

DOD-FIX-02 đã được chốt bằng `APPROVE_G0-12-DONE-FIX-02`. Approval này chỉ chốt
freshness/idempotency remediation của FIX-02, không tự triển khai DOD-FIX-03 và không đóng
`G0-12-DONE`.

Token kế tiếp sau khi chốt FIX-02 là `IMPLEMENT_G0_12_DONE_FIX_03`; token này đã được nhận và
record triển khai nằm ngay bên dưới.

## DOD-FIX-03 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | COMMENT missing-critical-context full-path E2E fixture |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_G0_12_DONE_FIX_03` |
| Approval token đã nhận | `APPROVE_G0-12-DONE-FIX-03` |
| Traceability | `DOD-16`, `E2E-S1-04`, `SEM-009` |
| Backend service/semantic | `37/37 PASS` |
| Admin Report AI focused | `63/63 PASS` |
| Full Backend | `624`, fail/error `0`, skipped `1` |
| Full-path E2E | `1/1 PASS` |
| Side effects | target mutation `false`; auto job `0`; cleanup `0/0/0` |
| Coverage | Resolution service line `100%`, branch `87.75%`; validator line `100%`, branch `96.72%` |
| Detailed Design | `../09-sprints/g0-12-done-fix-03-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T14-22-18-183+07-00/` |
| Production authority | `NOT_AUTHORIZED` |

Gate đã được chốt bằng `APPROVE_G0-12-DONE-FIX-03`. Approval này chỉ chốt FIX-03, không tự triển
khai DOD-FIX-04–06 và không đóng `G0-12-DONE`.

Bước kế tiếp: `IMPLEMENT_G0_12_DONE_FIX_04`.

## DOD-FIX-04 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Provider/n8n unavailable structured error và recovery retry full-path |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_G0_12_DONE_FIX_04` |
| Approval token đã nhận | `APPROVE_G0-12-DONE-FIX-04` |
| Traceability | `E2E-S1-13`, provider error contract |
| Backend focused | `64/64 PASS` |
| Full Backend | `625`, fail/error `0`, skipped `1` |
| Admin | typecheck/build `PASS` |
| Full-path E2E | `1/1 PASS` |
| Failure safety | resolution `0 -> 0`; structured HTTP `502` |
| Recovery safety | HTTP `200`; report/target mutation `false`; auto job `0` |
| Coverage | 4 changed BE class line `100%`; service branch `87.75%` |
| Detailed Design | `../09-sprints/g0-12-done-fix-04-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T14-53-04-516+07-00/` |
| Production authority | `NOT_AUTHORIZED` |

FIX-04 đã được chốt bằng `APPROVE_G0-12-DONE-FIX-04`. Approval này chỉ đóng FIX-04,
không tự triển khai DOD-FIX-05/06 và không cấp production authority.

Bước kế tiếp: `IMPLEMENT_G0_12_DONE_FIX_05`.

## DOD-FIX-05 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Bulk partial failure giữ success và không AI mutation |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_G0_12_DONE_FIX_05` |
| Approval token đã nhận | `APPROVE_G0-12-DONE-FIX-05` |
| Traceability | `DOD-18`, `SAF-011`, `E2E-S1-09` |
| Full-path E2E | `1/1 PASS`; per-item HTTP `200/409` |
| Side effects | target/report AI mutation `false`; auto job `0`; cleanup `0/0` |
| Admin | typecheck/build `PASS` |
| Full Backend | `625`, fail/error `0`, skipped `1` |
| Production source | Không đổi; audit gap là executable evidence |
| Detailed Design | `../09-sprints/g0-12-done-fix-05-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T15-20-53-107+07-00/` |
| Production authority | `NOT_AUTHORIZED` |

## DOD-FIX-06 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Terminal report FE disable và Backend bypass rejection |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_G0_12_DONE_FIX_06` |
| Approval token đã nhận | `APPROVE_G0-12-DONE-FIX-06` |
| Traceability | `DOD-19`, `SAF-012`, `E2E-S1-10` |
| Full-path E2E | `1/1 PASS`; FE request `0`; Backend bypass HTTP `409` |
| Side effects | resolution `0 -> 0`; report/target mutation `false`; auto job `0`; cleanup `0/0` |
| Admin | typecheck/build `PASS` |
| Full Backend | `625`, fail/error `0`, skipped `1` |
| Production source | Không đổi; audit gap là executable FE assertion |
| Detailed Design | `../09-sprints/g0-12-done-fix-06-detailed-design.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T15-20-53-108+07-00/` |
| Production authority | `NOT_AUTHORIZED` |

Hai package đã được chốt độc lập bằng đúng approval token và đã được thêm vào approved gate ledger.
Approval này hoàn tất chuỗi remediation bắt buộc nhưng không tự đóng Sprint 1, không cập nhật lại
kết quả audit cũ và không cấp production authority.

Bước `IMPLEMENT_G0_12_DONE_REAUDIT` đã được thực hiện.

## G0-12-DONE Re-audit record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Re-audit Definition of Done sau DOD-FIX-01–06 |
| Trạng thái | `COMPLETED_REAUDIT_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_G0_12_DONE_REAUDIT` |
| Approval token đã nhận | `APPROVE_G0-12-DONE` |
| Remediation | `6/6 COMPLETED_VERIFIED_APPROVED` |
| Sprint 1 DoD | `21/21 PASS/PASS_WITH_SCOPE`; partial `0`; blocked `0` |
| Current Backend regression | `625`, fail/error `0`, skipped `1` |
| Current Admin | typecheck/build `PASS` |
| Current adversarial | ADV `12/12 PASS`; provider không gọi |
| Source traceability | 15 file SHA-256 fingerprint |
| Production readiness | `9/14`; `NOT_READY` |
| Deliverable | `../09-sprints/g0-12-done-reaudit.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T16-32-07-654+07-00/` |
| Production authority | `NOT_AUTHORIZED` |

`APPROVE_G0-12-DONE` đã đóng technical Sprint 1 sau khi re-audit đạt
`21/21 PASS/PASS_WITH_SCOPE`. Approval này không cấp production authority, không khởi động
Sprint 2 và không thay đổi trạng thái production readiness `9/14 NOT_READY`.

## S2-REBASE-01 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Rebase Sprint 2 từ source và evidence Sprint 1 |
| Trạng thái | `COMPLETED_REBASE_APPROVED` |
| Implementation token đã nhận | `IMPLEMENT_S2_REBASE_01` |
| Approval token đã nhận | `APPROVE_S2_REBASE_01` |
| Verified gap | `9` |
| Core | `S2-DD-01`, `S2-01`–`S2-04`, `S2-DONE-AUDIT` |
| Conditional | `S2-05` provider/model + cost/latency benchmark |
| Deferred | authoritative adapters, numeric calibration, deep target/media capability |
| Deliverable | `../09-sprints/s2-rebase-01.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T20-19-18-018+07-00/` |
| Source/runtime change | `NONE` |
| Production authority | `NOT_AUTHORIZED` |

Approval rebase đã chốt phạm vi và sequencing, không cấp quyền sửa source. Token
`IMPLEMENT_S2_DD_01` đã được nhận để viết Detailed Design trước code.

## S2-DD-01 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Detailed Design Runtime Rule–Evidence–Prompt–Evaluation |
| Trạng thái | `COMPLETED_DESIGN_APPROVED` |
| Prerequisite approval | `APPROVE_S2_REBASE_01` |
| Implementation token đã nhận | `IMPLEMENT_S2_DD_01` |
| Approval token đã nhận | `APPROVE_S2_DD_01` |
| Design decisions | `14` |
| Test design cases | `18` |
| Critical/high/medium findings | `1/4/1` |
| Deliverable | `../09-sprints/s2-dd-01-runtime-rule-evidence-prompt-evaluation.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T20-34-57-762+07-00/` |
| Source/runtime/database change | `NONE` |
| Provider called | `NO` |
| Production authority | `NOT_AUTHORIZED` |

Approval DD đã chốt thiết kế và các proposed bounds. Token `IMPLEMENT_S2_01` đã được nhận để triển
khai riêng Runtime Rule Context/Evidence Projection; không mở các package S2-02 trở đi.

## S2-01 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Runtime Rule Context và M07-compatible Evidence Projection |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Prerequisite approval | `APPROVE_S2_DD_01` |
| Implementation token đã nhận | `IMPLEMENT_S2_01` |
| Approval token đã nhận | `APPROVE_S2_01` |
| Runtime context | typed DTO; `RRC-1.0.0-rc.1` |
| Evidence envelope | typed DTO; `1.0.0-rc.1` |
| Lifecycle | `PROPOSED_EVALUATION_ONLY`; fail-closed |
| Provider projection | allowlist; whole request bị loại |
| Focused/full Backend | `40/40`; `628`, fail/error `0`, skip `1` |
| n8n/M07 verification | ADV `12/12`; guards `3/3`; fixture `18/18`; cross-review `16/16` |
| Coverage | line `100%`; branch thấp nhất `88.84%` |
| Evidence | `../09-sprints/evidence/2026-07-29T21-05-20-543+07-00/` |
| Runtime publish/provider call | `NO` |
| DB/FE change trong S2-01 | `NONE` |
| Production authority | `NOT_AUTHORIZED` |

`S2-01` đã được khóa sau review bằng `APPROVE_S2_01`. Không triển khai `S2-02` trước khi
nhận đúng token `IMPLEMENT_S2_02`; approval này không activate policy, publish runtime hoặc cấp
production authority.

## S2-02 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Bounded strict schema và Backend semantic invariant hardening |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Prerequisite | `APPROVE_S2_01` |
| Implementation token đã nhận | `IMPLEMENT_S2_02` |
| Approval token đã nhận | `APPROVE_S2_02` |
| Canonical schema | Runtime request + provider output, Draft 2020-12 strict |
| Semantic burden | version, evidence, counter, scope, aggregation, action, ceiling |
| Focused/full Backend | `50/50`; `638`, fail/error `0`, skip `1` |
| Coverage | Semantic validator line `100%`, branch `88.49%`; catalog `100%/100%` |
| n8n/schema | boundary `7/7`; nested boundary `PASS`; provider parity `PASS` |
| Safety regression | ADV `12/12`; guards `3/3`; M07 `18/18`; cross-review `16/16` |
| Issue | `5/5 FIXED_VERIFIED` |
| Evidence | `../09-sprints/evidence/2026-07-29T21-36-56-340+07-00/` |
| Runtime publish/provider call | `NO` |
| FE/DB change trong S2-02 | `NONE` |
| Production authority | `NOT_AUTHORIZED` |

`APPROVE_S2_02` đã khóa package sau review và được thêm vào approved gate ledger. Approval này không
activate policy, publish runtime hoặc cấp production authority. Package kế tiếp chưa mở; chỉ được triển khai
khi nhận đúng token `IMPLEMENT_S2_03`.

## S2-03 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Versioned Evaluation Dataset/Harness V1 |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Prerequisite | `APPROVE_S2_02` |
| Implementation token đã nhận | `IMPLEMENT_S2_03` |
| Approval token đã nhận | `APPROVE_S2_03` |
| Dataset | `1.0.0-rc.1`; `26` record; `9` evidence bundle |
| Oracle classes | hard safety `20`; provisional semantic `4`; open disagreement `2` |
| Hard safety | `100%`; dataset negative guards `6/6 PASS` |
| External hard gates | schema `7/7`; ADV `12/12`; M07 `18/18`; cross `16/16`; Backend `50/50` |
| Full Backend | `638`, fail/error `0`, skipped `1` |
| Provider quality | `NOT_EVALUATED`; denominator `0`; provider call `NO` |
| Issue | `5/5 RESOLVED_VERIFIED`; production code bug `0` |
| Deliverable | `../10-verification/evaluation-dataset-design.md` |
| Evidence | `../09-sprints/evidence/2026-07-29T22-21-14-291+07-00/` |
| Production source/FE/DB/runtime | `NONE` |
| Production authority | `NOT_AUTHORIZED` |

`APPROVE_S2_03` đã khóa dataset/harness sau review và được thêm vào approved
gate ledger. Approval này không gọi provider, activate policy, publish n8n
runtime hoặc cấp production authority. Tại thời điểm khóa S2-03, token kế tiếp
là `IMPLEMENT_S2_04`.

## S2-04 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Rule-family Prompt Pilot |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Prerequisite | `APPROVE_S2_03` |
| Implementation token đã nhận | `IMPLEMENT_S2_04` |
| Approval token đã nhận | `APPROVE_S2_04` |
| Candidate | `report-ai-v2-sprint2.4-candidate.1`; `PROPOSED`; authority `false` |
| Pilot | `CSR.HAR.001`; BLOG direct + COMMENT context-dependent |
| Focused gate | schema `1/1`; branch `2/2`; mutation `4/4`; negative `5/5 + 6/6` |
| Regression | S2-03 hard safety `100%`; Backend `638`, fail/error `0`, skip `1` |
| Open issue | `REL.001` runtime-policy semantic mismatch; excluded/routed |
| Deliverable | `../10-verification/prompt-pilot-design.md` |
| Evidence | `../09-sprints/evidence/2026-07-30T15-16-05-395+07-00/` |
| Provider/runtime/production authority | `NO` |

`APPROVE_S2_04` đã khóa candidate artifacts sau review. Nó không tự gọi
provider, publish/import n8n, activate policy, sửa `REL.001` hoặc cấp production
authority. S2-05 đã đủ prerequisite nhưng vẫn conditional; bước tiếp theo cần
chọn `IMPLEMENT_S2_05` hoặc `IMPLEMENT_S2_DONE_AUDIT`.

## S2-05 record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Conditional provider/model benchmark |
| Trạng thái | `PARTIAL_HARNESS_FIXED_PROVIDER_RETEST_REQUIRED` |
| Prerequisite | `APPROVE_S2_03`, `APPROVE_S2_04` |
| Implementation token đã nhận | `IMPLEMENT_S2_05` |
| Approval token | `NONE` |
| Matrix | `3` model × `6` case × `2` repeat = `36` |
| Budget | projected `0.918/1 USD`; actual recorded `0 USD` |
| Execute lần 1 | `36/36 invalid_json_schema`; inference success `0` |
| Local fix | provider schema adapter + circuit breaker + evidence path |
| Focused coverage | library line `100%`, branch `91.36%` |
| Regression | S2-04 `PASS`; S2-03 full hard gate `PASS` |
| Quality authority | denominator `0`; model selection `NO` |
| Deliverable | `../10-verification/provider-model-benchmark-design.md` |
| Evidence | `../09-sprints/evidence/2026-07-30T15-39-00-343+07-00/` |
| Runtime/production authority | `NO` |

S2-05 chưa được approve vì chưa có successful provider inference. Giới hạn 36
external request của Bound đã dùng hết nên provider-compatible fix chỉ mới
static-verify. Trước S2-DONE-AUDIT, retest có thể được mở bằng
`IMPLEMENT_S2_05_RETEST_01`; audit sau đó đã chọn defer package này khỏi Sprint 2
core. Việc defer không activate model/prompt/policy và không cấp production authority.

## S2-DONE-AUDIT record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Audit Definition of Done Sprint 2 |
| Trạng thái | `PARTIAL_AUDIT_FINDINGS_APPROVED_REMEDIATION_REQUIRED` |
| Implementation token đã nhận | `IMPLEMENT_S2_DONE_AUDIT` |
| Approval token | `APPROVE_S2_DONE_AUDIT` |
| S2-05 disposition | `DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY` |
| DoD matrix | `10 PASS`; `1 PASS_WITH_SCOPE`; `3 PARTIAL`; `0 BLOCKED` |
| Backend focused/full | `50/50`; `638`, fail/error `0`, skip `1` |
| Hard safety | `100%` |
| Finding 1 | S2-01 evidence thiếu `summary.json` |
| Finding 2 | Service line coverage `98.77%`, thấp hơn gate `100%` |
| Finding 3 | Security harness fixture lệch canonical strict request |
| Deliverable | `../09-sprints/s2-done-audit.vi.md` |
| Evidence | `../09-sprints/evidence/2026-07-30T16-09-39-334+07-00/` |
| Production readiness | `9/14 NOT_READY` |
| Production authority | `NOT_AUTHORIZED` |

`IMPLEMENT_S2_DONE_AUDIT` chốt việc defer S2-05 khỏi core để audit, nhưng không
approve S2-05, không biến `0/36` successful inference thành quality evidence và
không cấp model-selection authority. Sprint 2 chưa được đóng vì còn ba finding
bắt buộc. `APPROVE_S2_DONE_AUDIT` đã xác nhận kết quả audit và thứ tự
remediation; token này không xác nhận ba finding đã được sửa. Package sửa hẹp
tiếp theo được phép mở bằng `IMPLEMENT_S2_DONE_FIX_01`.

## S2-DONE final approval record

| Thuộc tính | Giá trị |
|---|---|
| Nội dung | Đóng Sprint 2 core sau remediation và Lean Functional Gate |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Final approval | `APPROVE_S2_DONE` |
| Fix 01 | `COMPLETED_VERIFIED` |
| Fix 02 | `COMPLETED_VERIFIED` |
| Flyway | `20260730.01`; `target_snapshot_hash VARCHAR(71)` |
| Backend focused/full | `54/54`; `642`, fail/error `0`, skip `1` |
| Security | HMAC/freshness/replay/tamper/signed-response `PASS` |
| Admin E2E | `E2E-S1-13 PASS`; `E2E-S1-04 PASS` |
| Safety | no report/target mutation; auto-apply job `0`; cleanup `PASS` |
| Final evidence | `../09-sprints/evidence/2026-07-30T17-01-03-753+07-00/` |
| S2-05 | `DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY` |
| Production authority | `NOT_AUTHORIZED` |

Approval này đóng core S2 theo Lean Roadmap. Nó không approve S2-05, không chọn
provider/model, không activate policy/prompt candidate, không mở Sprint 3/4 và
không cấp quyền migrate/deploy production.
