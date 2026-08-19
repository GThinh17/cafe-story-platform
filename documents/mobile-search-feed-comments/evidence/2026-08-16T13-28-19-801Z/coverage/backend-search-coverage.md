# Changed backend coverage

Source: `1-cafe-story-backend-javaspring/target/site/jacoco/jacoco.csv` after `mvn test`.

| Production class | Lines | Branches | Methods |
| --- | ---: | ---: | ---: |
| `ExploreSearchServiceImpl` | 61/61 (100%) | 26/26 (100%) | 6/6 (100%) |
| `ExploreSearchController` | 4/4 (100%) | n/a | 2/2 (100%) |

Repository interfaces contain query declarations rather than executable Java lines. Response DTOs are excluded by the repository JaCoCo configuration. The mobile package has no unit/coverage command, recorded as `FRONTEND_COVERAGE_TOOLING_MISSING`.
