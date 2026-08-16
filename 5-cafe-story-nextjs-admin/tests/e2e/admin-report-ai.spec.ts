import {
  expect,
  request,
  test,
  type APIRequestContext,
  type APIResponse,
  type Page,
  type Response as PlaywrightResponse,
} from "@playwright/test";
import { execFileSync } from "node:child_process";
import { randomUUID } from "node:crypto";
import fs from "node:fs";
import path from "node:path";

type ReportTargetType = "BLOG" | "COMMENT" | "USER" | "CAFE_PAGE";
type ReportStatus = "OPEN" | "REVIEWING" | "RESOLVED" | "REJECTED";
type PostStatus = "DRAFT" | "PUBLISHED" | "HIDDEN" | "REMOVED";
type PageStatus = "DRAFT" | "ACTIVE" | "SUSPENDED";
type ReportDecision = "RESOLVE" | "REJECT" | "NEEDS_MANUAL_REVIEW";
type TargetAction = "KEEP_VISIBLE" | "NO_ACTION" | "APPROVE" | "HIDE" | "REMOVE" | "KEEP_ACTIVE" | "SUSPEND_USER" | "SUSPEND_PAGE" | "NONE";
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
  contractVersion: "2.0" | "legacy-v1";
  correlationId: string | null;
  automationMode: "A0_RECOMMEND_ONLY";
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
  createdAt: string;
  autoApplyJob?: AutoApplyJob | null;
  autoApplyWarning?: string | null;
  findings: Array<{
    ruleId: string;
    ruleVersion: string;
    outcome: "SUPPORTED" | "NOT_SUPPORTED" | "INCONCLUSIVE";
    evidenceIds: string[];
    counterEvidenceIds: string[];
    missingEvidenceIds: string[];
    violationLikelihood: "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN";
    rationale: string;
  }> | null;
  evidenceSummary: {
    usedEvidenceIds?: string[];
    counterEvidenceIds?: string[];
    missingEvidenceIds?: string[];
  } | null;
  blockedReasons: string[] | null;
  evidenceQuality: "HIGH" | "MEDIUM" | "LOW" | "UNUSABLE" | null;
  evidenceSufficiency: "SUFFICIENT" | "INSUFFICIENT" | "CONFLICTED" | "UNASSESSABLE" | null;
  violationLikelihood: "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN" | null;
  harmSeverity: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN" | null;
  actionRisk: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | null;
  policyVersion: string | null;
  ruleCatalogVersion: string | null;
  promptVersion: string | null;
  workflowVersion: string | null;
  targetSnapshotHash: string | null;
};

type AiPolicy = {
  reportId: string;
  targetType: ReportTargetType;
  reasonCode: string | null;
  contextSchemaVersion: string;
  policyVersion: string;
  policyStatus: string;
  ruleCatalogVersion: string;
  ruleCatalogStatus: string;
  evaluationMode: string;
  recommendationOnly: true;
  candidateRules: Array<{
    ruleId: string;
    ruleVersion: string;
    ruleStatus: string;
    requiredEvidenceKinds: string[];
    allowedCandidateActions: string[];
  }>;
};

type AdminReportAiOperationalError = {
  statusCode: number;
  status: "Fail";
  message: string;
  data: null;
  code: "AI_PROVIDER_BOUNDARY_FAILED";
  correlationId: string;
  retryable: true;
  stage: "N8N_PROVIDER";
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

type TargetSnapshot = {
  targetType: ReportTargetType;
  targetId: string;
  state: Record<string, string | boolean | null>;
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
const REPORT_AI_TEST_POSTGRES_CONTAINER =
  process.env.ADMIN_REPORT_AI_TEST_POSTGRES_CONTAINER ?? "cafestory-g0-12c-postgres";
const REPORT_AI_N8N_CONTAINER =
  process.env.ADMIN_REPORT_AI_N8N_CONTAINER ?? "cafestory-n8n";

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
  ["RAI-12", "AI response Contract V2", "Evidence, findings, categorical risk, versions, and no raw provider payload."],
  ["RAI-13", "n8n/OpenAI latency", "Ask AI <30s pass, 30-45s warn, >45s fail."],
  ["RAI-14", "UI khong tao auto apply", "No auto-apply creation control; A0 notice is visible."],
  ["RAI-15", "Legacy request bi chan A0", "autoApply=true returns warning and creates no job."],
  ["RAI-16", "Cancel legacy auto apply", "Existing SCHEDULED job can still be cancelled when fixture exists."],
  ["RAI-17", "A0 safety invariant", "AI creates no job and does not mutate report or target."],
  ["RAI-18", "Lap lai legacy request", "Repeated autoApply=true still creates no job."],
  ["RAI-19", "Legacy auto job history", "History remains readable while creation stays disabled."],
  ["RAI-20", "Resolve report", "Report becomes RESOLVED."],
  ["RAI-21", "Reopen report", "Report becomes OPEN."],
  ["RAI-22", "Mark reviewing", "Report becomes REVIEWING."],
  ["RAI-23", "Reject report", "Report becomes REJECTED and resolvedAt is set."],
  ["RAI-24", "Bulk dialog selected mode", "Dialog opens and selected mode can be used."],
  ["RAI-25", "Bulk Ask AI selected", "Progress separates recommended/manual/failed."],
  ["RAI-26", "Bulk A0 notice", "Automation-disabled notice is visible and no auto-apply control exists."],
  ["RAI-27", "Bulk recommendation only", "Bulk creates recommendations/manual outcomes and no jobs."],
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

function psqlForReportAiFixture(sql: string) {
  const databaseUser = execFileSync(
    "docker",
    ["exec", REPORT_AI_TEST_POSTGRES_CONTAINER, "printenv", "POSTGRES_USER"],
    { encoding: "utf8" },
  ).trim();
  const databaseName = execFileSync(
    "docker",
    ["exec", REPORT_AI_TEST_POSTGRES_CONTAINER, "printenv", "POSTGRES_DB"],
    { encoding: "utf8" },
  ).trim();
  if (!databaseUser || !databaseName) {
    throw new Error("Disposable PostgreSQL fixture container is missing POSTGRES_USER or POSTGRES_DB.");
  }
  return execFileSync(
    "docker",
    [
      "exec",
      REPORT_AI_TEST_POSTGRES_CONTAINER,
      "psql",
      "-v",
      "ON_ERROR_STOP=1",
      "-U",
      databaseUser,
      "-d",
      databaseName,
      "-At",
      "-c",
      sql,
    ],
    { encoding: "utf8" },
  ).trim();
}

function assertUuid(value: string, label: string) {
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value)) {
    throw new Error(`${label} must be a UUID.`);
  }
}

function createMissingCriticalCommentContextFixture(adminUserId: string) {
  assertUuid(adminUserId, "adminUserId");
  const targetOwnerId = psqlForReportAiFixture(
    `select user_id from users where account_status=true and user_id <> '${adminUserId}'::uuid order by user_id limit 1;`,
  );
  assertUuid(targetOwnerId, "targetOwnerId");
  const blogId = randomUUID();
  const commentId = randomUUID();
  psqlForReportAiFixture(`
    insert into blogs (
      allow_comment, comment_count, is_pinned, like_count, share_count,
      created_at, author_user_id, id, content, status
    ) values (
      true, 1, false, 0, 0,
      now(), '${targetOwnerId}'::uuid, '${blogId}'::uuid, '   ', 'PUBLISHED'
    );
    insert into comments (
      created_at, blog_id, id, user_id, actor_context_type, content, status
    ) values (
      now(), '${blogId}'::uuid, '${commentId}'::uuid, '${targetOwnerId}'::uuid,
      'USER', 'Đúng vậy.', 'PUBLISHED'
    );
  `);
  return { blogId, commentId, targetOwnerId };
}

