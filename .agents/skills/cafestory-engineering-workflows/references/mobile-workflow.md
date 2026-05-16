# React Native Mobile Workflow

Load this reference for work in `3-cafe-story-reactnative-mobile`.

## Discovery

1. Inspect `package.json` scripts and dependencies.
2. Search navigation and screens:
   - `rg "Navigation|Navigator|Screen|Stack|Tab" 3-cafe-story-reactnative-mobile`
   - `rg "fetch\\(|axios|api" 3-cafe-story-reactnative-mobile`
3. Reuse existing API client, auth storage, navigation, styling, and state patterns.

## Implementation Rules

- Keep screens ergonomic for repeated social actions: posting, reacting, commenting, following, reporting.
- Handle loading, empty, error, offline-ish, and unauthorized states when relevant.
- Avoid introducing new navigation/state libraries unless already used.
- Keep backend DTO/API assumptions explicit in the implementation note.
- Preserve platform-specific conventions already present in the app.

## Validation

Use actual scripts from `package.json`, commonly:

```powershell
npm run lint
npm test
npm run typecheck
```

For native builds, only run heavier commands when the user asks or the change touches native configuration.
