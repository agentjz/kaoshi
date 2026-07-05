export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface CurrentUser {
  id: number
  username: string
  displayName: string
  mustChangePassword: boolean
  roles: string[]
  permissions: string[]
}

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  tokenType: 'Bearer'
  expiresInSeconds: number
  user: CurrentUser
}

export interface RegistrationSettings {
  selfRegistrationEnabled: boolean
  emailVerificationRequired: boolean
  adminApprovalRequired: boolean
  defaultRoleCode: string
  defaultDepartmentId: number | null
  allowedEmailDomains: string[]
  termsText: string
}

export interface MailStatus {
  enabled: boolean
  configured: boolean
  deliveryMode: 'SMTP' | 'LOG'
  from: string | null
  host: string | null
  port: number | null
  message: string
}

export interface VerificationCodePayload {
  email: string
  purpose: 'REGISTER' | 'RESET_PASSWORD'
}

export interface VerificationCodeResult {
  email: string
  purpose: VerificationCodePayload['purpose']
  expiresAt: string
  debugCode: string | null
}

export interface RegisterPayload {
  email: string
  username: string
  displayName: string
  password: string
  confirmPassword: string
  verificationCode: string
}

export interface RegisterResult {
  userId: number
  username: string
  email: string
  emailVerified: boolean
  approvalStatus: 'APPROVED' | 'PENDING' | 'REJECTED'
  message: string
}

export interface ResetPasswordPayload {
  email: string
  code: string
  newPassword: string
  confirmPassword: string
}
