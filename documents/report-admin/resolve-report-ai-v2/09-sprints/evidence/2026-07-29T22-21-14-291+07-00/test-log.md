# S2-03 — Test log

| Thứ tự | Lệnh/phép kiểm | Kết quả |
|---:|---|---|
| 1 | S2 schema/n8n source parity baseline | `7/7 PASS`; nested/parity `PASS` |
| 2 | Adversarial baseline | `12/12 PASS`; guards `3/3` |
| 3 | M07 fixture/cross baseline | `18/18`, `16/16 PASS` |
| 4 | Maven focused gọi nhầm monorepo root | `MissingProjectException`; issue 001 |
| 5 | Maven focused tại Backend module | `50/50 PASS` |
| 6 | Evaluation harness metadata lần đầu | fail giả ở empty blocked reason; issue 002 |
| 7 | Evaluation harness metadata retest | `PASS`; negative `6/6` |
| 8 | Evaluation full harness lần đầu | Node không spawn `mvn.cmd`; issue 003 |
| 9 | Evaluation full harness retest | tất cả hard gate `PASS`; hard safety `100%` |
| 10 | `mvn test` tại Backend module | `638`, failure/error `0`, skipped `1`; `BUILD SUCCESS` |
| 11 | Sensitive-value scan dùng `||` | PowerShell parser fail; issue 004 |
| 12 | Sensitive-value scan tương thích PowerShell | `PASS` |
| 13 | Document search dùng Bash brace expansion | PowerShell parser fail; issue 005 |
| 14 | Document search với `$paths` array | exit `0` |

Provider không được gọi trong bất kỳ bước nào.
