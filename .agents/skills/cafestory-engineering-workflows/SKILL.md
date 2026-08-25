---
name: cafestory-engineering-workflows
description: Deterministic CafeStory engineering workflows for implementing, reviewing, refactoring, and validating features across the Java Spring Boot backend, Next.js TypeScript web app, and React Native mobile app. Use when a task mentions CafeStory domain features, backend services/controllers/entities/DTOs/mappers, Next.js routes/components/API integration, React Native screens, database changes, clean architecture enforcement, multi-agent implementation planning, or project convention validation. Do not use for unrelated Codex CLI resource-list maintenance under tools/awesome-codex-cli, generic writing tasks, or changes outside the CafeStory product unless explicitly requested.
---

# CafeStory Engineering Workflows

## Purpose

Use this skill to execute CafeStory software work with deterministic discovery, clean architecture, bounded context loading, and reproducible artifacts.

Argument hint: provide the requested feature/fix, target app if known (`backend`, `web`, `mobile`), affected domain, and expected artifact.

## Core Workflow

1. Discover scope before editing.
   - Run `scripts/check_project.py <repo-root>` when structure or validation commands are unclear.
   - Read only the relevant reference file below.
   - Inspect existing code patterns before adding new abstractions.
2. Classify the request.
   - Backend API/domain work: read `references/backend-workflow.md`.
   - Database, entity, migration, relationship, or repository work: read `references/db-context.md`.
   - Next.js web work: read `references/web-workflow.md`.
   - Mobile work: read `references/mobile-workflow.md`.
   - Cross-app or planning work: read `references/project-conventions.md` and the relevant workflow files.
3. Produce a short implementation plan for substantial work.
   - Use `templates/feature-plan.md` when the user asks for a plan or when work spans multiple layers.
   - Skip long planning for one-file fixes.
4. Implement with local conventions.
   - Keep changes scoped to requested behavior.
   - Prefer existing helpers, folders, and naming.
   - Add focused tests or validation proportional to risk.
5. Validate deterministically.
   - Use `references/validation.md` for command selection.
   - Report commands run, failures, and residual risk.

## Responsibilities

- Own feature implementation workflows for CafeStory backend, web, and mobile code.
- Enforce DTO/mapper boundaries, service/repository layering, UUID IDs, and naming conventions.
- Guide artifact generation: plans, implementation notes, API contracts, review checklists, and validation reports.
- Coordinate multi-agent work only when the user explicitly asks for subagents or parallel agent work.

## Non-Responsibilities

- Do not maintain the separate `tools/awesome-codex-cli` resource list unless the user asks for that repository.
- Do not invent missing business rules, schemas, endpoints, or UI behavior. Infer from code only when evidence is present; otherwise mark assumptions.
- Do not expose JPA entities directly in API responses.
- Do not rewrite architecture, change stacks, or add broad abstractions unless required by the task.
- Do not rely on remembered Next.js conventions without checking this project's docs/rules first.

## Expected Inputs

- User request describing a feature, bug, refactor, review, or generated artifact.
- Optional target area: `1-cafe-story-backend-javaspring`, `2-cafe-story-nextjs-web`, or `3-cafe-story-reactnative-mobile`.
- Optional acceptance criteria, endpoint names, screens, database fields, or business rules.

## Expected Outputs

- Code changes, tests, or generated artifacts requested by the user.
- For plans: a concise file-aware plan using `templates/feature-plan.md`.
- For implementation summaries: touched files, behavior changed, validation run, and unresolved risks.
- For reviews: findings first with file and line references, then test gaps and assumptions.

## Constraints

- Use deterministic shell discovery (`rg`, `rg --files`, package manifests, Maven/Node scripts) before implementation.
- Load references progressively; do not read every reference by default.
- Keep output concise and file-aware.
- Preserve user changes and unrelated worktree changes.
- Use scripts/templates from this skill instead of recreating repeated checklists.

## Project Conventions

Read `references/project-conventions.md` when a task needs business context, stack details, naming rules, or architecture constraints.
Read `references/db-context.md` when a task needs database tables, relationships, enum values, or source-of-truth rules.

Essential conventions:
- Backend stack: Java 21, Spring Boot 3, Spring Security, JWT, Maven, MapStruct, Lombok, JPA/Hibernate.
- Frontend stack: Next.js, TypeScript, TailwindCSS.
- Mobile stack: React Native, inferred from `3-cafe-story-reactnative-mobile`.
- Database: PostgreSQL / Supabase PostgreSQL.
- Backend flow: `Controller -> Service -> Repository -> Database`.
- DTO and Mapper are required between layers.
- API responses must not expose Entity classes directly.
- Entity names use singular PascalCase; table names use snake_case.
- DTOs end with `Request` or `Response`; implementations end with `Impl`.
- Database schema source of truth lives in `references/cafestory-schema.dbml`.

## Orchestration Guidance

Use subagents only when the user explicitly asks for agents, delegation, or parallel work. Split by disjoint ownership:
- Backend worker: controllers, DTOs, services, repositories, mappers, backend tests.
- Web worker: Next.js routes/components/state/API client, web tests.
- Mobile worker: React Native screens/navigation/API client, mobile tests.
- Verification worker: read-only validation, regression risk, missing tests.

Tell workers they are not alone in the codebase, must not revert others' edits, and must list changed files.

## Examples

- "Implement report inappropriate content" -> read backend workflow, inspect report patterns, create request/response DTOs, mapper, service, repository, controller, tests.
- "Add premium subscription page in web" -> read web workflow, inspect Next docs under `node_modules/next/dist/docs/`, reuse Tailwind patterns, validate TypeScript/lint.
- "Create an implementation plan for comments across backend and mobile" -> use `templates/feature-plan.md`, read project conventions plus backend/mobile workflows.
- "Review my CafeStory backend change" -> use review stance, check entity exposure, layering, DTO naming, transaction/security gaps, and tests.

## Failure Handling

- If required project files are missing, run `scripts/check_project.py` and report the missing structure.
- If validation commands are unavailable or dependencies are missing, report the exact command and blocker.
- If business behavior is underspecified, inspect existing code for precedent; if still ambiguous, state assumptions before editing.
- If Next.js docs are missing, report that the project rule could not be followed and avoid framework-specific rewrites unless safe from local code patterns.
