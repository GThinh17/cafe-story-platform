# G0-12A Detailed Design — HMAC, Freshness và Replay Protection

## 1. Document control

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12A` |
| Trạng thái | `COMPLETED_SOURCE_STATIC_APPROVED` |
| Approval đã nhận | `APPROVE_G0-12A` |
| Contract | `Admin Report AI Contract 2.0` |
| Automation | `A0_RECOMMEND_ONLY` |
| Runtime authority | `false` |
| Evidence run | `09-sprints/evidence/2026-07-26T12-14-52-989+07-00/` |
| Source commit | `bcc7932` |

Trạng thái trên chỉ xác nhận source và export đã được kiểm thử cục bộ. Nó không đồng nghĩa workflow đã
được import/publish, provider đã được gọi thật hoặc production deployment đã được phép.

## 2. Bound

### 2.1. Mục tiêu

Bảo vệ trust boundary Backend ↔ n8n của Resolve Report with AI bằng:

1. authentication và integrity qua `HMAC-SHA256`;
2. canonical payload theo JCS/RFC 8785;
3. timestamp freshness `±120 giây`;
4. nonce replay TTL `300 giây`;
5. ký cả request Backend → n8n và response n8n → Backend;
6. fail closed trước provider/persistence khi security envelope không hợp lệ.

### 2.2. Trong phạm vi

- Spring Boot webhook signer/verifier và wiring vào `AdminReportAiResolutionServiceImpl`;
- cấu hình HMAC/freshness/nonce bằng environment;
- n8n export verify request trước node OpenAI và ký response;
- cấu hình cho phép module built-in `crypto` trong n8n Code node;
- unit/integration/static workflow tests;
- evidence, issue register và Detailed Design delta.

### 2.3. Ngoài phạm vi

- thay đổi Admin FE;
- migration/database;
- import, activate hoặc publish workflow n8n;
- provider call thật;
- login/E2E qua account Admin;
- distributed nonce store cho multi-instance;
- rotate secret thay người dùng;
- G0-12B coverage cho toàn bộ các class Sprint 1.

## 3. Security contract

### 3.1. Headers hai chiều

```text
X-CafeStory-Contract-Version
X-CafeStory-Correlation-Id
X-CafeStory-Timestamp
X-CafeStory-Nonce
X-CafeStory-Body-SHA256
X-CafeStory-Signature
```

### 3.2. Canonicalization và chữ ký

```text
canonicalBody = JCS(payload)
bodyHash      = lowercaseHex(SHA256(UTF8(canonicalBody)))
signatureInput = timestamp + "\n" + nonce + "\n" + bodyHash
signature     = lowercaseHex(HMAC-SHA256(secret, UTF8(signatureInput)))
```

- `timestamp` là Unix epoch seconds UTC.
- `nonce` do bên gửi tạo mới cho từng message.
- `contractVersion` và `correlationId` vừa nằm trong signed body, vừa được đối chiếu với header.
- Backend dùng thư viện `io.github.erdtman:java-json-canonicalization:1.1` để tránh sai khác số
  `1.0`, exponent và thứ tự object giữa Java và JavaScript.
- So sánh digest/signature dùng constant-time comparison.

## 4. Luồng xử lý FE → BE → n8n → BE → FE

```mermaid
sequenceDiagram
    actor Admin
    participant FE as "Admin FE"
    participant BE as "Spring Boot"
    participant SIG as "Webhook Signer"
    participant N8N as "n8n"
    participant LLM as "OpenAI"

    Admin->>FE: "Yêu cầu AI recommendation"
    FE->>BE: "POST report AI resolution"
    BE->>BE: "Authorize + build Contract V2/evidence"
    BE->>SIG: "JCS + hash + timestamp + nonce + HMAC"
    SIG-->>BE: "Canonical body + 6 security headers"
    BE->>N8N: "Signed request"
    N8N->>N8N: "Verify header/body hash/HMAC/freshness/replay"
    alt "Security invalid"
        N8N--xLLM: "Không gọi provider"
    else "Security valid"
        N8N->>LLM: "Pinned recommendation request"
        LLM-->>N8N: "Structured result"
        N8N->>N8N: "Normalize + JCS + response nonce + HMAC"
        N8N-->>BE: "Signed response"
        BE->>SIG: "Verify contract/correlation/hash/HMAC/freshness/replay"
        alt "Response security invalid"
            BE-->>FE: "502 fail closed; không persist recommendation"
        else "Response security valid"
            BE->>BE: "Schema + semantic validation + sanitized persistence"
            BE-->>FE: "Evidence-first recommendation"
        end
    end
