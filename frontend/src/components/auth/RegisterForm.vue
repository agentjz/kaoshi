<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
    <div class="login-form__head">
      <h2>注册</h2>
      <p>{{ helperText }}</p>
    </div>
    <el-alert v-if="!settings.selfRegistrationEnabled" title="当前部署未开放自助注册，请联系管理员创建账号。" type="warning" :closable="false" />
    <el-alert v-else-if="!mailStatus.configured" :title="mailStatus.message" type="warning" :closable="false" />
    <template v-else>
      <el-form-item label="邮箱" prop="email">
        <div class="inline-action">
          <el-input v-model.trim="form.email" autocomplete="email" size="large" />
          <el-button :loading="sending" size="large" @click="sendCode">发送验证码</el-button>
        </div>
      </el-form-item>
      <el-form-item label="账号" prop="username">
        <el-input v-model.trim="form.username" autocomplete="username" size="large" />
      </el-form-item>
      <el-form-item label="姓名" prop="displayName">
        <el-input v-model.trim="form.displayName" autocomplete="name" size="large" />
      </el-form-item>
      <el-form-item label="验证码" prop="verificationCode">
        <el-input v-model.trim="form.verificationCode" maxlength="6" size="large" />
      </el-form-item>
      <p v-if="debugCode" class="auth-hint">演示验证码：{{ debugCode }}</p>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" autocomplete="new-password" show-password size="large" type="password" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" autocomplete="new-password" show-password size="large" type="password" @keyup.enter="submit" />
      </el-form-item>
      <el-button class="submit" :loading="submitting" size="large" type="primary" @click="submit">提交注册</el-button>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { computed, reactive, ref } from 'vue'

import type { MailStatus, RegisterPayload, RegistrationSettings } from '@/api/types'

const props = defineProps<{
  settings: RegistrationSettings
  mailStatus: MailStatus
  sending: boolean
  submitting: boolean
}>()
const emit = defineEmits<{ sendCode: [email: string]; submit: [payload: RegisterPayload] }>()

const formRef = ref<FormInstance>()
const debugCode = ref('')
const form = reactive({ email: '', username: '', displayName: '', verificationCode: '', password: '', confirmPassword: '' })
const helperText = computed(() => props.settings.adminApprovalRequired ? '邮箱验证后提交申请，管理员审核通过后可登录。' : '邮箱验证后即可创建考生账号。')

const rules: FormRules<typeof form> = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  username: [{ required: true, min: 3, max: 64, message: '账号长度 3-64 位', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  verificationCode: [{ required: true, len: 6, message: '请输入 6 位验证码', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }],
}

async function sendCode() {
  await formRef.value?.validateField('email')
  emit('sendCode', form.email)
}

function setDebugCode(code: string | null) {
  debugCode.value = code || ''
  ElMessage.success('验证码已发送')
}

async function submit() {
  await formRef.value?.validate()
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  emit('submit', { ...form })
}

defineExpose({ setDebugCode })
</script>
