import questionSet from '../../../../../backend/src/main/resources/question-sets/cet4/2023-03/set-1.json'

import type {
  AdminMenu,
  AdminPermission,
  AdminRole,
  AdminUser,
  Department,
} from '../../admin'
import type {
  Exam,
  ExamQuestion,
  ExamResultDetail,
  ExamResultQuestion,
  Question,
  QuestionAttachment,
  QuestionBank,
  QuestionContentNode,
  QuestionContentTree,
  QuestionOption,
} from '../../exam-business-types'
import type { CurrentUser } from '../../types'
import { isManualReviewType, type QuestionTypeCode } from '@/utils/question-types'

interface DemoUser extends AdminUser {
  password: string
  roleIds: number[]
}

interface ResourceCategory {
  code: string
  name: string
  description: string | null
  sortOrder: number
}

interface ResourceBank {
  code: string
  categoryCode: string
  name: string
  description: string | null
  status: QuestionBank['status']
}

interface ResourceAttachment {
  fileName: string
  fileUrl: string
  mediaType: QuestionAttachment['mediaType']
  sortOrder: number
}

interface ResourceOption {
  label: string
  content: string
  correct: boolean
  sortOrder: number
}

interface ResourceItem {
  code: string
  type: QuestionTypeCode
  stem: string
  itemLabel: string | null
  itemStem: string | null
  analysis: string | null
  difficulty: Question['difficulty']
  status: Question['status']
  options: ResourceOption[]
  attachments: ResourceAttachment[]
}

interface ResourceGroup {
  groupCode: string
  groupTitle: string
  groupDirection: string | null
  groupMaterial: string | null
  groupSortOrder: number
  bankCode: string
  sharedOptions: Array<{ label: string; content: string; sortOrder: number }>
  attachments: ResourceAttachment[]
  items: ResourceItem[]
}

interface ResourceSection {
  code: string
  title: string
  direction: string | null
  material: string | null
  sortOrder: number
  attachments: ResourceAttachment[]
  groups: ResourceGroup[]
}

interface ResourceExam {
  title: string
  description: string
  qualifyScore: number
  startTime: string
  endTime: string
  durationMinutes: number
  timeLimit: boolean
  attemptLimit: number | null
  displayMode: Exam['displayMode']
  questionOrderMode: Exam['questionOrderMode']
  openType: Exam['openType']
  status: Exam['status']
  paperQuestions: Array<{ questionCode: string; score: number; sortOrder: number }>
}

interface QuestionSetResource {
  categories: ResourceCategory[]
  banks: ResourceBank[]
  sections: ResourceSection[]
  exams: ResourceExam[]
}

export interface DemoAttempt {
  id: number
  examId: number
  userId: number
  status: ExamSessionStatus
  startedAt: string
  questions: ExamQuestion[]
}

type ExamSessionStatus = 'IN_PROGRESS' | 'SUBMITTED'

export interface DemoState {
  currentUserId: number | null
  departments: Department[]
  users: DemoUser[]
  roles: AdminRole[]
  permissions: AdminPermission[]
  menus: AdminMenu[]
  categories: Array<ResourceCategory & { id: number }>
  banks: QuestionBank[]
  nodes: QuestionContentNode[]
  nodeBankIds: Record<number, number>
  questions: Question[]
  questionIdByCode: Record<string, number>
  correctLabelsByQuestionId: Record<number, string[]>
  exams: Exam[]
  attempts: DemoAttempt[]
  results: ExamResultDetail[]
  nextId: number
}

function clone<T>(value: T): T {
  return structuredClone(value)
}

function nowIso() {
  return new Date().toISOString()
}

function nextId(state: Pick<DemoState, 'nextId'>) {
  state.nextId += 1
  return state.nextId
}

