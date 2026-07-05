import { describe, expect, it, vi } from 'vitest'

import {
  fetchMailStatus,
  fetchRegistrationSettings,
  register,
  resetPassword,
  sendPasswordResetCode,
  sendVerificationCode,
} from './auth'
import { apiClient } from './client'

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

const ok = <T>(data: T) => ({ data: { code: 0, message: 'OK', data, timestamp: '2026-07-01T00:00:00Z' } })

describe('auth api', () => {
  it('uses public registration and password reset endpoints', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok({}))
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))

    await fetchRegistrationSettings()
    await fetchMailStatus()
    await sendVerificationCode({ email: 'student@example.com', purpose: 'REGISTER' })
    await register({
      email: 'student@example.com',
      username: 'student',
      displayName: '学生',
      password: 'abcdef',
      confirmPassword: 'abcdef',
      verificationCode: '123456',
    })
    await sendPasswordResetCode('student@example.com')
    await resetPassword({
      email: 'student@example.com',
      code: '123456',
      newPassword: 'newpass1',
      confirmPassword: 'newpass1',
    })

    expect(apiClient.get).toHaveBeenCalledWith('/api/auth/registration-settings')
    expect(apiClient.get).toHaveBeenCalledWith('/api/auth/mail-status')
    expect(apiClient.post).toHaveBeenCalledWith('/api/auth/verification-codes', { email: 'student@example.com', purpose: 'REGISTER' })
    expect(apiClient.post).toHaveBeenCalledWith('/api/auth/register', {
      email: 'student@example.com',
      username: 'student',
      displayName: '学生',
      password: 'abcdef',
      confirmPassword: 'abcdef',
      verificationCode: '123456',
    })
    expect(apiClient.post).toHaveBeenCalledWith('/api/auth/password-reset-codes', { email: 'student@example.com' })
    expect(apiClient.post).toHaveBeenCalledWith('/api/auth/reset-password', {
      email: 'student@example.com',
      code: '123456',
      newPassword: 'newpass1',
      confirmPassword: 'newpass1',
    })
  })
})
