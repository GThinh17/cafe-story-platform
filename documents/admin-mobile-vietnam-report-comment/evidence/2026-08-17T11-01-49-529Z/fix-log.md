# Fix log

- ISSUE-001: removed the unreachable `IOException` catch alternative; focused compile and focused tests passed.
- ISSUE-002: no source fix; reran the focused tests with the Maven test selector quoted for PowerShell; passed.
- ISSUE-003: narrowed the static assertion to match only a standalone translator call, not `renderCommentContent`; `npm run test:report-comment` passed.
- ISSUE-004: aligned the stale dashboard E2E setup with the approved Vietnamese fresh default while preserving explicit EN/VI transitions; `npm run test:e2e:i18n` passed 5/5.
- ISSUE-005: Playwright trace proved event bubbling into the clickable table row; stopped propagation on translation controls; `admin-translation.spec.ts` passed 1/1.
- ISSUE-006: no source fix; started Docker Desktop and reran the full Maven suite; 1,383 tests passed, 0 failures, 0 errors, 1 skipped.
- ISSUE-007: no source fix; corrected the compose-run CLI shape, imported and published `cafestory-admin-content-translation-v1` as separate steps, then verified health and active workflow state.
- ISSUE-008: restart the backend with schedulers and data initializers disabled through ephemeral environment variables; no source or database cleanup mutation.
- ISSUE-009: no source fix; retry Expo with an equivalent process-local environment assignment through `cmd.exe`.
- ISSUE-010: no source fix; use the live accessible name `Tiếng Việt` for the switch-back step.
- ISSUE-011: no source fix; navigate Admin through `localhost`, matching its configured development origin.

- ISSUE-012: localized BottomBar, Explore tabs/title/fallbacks, self story, profile statistics and profile-tab accessibility with typed keys; Mobile i18n/typecheck/static regression passed and authenticated 390×844 runtime showed VI labels.
- ISSUE-013: removed wrapper text from the provider message and added deterministic protected-token masking/restoration including long diagnostic numbers; static contract/security passed and real OpenAI runtime passed EN→VI, VI→EN and identifier-heavy report description.
- ISSUE-014: no source fix; waited for active workflow registration after health before rerunning the webhook.
- ISSUE-015: added equal shrinkable statistic cells, compact VI display labels, and typed Profile cafe/empty-state copy; `mobile-profile-vietnamese-390x844-final.png` visually confirms separated labels and Vietnamese UI, then Mobile i18n/typecheck/report-comment checks passed.

## Runtime cleanup

- Mobile created one BLOG report and one COMMENT report through UI; both were moved to `REJECTED` after evidence.
- The original blog and comment remained `PUBLISHED` before and after report cleanup.
- The task-created Mobile comment and reply were deleted; no matching test marker remained.
- Mobile locale preference was restored to VI.
