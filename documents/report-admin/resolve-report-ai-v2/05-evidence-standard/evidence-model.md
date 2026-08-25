# Evidence Model

> Trạng thái: skeleton cũ đã được thay thế về mặt normative bởi
> `common-evidence-envelope.md` và `evidence-kind-catalog.md`.

```text
Claim
→ Target Metadata / Target Snapshot
→ Evidence Item
→ Observation
→ Finding (supporting/counter/missing Evidence IDs)
→ Quality and Sufficiency
→ Recommendation
→ Human Decision
→ Optional Execution
```

Evidence item dùng Common Evidence Envelope:

```text
evidenceId
envelopeVersion
evidenceKind
subject
source
capture
integrity
availability
quality
privacy
intendedUse
collectedForRuleIds[]
payload
```

Không còn field free-text `observation` hoặc `supportsRuleIds` trong evidence item.

Finding giữ Rule ID/version, outcome, supporting/counter Evidence IDs, missing requirements,
likelihood và rationale. Rationale là derived text và không bao giờ là evidence.
