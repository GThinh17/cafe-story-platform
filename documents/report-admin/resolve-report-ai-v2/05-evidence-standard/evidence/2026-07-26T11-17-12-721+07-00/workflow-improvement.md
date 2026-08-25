# Workflow improvement — G0-12M-08

- Cross-review phải scan example values, không chỉ heading/status; legacy alias nằm trong approved docs
  vẫn có thể dẫn implementation sai.
- Privacy review phải kiểm tra nested generic payload, không chỉ typed root fields.
- Lifecycle enum nên được generated từ một registry artifact thay vì lặp trong policy docs/schema.
- Contract review cần phân biệt `design approved`, `lifecycle approved`, `runtime active` và
  `production ready`.
- G0-12A nên reuse recursive forbidden-field fixture và bổ sung HMAC/timestamp/nonce negative cases.
