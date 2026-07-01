<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>成绩管理</h1>
        <p>查看考试提交后的成绩归档。</p>
      </div>
      <el-button :icon="Refresh" @click="loadResults">刷新</el-button>
    </header>

    <el-table v-loading="loading" :data="results" class="data-table" border>
      <el-table-column prop="examTitle" label="考试" min-width="180" />
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column prop="obtainedScore" label="得分" width="100" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column label="正确题数" width="120">
        <template #default="{ row }: { row: ExamResult }">{{ row.correctCount }} / {{ row.questionCount }}</template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" min-width="180" />
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'

import { fetchAdminResults, type ExamResult } from '@/api/exam-business'

const results = ref<ExamResult[]>([])
const loading = ref(false)

onMounted(loadResults)

async function loadResults() {
  loading.value = true
  try {
    results.value = await fetchAdminResults()
  } finally {
    loading.value = false
  }
}
</script>
