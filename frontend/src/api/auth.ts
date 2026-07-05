import { authAdapter } from './adapters/current'
import type {
  CurrentUser,
  LoginPayload,
  LoginResult,
  MailStatus,
  RegisterPayload,
  RegisterResult,
  RegistrationSettings,
  ResetPasswordPayload,
  VerificationCodePayload,
  VerificationCodeResult,
} from './types'

export function fetchRegistrationSettings(): Promise<RegistrationSettings> {
  return authAdapter.fetchRegistrationSettings()
}

export function fetchMailStatus(): Promise<MailStatus> {
  return authAdapter.fetchMailStatus()
}

export function login(payload: LoginPayload): Promise<LoginResult> {
  return authAdapter.login(payload)
}

export function sendVerificationCode(payload: VerificationCodePayload): Promise<VerificationCodeResult> {
  return authAdapter.sendVerificationCode(payload)
}

export function register(payload: RegisterPayload): Promise<RegisterResult> {
  return authAdapter.register(payload)
}

export function sendPasswordResetCode(email: string): Promise<VerificationCodeResult> {
  return authAdapter.sendPasswordResetCode(email)
}

export function resetPassword(payload: ResetPasswordPayload): Promise<void> {
  return authAdapter.resetPassword(payload)
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
