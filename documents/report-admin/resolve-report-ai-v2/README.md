# Policy–Evidence–AI System Design Dossier

Roadmap thực thi ngắn gọn hiện hành: `CURRENT-LEAN-ROADMAP.md`. File
`MASTER-ROADMAP-CHECKLIST.md` chỉ còn dùng để tra lịch sử chi tiết.

- Trạng thái: `IN_PROGRESS`
- Phạm vi: Admin Resolve Report with AI V2
- Vị trí chuẩn: `documents/report-admin/resolve-report-ai-v2/`
- Nguyên tắc: Bound → Execute → Verify → Done

## Thứ tự thực hiện đã chốt

1. Tạo toàn bộ cây hồ sơ.
2. Viết project charter, scope và glossary.
3. Viết bộ cơ sở lý thuyết.
4. Khảo sát report reasons/policy hiện có trong source và database.
5. Viết policy framework bản `PROPOSED`.
6. Viết rule catalog bản đầu.
7. Tạo traceability matrix.
8. Review và chốt các decision cần business approval.
9. Viết chi tiết Sprint 1 dựa trên policy đã chốt.
10. Chỉ sau đó mới sửa code.

## Tiến độ hiện tại

- Checklist tiến độ chính:
  `MASTER-ROADMAP-CHECKLIST.md`.
- G0-00–G0-12: `APPROVED`; G0-12 chỉ cho phép implementation, không cho phép production deploy.
- G0-12M Evidence Metadata Contract: `COMPLETED`; `G0-12M-00`–`G0-12M-08` đã approved.
- Sprint 1: `COMPLETED_TECHNICAL_DOD_APPROVED`; production vẫn `9/14 NOT_READY`.
- Detailed Design chính:
  `09-sprints/sprint-01-safety-contract.md`.
- Đã thiết kế component specs tại `07-system-design/`, target evidence tại
  `08-target-evidence/`, verification tại `10-verification/` và rollout/rollback tại
  `11-operations/`.
