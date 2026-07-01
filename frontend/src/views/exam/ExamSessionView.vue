<template>
  <section class="exam-session" v-loading="loading">
    <template v-if="exam && !session">
      <header class="admin-page__header">
        <div>
          <h1>{{ exam.title }}</h1>
          <p>{{ exam.paperName }}</p>
        </div>
        <el-button type="primary" :loading="starting" @click="beginExam">开始考试</el-button>
      </header>

      <section class="exam-ready">
        <article class="metric">
          <span>限时</span>
          <strong>{{ exam.durationMinutes }} 分钟</strong>
        </article>
        <article class="metric">
          <span>开始时间</span>
          <strong>{{ exam.startTime }}</strong>
        </article>
        <article class="metric">
          <span>截止时间</span>
          <strong>{{ exam.endTime }}</strong>
        </article>
      </section>
    </template>

    <template v-if="session">
      <header class="exam-status">
        <div>
          <h1>{{ session.title }}</h1>
          <p>{{ answeredCount }} / {{ session.questions.length }} 已作答</p>
        </div>
        <div class="exam-actions">
          <span class="countdown" :class="{ 'countdown--danger': remainingSeconds <= 300 }">{{ remainingText }}</span>
          <el-button type="primary" :loading="submitting" :disabled="submitted" @click="confirmSubmit">提交试卷</el-button>
        </div>
      </header>

      <section class="exam-workspace">
        <main class="question-stack">
          <article
            v-for="(question, index) in session.questions"
            :id="`question-${question.questionId}`"
            :key="question.questionId"
            class="question-panel"
          >
            <div class="question-title">
              <strong>{{ index + 1 }}. {{ question.stem }}</strong>
              <el-tag>{{ question.score }} 分</el-tag>
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

            <el-checkbox-group
              v-if="question.type === 'MULTIPLE_CHOICE'"
              v-model="multipleAnswers[question.questionId]"
              class="answer-options"
              :disabled="submitting || submitted"
            >
              <el-checkbox v-for="option in question.options" :key="option.id" :label="option.label" border>
                {{ option.label }}. {{ option.content }}
              </el-checkbox>
            </el-checkbox-group>
            <el-radio-group
              v-else
              v-model="singleAnswers[question.questionId]"
              class="answer-options"
              :disabled="submitting || submitted"
            >
              <el-radio v-for="option in question.options" :key="option.id" :label="option.label" border>
                {{ option.label }}. {{ option.content }}
              </el-radio>
            </el-radio-group>
          </article>
        </main>

        <aside class="answer-card">
          <div class="answer-card__header">
            <strong>答题卡</strong>
            <span>{{ unansweredCount }} 未答</span>
          </div>
          <div class="answer-card__grid">
            <button
              v-for="(question, index) in session.questions"
              :key="question.questionId"
              class="answer-card__item"
              :class="{ 'answer-card__item--answered': isAnswered(question) }"
              type="button"
              @click="scrollToQuestion(question.questionId)"
            >
              {{ index + 1 }}
            </button>
          </div>
        </aside>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { fetchExamTasks, startExam, submitExam, type Exam, type ExamQuestion, type ExamSession } from '@/api/exam-business'

const route = useRoute()
const router = useRouter()
const exam = ref<Exam | null>(null)
const session = ref<ExamSession | null>(null)
const loading = ref(false)
const starting = ref(false)
const submitting = ref(false)
const submitted = ref(false)
const remainingSeconds = ref(0)
let countdownTimer: number | undefined

const multipleAnswers = reactive<Record<number, string[]>>({})
const singleAnswers = reactive<Record<number, string>>({})

const remainingText = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const answeredCount = computed(() => session.value?.questions.filter((question) => isAnswered(question)).length ?? 0)
const unansweredCount = computed(() => Math.max(0, (session.value?.questions.length ?? 0) - answeredCount.value))
const hasActiveAttempt = computed(() => Boolean(session.value && !submitted.value))

onMounted(() => {
  window.addEventListener('beforeunload', preventUnload)
  void loadExam()
})

onBeforeUnmount(() => {
  stopCountdown()
  window.removeEventListener('beforeunload', preventUnload)
})

onBeforeRouteLeave(async () => {
  if (!hasActiveAttempt.value) {
    return true
  }
  try {
    await ElMessageBox.confirm('离开后本次作答仍在进行，未提交答案不会被锁定。', '离开考试', {
      confirmButtonText: '离开',
      cancelButtonText: '继续作答',
      type: 'warning',
    })
    return true
  } catch {
    return false
  }
})

async function loadExam() {
  loading.value = true
  try {
    const examId = Number(route.params.examId)
    const tasks = await fetchExamTasks()
    exam.value = tasks.find((item) => item.id === examId) || null
    if (!exam.value) {
      ElMessage.error('考试不存在或未发布')
      await router.replace({ name: 'exam-home' })
    }
  } finally {
    loading.value = false
  }
}

