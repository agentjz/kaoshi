<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>考试中心</h1>
        <p>查看已发布考试并进入作答。</p>
      </div>
      <el-button :icon="Refresh" @click="loadTasks">刷新</el-button>
    </header>

    <el-table v-loading="loading" :data="tasks" class="data-table" border>
      <el-table-column prop="title" label="考试名称" min-width="180" />
      <el-table-column prop="paperName" label="试卷" min-width="160" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="durationMinutes" label="限时" width="90" />
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: Exam }">
          <el-button type="primary" link @click="router.push({ name: 'exam-session', params: { examId: row.id } })">
            开始作答
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

import { fetchExamTasks, type Exam } from '@/api/exam-business'

const router = useRouter()
const tasks = ref<Exam[]>([])
const loading = ref(false)

onMounted(loadTasks)

async function loadTasks() {
  loading.value = true
  try {
    tasks.value = await fetchExamTasks()
  } finally {
    loading.value = false
  }
}
</script>
