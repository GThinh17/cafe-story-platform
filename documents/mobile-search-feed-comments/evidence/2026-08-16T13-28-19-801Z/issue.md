# Mobile search, feed, and comments issues

## MOB-RUNTIME-001

- Classification: `CONFIG_ENV`
- Evidence: authenticated API probe against `http://127.0.0.1:8080` timed out after 20 seconds before a usable access token and endpoint results were captured.
- Affected behavior: runtime verification for Feed, comments, and Explore search cannot yet distinguish a mobile bug from a stalled backend dependency.
- Likely owner: local backend runtime or dependency configuration; ownership remains provisional until the exact request is isolated.
- Proposed action: verify backend process/logs and probe registration/login and each endpoint separately. Reclassify only if source evidence proves a code defect.
- Auto-fix allowed: no.
- Resolution: backend was restarted with the current source and `/v3/api-docs` returned HTTP 200. Authenticated Expo runtime then loaded Feed and comments successfully, so this environment issue no longer blocks the scoped verification.

## MOB-CORS-001

- Classification: `CONFIG_ENV`
- Evidence: Expo Web started on `http://localhost:19006` received a 403 CORS preflight response. The backend allowlist includes the established Expo origin `http://localhost:8081`, not `19006`.
- Affected behavior: login on the ad-hoc port failed with `Failed to fetch`.
- Likely owner: local Expo runtime origin selection.
- Proposed action: run the existing Expo development server on port `8081` for this verification.
- Auto-fix allowed: no source change required.
- Resolution: reused the existing allowed `8081` server; authenticated runtime passed.

## MOB-COVERAGE-001

- Classification: `CONFIG_ENV`
- Evidence: the mobile package currently exposes `i18n:check` and `typecheck`, but no unit-test or coverage script.
- Affected behavior: changed-file line and branch coverage cannot be measured with existing mobile tooling.
- Likely owner: frontend test tooling.
- Proposed action: run available static checks and runtime/API verification; report `FRONTEND_COVERAGE_TOOLING_MISSING`.
- Auto-fix allowed: no.

## MOB-SEARCH-001

- Classification: `CODE_BUG`
- Evidence: authenticated Expo Web baseline on the backend-allowed port `8081` remained in the `Searching` state after more than 31 seconds. Direct probes showed `/api/users` timing out after 15 seconds while the current mobile implementation waits for complete user, cafe-page, and blog lists before filtering locally.
- Affected behavior: Explore search can remain indefinitely loading and transfers far more data than the eight results displayed per group.
- Likely owner: mobile search orchestration and missing bounded backend search contract.
- Proposed action: add a read-only aggregate search endpoint with bounded per-group results and switch mobile Explore to that single endpoint.
- Auto-fix allowed: yes.
- Resolution: fixed with `GET /api/search`, three bounded repository queries, minimal response DTOs, and one cached mobile request. Expo Web returned `cafe` results in about 1.38 seconds and a reviewer query in under 2 seconds without remaining in `Searching`.

## MOB-TEST-DATA-001

- Classification: `TEST_DATA`
- Evidence: deleting a temporary runtime user through `DELETE /api/users/me` returned HTTP 500 after the API probe.
- Affected behavior: one temporary user may remain in the local test database.
- Likely owner: local test data cleanup and existing delete-user relationships.
- Proposed action: identify and remove only the `codex_mobile_*`/`cx*` test account through a safe admin or database cleanup path after exact target verification.
- Auto-fix allowed: no.
- Current state: an exact full-name search confirms a `Codex Mobile Test` account remains. The admin API has status and role mutations but no delete endpoint, so no destructive database workaround was attempted.

## MOB-TEST-001

- Classification: `TEST_BUG`
- Evidence: PowerShell parsed the comma in `-Dtest=ExploreSearchServiceImplTest,ExploreSearchControllerTest` as a parameter separator and stopped before Maven started.
- Affected behavior: focused backend tests did not execute on the first attempt.
- Likely owner: test command quoting.
- Proposed action: quote the complete Maven `-Dtest=...` argument and rerun unchanged tests.
- Auto-fix allowed: yes.
- Resolution: quoted the full `-Dtest=ExploreSearchServiceImplTest,ExploreSearchControllerTest` argument; 4 focused tests passed.

## MOB-TEST-002

- Classification: `TEST_ENV`
- Evidence: after the full suite, an IDE background compiler overwrote the new test class under `target/test-classes` with unresolved-type bytecode. Surefire then failed JUnit discovery with `NoClassDefFoundError: Region` and ran 0 tests.
- Affected behavior: an incremental focused rerun could not discover the Search test class even though the previous Maven suite had passed it.
- Likely owner: generated Maven `target` output shared with the IDE compiler.
- Proposed action: remove only generated `target` output through Maven Clean, recompile with Maven `javac`, then rerun focused tests.
- Auto-fix allowed: yes; generated build output only.
- Resolution: `mvn clean '-Dtest=ExploreSearchServiceImplTest,ExploreSearchControllerTest' test` rebuilt 566 production and 143 test sources; 4/4 focused tests passed.

## MOB-RUNTIME-002

- Classification: `TEST_ENV`
- Evidence: Maven Clean replaced `target` while the dev-mode Spring Boot process was still using that classpath. The subsequent browser request received `Failed to fetch`, and a preflight probe temporarily returned 401.
- Affected behavior: the first post-clean UI recheck could not call Search.
- Likely owner: lifecycle of the backend process created for this verification.
- Proposed action: confirm the exact listener PID, stop only that task-owned Spring Boot process, restart it from the clean build, and retry without source changes.
- Auto-fix allowed: yes; task-owned runtime process only.
- Resolution: clean restart restored a 200 CORS preflight. The final authenticated Search completed in about 1.57 seconds; Feed exposed 9 comment actions and the selected comment dialog rendered existing comments.
