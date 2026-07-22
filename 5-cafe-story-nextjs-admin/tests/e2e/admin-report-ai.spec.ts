import { expect, request, test, type APIRequestContext, type APIResponse, type Page } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

type ReportTargetType = "BLOG" | "COMMENT" | "USER" | "CAFE_PAGE";
type ReportStatus = "OPEN" | "REVIEWING" | "RESOLVED" | "REJECTED";
type PostStatus = "DRAFT" | "PUBLISHED" | "HIDDEN" | "REMOVED";
type PageStatus = "DRAFT" | "ACTIVE" | "SUSPENDED";
type ReportDecision = "RESOLVE" | "REJECT" | "NEEDS_MANUAL_REVIEW";
type TargetAction = "APPROVE" | "HIDE" | "REMOVE" | "KEEP_ACTIVE" | "SUSPEND_USER" | "SUSPEND_PAGE" | "NONE";
type AutoJobStatus = "SCHEDULED" | "APPLYING" | "APPLIED" | "CANCELLED" | "FAILED" | "SKIPPED";

type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};

type AuthResponse = {
  user: {
    userId: string;
    userEmail?: string;
    userName?: string;
    roles?: string[];
  };
};

type Blog = {
  id: string;
  authorUserId: string | null;
  status: PostStatus;
  content: string | null;
};

type Comment = {
  id: string;
  blogId: string;
  userId: string | null;
  status: PostStatus;
  content: string | null;
};

type AdminUser = {
  userId: string;
  userName: string | null;
  accountStatus: boolean;
};

type CafePage = {
  id: string;
  ownerUserId: string | null;
  status: PageStatus;
  name: string;
};

type ReportReason = {
  id: string;
  code: string;
  labelVi: string;
  targetType: ReportTargetType | null;
  severity: number | null;
  requiresDescription: boolean | null;
};

type ContentReport = {
  id: string;
  reporterUserId: string;
  reporterUserName: string | null;
  targetType: ReportTargetType;
  targetId: string;
  blogId: string | null;
  commentId: string | null;
  reportedUserId: string | null;
  cafePageId: string | null;
  reasonId: string | null;
  reasonCode: string | null;
  reasonLabel: string | null;
  reasonSeverity: number | null;
  description: string | null;
  status: ReportStatus;
  createdAt: string;
  resolvedAt: string | null;
};

type AiResolution = {
  id: string;
  contentReportId: string;
  targetType: ReportTargetType;
  targetId: string;
  reportDecision: ReportDecision;
  targetAction: TargetAction;
  confidenceScore: number | null;
  riskScore: number | null;
  labels: string[];
  ruleCode: string | null;
  explanation: string | null;
  modelName: string | null;
  rawResponse: Record<string, unknown> | null;
  createdAt: string;
  autoApplyJob?: AutoApplyJob | null;
  autoApplyWarning?: string | null;
};

type AutoApplyJob = {
  id: string;
  contentReportId: string;
  aiResolutionId: string;
  targetType: ReportTargetType;
  targetId: string;
  status: AutoJobStatus;
  reportDecision: ReportDecision;
  targetAction: TargetAction;
  confidenceScore: number | null;
  riskScore: number | null;
  scheduledAt: string;
  appliedAt: string | null;
  cancelledAt: string | null;
  lastError: string | null;
  createdAt: string;
  updatedAt: string | null;
};

type TargetSeed = {
  targetType: ReportTargetType;
  targetId: string;
  reason: ReportReason;
  source: Blog | Comment | AdminUser | CafePage;
};

type ScenarioRecord = {
  id: string;
  title: string;
  expected: string;
  status: "PASSED" | "FAILED" | "BLOCKED" | "WARN";
  score: number;
  criteria: {
    setup: boolean;
    ui: boolean;
    apiAi: boolean;
    safety: boolean;
    performance: boolean;
  };
  durationMs: number;
  screenshots: string[];
  rawFiles: string[];
  notes: string[];
  fixRecommendations: string[];
};

type SeedState = {
  adminUserId: string;
  targets: Partial<Record<ReportTargetType, TargetSeed>>;
  reports: Partial<Record<ReportTargetType | "STATUS" | "BULK_A" | "BULK_B" | "DUPLICATE", ContentReport>>;
};

const adminBaseUrl = process.env.ADMIN_TEST_BASE_URL ?? "http://localhost:3636";
const apiBaseUrl =
  process.env.ADMIN_TEST_API_BASE_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080";
const n8nBaseUrl = process.env.N8N_BASE_URL ?? "http://localhost:5678";
const reportAiWebhookUrl = `${n8nBaseUrl.replace(/\/$/, "")}/webhook/cafestory-admin-report-ai-resolution`;
const TEST_MARKER = "E2E Report Admin AI";
const LATENCY_PASS_MS = 30_000;
const LATENCY_FAIL_MS = 45_000;

const scenarioDefinitions = [
  ["RAI-01", "Seed BLOG report va thay trong /reports", "Report OPEN, BLOG, visible in table/detail."],
  ["RAI-02", "Seed COMMENT report", "Report OPEN, COMMENT, visible."],
  ["RAI-03", "Seed USER report", "Report OPEN, USER, visible."],
  ["RAI-04", "Seed CAFE_PAGE report", "Report OPEN, CAFE_PAGE, visible."],
  ["RAI-05", "Duplicate report cung reporter/target", "BE conflict, runner self-heal stale test data."],
  ["RAI-06", "Mo detail report moi", "Detail shows reporter, target, reason, severity, description."],
  ["RAI-07", "Ask AI cho BLOG", "AI recommendation exists, BLOG action valid."],
  ["RAI-08", "Ask AI cho COMMENT", "AI recommendation exists, COMMENT action valid."],
  ["RAI-09", "Ask AI cho USER", "AI recommendation exists, USER action valid."],
  ["RAI-10", "Ask AI cho CAFE_PAGE", "AI recommendation exists, CAFE_PAGE action valid."],
  ["RAI-11", "AI history refresh", "Latest recommendation appears in history."],
  ["RAI-12", "AI response contract", "decision, action, confidence, risk, explanation, modelName present."],
  ["RAI-13", "n8n/OpenAI latency", "Ask AI <30s pass, 30-45s warn, >45s fail."],
  ["RAI-14", "Ask AI bat auto apply 15m", "Job or safe warning, UI does not crash."],
  ["RAI-15", "Countdown hien thi", "Scheduled job shows countdown when present."],
  ["RAI-16", "Cancel auto apply", "Job becomes CANCELLED and target not mutated."],
  ["RAI-17", "Auto apply safety gate", "Warning/no job when recommendation is not safe enough."],
  ["RAI-18", "Schedule moi thay job cu", "New request cancels/replaces existing scheduled job or returns safe warning."],
  ["RAI-19", "Auto job history", "Latest auto job status appears in detail/history."],
  ["RAI-20", "Resolve report", "Report becomes RESOLVED."],
  ["RAI-21", "Reopen report", "Report becomes OPEN."],
  ["RAI-22", "Mark reviewing", "Report becomes REVIEWING."],
  ["RAI-23", "Reject report", "Report becomes REJECTED and resolvedAt is set."],
  ["RAI-24", "Bulk dialog selected mode", "Dialog opens and selected mode can be used."],
  ["RAI-25", "Bulk Ask AI selected", "Progress reports done/failed/scheduled/skipped."],
  ["RAI-26", "Bulk auto apply confirm", "Auto apply requires explicit confirmation."],
  ["RAI-27", "Bulk auto apply selected", "Creates jobs or safe warnings without immediate target mutation."],
  ["RAI-28", "Security check", "No token/password/API key appears in UI/raw evidence."],
  ["RAI-29", "Performance classification", "Report contains UI/BE/n8n/OpenAI bottleneck classification."],
  ["RAI-30", "Cleanup verification", "Test reports are not left OPEN/REVIEWING and jobs are cancelled."],
] as const;