function seedState(): DemoState {
  const resource = questionSet as QuestionSetResource
  const state: DemoState = {
    currentUserId: null,
    departments: [
      { id: 1, parentId: null, name: '默认组织', code: 'ROOT', description: '演示组织根节点', status: 'ACTIVE', children: [] },
      { id: 2, parentId: 1, name: '教学部', code: 'TEACHING', description: '演示教学部门', status: 'ACTIVE', children: [] },
    ],
    users: [],
    roles: [],
    permissions: [],
    menus: [],
    categories: [],
    banks: [],
    nodes: [],
    nodeBankIds: {},
    questions: [],
    questionIdByCode: {},
    correctLabelsByQuestionId: {},
    exams: [],
    attempts: [],
    results: [],
    nextId: 1000,
  }
  state.departments[0].children = [state.departments[1]]
  state.permissions = buildPermissions()
  state.menus = buildMenus()
  state.roles = buildRoles(state.permissions, state.menus)
  state.users = buildUsers()
  buildQuestionSet(state, resource)
  buildExams(state, resource.exams)
  seedSubmittedResult(state)
  return state
}

function buildPermissions(): AdminPermission[] {
  const codes = [
    ['admin:users', '用户管理'],
    ['admin:roles', '角色管理'],
    ['admin:departments', '部门管理'],
    ['exam:questions', '题库管理'],
    ['exam:manage', '考试管理'],
    ['exam:review', '成绩阅卷'],
    ['exam:take', '在线考试'],
  ]
  return codes.map(([code, name], index) => ({ id: index + 1, code, name, description: `${name}权限` }))
}

function buildMenus(): AdminMenu[] {
  return [
    { id: 1, code: 'online-exam', title: '在线考试', path: '/my/exam', parentId: null, sortOrder: 10, icon: null },
    { id: 2, code: 'exam-management', title: '考试管理', path: '/exam/manage', parentId: null, sortOrder: 20, icon: null },
    { id: 3, code: 'system-management', title: '系统管理', path: '/sys/roles', parentId: null, sortOrder: 30, icon: null },
  ]
}

function buildRoles(permissions: AdminPermission[], menus: AdminMenu[]): AdminRole[] {
  return [
    { id: 1, code: 'ADMIN', name: '系统管理员', description: '拥有演示环境全部权限', permissions, menus },
    { id: 2, code: 'STUDENT', name: '考生', description: '参加考试并查看成绩', permissions: permissions.filter((item) => item.code === 'exam:take'), menus: menus.slice(0, 1) },
  ]
}

function buildUsers(): DemoUser[] {
  const timestamp = nowIso()
  return [
    { id: 1, departmentId: 1, departmentName: '默认组织', username: 'admin', displayName: '系统管理员', status: 'ACTIVE', roles: ['ADMIN'], roleIds: [1], password: 'password', createdAt: timestamp, updatedAt: timestamp },
    { id: 2, departmentId: 2, departmentName: '教学部', username: 'zhangsan', displayName: '张三', status: 'ACTIVE', roles: ['STUDENT'], roleIds: [2], password: 'password', createdAt: timestamp, updatedAt: timestamp },
  ]
}

function buildQuestionSet(state: DemoState, resource: QuestionSetResource) {
  const categoryIds = new Map<string, number>()
  for (const category of resource.categories) {
    const id = nextId(state)
    categoryIds.set(category.code, id)
    state.categories.push({ ...category, id })
  }

  const bankIds = new Map<string, number>()
  for (const bank of resource.banks) {
    const categoryId = categoryIds.get(bank.categoryCode) || state.categories[0].id
    const id = nextId(state)
    bankIds.set(bank.code, id)
    state.banks.push({
      id,
      categoryId,
      categoryName: state.categories.find((item) => item.id === categoryId)?.name || '',
      name: bank.name,
      description: bank.description,
      status: bank.status,
      questionCount: 0,
      singleChoiceCount: 0,
      multipleChoiceCount: 0,
      writingCount: 0,
    })
  }

  const sectionNodeByBankAndCode = new Map<string, QuestionContentNode>()
  for (const section of resource.sections) {
    for (const group of section.groups) {
      const bankId = bankIds.get(group.bankCode)
      if (!bankId) {
        continue
      }
      const sectionKey = `${bankId}:${section.code}`
      let sectionNode = sectionNodeByBankAndCode.get(sectionKey)
      if (!sectionNode) {
        sectionNode = createNode(state, bankId, null, section.code, 'SECTION', section.title, section.direction, section.material, section.sortOrder, section.attachments, [])
        sectionNodeByBankAndCode.set(sectionKey, sectionNode)
      }
      const sharedOptions = group.sharedOptions.map((option, index) => ({ id: nextId(state), label: option.label, content: option.content, sortOrder: option.sortOrder || (index + 1) * 10 }))
      const groupNode = createNode(state, bankId, sectionNode.id, group.groupCode, 'GROUP', group.groupTitle, group.groupDirection, group.groupMaterial, group.groupSortOrder, group.attachments, sharedOptions)
      sectionNode.children.push(groupNode)
      for (const item of group.items) {
        const question = createQuestionFromResource(state, bankId, section, group, item)
        groupNode.questions.push(question)
      }
    }
  }
  refreshBankCounts(state)
}

