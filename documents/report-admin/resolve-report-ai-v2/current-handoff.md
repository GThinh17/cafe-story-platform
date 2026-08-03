# Current Handoff

## Roadmap hiện hành

- Chế độ: `LEAN_FUNCTIONAL_CLOSURE`.
- Roadmap: `CURRENT-LEAN-ROADMAP.md`.
- Sprint 2 core đã `COMPLETED_VERIFIED_APPROVED`.
- Final approval đã nhận: `APPROVE_S2_DONE`.
- Không còn gate bắt buộc của core.
- S2-05 mở rộng, Sprint 3 và Sprint 4 là backlog, không chặn đóng core.

## Cập nhật hiện hành — S2-DONE

- Token final đã nhận: `APPROVE_S2_DONE`.
- Trạng thái: `COMPLETED_VERIFIED_APPROVED`.
- S2-01–S2-04: `COMPLETED_VERIFIED_APPROVED`.
- S2-05 core disposition:
  `DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY`; không model selection.
- `S2-DONE-FIX-01`: audit findings đã remediate và verify.
- `S2-DONE-FIX-02`: migration `20260730.01`, entity/DB hash length `71`.
- Final gates: focused `54/54`, full Backend `642` fail/error `0`, skip `1`,
  security harness pass, `E2E-S1-13` và `E2E-S1-04` pass.
- Report/target không mutation; auto-apply job `0`; fixture cleanup `0`.
- Production readiness giữ `9/14 NOT_READY`, deployment `NOT_AUTHORIZED`.
- Final DD: `09-sprints/s2-done-fix-02-db-hash-length-detailed-design.vi.md`.
- Final evidence:
  `09-sprints/evidence/2026-07-30T17-01-03-753+07-00/`.
- Token kế tiếp: không có.
- Các dòng lịch sử bên dưới được giữ để trace.

## Cập nhật lịch sử — S2-DONE-AUDIT trước remediation

- Audit ban đầu: `10 PASS`, `1 PASS_WITH_SCOPE`, `3 PARTIAL`, `0 BLOCKED`.
- Findings được review bằng `APPROVE_S2_DONE_AUDIT`.
- Ba finding đã được xử lý trong `S2-DONE-FIX-01`.
- Persistence mismatch `targetSnapshotHash 71 > VARCHAR(64)` phát hiện bằng
  full-path E2E và được sửa trong `S2-DONE-FIX-02`.
- Evidence audit:
  `09-sprints/evidence/2026-07-30T16-09-39-334+07-00/`.

## Cập nhật lịch sử — S2-05 trước S2-DONE-AUDIT

- `S2-01`–`S2-04`: `COMPLETED_VERIFIED_APPROVED`.
- `S2-05`: `PARTIAL_HARNESS_FIXED_PROVIDER_RETEST_REQUIRED`.
- Token đã nhận: `IMPLEMENT_S2_05`; chưa có `APPROVE_S2_05`.
- Matrix: 3 model × 6 case × 2 repeat; Bound tối đa 36 provider request,
  projected `0.918/1 USD`.
- Local contract/runner/manifest/sanitization/stop policy đã tạo.
- Execute lần 1: `36/36 invalid_json_schema`, inference success `0`, actual
  recorded cost `0 USD`.
- Đây là provider schema-boundary failure, không phải bằng chứng model quality.
- Fix đã static-verify: Structured Outputs adapter, internal post-validation,
  GLOBAL/MODEL/NONE circuit breaker và retest output riêng.
- Focused gate `PASS`; benchmark library coverage line `100%`, branch `91.36%`.
- S2-04 regression `PASS`; S2-03 full hard gate `PASS`.
- Provider hard safety, stability, successful latency/cost chưa được kiểm chứng.
- Quality denominator vẫn `0`; không model nào được chọn.
- Không đổi Backend/FE/mobile/DB/canonical n8n workflow; không publish/activate.
- DD: `10-verification/provider-model-benchmark-design.md`.
- Evidence:
  `09-sprints/evidence/2026-07-30T15-39-00-343+07-00/`.
- Token kế tiếp tại thời điểm S2-05: `IMPLEMENT_S2_05_RETEST_01`; hiện đã được
  supersede cho Sprint 2 core bởi disposition defer trong S2-DONE-AUDIT.
- Không dùng `APPROVE_S2_05` trước successful provider retest.
- Các dòng lịch sử bên dưới được giữ để trace.

## Cập nhật hiện hành — S2-04

