# Next.js Web Workflow

Load this reference for work in `2-cafe-story-nextjs-web`.

## Mandatory Next.js Rule

This project states:

```text
This is NOT the Next.js you know.
Read the relevant guide in node_modules/next/dist/docs/ before writing any code.
Heed deprecation notices.
```

Before framework-specific changes:
1. Inspect `2-cafe-story-nextjs-web/AGENTS.md`.
2. Locate relevant docs under `2-cafe-story-nextjs-web/node_modules/next/dist/docs/`.
3. Prefer existing project examples over memory.

If `node_modules` or docs are absent, report the limitation and use only patterns already present in the repo.

## Discovery

1. Inspect `package.json` scripts.
2. Search routes/pages/components:
   - `rg --files 2-cafe-story-nextjs-web`
   - `rg "fetch\\(|axios|useQuery|server action|route.ts|page.tsx" 2-cafe-story-nextjs-web`
3. Identify existing API client, auth/session handling, forms, and Tailwind conventions.

## Implementation Rules

- Keep UI consistent with existing components and Tailwind usage.
- Avoid adding a new state library, form library, or component system unless already used or explicitly requested.
- Keep API contracts aligned with backend DTOs.
- Handle loading, empty, error, and unauthorized states when user-facing behavior requires them.
- Keep text concise and domain-specific; avoid explanatory in-app text about implementation.

## Validation

Prefer scripts from `package.json`, commonly:

```powershell
npm run lint
npm run typecheck
npm test
npm run build
```

Use the actual script names found in the project.