```

FE không nhận secret, signature, nonce hoặc provider raw response. G0-12A không thay đổi UI contract.

## 5. Backend design

### 5.1. `AdminReportAiWebhookSigner`

Responsibility:

- lấy secret và security windows từ configuration;
- serialize request rồi canonicalize bằng JCS;
- phát sinh timestamp/UUID nonce;
- tạo body digest và HMAC;
- apply đúng sáu request headers;
- verify sáu response headers;
- đối chiếu contract/correlation;
- reject stale/future timestamp;
- reject malformed nonce/digest;
- reject body tamper/invalid signature;
- giữ response nonce cache thread-safe theo TTL và reject replay.

Backend nonce cache hiện là per-process. Multi-instance cần shared store ở hardening gate sau.

### 5.2. `AdminReportAiResolutionServiceImpl`

Thứ tự bắt buộc:

```text
build request
→ sign request
→ send canonical JSON
→ require 2xx and non-empty body
→ verify response security envelope
→ parse DTO
→ semantic validate
→ persist sanitized recommendation
```

Unsigned hoặc invalid response trả `502` với thông báo security riêng. Không parse/persist recommendation.

### 5.3. Configuration

```properties
admin.report.ai.hmac-secret=${ADMIN_REPORT_AI_HMAC_SECRET:}
admin.report.ai.timestamp-window-seconds=${ADMIN_REPORT_AI_TIMESTAMP_WINDOW_SECONDS:120}
admin.report.ai.nonce-ttl-seconds=${ADMIN_REPORT_AI_NONCE_TTL_SECONDS:300}
```

Secret không có default thật. Provider path fail closed nếu biến bị thiếu; local manual-only
`USER/CAFE_PAGE` vẫn không gọi provider.

## 6. n8n design

### 6.1. Request verifier

Node `Validate Contract V2 And Build Request` thực hiện security validation trước schema và trước node
`OpenAI Evidence Recommendation`:

1. require secret;
2. require sáu headers;
3. verify timestamp;
4. verify nonce format;
5. match contract/correlation;
6. JCS body hash;
7. constant-time HMAC verification;
8. purge nonce hết hạn và reject replay;
9. sau đó mới validate Contract V2 và build provider request.

### 6.2. Replay cache

- dùng `$getWorkflowStaticData('global')`;
- key là request nonce, value là epoch expiry;
- TTL `300 giây`;
- workflow phải active và chạy qua production webhook thì static data mới persist;
- chỉ bảo đảm theo thiết kế single-instance hiện tại;
- không được mô tả là distributed/strong replay protection dưới concurrency cao.

### 6.3. Signed response

Node `Validate And Normalize Recommendation V2` tạo object response trước, sau đó:

- canonicalize response;
- tạo timestamp và response nonce mới;
- hash và sign;
- chuyển response cùng metadata sang `Respond To Backend`.

Node response trả đúng sáu `X-CafeStory-*` headers. Workflow export vẫn `active=false`.

### 6.4. Runtime prerequisites

```text
N8N_BLOCK_ENV_ACCESS_IN_NODE=false
NODE_FUNCTION_ALLOW_BUILTIN=crypto
ADMIN_REPORT_AI_HMAC_SECRET=<same secret as Backend>
```

`ADMIN_REPORT_AI_HMAC_SECRET` hiện chưa được cấu hình trong `docker/.env`; runtime test phải dừng cho tới
khi người dùng cấu hình secret an toàn.

## 7. Failure behavior

| Failure | Nơi chặn | Provider call | Persist recommendation | Kết quả |
|---|---|---:|---:|---|
| Missing secret | BE/n8n | No | No | Fail closed |
| Missing headers | n8n/BE response | No/đã có response | No | Reject |
| Stale/future timestamp | Receiver | No/No persist | No | Reject |
| Replayed nonce | Receiver | No/No persist | No | Reject |
| Body tamper | Receiver | No/No persist | No | Reject |
| Invalid HMAC | Receiver | No/No persist | No | Reject |
| Contract/correlation mismatch | Receiver | No/No persist | No | Reject |
| Invalid JSON sau valid signature | Backend | Provider đã trả | No | `502` parse failure |

Không fallback sang unsigned mode để “giữ chức năng chạy”.

## 8. File mapping

| File | Vai trò |
|---|---|
| `pom.xml` | JCS RFC 8785 dependency |
| `AdminReportAiWebhookSigner.java` | Backend sign/verify/freshness/replay |
| `AdminReportAiResolutionServiceImpl.java` | Enforce signed request/response |
| `application.properties` | Secret/window/TTL settings |
| `AdminReportAiWebhookSignerTest.java` | Negative/positive/coverage tests |
| `AdminReportAiResolutionServiceImplTest.java` | HTTP boundary signed/unsigned tests |
| `cafestory-admin-report-ai-resolution-n8n-workflow.json` | Request verify + response sign |
| `docker-compose.n8n.yml` | Allow built-in `crypto` |
| `docker/tests/validate-admin-report-ai-security.mjs` | Executable workflow security contract test |

## 9. Verification result

| Acceptance criterion | Phương pháp | Kết quả |
|---|---|---|
| Backend signs request | Mock HTTP assertions | `PASS` |
| Backend verifies signed response | Service + signer tests | `PASS` |
| Unsigned/tampered/invalid signature fail closed | Negative tests | `PASS` |
| Freshness ±120 seconds | stale/future tests | `PASS` |
| Replay TTL 300 seconds | replay + expiry tests | `PASS` |
| JCS Java/JS numeric semantics | Java numeric vector + Node vector | `PASS` |
| Spring dependency wiring | `CafestoryApplicationTests` | `PASS` |
| Related Backend regression | 5 test classes, `45/45` | `PASS` |
| New signer coverage | JaCoCo line/branch | `100% / 85.71%` |
| n8n export security logic | Node executable validator | `PASS` |
| Compose syntax | `docker compose ... config --quiet` | `PASS` |
| Published webhook/runtime/provider | Docker daemon unavailable; secret missing | `NOT_RUN`, chuyển G0-12D |

## 10. Residual risks và cải tiến

### Phải xử lý trước G0-12D

1. Thu hồi/rotate provider key đã lộ trong output cục bộ.
2. Tạo `ADMIN_REPORT_AI_HMAC_SECRET` đủ mạnh và cấu hình cùng giá trị ở Backend/n8n.
3. Mở Docker Desktop.
4. Import export mới, giữ inactive khi review, rồi publish đúng version theo gate G0-12D.
5. Probe exact production webhook với valid/invalid/stale/replay requests.

### Nên cải tiến sau baseline

1. Thay n8n workflow static data bằng Redis/PostgreSQL atomic nonce store khi chạy multi-instance hoặc
   concurrency cao.
2. Thay Backend per-process response nonce cache bằng shared TTL store nếu scale ngang.
3. Thiết kế dual-secret rotation window có version/key ID.
4. Bổ sung rate limit và metrics riêng cho auth failure/replay nhưng không log payload/secret.

## 11. Gate result

```text
Source implementation: PASS
Focused Backend regression: PASS 45/45
New signer coverage: PASS 100% line / 85.71% branch
n8n executable static contract: PASS
Spring context: PASS
Published runtime: NOT RUN
Secret readiness: BLOCKED_USER_ACTION

G0-12A STATUS: COMPLETED_SOURCE_STATIC_APPROVED
NEXT TOKEN: IMPLEMENT_G0_12B
```
