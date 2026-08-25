# Output Schema V2

Required:

```text
contractVersion
correlationId
recommendationState
reportDecision
candidateTargetAction
findings[]
evidenceAssessment
riskAssessment
blockedReasons[]
explanation
versions
```

Finding requires Rule ID/version, outcome, supporting/counter/missing Evidence IDs,
categorical likelihood and rationale.

Strict guards:

- `additionalProperties=false`;
- categorical enums only;
- bounded arrays/strings;
- no raw provider response;
- no numeric confidence/risk authority;
- schema validity does not replace Backend semantic validation.

Canonical example and compatibility mapping are in Sprint 1 DD.