- Implementation commit hiện tại: `c4bb2a7`.
- G0-12A security boundary source commit: `bcc7932` (không chứa documents/secret).
- G0-12B coverage gate source/test commit: `f608528` (không chứa documents).
- G0-12D n8n/runtime source/test commit: `7168af7` (không chứa documents, `.env` hoặc secret).
- `G0-12M-02 — BLOG metadata schema`: `COMPLETED`.
- `G0-12M-03 — COMMENT metadata schema`: `COMPLETED`.
- `G0-12M-04 — USER/CAFE_PAGE minimal manual-only metadata`: `COMPLETED`.
- `G0-12M-05 — Evidence Kind Catalog`: `COMPLETED`.
- `G0-12M-06 — Rule-to-Evidence Requirement Matrix`: `COMPLETED`.
- Approval đã nhận: `APPROVE_G0-12M-06`.
- `G0-12M-07 — JSON Schema, fixtures và contract version`: `COMPLETED`.
- Approval đã nhận: `APPROVE_G0-12M-07`.
- `G0-12M-08 — Evidence Metadata Contract cross-review`: `COMPLETED`.
- `G0-12M Evidence Metadata Contract`: `COMPLETED`.
- Approval đã nhận: `APPROVE_G0-12M-08`.
- `G0-12A`: `COMPLETED_SOURCE_STATIC_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12A`.
- Detailed Design: `09-sprints/g0-12a-security-boundary-detailed-design.vi.md`.
- `G0-12B`: `COMPLETED_COVERAGE_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12B`.
- Detailed Design: `09-sprints/g0-12b-coverage-gate-detailed-design.vi.md`.
- `G0-12C`: `COMPLETED_RUNTIME_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12C`.
- Detailed Design: `09-sprints/g0-12c-postgresql-backend-runtime-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-26T13-26-59-327+07-00/`.
- `G0-12D`: `COMPLETED_RUNTIME_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12D`.
- OpenAI key mới đã được xác thực và đồng bộ local cho n8n/AI Python; Backend/FE/mobile không giữ provider key.
- Shared HMAC đã được cấu hình local; canonical V2 đã publish và khớp repo `5/5` node.
- Exact webhook/provider, response signature, freshness, replay, tamper và focused Backend regression đã được kiểm chứng.
- Evidence: `09-sprints/evidence/2026-07-29T09-53-00-590+07-00/`.
- Detailed Design: `09-sprints/g0-12d-n8n-provider-runtime-detailed-design.vi.md`.
- Lỗi đã highlight: static workflow data không chặn replay ở runtime; đã thay bằng atomic file claim. Residual: negative case hiện trả HTTP `200` body rỗng dù execution là `error`.
- `G0-12E`: `COMPLETED_VERIFIED_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12E`.
- Source/test commit: `9c155ff`; documents/evidence không nằm trong commit.
- Backend focused `32/32 PASS`; full regression `618` tests, 0 failure, 0 error, 1 integration skip.
- Admin typecheck/build và Playwright command pass.
- `29/30` scenario automated pass; legacy cancel được kiểm chứng bổ sung bằng fixture disposable, effective `30/30`.
- BLOG/COMMENT/USER/CAFE_PAGE target snapshot đều không đổi sau AI.
- Detailed Design: `09-sprints/g0-12e-admin-ui-e2e-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T11-10-13-870+07-00/`.
- `G0-12F`: `COMPLETED_VERIFIED_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12F`.
- Detailed Design as-built tiếng Việt: `09-sprints/g0-12-implementation-detailed-design.vi.md`.
- Evidence G0-12F: `09-sprints/evidence/2026-07-29T11-56-02-316+07-00/`.
- File tổng cũ đã được sửa lỗi traceability: không còn ghi n8n/provider/Admin UI là chưa chạy.
- Audit G0-12-DONE: `15 PASS/PASS_WITH_SCOPE`, `1 PARTIAL`, `5 BLOCKED`.
- Production readiness: `9/14 PASS`, `5/14 OPEN`.
- Audit deliverable: `09-sprints/g0-12-done-audit.vi.md`.
- Audit evidence: `09-sprints/evidence/2026-07-29T12-16-06-357+07-00/`.
- Approval audit đã nhận: `APPROVE_G0-12-DONE-AUDIT`.
- Gate hiện tại: `G0-12-DONE — COMPLETED_REAUDIT_APPROVED`.
- `DOD-FIX-01`: `COMPLETED_VERIFIED_APPROVED`; ADV `12/12 PASS`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-01`.
- `DOD-FIX-02`: `COMPLETED_VERIFIED_APPROVED`.
- Đã chặn cache stale reuse và provider-window stale persistence; `CT-010`/`SAF-010` có executable tests.
- Backend service `25/25`, Admin Report AI focused `63/63`, full Backend `624`, fail/error `0`, skipped `1`.
- Changed class coverage: line `100%`, branch `87.76%`.
- DD: `09-sprints/g0-12-done-fix-02-detailed-design.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T13-47-28-176+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-02`.
- `DOD-FIX-03`: `COMPLETED_VERIFIED_APPROVED`.
- COMMENT thiếu critical parent context được Backend khóa về
  `NEEDS_MANUAL_REVIEW + NO_ACTION + UNASSESSABLE`.
- Full-path E2E `E2E-S1-04`: `1/1 PASS`; no mutation, auto job `0`, cleanup sạch.
- Evidence: `09-sprints/evidence/2026-07-29T14-22-18-183+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-03`.
- `DOD-FIX-04`: `COMPLETED_VERIFIED_APPROVED`.
- Backend đã chuẩn hóa provider boundary thành structured HTTP `502`; Admin hiển thị
  code/stage/support reference/retry state.
- Full-path `E2E-S1-13`: failure không persist và recovery retry thành công; no mutation,
  auto job `0`, cleanup sạch.
- Evidence: `09-sprints/evidence/2026-07-29T14-53-04-516+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-04`.
- `DOD-FIX-05`: `COMPLETED_VERIFIED_APPROVED`; `E2E-S1-09 1/1 PASS`.
- `DOD-FIX-06`: `COMPLETED_VERIFIED_APPROVED`; `E2E-S1-10 1/1 PASS`.
- Hai package không cần sửa production source; audit gap là thiếu executable evidence.
- Evidence:
  - `09-sprints/evidence/2026-07-29T15-20-53-107+07-00/`;
  - `09-sprints/evidence/2026-07-29T15-20-53-108+07-00/`.
- Approval đã nhận:
  `APPROVE_G0-12-DONE-FIX-05`,
  `APPROVE_G0-12-DONE-FIX-06`.
- Toàn bộ remediation package đã approved; re-audit `G0-12-DONE` sau đó đã được thực hiện.
- Re-audit đã thực hiện:
  - technical DOD `21/21 PASS/PASS_WITH_SCOPE`;
  - partial `0`, blocked `0`;
  - Backend current-worktree `625`, fail/error `0`, skipped `1`;
  - Admin typecheck/build `PASS`; ADV `12/12 PASS`;
  - source fingerprint `15/15`;
  - production readiness giữ `9/14 NOT_READY`.
- Re-audit DD: `09-sprints/g0-12-done-reaudit.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T16-32-07-654+07-00/`.
- Approval đã nhận: `APPROVE_G0-12-DONE`; technical Sprint 1 đã đóng.
- Sprint 2 rebase: `COMPLETED_REBASE_APPROVED`.
- Detailed Rebase: `09-sprints/s2-rebase-01.vi.md`.
- Evidence: `09-sprints/evidence/2026-07-29T20-19-18-018+07-00/`.
- Approval rebase đã nhận: `APPROVE_S2_REBASE_01`.
- `S2-DD-01`, `S2-01`–`S2-04`: `COMPLETED_VERIFIED_APPROVED`.
- DD: `09-sprints/s2-dd-01-runtime-rule-evidence-prompt-evaluation.vi.md`.
- `S2-05`: `PARTIAL_HARNESS_FIXED_PROVIDER_RETEST_REQUIRED`.
- Provider execute lần 1 bị `36/36 invalid_json_schema` trước inference, cost ghi
  nhận `0 USD`; local adapter/circuit breaker đã static-verify.
- S2-05 DD: `10-verification/provider-model-benchmark-design.md`.
- S2-05 evidence: `09-sprints/evidence/2026-07-30T15-39-00-343+07-00/`.
- S2-05 disposition cho Sprint 2 core:
  `DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY`; kết quả benchmark chưa pass,
  quality denominator vẫn `0`, không chọn model.
- Sprint 2 core: `COMPLETED_VERIFIED_APPROVED`.
- S2-DONE-AUDIT ban đầu: `10 PASS`, `1 PASS_WITH_SCOPE`, `3 PARTIAL`, `0 BLOCKED`.
- `S2-DONE-FIX-01` đã xử lý ba remediation audit.
- `S2-DONE-FIX-02` đã sửa schema hash `71 > VARCHAR(64)` bằng migration mới
  `20260730.01`; focused `54/54`, full Backend `642`, security và hai Admin E2E
  đều pass, không mutation, auto-job `0`.
- Audit DD: `09-sprints/s2-done-audit.vi.md`.
- Audit evidence: `09-sprints/evidence/2026-07-30T16-09-39-334+07-00/`.
- Final evidence: `09-sprints/evidence/2026-07-30T17-01-03-753+07-00/`.
- Final approval đã nhận: `APPROVE_S2_DONE`.
- Không còn action bắt buộc để đóng core; production deployment vẫn chưa được phép.

## Quy tắc trạng thái

- `NOT_STARTED`: mới tạo file khung, chưa có nội dung được thẩm định.
- `DRAFT`: đang soạn, chưa review.
- `PROPOSED`: đã có đề xuất nhưng chưa được business phê duyệt.
- `APPROVED`: đã được người có thẩm quyền phê duyệt.
- `BLOCKED`: thiếu evidence, dữ liệu hoặc quyết định.
- `SUPERSEDED`: đã được thay thế bằng phiên bản khác.

## Ràng buộc

- Không coi file khung là tài liệu thiết kế đã hoàn thành.
- Không coi report của user là fact đã được xác minh.
- Không sửa FE, BE, database hoặc n8n trước khi hoàn thành các bước tiền triển khai bắt buộc.
- Mỗi bước phải được review riêng trước khi chuyển sang bước tiếp theo.
