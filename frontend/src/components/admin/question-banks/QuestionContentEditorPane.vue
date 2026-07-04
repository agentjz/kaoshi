<template>
  <section class="content-editor">
    <div class="content-editor__header">
      <div>
        <h2>题组结构</h2>
        <span class="muted-text">{{ selectedBank ? `${selectedBank.name} · ${totalQuestions} 小题` : '选择题库后维护结构' }}</span>
      </div>
      <div class="content-editor__actions">
        <el-upload :show-file-list="false" accept=".zip" :before-upload="handlePackageImport">
          <el-button :icon="Upload">导入包</el-button>
        </el-upload>
        <el-button :icon="Download" :disabled="!selectedBank" @click="$emit('export-package')">导出包</el-button>
      </div>
    </div>

    <el-empty v-if="!selectedBank" description="请选择左侧题库" />
    <template v-else>
      <div class="content-editor__toolbar">
        <el-button type="primary" :icon="Plus" @click="startCreateSection">新建大组</el-button>
        <el-button :icon="Plus" :disabled="!selectedSectionId" @click="startCreateGroup">新建小组</el-button>
      </div>

      <el-tree
        v-loading="loading"
        :data="treeData"
        node-key="key"
        default-expand-all
        highlight-current
        class="content-editor__tree"
        @node-click="selectNode"
      >
        <template #default="{ data }">
          <div class="content-editor__node">
            <span>{{ data.label }}</span>
            <el-tag size="small" effect="plain">{{ data.meta }}</el-tag>
          </div>
        </template>
      </el-tree>

      <el-form :model="form" label-width="86px" class="content-editor__form">
        <el-form-item label="类型">
          <el-segmented v-model="form.nodeType" :options="nodeTypeOptions" :disabled="Boolean(editingNodeId)" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model.trim="form.nodeCode" placeholder="如 listening-section-a" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model.trim="form.title" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :step="10" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.direction" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="材料">
          <el-input v-model="form.material" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item v-if="form.nodeType === 'GROUP'" label="共享选项">
          <QuestionNodeSharedOptionsEditor v-model="form.sharedOptions" />
        </el-form-item>
        <el-form-item label="附件">
          <QuestionNodeAttachmentsEditor
            v-model="form.attachments"
            :uploading="uploadingAttachment"
            :upload-attachment="uploadAttachment"
          />
        </el-form-item>
      </el-form>

      <div class="content-editor__footer">
        <el-upload v-if="selectedNode?.nodeType === 'GROUP'" :show-file-list="false" accept=".xlsx" :before-upload="handleGroupImport">
          <el-button :icon="Upload" :loading="importing">导入本组小题</el-button>
        </el-upload>
        <el-button v-if="editingNodeId" :icon="Delete" @click="$emit('delete-node', editingNodeId)">删除节点</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存节点</el-button>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Delete, Download, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage, type UploadRawFile } from 'element-plus'

import type {
  QuestionAttachmentPayload,
  QuestionBank,
  QuestionContentNode,
  QuestionContentTree,
  QuestionNodePayload,
} from '@/api/exam-business'
import QuestionNodeAttachmentsEditor from '@/components/admin/question-banks/QuestionNodeAttachmentsEditor.vue'
import QuestionNodeSharedOptionsEditor from '@/components/admin/question-banks/QuestionNodeSharedOptionsEditor.vue'

const props = defineProps<{
  selectedBank: QuestionBank | null
  tree: QuestionContentTree | null
  loading: boolean
  saving: boolean
  importing: boolean
  uploadingAttachment: boolean
  uploadAttachment: (file: UploadRawFile) => Promise<QuestionAttachmentPayload>
}>()

const emit = defineEmits<{
  'save-node': [nodeId: number | null, payload: QuestionNodePayload]
  'delete-node': [nodeId: number]
  'import-group': [nodeId: number, file: UploadRawFile]
  'export-package': []
  'import-package': [file: UploadRawFile]
}>()

const editingNodeId = ref<number | null>(null)
const selectedNode = ref<QuestionContentNode | null>(null)
const selectedSectionId = computed(() => selectedNode.value?.nodeType === 'SECTION' ? selectedNode.value.id : selectedNode.value?.parentId || null)
const nodeTypeOptions = [
  { label: '大组', value: 'SECTION' },
  { label: '小组', value: 'GROUP' },
]

const form = reactive<QuestionNodePayload>({
  parentId: null,
  nodeCode: '',
  nodeType: 'SECTION',
  title: '',
  direction: '',
  material: '',
  sortOrder: 10,
  sharedOptions: [],
  attachments: [],
})

const totalQuestions = computed(() => {
  if (!props.tree) {
    return 0
  }
  return props.tree.sections.reduce((sum, section) => sum + countQuestions(section), props.tree.ungroupedQuestions.length)
})

