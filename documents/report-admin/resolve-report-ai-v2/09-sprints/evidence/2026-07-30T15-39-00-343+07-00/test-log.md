# S2-05 — Test log

## 1. Focused benchmark gate

Lệnh:

```powershell
node --test --experimental-test-coverage docker/tests/validate-admin-report-ai-provider-benchmark.mjs
```

Kết quả:

- config schema `1/1 PASS`;
- unsafe config mutations `8/8 PASS`;
- internal output schema `1/1 PASS`;
- unsafe output mutations `5/5 PASS`;
- dataset refs `5/5 PASS`;
- direct fixture `1/1 PASS`;
- deterministic prompt assembly `6/6 PASS`;
- provider request boundary `PASS`;
- cost guard `0.918/1 USD PASS`;
- quality denominator `0 PASS`;
- static gate `PASS`.

## 2. Coverage

- benchmark library line: `100%`;
- benchmark library branch: `91.36%`;
- benchmark library functions: `100%`;
- all measured files line: `100%`;
- all measured files branch: `93.49%`.

## 3. Dry-run

Lệnh:

```powershell
node docker/tests/run-admin-report-ai-provider-benchmark.mjs
```

Kết quả:

- key readiness `present` nhưng value không được in;
- models `3`;
- cases `6`;
- repeats `2`;
- scheduled/max HTTP requests `36/36`;
- projected worst-case cost `0.918 USD`;
- budget `1 USD`;
- runtime authority `false`;
- retest output:
  `raw/provider-benchmark-retest-01-results.json`.

## 4. Regression S2-04

Lệnh:

```powershell
node docker/tests/validate-admin-report-ai-prompt-pilot.mjs
```

Kết quả: `S2_04_PROMPT_PILOT=PASS`.

## 5. Regression S2-03

Lệnh:

```powershell
node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs
```

Kết quả:

- full hard gate `PASS`;
- `26` records, `9` evidence bundles;
- dataset negative `6/6`;
- schema `7/7`;
- adversarial `12/12`;
- M07 `18/18`;
- cross-review `16/16`;
- Backend focused `50/50`;
- hard safety `100%` cho deterministic pre-provider gates.

## 6. Provider execute lần 1

Lệnh:

```powershell
node docker/tests/run-admin-report-ai-provider-benchmark.mjs --execute
```

Kết quả:

- provider HTTP request `36/36`;
- inference success `0/36`;
- safe error `MODEL_OR_REQUEST:invalid_json_schema` `36/36`;
- actual recorded cost `0 USD`;
- model selection
  `NO_MODEL_SELECTED_INSUFFICIENT_GROUND_TRUTH`;
- secret/raw response/raw provider error không được persist.

Kết luận: failed at provider schema boundary; không phải model-quality result.

## 7. Sau fix

Structured-output adapter, circuit breaker và evidence path đã được
focused/coverage/dry-run verify. Provider retest chưa chạy vì 36-request Bound
đã dùng hết.

