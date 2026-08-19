# AI Report language routing runtime verification

- Timestamp (UTC): `2026-08-15T08:11:16.314Z`
- Runtime endpoint: `http://127.0.0.1:5678/webhook/cafestory-admin-report-ai-resolution`
- Workflow ID: `cafestory-admin-report-ai-resolution-v2`
- Provider: real n8n workflow and real OpenAI response
- Contract: exact Admin Report AI Contract `2.0`
- Result: `PASS` for all three required language cases

## Cases

1. English description + English reason returned English `explanation` and `findings[].rationale`.
   - Finding: `CSR.INT.003`
   - Evidence: `EV-TARGET-CONTENT`
   - Concrete indicators: `cafe-reward.example`, free voucher, and an OTP request.
2. Vietnamese description + Vietnamese reason on a COMMENT returned Vietnamese `explanation` and `findings[].rationale`.
   - Finding: `CSR.INT.003`
   - Evidence: `EV-TARGET-CONTENT`
   - Concrete indicator: `gửi mã OTP để nhận ngay`.
3. English description + Vietnamese reason snapshot returned English `explanation` and `findings[].rationale`.
   - Finding: `CSR.INT.003`
   - Evidence: `EV-TARGET-CONTENT`
   - Concrete indicators: account password, OTP, and a guaranteed reward.

Every response passed provider JSON schema validation and signed-response verification. Findings referenced only supplied Evidence IDs. Score fields and execution fields were absent, and no response claimed that the AI mutated a target or report.

The test printed no HMAC or OpenAI secret values.
