# S2-03 — Fix log

| Issue | Hành động | Retest | Trạng thái |
|---|---|---|---|
| `S2-03-ISSUE-001` | Chạy lại Maven từ Backend module; không sửa source | `50/50 PASS` | `RESOLVED` |
| `S2-03-ISSUE-002` | Chỉ bắt `requiredBlockedReasons` khi oracle kỳ vọng `NEEDS_MANUAL_REVIEW` | Metadata-only `PASS`; negative guards `6/6 PASS` | `RESOLVED` |
| `S2-03-ISSUE-003` | Gọi `mvn.cmd` qua Windows `ComSpec` bằng đối số cố định | Full harness `PASS`; Backend `50/50 PASS` | `RESOLVED` |
| `S2-03-ISSUE-004` | Thay `||` bằng xử lý `$LASTEXITCODE` tương thích PowerShell | `SENSITIVE_VALUE_SCAN=PASS` | `RESOLVED` |
| `S2-03-ISSUE-005` | Thay Bash brace expansion bằng PowerShell `$paths` array | Search retest exit `0` | `RESOLVED` |