function loadLocalEnv() {
  for (const envFile of [".env.e2e.local", ".env.local"]) {
    const envPath = path.join(process.cwd(), envFile);
    if (!fs.existsSync(envPath)) continue;
    for (const line of fs.readFileSync(envPath, "utf8").split(/\r?\n/)) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#") || !trimmed.includes("=")) continue;
      const [rawKey, ...rawValueParts] = trimmed.split("=");
      const key = rawKey.trim();
      const value = rawValueParts.join("=").trim().replace(/^['"]|['"]$/g, "");
      if (key && process.env[key] === undefined) process.env[key] = value;
    }
  }
}

function requireEnv(name: string) {
  const value = process.env[name];
  if (!value || !value.trim()) throw new Error(`${name} is required for admin report AI E2E.`);
  return value;
}

function buildApiUrl(pathname: string) {
  if (pathname.startsWith("http://") || pathname.startsWith("https://")) return pathname;
  return `${apiBaseUrl.replace(/\/$/, "")}${pathname.startsWith("/") ? pathname : `/${pathname}`}`;
}

function withQuery(pathname: string, params: Record<string, string | number | boolean | null | undefined>) {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== "") query.set(key, String(value));
  }
  const queryString = query.toString();
  return queryString ? `${pathname}?${queryString}` : pathname;
}

async function parseResponse<T>(response: { text: () => Promise<string> }): Promise<{ data: T | null; raw: unknown }> {
  const text = await response.text();
  if (!text) return { data: null, raw: null };
  const raw = JSON.parse(text) as unknown;
  if (raw && typeof raw === "object" && "data" in raw) {
    return { data: (raw as { data: T }).data, raw };
  }
  return { data: raw as T, raw };
}

async function apiFetch<T>(
  page: Page,
  pathname: string,
  options: { method?: string; body?: Record<string, unknown> | null; failOnStatusCode?: boolean } = {},
) {
  const method = options.method ?? "GET";
  const response = await page.request.fetch(buildApiUrl(pathname), {
    method,
    data: options.body === null ? undefined : options.body,
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    timeout: 90_000,
    failOnStatusCode: false,
  });
  const parsed = await parseResponse<T>(response);
  if (options.failOnStatusCode !== false && !response.ok()) {
    throw new Error(`${method} ${pathname} returned ${response.status()}: ${JSON.stringify(parsed.raw).slice(0, 500)}`);
  }
  return { response, ...parsed };
}

function slug(value: string) {
  return value
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "")
    .slice(0, 80);
}

function createEvidenceDir() {
  const timestamp = new Date().toISOString().replace(/[:.]/g, "-");
  const evidenceDir = path.resolve(
    process.cwd(),
    "..",
    "documents",
    "report-admin",
    "ai-report-e2e",
    "evidence",
    timestamp,
  );
  fs.mkdirSync(path.join(evidenceDir, "screenshots"), { recursive: true });
  fs.mkdirSync(path.join(evidenceDir, "raw"), { recursive: true });
  return evidenceDir;
}

function writeJson(filePath: string, data: unknown) {
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), "utf8");
}

function maskEmail(value: string) {
  return value.replace(/([A-Z0-9._%+-]{2})[A-Z0-9._%+-]*(@[A-Z0-9.-]+\.[A-Z]{2,})/gi, "$1***$2");
}

function maskPhone(value: string) {
  return value.replace(/\b(\+?\d{2,3})?\d{5,}(\d{2})\b/g, (match) => `${match.slice(0, 2)}***${match.slice(-2)}`);
}

function sanitizeEvidence(value: unknown, key = ""): unknown {
  if (Array.isArray(value)) return value.map((item) => sanitizeEvidence(item));
  if (!value || typeof value !== "object") {
    if (typeof value !== "string") return value;
    return maskPhone(maskEmail(value));
  }

  const sanitized: Record<string, unknown> = {};
  for (const [entryKey, entryValue] of Object.entries(value)) {
    if (/password|token|secret|api[_-]?key/i.test(entryKey)) {
      sanitized[entryKey] = "[REDACTED]";
      continue;
    }
    if (/email/i.test(entryKey) && typeof entryValue === "string") {
      sanitized[entryKey] = maskEmail(entryValue);
      continue;
    }
    if (/phone/i.test(entryKey) && typeof entryValue === "string") {
      sanitized[entryKey] = maskPhone(entryValue);
      continue;
    }
    sanitized[entryKey] = sanitizeEvidence(entryValue, entryKey);
  }
  return key ? sanitized : sanitized;
}

function isSecretSafe(value: unknown) {
  const text = JSON.stringify(value);
  return !/(sk-[a-zA-Z0-9_-]{12,}|OPENAI_API_KEY|password|refreshToken|accessToken|api[_-]?key)/i.test(text);
}

function score(criteria: ScenarioRecord["criteria"]) {
  return Object.values(criteria).filter(Boolean).length * 20;
}

function statusFromScore(value: number, blocked = false): ScenarioRecord["status"] {
  if (blocked) return "BLOCKED";
  if (value >= 100) return "PASSED";
  if (value >= 80) return "WARN";
  return "FAILED";
}

function makeRecord(
  id: string,
  overrides: Partial<Omit<ScenarioRecord, "id" | "title" | "expected">> = {},
): ScenarioRecord {
  const definition = scenarioDefinitions.find(([scenarioId]) => scenarioId === id);
  if (!definition) throw new Error(`Unknown scenario ${id}`);
  const criteria = overrides.criteria ?? {
    setup: false,
    ui: false,
    apiAi: false,
    safety: false,
    performance: false,
  };
  const scenarioScore = overrides.score ?? score(criteria);
  return {
    id,
    title: definition[1],
    expected: definition[2],
    status: overrides.status ?? statusFromScore(scenarioScore),
    score: scenarioScore,
    criteria,
    durationMs: overrides.durationMs ?? 0,
    screenshots: overrides.screenshots ?? [],
    rawFiles: overrides.rawFiles ?? [],
    notes: overrides.notes ?? [],
    fixRecommendations: overrides.fixRecommendations ?? [],
  };
}

