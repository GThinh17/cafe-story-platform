# Missing Evidence

Missing evidence record:

```text
missingEvidenceId
requiredByRuleId
requirementType
requiredEvidenceKind
semanticRequirementCode
triggerCondition
description
critical
availability
reasonCode
recommendedNextStep
decisionEffect
```

Critical missing evidence → `INSUFFICIENT/UNASSESSABLE` and manual/no action. It never means
`REJECT`. Unavailable media, context, claimant authority, jurisdiction and pattern evidence are
explicit—not hidden in free-text explanation.

`requirementType` phân biệt:

- `EVIDENCE_KIND`: kind đã có trong M05 nhưng item không khả dụng;
- `SEMANTIC_REQUIREMENT`: burden đã xác định ở M06 nhưng kind/collector còn deferred;
- `POLICY_CONTEXT`: registry/list/version cần active;
- `OBSERVATION_REQUIREMENT`: evidence có nhưng observation bắt buộc chưa chứng minh được.

Chi tiết và examples: `rule-to-evidence-requirement-matrix.md`.
