<template>
  <section class="metric-grid">
    <article class="metric">
      <span>得分</span>
      <strong>{{ result.obtainedScore }}</strong>
    </article>
    <article class="metric">
      <span>总分</span>
      <strong>{{ result.totalScore }}</strong>
    </article>
    <article class="metric">
      <span>正确题数</span>
      <strong>{{ result.correctCount }} / {{ result.questionCount }}</strong>
    </article>
  </section>

  <section class="result-review">
    <article v-for="(question, index) in result.questions" :key="question.questionId" class="question-panel">
      <div class="question-title">
        <strong>{{ index + 1 }}. {{ question.stem }}</strong>
        <el-tag :type="question.correct ? 'success' : 'danger'">
          {{ question.obtainedScore }} / {{ question.score }} 分
        </el-tag>
      </div>

      <div v-if="question.attachments.length" class="question-media">
        <template v-for="attachment in question.attachments" :key="attachment.id">
          <img
            v-if="attachment.mediaType === 'IMAGE'"
            :src="attachment.fileUrl"
            :alt="attachment.fileName"
            class="question-media__image"
          />
          <audio v-else-if="attachment.mediaType === 'AUDIO'" :src="attachment.fileUrl" controls class="question-media__audio" />
          <el-link v-else :href="attachment.fileUrl" target="_blank">{{ attachment.fileName }}</el-link>
        </template>
      </div>

      <div class="review-options">
        <div
          v-for="option in question.options"
          :key="option.id"
          class="review-option"
          :class="{
            'review-option--selected': question.selectedLabels.includes(option.label),
            'review-option--correct': question.correctLabels.includes(option.label),
          }"
        >
          <span>{{ option.label }}. {{ option.content }}</span>
          <el-tag v-if="question.correctLabels.includes(option.label)" size="small" type="success">正确答案</el-tag>
          <el-tag v-else-if="question.selectedLabels.includes(option.label)" size="small" type="warning">我的答案</el-tag>
        </div>
      </div>

      <div class="review-summary">
        <span>我的答案：{{ labelsText(question.selectedLabels) }}</span>
        <span>正确答案：{{ labelsText(question.correctLabels) }}</span>
        <span>结果：{{ question.correct ? '正确' : '错误' }}</span>
      </div>

      <p v-if="question.analysis" class="analysis-text">解析：{{ question.analysis }}</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import type { ExamResultDetail } from '@/api/exam-business'

defineProps<{
  result: ExamResultDetail
}>()

function labelsText(labels: string[]) {
  return labels.length ? labels.join('、') : '未作答'
}
</script>

<style scoped>
.result-review {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.question-media {
  display: grid;
  gap: 10px;
  margin: 12px 0;
}

.question-media__image {
  display: block;
  max-width: min(520px, 100%);
  max-height: 280px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  object-fit: contain;
}

.question-media__audio {
  width: min(520px, 100%);
}

.review-options {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.review-option {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
  padding: 12px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel-muted);
  line-height: 1.5;
}

.review-option span {
  min-width: 0;
  overflow-wrap: anywhere;
}

.review-option--selected {
  border-color: var(--ks-warning);
  background: #fffaeb;
}

.review-option--correct {
  border-color: var(--ks-success);
  background: #ecfdf3;
}

.review-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 14px;
  color: var(--ks-text-muted);
  font-size: 13px;
}

.analysis-text {
  margin: 14px 0 0;
  color: var(--ks-text);
  line-height: 1.7;
  overflow-wrap: anywhere;
}
</style>
