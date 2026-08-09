# Runtime Issues Fixed

## ISSUE-001
- Classification: EXTERNAL_SERVICE
- Evidence: earlier n8n execution data showed OpenAI rejected the provider schema because `uniqueItems` is not accepted in response format JSON schema.
- Action: removed `uniqueItems` from the provider output schema and synced the n8n workflow JSON.

## ISSUE-002
- Classification: CODE_BUG
- Evidence: runtime A1 calls initially returned `POLICY_OR_RULE_CATALOG_NOT_ACTIVE`, preventing any non-manual action.
- Action: switched the backend runtime rule context from proposed lifecycle values to `ACTIVE` / `ACTIVE_RUNTIME`.

## ISSUE-003
- Classification: CODE_BUG
- Evidence: existing Flyway constraint accepted only `A0_RECOMMEND_ONLY`.
- Action: added a forward migration allowing `A1_AUTO_HIDE_BLOG_COMMENT`.

## ISSUE-004
- Classification: CODE_BUG
- Evidence: auto-apply worker threw `LazyInitializationException` while processing due jobs.
- Action: claim and apply due jobs in one transaction.

## ISSUE-005
- Classification: CODE_BUG
- Evidence: provider could attribute sibling comment text to the target comment.
- Action: removed sibling comment excerpts from provider projection and added a backend scam/spam auto-hide guard based on target text.
