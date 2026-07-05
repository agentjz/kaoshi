<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
    <div class="login-form__head">
      <h2>找回密码</h2>
      <p>通过账号绑定邮箱接收验证码后重置密码。</p>
    </div>
    <el-alert v-if="!mailStatus.configured" :title="mailStatus.message" type="warning" :closable="false" />
    <template v-else>
      <el-form-item label="邮箱" prop="email">
        <div class="inline-action">
          <el-input v-model.trim="form.email" autocomplete="email" size="large" />
          <el-button :loading="sending" size="large" @click="sendCode">发送验证码</el-button>
        </div>
      </el-form-item>
      <el-form-item label="验证码" prop="code">
        <el-input v-model.trim="form.code" maxlength="6" size="large" />
      </el-form-item>
      <p v-if="debugCode" class="auth-hint">演示验证码：{{ debugCode }}</p>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" autocomplete="new-password" show-password size="large" type="password" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" autocomplete="new-password" show-password size="large" type="password" @keyup.enter="submit" />
      </el-form-item>
      <el-button class="submit" :loading="submitting" size="large" type="primary" @click="submit">重置密码</el-button>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'

import type { MailStatus, ResetPasswordPayload } from '@/api/types'

defineProps<{ mailStatus: MailStatus; sending: boolean; submitting: boolean }>()
const emit = defineEmits<{ sendCode: [email: string]; submit: [payload: ResetPasswordPayload] }>()
const formRef = ref<FormInstance>()
const debugCode = ref('')
const form = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })

const rules: FormRules<typeof form> = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, len: 6, message: '请输入 6 位验证码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
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
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  emit('submit', { ...form })
}

defineExpose({ setDebugCode })
</script>