function createNode(
  state: DemoState,
  bankId: number,
  parentId: number | null,
  nodeCode: string,
  nodeType: QuestionContentNode['nodeType'],
  title: string | null,
  direction: string | null,
  material: string | null,
  sortOrder: number,
  attachments: ResourceAttachment[],
  sharedOptions: QuestionContentNode['sharedOptions'],
) {
  const node: QuestionContentNode = {
    id: nextId(state),
    parentId,
    nodeCode,
    nodeType,
    title,
    direction,
    material,
    sortOrder,
    sharedOptions,
    attachments: attachments.map((attachment) => createAttachment(state, attachment)),
    questions: [],
    children: [],
  }
  state.nodes.push(node)
  state.nodeBankIds[node.id] = bankId
  return node
}

function createQuestionFromResource(state: DemoState, bankId: number, section: ResourceSection, group: ResourceGroup, item: ResourceItem) {
  const bank = state.banks.find((entry) => entry.id === bankId)
  const options = item.options.map((option) => createOption(state, option))
  const groupAttachments = group.attachments.map((attachment) => createAttachment(state, attachment))
  const itemAttachments = item.attachments.map((attachment) => createAttachment(state, attachment))
  const question: Question = {
    id: nextId(state),
    bankId,
    bankName: bank?.name || '',
    type: item.type,
    stem: item.stem,
    sectionCode: section.code,
    sectionTitle: section.title,
    sectionSortOrder: section.sortOrder,
    groupCode: group.groupCode,
    groupTitle: group.groupTitle,
    groupDirection: group.groupDirection,
    groupMaterial: group.groupMaterial,
    groupSortOrder: group.groupSortOrder,
    itemLabel: item.itemLabel,
    itemStem: item.itemStem,
    analysis: item.analysis,
    difficulty: item.difficulty,
    status: item.status,
    options,
    attachments: [...groupAttachments, ...itemAttachments],
  }
  state.questions.push(question)
  state.questionIdByCode[item.code] = question.id
  state.correctLabelsByQuestionId[question.id] = options.filter((option) => option.correct).map((option) => option.label)
  return question
}

function createOption(state: DemoState, option: ResourceOption): QuestionOption {
  return { id: nextId(state), label: option.label, content: option.content, correct: option.correct, sortOrder: option.sortOrder }
}

function createAttachment(state: Pick<DemoState, 'nextId'>, attachment: ResourceAttachment): QuestionAttachment {
  return { id: nextId(state), fileName: attachment.fileName, fileUrl: attachment.fileUrl, mediaType: attachment.mediaType, sortOrder: attachment.sortOrder }
}

