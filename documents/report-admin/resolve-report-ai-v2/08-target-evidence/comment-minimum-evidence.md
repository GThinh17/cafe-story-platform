# COMMENT Minimum Evidence — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| Deep target | Có |

## Snapshot fields

- comment ID, status, created/updated timestamp;
- exact comment text;
- internal author reference;
- parent BLOG ID/status;
- bounded parent excerpt/context;
- reply-parent context if available and material;
- report-to-comment association.

## Context guards

- Comment meaning may depend on parent or replied-to text.
- Parent content is context evidence, not automatically the reported target.
- Do not attribute parent violation to comment author.
- Deleted/unavailable parent or ambiguous pronoun/quote → manual when material.
- Emoji/short text without context cannot be expanded using model imagination.
- Thread-wide harassment/spam pattern needs an approved multi-item collector; Sprint 1 single
  comment snapshot is insufficient for pattern conclusion.

## Required outcomes

```text
context available + rule-specific evidence sufficient
  → content-level recommendation

context missing/conflicted/materially ambiguous
  → NEEDS_MANUAL_REVIEW + NO_ACTION
```

Safe COMMENT fixtures are mandatory for full E2E claim. Missing fixture is `TEST_DATA/BLOCKED`,
not a reason to fabricate runtime data or call the flow fully passed.
