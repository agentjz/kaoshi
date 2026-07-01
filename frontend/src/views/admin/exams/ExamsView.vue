<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>考试管理</h1>
        <p>发布考试、维护考试时间、限时和状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建考试</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索考试名称" class="toolbar__search" @keyup.enter="loadExams" />
      <el-button :icon="Search" @click="loadExams">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="exams" class="data-table" border>
      <el-table-column prop="title" label="考试名称" min-width="180" />
      <el-table-column prop="paperName" label="试卷" min-width="160" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="durationMinutes" label="限时" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }: { row: Exam }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: Exam }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadExams" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingExam ? '编辑考试' : '新建考试'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="试卷" prop="paperId">
          <el-select v-model="form.paperId" filterable class="form-control">
            <el-option v-for="paper in papers" :key="paper.id" :label="paper.name" :value="paper.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="title">
          <el-input v-model.trim="form.title" maxlength="128" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model.trim="form.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="时间" required>
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="form-control"
          />
        </el-form-item>
        <el-form-item label="限时" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :min="1" :step="5" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-segmented v-model="form.status" :options="statusOptions" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitExamForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

import {
  createExam,
  fetchAdminExams,
  fetchPapers,
  updateExam,
  type Exam,
  type ExamPayload,
  type Paper,
} from '@/api/exam-business'

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '发布', value: 'PUBLISHED' },
  { label: '关闭', value: 'CLOSED' },
]

const papers = ref<Paper[]>([])
const exams = ref<Exam[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingExam = ref<Exam | null>(null)
const formRef = ref<FormInstance>()
const timeRange = ref<[string, string]>(['2026-01-01T00:00:00', '2026-12-31T23:59:59'])

const query = reactive({ page: 1, size: 20, keyword: '' })
const form = reactive<ExamPayload>({
  paperId: 1,
  title: '',
  description: '',
  startTime: '2026-01-01T00:00:00',
  endTime: '2026-12-31T23:59:59',
  durationMinutes: 30,
  status: 'PUBLISHED',
})

const rules: FormRules<ExamPayload> = {
  paperId: [{ required: true, message: '请选择试卷', trigger: 'change' }],
  title: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '请输入限时', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadPapers(), loadExams()])
})

async function loadPapers() {
  const result = await fetchPapers({ page: 1, size: 100 })
  papers.value = result.records
}

async function loadExams() {
  loading.value = true
  try {
    const result = await fetchAdminExams({ page: query.page, size: query.size, keyword: query.keyword || undefined })
    exams.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingExam.value = null
  form.paperId = papers.value[0]?.id || 1
  form.title = ''
  form.description = ''
  form.durationMinutes = papers.value[0]?.durationMinutes || 30
  form.status = 'PUBLISHED'
  timeRange.value = ['2026-01-01T00:00:00', '2026-12-31T23:59:59']
  dialogVisible.value = true
}

function openEditDialog(exam: Exam) {
  editingExam.value = exam
  form.paperId = exam.paperId
  form.title = exam.title
  form.description = exam.description || ''
  form.durationMinutes = exam.durationMinutes
  form.status = exam.status
  timeRange.value = [exam.startTime, exam.endTime]
  dialogVisible.value = true
}

async function submitExamForm() {
  await formRef.value?.validate()
  if (!timeRange.value?.[0] || !timeRange.value?.[1]) {
    ElMessage.error('请选择考试时间')
    return
  }
  form.startTime = timeRange.value[0]
  form.endTime = timeRange.value[1]
  saving.value = true
  try {
    if (editingExam.value) {
      await updateExam(editingExam.value.id, form)
      ElMessage.success('考试已更新')
    } else {
      await createExam(form)
      ElMessage.success('考试已创建')
    }
    dialogVisible.value = false
    await loadExams()
  } finally {
    saving.value = false
  }
}

function statusText(status: Exam['status']) {
  return status === 'PUBLISHED' ? '已发布' : status === 'CLOSED' ? '已关闭' : '草稿'
}

function statusType(status: Exam['status']) {
  return status === 'PUBLISHED' ? 'success' : status === 'CLOSED' ? 'info' : 'warning'
}
</script>
