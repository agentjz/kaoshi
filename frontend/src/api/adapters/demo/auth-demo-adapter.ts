import type { AuthAdapter } from '../auth-adapter'
import { buildCurrentUser, currentDemoState, nextId, nowIso } from './demo-store'

function normalizeEmail(email: string) {
  return email.trim().toLowerCase()
}

function latestCode(email: string, purpose: 'REGISTER' | 'RESET_PASSWORD') {
  const normalized = normalizeEmail(email)
  return currentDemoState().verificationCodes
    .filter((item) => item.email === normalized && item.purpose === purpose && !item.consumed)
    .sort((a, b) => b.expiresAt.localeCompare(a.expiresAt))[0]
}

function consumeCode(email: string, purpose: 'REGISTER' | 'RESET_PASSWORD', code: string) {
  const record = latestCode(email, purpose)
  if (!record || record.expiresAt < nowIso()) {
    throw new Error('验证码无效或已过期')
  }
  if (record.code !== code) {
    throw new Error('验证码不正确')
  }
  record.consumed = true
}

export const demoAuthAdapter: AuthAdapter = {
  async fetchRegistrationSettings() {
    return structuredClone(currentDemoState().registrationSettings)
  },
  async fetchMailStatus() {
    return structuredClone(currentDemoState().mailStatus)
  },
  async login(payload) {
    const state = currentDemoState()
    const user = state.users.find((item) => item.username === payload.username && item.password === payload.password && item.status === 'ACTIVE' && item.emailVerified && item.approvalStatus === 'APPROVED')
    if (!user) {
      throw new Error('账号或密码错误')
    }
    state.currentUserId = user.id
    return {
      accessToken: `demo-token-${user.id}`,
      tokenType: 'Bearer',
      expiresInSeconds: 86400,
      user: buildCurrentUser(state, user),
    }
  },
  async sendVerificationCode(payload) {
    const state = currentDemoState()
    const email = normalizeEmail(payload.email)
    const code = '123456'
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString()
    state.verificationCodes.push({ email, purpose: payload.purpose, code, expiresAt, consumed: false })
    return { email, purpose: payload.purpose, expiresAt, debugCode: code }
  },
  async register(payload) {
    const state = currentDemoState()
    const email = normalizeEmail(payload.email)
    if (state.users.some((user) => user.username === payload.username)) {
      throw new Error('账号已存在')
    }
    if (state.users.some((user) => user.email === email)) {
      throw new Error('邮箱已注册')
    }
    consumeCode(email, 'REGISTER', payload.verificationCode)
    const timestamp = nowIso()
    const studentRole = state.roles.find((role) => role.code === state.registrationSettings.defaultRoleCode) || state.roles.find((role) => role.code === 'STUDENT')
    const user = {
      id: nextId(state),
      departmentId: state.registrationSettings.defaultDepartmentId,
      departmentName: '教学部',
      username: payload.username,
      email,
      emailVerified: true,
      registrationSource: 'SELF_REGISTERED' as const,
      approvalStatus: state.registrationSettings.adminApprovalRequired ? 'PENDING' as const : 'APPROVED' as const,
      displayName: payload.displayName,
      status: 'ACTIVE' as const,
      roles: studentRole ? [studentRole.code] : ['STUDENT'],
      roleIds: studentRole ? [studentRole.id] : [2],
      password: payload.password,
      createdAt: timestamp,
      updatedAt: timestamp,
    }
    state.users.push(user)
    return {
      userId: user.id,
      username: user.username,
      email,
      emailVerified: true,
      approvalStatus: user.approvalStatus,
      message: user.approvalStatus === 'APPROVED' ? '注册成功，请登录' : '注册成功，等待管理员审核',
    }
  },
  async sendPasswordResetCode(email) {
    return this.sendVerificationCode({ email, purpose: 'RESET_PASSWORD' })
  },
  async resetPassword(payload) {
    if (payload.newPassword !== payload.confirmPassword) {
      throw new Error('两次输入的新密码不一致')
    }
    const email = normalizeEmail(payload.email)
    const user = currentDemoState().users.find((item) => item.email === email)
    if (!user) {
      throw new Error('邮箱未绑定任何账号')
    }
    consumeCode(email, 'RESET_PASSWORD', payload.code)
    user.password = payload.newPassword
    user.updatedAt = nowIso()
  },
  async fetchCurrentUser() {
    const state = currentDemoState()
    const user = state.users.find((item) => item.id === state.currentUserId) || state.users[0]
    state.currentUserId = user.id
    return buildCurrentUser(state, user)
  },
  async logout() {
    currentDemoState().currentUserId = null
  },
  async changePassword() {
    return undefined
  },
}
