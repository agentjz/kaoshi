import type { CurrentUser, LoginPayload, LoginResult } from '../types'

export interface AuthAdapter {
  login(payload: LoginPayload): Promise<LoginResult>
  fetchCurrentUser(): Promise<CurrentUser>
  logout(): Promise<void>
  changePassword(payload: { currentPassword: string; newPassword: string; confirmPassword: string }): Promise<void>
}