function cleanupMissingCriticalCommentContextFixture(
  fixture: { blogId: string; commentId: string },
  reportId?: string,
) {
  assertUuid(fixture.blogId, "blogId");
  assertUuid(fixture.commentId, "commentId");
  if (reportId) assertUuid(reportId, "reportId");
  psqlForReportAiFixture(`
    ${reportId ? `delete from admin_report_ai_auto_apply_jobs where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from admin_report_ai_resolutions where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from report_moderation_jobs where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from ai_moderation_results where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from content_reports where id='${reportId}'::uuid;` : ""}
    delete from ai_moderation_results where comment_id='${fixture.commentId}'::uuid;
    delete from comments where id='${fixture.commentId}'::uuid;
    delete from blogs where id='${fixture.blogId}'::uuid;
  `);
}

function createProviderUnavailableBlogFixture(adminUserId: string) {
  assertUuid(adminUserId, "adminUserId");
  const targetOwnerId = psqlForReportAiFixture(
    `select user_id from users where account_status=true and user_id <> '${adminUserId}'::uuid order by user_id limit 1;`,
  );
  assertUuid(targetOwnerId, "targetOwnerId");
  const blogId = randomUUID();
  psqlForReportAiFixture(`
    insert into blogs (
      allow_comment, comment_count, is_pinned, like_count, share_count,
      created_at, author_user_id, id, content, status
    ) values (
      true, 0, false, 0, 0,
      now(), '${targetOwnerId}'::uuid, '${blogId}'::uuid,
      'Bài viết fixture dùng để kiểm chứng lỗi vận hành provider.', 'PUBLISHED'
    );
  `);
  return { blogId, targetOwnerId };
}

function cleanupProviderUnavailableBlogFixture(
  fixture: { blogId: string },
  reportId?: string,
) {
  assertUuid(fixture.blogId, "blogId");
  if (reportId) assertUuid(reportId, "reportId");
  psqlForReportAiFixture(`
    ${reportId ? `delete from admin_report_ai_auto_apply_jobs where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from admin_report_ai_resolutions where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from report_moderation_jobs where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from ai_moderation_results where content_report_id='${reportId}'::uuid;` : ""}
    ${reportId ? `delete from content_reports where id='${reportId}'::uuid;` : ""}
    delete from ai_moderation_results where blog_id='${fixture.blogId}'::uuid;
    delete from blog_daily_metrics where blog_id='${fixture.blogId}'::uuid;
    delete from blog_events where blog_id='${fixture.blogId}'::uuid;
    delete from blog_images where blog_id='${fixture.blogId}'::uuid;
    delete from blog_likes where blog_id='${fixture.blogId}'::uuid;
    delete from blog_ranking_overrides where blog_id='${fixture.blogId}'::uuid;
    delete from blog_ratings where blog_id='${fixture.blogId}'::uuid;
    delete from blog_recommendation_scores where blog_id='${fixture.blogId}'::uuid;
    delete from blog_saves where blog_id='${fixture.blogId}'::uuid;
    delete from blog_shares where blog_id='${fixture.blogId}'::uuid;
    delete from blog_tagged_users where blog_id='${fixture.blogId}'::uuid;
    delete from blog_trending_scores where blog_id='${fixture.blogId}'::uuid;
    delete from feed_impressions where blog_id='${fixture.blogId}'::uuid;
    delete from blogs where id='${fixture.blogId}'::uuid;
  `);
}

function assertSafeDockerContainerName(containerName: string) {
  if (!/^[a-zA-Z0-9][a-zA-Z0-9_.-]+$/.test(containerName)) {
    throw new Error("ADMIN_REPORT_AI_N8N_CONTAINER contains an unsafe container name.");
  }
}

function stopN8nForProviderBoundaryTest() {
  assertSafeDockerContainerName(REPORT_AI_N8N_CONTAINER);
  execFileSync("docker", ["stop", REPORT_AI_N8N_CONTAINER], {
    encoding: "utf8",
    timeout: 30_000,
  });
}

function startN8nAfterProviderBoundaryTest() {
  assertSafeDockerContainerName(REPORT_AI_N8N_CONTAINER);
  execFileSync("docker", ["start", REPORT_AI_N8N_CONTAINER], {
    encoding: "utf8",
    timeout: 30_000,
  });
}

async function waitForN8nRecovery(apiContext: APIRequestContext) {
  for (let attempt = 1; attempt <= 30; attempt += 1) {
    try {
      const response = await apiContext.get(`${n8nBaseUrl.replace(/\/$/, "")}/healthz`, {
        failOnStatusCode: false,
        timeout: 5_000,
      });
      if (response.ok()) {
        const webhook = await checkWebhookActive(apiContext);
        if (webhook.ok) return;
      }
    } catch {
      // Expected while the disposable n8n container is restarting.
    }
    await new Promise((resolve) => setTimeout(resolve, 1_000));
  }
  throw new Error("n8n did not recover after the injected provider-boundary outage.");
}

function createFix03EvidenceDir() {
  const configured = process.env.DOD_FIX03_EVIDENCE_DIR;
  const evidenceDir = configured
    ? path.resolve(configured)
    : path.resolve(
        process.cwd(),
        "..",
        "documents",
        "report-admin",
        "resolve-report-ai-v2",
        "09-sprints",
        "evidence",
        new Date().toISOString().replace(/[:.]/g, "-"),
      );
  fs.mkdirSync(path.join(evidenceDir, "screenshots"), { recursive: true });
  fs.mkdirSync(path.join(evidenceDir, "raw"), { recursive: true });
  return evidenceDir;
}

function createFix04EvidenceDir() {
  const configured = process.env.DOD_FIX04_EVIDENCE_DIR;
  const evidenceDir = configured
    ? path.resolve(configured)
    : path.resolve(
        process.cwd(),
        "..",
        "documents",
        "report-admin",
        "resolve-report-ai-v2",
        "09-sprints",
        "evidence",
        new Date().toISOString().replace(/[:.]/g, "-"),
      );
  fs.mkdirSync(path.join(evidenceDir, "screenshots"), { recursive: true });
  fs.mkdirSync(path.join(evidenceDir, "raw"), { recursive: true });
  return evidenceDir;
}

function createRemediationEvidenceDir(envName: "DOD_FIX05_EVIDENCE_DIR" | "DOD_FIX06_EVIDENCE_DIR") {
  const configured = process.env[envName];
  const evidenceDir = configured
    ? path.resolve(configured)
    : path.resolve(
        process.cwd(),
        "..",
        "documents",
        "report-admin",
        "resolve-report-ai-v2",
        "09-sprints",
        "evidence",
        new Date().toISOString().replace(/[:.]/g, "-"),
      );
  fs.mkdirSync(path.join(evidenceDir, "screenshots"), { recursive: true });
  fs.mkdirSync(path.join(evidenceDir, "raw"), { recursive: true });
  return evidenceDir;
}

function createAdminActionsEvidenceDir() {
  const configured = process.env.ADMIN_REPORT_ACTIONS_EVIDENCE_DIR;
  const evidenceDir = configured
    ? path.resolve(configured)
    : path.resolve(
        process.cwd(),
        "..",
        "documents",
        "ai-report-resolution-admin-actions",
        "evidence",
        new Date().toISOString().replace(/[:.]/g, "-"),
      );
  fs.mkdirSync(path.join(evidenceDir, "screenshots"), { recursive: true });
  fs.mkdirSync(path.join(evidenceDir, "raw"), { recursive: true });
  return evidenceDir;
}

function allowedActionForTarget(targetType: ReportTargetType, action: TargetAction) {
  if (action === "NO_ACTION" || action === "NONE") return true;
  if (targetType === "BLOG" || targetType === "COMMENT") {
    return ["KEEP_VISIBLE", "APPROVE", "HIDE", "REMOVE"].includes(action);
  }
  return false;
}

