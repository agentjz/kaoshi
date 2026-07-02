<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <span class="eyebrow">考试</span>
        <h1>考试与考试管理平台</h1>
        <p>统一管理身份、权限、题库、试卷、考试、作答、评分和成绩归档。</p>
      </div>

      <el-form ref="formRef" class="login-form" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <h2>登录</h2>
        <el-form-item label="账号" prop="username">
          <el-input v-model.trim="form.username" autocomplete="username" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            autocomplete="current-password"
            show-password
            size="large"
            type="password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button class="submit" :loading="auth.loading" size="large" type="primary" @click="submit">
          进入平台
        </el-button>
        <p class="seed">本地种子账号：admin / password</p>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()

const form = reactive({
  username: 'admin',
  password: 'password',
})

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  await auth.login(form)
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
  await router.replace(redirect)
}
</script>

