<template>
  <section class="settings-page">
    <header class="page-header">
      <div>
        <h1>平台设置</h1>
        <p>维护自助注册、邮件服务状态和注册申请审核。</p>
      </div>
      <el-button :loading="loading" @click="loadData">刷新</el-button>
    </header>

    <div class="settings-grid">
      <section class="settings-section">
        <div class="section-head">
          <h2>注册策略</h2>
          <el-button type="primary" :loading="saving" @click="saveSettings">保存</el-button>
        </div>
        <el-form ref="formRef" :model="form" label-position="top">
          <el-form-item label="开放自助注册">
            <el-switch v-model="form.selfRegistrationEnabled" />
          </el-form-item>
          <el-form-item label="邮箱验证">
            <el-switch v-model="form.emailVerificationRequired" />
          </el-form-item>
          <el-form-item label="管理员审核">
            <el-switch v-model="form.adminApprovalRequired" />
          </el-form-item>
          <el-form-item label="默认角色">
            <el-select v-model="form.defaultRoleCode" class="full-control" filterable>
              <el-option v-for="role in roles" :key="role.code" :label="`${role.name} (${role.code})`" :value="role.code" />
            </el-select>
          </el-form-item>
          <el-form-item label="默认部门">
            <el-tree-select
              v-model="form.defaultDepartmentId"
              class="full-control"
              clearable
              :data="departmentOptions"
              :props="{ label: 'name', value: 'id', children: 'children' }"
              check-strictly
            />
          </el-form-item>
          <el-form-item label="允许邮箱域名">
            <el-input v-model="allowedDomainsText" placeholder="留空表示不限制，例如 example.com, school.edu" />
          </el-form-item>
          <el-form-item label="注册条款">
            <el-input v-model="form.termsText" type="textarea" :rows="4" maxlength="1000" show-word-limit />
          </el-form-item>
        </el-form>
      </section>

      <section class="settings-section">
        <div class="section-head">
          <h2>邮件服务</h2>
          <el-tag :type="mailStatus.configured ? 'success' : 'warning'">{{ mailStatus.configured ? '可用' : '未配置' }}</el-tag>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="状态">{{ mailStatus.message }}</el-descriptions-item>
          <el-descriptions-item label="发送模式">{{ mailStatus.deliveryMode }}</el-descriptions-item>
          <el-descriptions-item label="发件人">{{ mailStatus.from || '-' }}</el-descriptions-item>
          <el-descriptions-item label="SMTP">{{ mailStatus.host || '-' }}{{ mailStatus.port ? `:${mailStatus.port}` : '' }}</el-descriptions-item>
        </el-descriptions>
        <el-form class="test-mail" label-position="top" @submit.prevent>
          <el-form-item label="测试收件邮箱">
            <div class="test-mail__row">
              <el-input v-model.trim="testEmail" placeholder="admin@example.com" />
              <el-button :disabled="!mailStatus.configured || !testEmail" :loading="testingMail" @click="sendTest">发送测试</el-button>
            </div>
          </el-form-item>
        </el-form>
      </section>
    </div>

    <div class="settings-grid">
      <section class="settings-section">
        <div class="section-head">
          <h2>通知中心</h2>
          <el-tag>{{ unreadNotificationCount }} 条未读</el-tag>
        </div>
        <el-table :data="notifications" border max-height="260">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="category" label="分类" width="110" />
          <el-table-column prop="content" label="内容" min-width="220" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.read ? 'info' : 'warning'">{{ row.read ? '已读' : '未读' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.read" @click="readNotification(row.id)">标记已读</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="settings-section">
        <div class="section-head">
          <h2>外部集成</h2>
          <el-button type="primary" @click="saveIntegration">保存集成</el-button>
        </div>
        <el-form label-position="top">
          <el-form-item label="名称">
            <el-input v-model="integrationForm.name" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="integrationForm.integrationType" class="full-control">
              <el-option label="Webhook" value="WEBHOOK" />
              <el-option label="监考系统" value="PROCTORING" />
              <el-option label="成绩同步" value="RESULT_EXPORT" />
            </el-select>
          </el-form-item>
          <el-form-item label="Endpoint">
            <el-input v-model="integrationForm.endpointUrl" />
          </el-form-item>
          <el-form-item label="Secret">
            <el-input v-model="integrationForm.secretMask" show-password />
          </el-form-item>
          <el-form-item label="启用">
            <el-switch v-model="integrationForm.enabled" />
          </el-form-item>
        </el-form>
        <el-table :data="integrations" border max-height="220">
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="integrationType" label="类型" width="120" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="editIntegration(row)">编辑</el-button>
              <el-button link type="warning" @click="testIntegration(row.id)">测试</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-table :data="integrationEvents" border max-height="180" class="integration-events">
          <el-table-column prop="eventType" label="事件" width="100" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="payloadSummary" label="摘要" min-width="180" />
          <el-table-column prop="createdAt" label="时间" min-width="170" />
        </el-table>
      </section>
    </div>

    <section class="settings-section">
      <div class="section-head">
        <h2>注册申请</h2>
        <el-tag>{{ requests.length }} 条待审核</el-tag>
      </div>
      <el-table v-loading="loading" :data="requests" border>
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="displayName" label="姓名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="registeredAt" label="注册时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="approve(row.userId)">通过</el-button>
            <el-button type="danger" link @click="reject(row.userId)">拒绝</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待审核注册申请" />
        </template>
      </el-table>
    </section>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import {
  approveRegistrationRequest,
  createExternalIntegration,
  fetchExternalIntegrationEvents,
  fetchExternalIntegrations,
  fetchAdminMailStatus,
  fetchAdminRegistrationSettings,
  fetchAdminRoles,
  fetchDepartments,
  fetchPlatformNotifications,
  fetchRegistrationRequests,
  markPlatformNotificationRead,
  rejectRegistrationRequest,
  sendAdminTestMail,
  testExternalIntegration,
  updateExternalIntegration,
  updateAdminRegistrationSettings,
  type AdminRole,
  type Department,
  type ExternalIntegration,
  type ExternalIntegrationEvent,
  type ExternalIntegrationPayload,
  type PlatformNotification,
  type RegistrationRequest,
} from '@/api/admin'
import type { MailStatus, RegistrationSettings } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const testingMail = ref(false)
const roles = ref<AdminRole[]>([])
const departments = ref<Department[]>([])
const requests = ref<RegistrationRequest[]>([])
const notifications = ref<PlatformNotification[]>([])
const integrations = ref<ExternalIntegration[]>([])
const integrationEvents = ref<ExternalIntegrationEvent[]>([])
const allowedDomainsText = ref('')
const testEmail = ref('')
const editingIntegrationId = ref<number | null>(null)
const form = reactive<RegistrationSettings>({
  selfRegistrationEnabled: true,
  emailVerificationRequired: true,
  adminApprovalRequired: false,
  defaultRoleCode: 'STUDENT',
  defaultDepartmentId: null,
  allowedEmailDomains: [],
  termsText: '',
})
const mailStatus = reactive<MailStatus>({
  enabled: false,
  configured: false,
  deliveryMode: 'SMTP',
  from: null,
  host: null,
  port: null,
  message: '未读取',
})
const integrationForm = reactive<ExternalIntegrationPayload>({
  name: '',
  integrationType: 'WEBHOOK',
  endpointUrl: 'https://example.com/kaoshi/webhook',
  secretMask: '',
  enabled: false,
})

