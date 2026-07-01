import { expect, test, type Page } from '@playwright/test'

test('真实环境：管理员登录、创建角色并保存到后端', async ({ page }) => {
  await login(page)

  await page.getByRole('menuitem', { name: '角色管理' }).click()
  await expect(page.getByRole('heading', { name: '角色管理' })).toBeVisible()
  await page.getByRole('button', { name: '新建角色' }).click()

  await expect(page.getByRole('dialog', { name: '新建角色' })).toBeVisible()
  await page.getByLabel('角色名称').fill('浏览器验收员')
  await page.getByLabel('角色编码').fill(`E2E_AUDITOR_${Date.now()}`)
  await page.getByLabel('说明').fill('Playwright 真实端到端测试创建')
  await page.getByRole('button', { name: '全选' }).first().click()
  await page.getByRole('button', { name: '全选' }).nth(1).click()
  await page.getByRole('button', { name: '保存' }).click()

  await expect(page.getByText('角色已创建')).toBeVisible()
  await expect(page.getByText('浏览器验收员')).toBeVisible()
})

test('真实环境：打开管理页面并读取后端种子数据', async ({ page }) => {
  await login(page)

  await page.getByRole('menuitem', { name: '权限清单' }).click()
  await expect(page.getByRole('heading', { name: '权限清单' })).toBeVisible()
  await expect(page.getByRole('main').getByText('系统管理', { exact: true })).toBeVisible()

  await page.getByRole('menuitem', { name: '菜单清单' }).click()
  await expect(page.getByRole('heading', { name: '菜单清单' })).toBeVisible()
  await expect(page.getByRole('main').getByText('题库管理', { exact: true })).toBeVisible()
})

test('真实环境：考生开始考试、渲染附件并提交成绩', async ({ page }) => {
  await login(page)

  await page.getByRole('menuitem', { name: '考试端' }).click()
  await expect(page.getByRole('heading', { name: '考试中心' })).toBeVisible()
  await page.getByRole('button', { name: '开始作答' }).click()

  await expect(page.getByRole('heading', { name: '英语基础模拟考试' })).toBeVisible()
  await page.getByRole('button', { name: '开始考试' }).click()
  await expect(page.getByText('答题卡')).toBeVisible()
  await expect(page.locator('audio.question-media__audio').first()).toBeVisible()

  await page.getByText('B. He goes to school every day.').click()
  await page.getByText('A. book').click()
  await page.getByText('C. teacher').click()
  await page.getByText('A. improve').click()
  await page.getByText('A. The learner practiced reading.').click()
  await page.getByText('B. The learner practiced listening.').click()
  await page.getByRole('button', { name: '提交试卷' }).click()
  await page.getByRole('button', { name: '提交', exact: true }).click()

  await expect(page.getByRole('heading', { name: '提交结果' })).toBeVisible()
  await expect(page.getByRole('article').filter({ hasText: '得分' }).getByRole('strong')).toHaveText('20')
  await expect(page.getByRole('article').filter({ hasText: '总分' }).getByRole('strong')).toHaveText('20')
})

async function login(page: Page) {
  await page.goto('/login')
  await page.getByLabel('账号').fill('admin')
  await page.getByLabel('密码').fill('password')
  const loginResponse = page.waitForResponse((response) => {
    return response.url().includes('/api/auth/login') && response.request().method() === 'POST'
  })
  await page.getByRole('button', { name: '进入平台' }).click()
  await expect((await loginResponse).ok()).toBe(true)
  await expect(page).toHaveURL(/\/admin/)
  await expect(page.getByRole('menuitem', { name: '角色管理' })).toBeVisible()
  await expect
    .poll(() => page.evaluate(() => window.localStorage.getItem('kaoshi.accessToken')))
    .toBeTruthy()
}
