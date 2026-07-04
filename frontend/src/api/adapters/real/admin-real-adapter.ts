import { apiClient } from '../../client'
import type { AdminAdapter } from '../admin-adapter'
import type {
  AdminMenu,
  AdminPermission,
  AdminRole,
  AdminUser,
  Department,
  ExcelImportResult,
  PageResult,
} from '../../admin'
import type { ApiResponse } from '../../types'

export const realAdminAdapter: AdminAdapter = {
  async fetchAdminUsers(params) {
    const response = await apiClient.get<ApiResponse<PageResult<AdminUser>>>('/api/admin/users', { params })
    return response.data.data
  },
  async createAdminUser(payload) {
    const response = await apiClient.post<ApiResponse<AdminUser>>('/api/admin/users', payload)
    return response.data.data
  },
  async updateAdminUser(id, payload) {
    const response = await apiClient.put<ApiResponse<AdminUser>>(`/api/admin/users/${id}`, payload)
    return response.data.data
  },
  async changeAdminUserStatus(id, status) {
    const response = await apiClient.patch<ApiResponse<AdminUser>>(`/api/admin/users/${id}/status`, { status })
    return response.data.data
  },
  async downloadUserImportTemplate() {
    const response = await apiClient.get('/api/admin/users/import-template', { responseType: 'blob' })
    return response.data
  },
  async importUsers(file) {
    const form = new FormData()
    form.append('file', file)
    const response = await apiClient.post<ApiResponse<ExcelImportResult>>('/api/admin/users/import', form)
    return response.data.data
  },
  async downloadUserExport() {
    const response = await apiClient.get('/api/admin/users/export', { responseType: 'blob' })
    return response.data
  },
  async fetchAdminRoles() {
    const response = await apiClient.get<ApiResponse<AdminRole[]>>('/api/admin/roles')
    return response.data.data
  },
  async createAdminRole(payload) {
    const response = await apiClient.post<ApiResponse<AdminRole>>('/api/admin/roles', payload)
    return response.data.data
  },
  async updateAdminRole(id, payload) {
    const response = await apiClient.put<ApiResponse<AdminRole>>(`/api/admin/roles/${id}`, payload)
    return response.data.data
  },
  async fetchAdminPermissions() {
    const response = await apiClient.get<ApiResponse<AdminPermission[]>>('/api/admin/permissions')
    return response.data.data
  },
  async fetchAdminMenus() {
    const response = await apiClient.get<ApiResponse<AdminMenu[]>>('/api/admin/menus')
    return response.data.data
  },
  async fetchDepartments() {
    const response = await apiClient.get<ApiResponse<Department[]>>('/api/admin/departments')
    return response.data.data
  },
  async createDepartment(payload) {
    const response = await apiClient.post<ApiResponse<Department>>('/api/admin/departments', payload)
    return response.data.data
  },
  async updateDepartment(id, payload) {
    const response = await apiClient.put<ApiResponse<Department>>(`/api/admin/departments/${id}`, payload)
    return response.data.data
  },
  async deleteDepartment(id) {
    await apiClient.delete<ApiResponse<void>>(`/api/admin/departments/${id}`)
  },
}
