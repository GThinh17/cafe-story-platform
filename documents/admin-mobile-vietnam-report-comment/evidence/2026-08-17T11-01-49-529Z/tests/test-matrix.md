# Test matrix

| Area | Command/check | Result |
|---|---|---|
| Mobile | `npm run i18n:check` | PASS |
| Mobile | `npm run test:report-comment` | PASS |
| Mobile | `npm run typecheck` | PASS |
| Mobile | Expo Web bundle/runtime start | PASS |
| Admin | `npm run i18n:check` | PASS |
| Admin | `npm run typecheck` | PASS |
| Admin | `npm run build` | PASS |
| Admin | `npm run test:e2e:i18n` | 5/5 PASS |
| Admin | translation Playwright spec | 1/1 PASS |
| Backend | focused controller/service/DTO tests | PASS |
| Backend | `mvn test` | 1383 pass, 0 fail, 0 error, 1 skip |
| n8n | sync + contract/security/adversarial | PASS |
| n8n/OpenAI | real EN→VI, VI→EN and identifier-heavy runtime | PASS |
| Mobile runtime | login, feed, search, comment load/post/reply, locale invariance | PASS |
| Mobile runtime | BLOG and COMMENT report from UI; targets unchanged | PASS |
| Admin runtime | open COMMENT report, translate, retry, original/translation toggle | PASS |
| Cleanup | reject two reports, remove test comment/reply, preserve targets | PASS |

`FRONTEND_COVERAGE_TOOLING_MISSING`: neither frontend package currently exposes changed-file unit coverage instrumentation.
