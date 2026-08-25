# Idempotency Design — Sprint 1

## Key

```text
SHA256(environment | reportId | targetType | targetId | snapshotHash |
       contractVersion | policyVersion | ruleCatalogVersion |
       promptVersion | workflowVersion)
```

## Behavior

- completed same key → return same persisted result;
- in-flight same key → bounded wait or `AI_RECOMMENDATION_IN_PROGRESS`;
- new snapshot/version → new key;
- terminal report never returns cached recommendation as newly eligible;
- DB unique index is final race guard;
- retry to n8n keeps correlation/idempotency but uses a fresh nonce.

## Non-goals

Idempotency does not make provider calls free of duplicate cost under network ambiguity. It prevents
duplicate CafeStory records and actions; A0 independently prevents mutation.
