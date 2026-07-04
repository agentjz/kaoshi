import type {
  NamedCategory,
  Question,
  QuestionAttachmentPayload,
  QuestionBank,
  QuestionPayload,
} from '@/api/exam-business'
import { questionTypeMeta } from '@/utils/question-types'

export interface BankTreeNode {
  key: string
  type: 'category' | 'bank'
  label: string
  children: BankTreeNode[]
  categoryId?: number
  bankId?: number
  status?: QuestionBank['status']
  questionCount: number
  singleChoiceCount: number
  multipleChoiceCount: number
  writingCount: number
}

export interface QuestionContentTreeGroup {
  key: string
  title: string
  direction: string | null
  material: string | null
  questionCount: number
  sharedOptionCount: number
}

export interface QuestionContentTreeSection {
  key: string
  title: string
  questionCount: number
  groups: QuestionContentTreeGroup[]
}

const imageUrlPattern = /\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/i

export function buildBankTree(categories: NamedCategory[], banks: QuestionBank[]): BankTreeNode[] {
  return categories.map((category) => ({
    key: `category-${category.id}`,
    type: 'category',
    label: category.name,
    categoryId: category.id,
    questionCount: 0,
    singleChoiceCount: 0,
    multipleChoiceCount: 0,
    writingCount: 0,
    children: banks
      .filter((bank) => bank.categoryId === category.id)
      .map((bank) => ({
        key: `bank-${bank.id}`,
        type: 'bank',
        label: bank.name,
        bankId: bank.id,
        status: bank.status,
        questionCount: bank.questionCount,
        singleChoiceCount: bank.singleChoiceCount,
        multipleChoiceCount: bank.multipleChoiceCount,
        writingCount: bank.writingCount,
        children: [],
      })),
  }))
}

export function createQuestionPayload(bankId: number): QuestionPayload {
  return {
    bankId,
    type: 'SINGLE_CHOICE',
    stem: '',
    analysis: '',
    difficulty: 'EASY',
    status: 'ACTIVE',
    options: defaultOptions(),
    attachments: [],
  }
}

export function buildQuestionContentTree(questions: Question[]): QuestionContentTreeSection[] {
  const sections: QuestionContentTreeSection[] = []
  for (const question of [...questions].sort(compareQuestionStructure)) {
    const sectionKey = question.sectionCode || `ungrouped-${question.bankId}`
    let section = sections.find((item) => item.key === sectionKey)
    if (!section) {
      section = {
        key: sectionKey,
        title: question.sectionTitle || '未分组试题',
        questionCount: 0,
        groups: [],
      }
      sections.push(section)
    }
    const groupKey = question.groupCode || `question-${question.id}`
    let group = section.groups.find((item) => item.key === groupKey)
    if (!group) {
      group = {
        key: groupKey,
        title: question.groupTitle || question.stem,
        direction: question.groupDirection || null,
        material: question.groupMaterial || null,
        questionCount: 0,
        sharedOptionCount: sharedOptionCount(question, questions),
      }
      section.groups.push(group)
    }
    section.questionCount += 1
    group.questionCount += 1
  }
  return sections
}

function compareQuestionStructure(left: Question, right: Question) {
  return (left.sectionSortOrder || 0) - (right.sectionSortOrder || 0)
    || (left.groupSortOrder || 0) - (right.groupSortOrder || 0)
    || left.id - right.id
}

function sharedOptionCount(question: Question, questions: Question[]) {
  if (!['WORD_BANK', 'MATCHING'].includes(question.type)) {
    return 0
  }
  const groupQuestions = questions.filter((item) => item.groupCode && item.groupCode === question.groupCode)
  if (groupQuestions.length === 0) {
    return 0
  }
  const signature = optionSignature(groupQuestions[0])
  return groupQuestions.every((item) => optionSignature(item) === signature) ? groupQuestions[0].options.length : 0
}

function optionSignature(question: Question) {
  return question.options.map((option) => `${option.label}:${option.content}`).join('|')
}

export function questionToPayload(question: Question): QuestionPayload {
  return {
    bankId: question.bankId,
    type: question.type,
    stem: question.stem,
    sectionCode: question.sectionCode,
    sectionTitle: question.sectionTitle,
    sectionSortOrder: question.sectionSortOrder,
    groupCode: question.groupCode,
    groupTitle: question.groupTitle,
    groupDirection: question.groupDirection,
    groupMaterial: question.groupMaterial,
    groupSortOrder: question.groupSortOrder,
    itemLabel: question.itemLabel,
    itemStem: question.itemStem,
    analysis: question.analysis || '',
    difficulty: question.difficulty,
    status: question.status,
    options: question.options.map((option) => ({ label: option.label, content: option.content, correct: option.correct })),
    attachments: question.attachments.map((attachment) => ({
      fileName: attachment.fileName,
      fileUrl: attachment.fileUrl,
      mediaType: attachment.mediaType,
    })),
  }
}

export function normalizeOptionsForType(form: QuestionPayload) {
  if (!questionTypeMeta(form.type).optionBased) {
    form.options = []
    return
  }
  if (form.options.length === 0) {
    form.options = defaultOptions()
  }
}

export function questionOptionError(form: QuestionPayload) {
  if (!questionTypeMeta(form.type).optionBased) {
    return null
  }
  const correctCount = form.options.filter((option) => option.correct).length
  const meta = questionTypeMeta(form.type)
  if (meta.optionBased && !meta.multiple && correctCount !== 1) {
    return `${meta.label}必须且只能有一个正确答案`
  }
  if (meta.multiple && correctCount < 2) {
    return '多选题至少需要两个正确答案'
  }
  return null
}

export function mediaTypeText(type: QuestionAttachmentPayload['mediaType']) {
  const names: Record<QuestionAttachmentPayload['mediaType'], string> = {
    IMAGE: '图片',
    AUDIO: '音频',
    VIDEO: '视频',
    FILE: '文件',
  }
  return names[type]
}

export function isImageAttachment(attachment: QuestionAttachmentPayload) {
  return attachment.mediaType === 'IMAGE' || imageUrlPattern.test(attachment.fileUrl)
}

export function inferMediaType(url: string, fallback: QuestionAttachmentPayload['mediaType']) {
  if (imageUrlPattern.test(url)) {
    return 'IMAGE'
  }
  return fallback
}

export function nextOptionLabel(optionCount: number) {
  return String.fromCharCode(65 + optionCount)
}

function defaultOptions() {
  return [
    { label: 'A', content: '', correct: true },
    { label: 'B', content: '', correct: false },
  ]
}
