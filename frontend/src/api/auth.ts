import { authAdapter } from './adapters/current'
import type { CurrentUser, LoginPayload, LoginResult } from './types'

export function login(payload: LoginPayload): Promise<LoginResult> {
  return authAdapter.login(payload)
}

export function fetchCurrentUser(): Promise<CurrentUser> {
  return authAdapter.fetchCurrentUser()
}

export function logout(): Promise<void> {
  return authAdapter.logout()
}

export function changePassword(payload: {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}): Promise<void> {
  return authAdapter.changePassword(payload)
}
