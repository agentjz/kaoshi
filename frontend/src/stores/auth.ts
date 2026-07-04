import { defineStore } from 'pinia'

import { changePassword, fetchCurrentUser, login, logout } from '@/api/auth'
import type { CurrentUser, LoginPayload } from '@/api/types'
import { sessionStorageAdapter } from '@/runtime/session-storage'

interface AuthState {
  token: string
  user: CurrentUser | null
  loading: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: sessionStorageAdapter.readToken(),
    user: sessionStorageAdapter.readUser(),
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    displayName: (state) => state.user?.displayName || state.user?.username || '',
    permissions: (state) => state.user?.permissions || [],
  },
  actions: {
    async login(payload: LoginPayload) {
      this.loading = true
      try {
        const result = await login(payload)
        this.token = result.accessToken
        this.user = result.user
        sessionStorageAdapter.writeSession(result.accessToken, result.user)
      } finally {
        this.loading = false
      }
    },
    async loadCurrentUser() {
      if (!this.token) {
        return
      }
      this.user = await fetchCurrentUser()
      sessionStorageAdapter.writeUser(this.user)
    },
    async changePassword(payload: { currentPassword: string; newPassword: string; confirmPassword: string }) {
      await changePassword(payload)
      await this.loadCurrentUser()
    },
    async logout() {
      if (this.token) {
        await logout().catch(() => undefined)
      }
      this.clearSession()
    },
    clearSession() {
      this.token = ''
      this.user = null
      sessionStorageAdapter.clear()
    },
  },
})
