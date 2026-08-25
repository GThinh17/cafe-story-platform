# Master Roadmap Checklist — Resolve Report with AI V2

> File này được giữ làm lịch sử chi tiết. Roadmap thực thi hiện hành đã được rút
> gọn tại `CURRENT-LEAN-ROADMAP.md`; các Sprint 3–4 và benchmark mở rộng không
> còn chặn việc đóng chức năng core.

## 1. Mục đích và quy tắc sử dụng

Đây là **nguồn theo dõi tiến độ chính** cho toàn bộ chương trình Resolve Report with AI V2, từ
10 giai đoạn xây dựng hồ sơ đến 4 Sprint triển khai.

Quy ước:

- `[x]`: đã có deliverable, đã được review/approve hoặc đã có evidence kiểm chứng tương ứng.
- `[ ] ... PARTIAL`: đã làm một phần nhưng chưa đạt Definition of Done.
- `[ ] ... BLOCKED`: chưa thể hoàn thành vì thiếu môi trường, dữ liệu hoặc quyết định.
- `[ ] ... NOT_STARTED`: chưa bắt đầu.
- Sau mỗi approval hoặc lượt triển khai, phải cập nhật file này trước khi chuyển mục tiếp theo.
- Không dùng số lượng file hoặc code đã viết để tự suy ra `DONE`.

## 2. Trạng thái hiện tại

| Thuộc tính | Giá trị |
|---|---|
| Ngày cập nhật | `2026-07-30` |
| Nhánh | `n8n/Ai-agent/fix-bug-report-admin` |
| Implementation commit | `c4bb2a7` |
| G0-12A source commit | `bcc7932`; documents không nằm trong commit |
| G0-12B source/test commit | `f608528`; documents không nằm trong commit |
| G0-12D source/test commit | `7168af7`; documents, `.env` và secret không nằm trong commit |
| G0-12E source/test commit | `9c155ff`; documents và evidence không nằm trong commit |
| Giai đoạn hồ sơ | `10/10 hoàn thành` |
| Sprint hiện tại | `Sprint 2 — COMPLETED_VERIFIED_APPROVED` |
| Gate đang xử lý | Không còn gate bắt buộc của core |
| Mục vừa hoàn thành | `APPROVE_S2_DONE` sau Lean Functional Gate pass |
| Mục đang chờ review | Không có |
| Hành động kế tiếp | Không có; Sprint 3/4 chỉ mở khi user yêu cầu |
| Production deployment | `CHƯA ĐƯỢC PHÉP` |

## 3. Roadmap 10 giai đoạn

- [x] **Giai đoạn 1 — Tạo toàn bộ cây hồ sơ**
  - Deliverable: `documents/report-admin/resolve-report-ai-v2/`.
  - Gate liên quan: `G0-00`, `G0-01`, `DOSSIER_TREE`.

- [x] **Giai đoạn 2 — Project charter, scope và glossary**
  - Đã chốt mục tiêu, phạm vi, ngoài phạm vi và thuật ngữ chuẩn.
  - Gate liên quan: `G0-03`, `G0-04`, `G0-04-DECISIONS`.

- [x] **Giai đoạn 3 — Bộ cơ sở lý thuyết**
  - Đã tách Claim, Fact, Evidence, Observation, Inference, Finding và Recommendation.
  - Gate liên quan: `G0-05A`–`G0-05H`.

- [x] **Giai đoạn 4 — Khảo sát source/database/report reasons/policy hiện có**
  - Đã có current-state audit và gap register.
  - Gate liên quan: `G0-06A`–`G0-06D`.

- [x] **Giai đoạn 5 — Policy Framework bản PROPOSED**
  - Đã có policy framework, burden và decision framework.
  - Gate liên quan: `G0-07`.

- [x] **Giai đoạn 6 — Rule Catalog bản đầu**
  - Đã có rule families, intake mapping và insufficient-evidence rules.
  - Gate liên quan: `G0-08`.

- [x] **Giai đoạn 7 — Traceability Matrix**
  - Đã nối reason → rule → evidence → decision/action → test/design.
  - Gate liên quan: `G0-09`.

- [x] **Giai đoạn 8 — Business Decision Review**
  - Đã chốt `12/12` decision packages.
  - Gate liên quan: `G0-10`, `G0-10A`–`G0-10L`.

- [x] **Giai đoạn 9 — Sprint 1 Detailed Design**
  - Đã thiết kế FE → BE → n8n → BE → FE, A0 recommendation-only.
  - Gate liên quan: `G0-11`.

