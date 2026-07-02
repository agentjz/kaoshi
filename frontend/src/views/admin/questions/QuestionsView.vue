<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>试题管理</h1>
      </div>
      <div class="header-actions">
        <el-button :icon="Download" @click="downloadTemplate">下载模板</el-button>
        <el-upload :show-file-list="false" accept=".xlsx" :before-upload="handleImport">
          <el-button :icon="Upload">导入试题</el-button>
        </el-upload>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建试题</el-button>
      </div>
    </header>

    <el-alert v-if="importResult" :type="importResult.failureCount ? 'warning' : 'success'" show-icon :closable="false">
      <template #title>
        导入完成：成功 {{ importResult.successCount }} 条，失败 {{ importResult.failureCount }} 条
      </template>
      <ul v-if="importResult.errors.length" class="import-errors">
        <li v-for="error in importResult.errors" :key="error">{{ error }}</li>
      </ul>
    </el-alert>

    <div class="toolbar">
      <el-select v-model="query.bankId" clearable filterable placeholder="按题库筛选" class="toolbar__select" @change="loadQuestions">
        <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
      </el-select>
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索题干或题库" class="toolbar__search" @keyup.enter="loadQuestions" />
      <el-button :icon="Search" @click="loadQuestions">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="questions" class="data-table" border>
      <el-table-column prop="stem" label="题干" min-width="260" show-overflow-tooltip />
      <el-table-column prop="bankName" label="题库" width="150" />
      <el-table-column label="题型" width="120">
        <template #default="{ row }: { row: Question }">{{ questionTypeText(row.type) }}</template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editingQuestion ? '编辑试题' : '新建试题'" width="860px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="题库" prop="bankId">
          <el-select v-model="form.bankId" class="form-control">
            <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="type">
          <el-segmented v-model="form.type" :options="questionTypeOptions" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" class="form-control">
            <el-option label="简单" value="EASY" />
            <el-option label="困难" value="HARD" />
          </el-select>
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
        <el-form-item label="附件">
          <div class="attachment-editor">
            <el-upload :show-file-list="false" :before-upload="handleAttachmentUpload" accept=".jpg,.jpeg,.png,.gif,.webp,.mp3,.wav,.ogg,.mp4,.pdf">
              <el-button :icon="Upload" :loading="uploadingAttachment">上传附件</el-button>
            </el-upload>
            <div class="url-attachment">
              <el-input v-model.trim="attachmentUrl" placeholder="输入图片、音频、视频或文件 URL" />
              <el-select v-model="attachmentMediaType" class="url-attachment__type">
                <el-option label="图片" value="IMAGE" />
                <el-option label="音频" value="AUDIO" />
                <el-option label="视频" value="VIDEO" />
                <el-option label="文件" value="FILE" />
              </el-select>
              <el-button @click="addUrlAttachment">添加 URL</el-button>
            </div>
            <div v-if="form.attachments.length" class="attachment-list">
              <div v-for="(attachment, index) in form.attachments" :key="`${attachment.fileUrl}-${index}`" class="attachment-item">
                <div class="attachment-item__main">
                  <el-tag effect="plain">{{ mediaTypeText(attachment.mediaType) }}</el-tag>
                  <span>{{ attachment.fileName }}</span>
                  <a :href="attachment.fileUrl" target="_blank" rel="noreferrer">{{ attachment.fileUrl }}</a>
                </div>
                <el-button :icon="Delete" circle @click="removeAttachment(index)" />
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button value="ACTIVE">启用</el-radio-button>
            <el-radio-button value="DISABLED">禁用</el-radio-button>
          </el-radio-group>
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
import { ElMessage, type FormInstance, type FormRules, type UploadRawFile } from 'element-plus'
import { Delete, Download, Plus, Search, Upload } from '@element-plus/icons-vue'

import {
  createQuestion,
  downloadQuestionImportTemplate,
  fetchQuestionBanks,
  fetchQuestions,
  importQuestions,
  updateQuestion,
  uploadFile,
  type Question,
  type QuestionBank,
  type QuestionAttachmentPayload,
  type QuestionPayload,
} from '@/api/exam-business'
import type { ExcelImportResult } from '@/api/admin'
import { downloadBlob } from '@/utils/download'

