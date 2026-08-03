# Evidence Provenance

Required:

```text
sourceSystem
sourceEntity/reference
collector
capturedAt
snapshot/version/hash
transformation/redaction
availability
```

Backend platform records use repository/entity provenance. External references require authority,
URL/reference, retrieval time and freshness. Evidence without provenance is `LOW/UNUSABLE` and
cannot satisfy a critical burden.

Redaction must be recorded so reviewers know whether material detail was removed.