function buildExams(state: DemoState, exams: ResourceExam[]) {
  for (const exam of exams) {
    const paperQuestions = exam.paperQuestions
      .map((paperQuestion) => {
        const sourceId = state.questionIdByCode[paperQuestion.questionCode]
        const source = state.questions.find((question) => question.id === sourceId)
        return { paperQuestion, source }
      })
      .filter((entry): entry is { paperQuestion: ResourceExam['paperQuestions'][number]; source: Question } => Boolean(entry.source))

    const fallbackQuestions = state.questions.slice(0, 12).map((source, index) => ({
      paperQuestion: { questionCode: String(source.id), score: isManualReviewType(source.type) ? 30 : 5, sortOrder: (index + 1) * 10 },
      source,
    }))
    const selected = paperQuestions.length ? paperQuestions : fallbackQuestions
    state.exams.push({
      id: nextId(state),
      title: exam.title,
      description: exam.description,
      qualifyScore: exam.qualifyScore,
      startTime: exam.startTime,
      endTime: exam.endTime,
      durationMinutes: exam.durationMinutes,
      timeLimit: exam.timeLimit,
      attemptLimit: exam.attemptLimit,
      displayMode: exam.displayMode,
      questionOrderMode: exam.questionOrderMode,
      openType: exam.openType,
      departmentIds: [],
      rules: [],
      paperQuestions: selected.map(({ paperQuestion, source }) => ({
        questionId: source.id,
        bankId: source.bankId,
        bankName: source.bankName,
        type: source.type,
        stem: source.stem,
        sectionCode: source.sectionCode,
        sectionTitle: source.sectionTitle,
        sectionSortOrder: source.sectionSortOrder,
        groupCode: source.groupCode,
        groupTitle: source.groupTitle,
        groupDirection: source.groupDirection,
        groupMaterial: source.groupMaterial,
        groupSortOrder: source.groupSortOrder,
        itemLabel: source.itemLabel,
        itemStem: source.itemStem,
        score: paperQuestion.score,
        sortOrder: paperQuestion.sortOrder,
      })),
      status: exam.status,
      questionCount: selected.length,
      totalScore: selected.reduce((sum, item) => sum + item.paperQuestion.score, 0),
    })
  }
}

function seedSubmittedResult(state: DemoState) {
  const exam = state.exams[0]
  const user = state.users.find((item) => item.username === 'zhangsan')
  if (!exam || !user) {
    return
  }
  const sessionQuestions = buildSessionQuestions(state, exam)
  const answers = sessionQuestions.map((question) => ({
    questionId: question.questionId,
    selectedLabels: isManualReviewType(question.type) ? [] : [question.options[0]?.label || 'A'],
    answerText: isManualReviewType(question.type) ? 'This is a demo writing answer.' : null,
  }))
  const result = gradeSubmission(state, exam, user, sessionQuestions, answers)
  state.results.push({ ...result, gradingStatus: 'PENDING_REVIEW' })
}

export function buildCurrentUser(state: DemoState, user: DemoUser): CurrentUser {
  const permissions = state.roles
    .filter((role) => user.roleIds.includes(role.id))
    .flatMap((role) => role.permissions.map((permission) => permission.code))
  return {
    id: user.id,
    username: user.username,
    displayName: user.displayName,
    mustChangePassword: false,
    roles: user.roles,
    permissions: Array.from(new Set(permissions)),
  }
}

export function buildContentTree(state: DemoState, bankId: number): QuestionContentTree {
  const bank = state.banks.find((item) => item.id === bankId)
  const sections = state.nodes
    .filter((node) => node.parentId === null && state.nodeBankIds[node.id] === bankId)
    .sort((a, b) => a.sortOrder - b.sortOrder)
  return {
    bankId,
    bankName: bank?.name || '',
    sections: clone(sections),
    ungroupedQuestions: clone(state.questions.filter((question) => question.bankId === bankId && !question.groupCode)),
  }
}

export function buildSessionQuestions(state: DemoState, exam: Exam): ExamQuestion[] {
  return exam.paperQuestions
    .slice()
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((paperQuestion) => {
      const source = state.questions.find((question) => question.id === paperQuestion.questionId)
      return {
        questionId: paperQuestion.questionId,
        type: paperQuestion.type,
        stem: paperQuestion.stem,
        sectionCode: paperQuestion.sectionCode,
        sectionTitle: paperQuestion.sectionTitle,
        sectionSortOrder: paperQuestion.sectionSortOrder,
        groupCode: paperQuestion.groupCode,
        groupTitle: paperQuestion.groupTitle,
        groupDirection: paperQuestion.groupDirection,
        groupMaterial: paperQuestion.groupMaterial,
        groupSortOrder: paperQuestion.groupSortOrder,
        itemLabel: paperQuestion.itemLabel,
        itemStem: paperQuestion.itemStem,
        score: paperQuestion.score,
        sortOrder: paperQuestion.sortOrder,
        selectedLabels: [],
        answerText: null,
        attachments: source?.attachments ? clone(source.attachments) : [],
        options: source?.options.map(({ id, label, content, sortOrder }) => ({ id, label, content, sortOrder })) || [],
      }
    })
}

