import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox, type UploadRawFile } from 'element-plus'

import {
  createQuestionNode,
  deleteQuestionNode,
  downloadQuestionBankPackage,
  fetchQuestionContentTree,
  importQuestionBankPackage,
  importQuestionsToGroup,
  updateQuestionNode,
  type QuestionBank,
  type QuestionContentTree,
  type QuestionNodePayload,
} from '@/api/exam-business'
import type { ExcelImportResult } from '@/api/admin'
import { downloadBlob } from '@/utils/download'

export function useQuestionContentActions(
  selectedBankId: Ref<number | null>,
  selectedBank: Ref<QuestionBank | null>,
  setSelectedBank: (bankId: number) => void,
  setImportResult: (result: ExcelImportResult) => void,
  refreshCatalog: (preferredBankId?: number | null) => Promise<void>,
  refreshCategories: () => Promise<void>,
  refreshQuestions: () => Promise<void>,
) {
  const contentTree = ref<QuestionContentTree | null>(null)
  const contentTreeLoading = ref(false)
  const nodeSaving = ref(false)
  const groupImporting = ref(false)
  const packageImporting = ref(false)

  async function loadContentTree() {
    if (!selectedBankId.value) {
      contentTree.value = null
      return
    }
    contentTreeLoading.value = true
    try {
      contentTree.value = await fetchQuestionContentTree(selectedBankId.value)
    } finally {
      contentTreeLoading.value = false
    }
  }

  async function saveContentNode(nodeId: number | null, payload: QuestionNodePayload) {
    if (!selectedBankId.value) {
      return
    }
    nodeSaving.value = true
    try {
      if (nodeId) {
        await updateQuestionNode(nodeId, payload)
        ElMessage.success('内容节点已更新')
      } else {
        await createQuestionNode(selectedBankId.value, payload)
        ElMessage.success('内容节点已创建')
      }
      await refreshQuestions()
      await loadContentTree()
    } finally {
      nodeSaving.value = false
    }
  }

  async function deleteContentNode(nodeId: number) {
    await ElMessageBox.confirm('确定删除这个内容节点？节点下有子节点或小题时不能删除。', '删除节点', {
      type: 'warning',
      confirmButtonText: '删除节点',
      cancelButtonText: '取消',
    })
    await deleteQuestionNode(nodeId)
    ElMessage.success('内容节点已删除')
    await refreshQuestions()
    await loadContentTree()
  }

  async function importGroupQuestions(nodeId: number, file: UploadRawFile) {
    groupImporting.value = true
    try {
      setImportResult(await importQuestionsToGroup(nodeId, file))
      await refreshCatalog()
      await refreshQuestions()
      await loadContentTree()
    } finally {
      groupImporting.value = false
    }
  }

  async function exportSelectedBankPackage() {
    if (!selectedBank.value) {
      return
    }
    const blob = await downloadQuestionBankPackage(selectedBank.value.id)
    downloadBlob(blob, `${selectedBank.value.name}-题库包.zip`)
  }

  async function importBankPackage(file: UploadRawFile) {
    packageImporting.value = true
    try {
      const result = await importQuestionBankPackage(file)
      ElMessage.success(`题库包已导入：${result.bankName}`)
      setSelectedBank(result.bankId)
      await refreshCategories()
      await refreshCatalog(result.bankId)
      await refreshQuestions()
      await loadContentTree()
    } finally {
      packageImporting.value = false
    }
  }

  return {
    contentTree,
    contentTreeLoading,
    nodeSaving,
    groupImporting,
    packageImporting,
    loadContentTree,
    saveContentNode,
    deleteContentNode,
    importGroupQuestions,
    exportSelectedBankPackage,
    importBankPackage,
  }
}
