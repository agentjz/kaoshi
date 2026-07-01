<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>提交结果</h1>
        <p>本次考试已经提交，答案已锁定。</p>
      </div>
      <el-button type="primary" @click="router.push({ name: 'exam-home' })">返回考试中心</el-button>
    </header>

    <section class="metric-grid">
      <article class="metric">
        <span>得分</span>
        <strong>{{ score }}</strong>
      </article>
      <article class="metric">
        <span>总分</span>
        <strong>{{ total }}</strong>
      </article>
      <article class="metric">
        <span>状态</span>
        <strong>已提交</strong>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const score = computed(() => route.query.score || '-')
const total = computed(() => route.query.total || '-')
</script>