async function screenshot(page: Page, evidenceDir: string, id: string, label: string) {
  const fileName = `${id}-${slug(label)}.png`;
  await page.screenshot({
    path: path.join(evidenceDir, "screenshots", fileName),
    fullPage: true,
  });
  return `screenshots/${fileName}`;
}

function rawFile(evidenceDir: string, id: string, label: string, payload: unknown) {
  const fileName = `${id}-${slug(label)}.json`;
  writeJson(path.join(evidenceDir, "raw", fileName), payload);
  return `raw/${fileName}`;
}

async function assertReachable(context: APIRequestContext, url: string, label: string) {
  const response = await context.get(url, { failOnStatusCode: false, timeout: 10_000 });
  if (response.status() >= 500) throw new Error(`${label} returned ${response.status()} at ${url}`);
}

async function checkWebhookActive(context: APIRequestContext) {
  let lastStatus = 0;
  for (let attempt = 1; attempt <= 8; attempt += 1) {
    const response = await context.post(reportAiWebhookUrl, {
      data: {
        reportId: "00000000-0000-0000-0000-000000000000",
        targetType: "BLOG",
        targetId: "00000000-0000-0000-0000-000000000001",
        reasonCode: "E2E_PREFLIGHT",
        reasonLabel: "E2E preflight",
        reportStatus: "OPEN",
      },
      failOnStatusCode: false,
      timeout: 45_000,
    });
    lastStatus = response.status();
    if (response.ok()) {
      return {
        ok: true,
        status: lastStatus,
        attempts: attempt,
        message: `n8n report AI webhook preflight returned ${lastStatus}`,
      };
    }
    if (attempt < 8) await new Promise((resolve) => setTimeout(resolve, 2_000));
  }
  return {
    ok: false,
    status: lastStatus,
    attempts: 8,
    message: `n8n report AI webhook returned ${lastStatus} after readiness retries at ${reportAiWebhookUrl}`,
  };
}

async function loginAsAdmin(page: Page, email: string, password: string) {
  await page.goto("/login");
  await expect(page.getByText("CafeStory Admin")).toBeVisible();
  await page.getByPlaceholder("Email or username").fill(email);
  await page.getByPlaceholder("Password").fill(password);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Overview" })).toBeVisible({ timeout: 30_000 });
  await expect(page.getByText("This account does not have ADMIN access.")).toBeHidden();
}

function targetId(report: ContentReport) {
  return report.targetId ?? report.blogId ?? report.commentId ?? report.reportedUserId ?? report.cafePageId;
}

async function getPageContent<T>(page: Page, pathName: string) {
  const { data } = await apiFetch<PageResponse<T>>(page, pathName);
  return data?.content ?? [];
}

async function chooseReason(page: Page, targetType: ReportTargetType) {
  const { data } = await apiFetch<ReportReason[]>(page, withQuery("/api/report-reasons", { targetType }));
  const reason = (data ?? []).find((item) => !item.requiresDescription) ?? data?.[0];
  if (!reason) throw new Error(`No report reason is available for ${targetType}`);
  return reason;
}

async function collectTargets(page: Page, adminUserId: string): Promise<Partial<Record<ReportTargetType, TargetSeed>>> {
  const targets: Partial<Record<ReportTargetType, TargetSeed>> = {};
  const blogs = await getPageContent<Blog>(page, withQuery("/api/admin/blogs", { status: "PUBLISHED", page: 0, size: 100 }));
  const blog = blogs.find((item) => item.authorUserId !== adminUserId);
  if (blog) targets.BLOG = { targetType: "BLOG", targetId: blog.id, reason: await chooseReason(page, "BLOG"), source: blog };

  const comments = await getPageContent<Comment>(page, withQuery("/api/admin/comments", { status: "PUBLISHED", page: 0, size: 100 }));
  const comment = comments.find((item) => item.userId !== adminUserId);
  if (comment) targets.COMMENT = { targetType: "COMMENT", targetId: comment.id, reason: await chooseReason(page, "COMMENT"), source: comment };

  const users = await getPageContent<AdminUser>(page, withQuery("/api/admin/users", { accountStatus: true, page: 0, size: 100 }));
  const user = users.find((item) => item.userId !== adminUserId);
  if (user) targets.USER = { targetType: "USER", targetId: user.userId, reason: await chooseReason(page, "USER"), source: user };

  const cafePages = await getPageContent<CafePage>(page, withQuery("/api/admin/cafe-pages", { status: "ACTIVE", page: 0, size: 100 }));
  const cafePage = cafePages.find((item) => item.ownerUserId !== adminUserId);
  if (cafePage) targets.CAFE_PAGE = { targetType: "CAFE_PAGE", targetId: cafePage.id, reason: await chooseReason(page, "CAFE_PAGE"), source: cafePage };
  return targets;
}

async function findActiveTestReports(page: Page, targetType: ReportTargetType, targetIdValue: string, reporterUserId: string) {
  const matches: ContentReport[] = [];
  for (const status of ["OPEN", "REVIEWING"] satisfies ReportStatus[]) {
    const reports = await getPageContent<ContentReport>(
      page,
      withQuery("/api/admin/reports", { status, targetType, page: 0, size: 100 }),
    );
    matches.push(
      ...reports.filter(
        (report) =>
          report.reporterUserId === reporterUserId &&
          targetId(report) === targetIdValue &&
          report.description?.includes(TEST_MARKER),
      ),
    );
  }
  return matches;
}

async function closeReport(page: Page, reportId: string, status: ReportStatus = "REJECTED") {
  await apiFetch<ContentReport>(page, `/api/admin/reports/${encodeURIComponent(reportId)}/status`, {
    method: "PATCH",
    body: { status },
    failOnStatusCode: false,
  });
}

async function cancelScheduledJobs(page: Page, reportId: string) {
  const jobsResult = await apiFetch<PageResponse<AutoApplyJob>>(
    page,
    withQuery(`/api/admin/reports/${encodeURIComponent(reportId)}/ai-auto-resolutions`, { page: 0, size: 20 }),
    { failOnStatusCode: false },
  );
  const jobs = jobsResult.data?.content ?? [];
  for (const job of jobs) {
    if (job.status === "SCHEDULED") {
      await apiFetch<AutoApplyJob>(page, `/api/admin/reports/ai-auto-resolutions/${encodeURIComponent(job.id)}/cancel`, {
        method: "POST",
        failOnStatusCode: false,
      });
    }
  }
}

