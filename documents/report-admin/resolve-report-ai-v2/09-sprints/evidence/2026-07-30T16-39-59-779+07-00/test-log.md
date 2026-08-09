# S2-DONE-FIX-01 — Test log

| Gate | Lệnh/phương pháp | Mong đợi | Thực tế | Kết quả |
|---|---|---|---|---|
| Security | `node docker/tests/validate-admin-report-ai-security.mjs` | HMAC, freshness, replay, tamper và signed-response hợp lệ | Tất cả assertion pass; workflow vẫn inactive | `PASS` |
| Backend focused | `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test` | Happy/fail-closed regression không lỗi | `53` test, failure `0`, error `0`, skipped `0` | `PASS` |
| Static contract | `node docker/tests/validate-admin-report-ai-s2-contracts.mjs` | Contract/parity/nested schema hợp lệ | `7/7` và nested/parity pass | `PASS` |
| Adversarial | `node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs` | Prompt-injection/adversarial cases fail-safe | `12/12` pass | `PASS` |
| Prompt pilot | `node docker/tests/validate-admin-report-ai-prompt-pilot.mjs` | Pilot assertions hợp lệ | Pass | `PASS` |
| Evaluation dataset | `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs` | Dataset và regression floor hợp lệ | 26 records, 9 bundles, hard safety `100%`, negative `6/6`, focused `53` | `PASS` |
| Backend full | `mvn test` | Không có failure/error | `641` test, failure `0`, error `0`, skipped `1` | `PASS` |
| E2E provider failure/recovery | Playwright grep `E2E-S1-13` | Lần đầu `502` không persist; retry sau recovery persist đúng | Failure phase đúng; retry bị PostgreSQL từ chối vì `VARCHAR(64)` | `FAIL_BLOCKED` |
| E2E critical evidence | Playwright grep `E2E-S1-04` | HTTP `200`, manual/no-action, `UNASSESSABLE`, không mutation | HTTP `500` do cùng lỗi schema; no-mutation `true`, auto job `0` | `FAIL_BLOCKED` |
| Cleanup | Truy vấn row count trong `finally` của Playwright | Không còn fixture synthetic | E2E-S1-13 `0/0`; E2E-S1-04 `0/0/0` | `PASS` |

## Root-cause probe

Truy vấn `information_schema.columns` trên PostgreSQL disposable trả:

```text
target_snapshot_hash|character varying|64
```

Trong khi service và focused contract test dùng canonical format:

```text
sha256:<64 ký tự hex>  =>  tổng 71 ký tự
```

Không có secret nào được ghi vào evidence.
