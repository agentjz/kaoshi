import { apiClient } from '../../client'
import type { AuthAdapter } from '../auth-adapter'
import type {
  ApiResponse,
  CurrentUser,
  LoginResult,
  MailStatus,
  RegisterResult,
  RegistrationSettings,
  VerificationCodeResult,
} from '../../types'

export const realAuthAdapter: AuthAdapter = {
  async fetchRegistrationSettings() {
    const response = await apiClient.get<ApiResponse<RegistrationSettings>>('/api/auth/registration-settings')
    return response.data.data
  },
  async fetchMailStatus() {
    const response = await apiClient.get<ApiResponse<MailStatus>>('/api/auth/mail-status')
    return response.data.data
  },
  async login(payload) {
    const response = await apiClient.post<ApiResponse<LoginResult>>('/api/auth/login', payload)
    return response.data.data
  },
  async sendVerificationCode(payload) {
    const response = await apiClient.post<ApiResponse<VerificationCodeResult>>('/api/auth/verification-codes', payload)
    return response.data.data
  },
  async register(payload) {
    const response = await apiClient.post<ApiResponse<RegisterResult>>('/api/auth/register', payload)
    return response.data.data
  },
  async sendPasswordResetCode(email) {
    const response = await apiClient.post<ApiResponse<VerificationCodeResult>>('/api/auth/password-reset-codes', { email })
    return response.data.data
  },
  async resetPassword(payload) {
    await apiClient.post<ApiResponse<void>>('/api/auth/reset-password', payload)
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
