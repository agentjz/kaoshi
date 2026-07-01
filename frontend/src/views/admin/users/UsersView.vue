<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>用户管理</h1>
        <p>维护平台账号、启用状态和角色边界。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建用户</el-button>
    </header>

    <div class="toolbar">
      <el-input
        v-model.trim="query.keyword"
        clearable
        placeholder="搜索账号或姓名"
        class="toolbar__search"
        @keyup.enter="loadUsers"
        @clear="loadUsers"
      />
      <el-button :icon="Search" @click="loadUsers">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="users" class="data-table" border>
      <el-table-column prop="username" label="账号" min-width="140" />
      <el-table-column prop="displayName" label="姓名" min-width="140" />
      <el-table-column label="角色" min-width="220">
        <template #default="{ row }: { row: AdminUser }">
          <el-space wrap>
            <el-tag v-for="role in row.roles" :key="role" effect="plain">{{ role }}</el-tag>
          </el-space>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }: { row: AdminUser }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column fixed="right" label="操作" width="220">
        <template #default="{ row }: { row: AdminUser }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
            :disabled="row.id === 1 && row.status === 'ACTIVE'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadUsers"
        @current-change="loadUsers"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingUser ? '编辑用户' : '新建用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item v-if="!editingUser" label="账号" prop="username">
          <el-input v-model.trim="form.username" maxlength="64" />
        </el-form-item>
        <el-form-item label="姓名" prop="displayName">
          <el-input v-model.trim="form.displayName" maxlength="64" />
        </el-form-item>
        <el-form-item :label="editingUser ? '新密码' : '密码'" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="editingUser ? '不填写则保持原密码' : '至少 6 位'"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple class="form-control" placeholder="选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="`${role.name} (${role.code})`" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitUser">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

import {
  changeAdminUserStatus,
  createAdminUser,
  fetchAdminRoles,
  fetchAdminUsers,
  updateAdminUser,
  type AdminRole,
  type AdminUser,
} from '@/api/admin'

interface UserForm {
  username: string
  displayName: string
  password: string
  roleIds: number[]
}

const users = ref<AdminUser[]>([])
const roles = ref<AdminRole[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingUser = ref<AdminUser | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  size: 20,
  keyword: '',
})

const form = reactive<UserForm>({
  username: '',
  displayName: '',
  password: '',
  roleIds: [],
})

const rules: FormRules<UserForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    {
      validator: (_rule, value: string, callback) => {
        if (!editingUser.value && !value) {
          callback(new Error('请输入密码'))
          return
        }
        if (value && value.length < 6) {
          callback(new Error('密码至少 6 位'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请选择角色', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadRoles(), loadUsers()])
})

async function loadRoles() {
  roles.value = await fetchAdminRoles()
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await fetchAdminUsers({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
    })
    users.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingUser.value = null
  form.username = ''
  form.displayName = ''
  form.password = ''
  form.roleIds = []
  dialogVisible.value = true
}

function openEditDialog(user: AdminUser) {
  editingUser.value = user
  form.username = user.username
  form.displayName = user.displayName
  form.password = ''
  form.roleIds = roles.value.filter((role) => user.roles.includes(role.code)).map((role) => role.id)
  dialogVisible.value = true
}

async function submitUser() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingUser.value) {
      await updateAdminUser(editingUser.value.id, {
        displayName: form.displayName,
        password: form.password || undefined,
        roleIds: form.roleIds,
      })
      ElMessage.success('用户已更新')
    } else {
      await createAdminUser({
        username: form.username,
        displayName: form.displayName,
        password: form.password,
        roleIds: form.roleIds,
      })
      ElMessage.success('用户已创建')
    }
    dialogVisible.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(user: AdminUser) {
  await changeAdminUserStatus(user.id, user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE')
  ElMessage.success('用户状态已更新')
  await loadUsers()
}
</script>
