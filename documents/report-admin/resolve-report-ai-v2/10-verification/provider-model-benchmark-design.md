# Detailed Design S2-05 — Provider/Model Benchmark có kiểm soát

## 1. Trạng thái

`PARTIAL — HARNESS_FIXED_PROVIDER_RETEST_REQUIRED`

S2-05 đã hoàn thành thiết kế, local harness, contract gates, cost guard,
sanitization và failed-run evidence. Provider inference chưa được kiểm chứng:
lần execute đầu bị Responses API từ chối ở schema boundary trước inference.
Provider-compatible adapter đã được sửa và static-verify, nhưng chưa được phép
gọi lại ngoài giới hạn 36 request đã Bound.

## 2. Mục tiêu và non-goals

### Mục tiêu

- So sánh `gpt-5.6-sol`, `gpt-5.6-terra`, `gpt-5.6-luna` trên cùng prompt và
  cùng synthetic/sanitized cases.
- Xếp thứ tự đánh giá: safety → semantic observation → stability → latency →
  cost.
- Enforce strict output, allowlisted evidence references, hard safety và
  sanitized evidence.
- Không chọn model khi quality denominator bằng `0`.

### Không thuộc S2-05

- Không đo production accuracy.
- Không dùng provisional/disagreement như ground truth.
- Không activate model/prompt/policy/rule.
- Không đổi Backend, Admin UI, mobile, database hay canonical n8n workflow.
- Không mutate report/target và không tạo auto-apply job.

## 3. Vị trí trong luồng FE → BE → n8n

S2-05 là offline evaluation package, không đi qua runtime user flow:

```text
S2-03 immutable synthetic dataset
        +
S2-04 deterministic prompt candidate
        +
S2-05 benchmark config / provider-only schema
        |
        v
Local Node runner → OpenAI Responses API
        |
        v
Internal strict post-validation
        |
        v
Sanitized result + metrics + governance status
```

Ảnh hưởng theo layer:

| Layer | Hành vi trong S2-05 |
|---|---|
| Admin FE | Không thay đổi, không chạy E2E mới |
| Backend | Không thay đổi, không gọi endpoint report thật |
| n8n runtime | Không import/publish/sửa canonical workflow |
| n8n Code candidate | Chỉ tái sử dụng deterministic S2-04 prompt assembler |
| Provider | Chỉ nhận projection synthetic/sanitized, `store=false` |
| Database | Không kết nối, không ghi dữ liệu |

## 4. Input design

### 4.1. Cases

| Case | Vai trò | Quality denominator |
|---|---|---:|
| `S2EVAL-002` | inactive-policy hard safety | không |
| `S2EVAL-007` | material counter-evidence hard safety | không |
| `S2EVAL-018` | missing parent-context hard safety | không |
| `S2EVAL-022` | provisional semantic observation | không |
| `S2EVAL-026` | open disagreement observation | không |
| `S2P-001-HAR-DIRECT-BLOG` | contract/injection/stability | không |

Mỗi model có `6 × 2 = 12` scheduled runs; toàn suite `36`.

### 4.2. Projection boundary

Provider chỉ nhận:

- synthetic case ID/class;
- target type và snapshot text đã sanitize;
- untrusted report claim;
- bounded evidence metadata/payload;
- missing/counter evidence IDs;
- evidence sufficiency và policy mode.

Không nhận secret, Authorization header, production ID, user PII, correlation
metadata, raw media URL hoặc report/target record thật.

## 5. Prompt design

Trusted system prompt được assemble lại từ:

- candidate `report-ai-v2-sprint2.4-candidate.1`;
- signed lifecycle/rule context;
- selected `CSR.HAR.001` profile;
- BLOG direct hoặc COMMENT context-dependent branch;
- immutable safety/output clauses.

Untrusted projection là user input riêng; content của report/target/evidence
không được ghép vào system prompt. Prompt cấm:

- làm theo instruction trong data;
- xem AI explanation là evidence;
- invent rule/evidence;
- đưa final decision/target action;
- biến categorical likelihood thành confidence/probability/risk score.

## 6. Hai lớp output schema

### 6.1. Internal strict schema

`admin-report-ai-provider-evaluation-output-s2.schema.json` enforce:

