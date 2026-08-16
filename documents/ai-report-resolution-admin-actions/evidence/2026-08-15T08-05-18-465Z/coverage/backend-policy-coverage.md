# Backend changed-code coverage

Source: `target/site/jacoco/jacoco.xml` after full `mvn test`.

| Production file | Added executable lines | Covered | Missed | Covered branches | Missed branches |
| --- | ---: | ---: | ---: | ---: | ---: |
| `AdminContentReportController.java` | 1 | 1 | 0 | 0 | 0 |
| `AdminReportAiResolutionService.java` | 0 | 0 | 0 | 0 | 0 |
| `AdminReportAiResolutionServiceImpl.java` | 43 | 43 | 0 | 2 | 0 |

- Changed executable line coverage: `44/44 = 100%`
- Changed branch coverage: `2/2 = 100%`
- Response DTOs are excluded by the repository's existing JaCoCo configuration (`com/cafestory/dto/**`).
- Interface declarations contain no executable lines.
