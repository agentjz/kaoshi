<template>
  <div class="shared-options">
    <div v-for="(option, index) in options" :key="`${option.label}-${index}`" class="shared-options__row">
      <el-input v-model.trim="option.label" class="shared-options__label" />
      <el-input v-model.trim="option.content" placeholder="共享选项内容" />
      <el-button :icon="Delete" circle @click="options.splice(index, 1)" />
    </div>
    <el-button :icon="Plus" @click="addSharedOption">增加共享选项</el-button>
  </div>
</template>

<script setup lang="ts">
import { Delete, Plus } from '@element-plus/icons-vue'

import type { QuestionNodeOptionPayload } from '@/api/exam-business'
import { nextOptionLabel } from '@/utils/question-bank-editor'

const options = defineModel<QuestionNodeOptionPayload[]>({ required: true })

function addSharedOption() {
  options.value.push({ label: nextOptionLabel(options.value.length), content: '' })
}
</script>

<style scoped>
.shared-options {
  display: grid;
  gap: 10px;
  width: 100%;
}

.shared-options__row {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  width: 100%;
}
</style>
