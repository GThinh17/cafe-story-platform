# S2-DONE-AUDIT — Coverage

## Backend focused

| Production class | Line | Branch | Required | Result |
|---|---:|---:|---:|---|
| `AdminReportAiResolutionServiceImpl` | `564/571 = 98.77%` | `214/242 = 88.43%` | line `100%`; branch `>=85%` | `FAIL_LINE` |
| `AdminReportAiSemanticValidator` | `434/434 = 100%` | `446/504 = 88.49%` | line `100%`; branch `>=85%` | `PASS` |
| `AdminReportAiPolicyCatalog` | `109/109 = 100%` | `25/25 = 100%` | line `100%`; branch `>=85%` | `PASS` |

Missing source lines reported by JaCoCo:

```text
512, 636, 872–878
```

## Provider benchmark library

| Scope | Line | Branch | Function | Result |
|---|---:|---:|---:|---|
| benchmark library | `100%` | `91.36%` | `100%` | `PASS` |
| all focused Node files | `100%` | `93.49%` | `98.92%` | `PASS` |

Audit coverage conclusion: `PARTIAL`; remediation test coverage required.

