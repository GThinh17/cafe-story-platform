import { expect, test, type Page, type Route } from "@playwright/test";
import {
  formatCompactNumber,
  formatCurrency,
  formatDate,
  formatNumber,
  formatTime,
} from "../../src/features/i18n/formatters";

const API_PATTERN = "http://localhost:8080/**";
const ADMIN_USER = {
  userId: "admin-i18n-user",
  userName: "admin.i18n",
  userFullName: "Admin I18n",
  userEmail: "admin@example.com",
  userPhone: null,
  userAvatar: null,
  accountStatus: true,
  roles: ["ADMIN"],
};

function pageResponse<T>(content: T[]) {
  return {
    content,
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
    number: 0,
    size: 20,
    first: true,
    last: true,
  };
}

async function fulfillJson(route: Route, data: unknown, status = 200) {
  await route.fulfill({
    status,
    contentType: "application/json",
    headers: {
      "access-control-allow-origin": "http://localhost:3636",
      "access-control-allow-credentials": "true",
    },
    body: JSON.stringify(status >= 400 ? data : { data }),
  });
}

async function mockLoggedOut(page: Page, rawLoginFailure?: string) {
  await page.route(API_PATTERN, async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    if (path === "/api/auth/me") {
      await fulfillJson(route, { message: "Unauthenticated" }, 401);
      return;
    }
    if (path === "/api/auth/login" && rawLoginFailure) {
      await fulfillJson(route, { message: rawLoginFailure }, 500);
      return;
    }
    await fulfillJson(route, { message: `Unexpected mocked request: ${path}` }, 404);
  });
}

type AssistantCapture = {
  requestBody: Record<string, unknown> | null;
  executeRequests: number;
};

async function mockLoggedIn(page: Page, capture: AssistantCapture) {
  await page.context().addCookies([
    {
      name: "access_token",
      value: "mock-admin-token",
      domain: "localhost",
      path: "/",
    },
  ]);
  const conversation = {
    id: "conversation-i18n",
    adminUserId: ADMIN_USER.userId,
    title: "Audit conversation",
    createdAt: "2026-08-14T01:00:00.000Z",
    updatedAt: "2026-08-14T02:00:00.000Z",
  };
  const existingMessages = [
    {
      id: "message-user-existing",
      conversationId: conversation.id,
      role: "USER",
      content: "Nội dung người dùng phải được giữ nguyên",
      metadata: null,
      createdAt: "2026-08-14T01:10:00.000Z",
    },
    {
      id: "message-assistant-existing",
      conversationId: conversation.id,
      role: "ASSISTANT",
      content: "Stored AI answer must remain exactly in English.",
      metadata: null,
      createdAt: "2026-08-14T01:11:00.000Z",
    },
  ];

  await page.route(API_PATTERN, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;

    if (path === "/api/auth/me") {
      await fulfillJson(route, { user: ADMIN_USER });
      return;
    }
    if (path === "/api/admin/dashboard/summary") {
      await fulfillJson(route, {
        totalUsers: 1_234_567,
        activeUsers: 1_200_000,
        totalCafePages: 2_345,
        activeCafePages: 2_000,
        totalBlogs: 45_678,
        publishedBlogs: 40_000,
        hiddenBlogs: 1_234,
        totalComments: 987_654,
        totalPayments: 320,
        paidPayments: 300,
        pendingPayments: 15,
        failedPayments: 5,
        totalReviewers: 789,
        pendingModerationItems: 12,
      });
      return;
    }
    if (path === "/api/admin/analytics/revenue") {
      await fulfillJson(route, {
        rangeDays: 30,
        currency: "VND",
        totalAmount: 987_654_321,
        reviewerRegistrationAmount: 100_000_000,
        cafePageOpeningAmount: 700_000_000,
        advertiseAmount: 187_654_321,
        daily: [
          {
            date: "2026-08-14",
            totalAmount: 12_345_678,
            reviewerRegistrationAmount: 1_000_000,
            cafePageOpeningAmount: 10_000_000,
            advertiseAmount: 1_345_678,
          },
        ],
      });
      return;
    }
    if (path === "/api/admin/analytics/regions") {
      await fulfillJson(route, [
        {
          provinceCode: "79",
          provinceName: "Thành phố Hồ Chí Minh",
          userCount: 123_456,
          cafePageCount: 456,
          reviewerCount: 78,
        },
      ]);
      return;
    }
    if (path === "/api/admin/assistant/conversations" && request.method() === "GET") {
      await fulfillJson(route, pageResponse([conversation]));
      return;
    }
    if (path === "/api/admin/assistant/conversations" && request.method() === "POST") {
      await fulfillJson(route, conversation);
      return;
    }
    if (
      path === `/api/admin/assistant/conversations/${conversation.id}/messages` &&
      request.method() === "GET"
    ) {
      await fulfillJson(route, pageResponse([...existingMessages].reverse()));
      return;
    }
    if (
      path === `/api/admin/assistant/conversations/${conversation.id}/messages/stream` &&
      request.method() === "POST"
    ) {
      capture.requestBody = request.postDataJSON() as Record<string, unknown>;
      const chatResponse = {
        message: {
          id: "message-assistant-stream",
          conversationId: conversation.id,
          role: "ASSISTANT",
          content: "Mock AI answer stays in English after the Vietnamese UI switch.",
          metadata: { citations: [], toolCalls: [] },
          createdAt: "2026-08-14T02:10:00.000Z",
        },
        draftAction: {
          id: "draft-i18n",
          conversationId: conversation.id,
          messageId: "message-assistant-stream",
          actionType: "REPORT_RESOLVE",
          payload: { reportId: "report-i18n" },
          explanation: "Raw draft explanation stays unchanged.",
          sourceRefs: [],
          status: "PENDING",
          expiresAt: "2026-08-15T02:10:00.000Z",
          executedAt: null,
          executionResult: null,
          errorMessage: null,
          createdAt: "2026-08-14T02:10:00.000Z",
          updatedAt: null,
        },
        citations: [],
        toolCalls: [],
      };
      await route.fulfill({
        status: 200,
        contentType: "text/event-stream",
        body: `event: message\ndata: ${JSON.stringify(chatResponse)}\n\nevent: done\ndata: {}\n\n`,
      });
      return;
    }
    if (path.endsWith("/execute") && request.method() === "POST") {
      capture.executeRequests += 1;
      await fulfillJson(route, { message: "Execution must not be called automatically" }, 500);
      return;
    }

    await fulfillJson(route, { message: `Unexpected mocked request: ${path}` }, 404);
  });
}

