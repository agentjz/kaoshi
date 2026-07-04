import { adminAdapter } from './adapters/current'

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
