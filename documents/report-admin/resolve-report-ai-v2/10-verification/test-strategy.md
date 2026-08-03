# Sprint 1 Verification Strategy

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |

## Order

1. Static contract/schema and source placement.
2. Pure unit tests: semantic validator, snapshot, risk, signer.
3. Service/controller tests.
4. Persistence constraint/integration tests.
5. n8n static + signed negative/positive probe.
6. FE typecheck/build.
7. UI/API E2E.
8. Regression and side-effect verification.

## Required quality gates

- changed production files: `100%` line, `>=85%` branch;
- no AI-triggered report/target mutation;
- no scheduled job created under A0;
- no secret/raw provider payload in logs/API/evidence;
- 12 G0-10 decisions represented;
- BLOG and COMMENT safe fixtures required for full E2E;
- USER/PAGE provider-call suppression verified where observable.

## Honest status

- Unit pass is not E2E pass.
- Webhook `/healthz` is liveness, not workflow readiness.
- Published workflow must be probed at exact webhook.
- Missing fixture/config/provider becomes `BLOCKED`, `TEST_DATA` or `ENVIRONMENT`.
- Do not lower assertions to convert a real failure into pass.

## Commands

```powershell
mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiAutoApplyJobServiceImplTest,AdminContentReportControllerTest,AdminReportAiSemanticValidatorTest,AdminReportEvidenceSnapshotFactoryTest,AdminReportAiWebhookSignerTest' test
mvn test
npm run typecheck
npm run build
npm run test:e2e:admin-report-ai
```

Run Maven and Next build sequentially on this Windows workspace.
