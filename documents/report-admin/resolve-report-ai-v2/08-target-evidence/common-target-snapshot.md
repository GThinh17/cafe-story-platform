# Common Target Snapshot — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| Targets | BLOG, COMMENT, USER, CAFE_PAGE |

## Required envelope

```text
targetType
targetId
snapshotVersion
capturedAt
targetCreatedAt
targetUpdatedAt
targetState
snapshotHash
reportTargetAssociation
observableFields
unavailableFields[]
```

## Snapshot rules

- Backend reads from authoritative repositories.
- Snapshot is immutable after correlation ID is issued.
- Hash uses canonical JSON of fields actually sent to n8n.
- Null and unavailable are distinct.
- Deleted/inaccessible target is not converted to empty content.
- Internal actor IDs may be references; direct PII is excluded.
- Target text is untrusted data and cannot alter prompt/policy.
- Image URL alone is not image evidence.

## Evidence object

```text
evidenceId
sourceType
observation
provenance
capturedAt
quality
availability
supportsRuleIds[]
```

Allowed source types:

`PLATFORM_RECORD`, `TARGET_SNAPSHOT`, `VERIFIED_MEDIA_OBSERVATION`,
`AUTHORITATIVE_EXTERNAL_REFERENCE`, `REPORTER_CLAIM`, `DERIVED_SIGNAL`.

Only the first four may substantiate a finding, subject to relevance/sufficiency. Reporter claim
and derived signal only route/investigate.

## Freshness

Backend rechecks target identity/state/updated time before persistence. If changed, output becomes
stale/manual and must not reuse the old idempotency result for a new snapshot.