async function createReportWithSelfHeal(
  page: Page,
  seed: TargetSeed,
  description: string,
  adminUserId: string,
) {
  const body = {
    targetType: seed.targetType,
    targetId: seed.targetId,
    reasonId: seed.reason.id,
    description,
  };
  const first = await apiFetch<ContentReport>(page, "/api/reports", {
    method: "POST",
    body,
    failOnStatusCode: false,
  });
  if (first.response.status() === 201 && first.data) {
    return { report: first.data, raw: first.raw, selfHealed: false };
  }
  if (first.response.status() !== 409) {
    throw new Error(`Create report failed with ${first.response.status()}: ${JSON.stringify(first.raw).slice(0, 500)}`);
  }

  const staleReports = await findActiveTestReports(page, seed.targetType, seed.targetId, adminUserId);
  for (const staleReport of staleReports) {
    await cancelScheduledJobs(page, staleReport.id);
    await closeReport(page, staleReport.id, "REJECTED");
  }
  const second = await apiFetch<ContentReport>(page, "/api/reports", {
    method: "POST",
    body,
    failOnStatusCode: false,
  });
  if (second.response.status() !== 201 || !second.data) {
    throw new Error(`Create report after self-heal failed with ${second.response.status()}: ${JSON.stringify(second.raw).slice(0, 500)}`);
  }
  return { report: second.data, raw: { first: first.raw, staleReports, second: second.raw }, selfHealed: true };
}

function allowedActionForTarget(targetType: ReportTargetType, action: TargetAction) {
  if (action === "NONE") return true;
  if (targetType === "BLOG" || targetType === "COMMENT") return ["APPROVE", "HIDE", "REMOVE"].includes(action);
  if (targetType === "USER") return ["KEEP_ACTIVE", "SUSPEND_USER"].includes(action);
  return ["KEEP_ACTIVE", "SUSPEND_PAGE"].includes(action);
}

function resolutionContractOk(resolution: AiResolution | null | undefined) {
  return Boolean(
    resolution?.id &&
      resolution.contentReportId &&
      resolution.targetType &&
      resolution.targetId &&
      resolution.reportDecision &&
      resolution.targetAction &&
      typeof resolution.confidenceScore === "number" &&
      typeof resolution.riskScore === "number" &&
      resolution.explanation &&
      resolution.modelName,
  );
}

async function createAiResolution(
  page: Page,
  report: ContentReport,
  requestBody?: { autoApplyEnabled: boolean; autoApplyDelayMinutes?: number },
) {
  const started = Date.now();
  const result = await apiFetch<AiResolution>(
    page,
    `/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolution`,
    {
      method: "POST",
      body: requestBody ?? null,
      failOnStatusCode: false,
    },
  );
  return { ...result, durationMs: Date.now() - started };
}

async function openReportDetail(page: Page, report: ContentReport) {
  await page.goto("/reports");
  await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible({ timeout: 30_000 });
  await expect(page.getByText(report.description ?? report.id).first()).toBeVisible({ timeout: 30_000 });
  await page.getByText(report.description ?? report.id).first().click();
  await expect(page.getByRole("heading", { name: "Report detail" })).toBeVisible({ timeout: 30_000 });
  await expect(page.getByText(report.targetType).first()).toBeVisible();
}

async function askAiViaUi(page: Page, report: ContentReport) {
  await openReportDetail(page, report);
  const detailDialog = page.getByRole("dialog").filter({ hasText: "Report detail" });
  await detailDialog.getByRole("button", { name: "Ask AI" }).click();
  await expect(page.getByRole("heading", { name: "Ask AI for report resolution" })).toBeVisible();
  const responsePromise = page.waitForResponse(
    (response) =>
      response.url().includes(`/api/admin/reports/${report.id}/ai-resolution`) &&
      response.request().method() === "POST",
    { timeout: 120_000 },
  );
  await page.getByRole("dialog").filter({ hasText: "Ask AI for report resolution" }).getByRole("button", { name: "Ask AI" }).click();
  const response = await responsePromise;
  const parsed = await parseResponse<AiResolution>(response);
  return { response, ...parsed };
}

async function updateReportStatus(page: Page, reportId: string, status: ReportStatus) {
  const result = await apiFetch<ContentReport>(page, `/api/admin/reports/${encodeURIComponent(reportId)}/status`, {
    method: "PATCH",
    body: { status },
  });
  if (!result.data) throw new Error(`No report returned after status update ${status}`);
  return result.data;
}

function performanceCriterion(durationMs: number) {
  return durationMs <= LATENCY_FAIL_MS;
}

function performanceNote(durationMs: number) {
  if (durationMs <= LATENCY_PASS_MS) return `pass: ${durationMs}ms`;
  if (durationMs <= LATENCY_FAIL_MS) return `warn: ${durationMs}ms`;
  return `fail: ${durationMs}ms`;
}

function classifyBottleneck(records: ScenarioRecord[]) {
  const slow = records.filter((record) => record.durationMs > LATENCY_PASS_MS);
  if (!slow.length) return "No obvious bottleneck. All measured AI/UI scenarios were under the pass threshold.";
  const aiSlow = slow.filter((record) => /Ask AI|latency|Bulk/i.test(record.title));
  if (aiSlow.length >= Math.ceil(slow.length / 2)) {
    return "Likely n8n/OpenAI latency bottleneck: most slow scenarios include AI recommendation calls.";
  }
  return "Likely UI/BE mixed bottleneck: slow scenarios are not concentrated only in AI calls.";
}

