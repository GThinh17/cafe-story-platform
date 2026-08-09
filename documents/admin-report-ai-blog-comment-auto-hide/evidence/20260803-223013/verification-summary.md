# Admin Report AI Blog/Comment Auto Hide Verification

## Scope

- Implemented BLOG and COMMENT evidence enrichment for Admin Report AI.
- Enabled `A1_AUTO_HIDE_BLOG_COMMENT` as a backend-controlled automation mode.
- Kept `A0_RECOMMEND_ONLY` as the default safety mode.
- Auto-hide remains request-triggered by admin AI resolution, not a background scan.
- Image evidence is `PLATFORM_URL_METADATA_ONLY`; no image fetch and no vision scan.

## Backend Verification

Command:

```powershell
mvn clean '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiAutoApplyJobServiceImplTest,AdminReportAiSemanticValidatorTest,AdminReportAiResolutionSchemaContractTest' test
```

Result:

- `BUILD SUCCESS`
- `Tests run: 65, Failures: 0, Errors: 0, Skipped: 0`

Covered behavior:

- BLOG eligible recommendation schedules a delayed job and worker resolves report plus hides blog.
- COMMENT eligible recommendation schedules a delayed job and worker resolves report plus hides comment only.
- Parent blog remains visible for COMMENT auto-hide.
- Missing evidence, blocked reasons, weak evidence, resolved report, or target state changes skip auto-hide.
- A0 mode remains recommendation-only and does not mutate report or target.
- Cancelled scheduled jobs do not mutate report or target.
- Semantic validation still rejects unknown evidence references and unsafe output.

## n8n Static Verification

Command:

```powershell
node docker/tests/validate-admin-report-ai-s2-contracts.mjs
```

Result:

- `S2_CONTRACT_SCHEMA_COMPILE=PASS`
- `S2_SCHEMA_BOUNDARIES=7/7 PASS`
- `S2_N8N_NESTED_BOUNDARY=PASS`
- `S2_PROVIDER_SCHEMA_PARITY=PASS`

Command:

```powershell
node docker/tests/validate-admin-report-ai-security.mjs
```

Result:

- `ADMIN_REPORT_AI_N8N_SECURITY=PASS`
- `VALID_REQUEST=PASS`
- `REPLAY_REJECTED=PASS`
- `EXPIRED_NONCE_REACCEPTED=PASS`
- `ATOMIC_REPLAY_CLAIM=PASS`
- `STALE_TIMESTAMP_REJECTED=PASS`
- `TAMPERED_BODY_REJECTED=PASS`
- `INVALID_SIGNATURE_REJECTED=PASS`
- `SIGNED_RESPONSE=PASS`

## Not Verified

- Runtime smoke with a live backend, PostgreSQL, and n8n instance was not run in this pass.
