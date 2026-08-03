# Sprint 1 Rollout Plan

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` design |
| Trạng thái | `DESIGNED_NOT_EXECUTED` |

1. Confirm G0-12 approval and named owners.
2. Enable server A0 kill switch before other deployment.
3. Read-only inventory active auto-apply jobs.
4. Resolve any `APPLYING` blocker; cancel/skip `SCHEDULED` with evidence.
5. Apply additive migration after Flyway history verification.
6. Deploy BE and run focused/contract tests.
7. Import/publish same n8n workflow ID/path; signed readiness probe.
8. Deploy FE; verify auto-apply creation UI absent.
9. Canary safe BLOG and COMMENT fixtures.
10. Run E2E, side-effect and cleanup checks.

Stop rollout on security/safety test failure, target/report mutation, secret leak, invalid schema or
missing mandatory safe fixture.