- `S2-01`, `S2-02`, `S2-03`: `COMPLETED_VERIFIED_APPROVED`.
- `S2-04`: `COMPLETED_VERIFIED_APPROVED`.
- Candidate `report-ai-v2-sprint2.4-candidate.1` vẫn `PROPOSED`,
  `runtimeAuthority=false`, `A0_RECOMMEND_ONLY`.
- Pilot chỉ chọn `CSR.HAR.001`: BLOG direct và COMMENT context-dependent có
  `PARENT_BLOG_CONTEXT`; thiếu evidence/context route manual/no-action.
- Đã tạo strict schema/spec, deterministic n8n Code candidate assembler, hai
  fixture, SHA-256 manifest và focused harness.
- Focused: schema `1/1`, branch `2/2`, untrusted mutation `4/4`, assembler
  negative `5/5`, spec negative `6/6`, dataset ref `5/5`.
- S2-03 regression: hard safety `100%`; schema `7/7`, ADV `12/12`, M07
  `18/18`, cross `16/16`, Backend focused `50/50`.
- Full Backend: `638`, failure/error `0`, skipped `1`.
- Highlight HIGH CODE_BUG: `REL.001` runtime semantic hiện không khớp policy;
  rule đã bị loại khỏi pilot và route remediation, không che bằng prompt.
- Output-field CODE_BUG đã sửa từ `supportingEvidenceIds` về canonical
  `evidenceIds` và có parity guard.
- Hai TEST_BUG trong harness đã sửa hẹp và retest pass.
- Không đổi production source, S2-03 artifacts, FE, DB hoặc canonical/published
  n8n runtime; provider không được gọi.
- DD: `10-verification/prompt-pilot-design.md`.
- Evidence:
  `09-sprints/evidence/2026-07-30T15-16-05-395+07-00/`.
- Approval đã nhận: `APPROVE_S2_04`.
- Bước kế tiếp cần người dùng chọn:
  - `IMPLEMENT_S2_05`: benchmark provider/model có kiểm soát; conditional và có
    external call/cost;
  - `IMPLEMENT_S2_DONE_AUDIT`: bỏ/defer S2-05 và audit core để đóng Sprint 2.
- Các dòng lịch sử bên dưới được giữ để trace.

## Lịch sử gần nhất — S2-03

- `S2-03`: `COMPLETED_VERIFIED_APPROVED`.
- Dataset `1.0.0-rc.1`: `26` record, `9` evidence bundle; hard safety `100%`.
- Full hard gate và Backend `638` pass; provider không được gọi.
- Evidence: `09-sprints/evidence/2026-07-29T22-21-14-291+07-00/`.
- Approval đã nhận: `APPROVE_S2_03`.
- Các dòng lịch sử bên dưới được giữ để trace.

## Lịch sử gần nhất — S2-02

- `S2-01`: `COMPLETED_VERIFIED_APPROVED`.
- `S2-02`: `COMPLETED_VERIFIED_APPROVED`.
- Đã tạo hai canonical strict JSON Schema cho runtime request và provider output; unknown property
  bị reject, cardinality/text/code đều có bound.
- Backend semantic validator enforce rule version, required Evidence Kind, semantic missing requirement,
  independent evidence, counter-evidence, complete material scope, aggregation, action burden và ceiling.
- n8n request boundary kiểm tra exact keys cả outer lẫn nested evidence metadata; provider schema embedded
  được parity-test với canonical artifact.
- Policy/rule vẫn `PROPOSED`; mode evaluation-only; kết quả cuối vẫn manual/no-action.
- Version mới: rule catalog `RC-2.0.0-proposed.2`, rule `1.0.0-proposed.2`, prompt
  `report-ai-v2-sprint2.2`, workflow `cafestory-admin-report-ai-resolution-v2-s2.2`.
- Verify: focused Backend `50/50`; full Backend `638`, fail/error `0`, skip `1`; semantic coverage
  line `100%`, branch `88.49%`; schema boundary `7/7`; n8n nested boundary/parity `PASS`;
  ADV `12/12`; M07 `18/18`; cross-review `16/16`.
- Năm issue được phát hiện trong quá trình verify và đều đã fix/retest.
- Không đổi FE/DB trong S2-02; chưa publish/import n8n runtime, chưa gọi provider thật.
- Evidence: `09-sprints/evidence/2026-07-29T21-36-56-340+07-00/`.
- Approval đã nhận: `APPROVE_S2_02`.
- `S2-03` chưa mở; token triển khai kế tiếp là `IMPLEMENT_S2_03`.
- Các dòng lịch sử bên dưới được giữ để trace; mục này là trạng thái hiện hành.

