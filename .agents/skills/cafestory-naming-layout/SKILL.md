---
name: cafestory-naming-layout
description: Enforce CafeStory backend naming and package layout conventions for Java Spring Boot code. Use when Codex creates, reviews, or refactors backend services, service implementation tests, controller tests, DTOs, request/response models, package placement, or file naming in the CafeStory project.
---

# CafeStory Naming Layout

## Purpose

Use this skill to keep CafeStory backend files placed and named consistently.

Apply these conventions before creating or moving backend files.

## Backend Root

Backend code lives in:

`1-cafe-story-backend-javaspring/src/main/java/com/cafestory`

`1-cafe-story-backend-javaspring/src/test/java/com/cafestory`

Do not invent new top-level packages unless the existing codebase already has that pattern.

## Service Implementation Tests

All service implementation tests must be placed under the test `service` package.

Correct:

`src/test/java/com/cafestory/service/UserServiceImplTest.java`

`src/test/java/com/cafestory/service/ReviewerServiceImplTest.java`

`src/test/java/com/cafestory/service/CommentServiceImplTest.java`

Incorrect:

`src/test/java/com/cafestory/reviewer/ReviewerServiceImplTest.java`

`src/test/java/com/cafestory/user/UserServiceImplTest.java`

`src/test/java/com/cafestory/service/serviceImplement/UserServiceImplTest.java`

Naming rule:

`{Domain}ServiceImplTest.java`

Examples:

`UserServiceImplTest`

`ReviewerServiceImplTest`

`BlogServiceImplTest`

`NotificationServiceImplTest`

## Controller Tests

All controller tests must be placed under the test `controller` package.

Correct:

`src/test/java/com/cafestory/controller/UserControllerTest.java`

`src/test/java/com/cafestory/controller/ReviewerControllerTest.java`

`src/test/java/com/cafestory/controller/NotificationControllerTest.java`

Incorrect:

`src/test/java/com/cafestory/user/UserControllerTest.java`

`src/test/java/com/cafestory/reviewer/ReviewerControllerTest.java`

`src/test/java/com/cafestory/api/UserControllerTest.java`

Naming rule:

`{Domain}ControllerTest.java`

## Validation Tests

Validator tests must be placed under:

`src/test/java/com/cafestory/validation`

Correct:

`src/test/java/com/cafestory/validation/UserValidatorTest.java`

`src/test/java/com/cafestory/validation/BlogValidatorTest.java`

## DTO Placement

DTOs must be placed directly inside either `requestDTO` or `responseDTO`.

Correct:

`src/main/java/com/cafestory/dto/requestDTO/CreateUserRequestDTO.java`

`src/main/java/com/cafestory/dto/requestDTO/UpdateUserRegionRequestDTO.java`

`src/main/java/com/cafestory/dto/responseDTO/UserResponseDTO.java`

`src/main/java/com/cafestory/dto/responseDTO/ReviewerStatsResponseDTO.java`

Incorrect:

`src/main/java/com/cafestory/dto/requestDTO/user/CreateUserRequestDTO.java`

`src/main/java/com/cafestory/dto/responseDTO/chat/ChatResponseDTO.java`

`src/main/java/com/cafestory/dto/responseDTO/reviewer/ReviewerStatsResponseDTO.java`

Do not create domain subfolders under DTO packages.

Use flat DTO folders:

`dto/requestDTO`

`dto/responseDTO`

Not nested DTO folders:

`dto/requestDTO/user`

`dto/requestDTO/chat`

`dto/responseDTO/user`

`dto/responseDTO/chat`

`dto/responseDTO/reviewer`

## DTO Naming

Request DTOs must end with:

`RequestDTO`

Response DTOs must end with:

`ResponseDTO`

Good:

`LoginRequestDTO`

`RegisterRequestDTO`

`UpdateUserRegionRequestDTO`

`UserResponseDTO`

`RegionResponseDTO`

`ReviewerStatsResponseDTO`

Avoid redundant names:

`ChatDTO`

`ChatResponse`

`ResponseDTOChat`

`UserRequest`

## Service Code Placement

Service interfaces live in:

`src/main/java/com/cafestory/service/serviceInterface`

Service implementations live in:

`src/main/java/com/cafestory/service/serviceImplement`

Naming:

`UserService.java`

`UserServiceImpl.java`

`ReviewerService.java`

`ReviewerServiceImpl.java`

## Controller Placement

Controllers live in:

`src/main/java/com/cafestory/controller`

Naming:

`UserController.java`

`ReviewerController.java`

`NotificationController.java`

## Repository Placement

Repositories live in:

`src/main/java/com/cafestory/repository`

Naming:

`UserRepository.java`

`ReviewerRepository.java`

`RegionRepository.java`

## Entity Placement

Entities live in:

`src/main/java/com/cafestory/entity`

Entity names use singular PascalCase:

`User`

`Reviewer`

`Region`

`CafePage`

`Notification`

Table names should use snake_case.

## Before Creating Files

Before adding a new file:

1. Search for existing equivalent files.
2. Mirror existing package style.
3. Prefer moving misplaced new files into the correct package instead of creating parallel structures.
4. Do not create nested DTO folders.
5. Put service tests in `test/java/com/cafestory/service`.
6. Put controller tests in `test/java/com/cafestory/controller`.

## Review Checklist

When reviewing a backend change, flag these issues:

- `ServiceImplTest` is outside `src/test/java/com/cafestory/service`.
- `ControllerTest` is outside `src/test/java/com/cafestory/controller`.
- DTOs are nested under domain folders like `responseDTO/chat`.
- DTO names do not end with `RequestDTO` or `ResponseDTO`.
- Entity is exposed directly from controller.
- Service implementation is not in `service/serviceImplement`.
- Service interface is not in `service/serviceInterface`.
- Repository is not in `repository`.
