# CafeStory Review Checklist

## Backend

- Controllers return DTOs, not entities.
- Services own business rules.
- Repositories only handle persistence.
- DTO names end with `Request` or `Response`.
- MapStruct mapper exists when converting entity/DTO.
- UUID string IDs follow project convention.
- Authenticated actions do not trust user-controlled owner IDs.

## Web

- Next.js project docs/rules were checked before framework-specific changes.
- Existing routing/component/API patterns were reused.
- Loading, error, empty, and unauthorized states are handled where relevant.

## Mobile

- Existing navigation/API/state patterns were reused.
- User-facing async states are handled.
- Native build impact is called out when relevant.

## Validation

- Relevant tests/lint/typecheck/build commands were run or blockers are stated.
