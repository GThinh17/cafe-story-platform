# USER Minimum Evidence — Sprint 1 Boundary

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `MANUAL_ONLY_DESIGNED` |
| Deep target | Deferred |

Backend may capture:

- internal user ID;
- account active/state;
- profile updated timestamp;
- report association;
- sanitized public profile fields strictly needed for Admin context.

Sprint 1 must not:

- infer repeated/coordinated abuse from one report;
- send private profile/contact/session data;
- call provider to recommend `SUSPEND_USER`;
- convert one BLOG/COMMENT finding into actor-level finding.

Canonical result:

```text
NEEDS_MANUAL_REVIEW + NO_ACTION
blockedReason=TARGET_DEEP_POLICY_NOT_IN_SPRINT1
providerCalled=false
```
