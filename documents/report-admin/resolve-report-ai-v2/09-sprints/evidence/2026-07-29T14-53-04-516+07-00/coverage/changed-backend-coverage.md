# Changed Backend Coverage — DOD-FIX-04

Nguồn: `1-cafe-story-backend-javaspring/target/site/jacoco/jacoco.xml`, tạo bởi focused suite
64 test sau `mvn clean`.

| Class | Line | Branch |
|---|---:|---:|
| `AdminReportAiResolutionServiceImpl` | `464/464 = 100%` | `179/204 = 87.75%` |
| `AdminReportAiProviderBoundaryException` | `4/4 = 100%` | không có branch |
| `AdminReportAiOperationalErrorResponseDTO` | `7/7 = 100%` | không có branch |
| `AdminReportAiExceptionAdvice` | `4/4 = 100%` | không có branch |

Gate áp dụng: changed production Java class phải đạt line `100%` và class có branch phải đạt
ít nhất `85%`. Kết quả: `PASS`.
