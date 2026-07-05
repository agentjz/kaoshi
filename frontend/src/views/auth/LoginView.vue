<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-copy">
        <span class="auth-copy__eyebrow">kaoshi</span>
        <h1>现代化考试与考试管理平台</h1>
        <p>覆盖题库、试卷、发布快照、在线作答、评分阅卷、成绩复盘和账号生命周期。</p>
        <dl>
          <div>
            <dt>账号入口</dt>
            <dd>管理员导入/创建账号与邮箱自助注册并行。</dd>
          </div>
          <div>
            <dt>身份安全</dt>
            <dd>邮箱验证码、注册审批和找回密码由当前部署策略控制。</dd>
          </div>
        </dl>
      </div>

      <section class="auth-card" v-loading="loading">
        <div class="auth-card__head">
          <h2>{{ activeTitle }}</h2>
          <p>{{ activeDescription }}</p>
        </div>
        <el-tabs v-model="activeTab" stretch>
          <el-tab-pane label="登录" name="login">
            <LoginForm :loading="auth.loading" @submit="submitLogin" />
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <RegisterForm
              ref="registerFormRef"
              :settings="settings"
              :mail-status="mailStatus"
              :sending="sendingRegisterCode"
              :submitting="submittingRegister"
              @send-code="sendRegisterCode"
              @submit="submitRegister"
            />
          </el-tab-pane>
          <el-tab-pane label="找回密码" name="forgot">
            <ForgotPasswordForm
              ref="forgotFormRef"
              :mail-status="mailStatus"
              :sending="sendingResetCode"
              :submitting="submittingReset"
              @send-code="sendResetCode"
              @submit="submitResetPassword"
            />
          </el-tab-pane>
        </el-tabs>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import {
  fetchMailStatus,
  fetchRegistrationSettings,
  register,
  resetPassword,
  sendPasswordResetCode,
  sendVerificationCode,
} from '@/api/auth'
import type { MailStatus, RegisterPayload, RegistrationSettings, ResetPasswordPayload } from '@/api/types'
import ForgotPasswordForm from '@/components/auth/ForgotPasswordForm.vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import RegisterForm from '@/components/auth/RegisterForm.vue'
import { useAuthStore } from '@/stores/auth'

type AuthTab = 'login' | 'register' | 'forgot'

const activeTab = ref<AuthTab>('login')
const loading = ref(true)
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const sendingRegisterCode = ref(false)
const submittingRegister = ref(false)
const sendingResetCode = ref(false)
const submittingReset = ref(false)
const registerFormRef = ref<InstanceType<typeof RegisterForm>>()
const forgotFormRef = ref<InstanceType<typeof ForgotPasswordForm>>()
const settings = ref<RegistrationSettings>({
  selfRegistrationEnabled: false,
  emailVerificationRequired: true,
  adminApprovalRequired: false,
  defaultRoleCode: 'STUDENT',
  defaultDepartmentId: null,
  allowedEmailDomains: [],
  termsText: '',
})
const mailStatus = ref<MailStatus>({
  enabled: false,
  configured: false,
  deliveryMode: 'SMTP',
  from: null,
  host: null,
  port: null,
  message: '正在读取邮件服务状态',
})

const activeTitle = computed(() => {
  if (activeTab.value === 'register') {
    return '注册账号'
  }
  if (activeTab.value === 'forgot') {
    return '找回密码'
  }
  return '登录平台'
})

const activeDescription = computed(() => {
  if (activeTab.value === 'register') {
    return settings.value.adminApprovalRequired ? '提交注册后需要管理员审核。' : '通过邮箱验证码后即可完成注册。'
  }
  if (activeTab.value === 'forgot') {
    return '通过绑定邮箱验证码重置登录密码。'
  }
  return '使用账号密码进入考试中心或管理端。'
})

onMounted(async () => {
  try {
    const [loadedSettings, loadedMailStatus] = await Promise.all([fetchRegistrationSettings(), fetchMailStatus()])
    settings.value = loadedSettings
    mailStatus.value = loadedMailStatus
  } finally {
    loading.value = false
  }
})

async function submitLogin(payload: { username: string; password: string }) {
  await auth.login(payload)
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
  await router.replace(redirect)
}

async function sendRegisterCode(email: string) {
  sendingRegisterCode.value = true
  try {
    const result = await sendVerificationCode({ email, purpose: 'REGISTER' })
    registerFormRef.value?.setDebugCode(result.debugCode)
  } finally {
    sendingRegisterCode.value = false
  }
}

async function submitRegister(payload: RegisterPayload) {
  submittingRegister.value = true
  try {
    const result = await register(payload)
    ElMessage.success(result.message)
    activeTab.value = 'login'
  } finally {
    submittingRegister.value = false
  }
}

async function sendResetCode(email: string) {
  sendingResetCode.value = true
  try {
    const result = await sendPasswordResetCode(email)
    forgotFormRef.value?.setDebugCode(result.debugCode)
  } finally {
    sendingResetCode.value = false
  }
}

async function submitResetPassword(payload: ResetPasswordPayload) {
  submittingReset.value = true
  try {
    await resetPassword(payload)
    ElMessage.success('密码已重置，请使用新密码登录')
    activeTab.value = 'login'
  } finally {
    submittingReset.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: #f4f7fb;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.auth-panel {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 460px;
  gap: 32px;
  align-items: stretch;
}

.auth-copy {
  background: #17324d;
  color: #fff;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-copy__eyebrow {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
}

.auth-copy h1 {
  margin: 18px 0 16px;
  font-size: 36px;
  line-height: 1.2;
}

.auth-copy p {
  margin: 0;
  max-width: 560px;
  color: #dbe7f3;
  line-height: 1.8;
}

.auth-copy dl {
  margin: 40px 0 0;
  display: grid;
  gap: 18px;
}

.auth-copy dt {
  font-weight: 700;
  margin-bottom: 6px;
}

.auth-copy dd {
  margin: 0;
  color: #dbe7f3;
  line-height: 1.7;
}

.auth-card {
  background: #fff;
  padding: 30px;
  box-shadow: 0 18px 45px rgb(23 50 77 / 12%);
}

.auth-card__head {
  margin-bottom: 16px;
}

.auth-card__head h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.auth-card__head p {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
}

:deep(.auth-form) {
  padding-top: 8px;
}

:deep(.auth-submit) {
  width: 100%;
}

:deep(.auth-stack) {
  display: grid;
  gap: 14px;
}

:deep(.code-row) {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

@media (max-width: 900px) {
  .auth-page {
    padding: 16px;
    align-items: stretch;
  }

  .auth-panel {
    grid-template-columns: 1fr;
  }

  .auth-copy {
    padding: 28px;
  }

  .auth-copy h1 {
    font-size: 28px;
  }

  .auth-card {
    padding: 22px;
  }
}

@media (max-width: 520px) {
  :deep(.code-row) {
    grid-template-columns: 1fr;
  }
}
</style>