- [x] **Giai đoạn 10 — Triển khai và kiểm chứng source**
  - [x] Có approval bắt đầu implementation: `APPROVE_G0-12`.
  - [x] Có implementation commit: `c4bb2a7`.
  - [x] Baseline focused BE tests `27/27 PASS`; G0-12A focused regression `45/45 PASS`.
  - [x] Admin typecheck/build `PASS`.
  - [x] n8n export static validation `PASS`.
  - [x] Evidence metadata contract đã hoàn thành và approved.
  - [x] HMAC/freshness/replay đã implement, source/static verify và được chốt bằng `APPROVE_G0-12A`.
  - [x] Changed-file coverage gate đã đạt ở source/test và full regression; đã chốt bằng `APPROVE_G0-12B`.
  - [x] PostgreSQL/Flyway và Backend runtime đã kiểm chứng trên DB disposable; đã chốt bằng `APPROVE_G0-12C`.
  - [x] BE → n8n → provider runtime và signed response đã kiểm chứng; G0-12D đã approved.
  - [x] Admin UI E2E bằng account test đã kiểm chứng; `29/30` automated + `1/1` supplemental legacy cancel.
  - [x] Target snapshot BLOG/COMMENT/USER/CAFE_PAGE trước/sau AI đều không đổi.
  - [x] Detailed Design as-built tiếng Việt đã tái tạo theo Backend → n8n/OpenAI → Backend → Admin UI.
  - [x] G0-12E đã chốt bằng `APPROVE_G0-12E`.
  - [x] G0-12F đã chốt bằng `APPROVE_G0-12F`.
  - [x] Đã chạy audit `G0-12-DONE`; phát hiện 6 remediation package bắt buộc.
  - [x] Sprint 1 đạt technical Definition of Done: re-audit `21/21 PASS/PASS_WITH_SCOPE`
    và được chốt bằng `APPROVE_G0-12-DONE`.

## 4. Pre-Sprint Gate 0

### 4.1. Gate nền tảng

- [x] `G0-00` — Khởi tạo Pre-Sprint Gate 0.
- [x] `G0-01` — Xác lập mục tiêu và thứ tự hồ sơ.
- [x] `G0-02` — Bound phạm vi và nguyên tắc triển khai.
- [x] `G0-03` — Project charter.
- [x] `G0-04` — Scope và glossary.
- [x] `G0-04-DECISIONS` — Chốt semantics quan trọng.
- [x] `G0-05A`–`G0-05H` — Cơ sở lý thuyết.
- [x] `G0-06A`–`G0-06D` — Current-state audit.
- [x] `G0-07` — Policy framework.
- [x] `G0-08` — Rule catalog.
- [x] `G0-09` — Traceability.
- [x] `G0-10` — Business decision review.
- [x] `G0-10A`–`G0-10L` — `12/12` decision packages.
- [x] `G0-11` — Sprint 1 Detailed Design.
- [x] `G0-12` — Cho phép bắt đầu implementation Sprint 1; không cho phép production deploy.

### 4.2. G0-12M — Evidence Metadata Contract

- [x] `G0-12M-00` — Chốt ranh giới
  `Claim → Target Metadata → Evidence → Observation → Finding → Recommendation`.
- [x] `G0-12M-01` — Chốt Common Evidence Envelope.
- [x] `G0-12M-02` — Chốt BLOG metadata schema.
- [x] `G0-12M-03` — Chốt COMMENT metadata schema.
- [x] `G0-12M-04` — Chốt USER/CAFE_PAGE minimal manual-only metadata.
- [x] `G0-12M-05` — Chốt Evidence Kind Catalog.
- [x] `G0-12M-06` — Chốt Rule-to-Evidence Requirement Matrix.
- [x] `G0-12M-07` — Chốt JSON Schema, fixtures và contract version.
- [x] `G0-12M-08` — Review/approve toàn bộ metadata contract trước khi sửa tiếp source.

### 4.3. Gate còn lại của Sprint 1

- [x] `G0-12A` — HMAC, timestamp freshness và nonce replay protection — `COMPLETED_SOURCE_STATIC_APPROVED`; runtime vẫn thuộc `G0-12D`.
- [x] `G0-12B` — `COMPLETED_COVERAGE_APPROVED`; 5/5 class đạt `100%` line và `>=85%` branch.
- [x] `G0-12C` — `COMPLETED_RUNTIME_APPROVED`; migration, JPA validate, restart và API smoke đã pass trên PostgreSQL disposable.
- [x] `G0-12D` — `COMPLETED_RUNTIME_APPROVED`: provider/HMAC/publish/exact webhook/security matrix pass; approval `APPROVE_G0-12D`.
- [x] `G0-12E` — `COMPLETED_VERIFIED_APPROVED`; source commit `9c155ff`, approval `APPROVE_G0-12E`.
- [x] `G0-12F` — `COMPLETED_VERIFIED_APPROVED`; DD tiếng Việt đã tái tạo theo BE → n8n/OpenAI → BE → FE và evidence thật.
- [x] `G0-12-DONE` — `COMPLETED_REAUDIT_APPROVED`; re-audit đạt
  21 scoped pass, 0 partial, 0 blocked; production readiness giữ `9/14 NOT_READY`;
  approval `APPROVE_G0-12-DONE`.
