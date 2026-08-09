# CAFE_PAGE Minimum Evidence — Sprint 1 Boundary

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-11` |
| Trạng thái | `MANUAL_ONLY_DESIGNED` |
| Deep target | Deferred |

Backend may capture:

- Page ID, active/status and updated timestamp;
- owner/manager internal association reference;
- sanitized public identity fields;
- report association.

Sprint 1 must not:

- infer page-wide policy breach from one post/listing;
- decide merchant license, ownership or jurisdiction;
- recommend `SUSPEND_PAGE`;
- expose owner PII to n8n/model.

Canonical result:

```text
NEEDS_MANUAL_REVIEW + NO_ACTION
blockedReason=TARGET_DEEP_POLICY_NOT_IN_SPRINT1
providerCalled=false
```

Future page design must cover ownership, merchant identity, aggregation, regulated-commerce
jurisdiction and page-level burden separately.
