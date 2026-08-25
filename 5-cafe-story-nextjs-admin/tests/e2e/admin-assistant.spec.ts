import { expect, request, test, type APIRequestContext, type Page } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

type StreamRecord = {
  prompt: string;
  url: string;
  status: number;
  ok: boolean;
};

type EvidenceRecord = {
  index: number;
  prompt: string;
  screenshot: string;
  responsePreview: string;
  stream?: StreamRecord;
  status: "PASSED" | "FAILED";
  error?: string;
};

const prompts = [
  "Cho t\u00f4i xem c\u00e1c report \u0111ang review.",
  "B\u00e1o c\u00e1o b\u00e0i vi\u1ebft l\u1eeba \u0111\u1ea3o n\u00e0o c\u1ea7n x\u1eed l\u00fd?",
  "T\u00f3m t\u1eaft moderation queue hi\u1ec7n t\u1ea1i.",
  "T\u00ecm ng\u01b0\u1eddi d\u00f9ng b\u1ecb kh\u00f3a v\u00ec spam.",
  "Cho t\u00f4i xem b\u00e0i vi\u1ebft \u0111\u00e3 \u1ea9n.",
  "Cho t\u00f4i xem b\u00ecnh lu\u1eadn b\u1ecb x\u00f3a.",
  "Cho t\u00f4i xem cafe page \u0111ang b\u1ecb \u0111\u00ecnh ch\u1ec9.",
  "Report v\u00e0 moderation resolve kh\u00e1c nhau nh\u01b0 th\u1ebf n\u00e0o?",
  "What admin actions are allowed for reports?",
  "Summarize what tools you used for this answer.",
]

const adminBaseUrl = process.env.ADMIN_TEST_BASE_URL ?? "http://localhost:3636";
const apiBaseUrl =
  process.env.ADMIN_TEST_API_BASE_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080";
const n8nBaseUrl = process.env.N8N_BASE_URL ?? "http://localhost:5678";

const assistantSystemPrompt = [
  "You are CafeStory Admin Assistant for the admin operations workspace.",
  "Mirror the admin user language: answer Vietnamese prompts in Vietnamese and English prompts in English.",
  "Keep technical IDs, enum values, statuses, and actionType names exactly as returned by tools, without translating them.",
  "Use BE tool results as the source of truth. Do not invent counts, statuses, users, reports, or actions not present in toolResults or docsContext.",
  "You may combine at most the provided toolResults. Never ask n8n or the model to query SQL directly.",
  "Masking is intentional. Never ask for passwords, tokens, secrets, or raw payment data.",
  "Create draftAction only when the admin clearly asks for an action, the exact target id is available, and sourceRefs support the action.",
  "Draft actions are proposals only. Backend will require admin confirmation before execution.",
  "Payments and payouts are out of scope for V2 assistant actions.",
].join("\n");

function createModelPromptLog() {
  return {
    targetModel:
      process.env.ADMIN_ASSISTANT_PROMPT_LOG_MODEL ??
      process.env.OPENAI_ASSISTANT_MODEL ??
      "gpt-4o-mini",
    systemPrompt: assistantSystemPrompt,
    userPromptTemplate: JSON.stringify(
      {
        message: "<admin UI prompt>",
        pageContext: {
          route: "<current admin route>",
          query: "<current query string>",
        },
        history: "<recent conversation history from backend request>",
        selectedTools: "<up to 3 selected admin assistant tools>",
        docsContext: "<up to 3 policy snippets selected by n8n>",
        toolResults: "<masked backend tool responses>",
      },
      null,
      2,
    ),
    uiPrompts: prompts,
    note:
      "This is a prompt audit artifact. OPENAI_ASSISTANT_MODEL controls the runtime model; ADMIN_ASSISTANT_PROMPT_LOG_MODEL only labels the prompt log when reviewing a proposed model.",
  };
}

function loadLocalEnv() {
  const envFiles = [".env.e2e.local", ".env.local"];

  for (const envFile of envFiles) {
    const envPath = path.join(process.cwd(), envFile);
    if (!fs.existsSync(envPath)) continue;

    const lines = fs.readFileSync(envPath, "utf8").split(/\r?\n/);
    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#") || !trimmed.includes("=")) continue;

      const [rawKey, ...rawValueParts] = trimmed.split("=");
      const key = rawKey.trim();
      const value = rawValueParts.join("=").trim().replace(/^['"]|['"]$/g, "");
      if (key && process.env[key] === undefined) {
        process.env[key] = value;
      }
    }
  }
}

function requireEnv(name: string) {
  const value = process.env[name];
  if (!value || !value.trim()) {
    throw new Error(`${name} is required for admin assistant E2E.`);
  }
  return value;
}

function slug(value: string) {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "")
    .slice(0, 64);
}

