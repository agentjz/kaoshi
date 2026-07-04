import type { ExamQuestion, ExamResultQuestion, QuestionAttachment } from '@/api/exam-business'

export interface QuestionGroup<T extends ExamQuestion | ExamResultQuestion> {
  id: string
  sectionTitle: string
  title: string
  direction: string | null
  material: string | null
  attachments: QuestionAttachment[]
  sharedOptions: T['options']
  compactOptionItems: boolean
  questions: T[]
}

export function groupQuestionsForDisplay<T extends ExamQuestion | ExamResultQuestion>(questions: T[]) {
  const groups: QuestionGroup<T>[] = []
  for (const question of questions) {
    const groupId = [
      question.sectionCode || 'section',
      question.groupCode || `question-${question.questionId}`,
    ].join(':')
    const lastGroup = groups[groups.length - 1]
    if (!lastGroup || lastGroup.id !== groupId) {
      groups.push({
        id: groupId,
        sectionTitle: question.sectionTitle || '',
        title: question.groupTitle || question.sectionTitle || '',
        direction: question.groupDirection || null,
        material: question.groupMaterial || null,
        attachments: [],
        sharedOptions: [],
        compactOptionItems: false,
        questions: [],
      })
    }
    const currentGroup = groups[groups.length - 1]
    currentGroup.questions.push(question)
    currentGroup.attachments = uniqueAttachments([...currentGroup.attachments, ...question.attachments])
    currentGroup.compactOptionItems = isSharedOptionGroup(currentGroup.questions)
    currentGroup.sharedOptions = currentGroup.compactOptionItems ? sharedOptions(currentGroup.questions) : []
  }
  return groups
}

function uniqueAttachments(attachments: QuestionAttachment[]) {
  const seen = new Set<string>()
  return attachments.filter((attachment) => {
    const key = `${attachment.fileUrl}:${attachment.mediaType}`
    if (seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
}

function isSharedOptionGroup<T extends ExamQuestion | ExamResultQuestion>(questions: T[]) {
  if (questions.length === 0) {
    return false
  }
  const sharedTypes = new Set(['WORD_BANK', 'MATCHING'])
  if (!questions.every((question) => sharedTypes.has(question.type))) {
    return false
  }
  const first = optionSignature(questions[0].options)
  return first.length > 0 && questions.every((question) => optionSignature(question.options) === first)
}

function sharedOptions<T extends ExamQuestion | ExamResultQuestion>(questions: T[]) {
  return questions[0]?.options ?? []
}

function optionSignature(options: ExamQuestion['options']) {
  return options
    .map((option) => `${option.label}:${option.content}`)
    .join('|')
}
