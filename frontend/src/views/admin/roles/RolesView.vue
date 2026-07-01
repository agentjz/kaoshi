<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>角色管理</h1>
        <p>维护角色编码、权限集合和可见菜单。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建角色</el-button>
    </header>

    <el-table v-loading="loading" :data="roles" class="data-table" border>
      <el-table-column label="角色" min-width="220">
        <template #default="{ row }: { row: AdminRole }">
          <div class="entity-stack">
            <span class="entity-name">{{ row.name }}</span>
            <span class="code-text">{{ row.code }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="权限" min-width="240">
        <template #default="{ row }: { row: AdminRole }">
          <div class="tag-stack">
            <el-tag type="primary" effect="plain">{{ row.permissions.length }} 项权限</el-tag>
            <el-tag v-for="permission in row.permissions.slice(0, 3)" :key="permission.id" effect="plain">
              {{ permission.name }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="可见菜单" min-width="220">
        <template #default="{ row }: { row: AdminRole }">
          <div class="tag-stack">
            <el-tag type="success" effect="plain">{{ row.menus.length }} 个菜单</el-tag>
            <el-tag v-for="menu in row.menus.slice(0, 3)" :key="menu.id" type="success" effect="plain">
              {{ menu.title }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: AdminRole }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingRole ? '编辑角色' : '新建角色'" width="820px" class="role-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <section class="form-section">
          <h2>基础信息</h2>
          <el-form-item label="角色名称" prop="name">
            <el-input v-model.trim="form.name" maxlength="64" />
          </el-form-item>
          <el-form-item label="角色编码" prop="code">
            <el-input v-model.trim="form.code" maxlength="64" />
          </el-form-item>
          <el-form-item label="说明" prop="description">
            <el-input v-model.trim="form.description" maxlength="255" type="textarea" :rows="3" />
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="section-title">
            <h2>管理权限</h2>
            <div>
              <el-button link type="primary" @click="selectAllPermissions">全选</el-button>
              <el-button link @click="clearPermissions">清空</el-button>
            </div>
          </div>
          <el-form-item prop="permissionIds" label-width="0">
            <el-checkbox-group v-model="form.permissionIds" class="check-grid">
              <el-checkbox v-for="permission in permissions" :key="permission.id" :label="permission.id" border>
                <span class="check-title">{{ permission.name }}</span>
                <span class="code-text">{{ permission.code }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="section-title">
            <h2>可见菜单</h2>
            <div>
              <el-button link type="primary" @click="selectAllMenus">全选</el-button>
              <el-button link @click="clearMenus">清空</el-button>
            </div>
          </div>
          <el-form-item prop="menuIds" label-width="0">
            <el-checkbox-group v-model="form.menuIds" class="check-grid">
              <el-checkbox v-for="menu in menus" :key="menu.id" :label="menu.id" border>
                <span class="check-title">{{ menu.title }}</span>
                <span class="code-text">{{ menu.path }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

import {
  createAdminRole,
  fetchAdminMenus,
  fetchAdminPermissions,
  fetchAdminRoles,
  updateAdminRole,
  type AdminMenu,
  type AdminPermission,
  type AdminRole,
} from '@/api/admin'

interface RoleForm {
  code: string
  name: string
  description: string
  permissionIds: number[]
  menuIds: number[]
}

const roles = ref<AdminRole[]>([])
const permissions = ref<AdminPermission[]>([])
const menus = ref<AdminMenu[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingRole = ref<AdminRole | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<RoleForm>({
  code: '',
  name: '',
  description: '',
  permissionIds: [],
  menuIds: [],
})

const rules: FormRules<RoleForm> = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  permissionIds: [{ required: true, type: 'array', min: 1, message: '请选择权限', trigger: 'change' }],
  menuIds: [{ required: true, type: 'array', min: 1, message: '请选择菜单', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadRoles(), loadDictionaries()])
})

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await fetchAdminRoles()
  } finally {
    loading.value = false
  }
}

async function loadDictionaries() {
  const [permissionList, menuList] = await Promise.all([fetchAdminPermissions(), fetchAdminMenus()])
  permissions.value = permissionList
  menus.value = menuList
}

function openCreateDialog() {
  editingRole.value = null
  form.code = ''
  form.name = ''
  form.description = ''
  form.permissionIds = []
  form.menuIds = []
  dialogVisible.value = true
}

function openEditDialog(role: AdminRole) {
  editingRole.value = role
  form.code = role.code
  form.name = role.name
  form.description = role.description || ''
  form.permissionIds = role.permissions.map((permission) => permission.id)
  form.menuIds = role.menus.map((menu) => menu.id)
  dialogVisible.value = true
}

function selectAllPermissions() {
  form.permissionIds = permissions.value.map((permission) => permission.id)
}

function clearPermissions() {
  form.permissionIds = []
}

function selectAllMenus() {
  form.menuIds = menus.value.map((menu) => menu.id)
}

function clearMenus() {
  form.menuIds = []
}

async function submitRole() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      code: form.code,
      name: form.name,
      description: form.description,
      permissionIds: form.permissionIds,
      menuIds: form.menuIds,
    }
    if (editingRole.value) {
      await updateAdminRole(editingRole.value.id, payload)
      ElMessage.success('角色已更新')
    } else {
      await createAdminRole(payload)
      ElMessage.success('角色已创建')
    }
    dialogVisible.value = false
    await loadRoles()
  } finally {
    saving.value = false
  }
}
</script>
