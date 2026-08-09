# Model and Prompt Versioning

Persist for every V2 recommendation:

```text
contractVersion
policyVersion
ruleCatalogVersion
promptVersion
workflowId/version
requestedModelAlias
resolvedModelIdentity
snapshotHash
```

Rules:

- immutable semantic version for prompt/schema;
- environment alias alone is insufficient;
- changed model/prompt/workflow creates a new idempotency key;
- no silent fallback model;
- rollback preserves failed/replaced version history;
- model-change gate and slice evaluation required before activation.
