# S2-DONE-AUDIT — Audit Definition of Done Sprint 2

## 1. Kết luận

```text
PARTIAL — AUDIT_FINDINGS_REMEDIATION_REQUIRED
```

Sprint 2 core chưa đủ điều kiện `APPROVE_S2_DONE`. Source behavior gates chính
và full Backend regression đều pass, nhưng còn ba remediation bắt buộc:

1. S2-01 thiếu required `summary.json`;
2. changed production line coverage của `AdminReportAiResolutionServiceImpl`
   chỉ đạt `98.77%`, thấp hơn gate `100%`;
3. static security harness drift khỏi canonical S2 request và không chạy được.

Không có production deployment, provider call mới hoặc source fix trong audit.

## 2. Bound và decision S2-05

`IMPLEMENT_S2_DONE_AUDIT` được hiểu đúng theo Sprint 2 plan là quyết định đóng
audit core và defer conditional S2-05.

S2-05 đã có partial run nhưng không có successful inference. Trạng thái được
khóa trong audit:

```text
DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY
```

Điều này không có nghĩa:

- provider benchmark pass;
- model được chọn;
- prompt/model được activate;
- quality/latency/cost đã được đánh giá;
- failed run được xóa.

Failed-run evidence vẫn giữ `36/36 invalid_json_schema`, cost ghi nhận `0 USD`,
quality denominator `0`, selected model `null`.

## 3. Audit matrix

| ID | Tiêu chí | Actual | Trạng thái |
|---|---|---|---|
| `S2DOD-01` | S2-01–S2-04 approved | Approval ledger và evidence tồn tại | `PASS` |
| `S2DOD-02` | S2-05 approved hoặc deferred | Deferred bằng audit token, limitation giữ nguyên | `PASS_WITH_SCOPE` |
| `S2DOD-03` | Policy lifecycle fail-closed | Proposed/inactive route manual/no-action | `PASS` |
| `S2DOD-04` | Evidence kind/snapshot binding | S2 schema + Backend focused pass | `PASS` |
| `S2DOD-05` | Rule version/requirement/complete scope | Semantic validator `23/23` trong focused `50/50` | `PASS` |
| `S2DOD-06` | Prompt chỉ nhận safe projection | Prompt mutation `4/4`; adversarial `12/12` | `PASS` |
| `S2DOD-07` | Bounded schema/parity | Boundary `7/7`, nested/parity pass | `PASS` |
| `S2DOD-08` | Dataset/harness versioned | `1.0.0-rc.1`, manifest/fingerprint pass | `PASS` |
| `S2DOD-09` | Hard safety deterministic slice `100%` | `20` hard-safety records, achieved `100%` | `PASS` |
| `S2DOD-10` | A0/no-mutation invariant | Adversarial + focused/full Backend pass | `PASS` |
| `S2DOD-11` | Regression/security executable | Backend `638` pass; security harness drift/fail | `PARTIAL` |
| `S2DOD-12` | Changed production coverage | branch đạt; ResolutionService line `98.77%` | `PARTIAL` |
| `S2DOD-13` | DD/source/test/evidence traceable | Trace được, nhưng S2-01 thiếu `summary.json` | `PARTIAL` |
| `S2DOD-14` | Production readiness báo riêng | Vẫn `9/14 NOT_READY`, deployment `NOT_AUTHORIZED` | `PASS` |

Tổng:

- `PASS`: `10`;
- `PASS_WITH_SCOPE`: `1`;
- `PARTIAL`: `3`;
- `BLOCKED`: `0`.

## 4. Verification

### 4.1. Project/readiness

Project structure check pass, không warning:

```powershell
python .agents/skills/cafestory-engineering-workflows/scripts/check_project.py .
```

### 4.2. Contract/prompt/dataset

- S2 schema compile: pass;
- schema boundaries: `7/7`;
- nested n8n boundary: pass;
- provider schema parity: pass;
- adversarial: `12/12`;
- prompt pilot: pass;
- dataset: `26` records, `9` bundles;
- dataset hard safety: `100%`;
- dataset negative guards: `6/6`;
- Backend focused từ dataset gate: `50/50`;
- provider benchmark static: pass;
- provider benchmark library coverage: line `100%`, branch `91.36%`.

Provider không được gọi trong audit.

### 4.3. Backend

Full regression:

```text
Tests run: 638
Failures: 0
Errors: 0
Skipped: 1
BUILD SUCCESS
```

Focused:

```text
Tests run: 50
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Focused coverage:

| Class | Line | Branch | Gate |
|---|---:|---:|---|
| `AdminReportAiResolutionServiceImpl` | `98.77%` | `88.43%` | `FAIL_LINE` |
| `AdminReportAiSemanticValidator` | `100%` | `88.49%` | `PASS` |
| `AdminReportAiPolicyCatalog` | `100%` | `100%` | `PASS` |

### 4.4. Workflow/secret

- canonical workflow nodes tồn tại và code source sync `true`;
- actual API key value không xuất hiện trong dossier;
- OpenAI-key regex scan pass;
- `git diff --check` không có whitespace error, chỉ có LF/CRLF warning.

## 5. Findings

### `S2DONE-ISSUE-002` — Missing S2-01 summary

Evidence dạng Markdown đầy đủ nhưng thiếu machine-readable `summary.json`.
Remediation chỉ được dựng từ evidence hiện hữu, không được thay đổi kết quả cũ.

### `S2DONE-ISSUE-003` — Coverage gap

Safety-relevant fail-closed lines `872–878` cùng long-content/image branches
chưa được cover. Cần bổ sung test hẹp và đạt line `100%`.

### `S2DONE-ISSUE-004` — Security harness drift

Security fixture thiếu current S2 report-claim/runtime fields, bị strict
boundary reject trước HMAC/replay assertions. Cần update fixture theo canonical
S2 request, không nới workflow schema.

## 6. Residual risks không tự sửa

- `REL.001` runtime-policy semantic mismatch vẫn bị loại khỏi S2-04 pilot và
  route remediation; không được prompt-override.
- S2-05 không có provider quality evidence và bị deferred.
- Production readiness vẫn `9/14 NOT_READY`.
- PostgreSQL integration test skip `1` giữ nguyên baseline; audit này không mở
  DB runtime gate.
- Không chạy Admin UI/browser E2E vì Sprint 2 không đổi FE và audit không
  publish runtime.

## 7. Remediation order

Gom ba finding vào một package hẹp:

```text
S2-DONE-FIX-01
```

Thứ tự:

1. cập nhật security harness fixture;
2. thêm focused coverage tests;
3. tạo missing S2-01 `summary.json`;
4. chạy exact failing security command;
5. chạy focused coverage gate;
6. chạy S2 contracts/adversarial/prompt/dataset;
7. chạy full Backend;
8. re-audit.

Audit finding review token:

```text
APPROVE_S2_DONE_AUDIT
```

Approval findings đã nhận. Token này chỉ xác nhận kết quả audit và thứ tự sửa,
không xác nhận ba finding đã được khắc phục.

Package remediation kế tiếp:

```text
IMPLEMENT_S2_DONE_FIX_01
```

Không dùng `APPROVE_S2_DONE` trước remediation và re-audit pass.