async function beginExam() {
  if (!exam.value) {
    return
  }
  starting.value = true
  try {
    session.value = await startExam(exam.value.id)
    initializeAnswers(session.value)
    startCountdown(session.value)
  } finally {
    starting.value = false
  }
}

function initializeAnswers(currentSession: ExamSession) {
  for (const question of currentSession.questions) {
    if (question.type === 'MULTIPLE_CHOICE') {
      multipleAnswers[question.questionId] = []
    } else {
      singleAnswers[question.questionId] = ''
    }
  }
}

function startCountdown(currentSession: ExamSession) {
  stopCountdown()
  const startedAt = new Date(currentSession.startedAt).getTime()
  const deadline = startedAt + currentSession.durationMinutes * 60 * 1000
  const tick = () => {
    remainingSeconds.value = Math.max(0, Math.ceil((deadline - Date.now()) / 1000))
    if (remainingSeconds.value <= 0 && !submitted.value && !submitting.value) {
      void submit(true)
    }
  }
  tick()
  countdownTimer = window.setInterval(tick, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = undefined
  }
}

function isAnswered(question: ExamQuestion) {
  if (question.type === 'MULTIPLE_CHOICE') {
    return (multipleAnswers[question.questionId] || []).length > 0
  }
  return Boolean(singleAnswers[question.questionId])
}

function scrollToQuestion(questionId: number) {
  document.getElementById(`question-${questionId}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function confirmSubmit() {
  const message = unansweredCount.value > 0 ? `还有 ${unansweredCount.value} 题未作答，提交后答案将锁定。` : '提交后答案将锁定。'
  await ElMessageBox.confirm(message, '确认提交', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await submit(false)
}

async function submit(autoSubmit: boolean) {
  if (!session.value || submitted.value || submitting.value) {
    return
  }
  submitting.value = true
  try {
    const payload = session.value.questions.map((question) => ({
      questionId: question.questionId,
      selectedLabels:
        question.type === 'MULTIPLE_CHOICE'
          ? multipleAnswers[question.questionId] || []
          : singleAnswers[question.questionId]
            ? [singleAnswers[question.questionId]]
            : [],
    }))
    const result = await submitExam(session.value.examId, payload)
    submitted.value = true
    stopCountdown()
    ElMessage.success(autoSubmit ? '考试时间已到，试卷已自动提交' : '试卷已提交')
    await router.replace({ name: 'exam-result', params: { resultId: result.id }, query: { score: result.obtainedScore, total: result.totalScore } })
  } finally {
    submitting.value = false
  }
}

function preventUnload(event: BeforeUnloadEvent) {
  if (!hasActiveAttempt.value) {
    return
  }
  event.preventDefault()
  event.returnValue = ''
}
</script>

<style scoped>
.exam-status {
  position: sticky;
  z-index: 5;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
  padding: 18px 20px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: rgb(255 255 255 / 96%);
  box-shadow: var(--ks-shadow);
}

.exam-status h1 {
  margin: 0;
  font-size: 20px;
}

.exam-status p {
  margin: 6px 0 0;
  color: var(--ks-text-muted);
}

.exam-ready {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.exam-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 16px;
  align-items: start;
}

.exam-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.countdown {
  min-width: 84px;
  color: var(--ks-primary);
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 20px;
  font-weight: 700;
  text-align: right;
}

.countdown--danger {
  color: var(--ks-warning);
}

.question-media {
  display: grid;
  gap: 12px;
  margin: 12px 0 16px;
}

.question-media__image {
  width: min(100%, 720px);
  max-height: 360px;
  object-fit: contain;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel-muted);
}

.question-media__audio {
  width: min(100%, 720px);
}

.answer-options {
  display: grid;
  gap: 10px;
}

.answer-options :deep(.el-checkbox),
.answer-options :deep(.el-radio) {
  width: 100%;
  height: auto;
  margin: 0;
  padding: 12px;
  white-space: normal;
}

.answer-options :deep(.el-checkbox__label),
.answer-options :deep(.el-radio__label) {
  min-width: 0;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.answer-card {
  position: sticky;
  top: 92px;
  display: grid;
  gap: 14px;
  min-width: 0;
  padding: 16px;
  border: 1px solid var(--ks-border);
  border-radius: var(--ks-radius);
  background: var(--ks-panel);
}

.answer-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.answer-card__header span {
  color: var(--ks-text-muted);
  font-size: 13px;
}

.answer-card__grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.answer-card__item {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 1px solid var(--ks-border);
  border-radius: 6px;
  background: var(--ks-panel-muted);
  color: var(--ks-text-muted);
  cursor: pointer;
}

.answer-card__item--answered {
  border-color: var(--ks-success);
  background: #ecfdf3;
  color: #027a48;
}

@media (max-width: 900px) {
  .exam-workspace {
    grid-template-columns: 1fr;
  }

  .answer-card {
    position: static;
  }
}

@media (max-width: 760px) {
  .exam-ready {
    grid-template-columns: 1fr;
  }

  .exam-status,
  .exam-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
