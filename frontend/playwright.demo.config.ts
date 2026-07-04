import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e-demo',
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  webServer: {
    command: 'npm run preview:demo',
    url: 'http://127.0.0.1:4174/kaoshi/login',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
  use: {
    baseURL: 'http://127.0.0.1:4174',
    headless: true,
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