## Lịch sử gần nhất — S2-01

- `S2-DD-01`: `APPROVED`.
- `S2-01`: `COMPLETED_VERIFIED_APPROVED`.
- Đã chuyển Runtime Rule Context và Common Evidence Envelope sang typed DTO.
- Policy/rule hiện vẫn `PROPOSED`; mode `PROPOSED_EVALUATION_ONLY`; mọi output bị clamp
  `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- n8n chỉ gửi allowlisted `providerInput`; không gửi whole request, report claim, target ID,
  idempotency/correlation metadata, raw prior-AI hoặc raw media URL.
- Version mới: prompt `report-ai-v2-sprint2.1`; workflow
  `cafestory-admin-report-ai-resolution-v2-s2.1`.
- Verify: focused Backend `40/40`; full Backend `628`, fail/error `0`, skip `1`;
  ADV `12/12` + contract guards `3/3`; M07 fixtures `18/18`; cross-review `16/16`.
- Coverage changed production: line `100%`; branch thấp nhất `88.84%`.
- Chưa publish/import n8n runtime mới, chưa gọi provider thật, không đổi DB, không đổi FE trong S2-01.
- Evidence: `09-sprints/evidence/2026-07-29T21-05-20-543+07-00/`.
- Approval đã nhận: `APPROVE_S2_01`.
- Bước `IMPLEMENT_S2_02` đã được nhận và triển khai ở mục hiện hành phía trên.
- Các dòng lịch sử bên dưới được giữ để trace; mục này là trạng thái hiện hành.

- Dossier: `IN_PROGRESS`.
- Nguồn tiến độ chính: `MASTER-ROADMAP-CHECKLIST.md`.
- G0-00 đến G0-12: `APPROVED`.
- G0-12M Evidence Metadata Contract: `COMPLETED`; `G0-12M-00`–`G0-12M-08` đã approved.
- `G0-12M-01 — Common Evidence Envelope`: `COMPLETED`.
- Deliverable: `05-evidence-standard/common-evidence-envelope.md`.
- `G0-12M-02 — BLOG metadata schema`: `COMPLETED`.
- Deliverable: `08-target-evidence/blog-metadata-schema.md`.
- `G0-12M-03 — COMMENT metadata schema`: `COMPLETED`.
- Deliverable: `08-target-evidence/comment-metadata-schema.md`.
- Approval đã nhận: `APPROVE_G0-12M-03`.
- `G0-12M-04 — USER/CAFE_PAGE minimal manual-only metadata`: `COMPLETED`.
- Deliverable: `08-target-evidence/user-cafe-page-manual-only-metadata-schema.md`.
- Approval đã nhận: `APPROVE_G0-12M-04`.
- `G0-12M-05 — Evidence Kind Catalog`: `COMPLETED`.
- Deliverable: `05-evidence-standard/evidence-kind-catalog.md`.
- Approval đã nhận: `APPROVE_G0-12M-05`.
- `G0-12M-06 — Rule-to-Evidence Requirement Matrix`: `COMPLETED`.
- Deliverable: `05-evidence-standard/rule-to-evidence-requirement-matrix.md`.
- Approval đã nhận: `APPROVE_G0-12M-06`.
- `G0-12M-07 — JSON Schema, fixtures và contract version`: `COMPLETED`.
- Deliverable: `05-evidence-standard/evidence-metadata-contract-v2.md`.
- Executable artifacts: `05-evidence-standard/contracts/v2.0.0-rc.1/`.
- Validation: Ajv strict compile `PASS`; fixture suite hiện tại `18/18 PASS`.
- Approval đã nhận: `APPROVE_G0-12M-07`.
- `G0-12M-08 — Evidence Metadata Contract cross-review`: `COMPLETED`.
- Deliverable: `05-evidence-standard/evidence-metadata-contract-cross-review.md`.
- Cross-review: `16/16 PASS`; `4/4` issues fixed; open issue trong scope `0`.
- Approval đã nhận: `APPROVE_G0-12M-08`.
- Manifest lifecycle: `APPROVED`; runtime authority: `false`.
- Sprint 1 implementation và DD as-built: `COMPLETED_TECHNICAL_DOD_APPROVED`; source HEAD `9c155ff`.
- Contract target: `Admin Report AI Contract 2.0`.
- Automation: `A0_RECOMMEND_ONLY`; không tạo job hoặc AI-triggered mutation.
- Runtime flow đã thiết kế: Admin FE → Backend → n8n/OpenAI → Backend validator/persistence → FE.
- Implementation order đã thiết kế: BE safety → persistence/contract → evidence/validator/security
  → n8n → FE → verification → rollout.
- Evidence semantics: finding phải tham chiếu Evidence ID; missing/conflict critical
  → `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- Score semantics: categorical likelihood/harm/action risk; không numeric threshold authority.