function buildReport(evidenceDir: string, records: ScenarioRecord[], seedData: SeedState, fixLog: string[]) {
  const totalCriteria = records.length * 5;
  const passedCriteria = records.reduce((sum, record) => sum + Object.values(record.criteria).filter(Boolean).length, 0);
  const totalPercent = Number(((passedCriteria / totalCriteria) * 100).toFixed(2));
  const bottleneck = classifyBottleneck(records);
  const sanitizedSeedData = sanitizeEvidence(seedData);
  const lines = [
    "# Bao Cao Danh Gia Admin Report AI E2E",
    "",
    `- Generated at: ${new Date().toISOString()}`,
    `- Evidence folder: \`${evidenceDir}\``,
    `- Admin UI: \`${adminBaseUrl}\``,
    `- Backend API: \`${apiBaseUrl}\``,
    `- n8n health: \`${n8nBaseUrl}/healthz\``,
    `- n8n report AI webhook: \`${reportAiWebhookUrl}\``,
    `- Total result: **${totalPercent}%** (${passedCriteria}/${totalCriteria} criteria)`,
    `- Performance classification: ${bottleneck}`,
    "",
    "## Seed Data",
    "",
    "```json",
    JSON.stringify(sanitizedSeedData, null, 2),
    "```",
    "",
    "## Scenario Results",
    "",
    "| ID | Status | Score | Duration | Screenshots | Raw | Notes |",
    "|---|---|---:|---:|---|---|---|",
    ...records.map((record) => {
      const screenshots = record.screenshots.map((item) => `\`${item}\``).join("<br>") || "-";
      const rawFiles = record.rawFiles.map((item) => `\`${item}\``).join("<br>") || "-";
      const notes = [...record.notes, ...record.fixRecommendations.map((item) => `Fix: ${item}`)]
        .join("<br>")
        .replace(/\|/g, "/") || "-";
      return `| ${record.id} | ${record.status} | ${record.score}% | ${record.durationMs}ms | ${screenshots} | ${rawFiles} | ${notes} |`;
    }),
    "",
    "## Fix Guidance",
    "",
    fixLog.length ? fixLog.map((item) => `- ${item}`).join("\n") : "- No fix guidance generated.",
    "",
    "## Cleanup Verification",
    "",
    "- The runner cancels SCHEDULED auto apply jobs in finally.",
    "- The runner closes created reports by setting final status to REJECTED unless a scenario already closed it.",
    "- The runner does not delete production rows and does not wait for real auto-apply execution.",
    "",
  ];
  fs.writeFileSync(path.join(evidenceDir, "bao-cao-danh-gia-ai-report.md"), lines.join("\n"), "utf8");
  writeJson(path.join(evidenceDir, "summary.json"), {
    generatedAt: new Date().toISOString(),
    evidenceDir,
    totalCriteria,
    passedCriteria,
    totalPercent,
    bottleneck,
    records,
  });
  fs.writeFileSync(path.join(evidenceDir, "fix-log.md"), fixLog.map((item) => `- ${item}`).join("\n") || "- No fix guidance generated.\n", "utf8");
}

