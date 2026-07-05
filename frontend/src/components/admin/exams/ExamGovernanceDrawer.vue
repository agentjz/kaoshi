<template>
  <el-drawer v-model="visibleModel" size="760px" :title="exam ? `考试治理 - ${exam.title}` : '考试治理'" @open="loadData">
    <section v-if="exam" v-loading="loading" class="governance-drawer">
      <el-row :gutter="12">
        <el-col v-for="item in reportItems" :key="item.label" :span="6">
          <div class="metric">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </el-col>
      </el-row>

      <section class="governance-section">
        <div class="section-head">
          <h3>成绩发布策略</h3>
          <el-button type="primary" :loading="savingPolicy" @click="savePolicy">保存策略</el-button>
        </div>
        <el-form label-position="top">
          <el-form-item label="考生可见">
            <el-switch v-model="policy.visibleToStudents" />
          </el-form-item>
          <el-form-item label="展示正确答案">
            <el-switch v-model="policy.showAnswers" />
          </el-form-item>
          <el-form-item label="展示解析">
            <el-switch v-model="policy.showAnalysis" />
          </el-form-item>
          <el-form-item label="发布时间">
            <el-date-picker v-model="policy.releaseTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" clearable class="full-control" />
          </el-form-item>
        </el-form>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>安全策略</h3>
          <el-button type="primary" :loading="savingSecurity" @click="saveSecurityPolicy">保存策略</el-button>
        </div>
        <el-form label-position="top">
          <el-form-item label="要求全屏">
            <el-switch v-model="securityPolicy.requireFullscreen" />
          </el-form-item>
          <el-form-item label="禁止复制粘贴">
            <el-switch v-model="securityPolicy.forbidCopyPaste" />
          </el-form-item>
          <el-form-item label="记录离开页面">
            <el-switch v-model="securityPolicy.trackFocusLoss" />
          </el-form-item>
          <el-form-item label="离开页面阈值">
            <el-input-number v-model="securityPolicy.maxFocusLossCount" :min="0" :max="100" controls-position="right" />
          </el-form-item>
          <el-form-item label="设备检查">
            <el-switch v-model="securityPolicy.deviceCheckRequired" />
          </el-form-item>
        </el-form>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>考生名册</h3>
          <el-button type="primary" :loading="savingRoster" @click="saveRoster">保存名册</el-button>
        </div>
        <el-select v-model="selectedUserIds" multiple filterable class="full-control" placeholder="选择允许参加本场考试的考生">
          <el-option v-for="user in users" :key="user.id" :label="`${user.displayName} (${user.username})`" :value="user.id" />
        </el-select>
        <el-table :data="participants" border class="governance-table">
          <el-table-column prop="displayName" label="考生" min-width="140" />
          <el-table-column prop="departmentName" label="部门" min-width="120" />
          <el-table-column label="额外分钟" width="120">
            <template #default="{ row }: { row: ExamParticipant }">
              <el-input-number v-model="row.extraMinutes" :min="0" :max="1440" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="额外次数" width="120">
            <template #default="{ row }: { row: ExamParticipant }">
              <el-input-number v-model="row.extraAttempts" :min="0" :max="100" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="原因" min-width="180">
            <template #default="{ row }: { row: ExamParticipant }">
              <el-input v-model="row.reason" placeholder="调整原因" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }: { row: ExamParticipant }">
              <el-button link type="primary" @click="saveAllowance(row)">保存</el-button>
              <el-button link type="warning" @click="grantRetake(row)">补考</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="尚未指定考生；未指定时沿用考试开放范围" />
          </template>
        </el-table>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>文件资产</h3>
          <el-tag>{{ fileAssets.length }} 个</el-tag>
        </div>
        <el-table :data="fileAssets" border max-height="220">
          <el-table-column prop="originalName" label="文件" min-width="180" />
          <el-table-column prop="mediaType" label="类型" width="100" />
          <el-table-column prop="uploadedBy" label="上传人" width="120" />
          <el-table-column prop="uploadedAt" label="上传时间" min-width="170" />
        </el-table>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>阅卷 rubric</h3>
          <div class="section-actions">
            <el-button @click="addRubric">新增</el-button>
            <el-button type="primary" :loading="savingRubrics" @click="saveRubrics">保存 rubric</el-button>
          </div>
        </div>
        <el-table :data="rubrics" border class="governance-table">
          <el-table-column label="名称" min-width="140">
            <template #default="{ row }: { row: ExamReviewRubric }">
              <el-input v-model="row.title" />
            </template>
          </el-table-column>
          <el-table-column label="说明" min-width="220">
            <template #default="{ row }: { row: ExamReviewRubric }">
              <el-input v-model="row.description" />
            </template>
          </el-table-column>
          <el-table-column label="分值" width="110">
            <template #default="{ row }: { row: ExamReviewRubric }">
              <el-input-number v-model="row.maxScore" :min="0" :max="1000" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" @click="rubrics.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>阅卷任务与复核</h3>
          <el-button type="primary" @click="generateTasks">生成任务</el-button>
        </div>
        <el-table :data="reviewTasks" border max-height="260">
          <el-table-column prop="studentName" label="考生" min-width="120" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="reviewerUsername" label="阅卷人" width="120" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }: { row: ExamReviewTask }">
              <el-button link type="primary" @click="claimTask(row)">领取</el-button>
              <el-button link type="success" @click="completeTask(row)">完成</el-button>
              <el-button link type="warning" @click="requestRecheck(row)">复核</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无阅卷任务，可从待阅卷成绩生成" />
          </template>
        </el-table>
        <el-table :data="rechecks" border max-height="200" class="governance-table">
          <el-table-column prop="status" label="复核状态" width="120" />
          <el-table-column prop="requestedBy" label="发起人" width="120" />
          <el-table-column prop="reason" label="原因" min-width="180" />
          <el-table-column prop="resolution" label="处理意见" min-width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }: { row: ExamReviewRecheck }">
              <el-button link type="primary" @click="resolveRecheck(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>安全事件</h3>
          <el-tag>{{ securityEvents.length }} 条</el-tag>
        </div>
        <el-table :data="securityEvents" border max-height="220">
          <el-table-column prop="eventType" label="类型" min-width="150" />
          <el-table-column prop="severity" label="级别" width="90" />
          <el-table-column prop="username" label="考生" width="120" />
          <el-table-column prop="detail" label="详情" min-width="180" />
          <el-table-column prop="occurredAt" label="时间" min-width="170" />
        </el-table>
      </section>

      <section class="governance-section">
        <div class="section-head">
          <h3>治理事件</h3>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
        </div>
        <el-table :data="events" border max-height="260">
          <el-table-column prop="action" label="动作" min-width="210" />
          <el-table-column prop="username" label="对象" width="120" />
          <el-table-column prop="actorUsername" label="操作人" width="120" />
          <el-table-column prop="reason" label="原因" min-width="160" />
          <el-table-column prop="createdAt" label="时间" min-width="170" />
        </el-table>
      </section>
    </section>
  </el-drawer>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, reactive, ref } from 'vue'

