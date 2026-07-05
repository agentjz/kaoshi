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
} from '../types'

export interface AuthAdapter {
  fetchRegistrationSettings(): Promise<RegistrationSettings>
  fetchMailStatus(): Promise<MailStatus>
  login(payload: LoginPayload): Promise<LoginResult>
  sendVerificationCode(payload: VerificationCodePayload): Promise<VerificationCodeResult>
  register(payload: RegisterPayload): Promise<RegisterResult>
  sendPasswordResetCode(email: string): Promise<VerificationCodeResult>
  resetPassword(payload: ResetPasswordPayload): Promise<void>
  fetchCurrentUser(): Promise<CurrentUser>
  logout(): Promise<void>
  changePassword(payload: { currentPassword: string; newPassword: string; confirmPassword: string }): Promise<void>
}
