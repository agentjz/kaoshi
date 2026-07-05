import { adminAdapter } from './adapters/current'
import type { MailStatus, RegistrationSettings } from './types'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface AdminUser {
  id: number
  departmentId: number | null
  departmentName: string | null
  username: string
  displayName: string
  status: 'ACTIVE' | 'DISABLED'
  roles: string[]
  createdAt: string
  updatedAt: string
}

export interface UserCreatePayload {
  departmentId: number | null
  username: string
  displayName: string
  roleIds: number[]
}

export interface UserUpdatePayload {
  departmentId: number | null
  displayName: string
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

export interface ExcelImportResult {
  successCount: number
  failureCount: number
  errors: string[]
}

export interface Department {
  id: number
  parentId: number | null
  name: string
  code: string
  description: string | null
  status: 'ACTIVE' | 'DISABLED'
  children: Department[]
}

export interface DepartmentPayload {
  parentId: number | null
  name: string
  code: string
  description: string
  status: Department['status']
}

export interface RegistrationRequest {
  userId: number
  username: string
  displayName: string
  email: string
  status: 'ACTIVE' | 'DISABLED'
  approvalStatus: 'APPROVED' | 'PENDING' | 'REJECTED'
  registeredAt: string
}

export interface PlatformNotification {
  id: number
  recipientUserId: number | null
  title: string
  content: string
  category: string
  read: boolean
  createdAt: string
}

export interface ExternalIntegration {
  id: number
  name: string
  integrationType: string
  endpointUrl: string
  secretMask: string | null
  enabled: boolean
  updatedAt: string
}

export interface ExternalIntegrationPayload {
  name: string
  integrationType: string
  endpointUrl: string
  secretMask: string
  enabled: boolean
}

export interface ExternalIntegrationEvent {
  id: number
  integrationId: number
  eventType: string
  status: string
  payloadSummary: string | null
  errorMessage: string | null
  createdAt: string
}

export function fetchAdminUsers(params: { page: number; size: number; keyword?: string }): Promise<PageResult<AdminUser>> {
  return adminAdapter.fetchAdminUsers(params)
}

export function createAdminUser(payload: UserCreatePayload): Promise<AdminUser> {
  return adminAdapter.createAdminUser(payload)
}

export function updateAdminUser(id: number, payload: UserUpdatePayload): Promise<AdminUser> {
  return adminAdapter.updateAdminUser(id, payload)
}

export function changeAdminUserStatus(id: number, status: AdminUser['status']): Promise<AdminUser> {
  return adminAdapter.changeAdminUserStatus(id, status)
}

export function downloadUserImportTemplate(): Promise<Blob> {
  return adminAdapter.downloadUserImportTemplate()
}

export function importUsers(file: File): Promise<ExcelImportResult> {
  return adminAdapter.importUsers(file)
}

export function downloadUserExport(): Promise<Blob> {
  return adminAdapter.downloadUserExport()
}

export function fetchAdminRoles(): Promise<AdminRole[]> {
  return adminAdapter.fetchAdminRoles()
}

export function createAdminRole(payload: RoleSavePayload): Promise<AdminRole> {
  return adminAdapter.createAdminRole(payload)
}

export function updateAdminRole(id: number, payload: RoleSavePayload): Promise<AdminRole> {
  return adminAdapter.updateAdminRole(id, payload)
}

export function fetchAdminPermissions(): Promise<AdminPermission[]> {
  return adminAdapter.fetchAdminPermissions()
}

export function fetchAdminMenus(): Promise<AdminMenu[]> {
  return adminAdapter.fetchAdminMenus()
}

export function fetchDepartments(): Promise<Department[]> {
  return adminAdapter.fetchDepartments()
}

export function createDepartment(payload: DepartmentPayload): Promise<Department> {
  return adminAdapter.createDepartment(payload)
}

export function updateDepartment(id: number, payload: DepartmentPayload): Promise<Department> {
  return adminAdapter.updateDepartment(id, payload)
}

export function deleteDepartment(id: number): Promise<void> {
  return adminAdapter.deleteDepartment(id)
}

export function fetchAdminRegistrationSettings(): Promise<RegistrationSettings> {
  return adminAdapter.fetchRegistrationSettings()
}

export function updateAdminRegistrationSettings(payload: RegistrationSettings): Promise<RegistrationSettings> {
  return adminAdapter.updateRegistrationSettings(payload)
}

export function fetchAdminMailStatus(): Promise<MailStatus> {
  return adminAdapter.fetchMailStatus()
}

export function sendAdminTestMail(email: string): Promise<void> {
  return adminAdapter.sendTestMail(email)
}

export function fetchRegistrationRequests(): Promise<RegistrationRequest[]> {
  return adminAdapter.fetchRegistrationRequests()
}

export function approveRegistrationRequest(userId: number): Promise<RegistrationRequest> {
  return adminAdapter.approveRegistrationRequest(userId)
}

export function rejectRegistrationRequest(userId: number, reason: string): Promise<RegistrationRequest> {
  return adminAdapter.rejectRegistrationRequest(userId, reason)
}

export function fetchPlatformNotifications(): Promise<PlatformNotification[]> {
  return adminAdapter.fetchPlatformNotifications()
}

export function markPlatformNotificationRead(id: number): Promise<void> {
  return adminAdapter.markPlatformNotificationRead(id)
}

export function fetchExternalIntegrations(): Promise<ExternalIntegration[]> {
  return adminAdapter.fetchExternalIntegrations()
}

export function createExternalIntegration(payload: ExternalIntegrationPayload): Promise<ExternalIntegration> {
  return adminAdapter.createExternalIntegration(payload)
}

export function updateExternalIntegration(id: number, payload: ExternalIntegrationPayload): Promise<ExternalIntegration> {
  return adminAdapter.updateExternalIntegration(id, payload)
}

export function testExternalIntegration(id: number): Promise<ExternalIntegrationEvent[]> {
  return adminAdapter.testExternalIntegration(id)
}

export function fetchExternalIntegrationEvents(): Promise<ExternalIntegrationEvent[]> {
  return adminAdapter.fetchExternalIntegrationEvents()
}
