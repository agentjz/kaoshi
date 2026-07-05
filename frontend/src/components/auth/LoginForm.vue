<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
    <div class="login-form__head">
      <h2>登录</h2>
      <p>使用管理员创建、导入或自助注册后的账号进入平台。</p>
    </div>
    <el-form-item label="账号" prop="username">
      <el-input v-model.trim="form.username" autocomplete="username" size="large" />
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="form.password" autocomplete="current-password" show-password size="large" type="password" @keyup.enter="submit" />
    </el-form-item>
    <el-button class="submit" :loading="loading" size="large" type="primary" @click="submit">进入平台</el-button>
  </el-form>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'

defineProps<{ loading: boolean }>()
const emit = defineEmits<{ submit: [payload: { username: string; password: string }] }>()
const formRef = ref<FormInstance>()
const form = reactive({ username: 'admin', password: 'password' })

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  emit('submit', { ...form })
}
</script>
