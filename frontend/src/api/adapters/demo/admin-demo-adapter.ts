import type { AdminAdapter } from '../admin-adapter'
import type { AdminRole, AdminUser, Department } from '../../admin'
import { clone, currentDemoState, nextId, nowIso } from './demo-store'

function paginate<T>(records: T[], page: number, size: number) {
  return {
    records: clone(records.slice((page - 1) * size, page * size)),
    total: records.length,
    page,
    size,
  }
}

function departmentName(id: number | null) {
  return flattenDepartments(currentDemoState().departments).find((department) => department.id === id)?.name || null
}

function flattenDepartments(items: Department[]): Department[] {
  return items.flatMap((item) => [item, ...flattenDepartments(item.children)])
}

function rebuildDepartmentTree(items: Department[]) {
  const byId = new Map(items.map((item) => [item.id, { ...item, children: [] as Department[] }]))
  const roots: Department[] = []
  for (const department of byId.values()) {
    if (department.parentId && byId.has(department.parentId)) {
      byId.get(department.parentId)?.children.push(department)
    } else {
      roots.push(department)
    }
  }
  return roots
}

function roleCodes(roleIds: number[]) {
  const state = currentDemoState()
  return state.roles.filter((role) => roleIds.includes(role.id)).map((role) => role.code)
}

function toAdminUser(user: AdminUser & { roleIds: number[] }): AdminUser {
  return clone({
    id: user.id,
    departmentId: user.departmentId,
    departmentName: departmentName(user.departmentId),
    username: user.username,
    displayName: user.displayName,
    status: user.status,
    roles: roleCodes(user.roleIds),
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  })
}

function textBlob(name: string, content: string) {
  return new File([content], name, { type: 'text/plain;charset=utf-8' })
}

export const demoAdminAdapter: AdminAdapter = {
  async fetchAdminUsers(params) {
    const keyword = params.keyword?.trim().toLowerCase()
    const users = currentDemoState().users
      .filter((user) => !keyword || user.username.toLowerCase().includes(keyword) || user.displayName.toLowerCase().includes(keyword))
      .map(toAdminUser)
    return paginate(users, params.page, params.size)
  },
  async createAdminUser(payload) {
    const state = currentDemoState()
    const timestamp = nowIso()
    const user = {
      id: nextId(state),
      departmentId: payload.departmentId,
      departmentName: departmentName(payload.departmentId),
      username: payload.username,
      displayName: payload.displayName,
      status: 'ACTIVE' as const,
      roles: roleCodes(payload.roleIds),
      roleIds: payload.roleIds,
      password: '123456',
      createdAt: timestamp,
      updatedAt: timestamp,
    }
    state.users.push(user)
    return toAdminUser(user)
  },
  async updateAdminUser(id, payload) {
    const user = currentDemoState().users.find((item) => item.id === id)
    if (!user) {
      throw new Error('用户不存在')
    }
    user.departmentId = payload.departmentId
    user.departmentName = departmentName(payload.departmentId)
    user.displayName = payload.displayName
    user.roleIds = payload.roleIds
    user.roles = roleCodes(payload.roleIds)
    user.updatedAt = nowIso()
    return toAdminUser(user)
  },
  async changeAdminUserStatus(id, status) {
    const user = currentDemoState().users.find((item) => item.id === id)
    if (!user) {
      throw new Error('用户不存在')
    }
    user.status = status
    user.updatedAt = nowIso()
    return toAdminUser(user)
  },
  async downloadUserImportTemplate() {
    return textBlob('用户导入模板.txt', '账号,姓名,部门,角色')
  },
  async importUsers() {
    return { successCount: 0, failureCount: 0, errors: ['演示环境不持久化导入文件，请使用在线新建体验用户维护。'] }
  },
  async downloadUserExport() {
    return textBlob('用户导出.txt', currentDemoState().users.map((user) => `${user.username},${user.displayName}`).join('\n'))
  },
  async fetchAdminRoles() {
    return clone(currentDemoState().roles)
  },
  async createAdminRole(payload) {
    const state = currentDemoState()
    const role: AdminRole = {
      id: nextId(state),
      code: payload.code,
      name: payload.name,
      description: payload.description,
      permissions: state.permissions.filter((permission) => payload.permissionIds.includes(permission.id)),
      menus: state.menus.filter((menu) => payload.menuIds.includes(menu.id)),
    }
    state.roles.push(role)
    return clone(role)
  },
  async updateAdminRole(id, payload) {
    const state = currentDemoState()
    const role = state.roles.find((item) => item.id === id)
    if (!role) {
      throw new Error('角色不存在')
    }
    role.code = payload.code
    role.name = payload.name
    role.description = payload.description
    role.permissions = state.permissions.filter((permission) => payload.permissionIds.includes(permission.id))
    role.menus = state.menus.filter((menu) => payload.menuIds.includes(menu.id))
    return clone(role)
  },
  async fetchAdminPermissions() {
    return clone(currentDemoState().permissions)
  },
  async fetchAdminMenus() {
    return clone(currentDemoState().menus)
  },
  async fetchDepartments() {
    return clone(currentDemoState().departments)
  },
  async createDepartment(payload) {
    const state = currentDemoState()
    const flat = flattenDepartments(state.departments)
    const department: Department = { id: nextId(state), ...payload, children: [] }
    state.departments = rebuildDepartmentTree([...flat, department])
    return clone(department)
  },
  async updateDepartment(id, payload) {
    const state = currentDemoState()
    const flat = flattenDepartments(state.departments)
    const department = flat.find((item) => item.id === id)
    if (!department) {
      throw new Error('部门不存在')
    }
    Object.assign(department, payload)
    state.departments = rebuildDepartmentTree(flat)
    return clone(department)
  },
  async deleteDepartment(id) {
    const state = currentDemoState()
    const flat = flattenDepartments(state.departments)
    if (flat.some((department) => department.parentId === id)) {
      throw new Error('部门下存在下级，不能删除')
    }
    state.departments = rebuildDepartmentTree(flat.filter((department) => department.id !== id))
  },
}
