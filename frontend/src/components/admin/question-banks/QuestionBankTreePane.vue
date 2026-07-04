<template>
  <aside class="tree-pane">
    <div class="pane-title">
      <div class="pane-title__copy">
        <strong>题库树</strong>
        <span>{{ bankCount }} 个题库 / {{ questionTotal }} 题</span>
      </div>
      <div class="pane-title__actions">
        <el-button size="small" :icon="Plus" @click="$emit('create-category')">新建分类</el-button>
      </div>
    </div>
    <div class="bank-search">
      <el-input v-model.trim="keyword" clearable placeholder="搜索题库或分类" @keyup.enter="$emit('search')" />
      <el-button :icon="Search" @click="$emit('search')">搜索</el-button>
    </div>
    <el-tree
      ref="treeRef"
      v-loading="loading"
      :data="tree"
      node-key="key"
      default-expand-all
      highlight-current
      :expand-on-click-node="false"
      @node-click="$emit('select-node', $event)"
    >
      <template #default="{ data }: { data: BankTreeNode }">
        <div class="bank-node" :class="{ 'bank-node--category': data.type === 'category' }">
          <div class="bank-node__main">
            <span class="entity-name">{{ data.label }}</span>
            <span v-if="data.type === 'bank'" class="muted-text">
              {{ data.questionCount }} 题 · 单选 {{ data.singleChoiceCount }} · 多选 {{ data.multipleChoiceCount }} · 写作 {{ data.writingCount }}
            </span>
            <span v-else class="muted-text">{{ data.children.length }} 个题库</span>
          </div>
          <div class="bank-node__actions">
            <template v-if="data.type === 'category' && data.categoryId">
              <el-dropdown trigger="click" @command="handleCategoryCommand($event, data.categoryId!)">
                <el-button link type="primary" size="small" @click.stop>操作</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="createBank">新建题库</el-dropdown-item>
                    <el-dropdown-item command="editCategory">编辑分类</el-dropdown-item>
                    <el-dropdown-item command="deleteCategory">删除分类</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <el-tag v-else size="small" effect="plain" :type="data.status === 'ACTIVE' ? 'success' : 'info'">
              {{ data.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </div>
        </div>
      </template>
    </el-tree>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'

import type { BankTreeNode } from '@/utils/question-bank-editor'

const keyword = defineModel<string>('keyword', { required: true })
defineProps<{
  tree: BankTreeNode[]
  loading: boolean
  bankCount: number
  questionTotal: number
}>()

const emit = defineEmits<{
  search: []
  'select-node': [node: BankTreeNode]
  'create-category': []
  'create-bank': [categoryId?: number]
  'edit-category': [categoryId: number]
  'delete-category': [categoryId: number]
}>()

const treeRef = ref()
type CategoryCommand = 'createBank' | 'editCategory' | 'deleteCategory'

function handleCategoryCommand(command: string | number | object, categoryId: number) {
  const action = command as CategoryCommand
  if (action === 'createBank') {
    emit('create-bank', categoryId)
    return
  }
  if (action === 'editCategory') {
    emit('edit-category', categoryId)
    return
  }
  emit('delete-category', categoryId)
}

defineExpose({
  setCurrentKey(key: string | null) {
    treeRef.value?.setCurrentKey(key)
  },
})
</script>

<style scoped>
.tree-pane {
  min-width: 0;
  padding: 18px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel);
}

.tree-pane :deep(.el-tree-node__content) {
  height: auto;
  min-height: 54px;
  align-items: flex-start;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 6px;
}

.tree-pane :deep(.el-tree-node__expand-icon) {
  margin-top: 12px;
  flex: none;
}

.pane-title,
.bank-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.pane-title {
  margin-bottom: 12px;
}

.pane-title__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}

.pane-title__copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.pane-title__copy span {
  color: var(--ks-text-muted);
  font-size: 13px;
}

.bank-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  margin-bottom: 12px;
}

.bank-search .el-button {
  flex: none;
}

.bank-node {
  width: 100%;
  min-width: 0;
  min-height: 44px;
  padding: 2px 0;
}

.bank-node__actions {
  display: none;
  flex: none;
  align-items: center;
  gap: 4px;
}

.bank-node:hover .bank-node__actions,
.el-tree-node.is-current .bank-node__actions {
  display: flex;
}

.bank-node:not(.bank-node--category) .bank-node__actions {
  display: flex;
}

.bank-node__actions .el-button {
  padding: 0;
}

.bank-node__main {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.bank-node__main .entity-name,
.bank-node__main .muted-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 20px;
}

.bank-node .el-tag {
  flex: none;
}

.bank-node--category .entity-name {
  font-weight: 700;
}

@media (max-width: 900px) {
  .pane-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .pane-title__actions {
    justify-content: flex-start;
  }
}
</style>
