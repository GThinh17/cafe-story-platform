# Workflow Improvement — G0-12-DONE Re-audit

1. Mỗi remediation evidence nên ghi source SHA-256 ngay khi test chạy, không đợi final re-audit.
2. Historical evidence status và live approval ledger phải được tách rõ để tránh sửa snapshot cũ.
3. Definition of Done và production readiness phải luôn có hai counters riêng.
4. Final source commit nên được tạo sau approval và trước handoff/PR; thay đổi sau fingerprint phải
   trigger re-audit.
5. Một script machine-readable nên tự join DOD matrix, remediation summary và approval ledger trong
   lần cải tiến tiếp theo.
