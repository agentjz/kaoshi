<template>
  <section class="admin-page" v-loading="loading">
    <header class="admin-page__header">
      <div>
        <h1>{{ result?.examTitle || '提交结果' }}</h1>
        <p>本次考试已经提交，答案已锁定。</p>
      </div>
      <el-button type="primary" @click="router.push({ name: 'exam-home' })">返回考试中心</el-button>
    </header>

    <ExamResultReview v-if="result" :result="result" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { fetchMyExamResultDetail, type ExamResultDetail } from '@/api/exam-business'
import ExamResultReview from '@/components/exam/ExamResultReview.vue'

const route = useRoute()
const router = useRouter()
const result = ref<ExamResultDetail | null>(null)
const loading = ref(false)

onMounted(loadResult)

async function loadResult() {
  const resultId = Number(route.params.resultId)
  if (!Number.isFinite(resultId)) {
    ElMessage.error('成绩不存在')
    await router.replace({ name: 'exam-home' })
    return
  }
  loading.value = true
  try {
    result.value = await fetchMyExamResultDetail(resultId)
  } finally {
    loading.value = false
  }
}

</script>
