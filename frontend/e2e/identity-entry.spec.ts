import { expect, type APIRequestContext, type Page, test } from '@playwright/test'

test.describe('身份入口', () => {
  test('邮箱注册、登录和找回密码走通真实后端链路', async ({ page }) => {
    const suffix = Date.now()
    const email = `student${suffix}@example.com`
    const username = `student${suffix}`
    const initialPassword = 'abcdef'
    const resetPassword = 'newpass1'

    await page.goto('/login', { waitUntil: 'domcontentloaded' })
    await expect(page.getByRole('tab', { name: '登录' })).toBeVisible()
    await expect(page.getByRole('tab', { name: '注册' })).toBeVisible()
    await expect(page.getByRole('tab', { name: '找回密码' })).toBeVisible()

    await registerFromLoginPage(page, {
      email,
      username,
      displayName: '浏览器注册考生',
      password: initialPassword,
      expectedMessage: '注册成功，请登录',
    })

    await loginFromLoginPage(page, username, initialPassword)
    await expect(page.getByRole('menuitem', { name: '考试中心' })).toBeVisible()

    await clearSession(page)
    await resetPasswordFromLoginPage(page, email, resetPassword)
    await loginFromLoginPage(page, username, resetPassword)
    await expect(page.getByRole('menuitem', { name: '考试中心' })).toBeVisible()
  })

  test('管理员审核注册申请后待审账号才能登录', async ({ page, request }) => {
    const token = await adminToken(request)
    const suffix = Date.now()
    const email = `pending${suffix}@example.com`
    const username = `pending${suffix}`
    const password = 'abcdef'

    try {
      await configureRegistration(request, token, true)
      await page.goto('/login', { waitUntil: 'domcontentloaded' })
      await registerFromLoginPage(page, {
        email,
        username,
        displayName: '待审核考生',
        password,
        expectedMessage: '注册成功，等待管理员审核',
      })

      await submitLogin(page, username, password, false)
      await expect(page).toHaveURL(/\/login/)

      await loginFromLoginPage(page, 'admin', 'password')
      await page.goto('/sys/settings', { waitUntil: 'domcontentloaded' })
      await expect(page.getByRole('heading', { name: '平台设置' })).toBeVisible()
      const requestRow = page.getByRole('row').filter({ hasText: email })
      await expect(requestRow).toBeVisible()
      await requestRow.getByRole('button', { name: '通过' }).click()
      await expect(page.getByText('注册申请已通过')).toBeVisible()

      await clearSession(page)
      await loginFromLoginPage(page, username, password)
      await expect(page.getByRole('menuitem', { name: '考试中心' })).toBeVisible()
    } finally {
      await configureRegistration(request, token, false)
    }
  })
})

async function registerFromLoginPage(
  page: Page,
  payload: {
    email: string
    username: string
    displayName: string
    password: string
    expectedMessage: string
  },
) {
  await page.getByRole('tab', { name: '注册' }).click()
  const form = page.locator('#pane-register form')
  await expect(form).toBeVisible()
  await form.getByLabel('邮箱', { exact: true }).fill(payload.email)
  await form.getByRole('button', { name: '发送验证码' }).click()
  const verificationCode = await readDebugCode(form)
  await form.getByLabel('账号', { exact: true }).fill(payload.username)
  await form.getByLabel('姓名', { exact: true }).fill(payload.displayName)
  await form.getByLabel('验证码', { exact: true }).fill(verificationCode)
  await form.getByLabel('密码', { exact: true }).fill(payload.password)
  await form.getByLabel('确认密码', { exact: true }).fill(payload.password)
  await form.getByRole('button', { name: '提交注册' }).click()
  await expect(page.getByText(payload.expectedMessage)).toBeVisible()
}

async function resetPasswordFromLoginPage(page: Page, email: string, newPassword: string) {
  await page.goto('/login', { waitUntil: 'domcontentloaded' })
  await page.getByRole('tab', { name: '找回密码' }).click()
  const form = page.locator('#pane-forgot form')
  await expect(form).toBeVisible()
  await form.getByLabel('邮箱', { exact: true }).fill(email)
  await form.getByRole('button', { name: '发送验证码' }).click()
  const code = await readDebugCode(form)
  await form.getByLabel('验证码', { exact: true }).fill(code)
  await form.getByLabel('新密码', { exact: true }).fill(newPassword)
  await form.getByLabel('确认新密码', { exact: true }).fill(newPassword)
  await form.getByRole('button', { name: '重置密码' }).click()
  await expect(page.getByText('密码已重置，请使用新密码登录')).toBeVisible()
}

async function loginFromLoginPage(page: Page, username: string, password: string) {
  await page.goto('/login', { waitUntil: 'domcontentloaded' })
  await submitLogin(page, username, password, true)
  await expect(page).toHaveURL(/\/dashboard/)
}

async function submitLogin(page: Page, username: string, password: string, expectOk: boolean) {
  const form = page.locator('#pane-login form')
  await expect(form.getByLabel('账号', { exact: true })).toBeVisible()
  await form.getByLabel('账号', { exact: true }).fill(username)
  await form.getByLabel('密码', { exact: true }).fill(password)
  const loginResponse = page.waitForResponse((response) => {
    return response.url().includes('/api/auth/login') && response.request().method() === 'POST'
  })
  await form.getByRole('button', { name: '进入平台' }).click()
  await expect((await loginResponse).ok()).toBe(expectOk)
}

async function readDebugCode(form: ReturnType<Page['locator']>) {
  const hint = form.locator('.auth-hint')
  await expect(hint).toContainText(/演示验证码：\d{6}/)
  const text = await hint.textContent()
  const code = text?.match(/\d{6}/)?.[0]
  expect(code).toBeTruthy()
  return code as string
}

async function clearSession(page: Page) {
  await page.evaluate(() => {
    window.localStorage.clear()
    window.sessionStorage.clear()
  })
}

async function adminToken(request: APIRequestContext) {
  const response = await request.post('/api/auth/login', {
    data: { username: 'admin', password: 'password' },
  })
  expect(response.ok()).toBeTruthy()
  const body = await response.json()
  return body.data.accessToken as string
}

async function configureRegistration(request: APIRequestContext, token: string, adminApprovalRequired: boolean) {
  const response = await request.put('/api/admin/auth/registration-settings', {
    headers: { Authorization: `Bearer ${token}` },
    data: {
      selfRegistrationEnabled: true,
      emailVerificationRequired: true,
      adminApprovalRequired,
      defaultRoleCode: 'STUDENT',
      defaultDepartmentId: 2,
      allowedEmailDomains: [],
      termsText: '',
    },
  })
  expect(response.ok()).toBeTruthy()
}
