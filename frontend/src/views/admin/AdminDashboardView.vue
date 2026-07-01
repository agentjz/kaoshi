<template>
  <div class="dashboard">
    <section class="hero">
      <div>
        <p class="eyebrow">管理工作台</p>
        <h1>考试管理平台底座</h1>
        <p>这里汇总身份、权限、题库、试卷、考试和成绩等管理能力的入口状态。</p>
      </div>
      <el-tag effect="dark" type="success">已登录</el-tag>
    </section>

    <section class="metric-grid">
      <article class="metric">
        <span>用户</span>
        <strong>{{ auth.user?.displayName }}</strong>
      </article>
      <article class="metric">
        <span>角色</span>
        <strong>{{ auth.user?.roles.join(', ') }}</strong>
      </article>
      <article class="metric">
        <span>权限</span>
        <strong>{{ auth.permissions.length }}</strong>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>
