---
name: cafestory-mock-data-conventions
description: Enforce CafeStory frontend mock-data placement. Use when Codex creates, reviews, or refactors fake/sample/placeholder/demo UI data in the Next.js web app, especially arrays or objects used by pages, components, fixtures, prototypes, or Figma-to-code screens.
---

# CafeStory Mock Data Conventions

## Rule

Keep fake UI data out of route files and reusable components.

For `2-cafe-story-nextjs-web`, place mock data under:

```text
src/mocks/
```

Use pages and components to render data, not define large fake datasets inline.

## Workflow

1. Before adding fake data, inspect `src/mocks/` for an existing domain or screen file.
2. Put mock arrays/objects in a named export:
   - `src/mocks/feed.ts` for home feed/story/sidebar data.
   - `src/mocks/cafes.ts` for cafe lists/details.
   - `src/mocks/reviews.ts` for review/post data shared across screens.
   - `src/mocks/users.ts` for profiles, authors, suggestions, or account data.
3. Put reusable TypeScript data shapes under `src/types/`, not inside page files.
4. Import mock data into pages or demos:

```ts
import { mockFeedPosts } from "@/mocks/feed";
```

5. When replacing mock data with backend/API data, keep the component contract stable and remove unused mock exports.

## Do And Do Not

- Do keep small, one-off display constants in a component only when they are truly UI labels, such as `["Like", "Comment", "Share"]`.
- Do move realistic fake entities, cards, suggestions, stories, posts, cafes, users, and reviews into `src/mocks`.
- Do name mock exports with a `mock` prefix, such as `mockStories` or `mockTopCafes`.
- Do not define large fake arrays in `page.tsx`, layout files, or reusable components.
- Do not import mocks into production API clients or service functions.
- Do not mix DTO/API response mapping logic into mock files.

## Validation

For Next.js web changes, run the actual project validation script after refactoring mocks:

```powershell
npm run build
```
