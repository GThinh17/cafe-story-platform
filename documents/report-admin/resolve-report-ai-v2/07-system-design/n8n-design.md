# n8n Design — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| Workflow | `cafestory-admin-report-ai-resolution-v2` |
| Webhook path | `cafestory-admin-report-ai-resolution` |

## In-place workflow change

Không tạo workflow mới. Update export hiện hữu theo pipeline:

```text
Webhook
→ Verify Signature/Freshness/Replay
→ Validate Contract V2
→ Build Pinned OpenAI Request
→ OpenAI Responses API
→ Parse Strict Schema
→ Normalize/Fallback
→ Sign Response
→ Respond to Backend
```

## Pre-provider rejection

Không gọi OpenAI nếu:

- contract/correlation thiếu;
- HMAC/digest sai;
- timestamp ngoài ±120 giây;
- nonce đã thấy trong TTL;
- automation mode khác A0;
- target USER/CAFE_PAGE;
- candidate rule/evidence arrays invalid;
- payload vượt size limit.

## Prompt boundary

System section chứa:

- authority boundaries;
- supplied rule clauses;
- evidence definitions;
- decision/action compatibility;
- abstention rules;
- output schema.

User section chỉ chứa serialized untrusted data. Target/reporter text không được nối trực tiếp
vào system prompt hoặc JavaScript source.

## Normalization

- không synthesize missing Rule ID;
- không synthesize evidence;
- unknown enum/ref → manual fallback;
- strip fields ngoài schema;
- cap findings/evidence-reference counts;
- response không chứa OpenAI raw body/request ID nếu không cần audit;
- resolved model identity và prompt/workflow version vẫn được trả.

## Retry

- retry timeout, `429`, `5xx`;
- tối đa 2 retry sau lần đầu;
- validation, auth, schema và semantic error không retry;
- preserve correlation/idempotency key;
- no automatic fallback to a different model without versioned model-change gate.

## Runtime verification

- import validation;
- workflow active/readiness probe exact webhook;
- signed positive request;
- invalid signature, stale timestamp và replay negative requests;
- prompt-injection fixture;
- secret scan of execution/evidence export.
