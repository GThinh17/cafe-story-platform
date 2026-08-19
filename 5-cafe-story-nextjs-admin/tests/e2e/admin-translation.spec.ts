import { expect, test, type Page, type Route } from "@playwright/test";

const API_PATTERN = "http://localhost:8080/**";
const original =
  "Report More Close https://safe.example ACTIVE CSR.INT.003 0f4d1f50-8db1-4ad9-8e58-0f66ae7fdf1a";
const translated =
  "Báo cáo Xem thêm Đóng https://safe.example ACTIVE CSR.INT.003 0f4d1f50-8db1-4ad9-8e58-0f66ae7fdf1a";

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

async function mockAdmin(page: Page, capture: { calls: number; body: unknown }) {
  await page.context().addCookies([
    { name: "access_token", value: "mock-admin-token", domain: "localhost", path: "/" },
  ]);

  await page.route(API_PATTERN, async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    if (path === "/api/auth/me") {
      await fulfillJson(route, {
        user: {
          userId: "admin-translation",
          userName: "admin.translation",
          userFullName: "Admin Translation",
          userEmail: "admin@example.test",
          userPhone: null,
          userAvatar: null,
          accountStatus: true,
          roles: ["ADMIN"],
        },
      });
      return;
    }
    if (path === "/api/admin/comments" && request.method() === "GET") {
      await fulfillJson(route, {
        content: [
          {
            id: "comment-translation",
            blogId: "blog-translation",
            userId: "foreign-user",
            authorUserName: "foreign.user",
            authorUserAvatar: null,
            parentCommentId: null,
            content: original,
            imageUrls: [],
            status: "PUBLISHED",
            createdAt: "2026-08-17T10:00:00.000Z",
            updatedAt: null,
          },
        ],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
        first: true,
        last: true,
      });
      return;
    }
    if (path === "/api/admin/translations" && request.method() === "POST") {
      capture.calls += 1;
      capture.body = request.postDataJSON();
      if (capture.calls === 1) {
        await fulfillJson(route, { message: "provider unavailable" }, 502);
        return;
      }
      await fulfillJson(route, {
        requestId: "f3fa9364-1cf6-4cc1-a7bc-056f46bf2976",
        detectedLocale: "en",
        targetLocale: "vi",
        translatedText: translated,
        translationState: "TRANSLATED",
        modelName: "gpt-4o-mini",
      });
      return;
    }
    await fulfillJson(route, { message: `Unexpected mocked request: ${path}` }, 404);
  });
}

test("translation is opt-in, retries safely, toggles original, and preserves identifiers", async ({
  page,
}) => {
  const capture = { calls: 0, body: null as unknown };
  await mockAdmin(page, capture);
  await page.goto("/comments");

  await expect(page.getByText(original, { exact: true })).toBeVisible();
  expect(capture.calls).toBe(0);

  await page.getByRole("button", { name: "Dịch" }).first().click();
  await expect(
    page.getByText("Dịch thất bại. Nội dung gốc vẫn được giữ nguyên."),
  ).toBeVisible();
  await expect(page.getByText(original, { exact: true })).toBeVisible();
  expect(capture.calls).toBe(1);

  await page.getByRole("button", { name: "Thử dịch lại" }).click();
  await expect(page.getByText(translated, { exact: true })).toBeVisible();
  expect(capture.calls).toBe(2);
  expect(capture.body).toEqual({
    text: original,
    targetLocale: "vi",
    contentKind: "COMMENT_CONTENT",
  });

  await page.getByRole("button", { name: "Xem bản gốc" }).click();
  await expect(page.getByText(original, { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Xem bản dịch" }).click();
  await expect(page.getByText(translated, { exact: true })).toBeVisible();
  await expect(page.getByText("https://safe.example", { exact: false })).toBeVisible();
  await expect(page.getByText("CSR.INT.003", { exact: false })).toBeVisible();
});
