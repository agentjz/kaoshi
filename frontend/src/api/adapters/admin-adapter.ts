import type {
  AdminMenu,
  AdminPermission,
  AdminRole,
  AdminUser,
  Department,
  DepartmentPayload,
  ExcelImportResult,
  PageResult,
  RoleSavePayload,
  UserCreatePayload,
  UserUpdatePayload,
} from '../admin'

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
}