export function gradeSubmission(
  state: DemoState,
  exam: Exam,
  user: DemoUser,
  questions: ExamQuestion[],
  answers: Array<{ questionId: number; selectedLabels?: string[]; answerText?: string | null }>,
): ExamResultDetail {
  const answerMap = new Map(answers.map((answer) => [answer.questionId, answer]))
  let objectiveScore = 0
  let correctCount = 0
  const resultQuestions: ExamResultQuestion[] = questions.map((question) => {
    const answer = answerMap.get(question.questionId)
    const selectedLabels = answer?.selectedLabels || []
    const answerText = answer?.answerText || null
    const correctLabels = state.correctLabelsByQuestionId[question.questionId] || []
    const correct = isManualReviewType(question.type) ? null : sameLabels(selectedLabels, correctLabels)
    const obtainedScore = correct ? question.score : 0
    if (correct) {
      objectiveScore += obtainedScore
      correctCount += 1
    }
    return {
      ...question,
      analysis: state.questions.find((item) => item.id === question.questionId)?.analysis || null,
      obtainedScore,
      selectedLabels,
      answerText,
      correctLabels,
      correct,
      reviewComment: null,
      reviewerName: null,
      reviewedAt: null,
    }
  })
  const hasManualReview = resultQuestions.some((question) => isManualReviewType(question.type))
  const result: ExamResultDetail = {
    id: nextId(state),
    attemptId: nextId(state),
    examId: exam.id,
    examTitle: exam.title,
    userId: user.id,
    username: user.username,
    userName: user.displayName,
    departmentName: user.departmentName,
    totalScore: exam.totalScore,
    obtainedScore: objectiveScore,
    objectiveScore,
    subjectiveScore: 0,
    correctCount,
    questionCount: questions.length,
    gradingStatus: hasManualReview ? 'PENDING_REVIEW' : 'FINAL',
    passed: !hasManualReview && objectiveScore >= exam.qualifyScore,
    submittedAt: nowIso(),
    questions: resultQuestions,
  }
  return result
}

function sameLabels(left: string[], right: string[]) {
  if (left.length !== right.length) {
    return false
  }
  const leftSorted = [...left].sort()
  const rightSorted = [...right].sort()
  return leftSorted.every((label, index) => label === rightSorted[index])
}

export function updateResultTotals(result: ExamResultDetail, qualifyScore: number) {
  const objectiveScore = result.questions.filter((question) => !isManualReviewType(question.type)).reduce((sum, question) => sum + question.obtainedScore, 0)
  const subjectiveScore = result.questions.filter((question) => isManualReviewType(question.type)).reduce((sum, question) => sum + question.obtainedScore, 0)
  result.objectiveScore = objectiveScore
  result.subjectiveScore = subjectiveScore
  result.obtainedScore = objectiveScore + subjectiveScore
  result.correctCount = result.questions.filter((question) => question.correct).length
  result.passed = result.gradingStatus === 'FINAL' && result.obtainedScore >= qualifyScore
}

export function refreshBankCounts(state: DemoState) {
  for (const bank of state.banks) {
    const questions = state.questions.filter((question) => question.bankId === bank.id)
    bank.questionCount = questions.length
    bank.singleChoiceCount = questions.filter((question) => question.type === 'SINGLE_CHOICE').length
    bank.multipleChoiceCount = questions.filter((question) => question.type === 'MULTIPLE_CHOICE').length
    bank.writingCount = questions.filter((question) => isManualReviewType(question.type)).length
  }
}

export function currentDemoState() {
  return demoState
}

export { clone, nextId, nowIso }

const demoState = seedState()
