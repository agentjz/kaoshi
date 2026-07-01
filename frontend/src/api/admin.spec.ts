import { describe, expect, it, vi } from 'vitest'

import {
  changeAdminUserStatus,
  createAdminRole,
  createAdminUser,
  fetchAdminMenus,
  fetchAdminPermissions,
  fetchAdminRoles,
  fetchAdminUsers,
  updateAdminRole,
  updateAdminUser,
} from './admin'
import { apiClient } from './client'

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
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

    await createAdminUser({ username: 'teacher', displayName: '老师', password: 'password', roleIds: [2] })
    await updateAdminUser(2, { displayName: '主管', roleIds: [2, 3] })
    await changeAdminUserStatus(2, 'DISABLED')
    await createAdminRole({ code: 'AUDITOR', name: '审计员', description: '', permissionIds: [1], menuIds: [1] })
    await updateAdminRole(4, { code: 'AUDITOR', name: '审计管理员', description: '', permissionIds: [1], menuIds: [1, 2] })

    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/users', {
      username: 'teacher',
      displayName: '老师',
      password: 'password',
      roleIds: [2],
    })
    expect(apiClient.put).toHaveBeenCalledWith('/api/admin/users/2', { displayName: '主管', roleIds: [2, 3] })
    expect(apiClient.patch).toHaveBeenCalledWith('/api/admin/users/2/status', { status: 'DISABLED' })
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
})
