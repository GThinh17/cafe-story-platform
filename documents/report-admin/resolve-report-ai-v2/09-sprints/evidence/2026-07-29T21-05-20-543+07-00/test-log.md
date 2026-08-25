# S2-01 — Test log

## Baseline trước sửa

```text
mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0

node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs
ADV: 12/12 PASS
```

## Test compile sau typed migration

```text
mvn '-DskipTests' test-compile
BUILD SUCCESS
```

Lần chạy đầu phát hiện 13 compilation errors do test Map-based; đã ghi
`S2-01-ISSUE-001`, sửa fixture/assertion rồi rerun pass.

## Focused Backend cuối

```text
mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test
Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Phạm vi case mới:

- typed rule/evidence contract;
- proposed lifecycle manual clamp;
- null/unknown evaluation mode;
- proposed candidate-rule lifecycle manual clamp;
- catalog DTO không bị thay đổi bởi caller mutation;
- unique candidate Rule ID;
- unique Evidence ID;
- subject snapshot binding;
- legacy request constructor compatibility.

## Full Backend regression

```text
mvn test
Tests run: 628, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

Case skip là PostgreSQL integration được điều kiện hóa sẵn; không phát sinh từ S2-01.

## n8n executable source test

```text
node docker/tests/sync-admin-report-ai-workflow-code.mjs
ADMIN_REPORT_AI_WORKFLOW_CODE_SYNC=PASS

node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs
ADV-001..ADV-012=PASS
contractGuardsPassed=3
providerCalled=false
secretsPrinted=false
```

Ba negative contract guards:

1. duplicate candidate Rule ID;
2. duplicate Evidence ID;
3. evidence snapshot hash mismatch.

Test data-minimization xác nhận:

- target text allowlisted đi qua sanitized evidence payload;
- reporter claim không đi provider;
- raw media URL không đi provider;
- existing moderation/prior-AI không đi provider;
- secret không xuất hiện trong provider request/normalized output.

## M07 evidence contract regression

```text
node validate-fixtures.mjs
18/18 PASS

node cross-review-check.mjs
16/16 PASS
runtimeAuthority=false
```

## Static diff

```text
git diff --check
exit code 0
```

Chỉ có warning line-ending LF → CRLF của Git trên Windows; không có whitespace error.