const departmentOptions = computed(() => departments.value)
const unreadNotificationCount = computed(() => notifications.value.filter((item) => !item.read).length)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [settings, status, roleList, departmentList, registrationRequests, loadedNotifications, loadedIntegrations, loadedIntegrationEvents] = await Promise.all([
      fetchAdminRegistrationSettings(),
      fetchAdminMailStatus(),
      fetchAdminRoles(),
      fetchDepartments(),
      fetchRegistrationRequests(),
      fetchPlatformNotifications(),
      fetchExternalIntegrations(),
      fetchExternalIntegrationEvents(),
    ])
    Object.assign(form, settings)
    Object.assign(mailStatus, status)
    roles.value = roleList
    departments.value = departmentList
    requests.value = registrationRequests
    notifications.value = loadedNotifications
    integrations.value = loadedIntegrations
    integrationEvents.value = loadedIntegrationEvents
    allowedDomainsText.value = settings.allowedEmailDomains.join(', ')
    if (loadedIntegrations[0] && editingIntegrationId.value === null) {
      editIntegration(loadedIntegrations[0])
    }
  } finally {
    loading.value = false
  }
}

async function readNotification(id: number) {
  await markPlatformNotificationRead(id)
  await loadData()
}

function editIntegration(row: ExternalIntegration) {
  editingIntegrationId.value = row.id
  Object.assign(integrationForm, {
    name: row.name,
    integrationType: row.integrationType,
    endpointUrl: row.endpointUrl,
    secretMask: row.secretMask || '',
    enabled: row.enabled,
  })
}

async function saveIntegration() {
  const saved = editingIntegrationId.value
    ? await updateExternalIntegration(editingIntegrationId.value, integrationForm)
    : await createExternalIntegration(integrationForm)
  editingIntegrationId.value = saved.id
  ElMessage.success('外部集成已保存')
  await loadData()
}

async function testIntegration(id: number) {
  integrationEvents.value = await testExternalIntegration(id)
  ElMessage.success('测试事件已记录')
  await loadData()
}

async function saveSettings() {
  saving.value = true
  try {
    const saved = await updateAdminRegistrationSettings({
      ...form,
      allowedEmailDomains: allowedDomainsText.value.split(',').map((item) => item.trim()).filter(Boolean),
    })
    Object.assign(form, saved)
    allowedDomainsText.value = saved.allowedEmailDomains.join(', ')
    ElMessage.success('注册策略已保存')
  } finally {
    saving.value = false
  }
}

async function sendTest() {
  testingMail.value = true
  try {
    await sendAdminTestMail(testEmail.value)
    ElMessage.success('测试邮件已发送')
  } finally {
    testingMail.value = false
  }
}

async function approve(userId: number) {
  await approveRegistrationRequest(userId)
  ElMessage.success('注册申请已通过')
  await loadData()
}

async function reject(userId: number) {
  const reason = await ElMessageBox.prompt('请输入拒绝原因', '拒绝注册申请', {
    confirmButtonText: '拒绝',
    cancelButtonText: '取消',
    inputValue: '',
  }).then((result) => result.value).catch(() => null)
  if (reason === null) {
    return
  }
  await rejectRegistrationRequest(userId, reason)
  ElMessage.success('注册申请已拒绝')
  await loadData()
}
</script>

<style scoped>
.settings-page {
  display: grid;
  gap: 18px;
}

.page-header,
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h1,
.section-head h2 {
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #6b7280;
}

.settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 18px;
}

.settings-section {
  background: #fff;
  border: 1px solid #e5e7eb;
  padding: 18px;
}

.full-control {
  width: 100%;
}

.test-mail {
  margin-top: 18px;
}

.test-mail__row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.integration-events {
  margin-top: 12px;
}

@media (max-width: 960px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
