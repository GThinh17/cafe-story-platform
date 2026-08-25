# G0-12-DONE — Audit Definition of Done Sprint 1

## 1. Kiểm soát audit

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12-DONE` |
| Audit token | `IMPLEMENT_G0_12_DONE_AUDIT` |
| Nhánh | `n8n/Ai-agent/fix-bug-report-admin` |
| Source HEAD | `9c155ff` |
| Ngày audit | `2026-07-29` |
| Phạm vi | Sprint 1 Resolve Report with AI V2 |
| Production deployment | `NOT_AUTHORIZED` |
| Kết luận | `PARTIAL — REMEDIATION_REQUIRED` |
| Approval audit findings | `APPROVED — APPROVE_G0-12-DONE-AUDIT` |

## 2. Bound

### Mục tiêu

Đối chiếu từng tiêu chí bắt buộc của Sprint 1 với source hiện tại, approval ledger, test evidence, runtime evidence
và tài liệu as-built trước khi xem xét đánh dấu `G0-12-DONE`.

### Được phép

- đọc source, test và evidence hiện có;
- kiểm tra HEAD/fingerprint/JSON/UTF-8/traceability;
- cập nhật checklist, status, handoff và tài liệu audit;
- phân loại blocker, residual risk và production-readiness gap.

### Ngoài phạm vi

- sửa production/test code;
- thay đổi database, n8n runtime, provider key hoặc secret;
- chạy production deployment;
- tự động đánh dấu `G0-12-DONE`;
- tự động mở Sprint 2.

### Nguyên tắc chấm

- `PASS`: có executable hoặc runtime evidence trực tiếp và source HEAD vẫn khớp;
- `PARTIAL`: behavior cốt lõi có evidence nhưng thiếu một phần required matrix;
- `BLOCKED`: tiêu chí bắt buộc chưa có executable evidence;
- `DEFERRED_NON_BLOCKING`: đã được thiết kế và được phép chuyển Sprint sau;
- production readiness được đánh giá riêng với implementation closure.

## 3. Nguồn bằng chứng

| Nguồn | Vai trò |
|---|---|
| G0-12A evidence | HMAC/freshness/replay source/static |
| G0-12B evidence | Changed-file coverage và Backend regression |
| G0-12C evidence | PostgreSQL/Flyway/JPA disposable runtime |
| G0-12D evidence | Published n8n/provider/signed response/security runtime |
| G0-12E evidence | Admin UI/E2E/no-mutation/legacy cancel |
| G0-12F evidence | As-built DD và source fingerprint |
| `sprint-01-safety-contract.md` | Acceptance matrix và thiết kế Sprint 1 |
| `10-verification/` | Required contract/safety/semantic/adversarial/E2E matrices |
| `11-operations/` | Production readiness, rollout, rollback và monitoring |

Không chạy lại Maven/Playwright/n8n trong audit tài liệu này vì HEAD vẫn đúng `9c155ff`, là HEAD đã sinh evidence
G0-12E/F. Audit kiểm tra tính áp dụng của evidence và phát hiện các scenario chưa từng được thực thi.

## 4. Kết quả audit gate A–F

| Gate | Trạng thái | Kết quả audit |
|---|---|---|
| G0-12A | Approved | `PASS` |
| G0-12B | Approved | `PASS` |
| G0-12C | Approved | `PASS` trên PostgreSQL disposable |
| G0-12D | Approved | `PASS` trên local n8n/provider runtime |
| G0-12E | Approved | `PASS` cho evidence đã chạy |
| G0-12F | Approved | `PASS` |

Approval A–F chứng minh từng gate tương ứng. Chúng không tự chứng minh rằng tất cả scenario trong ma trận
`sprint-01-safety-contract.md`, `safety-test-matrix.md` và `e2e-test-plan.md` đã được thực thi.

## 5. Ma trận Definition of Done Sprint 1

| ID | Tiêu chí | Evidence | Kết quả |
|---|---|---|---|
| `DOD-01` | A0 mặc định, request không bật automation | Unit tests + G0-12E RAI-14/15/17/18 | `PASS` |
| `DOD-02` | Không tạo auto-apply job mới và không mutate report/target | Unit tests + no-mutation matrix 4 target | `PASS` |
| `DOD-03` | Legacy due job bị quarantine; legacy cancel dùng được | Unit tests + supplemental legacy cancel | `PASS` |
| `DOD-04` | Contract V2, categorical semantics, Rule/Evidence references | Backend tests + G0-12M + RAI-12 | `PASS` |
| `DOD-05` | USER/CAFE_PAGE local manual-only, không provider | Backend mock verification + RAI-09/10 | `PASS` |
| `DOD-06` | Unknown rule/evidence và critical missing bị clamp | Backend semantic/service tests | `PASS` |
| `DOD-07` | V2 raw payload và numeric score không cấp authority | Backend tests + UI + secret scan | `PASS` |
| `DOD-08` | Migration/schema constraints hoạt động | G0-12C: 17/17 cột, 8/8 CHECK, 2/2 index | `PASS_DISPOSABLE` |
| `DOD-09` | Changed-file coverage và regression đạt gate | G0-12B/E | `PASS` |
| `DOD-10` | Signed request/response, freshness và replay | G0-12D | `PASS_WITH_RESIDUAL` |
| `DOD-11` | Exact published webhook/provider path hoạt động | G0-12D | `PASS_LOCAL_RUNTIME` |
| `DOD-12` | Admin evidence-first UI, bulk, legacy history | G0-12E | `PASS` |
| `DOD-13` | BLOG/COMMENT/USER/PAGE representative runtime | RAI-07–10 | `PASS` |
| `DOD-14` | Prompt injection/adversarial suite thực thi | Chỉ có designed matrix ADV-001–012; không có execution evidence | `BLOCKED_TEST_EVIDENCE` |
| `DOD-15` | Stale snapshot không bị reuse (`CT-010`, `SAF-010`) | Idempotency same-snapshot có test; changed-snapshot case chưa có | `BLOCKED_TEST_EVIDENCE` |
| `DOD-16` | COMMENT thiếu critical parent/context ở full E2E | Unit evidence có critical guards; E2E chỉ chứng minh COMMENT fixture hiện có | `BLOCKED_E2E_FIXTURE` |
| `DOD-17` | Provider unavailable ở full FE→BE→n8n→BE→FE | Backend fail-closed có test; UI/E2E unavailable scenario chưa chạy | `BLOCKED_E2E_SCENARIO` |
| `DOD-18` | Bulk partial failure giữ success và không mutation (`SAF-011`) | RAI-25/27 chạy success batch; không inject per-item failure | `BLOCKED_E2E_SCENARIO` |
| `DOD-19` | Terminal report bị chặn ở cả BE và FE (`SAF-012`) | BE terminal test pass; FE disabled/rejected assertion chưa có evidence riêng | `PARTIAL` |
| `DOD-20` | Rollout/rollback/incident/model-change plan tồn tại | `11-operations/` | `PASS_DESIGN_ONLY` |
| `DOD-21` | Tài liệu as-built và source/evidence traceable | G0-12F + fingerprint | `PASS` |

Tổng:

```text
PASS/PASS_WITH_SCOPE : 15
PARTIAL              : 1
BLOCKED              : 5
TOTAL                : 21
```

## 6. Phản biện kết quả “effective 30/30”

G0-12E báo `29` automated scenario pass và `1` supplemental legacy-cancel pass, nên `30/30` là đúng trong phạm vi
runner RAI-01–RAI-30.

Tuy nhiên, RAI-01–RAI-30 không đồng nhất một-một với toàn bộ required matrices đã thiết kế:

- không có ADV-001–ADV-012 executable run;
- không có changed-snapshot stale reuse case;
- không có provider-unavailable E2E;
- không có COMMENT thiếu critical context E2E riêng;
- bulk run quan sát `Failed: 0`, không kiểm tra partial failure;
- terminal status được đổi qua API nhưng chưa chứng minh Ask AI bị disable/reject trên FE.

Vì vậy nói “G0-12E effective 30/30” là chính xác; dùng con số đó để suy ra “toàn bộ Sprint 1 DoD đã pass” là không
chính xác.

## 7. Production readiness

`11-operations/production-readiness-checklist.md` hiện:

```text
9/14 PASS
5/14 OPEN
```

Các mục mở:

1. chưa có named Policy/Security/Backend/Operations owners;
2. chưa inventory active legacy jobs trên target environment;
3. Flyway/schema mới chỉ verify trên disposable PostgreSQL, chưa phải external/production target;
4. rollback plan đã thiết kế nhưng chưa drill;
5. monitoring/alert/retention mới là design, chưa có runtime evidence.

Ngoài ra:

- n8n negative execution trên runtime đã test còn trả HTTP `200` body rỗng dù execution là error;
- replay store hiện local/process scoped, chưa chứng minh multi-instance;
- policy/rule version vẫn mang `proposed`;
- media OCR/vision và authoritative external evidence chưa có.

Các mục này giữ production ở `NOT_READY/NOT_AUTHORIZED`. Một số là Sprint 2+ improvement, nhưng năm mục checklist
trên phải được xử lý trong production gate riêng trước deployment.

## 8. Blocker cần remediation trước G0-12-DONE

| Ưu tiên | Package | Nội dung |
|---|---|---|
| P0 | `DOD-FIX-01` | `COMPLETED_VERIFIED_APPROVED`; ADV-001–012 `12/12 PASS` |
| P0 | `DOD-FIX-02` | Explicit changed-snapshot stale/idempotency test |
| P0 | `DOD-FIX-03` | COMMENT missing-critical-context E2E fixture |
| P0 | `DOD-FIX-04` | Provider-unavailable E2E và UI operational-error behavior |
| P0 | `DOD-FIX-05` | Bulk partial failure/no-mutation E2E |
| P1 | `DOD-FIX-06` | Terminal report FE disable/reject assertion |

Không sửa các blocker này trong lượt audit. Chúng cần một implementation gate riêng sau khi người dùng review
kết quả audit.

## 9. Residual không chặn audit findings

- RAI-16 vẫn dùng supplemental disposable fixture thay vì fixture tự động trong runner;
- legacy `ReportModerationJobWorker` lazy `imageUrls` là bug luồng moderation cũ;
- production owner/monitoring/rollback drill và target DB thuộc production-readiness gate;
- USER/CAFE_PAGE deep policy, OCR/vision, external authoritative evidence thuộc Sprint sau;
- không có model accuracy/calibration claim.

## 10. Kết luận

```text
Audit deliverable                    : PASS
Gate A–F approvals                  : PASS
Core A0/contract/runtime/UI baseline: PASS
Formal Sprint 1 test matrix complete: NO
Production readiness                : NOT_READY
G0-12-DONE                          : NOT_ACHIEVED
```

**Trạng thái:** `PARTIAL — AUDITED_REMEDIATION_REQUIRED`.

Kết quả audit đã được người dùng chốt bằng `APPROVE_G0-12-DONE-AUDIT`. Token triển khai package đầu tiên:

```text
IMPLEMENT_G0_12_DONE_FIX_01
```

`DOD-FIX-01` đã được mở ở bước kế tiếp; `G0-12-DONE` vẫn chưa đạt.

## 11. Remediation progress — DOD-FIX-01

`IMPLEMENT_G0_12_DONE_FIX_01` đã được thực thi sau approval audit:

- executable ADV-001–ADV-012: `12/12 PASS`;
- phát hiện và sửa guard prior AI/reporter claim không được làm evidence dương duy nhất;
- kiểm tra allowlist cho mọi evidence-reference field ở n8n và Backend;
- focused Backend `60/60 PASS`;
- full Backend `621 tests`, fail/error `0`, skipped `1`;
- changed class line `100%`, branch `96.61%`;
- evidence:
  `09-sprints/evidence/2026-07-29T12-34-03-705+07-00/`.

Trạng thái package: `COMPLETED_VERIFIED_APPROVED`.
Token review đã nhận:

```text
APPROVE_G0-12-DONE-FIX-01
```

Snapshot audit gốc `15/1/5` được giữ nguyên để bảo toàn lịch sử. Chưa đóng
`G0-12-DONE`; còn `DOD-FIX-02`–`DOD-FIX-06`. Bước kế tiếp:
`IMPLEMENT_G0_12_DONE_FIX_02`.
