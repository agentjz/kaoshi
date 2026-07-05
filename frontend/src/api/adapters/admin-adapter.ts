import type {
  AdminMenu,
  AdminPermission,
  AdminRole,
  AdminUser,
  Department,
  DepartmentPayload,
  ExcelImportResult,
  ExternalIntegration,
  ExternalIntegrationEvent,
  ExternalIntegrationPayload,
  PageResult,
  PlatformNotification,
  RegistrationRequest,
  RoleSavePayload,
  UserCreatePayload,
  UserUpdatePayload,
} from '../admin'
import type { MailStatus, RegistrationSettings } from '../types'

export interface AdminAdapter {
  fetchAdminUsers(params: { page: number; size: number; keyword?: string }): Promise<PageResult<AdminUser>>
  createAdminUser(payload: UserCreatePayload): Promise<AdminUser>
  updateAdminUser(id: number, payload: UserUpdatePayload): Promise<AdminUser>
  changeAdminUserStatus(id: number, status: AdminUser['status']): Promise<AdminUser>
  downloadUserImportTemplate(): Promise<Blob>
  importUsers(file: File): Promise<ExcelImportResult>
  downloadUserExport(): Promise<Blob>
  fetchAdminRoles(): Promise<AdminRole[]>
  createAdminRole(payload: RoleSavePayload): Promise<AdminRole>
  updateAdminRole(id: number, payload: RoleSavePayload): Promise<AdminRole>
  fetchAdminPermissions(): Promise<AdminPermission[]>
  fetchAdminMenus(): Promise<AdminMenu[]>
  fetchDepartments(): Promise<Department[]>
  createDepartment(payload: DepartmentPayload): Promise<Department>
  updateDepartment(id: number, payload: DepartmentPayload): Promise<Department>
  deleteDepartment(id: number): Promise<void>
  fetchRegistrationSettings(): Promise<RegistrationSettings>
  updateRegistrationSettings(payload: RegistrationSettings): Promise<RegistrationSettings>
  fetchMailStatus(): Promise<MailStatus>
  sendTestMail(email: string): Promise<void>
  fetchRegistrationRequests(): Promise<RegistrationRequest[]>
  approveRegistrationRequest(userId: number): Promise<RegistrationRequest>
  rejectRegistrationRequest(userId: number, reason: string): Promise<RegistrationRequest>
  fetchPlatformNotifications(): Promise<PlatformNotification[]>
  markPlatformNotificationRead(id: number): Promise<void>
  fetchExternalIntegrations(): Promise<ExternalIntegration[]>
  createExternalIntegration(payload: ExternalIntegrationPayload): Promise<ExternalIntegration>
  updateExternalIntegration(id: number, payload: ExternalIntegrationPayload): Promise<ExternalIntegration>
  testExternalIntegration(id: number): Promise<ExternalIntegrationEvent[]>
  fetchExternalIntegrationEvents(): Promise<ExternalIntegrationEvent[]>
}