- [x] `G0-12-DONE-AUDIT` — kết quả audit đã chốt bằng `APPROVE_G0-12-DONE-AUDIT`.
- [x] `DOD-FIX-01` — `COMPLETED_VERIFIED_APPROVED`.
  - [x] Executable ADV-001–ADV-012: `12/12 PASS`.
  - [x] Guard prior AI/reporter-claim-only evidence ở n8n và Backend.
  - [x] Guard toàn bộ evidence-reference fields.
  - [x] Focused `60/60`, full Backend `621` tests, changed class line `100%`, branch `96.61%`.
  - [x] User approval: `APPROVE_G0-12-DONE-FIX-01`.
- [x] `DOD-FIX-02` — changed-snapshot stale non-reuse — `COMPLETED_VERIFIED_APPROVED`.
  - [x] Hai baseline tests chứng minh cache stale reuse và provider-window stale persistence.
  - [x] Backend refresh/rebuild trước cache return và sau provider response.
  - [x] Stale clamp về `NEEDS_MANUAL_REVIEW + NO_ACTION`; target biến mất trả `409`, không persist.
  - [x] Service `25/25`, focused `63/63`, full Backend `624`, fail/error `0`, skipped `1`.
  - [x] Changed class coverage line `100%`, branch `87.76%`.
  - [x] User approval: `APPROVE_G0-12-DONE-FIX-02`.
- [x] `DOD-FIX-03` — COMMENT missing-critical-context E2E fixture —
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Fixture COMMENT có parent BLOG context whitespace trên PostgreSQL disposable.
  - [x] Backend coi blank parent context là `UNUSABLE/MISSING` và đặt
    `criticalEvidenceMissing=true`.
  - [x] Semantic trust boundary khóa critical-missing thành
    `NEEDS_MANUAL_REVIEW + NO_ACTION + UNASSESSABLE`.
  - [x] Full-path E2E `E2E-S1-04`: `1/1 PASS`; UI hiển thị critical reason/sufficiency.
  - [x] Target không mutation; auto-apply job `0`; cleanup fixture còn `0/0/0`.
  - [x] Service/semantic `37/37`; focused `63/63`; full Backend `624`, fail/error `0`,
    skipped `1`; Admin typecheck/build `PASS`.
  - [x] Hai production class đạt line `100%`, branch lần lượt `87.75%` và `96.72%`.
  - [x] User approval: `APPROVE_G0-12-DONE-FIX-03`.
- [x] `DOD-FIX-04` — provider-unavailable full E2E —
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Baseline chứng minh Backend thiếu structured error contract và UI thiếu operational metadata.
  - [x] Backend trả `502 + AI_PROVIDER_BOUNDARY_FAILED + correlationId + retryable + stage`.
  - [x] Admin hiển thị error code, stage, support reference và retry state.
  - [x] Full-path `E2E-S1-13`: failure không persist; recovery retry thành công.
  - [x] Report/target không mutation; auto-apply job `0`; cleanup report/blog `0/0`.
  - [x] Focused `64/64`; full Backend `625`, fail/error `0`, skipped `1`.
  - [x] Admin typecheck/build `PASS`; changed BE line `100%`, service branch `87.75%`.
  - [x] User approval: `APPROVE_G0-12-DONE-FIX-04`.
- [x] `DOD-FIX-05` — bulk partial-failure/no-mutation E2E —
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Fault-inject một terminal item sau khi FE bulk selection hoàn thành.
  - [x] Per-item response HTTP `200/409`; success vẫn được giữ, failed item không persist.
  - [x] UI `Completed 1`, `Manual review 1`, `Failed 1`, `Remaining 0`, `Total 2`.
  - [x] Report/target không bị AI mutation; auto job `0`; cleanup report/BLOG `0/0`.
  - [x] Playwright `E2E-S1-09 1/1 PASS`; Admin typecheck/build `PASS`.
  - [x] Full Backend `625`, fail/error `0`, skipped `1`.
  - [x] User approval: `APPROVE_G0-12-DONE-FIX-05`.
