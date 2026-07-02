<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>我的成绩</h1>
      </div>
      <el-button :icon="Refresh" @click="loadResults">刷新</el-button>
    </header>

    <el-table v-loading="loading" :data="results" class="data-table" border>
      <el-table-column prop="examTitle" label="考试" min-width="180" />
      <el-table-column prop="obtainedScore" label="得分" width="100" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column label="正确题数" width="120">
        <template #default="{ row }: { row: ExamResult }">{{ row.correctCount }} / {{ row.questionCount }}</template>
      </el-table-column>
      <el-table-column label="提交时间" min-width="180">
        <template #default="{ row }: { row: ExamResult }">{{ formatDateTime(row.submittedAt) }}</template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: ExamResult }">
          <el-button type="primary" link @click="router.push({ name: 'exam-result', params: { resultId: row.id } })">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import { fetchMyExamResults, type ExamResult } from '@/api/exam-business'
import { formatDateTime } from '@/utils/datetime'

const router = useRouter()
const results = ref<ExamResult[]>([])
const loading = ref(false)

onMounted(loadResults)

async function loadResults() {
  loading.value = true
  try {
    results.value = await fetchMyExamResults()
  } finally {
    loading.value = false
  }
}
</script>