- Target depth: BLOG/COMMENT detailed; USER/CAFE_PAGE local manual-only, không provider call.
- Legacy: V1 history readable; raw hidden; `APPROVE/NONE` là compatibility aliases.
- Traceability: `12/12` business decisions realized, `19/19` current gaps routed/deferred.
- Đã thực hiện source: A0 guard, Contract V2, persistence, signed n8n runtime và evidence-first UI.
- Đã verify n8n/provider runtime, Admin UI E2E và no-mutation trên môi trường local/disposable.
- `G0-12A`: `COMPLETED_SOURCE_STATIC_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12A`.
- G0-12A source commit: `bcc7932`; tài liệu được loại khỏi source commit theo yêu cầu.
- Deliverable: `09-sprints/g0-12a-security-boundary-detailed-design.vi.md`.
- Backend security regression: `45/45 PASS`; signer coverage `100% line / 85.71% branch`.
- n8n security validator: valid/replay/stale/tamper/invalid signature/signed response `PASS`; export `active=false`.
- G0-12A runtime security boundary vẫn thuộc G0-12D; HMAC dùng tại G0-12C chỉ là secret local tạm thời và không được lưu.
- `G0-12B`: `COMPLETED_COVERAGE_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12B`.
- G0-12B source/test commit: `f608528`; tài liệu được loại khỏi commit theo yêu cầu.
- Coverage: 5/5 class đạt `100%` line và `>=85%` branch; focused `56/56 PASS`.
- Full Backend regression: `617` tests, `0` failure, `0` error, `1` PostgreSQL integration skip.
- Deliverable: `09-sprints/g0-12b-coverage-gate-detailed-design.vi.md`.
- `G0-12C`: `COMPLETED_RUNTIME_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12C`.
- PostgreSQL 17 disposable: Flyway baseline `20260719.01`, migration `20260723.01` success.
- Schema: 17/17 cột V2, 8/8 CHECK constraint và 2/2 unique index tồn tại, valid.
- Backend: Flyway restart validation, JPA `validate`, package build và API smoke pass.
- Regression: focused `63/63 PASS`; full Backend `617` tests, `0` failure, `0` error, `1` unrelated skip.
- Cleanup: Backend và PostgreSQL disposable đã dừng; resolution/job row count đều `0`.
- Deliverable: `09-sprints/g0-12c-postgresql-backend-runtime-detailed-design.vi.md`.
- `G0-12D`: `COMPLETED_RUNTIME_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12D`.
- OpenAI key mới từ `docker/.env` đã được xác thực HTTP `200` và đồng bộ local cho n8n/AI Python; Backend/FE/mobile không giữ provider key.
- Shared HMAC đã được cấu hình local cho Backend/n8n mà không ghi secret vào source/evidence.
- Đã backup 5 workflow, unpublish runtime cũ, import và publish canonical V2.
- Published version: `4e221ccf-12a3-4583-8336-c4fda22b5b70`; parity `5/5` node.
- Exact webhook valid request: provider response có, response signature pass, kết quả `NEEDS_MANUAL_REVIEW + NO_ACTION`, không numeric score.
- Replay, stale/future, invalid signature, tampered body và missing headers đều có execution `error`.
- Phát hiện critical trong repair loop: `$getWorkflowStaticData('global')` không chặn replay ở runtime; đã thay bằng nonce file atomic `wx`, persist qua restart.
- Residual cần hardening trước production: negative case ở n8n `2.28.6` trả transport HTTP `200` body rỗng dù execution `error`; Backend hiện fail-closed vì response unsigned/empty.
- Backend focused regression: `37/37 PASS`.
- Evidence: `09-sprints/evidence/2026-07-29T09-53-00-590+07-00/`.
- Detailed Design: `09-sprints/g0-12d-n8n-provider-runtime-detailed-design.vi.md`.
- Source/test commit G0-12D: `7168af7`; documents, `.env` và secret không nằm trong commit.
- `G0-12E`: `COMPLETED_VERIFIED_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12E`.
- Phát hiện/sửa `G012E-CODE-004`: cached resolution bỏ qua A0 warning; regression mới pass.
- Playwright đã bổ sung target snapshot trước/sau cho BLOG/COMMENT/USER/CAFE_PAGE; tất cả không đổi.
- Automated E2E: `29 PASSED`, `0 WARN`, `0 FAILED`, `1 BLOCKED` do không tự tạo legacy job trong A0.
- Supplemental legacy cancel: UI `Scheduled → Cancelled`, DB `ADMIN_CANCELLED`, `applied_at` null.
- Effective acceptance scenario: `30/30 PASS`.
- Backend focused `32/32 PASS`; full `618` tests, 0 failure, 0 error, 1 integration skip.
- Coverage changed class: `100% line / 87.85% branch`; Admin typecheck/build pass.
- Source/test commit G0-12E: `9c155ff`; documents và evidence không nằm trong commit.
- Evidence: `09-sprints/evidence/2026-07-29T11-10-13-870+07-00/`.
- Detailed Design: `09-sprints/g0-12e-admin-ui-e2e-detailed-design.vi.md`.
- `G0-12F`: `COMPLETED_VERIFIED_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12F`.
- Đã tái tạo Detailed Design tổng tiếng Việt theo Backend → n8n/OpenAI → Backend → Admin UI.
- DD as-built: `09-sprints/g0-12-implementation-detailed-design.vi.md`.
- Evidence G0-12F: `09-sprints/evidence/2026-07-29T11-56-02-316+07-00/`.
- Không sửa source, database, secret hoặc n8n runtime trong G0-12F.
- `G0-12-DONE Audit`: `COMPLETED_AUDIT_APPROVED_REMEDIATION_REQUIRED`.
- Approval đã nhận: `APPROVE_G0-12-DONE-AUDIT`.
- Kết quả: 15 pass/scoped-pass, 1 partial, 5 blocked; production readiness 9/14.
- Blocker: adversarial execution, stale snapshot, COMMENT missing-context E2E, provider-unavailable E2E,
  bulk partial-failure E2E và terminal-report FE assertion.
