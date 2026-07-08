# Admin Assistant Agent Test Workflow

This runbook defines the local workflow for testing the CafeStory admin assistant as a real admin, capturing UI evidence, fixing local code defects, and retesting.

## Required Inputs

- `ADMIN_TEST_BASE_URL`: admin web URL, default `http://localhost:3636`.
- `ADMIN_TEST_API_BASE_URL` or `NEXT_PUBLIC_API_BASE_URL`: backend URL, default `http://localhost:8080`.
- `N8N_BASE_URL`: n8n URL, default `http://localhost:5678`.
- `ADMIN_TEST_EMAIL`: admin test account email or username.
- `ADMIN_TEST_PASSWORD`: admin test account password.
- Backend env for admin assistant:
  - `ADMIN_ASSISTANT_WEBHOOK_URL`
  - `ADMIN_ASSISTANT_TOOL_BASE_URL`
  - `ADMIN_ASSISTANT_TOOL_TOKEN`
  - `OPENAI_ASSISTANT_MODEL`
- n8n env for the assistant workflow:
  - `OPENAI_API_KEY`
  - `OPENAI_ASSISTANT_MODEL`
  - `ADMIN_ASSISTANT_PROMPT_LOG_MODEL` for prompt-log labeling only, when reviewing a proposed model before using it at runtime.
  - `ADMIN_ASSISTANT_TOOL_BASE_URL`
  - `ADMIN_ASSISTANT_TOOL_TOKEN`

Keep credentials in ignored local env files such as `5-cafe-story-nextjs-admin/.env.e2e.local` or `5-cafe-story-nextjs-admin/.env.local`. Do not commit secrets.

## Preflight

1. Start backend, admin web, and n8n.
2. Confirm n8n uses `docker/docker-compose.n8n.yml` and do not run `docker compose down -v`.
3. Confirm the n8n workflow `cafestory-admin-assistant-chat` is imported and active.
4. Confirm the admin test account has the `ADMIN` role.
5. From `5-cafe-story-nextjs-admin`, run:

```powershell
npm run typecheck
npm run build
npm run test:e2e:admin-assistant
```

If Playwright browsers are missing, run:

```powershell
npx playwright install chromium
```

## Test Loop

The E2E harness logs in, opens the assistant, starts a fresh chat, sends these 10 prompts, and captures a UI screenshot after each valid assistant response:

1. `Give me a dashboard summary.`
2. `Show the latest reports.`
3. `What is in the moderation queue?`
4. `Search recent users.`
5. `Show recent blogs.`
6. `Show recent comments.`
7. `Show recent cafe pages.`
8. `Explain what admin actions are allowed for reports.`
9. `Do we have any suspicious or pending moderation items?`
10. `Summarize what tools you used for this answer.`

Evidence is written to `documents/evidence/admin-assistant/<timestamp>/` with screenshots plus `report.json` and `report.md`. The report also includes a prompt audit log with the target model label, system prompt, user prompt template, and the 10 UI prompts.

## Auto-Fix Rules

- Fix local code defects only: UI runtime errors, API client bugs, SSE parsing bugs, backend assistant service bugs, or test harness bugs.
- After each fix, rerun the focused check first, then rerun `npm run test:e2e:admin-assistant`.
- Stop after 3 fix loops and report the remaining blocker with evidence.
- Stop and ask the user when the failure is caused by missing/wrong credentials, non-admin role, OpenAI quota/key/model, inactive n8n workflow, unavailable local services, or missing dev data.
- Do not execute assistant draft actions by default because they can mutate business data.

## Cleanup

- Keep final screenshots, Playwright trace/video on failure, `report.json`, `report.md`, and prompt-log artifacts.
- Delete only scratch files created during the run, such as `scratch-*.json`, `tmp-*.log`, or `debug-*.txt` inside the current evidence folder.
- Before reporting completion, run `git status --short` and separate intentional source changes from generated evidence.
