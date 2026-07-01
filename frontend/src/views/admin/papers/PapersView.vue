<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>试卷管理</h1>
        <p>维护试卷分类、题目清单、顺序、分值快照和限时。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建试卷</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索试卷或分类" class="toolbar__search" @keyup.enter="loadPapers" />
      <el-button :icon="Search" @click="loadPapers">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="papers" class="data-table" border>
      <el-table-column prop="name" label="试卷名称" min-width="180" />
      <el-table-column prop="categoryName" label="分类" width="150" />
      <el-table-column prop="totalScore" label="总分" width="90" />
      <el-table-column prop="durationMinutes" label="限时" width="100" />
      <el-table-column label="题数" width="90">
        <template #default="{ row }: { row: Paper }">{{ row.questions.length }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }: { row: Paper }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: Paper }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadPapers" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingPaper ? '编辑试卷' : '新建试卷'" width="780px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" class="form-control">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" maxlength="128" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model.trim="form.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="限时" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :min="1" :step="5" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-segmented v-model="form.status" :options="statusOptions" />
        </el-form-item>
        <el-form-item label="题目">
          <div class="question-picker">
            <div v-for="(question, index) in form.questions" :key="`${question.questionId}-${index}`" class="option-row">
              <el-select v-model="question.questionId" filterable class="question-select" placeholder="选择试题">
                <el-option v-for="item in availableQuestions" :key="item.id" :label="item.stem" :value="item.id" />
              </el-select>
              <el-input-number v-model="question.score" :min="0.5" :step="0.5" />
              <el-button :icon="Delete" circle :disabled="form.questions.length <= 1" @click="removeQuestion(index)" />
            </div>
            <el-button :icon="Plus" @click="addQuestion">增加题目</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPaper">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Plus, Search } from '@element-plus/icons-vue'

import {
  createPaper,
  fetchPaperCategories,
  fetchPapers,
  fetchQuestions,
  updatePaper,
  type NamedCategory,
  type Paper,
  type PaperPayload,
  type Question,
} from '@/api/exam-business'

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' },
]

const categories = ref<NamedCategory[]>([])
const papers = ref<Paper[]>([])
const availableQuestions = ref<Question[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingPaper = ref<Paper | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({ page: 1, size: 20, keyword: '' })
const form = reactive<PaperPayload>({
  categoryId: 1,
  name: '',
  description: '',
  durationMinutes: 30,
  status: 'ACTIVE',
  questions: [{ questionId: 1, score: 5 }],
})

const rules: FormRules<PaperPayload> = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '请输入限时', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadQuestions(), loadPapers()])
})

async function loadCategories() {
  categories.value = await fetchPaperCategories()
}

async function loadQuestions() {
  const result = await fetchQuestions({ page: 1, size: 100 })
  availableQuestions.value = result.records
}

async function loadPapers() {
  loading.value = true
  try {
    const result = await fetchPapers({ page: query.page, size: query.size, keyword: query.keyword || undefined })
    papers.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingPaper.value = null
  form.categoryId = categories.value[0]?.id || 1
  form.name = ''
  form.description = ''
  form.durationMinutes = 30
  form.status = 'ACTIVE'
  form.questions = [{ questionId: availableQuestions.value[0]?.id || 1, score: availableQuestions.value[0]?.score || 5 }]
  dialogVisible.value = true
}

function openEditDialog(paper: Paper) {
  editingPaper.value = paper
  form.categoryId = paper.categoryId
  form.name = paper.name
  form.description = paper.description || ''
  form.durationMinutes = paper.durationMinutes
  form.status = paper.status
  form.questions = paper.questions.map((item) => ({ questionId: item.questionId, score: item.score }))
  dialogVisible.value = true
}

function addQuestion() {
  form.questions.push({ questionId: availableQuestions.value[0]?.id || 1, score: availableQuestions.value[0]?.score || 5 })
}

function removeQuestion(index: number) {
  form.questions.splice(index, 1)
}

async function submitPaper() {
  await formRef.value?.validate()
  const ids = form.questions.map((item) => item.questionId)
  if (new Set(ids).size !== ids.length) {
    ElMessage.error('试卷题目不能重复')
    return
  }
  saving.value = true
  try {
    if (editingPaper.value) {
      await updatePaper(editingPaper.value.id, form)
      ElMessage.success('试卷已更新')
    } else {
      await createPaper(form)
      ElMessage.success('试卷已创建')
    }
    dialogVisible.value = false
    await loadPapers()
  } finally {
    saving.value = false
  }
}
</script>
