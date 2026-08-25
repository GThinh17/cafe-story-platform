# S2-04 — Test log

## Focused prompt candidate

Lệnh:

```powershell
node docker/tests/validate-admin-report-ai-prompt-pilot.mjs
```

Kết quả:

- spec schema `1/1 PASS`;
- selected profile `1/1 PASS`;
- executable branch `2/2 PASS`;
- untrusted mutation `4/4 PASS`;
- assembler negative guard `5/5 PASS`;
- spec negative guard `6/6 PASS`;
- dataset ref `5/5 PASS`;
- canonical provider output field parity `evidenceIds PASS`;
- prompt digest
  `1e5deb6a2e6cee4262d07ea72fc6338e25fc8d4ffaa9d0e10b20700a49f3bb0d`;
- provider called `false`;
- runtime authority `false`.

## Focused harness coverage

Lệnh:

```powershell
node --test --experimental-test-coverage docker/tests/validate-admin-report-ai-prompt-pilot.mjs
```

Kết quả:

- test `1/1 PASS`;
- harness line `100%`;
- branch `97.44%`;
- function `96.55%`.

Assembler được nạp động như n8n Code Node nên không xuất hiện thành coverage
unit riêng; không dùng số harness để tuyên bố assembler coverage.

## S2-03 full hard gate regression

Lệnh:

```powershell
node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs
```

Kết quả:

- dataset negative `6/6 PASS`;
- schema `7/7 PASS`;
- adversarial `12/12 PASS`;
- M07 fixture `18/18 PASS`;
- cross-review `16/16 PASS`;
- Backend focused `50/50 PASS`;
- hard safety `100%`;
- provider quality `NOT_EVALUATED_NO_PROVIDER_CALL`.

## Full Backend regression

Lệnh chạy từ `1-cafe-story-backend-javaspring`:

```powershell
mvn test
```

Kết quả: `638` test, failure `0`, error `0`, skipped `1`, `BUILD SUCCESS`.

Các WARN/ERROR stack trace trong output thuộc negative-path test đã pass, không
phải Maven failure.
