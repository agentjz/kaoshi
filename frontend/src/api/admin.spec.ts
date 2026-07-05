import { describe, expect, it, vi } from 'vitest'

import {
  changeAdminUserStatus,
  approveRegistrationRequest,
  createAdminRole,
  createAdminUser,
  createDepartment,
  createExternalIntegration,
  deleteDepartment,
  downloadUserExport,
  downloadUserImportTemplate,
  fetchExternalIntegrationEvents,
  fetchExternalIntegrations,
  fetchAdminMailStatus,
  fetchAdminMenus,
  fetchAdminPermissions,
  fetchAdminRegistrationSettings,
  fetchAdminRoles,
  fetchAdminUsers,
  fetchDepartments,
  fetchPlatformNotifications,
  fetchRegistrationRequests,
  importUsers,
  markPlatformNotificationRead,
  rejectRegistrationRequest,
  sendAdminTestMail,
  testExternalIntegration,
  updateDepartment,
  updateAdminRole,
  updateAdminRegistrationSettings,
  updateAdminUser,
  updateExternalIntegration,
} from './admin'
import { apiClient } from './client'

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

const ok = <T>(data: T) => ({ data: { code: 0, message: 'OK', data, timestamp: '2026-07-01T00:00:00Z' } })

describe('admin api', () => {
  it('requests paged users with filters', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(ok({ records: [], total: 0, page: 1, size: 20 }))

    await fetchAdminUsers({ page: 1, size: 20, keyword: 'admin' })

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/users', {
      params: { page: 1, size: 20, keyword: 'admin' },
    })
  })

  it('writes user and role management payloads through explicit endpoints', async () => {
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))
    vi.mocked(apiClient.put).mockResolvedValue(ok({}))
    vi.mocked(apiClient.patch).mockResolvedValue(ok({}))
    vi.mocked(apiClient.get).mockResolvedValue(ok({}))

    await createAdminUser({ departmentId: 1, username: 'teacher', displayName: '老师', roleIds: [2] })
    await updateAdminUser(2, { departmentId: 2, displayName: '主管', roleIds: [2, 3] })
    await changeAdminUserStatus(2, 'DISABLED')
    await downloadUserImportTemplate()
    await importUsers(new File(['xlsx'], 'users.xlsx'))
    await downloadUserExport()
    await createAdminRole({ code: 'AUDITOR', name: '审计员', description: '', permissionIds: [1], menuIds: [1] })
    await updateAdminRole(4, { code: 'AUDITOR', name: '审计管理员', description: '', permissionIds: [1], menuIds: [1, 2] })

    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/users', {
      departmentId: 1,
      username: 'teacher',
      displayName: '老师',
      roleIds: [2],
    })
    expect(apiClient.put).toHaveBeenCalledWith('/api/admin/users/2', { departmentId: 2, displayName: '主管', roleIds: [2, 3] })
    expect(apiClient.patch).toHaveBeenCalledWith('/api/admin/users/2/status', { status: 'DISABLED' })
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/users/import-template', { responseType: 'blob' })
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/users/import', expect.any(FormData))
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/users/export', { responseType: 'blob' })
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/roles', {
      code: 'AUDITOR',
      name: '审计员',
      description: '',
      permissionIds: [1],
      menuIds: [1],
    })
    expect(apiClient.put).toHaveBeenCalledWith('/api/admin/roles/4', {
      code: 'AUDITOR',
      name: '审计管理员',
      description: '',
      permissionIds: [1],
      menuIds: [1, 2],
    })
  })

  it('loads role permission and menu dictionaries', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok([]))

    await fetchAdminRoles()
    await fetchAdminPermissions()
    await fetchAdminMenus()

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/roles')
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/permissions')
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/menus')
  })

  it('uses department tree crud endpoints', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok([]))
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))
    vi.mocked(apiClient.put).mockResolvedValue(ok({}))
    vi.mocked(apiClient.delete).mockResolvedValue(ok({}))

    await fetchDepartments()
    await createDepartment({ parentId: null, name: '部门', code: 'DEPT', description: '', status: 'ACTIVE' })
    await updateDepartment(2, { parentId: 1, name: '部门', code: 'DEPT', description: '', status: 'DISABLED' })
    await deleteDepartment(2)

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/departments')
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/departments', {
      parentId: null,
      name: '部门',
      code: 'DEPT',
      description: '',
      status: 'ACTIVE',
    })
    expect(apiClient.put).toHaveBeenCalledWith('/api/admin/departments/2', {
      parentId: 1,
      name: '部门',
      code: 'DEPT',
      description: '',
      status: 'DISABLED',
    })
    expect(apiClient.delete).toHaveBeenCalledWith('/api/admin/departments/2')
  })

  it('uses identity settings and registration review endpoints', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok([]))
    vi.mocked(apiClient.put).mockResolvedValue(ok({}))
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))

    const settings = {
      selfRegistrationEnabled: true,
      emailVerificationRequired: true,
      adminApprovalRequired: false,
      defaultRoleCode: 'STUDENT',
      defaultDepartmentId: 2,
      allowedEmailDomains: ['example.com'],
      termsText: '',
    }

    await fetchAdminRegistrationSettings()
    await updateAdminRegistrationSettings(settings)
    await fetchAdminMailStatus()
    await sendAdminTestMail('admin@example.com')
    await fetchRegistrationRequests()
    await approveRegistrationRequest(8)
    await rejectRegistrationRequest(9, '信息不完整')

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/auth/registration-settings')
    expect(apiClient.put).toHaveBeenCalledWith('/api/admin/auth/registration-settings', settings)
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/auth/mail-status')
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/auth/test-mail', { email: 'admin@example.com' })
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/auth/registration-requests')
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/auth/registration-requests/8/approve')
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/auth/registration-requests/9/reject', { reason: '信息不完整' })
  })

  it('uses platform notification and integration endpoints', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok([]))
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))
    vi.mocked(apiClient.put).mockResolvedValue(ok({}))

    const integration = {
      name: 'Webhook',
      integrationType: 'WEBHOOK',
      endpointUrl: 'https://example.com/hook',
      secretMask: '****',
      enabled: true,
    }

    await fetchPlatformNotifications()
    await markPlatformNotificationRead(2)
    await fetchExternalIntegrations()
    await createExternalIntegration(integration)
    await updateExternalIntegration(3, integration)
    await testExternalIntegration(3)
    await fetchExternalIntegrationEvents()

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/platform/notifications')
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/platform/notifications/2/read')
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/platform/integrations')
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/platform/integrations', integration)
    expect(apiClient.put).toHaveBeenCalledWith('/api/admin/platform/integrations/3', integration)
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/platform/integrations/3/test')
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/platform/integration-events')
  })
})
