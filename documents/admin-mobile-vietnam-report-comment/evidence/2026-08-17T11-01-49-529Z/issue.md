# Issues

## ISSUE-001 — Backend translation compile failure

- Classification: `CODE_BUG`
- Evidence: `mvn -q -DskipTests compile`
- Affected behavior: `AdminContentTranslationServiceImpl` does not compile because its catch clause lists `IOException`, while `RestClient.exchange` does not expose that checked exception.
- Likely owner: code
- Proposed action: remove only the unreachable checked-exception catch alternative.
- Auto-fix allowed: yes

## Known tooling gaps

- `FRONTEND_COVERAGE_TOOLING_MISSING`: Mobile and Admin packages do not expose changed-file unit coverage instrumentation.

## ISSUE-002 — PowerShell parsed the Maven test list

- Classification: `CONFIG_ENV`
- Evidence: unquoted `-Dtest=...,...` focused-test command.
- Affected behavior: PowerShell interpreted commas as an argument list before Maven started.
- Likely owner: config
- Proposed action: quote the complete Maven `-Dtest` argument and re-run; no source change.
- Auto-fix allowed: no

## ISSUE-003 — Mobile regression assertion matched a helper-name suffix

- Classification: `TEST_BUG`
- Evidence: `npm run test:report-comment`
- Affected behavior: the negative assertion for `t(comment.content)` also matched the suffix of `renderCommentContent(comment.content)`, even though the source renders the comment verbatim.
- Likely owner: test
- Proposed action: require a non-identifier boundary before the translator call in the assertion.
- Auto-fix allowed: yes

## ISSUE-004 — Admin i18n E2E retained the old fresh-English expectation

- Classification: `TEST_BUG`
- Evidence: `npm run test:e2e:i18n` (4/5 passed, dashboard test failed at the initial `Overview` assertion).
- Affected behavior: the test suite correctly verifies a fresh English browser starts in Vietnamese in one case, but the following case still expected the former English default.
- Likely owner: test
- Proposed action: assert Vietnamese first, explicitly switch to English to verify the transition, then switch back to Vietnamese before continuing the raw AI-data invariance checks.
- Auto-fix allowed: yes

## ISSUE-005 — Translation controls bubbled clicks into clickable table rows

- Classification: `CODE_BUG`
- Evidence: bounded Playwright API trace and failure screenshot. Clicking `Dịch` also opened the row-detail dialog; the overlay then blocked the visible row-level `Thử dịch lại` control until timeout.
- Affected behavior: Admin table translation controls can unintentionally open the content detail dialog.
- Likely owner: UI event handling
- Proposed action: stop click propagation on Translate, toggle and retry controls while keeping their normal button behavior.
- Auto-fix allowed: yes

## ISSUE-006 — Full Maven integration suite cannot reach Docker

- Classification: `DOCKER_ENV`
- Evidence: `mvn test` ran 1,384 tests with 0 failures, 12 errors and 1 skipped; all 12 errors originate from `PostgresIntegrationTestSupport` because Testcontainers cannot find a valid Docker environment.
- Affected behavior: the focused translation tests pass, but the full integration regression suite is not yet green.
- Likely owner: local Docker runtime
- Proposed action: inspect/start Docker Desktop if available, then rerun the full Maven suite. Do not change test or production source to hide the environment failure.
- Auto-fix allowed: environment-only

## ISSUE-007 — Compose run duplicated the n8n executable token

- Classification: `CONFIG_ENV`
- Evidence: the stopped-container import command returned `Error: Command "n8n" not found` before importing anything.
- Affected behavior: translation workflow remains unimported; the persistent volume was not mutated by the failed command.
- Likely owner: CLI invocation
- Proposed action: pass `import:workflow`/`publish:workflow` directly to the image entrypoint instead of prefixing another `n8n` token.
- Auto-fix allowed: command-only

## ISSUE-008 — Dev backend started existing scheduled jobs

- Classification: `ENV_SIDE_EFFECT`
- Evidence: startup logs show the existing Feed metrics scheduler querying and inserting a missing `blog_daily_metrics` row immediately after the runtime backend became ready.
- Affected behavior: this task did not change scheduler source or schema, but an existing dev-runtime job may have updated derived metrics in the configured development database.
- Likely owner: runtime configuration
- Proposed action: stop the task-owned backend and restart with `APP_SCHEDULING_ENABLED=false` and `APP_DATA_INITIALIZER_ENABLED=false`; do not delete or rewrite database rows without a safe ownership trail.
- Auto-fix allowed: environment-only

