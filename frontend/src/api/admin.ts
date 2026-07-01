import { apiClient } from './client'
import type { ApiResponse } from './types'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface AdminUser {
  id: number
  username: string
  displayName: string
  status: 'ACTIVE' | 'DISABLED'
  roles: string[]
  createdAt: string
  updatedAt: string
}

export interface UserCreatePayload {
  username: string
  displayName: string
  password: string
  roleIds: number[]
}

export interface UserUpdatePayload {
  displayName: string
  password?: string
  roleIds: number[]
}

export interface AdminRole {
  id: number
  code: string
  name: string
  description: string | null
  permissions: AdminPermission[]
  menus: AdminMenu[]
}

export interface RoleSavePayload {
  code: string
  name: string
  description: string
  permissionIds: number[]
  menuIds: number[]
}

export interface AdminPermission {
  id: number
  code: string
  name: string
  description: string | null
}

export interface AdminMenu {
  id: number
  code: string
  title: string
  path: string
  parentId: number | null
  sortOrder: number
  icon: string | null
}

export async function fetchAdminUsers(params: {
  page: number
  size: number
  keyword?: string
}): Promise<PageResult<AdminUser>> {
  const response = await apiClient.get<ApiResponse<PageResult<AdminUser>>>('/api/admin/users', { params })
  return response.data.data
}

export async function createAdminUser(payload: UserCreatePayload): Promise<AdminUser> {
  const response = await apiClient.post<ApiResponse<AdminUser>>('/api/admin/users', payload)
  return response.data.data
}

export async function updateAdminUser(id: number, payload: UserUpdatePayload): Promise<AdminUser> {
  const response = await apiClient.put<ApiResponse<AdminUser>>(`/api/admin/users/${id}`, payload)
  return response.data.data
}

export async function changeAdminUserStatus(id: number, status: AdminUser['status']): Promise<AdminUser> {
  const response = await apiClient.patch<ApiResponse<AdminUser>>(`/api/admin/users/${id}/status`, { status })
  return response.data.data
}

export async function fetchAdminRoles(): Promise<AdminRole[]> {
  const response = await apiClient.get<ApiResponse<AdminRole[]>>('/api/admin/roles')
  return response.data.data
}

export async function createAdminRole(payload: RoleSavePayload): Promise<AdminRole> {
  const response = await apiClient.post<ApiResponse<AdminRole>>('/api/admin/roles', payload)
  return response.data.data
}

export async function updateAdminRole(id: number, payload: RoleSavePayload): Promise<AdminRole> {
  const response = await apiClient.put<ApiResponse<AdminRole>>(`/api/admin/roles/${id}`, payload)
  return response.data.data
}

export async function fetchAdminPermissions(): Promise<AdminPermission[]> {
  const response = await apiClient.get<ApiResponse<AdminPermission[]>>('/api/admin/permissions')
  return response.data.data
}

export async function fetchAdminMenus(): Promise<AdminMenu[]> {
  const response = await apiClient.get<ApiResponse<AdminMenu[]>>('/api/admin/menus')
  return response.data.data
}
