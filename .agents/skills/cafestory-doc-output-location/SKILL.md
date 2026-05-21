---
name: cafestory-doc-output-location
description: Enforce CafeStory documentation output location. Use whenever Codex creates, updates, moves, or references project documentation, API docs, implementation notes, plans, guides, Postman docs, markdown files, generated docs, or documentation artifacts in this repository; documentation should be written under the repository-root `documents/` folder instead of app-specific source folders unless the user explicitly requests a different path.
---

# CafeStory Doc Output Location

## Rule

Write generated documentation into the repository-root `documents/` directory.

Examples:

- Use `E:\LuanVanToTNghiep\cafe-story-platform\documents\API_DOCUMENTATION.md`.
- Use `E:\LuanVanToTNghiep\cafe-story-platform\documents\auth-cookie-jwt-flow.md`.
- Use `E:\LuanVanToTNghiep\cafe-story-platform\documents\postman_collection.json` when the file is documentation/test artifact rather than application code.

## Workflow

1. Resolve the repository root from the current workspace.
2. Prefer the existing root-level `documents/` directory.
3. If `documents/` is missing, create it at the repository root before writing docs.
4. Keep generated docs out of these app folders unless the user explicitly asks otherwise:
   - `1-cafe-story-backend-javaspring/`
   - `2-cafe-story-nextjs-web/`
   - `3-cafe-story-reactnative-mobile/`
5. If an existing doc is already in an app folder, ask whether to move it or update it in place when moving could break links or workflows.

## Exceptions

- Keep source code comments, inline code documentation, and framework-required files in their normal code locations.
- Keep database migration SQL under the backend migration folder.
- Honor an explicit user path even if it is outside `documents/`.