- Audit deliverable: `09-sprints/g0-12-done-audit.vi.md`.
- Audit evidence: `09-sprints/evidence/2026-07-29T12-16-06-357+07-00/`.
- `DOD-FIX-01`: `COMPLETED_VERIFIED_APPROVED`.
- ADV executable suite: `12/12 PASS`; không gọi provider thật.
- Đã fix 2 semantic gap: prior AI/reporter claim làm evidence dương duy nhất và
  evidence-reference fields chưa được allowlist đầy đủ.
- Focused Backend: `60/60 PASS`; full Backend: `621`, fail/error `0`, skipped `1`.
- Changed class coverage: line `100%`, branch `96.61%`.
- DD: `09-sprints/g0-12-done-fix-01-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T12-34-03-705+07-00/`.
- Trạng thái gate tại thời điểm FIX-01: `G0-12-DONE — AUDIT_APPROVED_REMEDIATION_REQUIRED`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-01`.
- `DOD-FIX-02`: `COMPLETED_VERIFIED_APPROVED`.
- Đã fix hai lỗi: cache snapshot cũ bị reuse và provider output stale bị persist.
- CT-010/SAF-010 executable tests: service `25/25`, Admin Report AI focused `63/63`,
  full Backend `624`, fail/error `0`, skipped `1`.
- Changed class coverage: line `100%`, branch `87.76%`.
- DD: `09-sprints/g0-12-done-fix-02-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T13-47-28-176+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-02`.
- `DOD-FIX-03`: `COMPLETED_VERIFIED_APPROVED`.
- Đã thêm fixture COMMENT có parent BLOG context whitespace và chạy full path
  Admin UI → Backend → n8n/OpenAI → Backend → Admin UI.
- Đã fix:
  - Backend từng coi parent content whitespace là evidence usable;
  - semantic validator từng giữ `INSUFFICIENT` khi thiếu critical evidence thay vì
    `UNASSESSABLE`.
- Kết quả E2E-S1-04: HTTP `200`, `NEEDS_MANUAL_REVIEW + NO_ACTION`, findings rỗng,
  `CRITICAL_EVIDENCE_MISSING`, `UNASSESSABLE`, no mutation, auto job `0`, cleanup `0/0/0`.
- Backend service/semantic `37/37`, focused `63/63`, full `624`, fail/error `0`, skipped `1`.
- Admin typecheck/build `PASS`; changed-class coverage line `100%`, branch `87.75%`/`96.72%`.
- DD: `09-sprints/g0-12-done-fix-03-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T14-22-18-183+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-03`.
- `DOD-FIX-04`: `COMPLETED_VERIFIED_APPROVED`.
- Đã fault-inject n8n unavailable trên full path Admin UI → Backend → n8n → Backend → Admin UI.
- Failure: HTTP `502`, `AI_PROVIDER_BOUNDARY_FAILED`, correlation ID, retryable/stage đầy đủ;
  resolution `0 -> 0`.
- Recovery: n8n được khôi phục, retry HTTP `200`, resolution `1`, report/target không mutation,
  auto job `0`.
- Backend focused `64/64`; full `625`, fail/error `0`, skipped `1`.
- Admin typecheck/build và Playwright `E2E-S1-13 1/1 PASS`.
- Changed Backend coverage: line `100%`, service branch `87.75%`.
- DD: `09-sprints/g0-12-done-fix-04-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T14-53-04-516+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-04`.
- `DOD-FIX-05`: `COMPLETED_VERIFIED_APPROVED`.
- Bulk partial failure: HTTP `200/409`, success resolution `0 -> 1`, failed resolution `0 -> 0`,
  no mutation, auto job `0`, cleanup `0/0`.
- Evidence FIX-05: `09-sprints/evidence/2026-07-29T15-20-53-107+07-00/`.
- DD FIX-05: `09-sprints/g0-12-done-fix-05-detailed-design.vi.md`.
- `DOD-FIX-06`: `COMPLETED_VERIFIED_APPROVED`.
- Terminal guard: Ask AI disabled, UI request `0`, Backend bypass HTTP `409`, resolution `0 -> 0`,
  no mutation, auto job `0`, cleanup `0/0`.
- Evidence FIX-06: `09-sprints/evidence/2026-07-29T15-20-53-108+07-00/`.
- DD FIX-06: `09-sprints/g0-12-done-fix-06-detailed-design.vi.md`.
- Cả hai package không đổi production source; chỉ bổ sung executable Playwright evidence.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-05` và `APPROVE_G0-12-DONE-FIX-06`.
- Toàn bộ `DOD-FIX-01`–`DOD-FIX-06` đã `COMPLETED_VERIFIED_APPROVED`.
- Re-audit `G0-12-DONE`: `COMPLETED_REAUDIT_APPROVED`.
- DOD sau remediation: `21/21 PASS/PASS_WITH_SCOPE`, partial `0`, blocked `0`.
- Current-worktree verification: Backend `625`, fail/error `0`, skipped `1`; Admin typecheck/build
  `PASS`; ADV `12/12 PASS`.
