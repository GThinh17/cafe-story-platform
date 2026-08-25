# Source Inventory — Backend, Config, Migration và Test

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-06B` |
| Approval | `APPROVE_G0-06B` |
| Trạng thái | `AUDITED_STATIC` |
| Checkout | `n8n/Ai-agent/fix-bug-report-admin` tại commit `68189a4` |
| Ngày audit | 2026-07-23 |
| Chế độ | Read-only; không chạy database, không gọi webhook, không sửa source |

`AUDITED_STATIC` chỉ có nghĩa là source hiện tại đã được đọc và lập inventory. Trạng thái này không chứng minh database runtime, n8n published workflow hoặc UI runtime đang đồng nhất.

## 2. Thành phần hiện có

### 2.1. API và quyền truy cập

| Thành phần | Source evidence | Current behavior |
|---|---|---|
| Report Admin API | `AdminContentReportController.java:33-104` | Base route `/api/admin/reports`; list/detail/status/resolve; tạo và đọc AI resolution; đọc/cancel auto-apply job |
| Authorization | `SecurityConfig.java:45-65` | `/api/admin/**` yêu cầu role `ADMIN` |
| AI eligibility | `AdminReportAiResolutionServiceImpl.java:51-52,262-273` | Chỉ report `OPEN` hoặc `REVIEWING`, target phải còn tồn tại |
| Create recommendation | `AdminReportAiResolutionServiceImpl.java:106-123` | BE build context, gọi n8n, validate response, persist; chỉ schedule khi request bật auto-apply |

### 2.2. Contract hiện tại

Request BE gửi sang n8n tại `AdminReportAiResolutionRequestDTO.java:16-38` gồm:

- report/target ID và type;
- reason code, label, severity và reporter description;
- target text và danh sách image URL;
- report status;
- moderation result gần nhất;
- số report đang mở trên cùng target.

Response n8n trả BE tại `AdminReportAiResolutionWebhookResponseDTO.java:13-29` gồm:

- `reportDecision`;
- `targetAction`;
- `confidenceScore`;
- `riskScore`;
- `labels`;
- `ruleCode`;
- `explanation`;
- `modelName`;
- `rawResponse`.

Contract này chưa có evidence reference, counter-evidence, missing evidence, evidence quality/sufficiency, violation likelihood, harm severity, action risk, snapshot/provenance, correlation/idempotency hoặc version metadata.

### 2.3. Decision/action hiện tại

| Report decision | Target | Action hợp lệ theo BE |
|---|---|---|
| `NEEDS_MANUAL_REVIEW` | Tất cả | `NONE` |
| `REJECT` | BLOG/COMMENT | `APPROVE` |
| `REJECT` | USER/CAFE_PAGE | `KEEP_ACTIVE` |
| `RESOLVE` | BLOG/COMMENT | `HIDE` hoặc `REMOVE` |
| `RESOLVE` | USER | `SUSPEND_USER` |
| `RESOLVE` | CAFE_PAGE | `SUSPEND_PAGE` |

Evidence: `AdminReportAiResolutionServiceImpl.java:275-314`.

BE là source of truth cho range score và combination decision/action, nhưng mới validate cấu trúc và range `0..100`; chưa validate semantics giữa score, evidence, uncertainty, rule và action (`AdminReportAiResolutionServiceImpl.java:226-300`).

### 2.4. Persistence và execution

| Thành phần | Current behavior | Evidence |
|---|---|---|
| Recommendation history | Lưu decision/action, hai score, labels, rule, explanation, model và raw response | `AdminReportAiResolution.java:61-96`; `V20260705_01__admin_report_ai_resolutions.sql:1-24` |
| Auto-apply request | Delay allowlist `15,30,60,120,360,720` phút | `AdminReportAiResolutionCreateRequestDTO.java:11-26` |
| Safety thresholds | Confidence ≥ 80; resolve risk ≥ 70; reject risk ≤ 30 | `AdminReportAiAutoApplyJobServiceImpl.java:44-46,381-401` |
| Active job uniqueness | Tối đa một job `SCHEDULED/APPLYING` cho mỗi report | `V20260706_01__admin_report_ai_auto_apply_jobs.sql:40-42` |
| Concurrent claiming | `FOR UPDATE SKIP LOCKED` | `AdminReportAiAutoApplyJobRepository.java:25-39` |
| Revalidation | Recheck report active, target ID, BLOG/COMMENT/page state và updated time | `AdminReportAiAutoApplyJobServiceImpl.java:188-275` |
| Mutation | Có thể hide/remove content, suspend user/page, rồi resolve/reject report | `AdminReportAiAutoApplyJobServiceImpl.java:202-207,287-344` |
| Worker | Chạy mỗi 5 giây sau initial delay 15 giây, batch mặc định 10 | `AdminReportAiAutoApplyJobWorker.java:12-31` |

Revalidation hiện thiếu freshness check tương đương cho USER và không re-evaluate evidence/policy/model/prompt version ngay trước mutation.

## 3. Report reason hiện được khai báo trong source

`ReportReasonDataInitializer.java:22-30` khai báo chín reason code:

1. `DISLIKE_CONTENT`;
2. `BULLYING_OR_UNWANTED_CONTACT`;
3. `SELF_HARM_OR_ABNORMAL_EATING`;
4. `VIOLENCE_HATE_OR_EXPLOITATION`;
5. `RESTRICTED_GOODS`;
6. `NUDITY_OR_SEXUAL_ACTIVITY`;
7. `SCAM_FRAUD_OR_SPAM`;
8. `FALSE_INFORMATION` — chỉ BLOG;
9. `INTELLECTUAL_PROPERTY`.

Entity có `code`, label/description tiếng Việt, optional target type, severity integer, requires-description, active và sort order (`ReportReason.java:35-75`).

Giới hạn quan trọng:

- initializer chỉ tạo record khi code chưa tồn tại, không update record đang có (`ReportReasonDataInitializer.java:40-51`);
- source không chứng minh dữ liệu runtime hiện có;
- severity `1..5` chưa có semantics/constraint được chuẩn hóa;
- một số reason gộp nhiều policy family khác nhau;
- reason chưa map tới rule ID/version hoặc burden of proof.

Vì vậy số lượng/value thực tế trong database vẫn là `UNKNOWN` cho tới `G0-06C`.

## 4. Finding register

| ID | Mức | Class | Finding và evidence | Liên hệ nền tảng đã duyệt |
|---|---|---|---|---|
| `SRC-001` | Thông tin | `CURRENT` | `/api/admin/**` yêu cầu ADMIN; AI chỉ nhận report OPEN/REVIEWING. `AdminContentReportController.java:33-104`; `SecurityConfig.java:62`; `AdminReportAiResolutionServiceImpl.java:262-273` | Phù hợp `HI1`, `HI2` |
| `SRC-002` | Thông tin | `CURRENT` | Ask AI tạo recommendation và history trước; không tự mutation nếu auto-apply không được request. `AdminReportAiResolutionServiceImpl.java:106-123` | Phù hợp `HI1`, `AR1` |
| `SRC-003` | Cao | `CONFLICT` | Reporter reason/severity/description, report count và AI moderation output cũ được đưa cùng một context không có trust/provenance label. `AdminReportAiResolutionServiceImpl.java:188-201,317-345` | Trái `E1`, `E6`, `H3`, `H4`, `R3` |
| `SRC-004` | Cao | `MISSING` | Contract không biểu diễn evidence/counter-evidence/missing evidence hoặc sufficiency theo finding/action. `AdminReportAiResolutionRequestDTO.java:16-38`; `AdminReportAiResolutionWebhookResponseDTO.java:13-29` | Thiếu `E3`, `E5`, `H7`, `H8`, `HI6`, `XA3`, `XA4` |
| `SRC-005` | Cao | `LEGACY` | Một `riskScore` generic cùng `confidenceScore` được dùng để validate/schedule; không có violation likelihood, harm severity và action risk riêng. `AdminReportAiResolutionWebhookResponseDTO.java:17-20`; `AdminReportAiAutoApplyJobServiceImpl.java:44-46,381-410` | Trái `U1`, `R1`, `R4`, `R8`, `R9` |
| `SRC-006` | **Nghiêm trọng** | `CONFLICT` | Auto-apply allow `REMOVE`, `SUSPEND_USER`, `SUSPEND_PAGE` khi qua ngưỡng score. `AdminReportAiAutoApplyJobServiceImpl.java:393-410` | Trái trực tiếp `HI3`, `AR2`, `AR3`, `AR4` |
| `SRC-007` | Cao | `CONFLICT` | Evidence sufficiency/critical missing evidence không nằm trong safety gate; confidence/risk có thể đủ để schedule destructive action. `AdminReportAiAutoApplyJobServiceImpl.java:381-410` | Trái `U3`, `U7`, `AR8` |
| `SRC-008` | Trung bình | `CURRENT` | Có delay, cancel, replace scheduled job, unique active job, atomic claim và state freshness checks. `AdminReportAiAutoApplyJobServiceImpl.java:78-123,171-275`; migration `V20260706...:40-42` | Phù hợp một phần `AR5`, `AR7`, `AR10` |
| `SRC-009` | Cao | `CONFLICT` | USER chỉ được recheck active; không kiểm tra profile/content changed-after-recommendation. Execution cũng không pin/revalidate policy/model/prompt. `AdminReportAiAutoApplyJobServiceImpl.java:224-240` | Chưa đạt `HI7`, `AR5`, `AR9` |
| `SRC-010` | Cao | `MISSING` | Recommendation và job không có correlation ID, idempotency key, snapshot hash, schema/policy/prompt/workflow version. Entity/migration chỉ có field legacy hiện tại. `AdminReportAiResolution.java:61-96`; `AdminReportAiAutoApplyJob.java:63-111` | Thiếu `AR6`, `AR9`, `XA6`, `XA9` |
| `SRC-011` | Trung bình | `LEGACY` | Raw model response được persist và trả thẳng qua Admin API; chưa thấy sanitize/retention/access-field policy trong feature. `AdminReportAiResolutionServiceImpl.java:219-222,405-423` | Chưa đạt `XA8` |
| `SRC-012` | Trung bình | `CONFLICT` | Khi JSON parse lỗi, BE log tối đa 300 ký tự response body; có rủi ro lộ nội dung/raw output vào log. `AdminReportAiResolutionServiceImpl.java:167-176` | Chưa đạt `XA8` |
| `SRC-013` | Trung bình | `MISSING` | BE gọi webhook không có application-level signature/HMAC/request timestamp trong request builder. `AdminReportAiResolutionServiceImpl.java:68-80,134-152` | Thiếu trust-boundary control và audit correlation |
| `SRC-014` | Trung bình | `MISSING` | Reason severity không có documented semantics, DB check constraint hoặc rule/version mapping. `ReportReason.java:48-68`; `ReportReasonDataInitializer.java:22-30` | Thiếu `R3`, `E5` |
| `SRC-015` | Trung bình | `UNKNOWN` | Initializer không update record có sẵn, nên source catalog có thể khác DB runtime. `ReportReasonDataInitializer.java:40-51` | Cần `G0-06C`; không được suy diễn |
| `SRC-016` | Thông tin | `CURRENT` | Unit/controller tests cover happy/error paths, target/action matrix, score range, terminal report, no immediate mutation, auto-apply/cancel/stale target. Test source: `AdminReportAiResolutionServiceImplTest.java:69-612`; `AdminReportAiAutoApplyJobServiceImplTest.java:90-534`; `AdminContentReportControllerTest.java:50-150` | Có regression baseline kỹ thuật |
| `SRC-017` | Cao | `MISSING` | Test hiện tại củng cố contract legacy nhưng chưa test evidence references, score semantics, version pinning, prompt injection, provenance hoặc destructive-action Human Review. Cùng các test source nêu trên | Cần test mới sau khi policy/rule được duyệt |

## 5. Kết luận source audit

Source hiện tại không chỉ là “skeleton UI”: đã có luồng recommendation, persistence, scheduling, cancel và mutation tương đối đầy đủ về mặt kỹ thuật. Tuy nhiên phần reasoning vẫn là skeleton chính sách/evidence:

- model quyết định trên context phẳng;
- contract chỉ có hai score mơ hồ;
- `ruleCode` là text do model trả, chưa được BE đối chiếu catalog/version;
- không chứng minh conclusion bằng evidence ID;
- auto-apply hiện rộng hơn baseline V2 đã duyệt.

Không dùng inventory này để kết luận dữ liệu DB hoặc runtime đang đúng. Bước kế tiếp là `G0-06C` read-only database audit.
