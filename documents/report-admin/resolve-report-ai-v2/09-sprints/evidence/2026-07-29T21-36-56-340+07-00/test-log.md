# S2-02 — Test log

| Lớp | Lệnh | Expected | Actual |
|---|---|---|---|
| Focused Backend | `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test` | Không failure/error | `50/50 PASS` |
| Coverage | Parse `target/site/jacoco/jacoco.xml` | line `100%`, branch `>=85%` cho changed semantic class | Validator `100%/88.49%`; catalog `100%/100%` |
| Schema/n8n | `node docker/tests/validate-admin-report-ai-s2-contracts.mjs` | Compile, boundary, nested runtime, parity pass | `7/7`, nested `PASS`, parity `PASS` |
| Adversarial | `node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs` | Hard safety vectors pass; không gọi provider | `12/12`, guards `3/3`, provider `false` |
| M07 fixture | `node documents/.../contracts/v2.0.0-rc.1/validate-fixtures.mjs` | Không regression | `18/18 PASS` |
| M07 cross-review | `node documents/.../contracts/v2.0.0-rc.1/cross-review-check.mjs` | Không drift | `16/16 PASS` |
| Full Backend | `mvn test` | Không failure/error | `638`, failures `0`, errors `0`, skipped `1` |

`skipped=1` là PostgreSQL integration test cần môi trường riêng đã tồn tại từ baseline; S2-02 không
mở runtime database gate và không dùng skip này để claim PostgreSQL verification.

Trong quá trình test, hai focused run đầu đã bắt được fail-closed NPE. Các lỗi đã được ghi trước khi
sửa trong `issue.md`, giữ regression test và retest đến khi toàn bộ suite pass.