test.describe("system locale and login selector", () => {
  test.use({ locale: "vi-VN" });

  test("uses Vietnamese by default, switches immediately, persists, and follows System when selected", async ({ page }) => {
    await mockLoggedOut(page);
    await page.goto("/login");

    await expect(page.locator("html")).toHaveAttribute("lang", "vi-VN");
    await expect(page).toHaveTitle("Quản trị CafeStory");
    await expect(page.getByRole("heading", { name: "Đăng nhập" })).toBeVisible();
    await expect(page.getByRole("radio", { name: "Tiếng Việt" })).toHaveAttribute(
      "aria-checked",
      "true",
    );

    await page.getByRole("radio", { name: "Tiếng Anh" }).click();
    await expect(page.locator("html")).toHaveAttribute("lang", "en-US");
    await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();
    await page.reload();
    await expect(page).toHaveTitle("CafeStory Admin");
    await expect(page.getByRole("radio", { name: "English" })).toHaveAttribute(
      "aria-checked",
      "true",
    );

    const englishRadio = page.getByRole("radio", { name: "English" });
    await englishRadio.focus();
    await englishRadio.press("ArrowRight");
    const vietnameseRadio = page.getByRole("radio", { name: "Tiếng Việt" });
    await expect(vietnameseRadio).toHaveAttribute("aria-checked", "true");
    await expect(vietnameseRadio).toBeFocused();
    await page.getByRole("radio", { name: "Tiếng Anh" }).click();

    await page.evaluate(() => {
      Object.defineProperty(navigator, "languages", {
        configurable: true,
        value: ["vi-VN"],
      });
      window.dispatchEvent(new Event("languagechange"));
    });
    await expect(page.locator("html")).toHaveAttribute("lang", "en-US");

    await page.getByRole("radio", { name: "System" }).click();
    await expect(page.locator("html")).toHaveAttribute("lang", "vi-VN");
    await expect(page.getByRole("heading", { name: "Đăng nhập" })).toBeVisible();

    await page.setViewportSize({ width: 390, height: 844 });
    const selector = await page.getByRole("radiogroup", { name: "Ngôn ngữ" }).boundingBox();
    expect(selector).not.toBeNull();
    expect((selector?.x ?? 0) + (selector?.width ?? 0)).toBeLessThanOrEqual(390);

    await page.evaluate(() => {
      window.localStorage.setItem("cafestory-admin-locale-preference", "en");
      document.cookie = "cafestory-admin-locale-preference=; path=/; max-age=0";
    });
    await page.reload();
    await expect(page.locator("html")).toHaveAttribute("lang", "en-US");
    await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();
  });

  test("does not expose a raw English backend error in Vietnamese", async ({ page }) => {
    const rawMessage = "RAW ENGLISH BACKEND FAILURE MUST NOT LEAK";
    await mockLoggedOut(page, rawMessage);
    await page.goto("/login");
    await page.getByLabel("Email hoặc tên đăng nhập").fill("admin@example.com");
    await page.getByLabel("Mật khẩu").fill("invalid");
    await page.getByRole("button", { name: "Đăng nhập" }).click();
    await expect(page.getByText("Máy chủ hiện không khả dụng. Vui lòng thử lại sau.")).toBeVisible();
    await expect(page.getByText(rawMessage)).toHaveCount(0);
  });
});

