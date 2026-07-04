<template>
  <section v-if="sections.length" class="content-tree">
    <div class="content-tree__header">
      <span>内容结构</span>
      <el-tag effect="plain">{{ totalQuestions }} 小题</el-tag>
    </div>
    <el-tree :data="treeData" node-key="key" default-expand-all>
      <template #default="{ data }">
        <div class="content-tree__node">
          <span>{{ data.label }}</span>
          <el-tag v-if="data.meta" size="small" effect="plain">{{ data.meta }}</el-tag>
        </div>
      </template>
    </el-tree>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import type { QuestionContentTreeSection } from '@/utils/question-bank-editor'

const props = defineProps<{
  sections: QuestionContentTreeSection[]
}>()

const totalQuestions = computed(() => props.sections.reduce((sum, section) => sum + section.questionCount, 0))
const treeData = computed(() => props.sections.map((section) => ({
  key: section.key,
  label: section.title,
  meta: `${section.questionCount} 小题`,
  children: section.groups.map((group) => ({
    key: group.key,
    label: group.title,
    meta: group.sharedOptionCount > 0 ? `${group.questionCount} 小题 · ${group.sharedOptionCount} 共享选项` : `${group.questionCount} 小题`,
    children: [],
  })),
})))
</script>

<style scoped>
.content-tree {
  display: grid;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel-muted);
}

.content-tree__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--ks-text);
  font-weight: 600;
}

.content-tree__node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  min-width: 0;
}

.content-tree__node span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
