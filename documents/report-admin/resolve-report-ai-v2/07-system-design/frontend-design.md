# Admin Frontend Design — Sprint 1

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `DESIGNED_FOR_G0-12_REVIEW` |
| App | `5-cafe-story-nextjs-admin` |

## Information architecture

```text
Recommendation status
Evidence sufficiency / blocked reasons
Findings by Rule ID
Evidence used
Counter-evidence
Missing/unreadable evidence
Likelihood / harm / action risk
AI rationale — not evidence
Version and snapshot context
Legacy history / legacy jobs
```

## Component changes

`admin-reports-page.tsx`:

- delete single/bulk auto-apply creation controls and delay selectors;
- retain cancel for a legacy `SCHEDULED` job;
- replace `Success/Scheduled/Skipped/Failed` with
  `Recommended/Manual review/Failed/Remaining`;
- render explicit A0 banner;
- render evidence-first V2 card;
- render V1 history in collapsed legacy card;
- disable Ask AI for terminal reports;
- show USER/PAGE manual-only reason without waiting for provider.

`types/admin.ts`:

- add V2 categorical enums and evidence/finding/version types;
- add canonical `NO_ACTION/KEEP_VISIBLE`;
- retain `NONE/APPROVE` as legacy union values;
- remove `rawResponse` from V2 public type;
- add `automationOutcome`.

`lib/api/admin.ts`:

- default create call sends no auto-apply body;
- parse structured operational errors;
- do not retry non-idempotent request in client without idempotency support.

## Copy rules

Use:

- “AI recommendation only”.
- “Evidence is insufficient; manual review is required.”
- “AI rationale — not evidence.”
- “No action has been applied.”

Do not use:

- “High confidence means safe”.
- generic “Risk” without type;
- “Approved” for `KEEP_VISIBLE`;
- “Success” when only scheduling/recommendation stage succeeded.

## Accessibility and state

- status not color-only;
- evidence sections use headings and list semantics;
- loading, empty, error, stale and manual are distinct;
- long evidence text wraps and is sanitized;
- correlation/version data copyable without exposing secret;
- bulk focus remains inside dialog and result rows announce failures.

## Responsive layout

- one-column evidence card on mobile widths;
- two-column likelihood/harm/action-risk at desktop only;
- no horizontal scroll for Evidence ID/rule code;
- raw JSON is not rendered as a UI fallback.