test.describe("Vietnamese fresh default on an English browser", () => {
  test.use({ locale: "en-US" });

  test("uses Vietnamese on a fresh non-Vietnamese browser", async ({ page }) => {
    await mockLoggedOut(page);
    await page.goto("/login");
    await expect(page.locator("html")).toHaveAttribute("lang", "vi-VN");
    await expect(page.getByRole("heading", { name: "Đăng nhập" })).toBeVisible();
    await expect(page.getByRole("radio", { name: "Tiếng Việt" })).toHaveAttribute(
      "aria-checked",
      "true",
    );
  });

  test("translates dashboard and assistant chrome while preserving AI data and request shape", async ({ page }) => {
    const capture: AssistantCapture = { requestBody: null, executeRequests: 0 };
    await mockLoggedIn(page, capture);
    await page.goto("/");

    await expect(page.getByRole("heading", { name: "Tổng quan" })).toBeVisible();
    await expect(page.getByText(formatNumber(1_234_567, "vi-VN"), { exact: true })).toBeVisible();
    await page.getByRole("radio", { name: "Tiếng Anh" }).click();
    await expect(page.getByRole("heading", { name: "Overview" })).toBeVisible();
    await expect(page.getByText(formatNumber(1_234_567, "en-US"), { exact: true })).toBeVisible();
    await page.getByRole("radio", { name: "Vietnamese" }).click();
    await expect(page.getByRole("heading", { name: "Tổng quan" })).toBeVisible();
    await expect(page.getByRole("link", { name: "Bài viết", exact: true })).toBeVisible();
    await expect(page.getByText(formatNumber(1_234_567, "vi-VN"), { exact: true })).toBeVisible();

    await page.reload();
    await expect(page).toHaveTitle("Quản trị CafeStory");
    await page.getByRole("button", { name: "Trợ lý" }).click();
    await expect(page.getByRole("heading", { name: "Trợ lý quản trị" })).toBeVisible();
    await expect(page.getByText("Stored AI answer must remain exactly in English.")).toBeVisible();
    await expect(page.getByText("Nội dung người dùng phải được giữ nguyên")).toBeVisible();

    const prompt = "Đây là prompt tiếng Việt cần giữ nguyên";
    await page.getByLabel("Nhập câu hỏi cho trợ lý").fill(prompt);
    await page.getByRole("button", { name: "Gửi tin nhắn cho trợ lý" }).click();
    await expect(
      page.getByText("Mock AI answer stays in English after the Vietnamese UI switch."),
    ).toBeVisible();
    await expect(page.getByText("Raw draft explanation stays unchanged.")).toBeVisible();
    await expect(page.getByText("Đây chỉ là bản nháp. Quản trị viên phải chọn Thực thi để áp dụng thao tác.")).toBeVisible();

    expect(capture.requestBody).toEqual({
      message: prompt,
      pageContext: { route: "/", query: "" },
    });
    expect(Object.keys(capture.requestBody ?? {}).sort()).toEqual(["message", "pageContext"]);
    expect(capture.executeRequests).toBe(0);

    await page.setViewportSize({ width: 390, height: 844 });
    await page.getByRole("button", { name: "Đóng trợ lý" }).click();
    await page.getByRole("button", { name: "Mở điều hướng" }).click();
    const selector = await page.getByRole("radiogroup", { name: "Ngôn ngữ" }).boundingBox();
    expect(selector).not.toBeNull();
    expect((selector?.x ?? 0) + (selector?.width ?? 0)).toBeLessThanOrEqual(390);
  });
});

test("pure formatters honor locale tags and safe fallbacks", () => {
  const instant = "2026-08-14T08:15:00.000Z";
  for (const localeTag of ["en-US", "vi-VN"] as const) {
    expect(formatDate(instant, localeTag)).toBe(
      new Intl.DateTimeFormat(localeTag, { dateStyle: "medium" }).format(new Date(instant)),
    );
    expect(formatTime(instant, localeTag)).toBe(
      new Intl.DateTimeFormat(localeTag, { hour: "2-digit", minute: "2-digit" }).format(
        new Date(instant),
      ),
    );
    expect(formatNumber(1_234_567.89, localeTag)).toBe(
      new Intl.NumberFormat(localeTag).format(1_234_567.89),
    );
    expect(formatCompactNumber(1_234_567, localeTag)).toBe(
      new Intl.NumberFormat(localeTag, {
        notation: "compact",
        maximumFractionDigits: 1,
      }).format(1_234_567),
    );
    expect(formatCurrency(1_234_567, "VND", localeTag)).toBe(
      new Intl.NumberFormat(localeTag, {
        style: "currency",
        currency: "VND",
        maximumFractionDigits: 0,
      }).format(1_234_567),
    );
  }
  expect(formatDate("not-a-date", "en-US")).toBe("—");
});
