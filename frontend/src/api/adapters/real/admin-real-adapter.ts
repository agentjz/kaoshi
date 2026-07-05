import { apiClient } from '../../client'
import type { AdminAdapter } from '../admin-adapter'
import type {
  AdminMenu,
  AdminPermission,
  AdminRole,
  AdminUser,
  Department,
  ExternalIntegration,
  ExternalIntegrationEvent,
  ExcelImportResult,
  PageResult,
  PlatformNotification,
  RegistrationRequest,
} from '../../admin'
import type { ApiResponse, MailStatus, RegistrationSettings } from '../../types'

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
  async fetchRegistrationSettings() {
    const response = await apiClient.get<ApiResponse<RegistrationSettings>>('/api/admin/auth/registration-settings')
    return response.data.data
  },
  async updateRegistrationSettings(payload) {
    const response = await apiClient.put<ApiResponse<RegistrationSettings>>('/api/admin/auth/registration-settings', payload)
    return response.data.data
  },
  async fetchMailStatus() {
    const response = await apiClient.get<ApiResponse<MailStatus>>('/api/admin/auth/mail-status')
    return response.data.data
  },
  async sendTestMail(email) {
    await apiClient.post<ApiResponse<void>>('/api/admin/auth/test-mail', { email })
  },
  async fetchRegistrationRequests() {
    const response = await apiClient.get<ApiResponse<RegistrationRequest[]>>('/api/admin/auth/registration-requests')
    return response.data.data
  },
  async approveRegistrationRequest(userId) {
    const response = await apiClient.post<ApiResponse<RegistrationRequest>>(`/api/admin/auth/registration-requests/${userId}/approve`)
    return response.data.data
  },
  async rejectRegistrationRequest(userId, reason) {
    const response = await apiClient.post<ApiResponse<RegistrationRequest>>(`/api/admin/auth/registration-requests/${userId}/reject`, { reason })
    return response.data.data
  },
  async fetchPlatformNotifications() {
    const response = await apiClient.get<ApiResponse<PlatformNotification[]>>('/api/admin/platform/notifications')
    return response.data.data
  },
  async markPlatformNotificationRead(id) {
    await apiClient.post<ApiResponse<void>>(`/api/admin/platform/notifications/${id}/read`)
  },
  async fetchExternalIntegrations() {
    const response = await apiClient.get<ApiResponse<ExternalIntegration[]>>('/api/admin/platform/integrations')
    return response.data.data
  },
  async createExternalIntegration(payload) {
    const response = await apiClient.post<ApiResponse<ExternalIntegration>>('/api/admin/platform/integrations', payload)
    return response.data.data
  },
  async updateExternalIntegration(id, payload) {
    const response = await apiClient.put<ApiResponse<ExternalIntegration>>(`/api/admin/platform/integrations/${id}`, payload)
    return response.data.data
  },
  async testExternalIntegration(id) {
    const response = await apiClient.post<ApiResponse<ExternalIntegrationEvent[]>>(`/api/admin/platform/integrations/${id}/test`)
    return response.data.data
  },
  async fetchExternalIntegrationEvents() {
    const response = await apiClient.get<ApiResponse<ExternalIntegrationEvent[]>>('/api/admin/platform/integration-events')
    return response.data.data
  },
}