function createEvidenceDir() {
  const timestamp = new Date().toISOString().replace(/[:.]/g, "-");
  const evidenceDir = path.resolve(
    process.cwd(),
    "..",
    "documents",
    "evidence",
    "admin-assistant-v2",
    timestamp,
  );
  fs.mkdirSync(evidenceDir, { recursive: true });
  return evidenceDir;
}

async function assertReachable(context: APIRequestContext, url: string, label: string) {
  try {
    const response = await context.get(url, {
      timeout: 10_000,
      failOnStatusCode: false,
    });
    if (response.status() >= 500) {
      throw new Error(`${label} returned ${response.status()}`);
    }
  } catch (error) {
    throw new Error(
      `${label} is not reachable at ${url}: ${
        error instanceof Error ? error.message : String(error)
      }`,
    );
  }
}

async function loginAsAdmin(page: Page, email: string, password: string) {
  await page.goto("/login");
  await expect(page.getByText("CafeStory Admin")).toBeVisible();
  await page.getByPlaceholder("Email or username").fill(email);
  await page.getByPlaceholder("Password").fill(password);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("button", { name: "Assistant" })).toBeVisible({
    timeout: 30_000,
  });
  await expect(page.getByText("This account does not have ADMIN access.")).toBeHidden();
}

async function openFreshAssistantChat(page: Page) {
  await page.getByRole("button", { name: "Assistant" }).click();
  await expect(page.getByRole("heading", { name: "Admin assistant" })).toBeVisible();
  const newChatButton = page.getByRole("button", { name: "New chat" });
  if (await newChatButton.isVisible()) {
    await newChatButton.click();
  }
  await expect(
    page.getByLabel("Ask assistant message"),
  ).toBeVisible();
}

async function sendPromptAndCapture(
  page: Page,
  prompt: string,
  index: number,
  evidenceDir: string,
  streamRecords: StreamRecord[],
): Promise<EvidenceRecord> {
  const assistantMessages = page.locator("main div.justify-start p.whitespace-pre-wrap");
  const previousAssistantCount = await assistantMessages.count();
  const textArea = page.getByLabel("Ask assistant message");
  const streamResponsePromise = page.waitForResponse(
    (response) =>
      response.url().includes("/api/admin/assistant/conversations/") &&
      response.url().includes("/messages/stream") &&
      response.request().method() === "POST",
    { timeout: 120_000 },
  );

  await textArea.fill(prompt);
  await page.getByRole("button", { name: "Send assistant message" }).click();
  const streamResponse = await streamResponsePromise;
  const streamRecord: StreamRecord = {
    prompt,
    url: streamResponse.url(),
    status: streamResponse.status(),
    ok: streamResponse.ok(),
  };
  streamRecords.push(streamRecord);

  expect(streamRecord.ok, `SSE stream failed for prompt: ${prompt}`).toBe(true);
  await waitForAssistantOutcome(page, assistantMessages, previousAssistantCount, prompt);
  await expect(page.getByText("Admin assistant service unavailable.")).toBeHidden();

  const latestAssistant = assistantMessages.nth(await assistantMessages.count() - 1);
  const responseText = (await latestAssistant.textContent())?.trim() ?? "";
  expect(responseText, `assistant response should not be empty for prompt: ${prompt}`).not.toBe("");

  const screenshotFile = `${String(index).padStart(2, "0")}-${slug(prompt)}.png`;
  await page.screenshot({
    path: path.join(evidenceDir, screenshotFile),
    fullPage: true,
  });

  return {
    index,
    prompt,
    screenshot: screenshotFile,
    responsePreview: responseText.slice(0, 500),
    stream: streamRecord,
    status: "PASSED",
  };
}

async function waitForAssistantOutcome(
  page: Page,
  assistantMessages: ReturnType<Page["locator"]>,
  previousAssistantCount: number,
  prompt: string,
) {
  const errorBanner = page.locator("aside div.text-accent", {
    hasText: /error|unavailable|failed|empty response|network/i,
  });
  const deadline = Date.now() + 120_000;

  while (Date.now() < deadline) {
    if ((await assistantMessages.count()) > previousAssistantCount) {
      return;
    }
    if ((await errorBanner.count()) > 0 && (await errorBanner.first().isVisible())) {
      const errorText = (await errorBanner.first().textContent())?.trim() || "unknown assistant error";
      throw new Error(`Assistant UI error for prompt "${prompt}": ${errorText}`);
    }
    await page.waitForTimeout(500);
  }

  throw new Error(`assistant response should appear for prompt: ${prompt}`);
}