function resolutionContractOk(resolution: AiResolution | null | undefined) {
  return Boolean(
    resolution?.id &&
      resolution.contractVersion === "2.0" &&
      resolution.automationMode === "A0_RECOMMEND_ONLY" &&
      resolution.contentReportId &&
      resolution.targetType &&
      resolution.targetId &&
      resolution.reportDecision &&
      resolution.targetAction &&
      resolution.evidenceQuality &&
      resolution.evidenceSufficiency &&
      resolution.violationLikelihood &&
      resolution.harmSeverity &&
      resolution.actionRisk &&
      resolution.policyVersion &&
      resolution.ruleCatalogVersion &&
      resolution.promptVersion &&
      resolution.workflowVersion &&
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

async function fetchTargetSnapshot(
  page: Page,
  targetType: ReportTargetType,
  targetId: string,
): Promise<TargetSnapshot> {
  if (targetType === "BLOG") {
    const result = await apiFetch<Blog>(page, `/api/admin/blogs/${encodeURIComponent(targetId)}`);
    if (!result.data) throw new Error(`BLOG target ${targetId} was not returned.`);
    return {
      targetType,
      targetId,
      state: {
        authorUserId: result.data.authorUserId,
        content: result.data.content,
        status: result.data.status,
      },
    };
  }
  if (targetType === "COMMENT") {
    const result = await apiFetch<Comment>(page, `/api/admin/comments/${encodeURIComponent(targetId)}`);
    if (!result.data) throw new Error(`COMMENT target ${targetId} was not returned.`);
    return {
      targetType,
      targetId,
      state: {
        blogId: result.data.blogId,
        content: result.data.content,
        status: result.data.status,
        userId: result.data.userId,
      },
    };
  }
  if (targetType === "USER") {
    const result = await apiFetch<AdminUser>(page, `/api/admin/users/${encodeURIComponent(targetId)}`);
    if (!result.data) throw new Error(`USER target ${targetId} was not returned.`);
    return {
      targetType,
      targetId,
      state: {
        accountStatus: result.data.accountStatus,
        userName: result.data.userName,
      },
    };
  }
  const result = await apiFetch<CafePage>(page, `/api/admin/cafe-pages/${encodeURIComponent(targetId)}`);
  if (!result.data) throw new Error(`CAFE_PAGE target ${targetId} was not returned.`);
  return {
    targetType,
    targetId,
    state: {
      name: result.data.name,
      ownerUserId: result.data.ownerUserId,
      status: result.data.status,
    },
  };
}

function targetWasNotMutated(before: TargetSnapshot, after: TargetSnapshot) {
  return JSON.stringify(before) === JSON.stringify(after);
}

function reportStateSnapshot(report: ContentReport) {
  return {
    id: report.id,
    reporterUserId: report.reporterUserId,
    targetType: report.targetType,
    targetId: report.targetId,
    blogId: report.blogId,
    commentId: report.commentId,
    reportedUserId: report.reportedUserId,
    cafePageId: report.cafePageId,
    reasonId: report.reasonId,
    reasonCode: report.reasonCode,
    reasonLabel: report.reasonLabel,
    reasonSeverity: report.reasonSeverity,
    description: report.description,
    status: report.status,
    createdAt: report.createdAt,
    resolvedAt: report.resolvedAt,
  };
}

function reportWasNotMutated(before: ContentReport, after: ContentReport) {
  return JSON.stringify(reportStateSnapshot(before)) === JSON.stringify(reportStateSnapshot(after));
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
  test("E2E-S1-09 bulk partial failure keeps success and has no AI mutation", async ({ page }) => {
    test.setTimeout(10 * 60_000);
    loadLocalEnv();
    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createRemediationEvidenceDir("DOD_FIX05_EVIDENCE_DIR");
    const fixtures: Array<ReturnType<typeof createProviderUnavailableBlogFixture>> = [];
    const reports: ContentReport[] = [];

    try {
      const preflight = await request.newContext();
      await assertReachable(preflight, adminBaseUrl, "Admin UI");
      await assertReachable(preflight, `${apiBaseUrl.replace(/\/$/, "")}/api/auth/me`, "Backend API");
      await assertReachable(preflight, `${n8nBaseUrl.replace(/\/$/, "")}/healthz`, "n8n");
      const webhookPreflight = await checkWebhookActive(preflight);
      await preflight.dispose();
      expect(webhookPreflight.ok, webhookPreflight.message).toBe(true);

      await loginAsAdmin(page, adminEmail, adminPassword);
      const auth = await apiFetch<AuthResponse>(page, "/api/auth/me");
      const adminUserId = auth.data?.user.userId ?? "";
      if (!adminUserId) throw new Error("Unable to resolve current admin user id.");

      const reason = await chooseReason(page, "BLOG");
      for (const label of ["success", "terminal-failure"] as const) {
        const fixture = createProviderUnavailableBlogFixture(adminUserId);
        fixtures.push(fixture);
        const seed: TargetSeed = {
          targetType: "BLOG",
          targetId: fixture.blogId,
          reason,
          source: {
            id: fixture.blogId,
            authorUserId: fixture.targetOwnerId,
            status: "PUBLISHED",
            content: `Bulk partial failure fixture ${label}.`,
          },
        };
        const created = await createReportWithSelfHeal(
          page,
          seed,
          `${TEST_MARKER} E2E-S1-09 ${label} ${Date.now()} ${randomUUID()}`,
          adminUserId,
        );
        reports.push(created.report);
      }

      const [successfulReport, failingReport] = reports;
      const targetBefore = await Promise.all(
        reports.map((report) => fetchTargetSnapshot(page, report.targetType, report.targetId)),
      );
      const reportBefore = await Promise.all(
        reports.map((report) =>
          apiFetch<ContentReport>(page, `/api/admin/reports/${encodeURIComponent(report.id)}`)),
      );
      const historyBefore = await Promise.all(
        reports.map((report) =>
          apiFetch<PageResponse<AiResolution>>(
            page,
            withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
              page: 0,
              size: 20,
            }),
          )),
      );

      await page.goto("/reports");
      await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible();
      await page.getByRole("button", { name: "Generate AI recommendations" }).click();
      const bulkDialog = page.getByRole("dialog").filter({ hasText: "Generate AI recommendations" });
      await bulkDialog.getByRole("button", { name: "Selected reports" }).click();
      for (const report of reports) {
        const reportRow = bulkDialog.locator("label").filter({ hasText: report.description ?? report.id });
        await expect(reportRow).toBeVisible();
        await reportRow.getByRole("checkbox").check();
      }
      await expect(bulkDialog.getByText("Selected: 2 of", { exact: false })).toBeVisible();

      const terminalReport = await updateReportStatus(page, failingReport.id, "RESOLVED");
      expect(terminalReport.status).toBe("RESOLVED");
      const terminalReportBeforeBulk = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(failingReport.id)}`,
      );
      expect(terminalReportBeforeBulk.data?.status).toBe("RESOLVED");
      const reportBeforeBulk = [reportBefore[0].data!, terminalReportBeforeBulk.data!];

      const itemResponses: Array<{ reportId: string; status: number }> = [];
      const responseListener = (response: PlaywrightResponse) => {
        if (response.request().method() !== "POST") return;
        const match = response.url().match(/\/api\/admin\/reports\/([^/]+)\/ai-resolution(?:\?|$)/);
        if (match) itemResponses.push({ reportId: match[1], status: response.status() });
      };
      page.on("response", responseListener);
      await bulkDialog.getByRole("button", { name: "Run AI", exact: true }).click();
      await expect(bulkDialog.getByText("Total: 2", { exact: true })).toBeVisible({ timeout: 30_000 });
      await expect(bulkDialog.getByText("Remaining: 0", { exact: true })).toBeVisible({ timeout: 180_000 });
      page.off("response", responseListener);

      await expect(bulkDialog.getByText("Completed: 1", { exact: true })).toBeVisible();
      await expect(bulkDialog.getByText("Failed: 1", { exact: true })).toBeVisible();
      const failureAlert = bulkDialog.getByRole("alert");
      await expect(failureAlert).toContainText("1 report failed. Successful recommendations were kept.");
      await expect(bulkDialog.getByText(failingReport.id.slice(0, 8), { exact: true })).toBeVisible();
      const recommended = Number((await bulkDialog.getByText(/^Recommended:/).textContent())?.split(":")[1].trim());
      const manualReview = Number((await bulkDialog.getByText(/^Needs manual review:/).textContent())?.split(":")[1].trim());
      expect(recommended + manualReview).toBe(1);
      await failureAlert.scrollIntoViewIfNeeded();
      const partialFailureScreenshot = await screenshot(
        page,
        evidenceDir,
        "E2E-S1-09",
        "bulk-partial-failure",
      );

      const historyAfter = await Promise.all(
        reports.map((report) =>
          apiFetch<PageResponse<AiResolution>>(
            page,
            withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
              page: 0,
              size: 20,
            }),
          )),
      );
      const targetAfter = await Promise.all(
        reports.map((report) => fetchTargetSnapshot(page, report.targetType, report.targetId)),
      );
      const reportAfter = await Promise.all(
        reports.map((report) =>
          apiFetch<ContentReport>(page, `/api/admin/reports/${encodeURIComponent(report.id)}`)),
      );
      const jobsAfter = await Promise.all(
        reports.map((report) =>
          apiFetch<PageResponse<AutoApplyJob>>(
            page,
            withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-auto-resolutions`, {
              page: 0,
              size: 20,
            }),
            { failOnStatusCode: false },
          )),
      );

      const result = {
        itemResponses,
        successfulReport: {
          id: successfulReport.id,
          resolutionCountBefore: historyBefore[0].data?.totalElements ?? 0,
          resolutionCountAfter: historyAfter[0].data?.totalElements ?? 0,
          reportStatusBefore: reportBefore[0].data?.status,
          reportStatusAfter: reportAfter[0].data?.status,
          reportStateBefore: reportStateSnapshot(reportBeforeBulk[0]),
          reportStateAfter: reportStateSnapshot(reportAfter[0].data!),
          reportMutation: !reportWasNotMutated(reportBeforeBulk[0], reportAfter[0].data!),
          targetMutation: !targetWasNotMutated(targetBefore[0], targetAfter[0]),
          autoApplyJobCount: jobsAfter[0].data?.totalElements ?? 0,
        },
        failedReport: {
          id: failingReport.id,
          injectedStatus: terminalReport.status,
          resolutionCountBefore: historyBefore[1].data?.totalElements ?? 0,
          resolutionCountAfter: historyAfter[1].data?.totalElements ?? 0,
          reportStatusAfter: reportAfter[1].data?.status,
          reportStateBefore: reportStateSnapshot(reportBeforeBulk[1]),
          reportStateAfter: reportStateSnapshot(reportAfter[1].data!),
          reportMutation: !reportWasNotMutated(reportBeforeBulk[1], reportAfter[1].data!),
          targetMutation: !targetWasNotMutated(targetBefore[1], targetAfter[1]),
          autoApplyJobCount: jobsAfter[1].data?.totalElements ?? 0,
        },
        ui: {
          completed: 1,
          recommended,
          manualReview,
          failed: 1,
          remaining: 0,
        },
        screenshots: [partialFailureScreenshot],
      };
      rawFile(evidenceDir, "E2E-S1-09", "bulk-partial-failure-result", result);

      expect(itemResponses.map((item) => item.status).sort()).toEqual([200, 409]);
      expect(historyAfter[0].data?.totalElements ?? 0).toBe(
        (historyBefore[0].data?.totalElements ?? 0) + 1,
      );
      expect(historyAfter[1].data?.totalElements ?? 0).toBe(
        historyBefore[1].data?.totalElements ?? 0,
      );
      expect(reportWasNotMutated(reportBeforeBulk[0], reportAfter[0].data!)).toBe(true);
      expect(reportWasNotMutated(reportBeforeBulk[1], reportAfter[1].data!)).toBe(true);
      expect(targetWasNotMutated(targetBefore[0], targetAfter[0])).toBe(true);
      expect(targetWasNotMutated(targetBefore[1], targetAfter[1])).toBe(true);
      expect(jobsAfter.flatMap((resultItem) => resultItem.data?.content ?? [])).toEqual([]);
    } finally {
      for (const report of reports) {
        await cancelScheduledJobs(page, report.id).catch(() => undefined);
      }
      for (const [index, fixture] of fixtures.entries()) {
        cleanupProviderUnavailableBlogFixture(fixture, reports[index]?.id);
      }
      if (fixtures.length) {
        const blogIds = fixtures.map((fixture) => `'${fixture.blogId}'::uuid`).join(",");
        const reportIds = reports.map((report) => `'${report.id}'::uuid`).join(",");
        const cleanupCounts = psqlForReportAiFixture(`
          select count(*) from blogs where id in (${blogIds});
          ${reportIds ? `select count(*) from content_reports where id in (${reportIds});` : ""}
        `);
        rawFile(evidenceDir, "E2E-S1-09", "cleanup-verification", {
          remainingRowCounts: cleanupCounts.split(/\r?\n/).filter(Boolean).map(Number),
        });
      }
    }
  });

  test("E2E-S1-10 terminal report disables Ask AI and Backend rejects bypass", async ({ page }) => {
    test.setTimeout(5 * 60_000);
    loadLocalEnv();
    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createRemediationEvidenceDir("DOD_FIX06_EVIDENCE_DIR");
    let fixture: ReturnType<typeof createProviderUnavailableBlogFixture> | null = null;
    let report: ContentReport | null = null;

    try {
      const preflight = await request.newContext();
      await assertReachable(preflight, adminBaseUrl, "Admin UI");
      await assertReachable(preflight, `${apiBaseUrl.replace(/\/$/, "")}/api/auth/me`, "Backend API");
      await preflight.dispose();

      await loginAsAdmin(page, adminEmail, adminPassword);
      const auth = await apiFetch<AuthResponse>(page, "/api/auth/me");
      const adminUserId = auth.data?.user.userId ?? "";
      if (!adminUserId) throw new Error("Unable to resolve current admin user id.");

      fixture = createProviderUnavailableBlogFixture(adminUserId);
      const reason = await chooseReason(page, "BLOG");
      const created = await createReportWithSelfHeal(
        page,
        {
          targetType: "BLOG",
          targetId: fixture.blogId,
          reason,
          source: {
            id: fixture.blogId,
            authorUserId: fixture.targetOwnerId,
            status: "PUBLISHED",
            content: "Terminal report FE guard fixture.",
          },
        },
        `${TEST_MARKER} E2E-S1-10 terminal FE guard ${Date.now()}`,
        adminUserId,
      );
      report = created.report;
      const targetBefore = await fetchTargetSnapshot(page, report.targetType, report.targetId);
      const terminalReport = await updateReportStatus(page, report.id, "RESOLVED");
      const persistedTerminalReport = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(report.id)}`,
      );
      expect(persistedTerminalReport.data?.status).toBe("RESOLVED");
      const historyBefore = await apiFetch<PageResponse<AiResolution>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
          page: 0,
          size: 20,
        }),
      );

      let uiAiRequestCount = 0;
      const requestListener = (outgoingRequest: { method: () => string; url: () => string }) => {
        if (
          outgoingRequest.method() === "POST" &&
          outgoingRequest.url().includes(`/api/admin/reports/${report!.id}/ai-resolution`)
        ) {
          uiAiRequestCount += 1;
        }
      };
      page.on("request", requestListener);
      await openReportDetail(page, persistedTerminalReport.data!);
      const detailDialog = page.getByRole("dialog").filter({ hasText: "Report detail" });
      const askAiButton = detailDialog.getByRole("button", { name: "Ask AI" });
      await expect(askAiButton).toBeDisabled();
      await askAiButton.evaluate((button: HTMLButtonElement) => button.click());
      await page.waitForTimeout(300);
      page.off("request", requestListener);
      expect(uiAiRequestCount).toBe(0);
      const terminalGuardScreenshot = await screenshot(
        page,
        evidenceDir,
        "E2E-S1-10",
        "terminal-report-ask-ai-disabled",
      );

      const bypassAttempt = await createAiResolution(page, persistedTerminalReport.data!);
      const historyAfter = await apiFetch<PageResponse<AiResolution>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
          page: 0,
          size: 20,
        }),
      );
      const reportAfter = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(report.id)}`,
      );
      const targetAfter = await fetchTargetSnapshot(page, report.targetType, report.targetId);
      const jobsAfter = await apiFetch<PageResponse<AutoApplyJob>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-auto-resolutions`, {
          page: 0,
          size: 20,
        }),
        { failOnStatusCode: false },
      );
      rawFile(evidenceDir, "E2E-S1-10", "terminal-report-guard-result", {
        terminalStatus: terminalReport.status,
        askAiDisabled: await askAiButton.isDisabled(),
        uiAiRequestCount,
        bypassHttpStatus: bypassAttempt.response.status(),
        bypassResponse: bypassAttempt.raw,
        resolutionCountBefore: historyBefore.data?.totalElements ?? 0,
        resolutionCountAfter: historyAfter.data?.totalElements ?? 0,
        reportStatusAfter: reportAfter.data?.status,
        reportStateBefore: reportStateSnapshot(persistedTerminalReport.data!),
        reportStateAfter: reportStateSnapshot(reportAfter.data!),
        reportMutation: !reportWasNotMutated(persistedTerminalReport.data!, reportAfter.data!),
        targetMutation: !targetWasNotMutated(targetBefore, targetAfter),
        autoApplyJobCount: jobsAfter.data?.totalElements ?? 0,
        screenshots: [terminalGuardScreenshot],
      });

      expect(bypassAttempt.response.status()).toBe(409);
      expect(historyAfter.data?.totalElements ?? 0).toBe(historyBefore.data?.totalElements ?? 0);
      expect(reportWasNotMutated(persistedTerminalReport.data!, reportAfter.data!)).toBe(true);
      expect(targetWasNotMutated(targetBefore, targetAfter)).toBe(true);
      expect(jobsAfter.data?.content ?? []).toEqual([]);
    } finally {
      if (report) {
        await cancelScheduledJobs(page, report.id).catch(() => undefined);
      }
      if (fixture) {
        cleanupProviderUnavailableBlogFixture(fixture, report?.id);
        const cleanupCounts = psqlForReportAiFixture(`
          select count(*) from blogs where id='${fixture.blogId}'::uuid;
          ${report ? `select count(*) from content_reports where id='${report.id}'::uuid;` : ""}
        `);
        rawFile(evidenceDir, "E2E-S1-10", "cleanup-verification", {
          remainingRowCounts: cleanupCounts.split(/\r?\n/).filter(Boolean).map(Number),
        });
      }
    }
  });

  test("E2E-S1-13 provider boundary unavailable is operational error and retryable", async ({ page }) => {
    test.setTimeout(10 * 60_000);
    loadLocalEnv();
    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createFix04EvidenceDir();
    let fixture: ReturnType<typeof createProviderUnavailableBlogFixture> | null = null;
    let report: ContentReport | null = null;
    let n8nStopped = false;

    try {
      const preflight = await request.newContext();
      await assertReachable(preflight, adminBaseUrl, "Admin UI");
      await assertReachable(preflight, `${apiBaseUrl.replace(/\/$/, "")}/api/auth/me`, "Backend API");
      await assertReachable(preflight, `${n8nBaseUrl.replace(/\/$/, "")}/healthz`, "n8n");
      const webhookPreflight = await checkWebhookActive(preflight);
      await preflight.dispose();
      expect(webhookPreflight.ok, webhookPreflight.message).toBe(true);

      await loginAsAdmin(page, adminEmail, adminPassword);
      const auth = await apiFetch<AuthResponse>(page, "/api/auth/me");
      const adminUserId = auth.data?.user.userId ?? "";
      if (!adminUserId) throw new Error("Unable to resolve current admin user id.");

      fixture = createProviderUnavailableBlogFixture(adminUserId);
      const reason = await chooseReason(page, "BLOG");
      const seed: TargetSeed = {
        targetType: "BLOG",
        targetId: fixture.blogId,
        reason,
        source: {
          id: fixture.blogId,
          authorUserId: fixture.targetOwnerId,
          status: "PUBLISHED",
          content: "Bài viết fixture dùng để kiểm chứng lỗi vận hành provider.",
        },
      };
      const created = await createReportWithSelfHeal(
        page,
        seed,
        `${TEST_MARKER} E2E-S1-13 provider unavailable ${Date.now()}`,
        adminUserId,
      );
      report = created.report;
      const targetBefore = await fetchTargetSnapshot(page, "BLOG", fixture.blogId);
      const reportBefore = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(report.id)}`,
      );
      const historyBefore = await apiFetch<PageResponse<AiResolution>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
          page: 0,
          size: 20,
        }),
      );

      await openReportDetail(page, report);
      const detailDialog = page.getByRole("dialog").filter({ hasText: "Report detail" });
      await detailDialog.getByRole("button", { name: "Ask AI" }).click();
      const askDialog = page.getByRole("dialog").filter({ hasText: "Ask AI for report resolution" });
      await expect(askDialog).toBeVisible();

      stopN8nForProviderBoundaryTest();
      n8nStopped = true;
      const failureResponsePromise = page.waitForResponse(
        (response) =>
          response.url().includes(`/api/admin/reports/${report!.id}/ai-resolution`) &&
          response.request().method() === "POST",
        { timeout: 120_000 },
      );
      await askDialog.getByRole("button", { name: "Ask AI" }).click();
      const failureResponse = await failureResponsePromise;
      const failureParsed = await parseResponse<AdminReportAiOperationalError>(failureResponse);
      const historyAfterFailure = await apiFetch<PageResponse<AiResolution>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
          page: 0,
          size: 20,
        }),
      );
      rawFile(evidenceDir, "E2E-S1-13", "provider-boundary-failure", {
        httpStatus: failureResponse.status(),
        error: failureParsed.raw,
        resolutionCountBefore: historyBefore.data?.totalElements ?? 0,
        resolutionCountAfterFailure: historyAfterFailure.data?.totalElements ?? 0,
      });

      expect(historyAfterFailure.data?.totalElements ?? 0).toBe(
        historyBefore.data?.totalElements ?? 0,
      );

      const alert = askDialog.getByRole("alert");
      const failureScreenshot = await screenshot(
        page,
        evidenceDir,
        "E2E-S1-13",
        "provider-boundary-operational-error",
      );
      await expect(alert).toContainText("AI recommendation was not created.");
      await expect(alert).toContainText("AI recommendation service is unavailable.");
      await expect(alert).toContainText("AI_PROVIDER_BOUNDARY_FAILED");
      await expect(alert).toContainText("Retry available");
      await expect(alert).toContainText((failureParsed.raw as AdminReportAiOperationalError).correlationId);
      expect(failureResponse.status()).toBe(502);
      expect(failureParsed.raw).toMatchObject({
        code: "AI_PROVIDER_BOUNDARY_FAILED",
        message: "AI recommendation service is unavailable.",
        retryable: true,
        stage: "N8N_PROVIDER",
      });
      expect((failureParsed.raw as AdminReportAiOperationalError).correlationId).toMatch(
        /^[0-9a-f-]{36}$/i,
      );

      startN8nAfterProviderBoundaryTest();
      n8nStopped = false;
      await waitForN8nRecovery(page.request);
      await expect(askDialog.getByRole("button", { name: "Ask AI" })).toBeEnabled();

      const retryResponsePromise = page.waitForResponse(
        (response) =>
          response.url().includes(`/api/admin/reports/${report!.id}/ai-resolution`) &&
          response.request().method() === "POST",
        { timeout: 120_000 },
      );
      await askDialog.getByRole("button", { name: "Ask AI" }).click();
      const retryResponse = await retryResponsePromise;
      const retryParsed = await parseResponse<AiResolution>(retryResponse);
      await expect(askDialog).toBeHidden();

      const targetAfter = await fetchTargetSnapshot(page, "BLOG", fixture.blogId);
      const reportAfter = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(report.id)}`,
      );
      const historyAfterRetry = await apiFetch<PageResponse<AiResolution>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-resolutions`, {
          page: 0,
          size: 20,
        }),
      );
      const jobs = await apiFetch<PageResponse<AutoApplyJob>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-auto-resolutions`, {
          page: 0,
          size: 20,
        }),
        { failOnStatusCode: false },
      );
      const noTargetMutation = targetWasNotMutated(targetBefore, targetAfter);
      const noReportMutation = reportBefore.data?.status === reportAfter.data?.status;
      rawFile(evidenceDir, "E2E-S1-13", "retry-recovery", {
        httpStatus: retryResponse.status(),
        resolution: retryParsed.data,
        resolutionCountAfterRetry: historyAfterRetry.data?.totalElements ?? 0,
        noTargetMutation,
        noReportMutation,
        autoApplyJobCount: jobs.data?.totalElements ?? jobs.data?.content.length ?? 0,
        screenshots: [failureScreenshot],
      });

      expect(retryResponse.ok()).toBe(true);
      expect(resolutionContractOk(retryParsed.data)).toBe(true);
      expect(historyAfterRetry.data?.totalElements ?? 0).toBe(
        (historyBefore.data?.totalElements ?? 0) + 1,
      );
      expect(noTargetMutation).toBe(true);
      expect(noReportMutation).toBe(true);
      expect(jobs.data?.content ?? []).toEqual([]);
    } finally {
      if (n8nStopped) {
        startN8nAfterProviderBoundaryTest();
        await waitForN8nRecovery(page.request).catch(() => undefined);
      }
      if (report) {
        await cancelScheduledJobs(page, report.id).catch(() => undefined);
        await closeReport(page, report.id, "REJECTED").catch(() => undefined);
      }
      if (fixture) {
        cleanupProviderUnavailableBlogFixture(fixture, report?.id);
        const cleanupCounts = psqlForReportAiFixture(`
          select count(*) from blogs where id='${fixture.blogId}'::uuid;
          ${report ? `select count(*) from content_reports where id='${report.id}'::uuid;` : ""}
        `);
        rawFile(evidenceDir, "E2E-S1-13", "cleanup-verification", {
          remainingRowCounts: cleanupCounts.split(/\r?\n/).filter(Boolean).map(Number),
        });
      }
    }
  });

  test("E2E-S1-04 COMMENT missing critical parent context is manual and has no mutation", async ({ page }) => {
    test.setTimeout(10 * 60_000);
    loadLocalEnv();
    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createFix03EvidenceDir();
    let fixture: ReturnType<typeof createMissingCriticalCommentContextFixture> | null = null;
    let report: ContentReport | null = null;

    try {
      const preflight = await request.newContext();
      await assertReachable(preflight, adminBaseUrl, "Admin UI");
      await assertReachable(preflight, `${apiBaseUrl.replace(/\/$/, "")}/api/auth/me`, "Backend API");
      await assertReachable(preflight, `${n8nBaseUrl.replace(/\/$/, "")}/healthz`, "n8n");
      const webhookPreflight = await checkWebhookActive(preflight);
      await preflight.dispose();
      expect(webhookPreflight.ok, webhookPreflight.message).toBe(true);

      await loginAsAdmin(page, adminEmail, adminPassword);
      const auth = await apiFetch<AuthResponse>(page, "/api/auth/me");
      const adminUserId = auth.data?.user.userId ?? "";
      if (!adminUserId) throw new Error("Unable to resolve current admin user id.");

      fixture = createMissingCriticalCommentContextFixture(adminUserId);
      const reason = await chooseReason(page, "COMMENT");
      const seed: TargetSeed = {
        targetType: "COMMENT",
        targetId: fixture.commentId,
        reason,
        source: {
          id: fixture.commentId,
          blogId: fixture.blogId,
          userId: fixture.targetOwnerId,
          status: "PUBLISHED",
          content: "Đúng vậy.",
        },
      };
      const created = await createReportWithSelfHeal(
        page,
        seed,
        `${TEST_MARKER} E2E-S1-04 missing critical parent context ${Date.now()}`,
        adminUserId,
      );
      report = created.report;
      const targetBefore = await fetchTargetSnapshot(page, "COMMENT", fixture.commentId);
      const result = await createAiResolution(page, report);
      const targetAfter = await fetchTargetSnapshot(page, "COMMENT", fixture.commentId);
      const jobs = await apiFetch<PageResponse<AutoApplyJob>>(
        page,
        withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-auto-resolutions`, {
          page: 0,
          size: 20,
        }),
        { failOnStatusCode: false },
      );
      const noMutation = targetWasNotMutated(targetBefore, targetAfter);
      const resolution = result.data;
      rawFile(evidenceDir, "E2E-S1-04", "runtime-result", {
        httpStatus: result.response.status(),
        resolution,
        noMutation,
        autoApplyJobCount: jobs.data?.totalElements ?? jobs.data?.content.length ?? 0,
      });

      await openReportDetail(page, report);
      await expect(page.getByText("CRITICAL_EVIDENCE_MISSING")).toBeVisible();
      await expect(page.getByText("UNASSESSABLE", { exact: true })).toBeVisible();
      const screenshotFile = await screenshot(
        page,
        evidenceDir,
        "E2E-S1-04",
        "comment-missing-critical-context-manual",
      );
      rawFile(evidenceDir, "E2E-S1-04", "ui-result", { screenshotFile });

      expect(result.response.ok()).toBe(true);
      expect(resolution?.reportDecision).toBe("NEEDS_MANUAL_REVIEW");
      expect(resolution?.targetAction).toBe("NO_ACTION");
      expect(resolution?.findings ?? []).toEqual([]);
      expect(resolution?.blockedReasons).toContain("CRITICAL_EVIDENCE_MISSING");
      expect(resolution?.evidenceSufficiency).toBe("UNASSESSABLE");
      expect(noMutation).toBe(true);
      expect(jobs.data?.content ?? []).toEqual([]);
    } finally {
      if (report) {
        await cancelScheduledJobs(page, report.id).catch(() => undefined);
        await closeReport(page, report.id, "REJECTED").catch(() => undefined);
      }
      if (fixture) {
        cleanupMissingCriticalCommentContextFixture(fixture, report?.id);
        const cleanupCounts = psqlForReportAiFixture(`
          select count(*) from comments where id='${fixture.commentId}'::uuid;
          select count(*) from blogs where id='${fixture.blogId}'::uuid;
          ${report ? `select count(*) from content_reports where id='${report.id}'::uuid;` : ""}
        `);
        rawFile(evidenceDir, "E2E-S1-04", "cleanup-verification", {
          remainingRowCounts: cleanupCounts.split(/\r?\n/).filter(Boolean).map(Number),
        });
      }
    }
  });

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

      const aiResults: Partial<Record<ReportTargetType, {
        resolution: AiResolution | null;
        durationMs: number;
        rawFile: string;
        targetBefore: TargetSnapshot;
        targetAfter: TargetSnapshot;
      }>> = {};
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
        const targetBefore = await fetchTargetSnapshot(page, targetType, report.targetId);
        const result = scenarioId === "RAI-07"
          ? await askAiViaUi(page, report).then((uiResult) => ({
              data: uiResult.data,
              raw: uiResult.raw,
              response: uiResult.response,
              durationMs: Date.now() - started,
            }))
          : await createAiResolution(page, report);
        const targetAfter = await fetchTargetSnapshot(page, targetType, report.targetId);
        const noMutation = targetWasNotMutated(targetBefore, targetAfter);
        const resolution = result.data;
        const raw = rawFile(evidenceDir, scenarioId, `ai-resolution-${targetType}`, result.raw);
        const noMutationRaw = rawFile(evidenceDir, scenarioId, `target-no-mutation-${targetType}`, {
          targetBefore,
          targetAfter,
          noMutation,
        });
        aiResults[targetType] = {
          resolution: resolution ?? null,
          durationMs: result.durationMs,
          rawFile: raw,
          targetBefore,
          targetAfter,
        };
        if (scenarioId !== "RAI-07") await openReportDetail(page, report);
        const shot = await screenshot(page, evidenceDir, scenarioId, `ai-resolution-${targetType}`);
        const aiFixes = result.response.ok()
          ? []
          : [`POST /api/admin/reports/{reportId}/ai-resolution returned ${result.response.status()}; check BE webhook URL, active n8n workflow cafestory-admin-report-ai-resolution, and OpenAI credentials in n8n.`];
        records.push(makeRecord(scenarioId, {
          durationMs: Date.now() - started,
          screenshots: [shot],
          rawFiles: [raw, noMutationRaw],
          notes: [performanceNote(result.durationMs), `Target unchanged: ${noMutation}.`],
          fixRecommendations: [
            ...aiFixes,
            ...(result.durationMs > LATENCY_PASS_MS ? ["Investigate n8n/OpenAI latency for admin report AI resolution workflow."] : []),
          ],
          criteria: {
            setup: true,
            ui: true,
            apiAi: result.response.ok() && Boolean(resolution) && allowedActionForTarget(targetType, resolution!.targetAction),
            safety: isSecretSafe(result.raw) && noMutation,
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
        (item): item is {
          resolution: AiResolution;
          durationMs: number;
          rawFile: string;
          targetBefore: TargetSnapshot;
          targetAfter: TargetSnapshot;
        } =>
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
        await openReportDetail(page, autoReport);
        const detailDialog = page.getByRole("dialog").filter({ hasText: "Report detail" });
        await detailDialog.getByRole("button", { name: "Ask AI" }).click();
        const askDialog = page.getByRole("dialog").filter({ hasText: "Ask AI for report resolution" });
        const a0NoticeVisible = await askDialog.getByText("A0 recommendation-only mode").isVisible();
        const autoCreationControlVisible = await askDialog.getByText("Auto apply after delay").isVisible().catch(() => false);
        const uiShot = await screenshot(page, evidenceDir, "RAI-14", "a0-recommendation-only-ui");
        await askDialog.getByRole("button", { name: "Close" }).click();
        records.push(makeRecord("RAI-14", {
          durationMs: Date.now() - started,
          screenshots: [uiShot],
          criteria: {
            setup: true,
            ui: a0NoticeVisible && !autoCreationControlVisible,
            apiAi: true,
            safety: true,
            performance: true,
          },
        }));

        const targetBeforeAutoRequest = await fetchTargetSnapshot(page, autoReport.targetType, autoReport.targetId);
        const result = await createAiResolution(page, autoReport, { autoApplyEnabled: true, autoApplyDelayMinutes: 15 });
        const targetAfterAutoRequest = await fetchTargetSnapshot(page, autoReport.targetType, autoReport.targetId);
        autoResolution = result.data ?? null;
        autoJob = autoResolution?.autoApplyJob ?? null;
        const raw = rawFile(evidenceDir, "RAI-15", "a0-blocked-auto-apply", result.raw);
        await openReportDetail(page, autoReport);
        const shot = await screenshot(page, evidenceDir, "RAI-15", "a0-blocked-auto-apply-ui");
        records.push(makeRecord("RAI-15", {
          durationMs: result.durationMs,
          screenshots: [shot],
          rawFiles: [raw],
          notes: [autoResolution?.autoApplyWarning ?? "No A0 warning returned."],
          fixRecommendations: result.response.ok()
            ? []
            : [`Legacy autoApply request returned ${result.response.status()}; inspect A0 compatibility handling.`],
          criteria: {
            setup: true,
            ui: true,
            apiAi: result.response.ok() && Boolean(autoResolution),
            safety: !autoJob && Boolean(autoResolution?.autoApplyWarning?.includes("A0_RECOMMEND_ONLY")),
            performance: performanceCriterion(result.durationMs),
          },
        }));

        const legacyJobs = await apiFetch<PageResponse<AutoApplyJob>>(
          page,
          withQuery(`/api/admin/reports/${encodeURIComponent(autoReport.id)}/ai-auto-resolutions`, { page: 0, size: 20 }),
        );
        const cancellableLegacyJob = legacyJobs.data?.content.find((job) => job.status === "SCHEDULED") ?? null;
        if (cancellableLegacyJob) {
          const cancelStarted = Date.now();
          const cancel = await apiFetch<AutoApplyJob>(page, `/api/admin/reports/ai-auto-resolutions/${encodeURIComponent(cancellableLegacyJob.id)}/cancel`, { method: "POST" });
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
            notes: ["No pre-existing SCHEDULED legacy job fixture exists; cancel endpoint remains covered by Backend tests."],
          }));
        }

        const reportAfter = await apiFetch<ContentReport>(
          page,
          `/api/admin/reports/${encodeURIComponent(autoReport.id)}`,
        );
        const targetNoMutation = targetWasNotMutated(targetBeforeAutoRequest, targetAfterAutoRequest);
        records.push(makeRecord("RAI-17", {
          rawFiles: [rawFile(evidenceDir, "RAI-17", "a0-safety-invariant", {
            resolution: autoResolution,
            reportAfter: reportAfter.data,
            targetBefore: targetBeforeAutoRequest,
            targetAfter: targetAfterAutoRequest,
            targetNoMutation,
          })],
          criteria: {
            setup: Boolean(autoResolution),
            ui: true,
            apiAi: !autoJob,
            safety: reportAfter.data?.status === autoReport.status && targetNoMutation,
            performance: true,
          },
        }));

        const repeat = await createAiResolution(page, autoReport, { autoApplyEnabled: true, autoApplyDelayMinutes: 15 });
        const repeatResolution = repeat.data ?? null;
        records.push(makeRecord("RAI-18", {
          durationMs: repeat.durationMs,
          rawFiles: [rawFile(evidenceDir, "RAI-18", "repeat-a0-request", repeat.raw)],
          criteria: {
            setup: true,
            ui: true,
            apiAi: repeat.response.ok() && Boolean(repeatResolution),
            safety: !repeatResolution?.autoApplyJob,
            performance: performanceCriterion(repeat.durationMs),
          },
        }));

        records.push(makeRecord("RAI-19", {
          rawFiles: [rawFile(evidenceDir, "RAI-19", "legacy-auto-job-history", legacyJobs.raw)],
          criteria: {
            setup: true,
            ui: await page.getByText("Legacy auto-apply history").isVisible().catch(() => false),
            apiAi: Array.isArray(legacyJobs.data?.content),
            safety: isSecretSafe(legacyJobs.raw),
            performance: true,
          },
        }));
      } else {
        for (const id of ["RAI-14", "RAI-15", "RAI-16", "RAI-17", "RAI-18", "RAI-19"] as const) {
          records.push(makeRecord(id, { status: "BLOCKED", notes: ["No report is available for auto apply test."] }));
        }
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
          && await bulkDialog.getByText(/Completed: [1-9]\d*/).isVisible().catch(() => false);
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
        const a0BulkNotice = await bulkDialog.getByText("Recommendation only — automation is disabled").isVisible();
        const autoControlVisible = await bulkDialog.getByText("Auto apply after delay").isVisible().catch(() => false);
        const confirmShot = await screenshot(page, evidenceDir, "RAI-26", "bulk-a0-notice");
        records.push(makeRecord("RAI-26", {
          screenshots: [confirmShot],
          criteria: { setup: true, ui: a0BulkNotice && !autoControlVisible, apiAi: true, safety: true, performance: true },
        }));

        const bulkRecommendationStarted = Date.now();
        await bulkDialog.getByRole("button", { name: "Run AI", exact: true }).click();
        await expect(page.getByText("Total:")).toBeVisible({ timeout: 30_000 });
        await expect(page.getByText(/Remaining: 0/)).toBeVisible({ timeout: 180_000 });
        const bulkRecommendationSucceeded = await bulkDialog.getByText("Failed: 0").isVisible().catch(() => false)
          && await bulkDialog.getByText(/Completed: [1-9]\d*/).isVisible().catch(() => false);
        const bulkJobs = await Promise.all(
          bulkReports.map((report) =>
            apiFetch<PageResponse<AutoApplyJob>>(
              page,
              withQuery(`/api/admin/reports/${encodeURIComponent(report.id)}/ai-auto-resolutions`, { page: 0, size: 20 }),
            )),
        );
        const noNewJobs = bulkJobs.every((result) =>
          (result.data?.content ?? []).every((job) => job.status !== "SCHEDULED" && job.status !== "APPLYING"));
        const bulkAutoShot = await screenshot(page, evidenceDir, "RAI-27", "bulk-recommendation-only");
        records.push(makeRecord("RAI-27", {
          durationMs: Date.now() - bulkRecommendationStarted,
          screenshots: [bulkAutoShot],
          criteria: {
            setup: true,
            ui: true,
            apiAi: bulkRecommendationSucceeded,
            safety: noNewJobs,
            performance: performanceCriterion(Date.now() - bulkRecommendationStarted),
          },
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

  test("E2E-REPORT-ACTIONS BLOG and COMMENT require admin confirmation and expose policy", async ({ page }) => {
    test.setTimeout(8 * 60_000);
    loadLocalEnv();
    const adminEmail = requireEnv("ADMIN_TEST_EMAIL");
    const adminPassword = requireEnv("ADMIN_TEST_PASSWORD");
    const evidenceDir = createAdminActionsEvidenceDir();
    const createdReports: ContentReport[] = [];
    const initialTargetStatuses = new Map<string, PostStatus>();

    await page.addInitScript(() => {
      window.localStorage.setItem("cafestory-admin-locale-preference", "en");
    });

    try {
      await loginAsAdmin(page, adminEmail, adminPassword);
      const auth = await apiFetch<AuthResponse>(page, "/api/auth/me");
      const adminUserId = auth.data?.user.userId ?? "";
      if (!adminUserId) throw new Error("Unable to resolve current admin user id.");
      const targets = await collectTargets(page, adminUserId);
      const blogSeed = targets.BLOG;
      const commentSeed = targets.COMMENT;
      if (!blogSeed || !commentSeed) {
        throw new Error("BLOG and COMMENT PUBLISHED fixtures are required for report action E2E.");
      }

      for (const seed of [blogSeed, commentSeed]) {
        initialTargetStatuses.set(seed.targetId, (seed.source as Blog | Comment).status);
        const created = await createReportWithSelfHeal(
          page,
          seed,
          `${TEST_MARKER} ACTIONS ${seed.targetType} ${Date.now()} ${randomUUID()}`,
          adminUserId,
        );
        createdReports.push(created.report);
      }

      const blogReport = createdReports.find((report) => report.targetType === "BLOG")!;
      const targetBeforeAi = await fetchTargetSnapshot(page, "BLOG", blogReport.targetId);
      const reportBeforeAi = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(blogReport.id)}`,
      );
      const aiResult = await askAiViaUi(page, blogReport);
      expect(aiResult.response.ok(), JSON.stringify(aiResult.raw).slice(0, 500)).toBe(true);
      const targetAfterAi = await fetchTargetSnapshot(page, "BLOG", blogReport.targetId);
      const reportAfterAi = await apiFetch<ContentReport>(
        page,
        `/api/admin/reports/${encodeURIComponent(blogReport.id)}`,
      );
      expect(targetWasNotMutated(targetBeforeAi, targetAfterAi)).toBe(true);
      expect(reportWasNotMutated(reportBeforeAi.data!, reportAfterAi.data!)).toBe(true);

      for (const report of createdReports) {
        await openReportDetail(page, report);
        const detailDialog = page.getByRole("dialog").filter({ hasText: "Report detail" });
        await expect(detailDialog.getByText(report.targetId).first()).toBeVisible({ timeout: 30_000 });
        const policyResponsePromise = page.waitForResponse(
          (response) =>
            response.url().includes(`/api/admin/reports/${report.id}/ai-policy`) &&
            response.request().method() === "GET",
        );
        const viewPolicyButton = detailDialog.getByRole("button", { name: "View policy" });
        await viewPolicyButton.focus();
        await expect(viewPolicyButton).toBeFocused();
        await viewPolicyButton.press("Enter");
        const policyResponse = await policyResponsePromise;
        expect(policyResponse.ok()).toBe(true);
        const policyPayload = await parseResponse<AiPolicy>(policyResponse);
        const policy = policyPayload.data;
        expect(policy).not.toBeNull();
        if (!policy) throw new Error("Policy API did not return a data payload.");
        expect(policy.recommendationOnly).toBe(true);
        expect(policy.candidateRules.length).toBeGreaterThan(0);
        const policySheet = page.getByRole("dialog").filter({ hasText: "AI policy" });
        await expect(policySheet.getByText(policy.candidateRules[0].ruleId).first()).toBeVisible();
        const technicalDetailsTab = policySheet.getByRole("tab", { name: "Technical details" });
        await technicalDetailsTab.focus();
        await expect(technicalDetailsTab).toBeFocused();
        await technicalDetailsTab.press("Enter");
        await expect(policySheet.getByText(policy.contextSchemaVersion).first()).toBeVisible();
        await expect(policySheet.getByText(policy.ruleCatalogVersion).first()).toBeVisible();
        await screenshot(page, evidenceDir, `POLICY-${report.targetType}`, "desktop-light");
        await page.evaluate(() => document.documentElement.classList.add("dark"));
        await screenshot(page, evidenceDir, `POLICY-${report.targetType}`, "desktop-dark");
        await page.evaluate(() => document.documentElement.classList.remove("dark"));
        await policySheet.getByRole("button", { name: "Close" }).click();

        const patchResponsePromise = page.waitForResponse(
          (response) =>
            response.url().includes(`/api/admin/${report.targetType === "BLOG" ? "blogs" : "comments"}/${report.targetId}/status`) &&
            response.request().method() === "PATCH",
        );
        await detailDialog.getByRole("button", { name: "Hide content" }).click();
        const confirmDialog = page.getByRole("dialog").filter({ hasText: "does not close the report" });
        await expect(confirmDialog.getByText(report.targetId)).toBeVisible();
        await confirmDialog.getByRole("button", { name: "Confirm Hidden" }).click();
        expect((await patchResponsePromise).ok()).toBe(true);
        const targetHidden = await fetchTargetSnapshot(page, report.targetType, report.targetId);
        const reportUnchanged = await apiFetch<ContentReport>(
          page,
          `/api/admin/reports/${encodeURIComponent(report.id)}`,
        );
        expect(targetHidden.state.status).toBe("HIDDEN");
        expect(reportUnchanged.data?.status).toBe(report.status);
        await expect(detailDialog.getByText(/Target status changed from/)).toBeVisible();
        if (report.targetType === "BLOG") {
          await expect(detailDialog.getByText("AI recommendation may be stale")).toBeVisible();
        }

        await page.setViewportSize({ width: 390, height: 844 });
        await detailDialog.getByText("Moderate content").scrollIntoViewIfNeeded();
        await screenshot(page, evidenceDir, `ACTION-${report.targetType}`, "mobile");
        const noHorizontalOverflow = await page.evaluate(
          () => document.documentElement.scrollWidth <= document.documentElement.clientWidth,
        );
        expect(noHorizontalOverflow).toBe(true);
        await page.setViewportSize({ width: 1440, height: 1000 });

        const restoreResponsePromise = page.waitForResponse(
          (response) =>
            response.url().includes(`/api/admin/${report.targetType === "BLOG" ? "blogs" : "comments"}/${report.targetId}/status`) &&
            response.request().method() === "PATCH",
        );
        await detailDialog.getByRole("button", { name: "Publish content" }).click();
        const restoreDialog = page.getByRole("dialog").filter({ hasText: "does not close the report" });
        await restoreDialog.getByRole("button", { name: "Confirm Published" }).click();
        expect((await restoreResponsePromise).ok()).toBe(true);
        const restoredTarget = await fetchTargetSnapshot(page, report.targetType, report.targetId);
        expect(restoredTarget.state.status).toBe(initialTargetStatuses.get(report.targetId));
        await detailDialog.getByRole("button", { name: "Close" }).click();
      }

      writeJson(path.join(evidenceDir, "raw", "report-actions-policy-summary.json"), {
        createdReports: createdReports.map((report) => ({
          id: report.id,
          targetType: report.targetType,
          targetId: report.targetId,
          originalReportStatus: report.status,
        })),
        aiDidNotMutateBlogTarget: true,
        aiDidNotMutateBlogReport: true,
        policyRecommendationOnly: true,
        targetStatesRestored: true,
      });
    } finally {
      for (const report of createdReports) {
        const originalStatus = initialTargetStatuses.get(report.targetId);
        if (originalStatus) {
          await apiFetch(
            page,
            `/api/admin/${report.targetType === "BLOG" ? "blogs" : "comments"}/${encodeURIComponent(report.targetId)}/status`,
            { method: "PATCH", body: { status: originalStatus }, failOnStatusCode: false },
          ).catch(() => undefined);
        }
        await closeReport(page, report.id, "REJECTED").catch(() => undefined);
      }
    }
  });
});
