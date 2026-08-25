# Security Design — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12A` |
| Trạng thái | `COMPLETED_SOURCE_STATIC_APPROVED` |

## Threats and controls

| Threat | Control |
|---|---|
| Non-admin invocation | Existing ADMIN route authorization + controller tests |
| Forged BE/n8n request | HMAC-SHA256 over canonical digest |
| Replay/cost abuse | timestamp, nonce TTL, correlation/idempotency |
| Prompt injection | data/system separation, candidate-rule allowlist, strict schema |
| Rule/action escalation | Backend semantic validator and A0 |
| Secret/PII leakage | minimization, sanitizer, no raw logs/API/persistence |
| Stale target | snapshot hash/updatedAt recheck |
| Malicious provider output | signature boundary, schema + semantic validation |

## Secret handling

- Environment/credential store only.
- No default real secret.
- Never return signature/API key to FE.
- Evidence masks direct identities and credentials.
- Rotate secret if disclosed; dual-secret rotation window requires explicit design before use.

## Limits

- request body, text, finding and reference counts capped;
- webhook timeout bounded;
- retry bounded;
- rate limit per Admin/report;
- provider call skipped for USER/PAGE/manual-local cases.

## Required negative tests

Invalid signature, stale timestamp, nonce replay, body tamper, prompt injection, oversized payload,
unknown Rule ID, raw-log scan and unauthorized Admin API.

## G0-12A implementation delta

- Backend signer/verifier: `AdminReportAiWebhookSigner`.
- Canonicalization: JCS/RFC 8785, không dùng ad-hoc key sorting.
- Freshness: `±120 giây`.
- Nonce TTL: `300 giây`.
- Request và response cùng dùng sáu `X-CafeStory-*` headers.
- n8n verify security trước provider và ký response sau normalize.
- Workflow export vẫn `active=false`; published runtime verification thuộc `G0-12D`.
- n8n nonce cache là single-instance workflow static data; distributed store là cải tiến bắt buộc khi scale.
- Detailed Design và evidence: `../09-sprints/g0-12a-security-boundary-detailed-design.vi.md`.
