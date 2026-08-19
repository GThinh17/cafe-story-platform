# Workflow improvement

- Add a mobile test runner and changed-file coverage instrumentation so search service behavior can be tested without relying only on TypeScript and browser runtime checks.
- Add an authenticated Android smoke-test lane for Login → Feed → Comments → Explore Search.
- Add a safe test-user fixture lifecycle or admin-only cleanup endpoint for local E2E data.
- Keep Expo runtime on a documented allowed origin, or parameterize the development CORS allowlist explicitly instead of starting on arbitrary ports.
- Add a backend performance regression test that asserts Explore Search uses bounded queries and never calls the legacy full-user listing service.
