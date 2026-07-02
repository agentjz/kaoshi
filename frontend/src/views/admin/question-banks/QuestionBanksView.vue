<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <h1>题库管理</h1>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建题库</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索题库或分类" class="toolbar__search" @keyup.enter="loadBanks" />
      <el-button :icon="Search" @click="loadBanks">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="banks" class="data-table" border>
      <el-table-column prop="name" label="题库名称" min-width="180" />
      <el-table-column prop="categoryName" label="分类" width="160" />
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="110">
        <template #default="{ row }: { row: QuestionBank }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }: { row: QuestionBank }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadBanks"
        @current-change="loadBanks"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingBank ? '编辑题库' : '新建题库'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" class="form-control">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" maxlength="128" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model.trim="form.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-segmented v-model="form.status" :options="statusOptions" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitBank">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

import {
  createQuestionBank,
  fetchQuestionBanks,
  fetchQuestionCategories,
  updateQuestionBank,
  type NamedCategory,
  type QuestionBank,
  type QuestionBankPayload,
} from '@/api/exam-business'

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' },
]

const categories = ref<NamedCategory[]>([])
const banks = ref<QuestionBank[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingBank = ref<QuestionBank | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({ page: 1, size: 20, keyword: '' })
const form = reactive<QuestionBankPayload>({ categoryId: 1, name: '', description: '', status: 'ACTIVE' })

const rules: FormRules<QuestionBankPayload> = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入题库名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadBanks()])
})

async function loadCategories() {
  categories.value = await fetchQuestionCategories()
}

async function loadBanks() {
  loading.value = true
  try {
    const result = await fetchQuestionBanks({ page: query.page, size: query.size, keyword: query.keyword || undefined })
    banks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingBank.value = null
  form.categoryId = categories.value[0]?.id || 1
  form.name = ''
  form.description = ''
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

function openEditDialog(bank: QuestionBank) {
  editingBank.value = bank
  form.categoryId = bank.categoryId
  form.name = bank.name
  form.description = bank.description || ''
  form.status = bank.status
  dialogVisible.value = true
}

async function submitBank() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingBank.value) {
      await updateQuestionBank(editingBank.value.id, form)
      ElMessage.success('题库已更新')
    } else {
      await createQuestionBank(form)
      ElMessage.success('题库已创建')
    }
    dialogVisible.value = false
    await loadBanks()
  } finally {
    saving.value = false
  }
}
</script>