- [x] `DOD-FIX-06` — terminal-report FE guard assertion —
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Report `RESOLVED` hiển thị Ask AI disabled.
  - [x] DOM click không phát sinh request; direct Backend bypass HTTP `409`.
  - [x] Resolution `0 -> 0`; report/target không mutation; auto job `0`.
  - [x] Playwright `E2E-S1-10 1/1 PASS`; Admin typecheck/build `PASS`.
  - [x] Full Backend `625`, fail/error `0`, skipped `1`; cleanup `0/0`.
  - [x] User approval: `APPROVE_G0-12-DONE-FIX-06`.
- [x] `G0-12-DONE-REAUDIT` — `COMPLETED_REAUDIT_APPROVED`.
  - [x] Sáu remediation package đều `COMPLETED_VERIFIED_APPROVED`.
  - [x] DOD matrix: `21/21 PASS/PASS_WITH_SCOPE`, partial `0`, blocked `0`.
  - [x] Backend current-worktree regression `625`, fail/error `0`, skipped `1`.
  - [x] Admin typecheck/build `PASS`; ADV `12/12 PASS`; n8n JSON parse `PASS`.
  - [x] Source fingerprint `15/15`; production readiness tách riêng `9/14 NOT_READY`.
  - [x] User approval: `APPROVE_G0-12-DONE`.

## 5. Roadmap 4 Sprint

### Sprint 1 — Safety Contract và recommendation-only baseline

Trạng thái: **COMPLETED_TECHNICAL_DOD_APPROVED**

- [x] A0 recommendation-only; không tạo auto-apply mới.
- [x] Legacy due jobs bị quarantine thành `SKIPPED`, không mutate target/report.
- [x] Contract V2, categorical semantics và stable Rule IDs.
- [x] USER/CAFE_PAGE local manual-only.
- [x] Minimum evidence-first Admin UI.
- [x] n8n V2 export ở trạng thái inactive.
- [x] Hoàn thành `G0-12M` evidence metadata contract.
- [x] Hoàn thành `G0-12A` security boundary — source/static pass và đã approved; runtime thuộc `G0-12D`.
- [x] Đạt coverage gate — source/test đã pass và được chốt bằng `APPROVE_G0-12B`.
- [x] Verify database migration/runtime — đã pass trên PostgreSQL disposable và được chốt bằng `APPROVE_G0-12C`.
- [x] Verify published n8n/provider runtime — canonical active, parity `5/5`, provider và signed response pass; đã approved.
- [x] Verify Admin UI bằng account test.
- [x] Chạy E2E representative scenarios và chứng minh no mutation.
- [x] Cập nhật Detailed Design as-built cuối cùng.
- [x] Chốt G0-12E bằng `APPROVE_G0-12E`.
- [x] Chốt G0-12F bằng `APPROVE_G0-12F`.
- [x] Chạy audit Definition of Done.
- [x] Khắc phục và approve `DOD-FIX-01`–`DOD-FIX-03`.
- [x] Khắc phục và approve `DOD-FIX-04`.
- [x] Chốt approval `DOD-FIX-05`–`DOD-FIX-06`; implementation/evidence đã verify.
- [x] Re-audit `G0-12-DONE` sau remediation: `21/21 PASS/PASS_WITH_SCOPE`.
- [x] Đóng technical Sprint 1 bằng `APPROVE_G0-12-DONE`; không cấp production authority.

### Sprint 2 — Prompt/schema/validator chuyên sâu

Trạng thái: **COMPLETED_VERIFIED_APPROVED**

- [x] Thực hiện source/evidence-backed rebase bằng `IMPLEMENT_S2_REBASE_01`.
- [x] Xác định 9 gap và route core/conditional/deferred.
- [x] Chốt thứ tự dependency đề xuất:
  `Rule Context → Schema/Validator → Dataset/Harness → Prompt Pilot → Benchmark`.
- [x] Tạo Detailed Rebase: `09-sprints/s2-rebase-01.vi.md`.
- [x] User approval: `APPROVE_S2_REBASE_01`.
- [x] `S2-DD-01` — `COMPLETED_DESIGN_APPROVED`.
  - [x] Source-backed as-is và five-layer to-be flow.
  - [x] Runtime Rule Context + M07-compatible Evidence Envelope.
  - [x] Policy lifecycle evaluation-only fail-closed.
  - [x] Bounded schema, semantic aggregation và safe provider projection.
  - [x] Evaluation/test/rollout/rollback design.
  - [x] User approval: `APPROVE_S2_DD_01`.
