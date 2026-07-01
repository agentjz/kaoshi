<template>
  <el-container class="exam-shell">
    <el-header class="exam-header">
      <div>
        <strong>考试端</strong>
        <span>稳定、专注、可追踪的在线考试入口</span>
      </div>
      <el-button text @click="router.push('/admin')">返回管理端</el-button>
    </el-header>
    <el-main class="exam-main">
      <RouterView />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

