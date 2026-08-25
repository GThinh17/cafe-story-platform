# Workflow improvement

- Add a focused regression fixture for `latestComputedAt == null`; exception-only fallback tests do not cover the internal fallback return.
- Treat cached feed objects as ranking/content payloads and explicitly audit every viewer-specific field whenever cache semantics change.
- Add a small web test runner so mixed-item ordering, first-post eager loading, owner-only pricing, and redirects can be covered without relying on mutable seed data.
- Maintain one reusable local owner fixture with an unused paid Ads package and one deterministic ad-first feed fixture for smoke environments; never create a real payment during routine validation.
- Run heavy Maven and Next production builds sequentially on this Windows workstation; parallel execution caused a non-diagnostic Next worker exit.
- After merge resolution, run a caller/route inventory (`rg`) for duplicate services, API clients, and compatibility routes before UI smoke.
