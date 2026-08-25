# Evidence Metadata Contract `2.0.0-rc.1`

Đây là executable design artifact của `G0-12M-07`, chưa phải runtime-active contract.

## Chạy validation

Từ repository root:

```powershell
node "documents/report-admin/resolve-report-ai-v2/05-evidence-standard/contracts/v2.0.0-rc.1/validate-fixtures.mjs"
```

Kết quả đạt:

```text
manifestMatchesFiles = true
actualScenarioCount = 18
passed = 18
failed = 0
allPassed = true
```

## Thành phần

- `evidence-metadata-contract.schema.json`: JSON Schema Draft 2020-12.
- `contract-manifest.json`: version pinning, lifecycle và fixture index.
- `validate-fixtures.mjs`: Ajv strict validation + semantic invariants.
- `fixtures/bases`: four canonical target bases.
- `fixtures/cases`: positive/negative scenario overlays.

## Guard

- Manifest đã `APPROVED` tại M08 nhưng vẫn không phải production authority.
- Không đổi `runtimeAuthority` trước khi Rule Catalog/policy ACTIVE và các runtime/security gate đạt.
- Không coi fixture pass là BE/FE/n8n E2E pass.
