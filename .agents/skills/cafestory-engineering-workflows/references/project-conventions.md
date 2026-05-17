# CafeStory Project Conventions

Load this reference for cross-app planning, architecture decisions, naming checks, or business context.

## Product Scope

CafeStory is a social blogging platform with normal users and administrators.

Core capabilities:
- Create blogs/posts.
- Follow users and pages.
- Like, comment, and share content.
- Create community pages.
- Report inappropriate content.
- Purchase premium subscriptions.
- Receive notifications.
- Interact socially in a model similar to Facebook plus Medium.

## Repository Areas

- `1-cafe-story-backend-javaspring`: Java Spring Boot backend.
- `2-cafe-story-nextjs-web`: Next.js TypeScript web app.
- `3-cafe-story-reactnative-mobile`: React Native mobile app.


## Stack

Backend:
- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- Maven
- MapStruct
- Lombok
- JPA/Hibernate

Web:
- Next.js
- TypeScript
- TailwindCSS

Mobile:
- React native

Database:
- PostgreSQL
- Supabase PostgreSQL

## Architecture Rules

Backend layering:

```text
Controller -> Service -> Repository -> Database
```

Rules:
- Use DTOs at API boundaries.
- Use MapStruct mappers between entities and DTOs.
- Never expose JPA Entity classes directly in API responses.
- Keep controllers thin; put business rules in services.
- Keep repositories focused on persistence queries.
- Keep security/JWT checks consistent with existing filters/config.

## Naming Rules

Entities:
- Singular PascalCase: `User`, `Blog`, `Page`, `Comment`, `Report`.

Tables:
- snake_case: `users`, `blog`, `page`, `pricing_rule`.

DTOs:
- Request DTOs end with `Request`.
- Response DTOs end with `Response`.
- Examples: `CreateBlogRequest`, `BlogResponse`.

Services:
- Interface: `UserService`, `BlogService`.
- Implementation: `UserServiceImpl`, `BlogServiceImpl`.

Database IDs:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private String id;
```

## Artifact Rules

Generated plans and summaries should include:
- Scope.
- Files likely touched or changed.
- Business assumptions.
- Validation commands.
- Risks or TODOs that remain.

## Database Context

Use `db-context.md` for table ownership, source-of-truth rules, and relationship guidance.
Use `cafestory-schema.dbml` when exact columns, enums, indexes, or foreign keys are needed.
