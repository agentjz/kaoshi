import { apiClient } from '../../client'
import type { AuthAdapter } from '../auth-adapter'
import type { ApiResponse, CurrentUser, LoginResult } from '../../types'

export const realAuthAdapter: AuthAdapter = {
  async login(payload) {
    const response = await apiClient.post<ApiResponse<LoginResult>>('/api/auth/login', payload)
    return response.data.data
  },
  async fetchCurrentUser() {
    const response = await apiClient.get<ApiResponse<CurrentUser>>('/api/auth/me')
    return response.data.data
  },
  async logout() {
    await apiClient.post<ApiResponse<void>>('/api/auth/logout')
  },
  async changePassword(payload) {
    await apiClient.post<ApiResponse<void>>('/api/auth/change-password', payload)
  },
}