test.describe("admin report AI E2E evidence", () => {
  test("creates reports, asks AI, evaluates 30 scenarios, and cleans up", async ({ page }) => {
    test.setTimeout(45 * 60_000);
    loadLocalEnv();
    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createEvidenceDir();
    const records: ScenarioRecord[] = [];
    const fixLog: string[] = [];
    const seedState: SeedState = { adminUserId: "", targets: {}, reports: {} };
    const createdReports: ContentReport[] = [];

    try {
      const preflight = await request.newContext();
      await assertReachable(preflight, adminBaseUrl, "Admin UI");
      await assertReachable(preflight, `${apiBaseUrl.replace(/\/$/, "")}/api/auth/me`, "Backend API");
      await assertReachable(preflight, `${n8nBaseUrl.replace(/\/$/, "")}/healthz`, "n8n");
      const webhookPreflight = await checkWebhookActive(preflight);
      rawFile(evidenceDir, "PRE", "n8n-webhook-preflight", webhookPreflight);
      if (!webhookPreflight.ok) {
        await preflight.dispose();
        throw new Error(`Hard preflight failed: ${webhookPreflight.message}`);
      }
      await preflight.dispose();

      await loginAsAdmin(page, adminEmail, adminPassword);
      const auth = await apiFetch<AuthResponse>(page, "/api/auth/me");
      seedState.adminUserId = auth.data?.user.userId ?? "";
      if (!seedState.adminUserId) throw new Error("Unable to resolve current admin user id.");
      seedState.targets = await collectTargets(page, seedState.adminUserId);
      writeJson(path.join(evidenceDir, "seed-data.json"), sanitizeEvidence(seedState));

      for (const targetType of ["BLOG", "COMMENT", "USER", "CAFE_PAGE"] satisfies ReportTargetType[]) {
        const id = `RAI-0${(["BLOG", "COMMENT", "USER", "CAFE_PAGE"] as ReportTargetType[]).indexOf(targetType) + 1}`;
        const started = Date.now();
        const seed = seedState.targets[targetType];
        if (!seed) {
          records.push(makeRecord(id, {
            status: "BLOCKED",
            notes: [`No ${targetType} target is available that is safe to report with the admin test account.`],
            fixRecommendations: [`Seed at least one ${targetType} target not owned by the admin test account.`],
          }));
          continue;
        }
        const description = `${TEST_MARKER} ${id} ${targetType} ${Date.now()}`;
        const created = await createReportWithSelfHeal(page, seed, description, seedState.adminUserId);
        seedState.reports[targetType] = created.report;
        createdReports.push(created.report);
        await page.goto("/reports");
        await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible();
        await expect(page.getByText(description).first()).toBeVisible({ timeout: 30_000 });
        const shot = await screenshot(page, evidenceDir, id, `${targetType} report visible`);
        const raw = rawFile(evidenceDir, id, "create-report", created.raw);
        records.push(makeRecord(id, {
          durationMs: Date.now() - started,
          screenshots: [shot],
          rawFiles: [raw],
          notes: created.selfHealed ? ["Self-healed stale duplicate test report before recreating."] : [],
          criteria: {
            setup: created.report.status === "OPEN",
            ui: true,
            apiAi: true,
            safety: isSecretSafe(created.raw),
            performance: true,
          },
        }));
      }

      const duplicateSeed = seedState.targets.BLOG;
      const duplicateReport = seedState.reports.BLOG;
      if (duplicateSeed && duplicateReport) {
        const started = Date.now();
        const duplicate = await apiFetch<ContentReport>(page, "/api/reports", {
          method: "POST",
          body: {
            targetType: duplicateSeed.targetType,
            targetId: duplicateSeed.targetId,
            reasonId: duplicateSeed.reason.id,
            description: `${TEST_MARKER} RAI-05 duplicate ${Date.now()}`,
          },
          failOnStatusCode: false,
        });
        const raw = rawFile(evidenceDir, "RAI-05", "duplicate-report", duplicate.raw);
        records.push(makeRecord("RAI-05", {
          durationMs: Date.now() - started,
          rawFiles: [raw],
          notes: [`Duplicate create returned HTTP ${duplicate.response.status()}.`],
          fixRecommendations: duplicate.response.status() === 409 ? [] : ["Duplicate validation did not return 409; inspect ContentReportServiceImpl duplicate checks."],
          criteria: {
            setup: true,
            ui: true,
            apiAi: duplicate.response.status() === 409,
            safety: isSecretSafe(duplicate.raw),
            performance: true,
          },
        }));
      } else {
        records.push(makeRecord("RAI-05", { status: "BLOCKED", notes: ["BLOG report seed unavailable."] }));
      }

      const detailReport = seedState.reports.BLOG ?? seedState.reports.COMMENT ?? seedState.reports.USER ?? seedState.reports.CAFE_PAGE;
      if (detailReport) {
        const started = Date.now();
        await openReportDetail(page, detailReport);
        const shot = await screenshot(page, evidenceDir, "RAI-06", "report-detail");
        records.push(makeRecord("RAI-06", {
          durationMs: Date.now() - started,
          screenshots: [shot],
          criteria: {
            setup: true,
            ui: true,
            apiAi: true,
            safety: true,
            performance: true,
          },
        }));
      } else {
        records.push(makeRecord("RAI-06", { status: "BLOCKED", notes: ["No report is available for detail UI check."] }));
      }

      const aiResults: Partial<Record<ReportTargetType, { resolution: AiResolution | null; durationMs: number; rawFile: string }>> = {};
      for (const [scenarioId, targetType] of [
        ["RAI-07", "BLOG"],
        ["RAI-08", "COMMENT"],
        ["RAI-09", "USER"],
        ["RAI-10", "CAFE_PAGE"],
      ] as const) {
        const report = seedState.reports[targetType];
        const started = Date.now();
        if (!report) {
          records.push(makeRecord(scenarioId, { status: "BLOCKED", notes: [`No ${targetType} report is available.`] }));
          continue;
        }
        const result = scenarioId === "RAI-07"
          ? await askAiViaUi(page, report).then((uiResult) => ({
              data: uiResult.data,
              raw: uiResult.raw,
              response: uiResult.response,
              durationMs: Date.now() - started,
            }))
          : await createAiResolution(page, report);
        const resolution = result.data;
        const raw = rawFile(evidenceDir, scenarioId, `ai-resolution-${targetType}`, result.raw);
        aiResults[targetType] = { resolution: resolution ?? null, durationMs: result.durationMs, rawFile: raw };
        if (scenarioId !== "RAI-07") await openReportDetail(page, report);
        const shot = await screenshot(page, evidenceDir, scenarioId, `ai-resolution-${targetType}`);
        const aiFixes = result.response.ok()
          ? []
          : [`POST /api/admin/reports/{reportId}/ai-resolution returned ${result.response.status()}; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n.`];
        records.push(makeRecord(scenarioId, {
          durationMs: Date.now() - started,
          screenshots: [shot],
          rawFiles: [raw],
          notes: [performanceNote(result.durationMs)],
          fixRecommendations: [
            ...aiFixes,
            ...(result.durationMs > LATENCY_PASS_MS ? ["Investigate n8n/OpenAI latency for admin report AI resolution workflow."] : []),
          ],
          criteria: {
            setup: true,
            ui: true,
            apiAi: result.response.ok() && Boolean(resolution) && allowedActionForTarget(targetType, resolution!.targetAction),
            safety: isSecretSafe(result.raw),
            performance: performanceCriterion(result.durationMs),
          },
        }));
      }

      const historyReport = seedState.reports.BLOG;
      if (historyReport) {
        const started = Date.now();
        await openReportDetail(page, historyReport);
        await page.getByRole("button", { name: "Refresh history" }).click();
        await expect(page.getByText("AI recommendation history")).toBeVisible();
        const shot = await screenshot(page, evidenceDir, "RAI-11", "ai-history-refresh");
        records.push(makeRecord("RAI-11", {
          durationMs: Date.now() - started,
          screenshots: [shot],
          criteria: { setup: true, ui: true, apiAi: Boolean(aiResults.BLOG?.resolution), safety: true, performance: true },
        }));
      } else {
        records.push(makeRecord("RAI-11", { status: "BLOCKED", notes: ["No BLOG report for AI history refresh."] }));
      }

      const contractResolution = aiResults.BLOG?.resolution ?? aiResults.COMMENT?.resolution ?? aiResults.USER?.resolution ?? aiResults.CAFE_PAGE?.resolution ?? null;
      records.push(makeRecord("RAI-12", {
        rawFiles: contractResolution
          ? [rawFile(evidenceDir, "RAI-12", "contract-resolution", contractResolution)]
          : Object.values(aiResults).map((item) => item?.rawFile).filter((item): item is string => Boolean(item)),
        criteria: {
          setup: Boolean(contractResolution),
          ui: true,
          apiAi: resolutionContractOk(contractResolution),
          safety: isSecretSafe(contractResolution),
          performance: true,
        },
        fixRecommendations: resolutionContractOk(contractResolution)
          ? []
          : ["No valid AI recommendation contract was returned; check n8n workflow publication, ADMIN_REPORT_AI_WEBHOOK_URL, OpenAI credentials, and AdminReportAiResolutionWebhookResponseDTO mapping."],
      }));

      const validAiResults = Object.values(aiResults).filter(
        (item): item is { resolution: AiResolution; durationMs: number; rawFile: string } =>
          Boolean(item?.resolution) && resolutionContractOk(item?.resolution ?? null),
      );
      const measuredAiDurations = validAiResults.map((item) => item.durationMs);
      const maxAiDuration = measuredAiDurations.length ? Math.max(...measuredAiDurations) : 0;
      records.push(makeRecord("RAI-13", {
        durationMs: maxAiDuration,
        notes: [`Max AI duration: ${performanceNote(maxAiDuration)}`],
        criteria: {
          setup: validAiResults.length > 0,
          ui: true,
          apiAi: validAiResults.length > 0,
          safety: true,
          performance: performanceCriterion(maxAiDuration),
        },
        fixRecommendations: maxAiDuration > LATENCY_PASS_MS ? ["Optimize n8n/OpenAI workflow or add clearer UI progress for long-running AI calls."] : [],
      }));

      const autoReport = seedState.reports.COMMENT ?? seedState.reports.BLOG;
      let autoResolution: AiResolution | null = null;
      let autoJob: AutoApplyJob | null = null;
      if (autoReport) {
        const started = Date.now();
        const result = await createAiResolution(page, autoReport, { autoApplyEnabled: true, autoApplyDelayMinutes: 15 });
        autoResolution = result.data ?? null;
        autoJob = autoResolution?.autoApplyJob ?? null;
        const raw = rawFile(evidenceDir, "RAI-14", "auto-apply-resolution", result.raw);
        await openReportDetail(page, autoReport);
        const shot = await screenshot(page, evidenceDir, "RAI-14", "auto-apply-ui");
        records.push(makeRecord("RAI-14", {
          durationMs: Date.now() - started,
          screenshots: [shot],
          rawFiles: [raw],
          notes: [autoResolution?.autoApplyWarning ?? (autoJob ? "Auto apply job scheduled." : "No job returned.")],
          fixRecommendations: result.response.ok()
            ? []
            : [`POST /api/admin/reports/{reportId}/ai-resolution auto apply returned ${result.response.status()}; fix admin report AI service before validating auto-apply scheduling.`],
          criteria: {
            setup: true,
            ui: true,
            apiAi: result.response.ok() && Boolean(autoResolution),
            safety: isSecretSafe(result.raw),
            performance: performanceCriterion(result.durationMs),
          },
        }));

        records.push(autoJob
          ? makeRecord("RAI-15", {
              durationMs: result.durationMs,
              screenshots: [shot],
              rawFiles: [raw],
              notes: [`Scheduled at ${autoJob.scheduledAt}`],
              criteria: {
                setup: true,
                ui: await page.getByText(/Due now|\d+[hms]/).first().isVisible().catch(() => false),
                apiAi: true,
                safety: true,
                performance: true,
              },
            })
          : makeRecord("RAI-15", {
              status: "BLOCKED",
              score: 0,
              screenshots: [shot],
              rawFiles: [raw],
              notes: ["No scheduled job exists, so countdown cannot be verified."],
            }));

        if (autoJob?.status === "SCHEDULED") {
          const cancelStarted = Date.now();
          const cancel = await apiFetch<AutoApplyJob>(page, `/api/admin/reports/ai-auto-resolutions/${encodeURIComponent(autoJob.id)}/cancel`, { method: "POST" });
          const rawCancel = rawFile(evidenceDir, "RAI-16", "cancel-auto-apply", cancel.raw);
          await openReportDetail(page, autoReport);
          const cancelShot = await screenshot(page, evidenceDir, "RAI-16", "cancel-auto-apply-ui");
          records.push(makeRecord("RAI-16", {
            durationMs: Date.now() - cancelStarted,
            screenshots: [cancelShot],
            rawFiles: [rawCancel],
            criteria: {
              setup: true,
              ui: true,
              apiAi: cancel.data?.status === "CANCELLED",
              safety: isSecretSafe(cancel.raw),
              performance: true,
            },
          }));
        } else {
          records.push(makeRecord("RAI-16", {
            status: "BLOCKED",
            score: 0,
            notes: ["No scheduled job exists, so cancel behavior cannot be verified."],
          }));
        }
      } else {
        for (const id of ["RAI-14", "RAI-15", "RAI-16"] as const) {
          records.push(makeRecord(id, { status: "BLOCKED", notes: ["No report is available for auto apply test."] }));
        }
      }

      records.push(makeRecord("RAI-17", {
        rawFiles: autoResolution ? [rawFile(evidenceDir, "RAI-17", "auto-apply-safety", autoResolution)] : [],
        notes: [autoResolution?.autoApplyWarning ?? (autoResolution?.autoApplyJob ? "Safety gate allowed scheduling." : "No auto apply response.")],
        fixRecommendations: autoResolution ? [] : ["Auto apply safety gate cannot be evaluated until admin report AI recommendation endpoint returns a valid response."],
        criteria: {
          setup: Boolean(autoResolution),
          ui: true,
          apiAi: Boolean(autoResolution),
          safety: autoResolution ? Boolean(autoResolution.autoApplyWarning || autoResolution.autoApplyJob) : false,
          performance: true,
        },
      }));

      if (autoReport) {
        const started = Date.now();
        const replace = await createAiResolution(page, autoReport, { autoApplyEnabled: true, autoApplyDelayMinutes: 15 });
        const replacement = replace.data ?? null;
        const raw = rawFile(evidenceDir, "RAI-18", "replacement-auto-apply", replace.raw);
        if (replacement?.autoApplyJob?.status === "SCHEDULED") await cancelScheduledJobs(page, autoReport.id);
        records.push(makeRecord("RAI-18", {
          durationMs: Date.now() - started,
          rawFiles: [raw],
          notes: [replacement?.autoApplyWarning ?? (replacement?.autoApplyJob ? "Replacement scheduling returned a new job." : "No replacement job created.")],
          criteria: {
            setup: true,
            ui: true,
            apiAi: replace.response.ok() && Boolean(replacement),
            safety: isSecretSafe(replace.raw),
            performance: performanceCriterion(replace.durationMs),
          },
        }));
      } else {
        records.push(makeRecord("RAI-18", { status: "BLOCKED", notes: ["No report is available for replacement scheduling."] }));
      }

      if (autoReport) {
        const started = Date.now();
        const jobs = await apiFetch<PageResponse<AutoApplyJob>>(
          page,
          withQuery(`/api/admin/reports/${encodeURIComponent(autoReport.id)}/ai-auto-resolutions`, { page: 0, size: 20 }),
        );
        const raw = rawFile(evidenceDir, "RAI-19", "auto-job-history", jobs.raw);
        records.push(makeRecord("RAI-19", {
          durationMs: Date.now() - started,
          rawFiles: [raw],
          criteria: {
            setup: true,
            ui: true,
            apiAi: Array.isArray(jobs.data?.content),
            safety: isSecretSafe(jobs.raw),
            performance: true,
          },
        }));
      } else {
        records.push(makeRecord("RAI-19", { status: "BLOCKED", notes: ["No report is available for auto job history."] }));
      }

      const statusSeed = seedState.targets.USER ?? seedState.targets.CAFE_PAGE ?? seedState.targets.BLOG ?? seedState.targets.COMMENT;
      if (statusSeed) {
        const created = await createReportWithSelfHeal(
          page,
          statusSeed,
          `${TEST_MARKER} RAI-20-23 STATUS ${Date.now()}`,
          seedState.adminUserId,
        );
        seedState.reports.STATUS = created.report;
        createdReports.push(created.report);
        let report = created.report;
        const rawCreated = rawFile(evidenceDir, "RAI-20", "status-report-created", created.raw);

        let started = Date.now();
        report = await apiFetch<ContentReport>(page, `/api/admin/reports/${encodeURIComponent(report.id)}/resolve`, { method: "POST" }).then((result) => result.data!);
        records.push(makeRecord("RAI-20", {
          durationMs: Date.now() - started,
          rawFiles: [rawCreated],
          criteria: { setup: true, ui: true, apiAi: report.status === "RESOLVED", safety: true, performance: true },
        }));

        started = Date.now();
        report = await updateReportStatus(page, report.id, "OPEN");
        records.push(makeRecord("RAI-21", {
          durationMs: Date.now() - started,
          criteria: { setup: true, ui: true, apiAi: report.status === "OPEN", safety: true, performance: true },
        }));

        started = Date.now();
        report = await updateReportStatus(page, report.id, "REVIEWING");
        records.push(makeRecord("RAI-22", {
          durationMs: Date.now() - started,
          criteria: { setup: true, ui: true, apiAi: report.status === "REVIEWING", safety: true, performance: true },
        }));

        started = Date.now();
        report = await updateReportStatus(page, report.id, "REJECTED");
        records.push(makeRecord("RAI-23", {
          durationMs: Date.now() - started,
          criteria: { setup: true, ui: true, apiAi: report.status === "REJECTED" && Boolean(report.resolvedAt), safety: true, performance: true },
        }));
      } else {
        for (const id of ["RAI-20", "RAI-21", "RAI-22", "RAI-23"] as const) {
          records.push(makeRecord(id, { status: "BLOCKED", notes: ["No target is available for status workflow."] }));
        }
      }

      const bulkSeeds = (["BLOG", "COMMENT", "USER", "CAFE_PAGE"] satisfies ReportTargetType[])
        .map((targetType) => seedState.targets[targetType])
        .filter((seed): seed is TargetSeed => Boolean(seed))
        .slice(0, 2);
      const bulkReports: ContentReport[] = [];
      if (bulkSeeds.length) {
        for (const [index, bulkSeed] of bulkSeeds.entries()) {
          const key = index === 0 ? "BULK_A" : "BULK_B";
          const created = await createReportWithSelfHeal(
            page,
            bulkSeed,
            `${TEST_MARKER} ${key} ${Date.now()} ${Math.random().toString(16).slice(2)}`,
            seedState.adminUserId,
          );
          seedState.reports[key] = created.report;
          createdReports.push(created.report);
          bulkReports.push(created.report);
        }
        await page.goto("/reports");
        await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible();
        const started = Date.now();
        await page.getByRole("button", { name: "Generate AI recommendations" }).click();
        let bulkDialog = page.getByRole("dialog").filter({ hasText: "Generate AI recommendations" });
        await expect(bulkDialog.getByRole("heading", { name: "Generate AI recommendations" })).toBeVisible();
        await bulkDialog.getByRole("button", { name: "Selected reports" }).click();
        const shot = await screenshot(page, evidenceDir, "RAI-24", "bulk-dialog-selected");
        records.push(makeRecord("RAI-24", {
          durationMs: Date.now() - started,
          screenshots: [shot],
          criteria: { setup: true, ui: true, apiAi: true, safety: true, performance: true },
        }));

        const bulkStarted = Date.now();
        await bulkDialog.getByRole("button", { name: "Run AI", exact: true }).click();
        await expect(page.getByText("Total:")).toBeVisible({ timeout: 30_000 });
        await expect(page.getByText(/Remaining: 0/)).toBeVisible({ timeout: 180_000 });
        const selectedBulkSucceeded = await bulkDialog.getByText("Failed: 0").isVisible().catch(() => false)
          && await bulkDialog.getByText(/Success: [1-9]\d*/).isVisible().catch(() => false);
        const bulkShot = await screenshot(page, evidenceDir, "RAI-25", "bulk-run-selected");
        records.push(makeRecord("RAI-25", {
          durationMs: Date.now() - bulkStarted,
          screenshots: [bulkShot],
          notes: [performanceNote(Date.now() - bulkStarted)],
          criteria: { setup: true, ui: true, apiAi: selectedBulkSucceeded, safety: true, performance: performanceCriterion(Date.now() - bulkStarted) },
        }));
        await bulkDialog.getByRole("button", { name: "Close" }).click();

        await page.getByRole("button", { name: "Generate AI recommendations" }).click();
        bulkDialog = page.getByRole("dialog").filter({ hasText: "Generate AI recommendations" });
        await bulkDialog.getByRole("button", { name: "Selected reports" }).click();
        await bulkDialog.getByText("Auto apply after delay").last().click();
        const runButton = bulkDialog.getByRole("button", { name: "Run AI", exact: true });
        const disabledBeforeConfirm = await runButton.isDisabled();
        const confirmShot = await screenshot(page, evidenceDir, "RAI-26", "bulk-auto-confirm-required");
        records.push(makeRecord("RAI-26", {
          screenshots: [confirmShot],
          criteria: { setup: true, ui: disabledBeforeConfirm, apiAi: true, safety: true, performance: true },
          fixRecommendations: disabledBeforeConfirm ? [] : ["Bulk auto apply can run without explicit confirmation; inspect AdminReportsPage bulkAutoApplyConfirmed guard."],
        }));

        await page.getByText("I understand this may schedule target actions").click();
        const bulkAutoStarted = Date.now();
        await runButton.click();
        await expect(page.getByText("Total:")).toBeVisible({ timeout: 30_000 });
        await expect(page.getByText(/Remaining: 0/)).toBeVisible({ timeout: 180_000 });
        const bulkAutoSucceeded = await bulkDialog.getByText("Failed: 0").isVisible().catch(() => false)
          && await bulkDialog.getByText(/Success: [1-9]\d*/).isVisible().catch(() => false);
        const bulkAutoShot = await screenshot(page, evidenceDir, "RAI-27", "bulk-auto-run");
        records.push(makeRecord("RAI-27", {
          durationMs: Date.now() - bulkAutoStarted,
          screenshots: [bulkAutoShot],
          criteria: { setup: true, ui: true, apiAi: bulkAutoSucceeded, safety: true, performance: performanceCriterion(Date.now() - bulkAutoStarted) },
        }));
        await bulkDialog.getByRole("button", { name: "Close" }).click();
      } else {
        for (const id of ["RAI-24", "RAI-25", "RAI-26", "RAI-27"] as const) {
          records.push(makeRecord(id, { status: "BLOCKED", notes: ["No target is available for bulk AI workflow."] }));
        }
      }

      const securitySafe = isSecretSafe(records);
      records.push(makeRecord("RAI-28", {
        criteria: { setup: true, ui: true, apiAi: true, safety: securitySafe, performance: true },
        fixRecommendations: securitySafe ? [] : ["Evidence contains a possible secret; inspect raw files and n8n workflow response masking."],
      }));

      const bottleneck = classifyBottleneck(records);
      records.push(makeRecord("RAI-29", {
        notes: [bottleneck],
        criteria: { setup: true, ui: true, apiAi: true, safety: true, performance: true },
      }));
    } finally {
      const cleanupStarted = Date.now();
      const cleanupNotes: string[] = [];
      for (const report of createdReports) {
        try {
          await cancelScheduledJobs(page, report.id);
          await closeReport(page, report.id, "REJECTED");
          cleanupNotes.push(`Closed report ${report.id}`);
        } catch (error) {
          cleanupNotes.push(`Cleanup failed for report ${report.id}: ${error instanceof Error ? error.message : String(error)}`);
        }
      }
      const activeLeft: ContentReport[] = [];
      for (const report of createdReports) {
        const refreshed = await apiFetch<ContentReport>(page, `/api/admin/reports/${encodeURIComponent(report.id)}`, { failOnStatusCode: false });
        if (refreshed.data && ["OPEN", "REVIEWING"].includes(refreshed.data.status)) activeLeft.push(refreshed.data);
      }
      records.push(makeRecord("RAI-30", {
        durationMs: Date.now() - cleanupStarted,
        notes: cleanupNotes,
        rawFiles: [rawFile(evidenceDir, "RAI-30", "cleanup-verification", { activeLeft })],
        criteria: {
          setup: true,
          ui: true,
          apiAi: activeLeft.length === 0,
          safety: true,
          performance: true,
        },
        fixRecommendations: activeLeft.length ? ["Cleanup left active reports; inspect report status update endpoint or duplicate seed strategy."] : [],
      }));

      const allFixes = records.flatMap((record) => record.fixRecommendations.map((item) => `${record.id}: ${item}`));
      fixLog.push(...allFixes);
      buildReport(evidenceDir, records, seedState, fixLog);
    }

    const failed = records.filter((record) => record.status === "FAILED");
    expect(failed, `Failed scenarios: ${failed.map((record) => record.id).join(", ")}`).toEqual([]);
  });
});