import { fetchAdminUsers, type AdminUser } from '@/api/admin'
import {
  fetchExamEvents,
  fetchExamParticipants,
  fetchExamReport,
  fetchExamReviewRechecks,
  fetchExamReviewRubrics,
  fetchExamReviewTasks,
  fetchExamResultPolicy,
  fetchExamSecurityEvents,
  fetchExamSecurityPolicy,
  fetchFileAssets,
  generateExamReviewTasks,
  grantExamRetake,
  claimExamReviewTask,
  replaceExamReviewRubrics,
  replaceExamParticipants,
  requestExamReviewRecheck,
  updateExamAllowance,
  updateExamReviewRecheck,
  updateExamReviewTask,
  updateExamResultPolicy,
  updateExamSecurityPolicy,
  type Exam,
  type ExamAttemptEvent,
  type ExamParticipant,
  type ExamReport,
  type ExamReviewRecheck,
  type ExamReviewRubric,
  type ExamReviewTask,
  type ExamResultPolicy,
  type ExamSecurityEvent,
  type ExamSecurityPolicy,
  type FileAsset,
} from '@/api/exam-business'

const props = defineProps<{ visible: boolean; exam: Exam | null }>()
const emit = defineEmits<{ 'update:visible': [value: boolean] }>()
const visibleModel = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