- [x] `S2-01` — Runtime Rule Context Contract: `COMPLETED_VERIFIED_APPROVED`.
  - [x] Typed Runtime Rule Context và candidate rule metadata.
  - [x] M07-compatible typed Common Evidence Envelope.
  - [x] Unique Rule/Evidence ID và snapshot-binding guards.
  - [x] `PROPOSED_EVALUATION_ONLY` fail-closed về manual/no-action.
  - [x] n8n provider projection theo allowlist; không gửi whole request.
  - [x] Focused Backend `40/40`; full Backend `628`, fail/error `0`, skip `1`.
  - [x] Changed production coverage: line `100%`, branch thấp nhất `88.84%`.
  - [x] ADV `12/12`, contract guards `3/3`, M07 fixture `18/18`, cross-review `16/16`.
  - [x] User approval: `APPROVE_S2_01`.
- [x] `S2-02` — bounded schema và semantic invariant hardening:
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Hai canonical Draft 2020-12 schema strict, bounded và reject unknown property.
  - [x] Embedded provider schema parity với canonical artifact.
  - [x] n8n runtime boundary kiểm tra exact keys cho outer và nested objects.
  - [x] Backend enforce ruleVersion, evidence/semantic burden, complete material scope,
    aggregation, action burden và evaluation ceiling.
  - [x] Invalid/missing/unsafe output clamp về `NEEDS_MANUAL_REVIEW + NO_ACTION`.
  - [x] Focused Backend `50/50`; full Backend `638`, fail/error `0`, skip `1`.
  - [x] Semantic validator coverage line `100%`, branch `88.49%`.
  - [x] Schema boundary `7/7`; n8n nested boundary `PASS`; parity `PASS`.
  - [x] ADV `12/12`, guards `3/3`, M07 fixture `18/18`, cross-review `16/16`.
  - [x] Năm issue trong scope đã fix và regression verify.
  - [x] User approval: `APPROVE_S2_02`.
- [x] `S2-03` — versioned evaluation dataset/harness:
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Dataset/schema/rubric/manifest version `1.0.0-rc.1`, lifecycle `PROPOSED`,
    runtime authority `false`.
  - [x] `26` record: `20` hard safety, `4` provisional semantic, `2` open disagreement.
  - [x] `9` evidence bundle; tách `collectionState`, `quality` và `evidenceSufficiency`.
  - [x] SHA-256 fingerprint cho ba artifact và năm dependency.
  - [x] Dataset self-test và negative guards `6/6 PASS`.
  - [x] Full hard gate: schema `7/7`, ADV `12/12`, M07 `18/18`,
    cross-review `16/16`, Backend focused `50/50`.
  - [x] Full Backend regression `638`, fail/error `0`, skip `1`.
  - [x] Hard safety `100%`; provider quality `NOT_EVALUATED`, denominator `0`,
    provider call `false`.
  - [x] Năm issue data/test/command/harness được ghi nhận, sửa hẹp và retest.
  - [x] User approval: `APPROVE_S2_03`.
- [x] `S2-04` — rule-family prompt pilot cho text-only BLOG/COMMENT:
  `COMPLETED_VERIFIED_APPROVED`.
  - [x] Candidate `report-ai-v2-sprint2.4-candidate.1`, lifecycle `PROPOSED`,
    runtime authority `false`.
  - [x] Chọn `CSR.HAR.001` cho BLOG direct và COMMENT context-dependent.
  - [x] Strict schema/spec, deterministic assembler, fixture và SHA-256 manifest.
  - [x] Untrusted report/target/evidence/prior-AI không đổi system prompt `4/4`.
  - [x] Assembler negative `5/5`; spec negative `6/6`; dataset ref `5/5`.
  - [x] Provider output field parity khóa canonical `evidenceIds`.
  - [x] S2-03 hard safety `100%`; Backend full `638`, fail/error `0`, skip `1`.
  - [x] `REL.001` semantic mismatch được highlight, loại khỏi pilot và route remediation;
    output-field mismatch đã sửa về canonical `evidenceIds`.
  - [x] User approval: `APPROVE_S2_04`.
