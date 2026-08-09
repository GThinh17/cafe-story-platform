# S2-05 — Issue log

## S2-05-ISSUE-001

| Trường | Giá trị |
|---|---|
| Classification | `CODE_BUG` |
| Severity | `HIGH` |
| Status | `RESOLVED_VERIFIED` |
| Owner | benchmark contract |
| Auto-fix | `YES` |

### Hiện tượng

S2-04 prompt candidate yêu cầu provider chỉ trả per-rule finding và cấm final
decision/action. Canonical S2-02 provider schema lại bắt buộc
`recommendationState`, `reportDecision`, `targetAction` cùng các aggregate
field. Dùng schema đó cho S2-05 sẽ tạo instruction/schema contradiction.

### Xử lý

Tạo output schema chỉ dành cho evaluation benchmark, gồm metadata evaluation và
exactly one per-rule finding. Không thay canonical runtime schema.

Focused schema/negative gate đã pass; canonical runtime schema và published
workflow không bị thay đổi.

## S2-05-ISSUE-002

| Trường | Giá trị |
|---|---|
| Classification | `CONFIG_ENV` |
| Severity | `LOW` |
| Status | `OPEN_NON_BLOCKING` |
| Owner | local Codex/OpenAI Docs integration |
| Auto-fix | `NO` |

### Hiện tượng

Official OpenAI Developer Docs MCP chưa callable trong session. Lệnh cài
`codex mcp add openaiDeveloperDocs --url https://developers.openai.com/mcp`
bị WindowsApps từ chối `Access is denied`.

### Xử lý

Dùng fallback chỉ trên official `developers.openai.com`; không sửa repo hoặc
provider code để che lỗi. User có thể restart/cấu hình Docs MCP sau, nhưng lỗi
này không chặn API benchmark.

## S2-05-ISSUE-003

| Trường | Giá trị |
|---|---|
| Classification | `TEST_DATA` |
| Severity | `HIGH` |
| Status | `OPEN_EXPECTED_LIMITATION` |
| Owner | evaluation governance/business review |
| Auto-fix | `NO` |

### Hiện tượng

HAR pilot slice có ba hard-safety invariant, một provisional semantic case và
một open disagreement. Rubric đặt provider quality denominator bằng `0` và cấm
dùng provisional/disagreement như ground truth.

### Ảnh hưởng

S2-05 không được báo accuracy/precision/recall/F1, calibration hoặc chọn model
winner theo semantic quality. Có thể đo contract/safety/stability/latency/cost
và báo semantic outputs để business review sau.

## S2-05-ISSUE-004

| Trường | Giá trị |
|---|---|
| Classification | `TEST_CODE` |
| Severity | `LOW` |
| Status | `RESOLVED_VERIFIED` |
| Owner | benchmark focused gate |
| Auto-fix | `YES` |

### Hiện tượng

Test ban đầu cấm chuỗi `targetAction` trong toàn bộ provider request. Điều này
tạo false positive vì trusted system prompt phải có câu phủ định
`Do not return targetAction`.

### Xử lý

Gate được sửa để kiểm tra đúng authority boundary: provider output schema không
có property `targetAction`, trong khi trusted prompt vẫn giữ câu lệnh cấm.
Focused gate và coverage đã pass lại.

## S2-05-ISSUE-005

| Trường | Giá trị |
|---|---|
| Classification | `CODE_BUG` |
| Severity | `HIGH` |
| Status | `FIXED_STATIC_VERIFIED_PROVIDER_RETEST_PENDING` |
| Owner | Responses API structured-output adapter |
| Auto-fix | `YES_LOCAL`; `NO_EXTERNAL_RETEST_WITHOUT_NEW_TOKEN` |

### Hiện tượng

Lần execute đầu gửi internal Draft 2020-12 schema trực tiếp cho Responses API.
Cả `36/36` request bị từ chối trước inference với safe code
`invalid_json_schema`; actual provider cost ghi nhận `0 USD`.

Internal schema hợp lệ với Ajv nhưng có keyword không thuộc phần schema nên gửi
cho Structured Outputs (`const`, `minLength`, `maxLength`, `uniqueItems` và
metadata root). Internal validation và provider-compatible schema là hai lớp
khác nhau.

### Xử lý

Thêm deterministic adapter:

- bỏ metadata/keyword không được gửi cho Structured Outputs;
- chuyển `const` thành single-value `enum`;
- giữ internal strict schema để post-validate đầy đủ sau response;
- thêm negative test bảo đảm schema gửi provider không còn keyword đã loại;
- đối chiếu yêu cầu official: root object, mọi field required,
  `additionalProperties=false`.

Static/coverage gate và dry-run đã pass. Provider retest chưa chạy vì Bound
`36` request đã dùng hết; không tự mở rộng external-call scope.

## S2-05-ISSUE-006

| Trường | Giá trị |
|---|---|
| Classification | `CODE_BUG` |
| Severity | `HIGH` |
| Status | `RESOLVED_STATIC_VERIFIED` |
| Owner | benchmark stop policy |
| Auto-fix | `YES` |

### Hiện tượng

Sau `invalid_json_schema` đầu tiên, runner vẫn gửi thêm 35 request có cùng lỗi
cấu hình toàn suite. Budget không bị tính phí nhưng quota/thời gian bị lãng phí.

### Xử lý

Thêm circuit breaker có ba scope:

- `GLOBAL`: invalid schema, invalid key hoặc max-provider-call guard;
- `MODEL`: model access/not-found/invalid-model;
- `NONE`: lỗi riêng có thể tiếp tục theo retry policy.

Unit/focused assertions cho cả ba scope đã pass. Chưa gọi provider lại trong
Bound hiện tại.

## S2-05-ISSUE-007

| Trường | Giá trị |
|---|---|
| Classification | `EVIDENCE_INTEGRITY` |
| Severity | `MEDIUM` |
| Status | `RESOLVED_STATIC_VERIFIED` |
| Owner | benchmark evidence writer |
| Auto-fix | `YES` |

### Hiện tượng

Default output của runner sau fix vẫn trỏ tới artifact lần chạy thất bại; retest
có thể ghi đè bằng chứng `36/36 invalid_json_schema`.

### Xử lý

Giữ nguyên `raw/provider-benchmark-results.json` như failed-run evidence và đổi
default retest output sang
`raw/provider-benchmark-retest-01-results.json`. Dry-run xác nhận path mới.