## ISSUE-009 — Initial Expo override launch was rejected before process creation

- Classification: `TOOL_POLICY_ENV`
- Evidence: the execution layer rejected the PowerShell command that assigned `EXPO_PUBLIC_API_BASE_URL` inline; Expo did not start and no source file changed.
- Affected behavior: authenticated Expo Web verification has not started yet.
- Likely owner: local command execution policy
- Proposed action: launch the same ephemeral process environment through `cmd.exe set` without editing Mobile `.env`.
- Auto-fix allowed: command-only

## ISSUE-010 — Mobile locale smoke used the wrong accessible name

- Classification: `TEST_BUG`
- Evidence: after switching Mobile to English, the smoke test looked for radio name `Vietnamese`; the live DOM intentionally exposes the language's native name `Tiếng Việt`.
- Affected behavior: only the switch-back test step failed; the English switch and screenshots succeeded.
- Likely owner: test interaction
- Proposed action: target the accessible name shown by the live DOM (`Tiếng Việt`) and rerun the switch-back assertion.
- Auto-fix allowed: interaction-only

## ISSUE-011 — Next dev rejected the 127.0.0.1 development origin

- Classification: `CONFIG_ENV`
- Evidence: Admin returned HTML at `127.0.0.1:3636`, but Next dev logged that cross-origin development resources from `127.0.0.1` were blocked; the live DOM remained empty.
- Affected behavior: the first Admin screenshot attempt failed before login UI rendered.
- Likely owner: local URL choice
- Proposed action: use the configured/default `http://localhost:3636` origin; do not add source-level `allowedDevOrigins` only for this runtime.
- Auto-fix allowed: navigation-only

## ISSUE-012 — Mobile navigation and Explore/Profile UI retained English labels

- Classification: `CODE_BUG`
- Evidence: authenticated VI runtime showed `Home`, English Explore tabs/title, `Your posts`, and English profile statistics/accessibility labels.
- Affected behavior: UI chrome was mixed-language even though caption/comment data correctly remained raw.
- Likely owner: Mobile UI i18n
- Proposed action: route only these UI labels through typed translation keys; keep recommendation reason, caption, names and comments unchanged.
- Auto-fix allowed: yes

## ISSUE-013 — Translation provider translated wrapper and protected identifiers

- Classification: `CODE_BUG`
- Evidence: n8n executions 352/353/356 returned safe errors because OpenAI translated `CONTENT_KIND`, delimiter text, `ACTIONS` and `COMMENT`; the normalizer correctly rejected the response.
- Affected behavior: an identifier-heavy report description displayed the safe Admin retry state instead of a translation.
- Likely owner: n8n prompt/protected-token boundary
- Proposed action: send only source text, mask protected URL/UUID/enum/diagnostic tokens before OpenAI, restore them after strict placeholder validation, and add an identifier-heavy real-runtime case.
- Auto-fix allowed: yes

## ISSUE-014 — n8n health became ready before webhook registration

- Classification: `CONFIG_ENV`
- Evidence: the first request immediately after restart returned 404 while startup logs were still activating published workflows.
- Affected behavior: only the first runtime attempt after publish failed.
- Likely owner: runtime readiness sequencing
- Proposed action: wait for active-workflow registration after health before calling the production webhook.
- Auto-fix allowed: command-only

## ISSUE-015 — Vietnamese Profile statistics touched at 390px and empty UI remained English

- Classification: `CODE_BUG`
- Evidence: final 390×844 visual review showed `người theo dõi` touching `đang theo dõi`, while the cafe-page fallback and empty-state copy still displayed English.
- Affected behavior: Profile UI was difficult to scan and did not fully follow the selected Vietnamese locale; user/backend profile data itself remained unchanged.
- Likely owner: Mobile Profile layout and typed UI copy
- Proposed action: give each statistic an equal shrinkable cell, use compact localized display labels, and move the remaining Profile fallback/empty copy into the typed EN/VI dictionaries.
- Auto-fix allowed: yes