- [ ] `S2-05` — provider/model + cost/latency benchmark:
  `PARTIAL_HARNESS_FIXED_PROVIDER_RETEST_REQUIRED`.
  - [x] Bound `3` model × `6` case × `2` repeat; maximum `36` request.
  - [x] Benchmark-only schema/config/runner/manifest/sanitization.
  - [x] Dry-run cost guard `0.918/1 USD`.
  - [x] Execute lần 1 dùng `36/36` request, cost `0 USD`.
  - [x] Highlight `invalid_json_schema` `36/36`; không gọi đây là model failure.
  - [x] Provider-compatible schema adapter đã static-verify.
  - [x] GLOBAL/MODEL/NONE circuit breaker đã coverage-test.
  - [x] Failed-run evidence được giữ riêng, không bị retest ghi đè.
  - [x] Focused library coverage line `100%`, branch `91.36%`.
  - [x] S2-04 regression và S2-03 full hard gate `PASS`.
  - [ ] Provider retest có successful inference — `BLOCKED_BY_NEW_EXTERNAL_CALL_TOKEN`.
  - [ ] Provider hard safety `100%`.
  - [ ] Stability/latency/cost trên successful runs.
  - [ ] Approval `APPROVE_S2_05`.
  - [x] Sprint 2 core disposition:
    `DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY`.
- [ ] External authoritative adapters và numeric calibration — `DEFERRED_OUT_OF_S2_CORE`.
- [x] `S2-DONE-AUDIT` — remediation hoàn tất và final functional re-audit pass.
  - [x] Approval/evidence S2-01–S2-04 traceability audit.
  - [x] S2-05 conditional defer decision được ghi minh bạch.
  - [x] Schema `7/7`, ADV `12/12`, prompt và dataset hard gate `PASS`.
  - [x] Backend focused `50/50`; full `638`, fail/error `0`, skip `1`.
  - [x] Hard safety deterministic slice `100%`.
  - [x] S2-01 required `summary.json` — reconstructed và JSON parse pass.
  - [x] ResolutionService safety branches — focused regression bổ sung; required behavior pass.
  - [x] Security harness fixture drift — canonical fixture và security harness pass.
  - [x] Review findings bằng `APPROVE_S2_DONE_AUDIT`.
  - [x] Triển khai `IMPLEMENT_S2_DONE_FIX_01`.
  - [x] Sửa DB hash length bằng `IMPLEMENT_S2_DONE_FIX_02_DB_HASH_LENGTH`.
  - [x] Flyway `20260730.01`, focused `54/54`, full `642`, security và hai E2E pass.
  - [x] Re-audit và `APPROVE_S2_DONE`.
- [x] Đóng Sprint 2 core; không cấp production deployment authority.

### Sprint 3 — Audit, security và Admin operations nâng cao

Trạng thái: **DEFERRED_REBASE_REQUIRED**

- [ ] Rebase phạm vi Sprint 3 sau Sprint 1.
- [ ] Dedicated audit explorer.
- [ ] Two-person manual action approval UI/API.
- [ ] Appeal/counter-notice workflow.
- [ ] Advanced incident/retention administration.
- [ ] Shared replay store cho multi-instance.
- [ ] Admin quality analytics.
- [ ] Đóng Sprint 3.

### Sprint 4 — Deep Target Evidence

Trạng thái: **DEFERRED_BY_K1**

- [ ] Approved target-specific evidence profiles.
- [ ] USER behavioral/temporal aggregation.
- [ ] CAFE_PAGE ownership/merchant/page-responsibility evidence.
- [ ] Cross-target coordinated-abuse evidence.
- [ ] Verified media/OCR pipeline.
- [ ] Legal/privacy evidence intake.
- [ ] Target-specific burden/evaluation datasets.
- [ ] Privacy/security review và action/quorum design.
- [ ] Đóng Sprint 4.

## 6. Definition of Done toàn chương trình

- [x] Tất cả mục bắt buộc của Sprint 1 đã pass.
- [x] Không còn AI-triggered mutation ngoài automation mode được business phê duyệt.
- [x] Evidence contract typed, versioned, validated và traceable.
- [x] Security boundary có positive/negative tests.
- [x] Runtime FE → BE → n8n/provider → BE → FE đã được kiểm chứng.
- [x] BLOG và COMMENT có representative fixtures.
- [x] USER và CAFE_PAGE giữ manual-only cho tới khi Sprint 4 được approve.
- [x] Tài liệu, source và runtime evidence không mâu thuẫn trong phạm vi re-audit Sprint 1.
- [ ] Các Sprint deferred được rebase hoặc đóng bằng quyết định business rõ ràng.

## 7. Nhật ký cập nhật

