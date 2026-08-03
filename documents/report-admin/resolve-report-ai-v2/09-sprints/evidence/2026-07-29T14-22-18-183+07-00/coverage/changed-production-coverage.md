# Changed production coverage — DOD-FIX-03

Command:

```powershell
mvn clean '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test
```

| Class | Line | Branch | Gate |
|---|---:|---:|---|
| `AdminReportAiResolutionServiceImpl` | `464/464 = 100%` | `179/204 = 87.75%` | PASS |
| `AdminReportAiSemanticValidator` | `105/105 = 100%` | `118/122 = 96.72%` | PASS |

Required gate: line `100%`, branch `>=85%` cho mỗi changed production class.