const loading = ref(false)
const savingRoster = ref(false)
const savingPolicy = ref(false)
const savingSecurity = ref(false)
const savingRubrics = ref(false)
const users = ref<AdminUser[]>([])
const participants = ref<ExamParticipant[]>([])
const selectedUserIds = ref<number[]>([])
const events = ref<ExamAttemptEvent[]>([])
const securityEvents = ref<ExamSecurityEvent[]>([])
const rubrics = ref<ExamReviewRubric[]>([])
const reviewTasks = ref<ExamReviewTask[]>([])
const rechecks = ref<ExamReviewRecheck[]>([])
const fileAssets = ref<FileAsset[]>([])
const report = ref<ExamReport | null>(null)
const policy = reactive<ExamResultPolicy>({
  examId: 0,
  visibleToStudents: true,
  showAnswers: true,
  showAnalysis: true,
  releaseTime: null,
  updatedAt: null,
})
const securityPolicy = reactive<ExamSecurityPolicy>({
  examId: 0,
  requireFullscreen: false,
  forbidCopyPaste: true,
  trackFocusLoss: true,
  maxFocusLossCount: 3,
  deviceCheckRequired: false,
  updatedAt: null,
})

const reportItems = computed(() => [
  { label: '名册人数', value: report.value?.participantCount ?? 0 },
  { label: '提交人数', value: report.value?.submittedCount ?? 0 },
  { label: '待阅卷', value: report.value?.pendingReviewCount ?? 0 },
  { label: '通过率', value: `${report.value?.passRate ?? 0}%` },
])

async function loadData() {
  if (!props.exam) {
    return
  }
  loading.value = true
  try {
    const [
      userPage,
      loadedParticipants,
      loadedPolicy,
      loadedReport,
      loadedEvents,
      loadedAssets,
      loadedSecurityPolicy,
      loadedSecurityEvents,
      loadedRubrics,
      loadedTasks,
      loadedRechecks,
    ] = await Promise.all([
      fetchAdminUsers({ page: 1, size: 200 }),
      fetchExamParticipants(props.exam.id),
      fetchExamResultPolicy(props.exam.id),
      fetchExamReport(props.exam.id),
      fetchExamEvents(props.exam.id),
      fetchFileAssets(),
      fetchExamSecurityPolicy(props.exam.id),
      fetchExamSecurityEvents(props.exam.id),
      fetchExamReviewRubrics(props.exam.id),
      fetchExamReviewTasks(props.exam.id),
      fetchExamReviewRechecks(props.exam.id),
    ])
    users.value = userPage.records
    participants.value = loadedParticipants
    selectedUserIds.value = loadedParticipants.map((participant) => participant.userId)
    Object.assign(policy, loadedPolicy)
    Object.assign(securityPolicy, loadedSecurityPolicy)
    report.value = loadedReport
    events.value = loadedEvents
    fileAssets.value = loadedAssets
    securityEvents.value = loadedSecurityEvents
    rubrics.value = loadedRubrics
    reviewTasks.value = loadedTasks
    rechecks.value = loadedRechecks
  } finally {
    loading.value = false
  }
}

async function saveSecurityPolicy() {
  if (!props.exam) {
    return
  }
  savingSecurity.value = true
  try {
    Object.assign(securityPolicy, await updateExamSecurityPolicy(props.exam.id, {
      requireFullscreen: securityPolicy.requireFullscreen,
      forbidCopyPaste: securityPolicy.forbidCopyPaste,
      trackFocusLoss: securityPolicy.trackFocusLoss,
      maxFocusLossCount: securityPolicy.maxFocusLossCount,
      deviceCheckRequired: securityPolicy.deviceCheckRequired,
    }))
    ElMessage.success('安全策略已保存')
    await loadData()
  } finally {
    savingSecurity.value = false
  }
}

function addRubric() {
  rubrics.value.push({
    id: Date.now(),
    examId: props.exam?.id || 0,
    title: '',
    description: '',
    maxScore: 0,
    sortOrder: (rubrics.value.length + 1) * 10,
  })
}

