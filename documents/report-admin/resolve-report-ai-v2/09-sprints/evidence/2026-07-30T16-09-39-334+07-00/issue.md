# S2-DONE-AUDIT — Issue log

## S2DONE-ISSUE-001

| Trường | Giá trị |
|---|---|
| Classification | `TEST_BUG` |
| Severity | `LOW` |
| Status | `RESOLVED_VERIFIED` |
| Owner | audit PowerShell command |
| Auto-fix | `YES` |

### Evidence

Approval/evidence audit command fail trước execution:

```text
ParserError: An empty pipe element is not allowed.
```

### Ảnh hưởng và xử lý

Không source/test nào đã chạy hoặc bị thay đổi. Sửa command bằng cách thu
`foreach` output vào biến trước khi pipe sang `Format-Table`, sau đó chạy lại.

Kết quả chạy lại: S2-01–S2-05 đều có evidence/deliverable tồn tại; S2-01–S2-04
có approval, S2-05 không có approval và được audit route sang deferred.

## S2DONE-ISSUE-002

| Trường | Giá trị |
|---|---|
| Classification | `TEST_DATA` |
| Severity | `MEDIUM` |
| Status | `OPEN_REMEDIATION_REQUIRED` |
| Owner | S2-01 evidence package |
| Auto-fix | `NO` trong audit |

### Evidence

Directory
`09-sprints/evidence/2026-07-29T21-05-20-543+07-00/` tồn tại và có:

- `bao-cao-danh-gia.md`;
- `test-log.md`;
- `coverage.md`;
- `issue.md`;
- `changed-files.md`;
- `workflow-improvement.md`.

Nhưng thiếu `summary.json`, trái với machine-readable evidence package quy
định trong workflow.

### Ảnh hưởng

Approval và test result S2-01 vẫn trace được từ report, status và approval
ledger; không phải mất toàn bộ evidence. Tuy nhiên technical DoD chưa thể được
đánh dấu sạch hoàn toàn khi một required artifact bị thiếu.

### Đề xuất

Mở remediation hẹp sau audit để tạo `summary.json` từ evidence S2-01 hiện hữu,
validate JSON và cross-check với status; không thay source/test result.

## S2DONE-ISSUE-003

| Trường | Giá trị |
|---|---|
| Classification | `TEST_BUG` |
| Severity | `HIGH` |
| Status | `OPEN_REMEDIATION_REQUIRED` |
| Owner | `AdminReportAiResolutionServiceImplTest` |
| Auto-fix | `YES` sau audit; `NO` trong audit token |

### Evidence

Focused command:

```powershell
mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test
```

Test `50/50 PASS`, nhưng JaCoCo hiện tại:

| Class | Line | Branch |
|---|---:|---:|
| `AdminReportAiResolutionServiceImpl` | `564/571 = 98.77%` | `214/242 = 88.43%` |
| `AdminReportAiSemanticValidator` | `434/434 = 100%` | `446/504 = 88.49%` |
| `AdminReportAiPolicyCatalog` | `109/109 = 100%` | `25/25 = 100%` |

Gate yêu cầu changed production line `100%`, branch `>=85%`.

Các source line chưa cover gồm:

- `512`: long-content availability branch;
- `636`: `INAPPROPRIATE_IMAGE` reason branch;
- `872–878`: fail-closed clamp khi decision/action không được phép.

### Ảnh hưởng

Behavior hiện không fail test, branch gate đạt; nhưng safety-relevant fail-closed
path chưa có executable coverage đầy đủ và line gate không đạt. Không được đánh
dấu Sprint 2 audit `PASS`.

### Đề xuất

Mở remediation test-only hẹp:

1. thêm case cho long content;
2. thêm case cho inappropriate-image requirement;
3. thêm case chứng minh invalid decision/action bị clamp manual/no-action;
4. chạy focused, parse JaCoCo, rồi full Backend regression.

## S2DONE-ISSUE-004

| Trường | Giá trị |
|---|---|
| Classification | `TEST_BUG` |
| Severity | `HIGH` |
| Status | `OPEN_REMEDIATION_REQUIRED` |
| Owner | `docker/tests/validate-admin-report-ai-security.mjs` |
| Auto-fix | `YES` sau audit; `NO` trong audit token |

### Evidence

Command:

```powershell
node docker/tests/validate-admin-report-ai-security.mjs
```

Actual:

```text
Error: Report claim is missing required properties:
status,reasonCode,reasonCatalogVersion,description,trustLevel
```

### Phân loại

Security harness vẫn dựng request fixture theo Contract V2 cũ, trong khi current
S2 boundary yêu cầu Runtime Rule Context/report-claim fields đầy đủ. Workflow
reject fixture trước khi HMAC/replay/no-authority assertions chạy.

Đây là harness drift (`TEST_BUG`), chưa phải evidence security production code
hỏng. Tuy nhiên security static regression hiện không executable nên audit
không thể `PASS`.

### Đề xuất

Remediation phải cập nhật fixture theo canonical S2 runtime request, dùng
allowlisted/synthetic values, sau đó chạy:

1. exact failing security command;
2. S2 contracts/adversarial;
3. focused Backend;
4. full Backend regression.