| Ngày | Thay đổi | Evidence/approval |
|---|---|---|
| 2026-07-23 | Hoàn thành dossier foundation đến G0-11 | `APPROVE_G0-00`–`APPROVE_G0-11` |
| 2026-07-24 | Cho phép và triển khai một phần Sprint 1 | `APPROVE_G0-12`, commit `c4bb2a7` |
| 2026-07-24 | Mở Evidence Metadata Contract và chốt terminology | `APPROVE_G0-12M-00` |
| 2026-07-24 | Soạn Common Evidence Envelope | `G0-12M-01 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt Common Evidence Envelope | `APPROVE_G0-12M-01` |
| 2026-07-26 | Soạn BLOG metadata schema từ source/schema thật | `G0-12M-02 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt BLOG metadata schema | `APPROVE_G0-12M-02` |
| 2026-07-26 | Soạn COMMENT metadata schema từ entity/migration/DBML | `G0-12M-03 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt COMMENT metadata schema và 17 decision | `APPROVE_G0-12M-03` |
| 2026-07-26 | Soạn USER/CAFE_PAGE minimal manual-only metadata từ source/schema thật | `G0-12M-04 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt USER/CAFE_PAGE minimal manual-only metadata và 18 decision | `APPROVE_G0-12M-04` |
| 2026-07-26 | Soạn Evidence Kind Catalog và normalize provisional slots M02–M04 | `G0-12M-05 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt Evidence Kind Catalog và 20 decision | `APPROVE_G0-12M-05` |
| 2026-07-26 | Soạn Rule-to-Evidence Requirement Matrix, cover 24 Rule ID | `G0-12M-06 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt Rule-to-Evidence Requirement Matrix và 24 decision | `APPROVE_G0-12M-06` |
| 2026-07-26 | Soạn executable JSON Schema, semantic validator và 16 fixtures | `G0-12M-07 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt executable contract M07 và 18 decision | `APPROVE_G0-12M-07` |
| 2026-07-26 | Cross-review M00–M07, fix 4 contract issues, regression 18/18 | `G0-12M-08 READY_FOR_REVIEW` |
| 2026-07-26 | Chốt G0-12M cross-review và 18 decision M08 | `APPROVE_G0-12M-08` |
| 2026-07-26 | Chốt G0-12A security boundary ở cấp source/static; không cấp runtime/production authority | `APPROVE_G0-12A`, commit `bcc7932` |
| 2026-07-26 | G0-12B đạt coverage gate 5/5 class; focused 56/56 và full regression 617 tests | `APPROVE_G0-12B`, commit `f608528` |
| 2026-07-26 | G0-12C apply migration V2, verify PostgreSQL/JPA/Backend restart/API smoke và constraint rollback probes | `APPROVE_G0-12C`; không đổi source |
| 2026-07-29 | G0-12D rotate/sync key local, cấu hình HMAC, fix replay runtime, publish canonical và verify provider/security matrix | `APPROVE_G0-12D`, commit `7168af7` |
| 2026-07-29 | G0-12E fix cached A0 warning, bổ sung target no-mutation snapshot, verify Admin UI/E2E và legacy cancel | `APPROVE_G0-12E`, commit `9c155ff` |
| 2026-07-29 | G0-12F tái tạo Detailed Design as-built tiếng Việt và traceability G0-12A–E | `APPROVE_G0-12F`; không sửa source |
| 2026-07-29 | Audit G0-12-DONE; phân biệt RAI 30-scenario với full CT/SAF/ADV/E2E matrix | `PARTIAL`; 6 remediation package, production `9/14` |
| 2026-07-29 | Chốt kết quả audit và thứ tự remediation | `APPROVE_G0-12-DONE-AUDIT`; G0-12-DONE vẫn chưa đạt |
| 2026-07-29 | DOD-FIX-01 executable ADV-001–012, fix evidence-basis/reference guards và verify regression | `IMPLEMENTED_VERIFIED_AWAITING_APPROVAL`; `12/12 ADV PASS` |
| 2026-07-29 | Chốt DOD-FIX-01 sau review | `APPROVE_G0-12-DONE-FIX-01`; bước kế tiếp `IMPLEMENT_G0_12_DONE_FIX_02` |
| 2026-07-29 | DOD-FIX-02 chặn reuse/persist output của stale snapshot và verify CT-010/SAF-010 | `COMPLETED_VERIFIED_AWAITING_APPROVAL`; chờ `APPROVE_G0-12-DONE-FIX-02` |
| 2026-07-29 | Chốt DOD-FIX-02 sau review | `APPROVE_G0-12-DONE-FIX-02`; bước kế tiếp `IMPLEMENT_G0_12_DONE_FIX_03` |
| 2026-07-29 | DOD-FIX-03 COMMENT missing critical context | Full-path E2E `1/1 PASS`; chờ `APPROVE_G0-12-DONE-FIX-03` |
| 2026-07-29 | Chốt DOD-FIX-03 sau review | `APPROVE_G0-12-DONE-FIX-03`; bước kế tiếp `IMPLEMENT_G0_12_DONE_FIX_04` |
| 2026-07-29 | DOD-FIX-04 provider unavailable/recovery E2E và structured operational error | `COMPLETED_VERIFIED_AWAITING_APPROVAL`; `E2E-S1-13 1/1 PASS` |
| 2026-07-29 | Chốt DOD-FIX-04 sau review | `APPROVE_G0-12-DONE-FIX-04`; bước kế tiếp `IMPLEMENT_G0_12_DONE_FIX_05` |
| 2026-07-29 | DOD-FIX-05 bulk partial failure/no-mutation executable evidence | `COMPLETED_VERIFIED_AWAITING_APPROVAL`; `E2E-S1-09 1/1 PASS` |
| 2026-07-29 | DOD-FIX-06 terminal FE + Backend defense-in-depth evidence | `COMPLETED_VERIFIED_AWAITING_APPROVAL`; `E2E-S1-10 1/1 PASS` |
| 2026-07-29 | Chốt DOD-FIX-05 và DOD-FIX-06 sau review | `APPROVE_G0-12-DONE-FIX-05`, `APPROVE_G0-12-DONE-FIX-06`; bước kế tiếp `IMPLEMENT_G0_12_DONE_REAUDIT` |
| 2026-07-29 | Re-audit G0-12-DONE sau sáu remediation | `21/21 PASS/PASS_WITH_SCOPE`; production `9/14 NOT_READY`; chờ `APPROVE_G0-12-DONE` |
| 2026-07-29 | Đóng technical Sprint 1 sau review re-audit | `APPROVE_G0-12-DONE`; production vẫn `9/14 NOT_READY`, không cấp deployment authority |
| 2026-07-29 | Rebase Sprint 2 từ source và evidence Sprint 1 | `IMPLEMENT_S2_REBASE_01`; 9 gap; chờ `APPROVE_S2_REBASE_01` |
| 2026-07-29 | Chốt phạm vi và sequencing Sprint 2 sau rebase | `APPROVE_S2_REBASE_01`; bắt đầu `IMPLEMENT_S2_DD_01` |
| 2026-07-29 | Hoàn thành Detailed Design Sprint 2 trước code | `IMPLEMENT_S2_DD_01`; 14 decision, 18 test case; chờ `APPROVE_S2_DD_01` |
| 2026-07-29 | Chốt Detailed Design và mở package code S2-01 | `APPROVE_S2_DD_01`; bắt đầu `IMPLEMENT_S2_01` |
| 2026-07-29 | Chốt S2-01 sau source, evidence và verification | `APPROVE_S2_01`; bước kế tiếp chờ `IMPLEMENT_S2_02` |
| 2026-07-29 | Chốt S2-02 sau bounded schema/semantic verification | `APPROVE_S2_02`; bước kế tiếp chờ `IMPLEMENT_S2_03` |
| 2026-07-29 | Hoàn thành S2-03 dataset/harness V1 | `26` case, hard safety `100%`, full Backend `638`; chờ `APPROVE_S2_03` |
| 2026-07-30 | Chốt S2-03 sau review | `APPROVE_S2_03`; bước kế tiếp chờ `IMPLEMENT_S2_04` |
| 2026-07-30 | Triển khai và verify S2-04 prompt pilot | HAR.001 direct/context; focused pass; S2-03 `100%`; chờ `APPROVE_S2_04` |
| 2026-07-30 | Chốt S2-04 sau review | `APPROVE_S2_04`; chờ chọn conditional S2-05 hoặc core S2-DONE-AUDIT |
| 2026-07-30 | Audit Definition of Done Sprint 2; defer S2-05 khỏi core nhưng không cấp quality authority | `10 PASS`, `1 PASS_WITH_SCOPE`, `3 PARTIAL`; chờ `APPROVE_S2_DONE_AUDIT` |
| 2026-07-30 | Chốt ba finding của S2-DONE-AUDIT; chưa đóng Sprint 2 | `APPROVE_S2_DONE_AUDIT`; bước tiếp theo `IMPLEMENT_S2_DONE_FIX_01` |
| 2026-07-30 | Hoàn tất remediation và phát hiện/sửa DB hash length `71 > 64` | Flyway `20260730.01`; focused `54/54`; full `642`; hai Admin E2E pass |
| 2026-07-30 | Đóng Sprint 2 core | `APPROVE_S2_DONE`; backlog S2-05/Sprint 3/4 không chặn; production vẫn `NOT_AUTHORIZED` |