async function saveRubrics() {
  if (!props.exam) {
    return
  }
  savingRubrics.value = true
  try {
    rubrics.value = await replaceExamReviewRubrics(props.exam.id, rubrics.value.map((rubric, index) => ({
      title: rubric.title,
      description: rubric.description || '',
      maxScore: rubric.maxScore,
      sortOrder: (index + 1) * 10,
    })))
    ElMessage.success('阅卷 rubric 已保存')
    await loadData()
  } finally {
    savingRubrics.value = false
  }
}

async function generateTasks() {
  if (!props.exam) {
    return
  }
  reviewTasks.value = await generateExamReviewTasks(props.exam.id)
  ElMessage.success('阅卷任务已生成')
  await loadData()
}

async function claimTask(row: ExamReviewTask) {
  if (!props.exam) {
    return
  }
  reviewTasks.value = await claimExamReviewTask(props.exam.id, row.id)
  ElMessage.success('阅卷任务已领取')
  await loadData()
}

async function completeTask(row: ExamReviewTask) {
  if (!props.exam) {
    return
  }
  reviewTasks.value = await updateExamReviewTask(props.exam.id, row.id, 'COMPLETED')
  ElMessage.success('阅卷任务已完成')
  await loadData()
}

async function requestRecheck(row: ExamReviewTask) {
  if (!props.exam) {
    return
  }
  const reason = await ElMessageBox.prompt('请输入复核原因', '发起复核', {
    confirmButtonText: '发起',
    cancelButtonText: '取消',
  }).then((result) => result.value).catch(() => null)
  if (reason === null) {
    return
  }
  rechecks.value = await requestExamReviewRecheck(props.exam.id, row.id, reason)
  ElMessage.success('复核已发起')
  await loadData()
}

async function resolveRecheck(row: ExamReviewRecheck) {
  if (!props.exam) {
    return
  }
  const resolution = await ElMessageBox.prompt('请输入处理意见', '处理复核', {
    confirmButtonText: '处理',
    cancelButtonText: '取消',
  }).then((result) => result.value).catch(() => null)
  if (resolution === null) {
    return
  }
  rechecks.value = await updateExamReviewRecheck(props.exam.id, row.id, 'RESOLVED', resolution)
  ElMessage.success('复核已处理')
  await loadData()
}

async function saveRoster() {
  if (!props.exam) {
    return
  }
  savingRoster.value = true
  try {
    participants.value = await replaceExamParticipants(props.exam.id, selectedUserIds.value)
    ElMessage.success('考生名册已保存')
    await loadData()
  } finally {
    savingRoster.value = false
  }
}

async function saveAllowance(row: ExamParticipant) {
  if (!props.exam) {
    return
  }
  await updateExamAllowance(props.exam.id, row.userId, {
    extraMinutes: row.extraMinutes,
    extraAttempts: row.extraAttempts,
    reason: row.reason || '',
  })
  ElMessage.success('个人授权已保存')
  await loadData()
}

async function grantRetake(row: ExamParticipant) {
  if (!props.exam) {
    return
  }
  await grantExamRetake(props.exam.id, row.userId, row.reason || '管理员授予补考')
  ElMessage.success('已授予一次补考')
  await loadData()
}

async function savePolicy() {
  if (!props.exam) {
    return
  }
  savingPolicy.value = true
  try {
    Object.assign(policy, await updateExamResultPolicy(props.exam.id, {
      visibleToStudents: policy.visibleToStudents,
      showAnswers: policy.showAnswers,
      showAnalysis: policy.showAnalysis,
      releaseTime: policy.releaseTime,
    }))
    ElMessage.success('成绩发布策略已保存')
    await loadData()
  } finally {
    savingPolicy.value = false
  }
}
</script>

<style scoped>
.governance-drawer {
  display: grid;
  gap: 16px;
}

.metric {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 12px;
  display: grid;
  gap: 6px;
}

.metric span {
  color: #6b7280;
}

.metric strong {
  font-size: 20px;
}

.governance-section {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-head h3 {
  margin: 0;
  font-size: 16px;
}

.full-control {
  width: 100%;
}

.governance-table {
  margin-top: 12px;
}
</style>