const treeData = computed(() => (props.tree?.sections || []).map((section) => ({
  key: `node-${section.id}`,
  node: section,
  label: section.title || section.nodeCode,
  meta: `${countQuestions(section)} 小题`,
  children: section.children.map((group) => ({
    key: `node-${group.id}`,
    node: group,
    label: group.title || group.nodeCode,
    meta: group.sharedOptions.length ? `${group.questions.length} 小题 · ${group.sharedOptions.length} 共享选项` : `${group.questions.length} 小题`,
    children: [],
  })),
})))

watch(() => props.selectedBank?.id, () => {
  resetForm()
})

function selectNode(data: { node: QuestionContentNode }) {
  selectedNode.value = data.node
  editingNodeId.value = data.node.id
  Object.assign(form, {
    parentId: data.node.parentId,
    nodeCode: data.node.nodeCode,
    nodeType: data.node.nodeType,
    title: data.node.title || '',
    direction: data.node.direction || '',
    material: data.node.material || '',
    sortOrder: data.node.sortOrder,
    sharedOptions: data.node.sharedOptions.map((option) => ({ label: option.label, content: option.content })),
    attachments: data.node.attachments.map((attachment) => ({
      fileName: attachment.fileName,
      fileUrl: attachment.fileUrl,
      mediaType: attachment.mediaType,
    })),
  })
}

function startCreateSection() {
  resetForm()
  form.nodeType = 'SECTION'
  form.sortOrder = nextSortOrder(props.tree?.sections || [])
}

function startCreateGroup() {
  resetForm()
  form.nodeType = 'GROUP'
  form.parentId = selectedSectionId.value
  form.sortOrder = nextSortOrder(selectedSection()?.children || [])
}

function resetForm() {
  editingNodeId.value = null
  selectedNode.value = null
  Object.assign(form, {
    parentId: null,
    nodeCode: '',
    nodeType: 'SECTION',
    title: '',
    direction: '',
    material: '',
    sortOrder: 10,
    sharedOptions: [],
    attachments: [],
  })
}

function submit() {
  if (!form.nodeCode || !form.title) {
    ElMessage.error('请填写节点编码和标题')
    return
  }
  emit('save-node', editingNodeId.value, { ...form, sharedOptions: [...form.sharedOptions], attachments: [...form.attachments] })
}

function handleGroupImport(file: UploadRawFile) {
  if (editingNodeId.value) {
    emit('import-group', editingNodeId.value, file)
  }
  return false
}

function handlePackageImport(file: UploadRawFile) {
  emit('import-package', file)
  return false
}

function selectedSection() {
  return (props.tree?.sections || []).find((section) => section.id === selectedSectionId.value)
}

function nextSortOrder(nodes: QuestionContentNode[]) {
  return nodes.length ? Math.max(...nodes.map((node) => node.sortOrder)) + 10 : 10
}

function countQuestions(node: QuestionContentNode): number {
  return node.questions.length + node.children.reduce((sum, child) => sum + countQuestions(child), 0)
}
</script>

<style scoped>
.content-editor {
  display: grid;
  gap: 14px;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 18px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel);
}

.content-editor__header,
.content-editor__actions,
.content-editor__toolbar,
.content-editor__footer {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.content-editor__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  justify-content: space-between;
  align-items: flex-start;
}

.content-editor__actions,
.content-editor__toolbar,
.content-editor__footer {
  justify-content: flex-start;
  flex-wrap: wrap;
}

.content-editor__actions {
  flex: none;
  justify-content: flex-end;
}

.content-editor__actions :deep(.el-button + .el-button),
.content-editor__toolbar :deep(.el-button + .el-button),
.content-editor__footer :deep(.el-button + .el-button) {
  margin-left: 0;
}

.content-editor__actions :deep(.el-upload),
.content-editor__footer :deep(.el-upload) {
  display: flex;
}

.content-editor__header h2 {
  margin: 0;
  font-size: 20px;
  letter-spacing: 0;
}

.content-editor__tree {
  min-height: 120px;
  padding: 8px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
}

.content-editor__node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  min-width: 0;
}

.content-editor__node span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-editor__form {
  display: grid;
  gap: 10px;
}

.content-editor__form :deep(.el-form-item) {
  min-width: 0;
}

.content-editor__form :deep(.el-form-item__content) {
  min-width: 0;
}

.content-editor__form :deep(.el-input),
.content-editor__form :deep(.el-textarea),
.content-editor__form :deep(.el-input-number),
.content-editor__form :deep(.el-segmented) {
  max-width: 100%;
}

@media (max-width: 900px) {
  .content-editor__header {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .content-editor__actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
