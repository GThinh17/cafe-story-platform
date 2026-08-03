# S2-DONE-AUDIT — Test log

| Gate | Lệnh | Actual |
|---|---|---|
| Project structure | `python .agents/.../check_project.py .` | `PASS`, warning `0` |
| S2 contracts | `node docker/tests/validate-admin-report-ai-s2-contracts.mjs` | schema compile; boundary `7/7`; nested/parity `PASS` |
| Adversarial | `node docker/tests/validate-admin-report-ai-prompt-adversarial.mjs` | `12/12 PASS`, provider `false` |
| Prompt pilot | `node docker/tests/validate-admin-report-ai-prompt-pilot.mjs` | `PASS` |
| Dataset hard gate | `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs` | `PASS`; hard safety `100%`; Backend `50/50` |
| Provider static | `node --test --experimental-test-coverage docker/tests/validate-admin-report-ai-provider-benchmark.mjs` | `PASS`; no provider call |
| Backend full | `mvn test` | `638`, fail/error `0`, skip `1`, build success |
| Backend focused | `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test` | `50/50 PASS` |
| Security static | `node docker/tests/validate-admin-report-ai-security.mjs` | `FAIL`; stale fixture missing S2 fields |
| Workflow sync | read-only node/source comparison | both code nodes `true` |
| Secret scan | exact key + regex | no key match |
| Whitespace | `git diff --check -- ...` | no error; LF/CRLF warnings only |
| Audit artifact integrity | parse `status.json`, `summary.json`, TSV; required-file and token checks | `PASS`; 14 rows = `10/1/3/0`, 10/10 required evidence files |
| Approval bookkeeping | parse governance JSON + roadmap/token consistency | `PASS`; approval recorded once, next token `IMPLEMENT_S2_DONE_FIX_01` |

Không chạy:

- `validate-admin-report-ai-runtime.mjs`, vì script gọi live n8n/provider và nằm
  ngoài audit Bound;
- Admin browser E2E, vì Sprint 2 không đổi FE/published runtime;
- production DB/deploy.
