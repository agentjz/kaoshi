import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormRules } from 'element-plus'

import {
  createQuestionBank,
  createQuestionCategory,
  deleteQuestionCategory,
  fetchQuestionBanks,
  fetchQuestionCategories,
  updateQuestionBank,
  updateQuestionCategory,
  type NamedCategory,
  type QuestionBank,
  type QuestionBankPayload,
  type QuestionCategoryPayload,
} from '@/api/exam-business'
import { buildBankTree, type BankTreeNode } from '@/utils/question-bank-editor'

export function useQuestionBankCatalog(refreshQuestions: () => Promise<void>) {
  const categories = ref<NamedCategory[]>([])
  const banks = ref<QuestionBank[]>([])
  const selectedCategoryId = ref<number | null>(null)
  const selectedBankId = ref<number | null>(null)
  const bankLoading = ref(false)
  const bankSaving = ref(false)
  const categorySaving = ref(false)
  const categoryDialogVisible = ref(false)
  const bankDialogVisible = ref(false)
  const editingCategory = ref<NamedCategory | null>(null)
  const editingBank = ref<QuestionBank | null>(null)
  const bankTreeRef = ref()
  const bankKeyword = ref('')

  const categoryForm = reactive<QuestionCategoryPayload>({ name: '', description: '', sortOrder: 0 })
  const bankForm = reactive<QuestionBankPayload>({ categoryId: 1, name: '', description: '', status: 'ACTIVE' })

  const bankRules: FormRules<QuestionBankPayload> = {
    categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
    name: [{ required: true, message: '请输入题库名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  }

  const categoryRules: FormRules<QuestionCategoryPayload> = {
    name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  }

  const selectedBank = computed(() => banks.value.find((bank) => bank.id === selectedBankId.value) || null)
  const selectedCategory = computed(() => categories.value.find((category) => category.id === selectedCategoryId.value) || null)
  const bankQuestionTotal = computed(() => banks.value.reduce((sum, bank) => sum + bank.questionCount, 0))
  const bankTree = computed<BankTreeNode[]>(() => buildBankTree(categories.value, banks.value))

  async function loadCategories() {
    categories.value = await fetchQuestionCategories()
    if (selectedCategoryId.value && !categories.value.some((category) => category.id === selectedCategoryId.value)) {
      selectedCategoryId.value = null
    }
  }

  async function loadBanks(preferredBankId = selectedBankId.value) {
    bankLoading.value = true
    try {
      const result = await fetchQuestionBanks({ page: 1, size: 500, keyword: bankKeyword.value || undefined })
      banks.value = result.records
      if (preferredBankId && banks.value.some((bank) => bank.id === preferredBankId)) {
        selectedBankId.value = preferredBankId
        selectedCategoryId.value = banks.value.find((bank) => bank.id === preferredBankId)?.categoryId || selectedCategoryId.value
      } else if (selectedBankId.value && !banks.value.some((bank) => bank.id === selectedBankId.value)) {
        selectedBankId.value = null
      }
      await nextTick()
      setCurrentTreeNode()
    } finally {
      bankLoading.value = false
    }
  }

  async function selectBankNode(node: BankTreeNode) {
    if (node.type === 'category' && node.categoryId) {
      selectedCategoryId.value = node.categoryId
      selectedBankId.value = null
      await refreshQuestions()
      return
    }
    if (node.type !== 'bank' || !node.bankId) {
      return
    }
    selectedBankId.value = node.bankId
    selectedCategoryId.value = node.categoryId || banks.value.find((bank) => bank.id === node.bankId)?.categoryId || null
    await refreshQuestions()
  }

  async function clearSelectedBank() {
    selectedBankId.value = null
    selectedCategoryId.value = null
    bankTreeRef.value?.setCurrentKey(null)
    await refreshQuestions()
  }

  function openCreateCategoryDialog() {
    editingCategory.value = null
    categoryForm.name = ''
    categoryForm.description = ''
    categoryForm.sortOrder = nextCategorySortOrder()
    categoryDialogVisible.value = true
  }

  function openEditCategoryDialog(categoryId: number) {
    const category = categories.value.find((item) => item.id === categoryId)
    if (!category) {
      return
    }
    editingCategory.value = category
    categoryForm.name = category.name
    categoryForm.description = category.description || ''
    categoryForm.sortOrder = category.sortOrder
    categoryDialogVisible.value = true
  }

  async function submitCategory(validate: () => Promise<boolean> | undefined) {
    await validate()
    categorySaving.value = true
    try {
      const saved = editingCategory.value
        ? await updateQuestionCategory(editingCategory.value.id, categoryForm)
        : await createQuestionCategory(categoryForm)
      ElMessage.success(editingCategory.value ? '分类已更新' : '分类已创建')
      categoryDialogVisible.value = false
      selectedCategoryId.value = saved.id
      selectedBankId.value = null
      await loadCategories()
      await loadBanks(null)
      await refreshQuestions()
    } finally {
      categorySaving.value = false
    }
  }

  async function deleteCategory(categoryId: number) {
    const category = categories.value.find((item) => item.id === categoryId)
    if (!category) {
      return
    }
    await ElMessageBox.confirm(`确定删除分类“${category.name}”？只有空分类可以删除。`, '删除分类', {
      type: 'warning',
      confirmButtonText: '删除分类',
      cancelButtonText: '取消',
    })
    try {
      await deleteQuestionCategory(categoryId)
    } catch {
      return
    }
    ElMessage.success('分类已删除')
    if (selectedCategoryId.value === categoryId) {
      selectedCategoryId.value = null
      selectedBankId.value = null
    }
    await loadCategories()
    await loadBanks(null)
    await refreshQuestions()
  }

  function openCreateBankDialog(categoryId?: number) {
    editingBank.value = null
    bankForm.categoryId = categoryId || selectedBank.value?.categoryId || selectedCategoryId.value || categories.value[0]?.id || 1
    bankForm.name = ''
    bankForm.description = ''
    bankForm.status = 'ACTIVE'
    bankDialogVisible.value = true
  }

  function openEditBankDialog(bank: QuestionBank) {
    editingBank.value = bank
    bankForm.categoryId = bank.categoryId
    bankForm.name = bank.name
    bankForm.description = bank.description || ''
    bankForm.status = bank.status
    bankDialogVisible.value = true
  }

  async function submitBank(validate: () => Promise<boolean> | undefined) {
    await validate()
    bankSaving.value = true
    try {
      const saved = editingBank.value ? await updateQuestionBank(editingBank.value.id, bankForm) : await createQuestionBank(bankForm)
      ElMessage.success(editingBank.value ? '题库已更新' : '题库已创建')
      bankDialogVisible.value = false
      selectedCategoryId.value = saved.categoryId
      selectedBankId.value = saved.id
      bankKeyword.value = ''
      await loadBanks(saved.id)
      await refreshQuestions()
    } finally {
      bankSaving.value = false
    }
  }

  function setSelectedBank(bankId: number) {
    selectedBankId.value = bankId
  }

  function setCurrentTreeNode() {
    if (selectedBankId.value) {
      bankTreeRef.value?.setCurrentKey(`bank-${selectedBankId.value}`)
    } else if (selectedCategoryId.value) {
      bankTreeRef.value?.setCurrentKey(`category-${selectedCategoryId.value}`)
    }
  }

  function nextCategorySortOrder() {
    return categories.value.length ? Math.max(...categories.value.map((category) => category.sortOrder)) + 10 : 10
  }

  return {
    categories,
    banks,
    selectedCategoryId,
    selectedBankId,
    selectedBank,
    selectedCategory,
    bankQuestionTotal,
    bankTree,
    bankLoading,
    bankSaving,
    categorySaving,
    categoryDialogVisible,
    bankDialogVisible,
    editingCategory,
    editingBank,
    bankTreeRef,
    bankKeyword,
    categoryForm,
    bankForm,
    bankRules,
    categoryRules,
    loadCategories,
    loadBanks,
    selectBankNode,
    clearSelectedBank,
    openCreateCategoryDialog,
    openEditCategoryDialog,
    submitCategory,
    deleteCategory,
    openCreateBankDialog,
    openEditBankDialog,
    submitBank,
    setSelectedBank,
  }
}
