# S2-01 — Changed production coverage

Gate:

```text
line = 100%
branch >= 85%
```

Kết quả từ `target/site/jacoco/jacoco.xml` sau focused và full regression:

| Production class | Line | Branch |
|---|---:|---:|
| `AdminReportAiResolutionRequestDTO` | `100% (16/16)` | `100% (0/0)` |
| `AdminReportAiCandidateRuleRequestDTO` | `100%` | `100%` |
| `AdminReportAiEvidenceItemRequestDTO` | `100%` | `100%` |
| `AdminReportAiMissingRequirementRequestDTO` | `100%` | `100%` |
| `AdminReportAiPolicyContextRequestDTO` | `100%` | `100%` |
| `AdminReportAiRuleRequirementRequestDTO` | `100%` | `100%` |
| `AdminReportAiPolicyCatalog` | `100% (103/103)` | `100% (23/23)` |
| `AdminReportAiResolutionServiceImpl` | `100% (571/571)` | `88.84% (215/242)` |
| `AdminReportAiSemanticValidator` | `100% (145/145)` | `90.56% (163/180)` |

Các DTO Lombok mới không tạo executable counter sau JaCoCo filtering; được ghi `100%` theo zero-counter
policy của gate.

Kết luận: `PASS`.
