# S2-04 — Issue log

## S2-04-ISSUE-001

| Trường | Giá trị |
|---|---|
| Classification | `CODE_BUG` |
| Severity | `HIGH` |
| Status | `OPEN_OUT_OF_S2_04_ACTIVATION_SCOPE` |
| Auto-fix | `NO` trong S2-04 |
| Component | Backend Runtime Rule Context catalog |
| Source | `AdminReportAiPolicyCatalog.ruleDefinitions()` |

### Hiện tượng

`CSR.REL.001` thuộc family `REL`, nhưng source hiện chỉ coi family `ROUTE` là
routing/non-violation. Vì vậy `REL.001` được phát hành thành:

- `ruleType=VIOLATION`;
- `materialForDecision=true`;
- có punitive candidate actions theo target type.

Điều này trái với dossier đã chốt: relevance chỉ là `NON_VIOLATION_SIGNAL`,
không được tự tạo punitive action.

### Ảnh hưởng

Nếu đưa `REL.001` vào prompt pilot, prompt phải chọn một trong hai cách đều sai:

1. tin runtime context và cho phép violation/punitive semantics; hoặc
2. ghi đè Backend bằng catalog song song trong n8n.

Cả hai vi phạm source-of-truth boundary.

### Xử lý trong S2-04

- Không che lỗi bằng prompt.
- Loại `REL.001` khỏi selected pilot.
- Dùng `CSR.HAR.001` để cover cả direct BLOG và context-dependent COMMENT.
- Không sửa Backend/catalog và không đổi fingerprint S2-03 trong package này.

### Hướng xử lý sau

Mở một remediation package riêng trước khi activate/benchmark `REL.001`: sửa
semantic catalog, thêm Backend regression test, phát hành lại signed Runtime
Rule Context và rebaseline dependency fingerprint có approval.

## S2-04-ISSUE-002

| Trường | Giá trị |
|---|---|
| Classification | `TEST_BUG` |
| Severity | `LOW` |
| Status | `RESOLVED_VERIFIED` |
| Auto-fix | `YES` |
| Component | S2-04 focused harness |

### Hiện tượng

Lần chạy đầu của `validate-admin-report-ai-prompt-pilot.mjs` fail tại assertion
kiểm tra mutation. Với mutation dạng chuỗi `priorAiOutput`, test dùng
`Object.values(attack)[0]`, nhận ký tự đầu tiên `"A"` thay vì marker đầy đủ.
System prompt hợp lệ có chữ `"A"`, nên test tạo false failure.

### Sửa hẹp

Mỗi mutation khai báo marker rõ ràng; assertion kiểm tra đúng marker đó. Không
đổi assembler, spec, fixture semantics hoặc production source.

Retest: untrusted system-prompt mutations `4/4 PASS`.

## S2-04-ISSUE-003

| Trường | Giá trị |
|---|---|
| Classification | `TEST_BUG` |
| Severity | `LOW` |
| Status | `RESOLVED_VERIFIED` |
| Auto-fix | `YES` |
| Component | S2-04 focused harness |

### Hiện tượng

Negative test “context expansion must fail” sửa nhánh COMMENT nhưng thực thi
fixture BLOG. Assembler chọn nhánh BLOG không bị sửa, nên đúng ra không reject.

### Sửa hẹp

Mutation được chuyển sang nhánh BLOG đang thực thi. Không đổi production source,
assembler hoặc rule semantics.

Retest: assembler negative guards `5/5 PASS`.

## S2-04-ISSUE-004

| Trường | Giá trị |
|---|---|
| Classification | `CODE_BUG` |
| Severity | `HIGH` |
| Status | `RESOLVED_VERIFIED` |
| Auto-fix | `YES` |
| Component | S2-04 prompt output clause |

### Hiện tượng

Candidate output instruction dùng tên `supportingEvidenceIds`, trong khi
canonical provider schema và runtime normalizer dùng field bắt buộc
`evidenceIds`. Nếu không sửa, candidate sẽ không tương thích output contract
khi đi vào provider benchmark.

### Sửa hẹp

- Đổi clause về canonical `evidenceIds`.
- Harness đọc canonical provider schema và assert field `evidenceIds` vẫn tồn
  tại/bắt buộc, đồng thời cấm `supportingEvidenceIds`.
- Pin provider schema vào candidate dependency manifest.

Retest: focused prompt gate `PASS`; canonical `evidenceIds` parity `PASS`.
