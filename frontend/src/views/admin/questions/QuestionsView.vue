<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>试题管理</h1>
        <p>维护单选题、多选题、选项、答案、解析和附件元数据。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建试题</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索题干或题库" class="toolbar__search" @keyup.enter="loadQuestions" />
      <el-button :icon="Search" @click="loadQuestions">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="questions" class="data-table" border>
      <el-table-column prop="stem" label="题干" min-width="260" show-overflow-tooltip />
      <el-table-column prop="bankName" label="题库" width="150" />
      <el-table-column label="题型" width="120">
        <template #default="{ row }: { row: Question }">{{ questionTypeText(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="score" label="分值" width="90" />
      <el-table-column label="答案" min-width="140">
        <template #default="{ row }: { row: Question }">
          <el-tag v-for="option in row.options.filter((item) => item.correct)" :key="option.id" class="answer-tag">
            {{ option.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: Question }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadQuestions" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingQuestion ? '编辑试题' : '新建试题'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="题库" prop="bankId">
          <el-select v-model="form.bankId" class="form-control">
            <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="type">
          <el-segmented v-model="form.type" :options="questionTypeOptions" />
        </el-form-item>
        <el-form-item label="题干" prop="stem">
          <el-input v-model.trim="form.stem" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="选项">
          <div class="option-editor">
            <div v-for="(option, index) in form.options" :key="option.label" class="option-row">
              <el-checkbox v-model="option.correct" />
              <el-input v-model.trim="option.label" class="option-label" />
              <el-input v-model.trim="option.content" placeholder="选项内容" />
              <el-button :icon="Delete" circle :disabled="form.options.length <= 2" @click="removeOption(index)" />
            </div>
            <el-button :icon="Plus" @click="addOption">增加选项</el-button>
          </div>
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model.trim="form.analysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="附件 URL">
          <el-input v-model.trim="attachmentUrl" placeholder="可选，例如 /assets/audio.mp3" />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="0.5" :step="0.5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitQuestion">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Plus, Search } from '@element-plus/icons-vue'

import {
  createQuestion,
  fetchQuestionBanks,
  fetchQuestions,
  updateQuestion,
  type Question,
  type QuestionBank,
  type QuestionPayload,
} from '@/api/exam-business'

const questionTypeOptions = [
  { label: '单选', value: 'SINGLE_CHOICE' },
  { label: '多选', value: 'MULTIPLE_CHOICE' },
]

const banks = ref<QuestionBank[]>([])
const questions = ref<Question[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingQuestion = ref<Question | null>(null)
const attachmentUrl = ref('')
const formRef = ref<FormInstance>()

const query = reactive({ page: 1, size: 20, keyword: '' })
const form = reactive<QuestionPayload>({
  bankId: 1,
  type: 'SINGLE_CHOICE',
  stem: '',
  analysis: '',
  score: 5,
  difficulty: 'EASY',
  status: 'ACTIVE',
  options: [
    { label: 'A', content: '', correct: true },
    { label: 'B', content: '', correct: false },
  ],
  attachments: [],
})

const rules: FormRules<QuestionPayload> = {
  bankId: [{ required: true, message: '请选择题库', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  stem: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadBanks(), loadQuestions()])
})

async function loadBanks() {
  const result = await fetchQuestionBanks({ page: 1, size: 100 })
  banks.value = result.records
}

async function loadQuestions() {
  loading.value = true
  try {
    const result = await fetchQuestions({ page: query.page, size: query.size, keyword: query.keyword || undefined })
    questions.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingQuestion.value = null
  form.bankId = banks.value[0]?.id || 1
  form.type = 'SINGLE_CHOICE'
  form.stem = ''
  form.analysis = ''
  form.score = 5
  form.options = [
    { label: 'A', content: '', correct: true },
    { label: 'B', content: '', correct: false },
  ]
  attachmentUrl.value = ''
  dialogVisible.value = true
}

function openEditDialog(question: Question) {
  editingQuestion.value = question
  form.bankId = question.bankId
  form.type = question.type
  form.stem = question.stem
  form.analysis = question.analysis || ''
  form.score = question.score
  form.options = question.options.map((option) => ({ label: option.label, content: option.content, correct: option.correct }))
  attachmentUrl.value = question.attachments[0]?.fileUrl || ''
  dialogVisible.value = true
}

function addOption() {
  const label = String.fromCharCode(65 + form.options.length)
  form.options.push({ label, content: '', correct: false })
}

function removeOption(index: number) {
  form.options.splice(index, 1)
}

async function submitQuestion() {
  await formRef.value?.validate()
  const correctCount = form.options.filter((option) => option.correct).length
  if (form.type === 'SINGLE_CHOICE' && correctCount !== 1) {
    ElMessage.error('单选题必须且只能有一个正确答案')
    return
  }
  if (form.type === 'MULTIPLE_CHOICE' && correctCount < 2) {
    ElMessage.error('多选题至少需要两个正确答案')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      attachments: attachmentUrl.value
        ? [{ fileName: attachmentUrl.value.split('/').pop() || 'attachment', fileUrl: attachmentUrl.value, mediaType: 'FILE' }]
        : [],
    }
    if (editingQuestion.value) {
      await updateQuestion(editingQuestion.value.id, payload)
      ElMessage.success('试题已更新')
    } else {
      await createQuestion(payload)
      ElMessage.success('试题已创建')
    }
    dialogVisible.value = false
    await loadQuestions()
  } finally {
    saving.value = false
  }
}

function questionTypeText(type: Question['type']) {
  return type === 'SINGLE_CHOICE' ? '单选' : '多选'
}
</script>
