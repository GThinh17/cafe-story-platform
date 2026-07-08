import { defineConfig, devices } from "@playwright/test";

const baseURL = process.env.ADMIN_TEST_BASE_URL ?? "http://localhost:3636";

export default defineConfig({
  testDir: "./tests/e2e",
  timeout: 30 * 60_000,
  expect: {
    timeout: 15_000,
  },
  fullyParallel: false,
  retries: 0,
  reporter: [
    ["list"],
    ["html", { open: "never", outputFolder: ".playwright-report" }],
  ],
  outputDir: ".playwright-results",
  use: {
    baseURL,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
        viewport: { width: 1440, height: 1000 },
      },
    },
  ],
});
