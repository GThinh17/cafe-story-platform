---
name: spring-unit-api-testing
description: Create, review, and improve Java Spring Boot tests using JUnit 5, Mockito, MockMvc, AssertJ, and Bean Validation. Use when Codex needs to write or review unit tests for service layer, controller tests, validator tests, DTO validation tests, API endpoint tests, authorization tests, repository mocking, exception-path coverage, or regression tests in a Spring Boot backend.
---

# Spring Unit API Testing

## Purpose

Use this skill to write focused, maintainable tests for Java Spring Boot backend code.

Prefer narrow tests first:
- Service tests with JUnit 5 + Mockito.
- Controller tests with MockMvc or WebMvcTest.
- Validation tests for custom validators and DTO annotations.
- API behavior tests for request/response shape, status code, auth, and error handling.

Do not rewrite production code unless a test exposes a real bug and the user requested fixes.

## Discovery

Before writing tests:

1. Inspect the class under test and its public methods.
2. Inspect existing tests in the same package/module.
3. Identify project conventions:
   - Test framework: JUnit 5.
   - Mocking: Mockito.
   - Assertions: AssertJ when available.
   - Controller testing: MockMvc when available.
4. Inspect exception style:
   - `ResponseStatusException`
   - custom exceptions
   - validation exceptions
   - global exception handler response format
5. Check build command from `pom.xml`.

Prefer existing naming, package layout, helper methods, fixture builders, and assertion style.

## Service Test Workflow

Use Mockito unit tests for service classes when business logic can be tested without Spring context.

Recommended structure:

```java
@ExtendWith(MockitoExtension.class)
class SomeServiceImplTest {

    @Mock
    private SomeRepository someRepository;

    @Mock
    private OtherDependency otherDependency;

    private SomeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SomeServiceImpl(someRepository, otherDependency);
    }

    @Test
    void methodName_condition_expectedResult() {
        // arrange
        // act
        // assert
    }
}
