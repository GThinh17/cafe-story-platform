# Detailed Design — G0-12-DONE-FIX-01

## 1. Mục tiêu và phạm vi

`DOD-FIX-01` biến ma trận ADV-001–ADV-012 từ tài liệu mô tả thành test có thể chạy
lại, đồng thời khóa hai lỗ hổng semantic được test phát hiện:

1. prior AI/derived signal hoặc reporter claim có thể bị dùng làm bằng chứng dương duy nhất;
2. một số trường Evidence ID chưa được kiểm tra allowlist.

Ngoài phạm vi: UI, database, publish n8n runtime, gọi OpenAI thật, production deploy,
`DOD-FIX-02`–`DOD-FIX-06`.

## 2. Luồng thiết kế Backend → n8n/OpenAI → Backend

### 2.1 Backend tạo evidence packet

Backend tạo Contract V2 gồm:

- `reportClaim`: dữ liệu claim không đáng tin;
- `targetSnapshot`: snapshot target;
- `evidence[]`: mỗi item có `evidenceId`, `sourceType`, `quality`, `availability`;
- `policyContext.candidateRules`: allowlist rule;
- `executionConstraints`: A0 recommendation-only.

Phân loại evidence liên quan guard:

| sourceType | Vai trò | Có thể làm evidence dương độc lập duy nhất? |
|---|---|---|
| `PLATFORM_RECORD` khả dụng | Quan sát từ hệ thống | Có, còn phụ thuộc rule/evidence sufficiency |
| `REPORTER_CLAIM` | Lời khai/route/report count | Không |
| `DERIVED_SIGNAL` | Kết quả AI/moderation trước đó | Không |
| evidence `UNUSABLE` hoặc không `AVAILABLE` | Thiếu/chưa đọc/chưa đánh giá | Không |

### 2.2 n8n build provider request

Node build:

1. xác minh HMAC/timestamp/nonce/Contract V2;
2. đặt policy ở system message;
3. serialize toàn bộ report/target/evidence vào user message với nhãn untrusted;
4. dùng strict JSON Schema;
5. không cấp tools;
6. chỉ cho action `KEEP_VISIBLE`, `HIDE`, `REMOVE`, `NO_ACTION`.

ADV-001, 002, 004, 005, 006, 007 và 009 xác minh boundary này không đổi khi
payload chứa prompt injection.

### 2.3 n8n normalize provider output

Node normalize dựng:

- `allowedRules` từ `candidateRules`;
- `evidenceById` từ packet Backend;
- allowlist cho mọi reference trong finding và `evidenceSummary`;
- predicate `isIndependentUsableEvidence`.

Nếu phát hiện:

- rule/evidence reference lạ → `UNKNOWN_RULE_OR_EVIDENCE_REFERENCE`;
- finding `SUPPORTED` không có independent usable evidence →
  `UNSUPPORTED_EVIDENCE_BASIS`;
- action ngoài decision matrix → `DECISION_ACTION_NOT_ALLOWED`;

thì response bị clamp:

```text
recommendationState = NEEDS_MANUAL_REVIEW
reportDecision      = NEEDS_MANUAL_REVIEW
targetAction        = NO_ACTION
findings            = []
```

### 2.4 Backend semantic boundary

`AdminReportAiSemanticValidator` lặp lại cùng policy ở trust boundary Backend:

1. map evidence theo ID;
2. kiểm tra finding rule/evidence references;
3. kiểm tra reference trong `evidenceSummary`;
4. kiểm tra evidence dương độc lập, khả dụng cho finding `SUPPORTED`;
5. clamp về manual/no-action nếu có blocker.

Guard kép ngăn việc bypass n8n hoặc response đã ký nhưng sai semantic lọt vào persistence.

## 3. Thiết kế executable suite

Suite đọc `jsCode` trực tiếp từ workflow JSON canonical và thực thi trong test harness
với:

- HMAC secret chỉ dùng cho test;
- nonce directory tạm và tự cleanup;
- packet tổng hợp, không chứa dữ liệu người dùng thật;
- provider response giả lập deterministic;
- không gọi network/OpenAI;
- không mutate database hoặc n8n runtime.

Fixture JSON giữ traceability một-một giữa `ADV-001` và `ADV-012`. Mỗi test kiểm tra
prompt boundary, output schema, allowlist, blocked reason, no-secret và immutability.

## 4. Backend regression và coverage

`AdminReportAiSemanticValidatorTest` bổ sung:

- mọi evidence-reference field phải theo Backend allowlist;
- `REPORTER_CLAIM`, `DERIVED_SIGNAL`, unavailable, unusable, missing source không đủ
  làm evidence dương duy nhất;
- finding `NOT_SUPPORTED` không bị ép phải có evidence dương.

Kết quả:

- focused Admin Report AI tests: `60/60 PASS`;
- full Backend regression: `621 tests`, `0 failures`, `0 errors`, `1 skipped`;
- changed class coverage:
  - line `104/104 = 100%`;
  - branch `114/118 = 96.61%`.

## 5. Trạng thái triển khai

- Source implementation: đã thực hiện và kiểm chứng local.
- n8n workflow export: đã cập nhật.
- Published n8n runtime: chưa cập nhật trong gate này.
- Production: chưa được phép.
- Gate: `COMPLETED_VERIFIED_APPROVED`.
- Approval đã nhận: `APPROVE_G0-12-DONE-FIX-01`.
