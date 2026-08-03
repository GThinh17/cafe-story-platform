# BLOG Minimum Evidence — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| Deep target | Có |

## Snapshot fields

- blog ID, status, created/updated timestamp;
- title and sanitized content/body;
- author internal ID and optional Cafe Page association;
- visible image/media references with availability status;
- report-to-blog association;
- minimum surrounding fields needed by selected rule.

## Rule-specific minimum

| Rule family | Minimum beyond content snapshot |
|---|---|
| Spam | repetition/unsolicited observation; one post alone may be insufficient |
| Harassment/Hate | exact targeted expression + target/group context |
| Violence/Self-harm | exact threat/encouragement + target/context |
| Sexual | verified media/text observation + age/context flags when material |
| Impersonation/Scam | represented identity/offer + authoritative platform record |
| Misinformation | exact material claim + authoritative counter-source |
| Restricted goods | item/service + transaction intent + jurisdiction when regulated |
| IP/Privacy | claimant/authority/consent route; human/legal default |

## Abstention

- critical media unreadable;
- content truncated at material point;
- target edited after snapshot;
- quoted/reported speech cannot be distinguished;
- pattern-based rule has only one event;
- source/jurisdiction/authority required but missing.

All cases above route manual; they do not produce `REJECT`.
