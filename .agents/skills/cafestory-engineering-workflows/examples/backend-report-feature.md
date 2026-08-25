# Example: Backend Report Feature

User request:

```text
Implement backend support for reporting inappropriate posts.
```

Skill workflow:

1. Read `references/backend-workflow.md`.
2. Inspect existing blog/post, user, security, and report patterns.
3. Create or update `Report` entity only if no existing domain model covers it.
4. Add request/response DTOs such as `CreateReportRequest` and `ReportResponse`.
5. Add MapStruct mapper.
6. Add repository, service interface, service implementation, and controller.
7. Ensure authenticated user identity is used as reporter.
8. Validate with Maven tests or package command.

Expected output:

- Code changes.
- Focused tests if project test patterns exist.
- Summary using `templates/implementation-summary.md`.