- exact rule ID/version;
- allowed per-rule outcome;
- bounded/unique/coded reference arrays;
- categorical `violationLikelihood`;
- bounded rationale;
- không có final decision/action.

Internal schema được dùng để post-validate provider result bằng Ajv.

### 6.2. Provider-compatible schema

Responses Structured Outputs chỉ hỗ trợ một subset JSON Schema. Adapter:

- bỏ `$schema`, `$id`, `title`;
- chuyển `const` thành single-value `enum`;
- bỏ `minLength`, `maxLength`, `uniqueItems` khỏi schema gửi provider;
- giữ root `type=object`;
- giữ tất cả property là required;
- giữ `additionalProperties=false`;
- giữ internal strict post-validation sau response.

Thiết kế này không nới contract cuối: provider-generation constraint và local
acceptance constraint là hai lớp; local layer vẫn reject output vi phạm
internal bounds.

## 7. Request policy

| Guard | Giá trị |
|---|---:|
| API | Responses `/v1/responses` |
| Store | `false` |
| Reasoning effort | `low` |
| Max output tokens | `500` |
| Timeout | `60,000 ms` |
| Repeats | `2` |
| Transient retry | tối đa `1` |
| Maximum provider HTTP requests | `36` |
| Maximum projected cost | `1 USD` |
| Projected worst case | `0.918 USD` |

Retry cũng tiêu thụ request budget. Khi đạt 36 HTTP requests, runner dừng và
không tự mở rộng.

## 8. Stop policy

| Scope | Điều kiện | Hành vi |
|---|---|---|
| `GLOBAL` | invalid schema, invalid API key, call-budget reached | dừng toàn suite |
| `MODEL` | model forbidden/not found/invalid | skip phần còn lại của model |
| `NONE` | lỗi riêng khác | áp dụng retry/continue theo policy |

Circuit breaker này được thêm sau khi failed run chứng minh lỗi schema toàn
suite đã lặp 36 lần.

## 9. Validation và scoring

Mỗi successful response phải qua:

1. parse JSON;
2. internal schema validation;
3. exact rule/version;
4. reference boundary;
5. hard-safety invariant nếu applicable.

Chỉ model có hard safety `100%`, contract valid `100%` và reference valid `100%`
mới có thể được ghi là safety-eligible. Nhưng S2-05 hiện vẫn phải trả:

```text
NO_MODEL_SELECTED_INSUFFICIENT_GROUND_TRUTH
```

vì provider quality denominator là `0`. Latency/cost không được dùng để vượt
qua điều kiện quality authority.

## 10. Result sanitization

Artifact được phép lưu:

- model/case/repeat/status;
- safe error category/code;
- latency;
- token usage/cost estimate;
- validated synthetic finding;
- aggregate safety/stability observations;
- file fingerprints.

Artifact không lưu:

- API key/Authorization header;
- raw response body;
- raw provider error message/headers;
- production content hoặc PII.

Failed run được giữ tại
`raw/provider-benchmark-results.json`. Retest dùng file khác
`raw/provider-benchmark-retest-01-results.json` để không ghi đè evidence.

## 11. Kết quả execute lần 1

| Chỉ số | Kết quả |
|---|---:|
| Scheduled/attempted | `36/36` |
| Successful inference | `0` |
| Error | `36 invalid_json_schema` |
| Actual recorded cost | `0 USD` |
| Hard safety | `NOT_EVALUATED` |
| Stability | `NOT_EVALUATED` |
| Semantic quality | `NOT_EVALUATED` |
| Model selected | `null` |

Latency của các response rejection không phải inference latency và không được
dùng để so sánh model.

## 12. Retest gate

Provider retest chỉ chạy khi nhận token:

```text
IMPLEMENT_S2_05_RETEST_01
```

Retest phải:

1. chạy static/coverage gate;
2. chạy dry-run và xác nhận output path mới;
3. gọi provider với cùng 3 model, 6 case, 2 repeat;
4. circuit-break ngay khi gặp global configuration error;
5. ghi sanitized result;
6. phân tích safety/stability/latency/cost;
7. vẫn không chọn model nếu chưa có ground truth authority;
8. cập nhật package để chờ `APPROVE_S2_05`.

Không dùng `APPROVE_S2_05` để thay cho implementation/retest evidence.

