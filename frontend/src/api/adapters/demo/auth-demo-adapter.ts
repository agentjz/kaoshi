import type { AuthAdapter } from '../auth-adapter'
import { buildCurrentUser, currentDemoState } from './demo-store'

export const demoAuthAdapter: AuthAdapter = {
  async login(payload) {
    const state = currentDemoState()
    const user = state.users.find((item) => item.username === payload.username && item.password === payload.password && item.status === 'ACTIVE')
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