function writeReport(
  evidenceDir: string,
  records: EvidenceRecord[],
  streamRecords: StreamRecord[],
  consoleErrors: string[],
  pageErrors: string[],
) {
  const report = {
    generatedAt: new Date().toISOString(),
    adminBaseUrl,
    apiBaseUrl,
    n8nBaseUrl,
    modelPromptLog: createModelPromptLog(),
    prompts: records,
    streams: streamRecords,
    consoleErrors,
    pageErrors,
  };
  fs.writeFileSync(path.join(evidenceDir, "report.json"), JSON.stringify(report, null, 2));

  const lines = [
    "# Admin Assistant E2E Evidence",
    "",
    `Generated at: ${report.generatedAt}`,
    `Admin base URL: ${adminBaseUrl}`,
    `API base URL: ${apiBaseUrl}`,
    `n8n base URL: ${n8nBaseUrl}`,
    `Prompt log target model: ${report.modelPromptLog.targetModel}`,
    "",
    "## Results",
    "",
    ...records.map(
      (record) =>
        `${record.index}. ${record.prompt} - ${record.status} - status ${record.stream?.status ?? "n/a"} - screenshot \`${record.screenshot}\`${record.error ? ` - ${record.error}` : ""}`,
    ),
    "",
    "## Console Errors",
    "",
    consoleErrors.length ? consoleErrors.map((error) => `- ${error}`).join("\n") : "None",
    "",
    "## Page Errors",
    "",
    pageErrors.length ? pageErrors.map((error) => `- ${error}`).join("\n") : "None",
    "",
    "## Model Prompt Log",
    "",
    "System prompt:",
    "",
    "```text",
    report.modelPromptLog.systemPrompt,
    "```",
    "",
    "User prompt template:",
    "",
    "```json",
    report.modelPromptLog.userPromptTemplate,
    "```",
    "",
    report.modelPromptLog.note,
    "",
  ];
  fs.writeFileSync(path.join(evidenceDir, "report.md"), lines.join("\n"));
}

function cleanupScratchFiles(evidenceDir: string) {
  for (const entry of fs.readdirSync(evidenceDir)) {
    if (/^(scratch-|tmp-|debug-).*\.(json|log|txt)$/i.test(entry)) {
      fs.rmSync(path.join(evidenceDir, entry), { force: true });
    }
  }
}

test.describe("admin assistant workflow evidence", () => {
  test("logs in as admin and captures valid UI responses for 10 prompts", async ({ page }) => {
    test.setTimeout(30 * 60_000);
    loadLocalEnv();

    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createEvidenceDir();
    const consoleErrors: string[] = [];
    const pageErrors: string[] = [];
    const streamRecords: StreamRecord[] = [];
    const records: EvidenceRecord[] = [];

    let captureRuntimeErrors = false;
    page.on("console", (message) => {
      if (!captureRuntimeErrors) return;
      if (message.type() === "error") {
        consoleErrors.push(message.text());
      }
    });
    page.on("pageerror", (error) => {
      pageErrors.push(error.message);
    });

    const preflight = await request.newContext();
    await assertReachable(preflight, adminBaseUrl, "Admin web");
    await assertReachable(preflight, `${apiBaseUrl.replace(/\/$/, "")}/api/auth/me`, "Backend API");
    await assertReachable(preflight, `${n8nBaseUrl.replace(/\/$/, "")}/healthz`, "n8n");
    await preflight.dispose();

    await loginAsAdmin(page, adminEmail, adminPassword);
    await page.screenshot({
      path: path.join(evidenceDir, "00-admin-dashboard-after-login.png"),
      fullPage: true,
    });

    await openFreshAssistantChat(page);
    await page.screenshot({
      path: path.join(evidenceDir, "00-admin-assistant-opened.png"),
      fullPage: true,
    });
    captureRuntimeErrors = true;

    try {
      for (const [promptIndex, prompt] of prompts.entries()) {
        records.push(
          await sendPromptAndCapture(page, prompt, promptIndex + 1, evidenceDir, streamRecords),
        );
      }

      expect(consoleErrors, "browser console should not contain errors").toEqual([]);
      expect(pageErrors, "page should not throw uncaught errors").toEqual([]);
    } catch (error) {
      const failedPrompt = prompts[records.length] ?? "unknown prompt";
      const screenshotFile = `${String(records.length + 1).padStart(2, "0")}-failed-${slug(failedPrompt)}.png`;
      let capturedScreenshot = screenshotFile;
      try {
        if (!page.isClosed()) {
          await page.screenshot({
            path: path.join(evidenceDir, screenshotFile),
            fullPage: true,
          });
        } else {
          capturedScreenshot = "not-captured-browser-closed";
        }
      } catch {
        capturedScreenshot = "not-captured-screenshot-failed";
      }
      records.push({
        index: records.length + 1,
        prompt: failedPrompt,
        screenshot: capturedScreenshot,
        responsePreview: "",
        stream: streamRecords.at(-1),
        status: "FAILED",
        error: error instanceof Error ? error.message : String(error),
      });
      throw error;
    } finally {
      cleanupScratchFiles(evidenceDir);
      writeReport(evidenceDir, records, streamRecords, consoleErrors, pageErrors);
    }
  });
});
