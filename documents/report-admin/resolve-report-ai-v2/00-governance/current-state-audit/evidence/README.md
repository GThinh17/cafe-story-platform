# Current-state Audit Evidence

Không tạo evidence giả trước khi chạy audit.

Mỗi audit run tạo thư mục Windows-safe timestamp:

```text
evidence/<timestamp>/
├── raw/
├── logs/
├── bao-cao-danh-gia.md
├── issue.md
├── fix-log.md
├── summary.json
└── workflow-improvement.md
```

## Sanitization bắt buộc

- Không lưu `Authorization`, cookie, token, password, secret hoặc API key.
- Mask email/phone trực tiếp khi không cần cho finding.
- Không copy raw env file.
- SQL evidence ưu tiên aggregate/schema, không lưu row chứa PII.
- n8n/raw response phải loại credential header và secret.

## Quy tắc báo cáo

- Static audit không được gọi là runtime pass.
- Missing environment phải ghi `PARTIAL` hoặc `BLOCKED`.
- `issue.md` ghi finding/blocker; `fix-log.md` phải ghi “không sửa” trong read-only gate.
- `summary.json` phải machine-readable và không chứa secret.