- Re-audit DD: `09-sprints/g0-12-done-reaudit.vi.md`.
- Re-audit evidence: `09-sprints/evidence/2026-07-29T16-32-07-654+07-00/`.
- Source state: HEAD `9c155ff`, 15 remediation file đang dirty và đã fingerprint SHA-256.
- Production readiness không đổi: `9/14 NOT_READY`; production deployment chưa được phép.
- Approval đã nhận: `APPROVE_G0-12-DONE`; technical Sprint 1 đã đóng.
- Sprint 2 rebase đã được chốt bằng `APPROVE_S2_REBASE_01`.
- Trạng thái rebase: `COMPLETED_REBASE_APPROVED`; 9 gap đã được route thành core,
  conditional và deferred.
- Detailed Rebase: `09-sprints/s2-rebase-01.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T20-19-18-018+07-00/`.
- `S2-DD-01`: `COMPLETED_DESIGN_AWAITING_APPROVAL`.
- DD: `09-sprints/s2-dd-01-runtime-rule-evidence-prompt-evaluation.vi.md`.
- DD evidence: `09-sprints/evidence/2026-07-29T20-34-57-762+07-00/`.
- Highlight: policy/catalog đang `PROPOSED` nhưng runtime chưa fail-closed theo lifecycle.
- Approval cần nhận: `APPROVE_S2_DD_01`.
- Chưa sửa code; sau approval mới được đề xuất `IMPLEMENT_S2_01`.
- Không sửa/publish n8n workflow; production deployment chưa được phép.
- Ràng buộc: G0-12 cho phép tiếp tục implementation Sprint 1, không cho phép production deploy.
