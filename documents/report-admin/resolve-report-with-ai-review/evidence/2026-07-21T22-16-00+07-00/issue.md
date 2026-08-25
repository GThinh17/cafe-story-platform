# Issue classification

| ID | Priority | Classification | Evidence | Auto-fix decision |
|---|---|---|---|---|
| RAI-001 | P0 | CONFIG_ENV | n8n `/healthz` 200; production webhook POST 404; duplicate workflow records/path observed. | Not changed. Publishing/deleting workflow is an external state mutation outside review-only scope. |
| RAI-002 | P1 | SECURITY_DESIGN | Webhook node has no authentication; request may contain moderation/user/page context. | Not changed. Requires coordinated backend+n8n secret contract. |
| RAI-003 | P1 | CODE_DESIGN | Scheduled action has no target snapshot/version/hash revalidation. | Not changed. Requires schema and business-rule design. |
| RAI-004 | P1 | CODE_BUG | Backend validates presence and target-type action but not the complete decision-action matrix. | Not changed. Source edits are outside this review scope. |
| RAI-005 | P1 | CONFIG_ENV / RELIABILITY | Backend timeout 15s, n8n OpenAI timeout 30s; no bounded retry/error workflow/idempotency. | Not changed. Documented V2 contract. |
| RAI-006 | P2 | FUNCTION_GAP | Image URLs are serialized as text; workflow does not submit image input. | Not changed. Requires guarded multimodal design. |
| RAI-007 | P2 | CODE_BUG / UI | Ask AI error renders behind active dialog; bulk failures have no per-report reason/retry. | Not changed. Review-only bound. |
| RAI-008 | P2 | TEST_BUG | Fast 502 counted as successful latency; no-job countdown/cancel and failed bulk paths can be marked pass. | Not changed. Existing test artifact retained as evidence; recommendations documented. |
| RAI-009 | P2 | TEST_DATA | No safe COMMENT target was available for the admin test account. | Not changed. Add deterministic COMMENT fixture. |
| RAI-010 | P3 | CONFIG_ENV | Runtime DB schema version is newer than latest migration in current checkout. | Outside feature scope; no source/config workaround applied. |

## Blocker chain

`duplicate/unregistered n8n production webhook -> POST 404 -> backend maps to 502 -> no AI contract -> no scheduled job -> safety gate/apply/cancel runtime cannot be verified`

## Why no source auto-fix was performed

The bounded task is a review, verification run, and detailed-design document. Allowed persistent changes were limited to `documents/`. Fixes requiring source, migration, n8n publication, secrets, or test code were intentionally not applied.