const questionTypeOptions = [
  { label: '单选', value: 'SINGLE_CHOICE' },
  { label: '多选', value: 'MULTIPLE_CHOICE' },
]

const banks = ref<QuestionBank[]>([])
const questions = ref<Question[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const uploadingAttachment = ref(false)
const dialogVisible = ref(false)
const editingQuestion = ref<Question | null>(null)
const importResult = ref<ExcelImportResult | null>(null)
const attachmentUrl = ref('')
const attachmentMediaType = ref<QuestionAttachmentPayload['mediaType']>('FILE')
const formRef = ref<FormInstance>()

const query = reactive({ page: 1, size: 20, keyword: '', bankId: undefined as number | undefined })
const form = reactive<QuestionPayload>({
  bankId: 1,
  type: 'SINGLE_CHOICE',
  stem: '',
  analysis: '',
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
    const result = await fetchQuestions({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      bankId: query.bankId,
    })
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
  form.difficulty = 'EASY'
  form.status = 'ACTIVE'
  form.options = [
    { label: 'A', content: '', correct: true },
    { label: 'B', content: '', correct: false },
  ]
  form.attachments = []
  attachmentUrl.value = ''
  attachmentMediaType.value = 'FILE'
  dialogVisible.value = true
}

function openEditDialog(question: Question) {
  editingQuestion.value = question
  form.bankId = question.bankId
  form.type = question.type
  form.stem = question.stem
  form.analysis = question.analysis || ''
  form.difficulty = question.difficulty
  form.status = question.status
  form.options = question.options.map((option) => ({ label: option.label, content: option.content, correct: option.correct }))
  form.attachments = question.attachments.map((attachment) => ({
    fileName: attachment.fileName,
    fileUrl: attachment.fileUrl,
    mediaType: attachment.mediaType,
  }))
  attachmentUrl.value = ''
  attachmentMediaType.value = 'FILE'
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
    const payload = { ...form, attachments: [...form.attachments] }
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

async function downloadTemplate() {
  const blob = await downloadQuestionImportTemplate()
  downloadBlob(blob, '试题导入模板.xlsx')
}

async function handleImport(file: UploadRawFile) {
  importResult.value = await importQuestions(file)
  await loadQuestions()
  return false
}

async function handleAttachmentUpload(file: UploadRawFile) {
  uploadingAttachment.value = true
  try {
    form.attachments.push(await uploadFile(file))
    ElMessage.success('附件已上传')
  } finally {
    uploadingAttachment.value = false
  }
  return false
}

function addUrlAttachment() {
  if (!attachmentUrl.value) {
    ElMessage.error('请输入附件 URL')
    return
  }
  form.attachments.push({
    fileName: attachmentUrl.value.split('/').pop() || 'attachment',
    fileUrl: attachmentUrl.value,
    mediaType: attachmentMediaType.value,
  })
  attachmentUrl.value = ''
  attachmentMediaType.value = 'FILE'
}

function removeAttachment(index: number) {
  form.attachments.splice(index, 1)
}

function questionTypeText(type: Question['type']) {
  return type === 'SINGLE_CHOICE' ? '单选' : '多选'
}

function mediaTypeText(type: QuestionAttachmentPayload['mediaType']) {
  const names: Record<QuestionAttachmentPayload['mediaType'], string> = {
    IMAGE: '图片',
    AUDIO: '音频',
    VIDEO: '视频',
    FILE: '文件',
  }
  return names[type]
}
</script>

<style scoped>
.import-errors {
  margin: 8px 0 0;
  padding-left: 18px;
}

.attachment-editor {
  display: grid;
  gap: 10px;
  width: 100%;
}

.url-attachment {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 120px auto;
  gap: 10px;
  align-items: center;
}

.url-attachment__type {
  width: 120px;
}

.attachment-list {
  display: grid;
  gap: 8px;
}

.attachment-item {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
}

.attachment-item__main {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.attachment-item__main span,
.attachment-item__main a {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-item__main a {
  max-width: 320px;
  color: var(--el-color-primary);
}

@media (max-width: 720px) {
  .url-attachment {
    grid-template-columns: 1fr;
  }

  .attachment-item,
  .attachment-item__main {
    align-items: flex-start;
  }

  .attachment-item__main {
    flex-direction: column;
  }
}
</style>
