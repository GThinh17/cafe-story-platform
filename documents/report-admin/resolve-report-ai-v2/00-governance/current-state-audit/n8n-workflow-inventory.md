# n8n Workflow Inventory

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-06B` |
| Approval | `APPROVE_G0-06B` |
| Trạng thái | `AUDITED_STATIC` |
| Artifact | `docker/cafestory-admin-report-ai-resolution-n8n-workflow.json` |
| Workflow ID trong export | `cafestory-admin-report-ai-resolution-v2` |
| Ngày audit | 2026-07-23 |
| Chế độ | Parse/read static export; không import, activate, publish hoặc call webhook |

## 2. Topology hiện tại

Export parse thành công và có năm node:

1. `Admin Report AI Resolution Webhook`;
2. `Validate And Build Decision Request`;
3. `OpenAI Admin Decision`;
4. `Normalize CafeStory Admin Recommendation`;
5. `Respond To Backend`.

Webhook:

- method `POST`;
- path `cafestory-admin-report-ai-resolution`;
- response qua dedicated response node.

Evidence: workflow JSON `:1-20,83-97,100-144`.

Export có `"active": false` tại dòng 146. Đây chỉ là trạng thái artifact; không dùng nó để kết luận workflow runtime đang inactive hoặc active.

## 3. Prompt/input/output hiện tại

### Input

Node build context nhận:

- reporter reason code/label/severity/description;
- target content text;
- tối đa mười image URL;
- report status;
- previous moderation output;
- open-report count.

Tất cả được stringify thành một `input_text` user message. Image URL không được gửi dưới dạng image input, nên workflow không có bằng chứng rằng model đã nhìn thấy pixel/OCR/nội dung ảnh.

### Policy prompt

Prompt hiện có bảy câu rule tổng quát:

- AI chỉ recommend;
- decision/action enum;
- mapping low/unclear/clear violation cho từng target;
- `REMOVE` dành cho severe content;
- report count và existing moderation result có thể tăng risk.

Đây là prompt policy embedded trong workflow, không phải policy catalog có version.

### Structured output

JSON Schema strict yêu cầu decision/action, confidence/risk, labels, rule code và explanation. Normalize:

- parse output text;
- validate score `0..100`;
- tương thích legacy bằng cách đổi `(0,1]` thành phần trăm;
- fallback combination sai về `NEEDS_MANUAL_REVIEW + NONE`;
- trả model name và raw source decision.

Evidence: workflow JSON `:23,72`.

## 4. Finding register

| ID | Mức | Class | Finding và evidence | Liên hệ nền tảng đã duyệt |
|---|---|---|---|---|
| `N8N-001` | Thông tin | `CURRENT` | Workflow có topology tuyến tính rõ, dùng strict JSON Schema và không trực tiếp gọi API mutation của CafeStory. JSON `:1-144` | Phù hợp `HI1` |
| `N8N-002` | Trung bình | `UNKNOWN` | Export ghi `active:false`, nhưng static artifact không chứng minh trạng thái published/active runtime. JSON `:146` | Runtime probe cần approval riêng |
| `N8N-003` | Cao | `CONFLICT` | Prompt nói report count và previous moderation result “can increase risk”; đây là claim/derived AI output, không phải evidence violation độc lập. JSON `:23` | Trái `E1`, `E6`, `H3`, `H4`, `H5` |
| `N8N-004` | Cao | `MISSING` | Schema không có evidence reference, counter-evidence, missing evidence, quality/sufficiency hoặc critical uncertainty. JSON `:23` | Thiếu `E3`, `E5`, `H7`, `H8`, `HI6`, `XA3`, `XA4` |
| `N8N-005` | Cao | `MISSING` | `imageUrls` chỉ nằm trong JSON text; không có image input, fetch validation, OCR hoặc unreadable-image abstention. JSON `:23` | Critical missing evidence phải theo `H8`, `U3`, `HI4`, `AR8` |
| `N8N-006` | Cao | `LEGACY` | Prompt/schema chỉ có `confidenceScore` và generic `riskScore`; normalize còn hỗ trợ score `0..1`. JSON `:23,72` | Trái canonical `U1`, `R1`, `R8` |
| `N8N-007` | Cao | `MISSING` | Không pin/lưu schema, policy, prompt và workflow version trong request/response. Model chỉ lấy từ env/default. JSON `:23,72` | Thiếu `AR9`, `XA6` |
| `N8N-008` | Cao | `MISSING` | Webhook node không khai báo auth/signature/timestamp/replay protection; BE cũng không gửi signature. JSON `:5-20`; BE `AdminReportAiResolutionServiceImpl.java:134-152` | Trust boundary chưa được bảo vệ ở application layer |
| `N8N-009` | Trung bình | `MISSING` | Không có correlation ID/idempotency key xuyên BE–n8n–OpenAI–job. JSON `:23,72` | Thiếu `AR6`, `XA9` |
| `N8N-010` | Trung bình | `CURRENT` | Combination sai được normalize về `NEEDS_MANUAL_REVIEW + NONE`, và BE còn validate lại. JSON `:72`; BE `AdminReportAiResolutionServiceImpl.java:226-291` | Defense-in-depth phù hợp `HI2` |
| `N8N-011` | Trung bình | `CONFLICT` | HTTP node bật retry 3 lần nhưng không thấy classifier chỉ retry transient failure. JSON `:34-68` | Chưa chứng minh đạt `AR11` |
| `N8N-012` | Trung bình | `MISSING` | Không có structured operational error contract/branch; parse/schema/provider failure không được phân loại riêng với content uncertainty. JSON topology `:100-144` | Thiếu `U8` |
| `N8N-013` | Trung bình | `MISSING` | Không có prompt-injection-specific instruction, input trust labels hoặc policy rule lookup do BE cấp. JSON `:23` | Thiếu defense cho untrusted target/reporter text |
| `N8N-014` | Trung bình | `LEGACY` | `ruleCode` do model tự sinh/fallback text; BE chỉ kiểm tra non-blank explanation/model, không validate rule existence/version. JSON `:23,72`; BE `AdminReportAiResolutionServiceImpl.java:226-260` | Trái `E5`, `XA3`, `XA12` |

## 5. Kết luận workflow

Workflow hiện tại làm tốt vai trò orchestrator mỏng và có structured output/fallback. Tuy nhiên policy/evidence layer vẫn là prompt-level skeleton:

- policy nhúng trực tiếp và quá tổng quát;
- không có rule catalog/version;
- không có evidence object;
- không thực sự xử lý ảnh;
- output score chưa có semantics chuẩn;
- thiếu request authentication và audit correlation.

Không thay đổi workflow trong gate này. Runtime readiness/activation vẫn `UNKNOWN`.
