# Backend Workflow

Load this reference for Java Spring Boot backend features, reviews, refactors, entities, DTOs, services, repositories, security, or database work.

## Discovery

1. Inspect `pom.xml` for available test/build commands.
2. Search existing domain patterns before adding files:
   - Controllers: `rg "class .*Controller" 1-cafe-story-backend-javaspring`
   - Services: `rg "interface .*Service|class .*ServiceImpl" 1-cafe-story-backend-javaspring`
   - DTOs: `rg "Request|Response" 1-cafe-story-backend-javaspring`
   - Mappers: `rg "@Mapper|Mapper" 1-cafe-story-backend-javaspring`
3. Identify package layout and mirror it.

## Implementation Sequence

Use this sequence for new backend capabilities:

1. Domain model or existing entity update.
2. Repository query changes.
3. Request/response DTOs.
4. MapStruct mapper.
5. Service interface.
6. Service implementation with business rules and transactions.
7. Controller endpoint.
8. Security/authorization integration.
9. Tests or focused validation.

## Architecture Checks

- Controller returns response DTOs or response wrappers, not entities.
- Controller delegates business behavior to service.
- Service does not return entities to controller when a response DTO is expected.
- Mapper owns entity-to-DTO conversion.
- Repository does not contain business logic.
- Exceptions follow existing project style.
- Validation annotations follow existing DTO style.

## Database Checks

- IDs are UUID strings unless existing schema proves otherwise.
- Table names use snake_case.
- Relationship mappings avoid accidental eager loading unless project patterns require it.
- New persistence behavior considers pagination for list endpoints.

## Security Checks

- Match existing JWT/auth principal extraction.
- Do not trust user IDs from request bodies when authenticated identity should own the action.
- Admin-only behavior must use existing authorization conventions.

## Validation

Prefer the narrowest meaningful command first:

```powershell
mvn test
mvn -DskipTests package
```

Run from `1-cafe-story-backend-javaspring` unless the repo defines wrapper scripts elsewhere.
